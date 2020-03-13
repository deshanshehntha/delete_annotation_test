package com.example.demo.audit.deleteaudit;

import com.example.demo.audit.AuditTrailDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DeleteAuditService {

    private final AuditTrailDAO auditTrailDAO;
    private final DeleteAuditDAO deleteAuditDAO;

    public static Object getValueOf(Object className, String variableName) throws NoSuchFieldException, IllegalAccessException {
        Field field;

        Field[] declaredFields = className.getClass().getDeclaredFields();
        ArrayList<String> declaredFieldNames = new ArrayList<>();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFieldNames.add(declaredFields[i].getName());
        }

        if (declaredFieldNames.contains(variableName)) {
            field = className.getClass().getDeclaredField(variableName);
            field.setAccessible(true);
            return field.get(className);

        } else {
            Field[] declaredSuperFields = className.getClass().getSuperclass().getDeclaredFields();
            ArrayList<String> declaredSuperClassFieldNames = new ArrayList<>();

            for (int i = 0; i < declaredSuperFields.length; i++) {
                declaredSuperClassFieldNames.add(declaredSuperFields[i].getName());
            }
            if (declaredSuperClassFieldNames.contains(variableName)) {
                field = className.getClass().getSuperclass().getDeclaredField(variableName);
                field.setAccessible(true);
                return field.get(className);
            }
        }

        return null;
    }

    public void populateAuditEntryBeforeUpdate(String[] targetMethodParamNames, Object[] targetMethodParamValues,
                                               DeleteAudit auditAnnotation, boolean isKeyFieldValuePresent) throws NoSuchFieldException,
            IllegalAccessException {

        String schemaName = auditAnnotation.schemaName();
        String tableName = auditAnnotation.tableName();
        String[] auditableColumnNames = auditAnnotation.columnNames();
        String[] selectionFieldNames = targetMethodParamNames;
        String pkColumnName = auditAnnotation.pkColumnName();

        for (int element = 0; element < targetMethodParamValues.length; element++) {

            Map<String, Map<String, String>>[] beforeResultsTempArr;

            if (targetMethodParamValues[element] instanceof List) {
                List<Object> list = (List<Object>) targetMethodParamValues[element];
                beforeResultsTempArr = new Map[list.size()];
                int count = 0;
                for (Object object : list) {
                    Object[] objects = new Object[1];
                    objects[0] = object;
                    targetMethodParamValues = objects;

                    Object[] selectionValues = getSelectionValues(targetMethodParamNames, targetMethodParamValues,
                            selectionFieldNames);

                    beforeResultsTempArr[count] = fetchPersistedValues(
                            schemaName, tableName, auditableColumnNames, selectionFieldNames,
                            selectionValues, pkColumnName, isKeyFieldValuePresent);
                    ++count;
                }
            } else {
                beforeResultsTempArr = new Map[1];

                Object[] selectionValues = getSelectionValues(targetMethodParamNames, targetMethodParamValues, selectionFieldNames);

                beforeResultsTempArr[0] = fetchPersistedValues(
                        schemaName, tableName, auditableColumnNames, selectionFieldNames,
                        selectionValues, pkColumnName, isKeyFieldValuePresent);

            }

            deleteAuditDAO.saveDeletedTrail(beforeResultsTempArr, tableName);
            if (auditAnnotation.isSingleEntryUpdate()) {
                break;
            }
        }
    }

    private Object[] getSelectionValues(String[] targetMethodParamNames, Object[] targetMethodParamValues,
                                        String[] selectionColumnNames) throws NoSuchFieldException, IllegalAccessException {

        Object[] selectionValues = extractSelectionValues(targetMethodParamNames, targetMethodParamValues,
                selectionColumnNames);

        return selectionValues;
    }

    private Map<String, Map<String, String>> fetchPersistedValues(String schemaName, String tableName, String[] auditableColumnNames,
                                                                  String[] selectionColumnNames,
                                                                  Object[] selectionValues, String pkColumnName, boolean isKeyFieldValuePresent) {

        if (isKeyFieldValuePresent) {
            return keyFieldBasedExistingValueFetch(schemaName, tableName, auditableColumnNames, selectionValues, pkColumnName);
        }

        return auditTrailDAO.loadExistingValues(
                schemaName, tableName, auditableColumnNames, selectionColumnNames,
                selectionValues, pkColumnName);
    }

    private Map<String, Map<String, String>> keyFieldBasedExistingValueFetch(String schemaName, String tableName, String[] auditableColumnNames,
                                                                             Object[] selectionValues, String pkColumnName) {

        String[] keyFieldBasedSelectionColumns = new String[1];
        Array.set(keyFieldBasedSelectionColumns, 0, pkColumnName);

        return auditTrailDAO.loadExistingValues(
                schemaName, tableName, auditableColumnNames, keyFieldBasedSelectionColumns,
                selectionValues, pkColumnName);
    }

    private Object[] extractSelectionValues(String[] targetMethodParamNames,
                                            Object[] targetMethodParamValues,
                                            String[] selectionValueRefNames) throws NoSuchFieldException, IllegalAccessException {

        Object[] selectionValues = new Object[selectionValueRefNames.length];

        for (String selectionValueRefName : selectionValueRefNames) {

            for (int count = 0; count < targetMethodParamNames.length; count++) {
                String targetMethodParamName = targetMethodParamNames[count];

                if (targetMethodParamName.equals(selectionValueRefName)) {
                    Object paramValue = targetMethodParamValues[count];
                    if (getValueOf(paramValue, targetMethodParamNames[count]) != null) {
                        paramValue = getValueOf(paramValue, targetMethodParamNames[count]);
                    }

                    if (paramValue != null && isValidParameterValue(paramValue)) {
                        selectionValues[count] = paramValue;
                    }
                }
            }
        }

        return selectionValues;
    }

    private boolean isValidParameterValue(Object paramValue) { // should change this to check the object is valid

        boolean valid = false;
        if (ClassUtils.isPrimitiveOrWrapper(paramValue.getClass()) || paramValue instanceof String) {
            valid = true;

        } else if (List.class.isAssignableFrom(paramValue.getClass())) {
            List listObj = (List) paramValue;
            if (!listObj.isEmpty() && ClassUtils.isPrimitiveOrWrapper(listObj.get(0).getClass())) {
                valid = true;
            }
        }
        return valid;
    }

}
