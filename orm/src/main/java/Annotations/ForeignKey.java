package Annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface ForeignKey {
    String columnName();
    String type();
    String referenceTableName();
    String referenceTableColumn();
}
