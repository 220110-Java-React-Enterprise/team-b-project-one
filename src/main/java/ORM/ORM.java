package ORM;

import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;
import Persistence.FieldType;
import Util.ConnectionManager;
import org.mariadb.jdbc.Driver;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Hashtable;
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
        }


    }
    private void getTableFormat(Object obj){
        // check if object has annotations, if so use to parse
        // else parse in a different way.

//        String tableName;
//        String[] columns= new String[2*obj.getClass().getDeclaredFields().length];
//        String primaryKey = "";
//        String primaryKeyDataType = "";
//        String[] foreignKey= new String[4*obj.getClass().getDeclaredFields().length];


        // Initial class (top level) name
        if (obj.getClass().isAnnotationPresent(Table.class)) {

            Annotation a = obj.getClass().getAnnotation(Table.class);
            Table annotation = (Table) a;
            tableName = annotation.name();

        }else{
            tableName = obj.getClass().getCanonicalName();

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

//           if (!checkIfTableExists(tableName.toLowerCase(Locale.ROOT))){
//                createTable(tableName,primaryKey,primaryKeyDataType,columns,foreignKey);
//           }


    }

    private void createTable(){
        String sql = "CREATE TABLE " + tableName.toLowerCase(Locale.ROOT);
        String sqlColumns = " ( ";


        if ((primaryKey != null && primaryKey != "") && (primaryKeyType!= null && primaryKeyType != "")){
            if (typeConversion(primaryKeyType).equals(FieldType.INT.toString())) {
                sqlColumns += primaryKey + " " + typeConversion(primaryKeyType) + " AUTO_INCREMENT PRIMARY KEY";
            }else{
                sqlColumns += primaryKey + " " + typeConversion(primaryKeyType)  + " PRIMARY KEY UNIQUE NOT NULL";
            }
            System.out.println("\n" + sql + sqlColumns);
        }else {
            sqlColumns += primaryKey + " " + typeConversion(primaryKeyType);

        }

        int iterator = 0;
        while (foreignKeyArray[iterator] != null){
            sqlColumns += ", " + foreignKeyArray[iterator] + " " + typeConversion(foreignKeyArray[iterator+1]);
            iterator += 4;
        }
        iterator = 0;
        while (columnNameArray[iterator] != null){
            sqlColumns += ", " + columnNameArray[iterator] + " " + typeConversion(columnNameArray[iterator+1]);
            iterator += 2;
        }

        if (foreignKeyArray[0] != null) {

            iterator = 0;
            while (foreignKeyArray[iterator] != null && checkIfTableExists(foreignKeyArray[iterator+2])){

                sqlColumns += ", FOREIGN KEY ( " + foreignKeyArray[iterator] +
                              " ) REFERENCES " + foreignKeyArray[iterator+2] +
                              "( " + foreignKeyArray[iterator+3] + " )";
                iterator += 4;
            }
            sqlColumns = sqlColumns + " ) ";
        }else{
            sqlColumns = sqlColumns + " ) ";
        }

        System.out.println("\n" + sql + sqlColumns);

        try {
            sql = sql + sqlColumns;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }


    }

    public void insertToTable(Object obj) {
        // i can retrieve all values in the attribute section,
        // its stored onto an array and i can use instance of to check
        // datatype
        // I should create a function that checks instance type to call
        // the appropriate prepared statement type.
        
        Object[] values = new Object[obj.getClass().getDeclaredFields().length];
        int iterator = 0;

        for (Field field : obj.getClass().getDeclaredFields()){
            //NoSuchFieldException
            field.setAccessible(true);

            try {
                values[iterator] = field.get(obj);
                System.out.print(field.getType().getTypeName().replaceFirst("java.lang.", "") + " ");
                System.out.print(values[iterator]);
                System.out.println(values[iterator]);
                iterator++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


//        String sql = "INSERT INTO "+ tableName + " (";
//
//        for (int i =0; i <  obj.getClass().getDeclaredFields().length; i++){
//
//        }
//
//
//
//        int iterator = 0;
//        while (foreignKeyArray[iterator] != null){
//            sql += ", ? ?";
//            iterator += 4;
//        }
//        iterator = 0;
//        while (columnNameArray[iterator] != null){
//            sql += ", ? ?";
//            iterator += 2;
//        }
    }

    private String typeConversion(String type){
        switch (type){
            case "String":
            case "string":
                return FieldType.VARCHAR.name()+ "(255)";
            case "Character":
            case "char":
                return FieldType.CHAR.name();
            case "Integer":
            case "int":
                return FieldType.INT.name();
            case "Float":
            case "float":
                return FieldType.DECIMAL.name();
            case "Boolean":
            case "boolean":
                return FieldType.BOOLEAN.name();
            case "Enum":
                return FieldType.ENUM.name();
        }return null;
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
