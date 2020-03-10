package com.example.demo.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * Schema name where the audit should trigger.
     */
    String schemaName();

    /**
     * Table name where the audit should trigger.
     */
    String tableName();

    /**
     * Only the value changes in these columns will be audited.
     */
    String[] columnNames();

    /**
     * Parameter name which is used as a variable reference to denote the querry selection. Can be one or multiple
     * reference names. These selection column names should be similar to column names at table and usually these column
     * names are used after the SQL where clause.
     */
    String[] selectionColumnNames();

    /**
     * Parameter name which is used as a variable value to perform the querry selection. Can be one or multiple
     * parameter names. These selection value names are usually passed to DAO method parameter list and used for binding
     * values to SQL where clause.
     * <p>
     * If selectionValueNames are specified, no need to provide keyFieldValue attribute.
     */
    String[] selectionValueNames() default "";

    /**
     * Use only when selectionValueName is not available as method parameter and a object is available including a
     * selection field. keyFieldValue is used same as selectionValueNames in SQL where clause filtering.
     * <p>
     * ex: updateProduct(Product product)
     * <p>
     * keyFieldValue is a fieldName which is present in the object. this field should not be null.
     * <p>
     * if keyFieldValue and selectionValueNames both provided, only the keyFieldValue will be considered. Multiple
     * keyFieldValues are not supported.
     */
    String keyFieldValue() default "";

    /**
     * Set false if batch update should audited. List of objects should be passed to method parameter list.
     * <p>
     * Default value is true.
     */
    boolean isSingleEntryUpdate() default true;

    /**
     * Defines the primary key column name of the table.
     */
    String pkColumnName();
}
