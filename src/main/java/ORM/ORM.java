package ORM;

import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ORM {


    public void createTable(Object obj){
        // check if object has annotations, if so use to dissect
        // else dont use and dissect manually.

        // Check if table exists...
        // if exists, dont overwrite, let user know.
        // else, create table using reflection/annotations to dissect the object class
        String tableName = "";
        String[] columns= new String[1+obj.getClass().getDeclaredFields().length];
        String primaryKey = "";
        String[] foreignKey= new String[1+obj.getClass().getDeclaredFields().length];

        // Initial class name
        if (obj.getClass().isAnnotationPresent(Table.class)) {

            Annotation a = obj.getClass().getAnnotation(Table.class);
            Table annotation = (Table) a;
            tableName = annotation.name();

        }else{
            tableName = obj.getClass().getCanonicalName();

        }

        // **********Check for additional classes and add to the creation of**************
        int fkIterator = 0;
        int colIterator = 0;

        for (Field field : obj.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(PrimaryKey.class)){
                primaryKey = field.getAnnotation(PrimaryKey.class).name();
            }else if (field.isAnnotationPresent(ForeignKey.class)) {
                foreignKey[fkIterator] = field.getAnnotation(ForeignKey.class).name();
                fkIterator++;
            }else if (field.isAnnotationPresent(Column.class)){
                columns[colIterator] = field.getAnnotation(Column.class).name();
                colIterator++;
            }else{
                columns[colIterator] = field.getName();
            }



//            Field[] declaredFields = obj.getClass().getDeclaredFields();
//            for (int i = 0; i < declaredFields.length; i++){
//                if (declaredFields[i].isAnnotationPresent(Column.class)){
//                    Annotation a = obj.getClass().getAnnotation(Column.class);
//                    Column annotation = (Column) a;
//                    System.out.println(annotation.name());
//                }
//            }




        }
        System.out.println("Table name:  " + tableName);
        System.out.println("Primary key:  " + primaryKey);
        System.out.println("Foreign key(s): ");
        for (int i = 0; i < foreignKey.length; i++){
            System.out.print(foreignKey[i]+ " ");
        }
        System.out.println("\nColumns: ");
        for (int i = 0; i < columns.length; i++){
            System.out.print(columns[i]+ " ");
        }

//    public void createNestedTable(Class nestedClass){
//        // check if object has annotations, if so use to dissect
//        // else dont use and dissect manually.
//
//        // Check if table exists...
//        // if exists, dont overwrite, let user know.
//        // else, create table using reflection/annotations to dissect the object class
//        String[] tableName = new String[1+obj.getClass().getDeclaredClasses().length];
//        String[] columns= new String[1+obj.getClass().getDeclaredFields().length];
//        String[] primaryKey= new String[1+obj.getClass().getDeclaredFields().length];
//        String[] foreignKey= new String[1+obj.getClass().getDeclaredFields().length];
//
//        // Initial class name
//        if (obj.getClass().isAnnotationPresent(Table.class)) {
//
//            Annotation a = obj.getClass().getAnnotation(Table.class);
//            Table annotation = (Table) a;
//            System.out.println(annotation.name());
//            tableName[0] = annotation.name();
//
//        }else{
//            tableName[0] = obj.getClass().getCanonicalName();
//
//        }
//
//        // Check for additional classes and add to the creation of
//
//        for (Field field : obj.getClass().getDeclaredFields()){
//            if (field.isAnnotationPresent(Column.class)){
//                System.out.println(field.getAnnotation(Column.class).name());
//            }
//        }
//
//
//
//

//            Field[] declaredFields = obj.getClass().getDeclaredFields();
//            for (int i = 0; i < declaredFields.length; i++){
//                if (declaredFields[i].isAnnotationPresent(Column.class)){
//                    Annotation a = obj.getClass().getAnnotation(Column.class);
//                    Column annotation = (Column) a;
//                    System.out.println(annotation.name());
//                }
//            }




    }



}
