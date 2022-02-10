package ORM;
import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;
import Persistence.FieldType;
import Util.ConnectionManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ORM {

    private String tableName;
    private String primaryKey;
    private String primaryKeyType;
    private String[] columnNameArray;
    private String[] foreignKeyArray;
    private List<Object> list;
    private List<Object> resultList;
    private Connection connection;
    private String dbName;


    public  ORM(){

    }

    public void connect(String hostname, String port, String DBname, String userName, String password){
        dbName = DBname;
        connection = ConnectionManager.getConnection(hostname, port, DBname, userName, password);
    }


    public Object ormEntry(Object obj, String action){
    /** Entry point of program, this function is in charge of
     * choosing between methods based on input given.
     *      Input - User object and action string which consists of (create, insert, delete, search, update)
     *      Output - None.
     * ***************************************************************************************************************/
        columnNameArray = new String[2*obj.getClass().getDeclaredFields().length];
        foreignKeyArray = new String[4*obj.getClass().getDeclaredFields().length];
        getTableFormat(obj);
        Object result = null;

        switch (action){
            case "create":
                createTable();
                break;
            case "insert":
                insertToTable(obj);
                break;
            case "delete":
                deleteFromTable(obj);
                break;
            case "search":
                result = getFromTable(obj);
                break;
            case "update":
                updateTable(obj);
                break;
            case "getall":
                getAllPertainingToUser(obj);
                break;
            case "load":
                break;
        }

        return result;
    }


    private void getTableFormat(Object obj){
    /** This method is in charge of updating the fields that are within this class. This method is invoked first before any other.
     * Reflection is used to retrieve field information of the given object and is placed into the fields within this class.
     *      Input - User object
     *      Output - None.
     * ***************************************************************************************************************/
        // check if object has annotations, if so use to parse
        // else parse in a different way.

        // Initial class (top level) name
        if (obj.getClass().isAnnotationPresent(Table.class)) {

            Annotation a = obj.getClass().getAnnotation(Table.class);
            Table annotation = (Table) a;
            tableName = annotation.name().toLowerCase(Locale.ROOT);

        }else{

            tableName = obj.getClass().getCanonicalName().toLowerCase(Locale.ROOT);

        }
        // Class attributes (fields) are iterated through.
        // Depending on annotation, it is saved in the proper location
        // Arrays in the following loop are formed the following
        // way: [field1 name, field1 datatype, field2 name, field2 datatype,...]
        // revisit to update foreign key array layout

        int fkIterator = 0;
        int colIterator = 0;

        for (Field field : obj.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(PrimaryKey.class)){
                primaryKey = field.getAnnotation(PrimaryKey.class).name();
                primaryKeyType = field.getAnnotation(PrimaryKey.class).type();
            }else if (field.isAnnotationPresent(ForeignKey.class)) {
                foreignKeyArray[fkIterator] = field.getAnnotation(ForeignKey.class).columnName();
                foreignKeyArray[fkIterator+1] = field.getAnnotation(ForeignKey.class).type();
                foreignKeyArray[fkIterator+2] = field.getAnnotation(ForeignKey.class).referenceTableName();
                foreignKeyArray[fkIterator+3] = field.getAnnotation(ForeignKey.class).referenceTableColumn();
                fkIterator += 4;
            }else if (field.isAnnotationPresent(Column.class)){
                columnNameArray[colIterator] = field.getAnnotation(Column.class).name();
                columnNameArray[colIterator+1] = field.getAnnotation(Column.class).type();
                colIterator+=2;
            }else{
                columnNameArray[colIterator] = field.getName();
                columnNameArray[colIterator+1] = field.getGenericType().getTypeName().replaceFirst("java.lang.", "");
                colIterator+=2;
            }


        }

        for (int i = 0; i < foreignKeyArray.length; i++){
            if (foreignKeyArray[i] == null) {break;}
        }

        for (int i = 0; i < columnNameArray.length; i++){
            if (columnNameArray[i] == null) {break;}
        }



    }


    private void createTable(){
        /** Creates a table in the database using the information retrieved by the "getTableFormat" method.
         * Primary and foreign key constraints are set here.
         *      Input - None. The variables initialized by the getTableFormat method are used to create table.
         *      Output - None.
         * ***************************************************************************************************************/
        if (!checkIfTableExists(tableName)) {
            String sql = "CREATE TABLE " + tableName.toLowerCase(Locale.ROOT);
            String sqlColumns = " ( ";


            if ((primaryKey != null && primaryKey != "") && (primaryKeyType != null && primaryKeyType != "")) {
                if (typeConversion(primaryKeyType, false).equals(FieldType.INT.toString())) {
                    sqlColumns += primaryKey + " " + typeConversion(primaryKeyType, false) + " AUTO_INCREMENT PRIMARY KEY";
                } else {
                    sqlColumns += primaryKey + " " + typeConversion(primaryKeyType, false) + " PRIMARY KEY UNIQUE NOT NULL";
                }

            } else {
                sqlColumns += primaryKey + " " + typeConversion(primaryKeyType, false);

            }

            int iterator = 0;
            while (foreignKeyArray[iterator] != null) {
                sqlColumns += ", " + foreignKeyArray[iterator] + " " + typeConversion(foreignKeyArray[iterator + 1], false);
                iterator += 4;
            }
            iterator = 0;
            while (columnNameArray[iterator] != null) {
                sqlColumns += ", " + columnNameArray[iterator] + " " + typeConversion(columnNameArray[iterator + 1], false);
                iterator += 2;
            }

            if (foreignKeyArray[0] != null) {

                iterator = 0;
                while (foreignKeyArray[iterator] != null && checkIfTableExists(foreignKeyArray[iterator + 2])) {

                    sqlColumns += ", FOREIGN KEY ( " + foreignKeyArray[iterator] +
                            " ) REFERENCES " + foreignKeyArray[iterator + 2] +
                            "( " + foreignKeyArray[iterator + 3] + " )";
                    iterator += 4;
                }
                sqlColumns = sqlColumns + " ) ";
            } else {
                sqlColumns = sqlColumns + " ) ";
            }

            try {
                sql = sql + sqlColumns;
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }




    // insert entire object to table
    public void insertToTable(Object obj) {
    /** This method inserts a new entry into an existing table.
     *      Input - User object
     *      Output - None.
     *****************************************************************************************************************/
    Object[][] colNameAndValue = new Object[obj.getClass().getDeclaredFields().length][2];

    int iterator = 0;
    if (checkIfTableExists(tableName)){
        for (Field field : obj.getClass().getDeclaredFields()){
            // Sets private fields accessible which allow me to retrieve info.
            try {
                field.setAccessible(true);
                colNameAndValue[iterator][0] = field.getName();
                colNameAndValue[iterator][1] = field.get(obj);
                iterator++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        String sql = "INSERT INTO " + tableName + "( ";
        for (int i =1; i < colNameAndValue.length; i++){
            if ((i+1 ) == colNameAndValue.length){
                sql += colNameAndValue[i][0] + " )";
            }else {
                sql += colNameAndValue[i][0] + ",";
            }
        }

        sql += " VALUES ( ";
        for (int i =1; i < colNameAndValue.length; i++){
            if ((i+1 ) == colNameAndValue.length){
                sql += "?" + " )";
            }else {
                sql += "?" + ",";
            }
        }

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 1; i < colNameAndValue.length; i++){

                switch (colNameAndValue[i][1].getClass().getTypeName().replaceFirst("java.lang.", "")) {
                    case "String":
                    case "string":
                    case "Enum":
                    case "enum":

                        statement.setString(i, colNameAndValue[i][1].toString());
                        continue;
                    case "Character":
                    case "char":
                        statement.setString(i, String.valueOf(colNameAndValue[i][1]));
                        continue;
                    case "Integer":
                    case "int":
                        statement.setInt(i, ((int) colNameAndValue[i][1]));
                        continue;
                    case "Float":
                    case "float":
                    case "double":
                        statement.setFloat(i, ((float) colNameAndValue[i][1]));
                        continue;
                    case "Boolean":
                    case "boolean":
                        statement.setBoolean(i, ((boolean) colNameAndValue[i][1]));
                }

            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }else{
    }


    }

    private void deleteFromTable(Object obj) {
        /** Method deletes a row from a table that matches the object given unique id.
         *      Input - User object
         *      Output - None.
         * ***************************************************************************************************************/
        String sql = "SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'";
        Object pkValue = 0;
        String pkColumnName = "";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            result.next();
            pkColumnName = result.getString("Column_name");

            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.getName().equals(pkColumnName)) {
                    field.setAccessible(true);
                    pkValue = field.get(obj);
                    break;
                }
            }

            if (!primaryKey.equals(pkColumnName)) {

            } else if (pkValue instanceof Integer && (Integer) pkValue != 0) {

                sql = "DELETE FROM " + tableName + " WHERE " + pkColumnName + " = ?";
                PreparedStatement pkDeleteStatement = connection.prepareStatement(sql);
                pkDeleteStatement.setInt(1, (Integer) pkValue);
                pkDeleteStatement.executeUpdate();

            } else {

                switch (pkValue.getClass().getTypeName().replaceFirst("java.lang.", "")) {
                    case "String":
                    case "string":
                    case "Enum":
                    case "enum":
                        statement.setString(1, pkValue.toString());
                    case "Character":
                    case "char":
                        statement.setString(1, String.valueOf(pkValue));
                    case "Float":
                    case "float":
                    case "double":
                        statement.setFloat(1, ((float) pkValue));
                    case "Boolean":
                    case "boolean":
                        statement.setBoolean(1, ((boolean) pkValue));

                }
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

        private Object getFromTable(Object obj) {
            /** Retrieves a row from a table given object with a unique id.
             * After retrieving, it creates a new reference to the object given and updates its information for return.
             *      Input - User object with a unique identifier.
             *      Output - object with fields updated with values retrieved from table.
             * ***************************************************************************************************************/
            Object[][] colNameAndDataType = new Object[obj.getClass().getDeclaredFields().length][2];
            Object pkValue = null;
            int iterator = 0;
            if (checkIfTableExists(tableName)) {
                for (Field field : obj.getClass().getDeclaredFields()) {
                    // Sets private fields accessible which allow me to retrieve info.
                        field.setAccessible(true);
                        colNameAndDataType[iterator][0] = field.getName();
                        if (field.getName().equals(primaryKey)){
                            try {
                                pkValue = field.get(obj);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        iterator++;
                }

                String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";
                try {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    switch (pkValue.getClass().getTypeName().replaceFirst("java.lang.", "")) {
                        case "Integer":
                        case "int":
                            statement.setInt(1, (int)pkValue);
                            break;
                        case "String":
                        case "string":
                        case "Enum":
                        case "enum":
                            statement.setString(1, pkValue.toString());
                            break;
                        case "Character":
                        case "char":
                            statement.setString(1, String.valueOf(pkValue));
                            break;
                        case "Float":
                        case "float":
                        case "double":
                            statement.setFloat(1, ((float) ((Integer)pkValue/1.0f)));
                            break;
                        case "Boolean":
                        case "boolean":
                            statement.setBoolean(1, (Integer) pkValue > 0);
                            break;
                        }

                    ResultSet result = statement.executeQuery();
                    result.next();
                    iterator = 0;
                    Object obj2 = obj;

                    for (Field field : obj.getClass().getDeclaredFields()){
                        field.setAccessible(true);
                        String currentDataType = field.getType().getTypeName().replaceFirst("java.lang.", "");
                        switch (currentDataType){
                            case "Integer":
                            case "int":
                                field.set(obj2,result.getInt(field.getName()));
                                continue;
                            case "String":
                            case "string":
                            case "Enum":
                            case "enum":
                            case "Character":
                            case "char":
                                field.set(obj2,result.getString(field.getName()));
                                continue;
                            case "Float":
                            case "float":
                            case "double":
                                field.set(obj2, result.getFloat(field.getName()));
                                continue;
                            case "Boolean":
                            case "boolean":
                                field.set(obj2, result.getBoolean(field.getName()));
                        }
                        iterator++;
                        if (iterator == colNameAndDataType.length){
                            break;}
                    }


                    return obj2;

                    } catch(SQLException | IllegalAccessException e){
                        e.printStackTrace();
                    }


            }else{
            }
            return obj;
        }

        private void updateTable(Object obj) {
            /** Methods updates a row from a table given an object with a unique identifier.
             * Inserts into the row all columns(fields) from object regardless if there was change.
             *      Input - User object with a unique identifier
             *      Output - None.
             * ***************************************************************************************************************/
            Object[][] colNameAndValue = new Object[obj.getClass().getDeclaredFields().length][2];

            int iterator = 0;
            Object pkValue = null;
            if (checkIfTableExists(tableName)){
                for (Field field : obj.getClass().getDeclaredFields()){
                    // Sets private fields accessible which allow me to retrieve info.
                    try {
                        field.setAccessible(true);
                        colNameAndValue[iterator][0] = field.getName();
                        colNameAndValue[iterator][1] = field.get(obj);

                        if (field.getName().equals(primaryKey)){
                                pkValue = field.get(obj);
                            }


                        iterator++;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                String sql = " UPDATE " + tableName + " SET ";
                for (int i =1; i < colNameAndValue.length; i++){
                    if ((i+1 ) == colNameAndValue.length){
                        sql += colNameAndValue[i][0] + "=? ";
                    }else {
                        sql += colNameAndValue[i][0] + "=?, ";
                    }
                }

                sql += " WHERE " + primaryKey + "= ?";


                try {
                    PreparedStatement statement = connection.prepareStatement(sql);

                    for (int i = 0; i < colNameAndValue.length; i++){

                        switch (colNameAndValue[i][1].getClass().getTypeName().replaceFirst("java.lang.", "")) {
                            case "String":
                            case "string":
                            case "Enum":
                            case "enum":
                                if (i==0){ statement.setString(colNameAndValue.length, pkValue.toString());}
                                else {statement.setString(i, colNameAndValue[i][1].toString());}
                                continue;
                            case "Character":
                            case "char":
                                if (i==0){statement.setString(colNameAndValue.length,String.valueOf(pkValue.toString()));}
                                else {statement.setString(i, String.valueOf(colNameAndValue[i][1]));}
                                continue;
                            case "Integer":
                            case "int":
                                if (i==0){statement.setInt(colNameAndValue.length, ((int) pkValue));}
                                else{statement.setInt(i, ((int) colNameAndValue[i][1]));}
                                continue;
                            case "Float":
                            case "float":
                            case "double":
                                if(i==0){statement.setFloat(colNameAndValue.length, ((float) pkValue));}
                                else{statement.setFloat(i, ((float) colNameAndValue[i][1]));}
                                continue;
                            case "Boolean":
                            case "boolean":
                                if(i==0){statement.setBoolean(colNameAndValue.length, ((boolean) pkValue));}
                                else{statement.setBoolean(i, ((boolean) colNameAndValue[i][1]));};
                        }

                    }

                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }else{
            }


        }

    private String typeConversion(String type, Boolean reverse){
        /** Method switches between java data types and sql types.
         * By default, it does switches from java to sql types. The opposite can be achieved
         * by setting reverse boolean to true.
         * (i.e java.String -> sql.VARCHAR or sql.VARCHAR -> java.String)
         *      Input - String value of the data type, and a boolean value to control which type of switching to conduct.
         *      Output - String of the equivalent datatype of the opposing language.
         * ***************************************************************************************************************/
        if (reverse == null){
            reverse = false;
        }
        if (!reverse) {
            switch (type) {
                case "String":
                case "string":
                    return FieldType.VARCHAR.name() + "(200)";
                case "Character":
                case "char":
                    return FieldType.CHAR.name();
                case "Integer":
                case "int":
                    return FieldType.INT.name();
                case "Float":
                case "float":
                case "double":
                    return FieldType.DECIMAL.name();
                case "Boolean":
                case "boolean":
                    return FieldType.BOOLEAN.name();
                case "Enum":
                    return FieldType.ENUM.name();
            }
            return null;
        } else{
            switch (type) {
                case "varchar":
                    return "String";
                case "char":
                    return "Character";
                case "int":
                    return "Integer";
                case "decimal":
                    return "Float";
                case "boolean":
                case "tinyint":
                    return "Boolean";
                case "enum":
                    return "Enum";
            }
            return null;
        }
    }
    // returns true if exists, false otherwise.


    private boolean checkIfTableExists(String tableName){
        /** Checks if table exists in database.
         *      Input - Table name string.
         *      Output - boolean value
         * ***************************************************************************************************************/
        String sql = "SELECT * \n" +
                     "FROM information_schema.TABLES t \n" +
                      "WHERE TABLE_SCHEMA = ? \n" +
                      "AND TABLE_NAME = ? \n" +
                      "LIMIT 1";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1,dbName);
            statement.setString(2, tableName);
            statement.executeUpdate();
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()){
                return true;
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void objectTypes(List<Object> obj){
        /** Method is in charge of receiving all the different object types that are in the database.
         * These object types are used to assign the values retrieved from DB into their member variables.
         * They are then added to a linked list. This is done in the packObjectsToList method.
         *      Input - Object list of all objects who have tables in database.
         *      Output - Class member variable list is assigned the value given by user.
         * ***************************************************************************************************************/
        this.list = obj;
    }
    private void packObjectsToList(ResultSet results){
        /** Method is responsible for parsing the ResultSet and assigning the values
         * to the corresponding Object's fields. It then appends the object to a linked list
         * for later use.
         *      Input - ResultSet
         *      Output - Class member variable resultSet is assigned the ending value of this meethod.
         * ***************************************************************************************************************/
            List<Object> finalList = new LinkedList<>();

        try {
            while (results.next()) {
                for (Object obj : list) {
                    Object tempObject = obj;
                    for (Field field : obj.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        String currentDataType = field.getType().getTypeName().replaceFirst("java.lang.", "");

                        switch (currentDataType) {
                            case "Integer":
                            case "int":

                                field.set(tempObject, results.getInt(field.getName()));
                                continue;
                            case "String":
                            case "string":
                            case "Enum":
                            case "enum":
                            case "Character":
                            case "char":
                                field.set(tempObject, results.getString(field.getName()));
                                continue;
                            case "Float":
                            case "float":
                            case "double":

                                field.set(tempObject, results.getFloat(field.getName()));
                                continue;
                            case "Boolean":
                            case "boolean":

                                field.set(tempObject, results.getBoolean(field.getName()));
                        }
                    }
                    finalList.add(tempObject);
                }

            }

        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
            resultList = finalList;
    }
    public List<Object> getAllObjects(){
        /** Method returns all objects pertaining to a specific object given by the user.
         *      Input - None, class variable is holding the information needed.
         *      Output - Linked list with all objects.
         * ***************************************************************************************************************/
        return resultList;
    }
    public void getAllPertainingToUser(Object obj){
        /** Method retrieves all data pertaining to user by identifying the schema of the
         * DB and retrieving all tables who have a foreign key that references the table that
         * the object given is stored. Iteration over the foreign key column is done to identify the
         * index of the table name that hold a fk to the pk of our object. This information is then used to build
         * our sql string which allow for multiple JOIN statements.
         * It will return a ResultSet which will be handed off to a method that will
         * further parse the data and bring it together into an Object linked list.
         *      Input - Object which is considered to be the object by which all other objects will be queried.
         *      Output - ResultSet
         * ***************************************************************************************************************/
        List<String> tableName = new LinkedList<String>();
        List<String> fkColumn = new LinkedList<String>();
        List<String> referenceTableName = new LinkedList<String>();
        List<String> referenceTableColumnName = new LinkedList<String>();

        String schema = "SELECT `TABLE_SCHEMA`," +
                         "`TABLE_NAME`,`COLUMN_NAME`," +
                        "`REFERENCED_TABLE_SCHEMA`," +
                        "`REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` " +
                 "FROM`INFORMATION_SCHEMA`.`KEY_COLUMN_USAGE`" +
                "WHERE `TABLE_SCHEMA` = SCHEMA() " +
                "AND `REFERENCED_TABLE_NAME` IS NOT NULL; ";
        try{
            PreparedStatement statement = connection.prepareStatement(schema);
            ResultSet results = statement.executeQuery();
            while (results.next()){
                tableName.add(results.getString("TABLE_NAME"));
                fkColumn.add(results.getString("COLUMN_NAME"));
                referenceTableName.add(results.getString("REFERENCED_TABLE_NAME"));
                referenceTableColumnName.add(results.getString("REFERENCED_COLUMN_NAME"));
            }
            String customerData = "SELECT * FROM ";

            int columnPKLocationInList = 0;

                for(int i =0; i < referenceTableName.size();i++){
                    if (referenceTableColumnName.get(i).equals(primaryKey)){
                        columnPKLocationInList = i;
                        break;
                    }
                }
                customerData += tableName.get(columnPKLocationInList);
                for (int i = columnPKLocationInList; i < referenceTableName.size(); i++){
                    if (tableName.get(i).equals(tableName.get(columnPKLocationInList))){
                        customerData += " INNER JOIN " + referenceTableName.get(i) +
                                        " ON " + tableName.get(i) + "." + fkColumn.get(i) + " = " +
                                        referenceTableName.get(i) + "." + referenceTableColumnName.get(i) + " ";

                    }
                }
            Object pkValue = "";
                for (Field field : obj.getClass().getDeclaredFields()){
                    field.setAccessible(true);
                    if (field.getName().equals(primaryKey)){
                        pkValue = field.get(obj);
                    }
                }
                customerData += "WHERE " + this.tableName + "." + this.primaryKey + " = ?";
                statement = connection.prepareStatement(customerData);

                switch (primaryKeyType) {
                case "String":
                case "string":
                case "Enum":
                case "enum":
                case "Character":
                case "char":
                    statement.setString(1, (String) pkValue);
                   break;
                case "Integer":
                case "int":
                    statement.setInt(1,(Integer) pkValue);
                    break;
                case "Float":
                case "float":
                case "double":
                    statement.setFloat(1, (Float) pkValue);
                    break;
            }


            results = statement.executeQuery();
                packObjectsToList(results);



        }catch (SQLException | IllegalAccessException e){
            e.printStackTrace();
        }

    }

}
