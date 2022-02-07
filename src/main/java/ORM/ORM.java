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
import java.util.Locale;

public class ORM {

    private String tableName;
    private String primaryKey;
    private String primaryKeyType;
    private String[] columnNameArray;
    private String[] foreignKeyArray;

    private Connection connection = ConnectionManager.getConnection();
    private String dbName = "ProjectOne";


    public  ORM(){

    }




    public void ORM(Object obj, String action){
        columnNameArray = new String[2*obj.getClass().getDeclaredFields().length];
        foreignKeyArray = new String[4*obj.getClass().getDeclaredFields().length];
        getTableFormat(obj);

        switch (action){
            case "create":
                createTable();
            case "insert":
                insertToTable(obj);
            case "delete":
                deleteFromTable(obj);
        }


    }
    private void getTableFormat(Object obj){
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

    /**
     * This function enters an entire row (except id) into a table.
     * I need to take care of an occasion when user is entering a row to a table with a foreign key
     * constraint. There could be a situation where a value is inserted into the fk column
     * but the value does not match any value in the table that the key references.
     *
     */


    // insert entire object to table
    public void insertToTable(Object obj) {
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

        for (int i =0; i < colNameAndValue.length; i++){
            for(int j = 0; j < colNameAndValue[0].length; j++){
                System.out.print(colNameAndValue[i][j] + " ");
                if ( j == 1) {
                    System.out.println(" " + ((Object) colNameAndValue[i][j]).getClass().getSimpleName());
                }
            }
            System.out.println("\n");
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
            System.out.println(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }else{
        System.out.println("Table with that name does not exist, maybe try creating one first.");
    }


    }

    /**
     * Function accepts an object and uses its pk value to delete an entire row from the table
     * it belongs to.
     * @param obj
     */
    private void deleteFromTable(Object obj){
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
                return;

            } else if (pkValue instanceof Integer && (Integer) pkValue != 0) {

                sql = "DELETE FROM " + tableName + " WHERE " + pkColumnName + " = ?";
                PreparedStatement pkDeleteStatement = connection.prepareStatement(sql);
                pkDeleteStatement.setInt(1, (Integer) pkValue);
                pkDeleteStatement.executeUpdate();

            } else {

                System.out.println("Unable to delete object without a primary key.");

            }

            } catch(SQLException | IllegalAccessException e){
                e.printStackTrace();
            }

        }


    private String typeConversion(String type, Boolean reverse){
        if (reverse == null){
            reverse = false;
        }
        if (!reverse) {
            switch (type) {
                case "String":
                case "string":
                    return FieldType.VARCHAR.name() + "(255)";
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }



}
