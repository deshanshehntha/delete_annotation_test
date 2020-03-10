package com.example.demo.audit.deleteaudit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteAudit {

    String schemaName();

    String tableName();

    String[] columnNames();

    String[] selectionColumnNames();

    String[] selectionValueNames();

    String pkColumnName();

    boolean isSingleEntryUpdate() default true;

}