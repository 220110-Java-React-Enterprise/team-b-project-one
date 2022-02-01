package Persistence;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ScriptMaker {

    HashMap<Field, FieldType> fieldList = new HashMap<>();

    String tableName = "";

    String createSQL = "INSERT INTO " + tableName + " (" + fieldList + ")";
}
