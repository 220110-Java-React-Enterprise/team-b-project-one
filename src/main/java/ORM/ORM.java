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
        System.out.println(obj.getClass().isAnnotationPresent(Table.class));
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
            System.out.print(foreignKeyArray[i] + " ");
        }
        System.out.println("\n");
        for (int i = 0; i < columnNameArray.length; i++){
            if (columnNameArray[i] == null) {break;}
            System.out.print(columnNameArray[i] + " ");
        }
        System.out.println("\n");


//           if (!checkIfTableExists(tableName.toLowerCase(Locale.ROOT))){
//                createTable(tableName,primaryKey,primaryKeyDataType,columns,foreignKey);
//           }


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
                System.out.println("\n" + sql + sqlColumns);
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

            System.out.println("\n" + sql + sqlColumns);

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
        System.out.println("Insert to table " + tableName);
    if (checkIfTableExists(tableName)){
        for (Field field : obj.getClass().getDeclaredFields()){
            // Sets private fields accessible which allow me to retrieve info.
            try {
                field.setAccessible(true);
                colNameAndValue[iterator][0] = field.getName();
                colNameAndValue[iterator][1] = field.get(obj);
                System.out.println("Inside first field for loop "+field.getName() + " " + field.get(obj));
                iterator++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

//        System.out.println("After table name exist:");
//        for (int i =0; i < colNameAndValue.length; i++){
//            System.out.println("inside first for loop");
//            for(int j = 0; j < colNameAndValue[0].length; j++){
//                System.out.println("inside second for loop");
//                System.out.print(colNameAndValue[i][j] + " ");
//                if ( j == 1) {
//                    System.out.println("inside if statement");
//                    System.out.println(" " + colNameAndValue[i][j].getClass().getSimpleName());
//                }
//            }
//            System.out.println("\n");
//        }

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
        System.out.println(sql);
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
            System.out.println(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }else{
        System.out.println("Table name " + tableName +" name does not exist, maybe try creating one first.");
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

                System.out.println("your pk field does not match the pk field in table.");

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
                                System.out.println("Problem trying to get value for search query.");
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
                            System.out.println("bool switch");
                            statement.setBoolean(1, (Integer) pkValue > 0);
                            break;
                        }

                    System.out.println(statement);
                    ResultSet result = statement.executeQuery();
                    result.next();
                    iterator = 0;
                    Object obj2 = obj;

                    for (Field field : obj.getClass().getDeclaredFields()){
                        field.setAccessible(true);
                        String currentDataType = field.getType().getTypeName().replaceFirst("java.lang.", "");
                        System.out.println(currentDataType);
                        switch (currentDataType){
                            case "Integer":
                            case "int":
                                System.out.print(field.getName() + " " + result.getInt(field.getName()) + "\n");
                                field.set(obj2,result.getInt(field.getName()));
                                continue;
                            case "String":
                            case "string":
                            case "Enum":
                            case "enum":
                            case "Character":
                            case "char":
                                System.out.println(field.getName());
                                System.out.println(result.getString(field.getName()));
                                System.out.print(field.getName() + " " + result.getString(field.getName()) + "\n");
                                field.set(obj2,result.getString(field.getName()));
                                continue;
                            case "Float":
                            case "float":
                            case "double":
                                System.out.print(field.getName() + " " + result.getFloat(field.getName()) + "\n");
                                field.set(obj2, result.getFloat(field.getName()));
                                continue;
                            case "Boolean":
                            case "boolean":
                                System.out.print(field.getName() + " " + result.getBoolean(field.getName()) + "\n");
                                field.set(obj2, result.getBoolean(field.getName()));
                        }
                        iterator++;
                        System.out.println("np2");
                        if (iterator == colNameAndDataType.length){
                            System.out.println("np3");
                            break;}
                    }


                    return obj2;

                    } catch(SQLException | IllegalAccessException e){
                        e.printStackTrace();
                    }


            }else{
                System.out.println("Unable to retrieve object from table.");
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

                System.out.println(sql);

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

                    System.out.println(statement);
                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }else{
                System.out.println("Table with that name does not exist, maybe try creating one first.");
            }


        }
    /** Method retrieves all data pertaining to user.
     * It will return a linked list of objects of the same type that it was given.
     * The first object fields will be used to retrieve data.
     *      Input - Object list of all objects who have tables in database and contain a primary key.
     *      Output - Linked list with all information pertaining to user.
     * ***************************************************************************************************************/
    private List<Object>getAllFromList(List<Object> obj){
        List<Object> result = new LinkedList<>();
        return result;
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
        System.out.println("check if table exist: " + tableName);
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
                System.out.println("Table with name: " + resultSet.getString("TABLE_NAME")+
                                   " already exists in database.");
                return true;
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("sql exception");
            e.printStackTrace();
            return false;
        }
        return false;
    }



}
