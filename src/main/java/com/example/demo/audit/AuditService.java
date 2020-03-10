package com.example.demo.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AuditService {

    private static final ThreadLocal<Map<String, Map<String, String>>[]> beforeResults = new ThreadLocal<Map<String, Map<String, String>>[]>();
    private final AuditTrailDAO auditTrailDAO;

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
                                               Audit auditAnnotation, boolean isKeyFieldValuePresent) throws NoSuchFieldException,
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
            beforeResults.set(beforeResultsTempArr);

            if (auditAnnotation.isSingleEntryUpdate()) {
                break;
            }
        }
    }

    public void populateAuditEntryAfterUpdate(String[] targetMethodParamNames, Object[] targetMethodParamValues,
                                              Audit auditAnnotation, boolean isKeyFieldValuePresent) throws NoSuchFieldException,
            IllegalAccessException {
        String schemaName = auditAnnotation.schemaName();
        String tableName = auditAnnotation.tableName();
        String[] auditableColumnNames = auditAnnotation.columnNames();
        String[] selectionFieldNames = targetMethodParamNames;
        String pkColumnName = auditAnnotation.pkColumnName();

        for (int element = 0; element < targetMethodParamValues.length; element++) {

            if (targetMethodParamValues[element] instanceof List) {
                populateAuditEntriesForList(targetMethodParamNames, targetMethodParamValues, selectionFieldNames,
                        auditAnnotation, element, isKeyFieldValuePresent);

            } else {
                Map<String, Map<String, String>>[] beforeResultsTempArr = beforeResults.get();

                Object[] selectionValues = extractSelectionValues(targetMethodParamNames,
                        targetMethodParamValues, selectionFieldNames);

                Map<String, Map<String, String>> afterUpdateResults = fetchPersistedValues(schemaName, tableName, auditableColumnNames, selectionFieldNames,
                        selectionValues, pkColumnName, isKeyFieldValuePresent);

                Map<String, Map<String, String[]>> recordsDiff = getRecordsDiff(beforeResultsTempArr[0], afterUpdateResults);

                if (!CollectionUtils.isEmpty(recordsDiff)) {
                    addAuditEntries(tableName, recordsDiff);
                }

            }
            if (auditAnnotation.isSingleEntryUpdate()) {
                break;
            }
        }
        beforeResults.remove();

    }

    private void addAuditEntries(String tableName, Map<String, Map<String, String[]>> recordsDiff) {
        List<AuditTrail> auditTrails = getAuditTrails(tableName, recordsDiff);
        if (!CollectionUtils.isEmpty(auditTrails)) {
            auditTrailDAO.addAuditEntries(auditTrails);
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

    private void populateAuditEntriesForList(String[] targetMethodParamNames,
                                             Object[] targetMethodParamValues,
                                             String[] selectionColumnNames,
                                             Audit auditAnnotation,
                                             int element, boolean isKeyFieldValuePresent) throws NoSuchFieldException,
            IllegalAccessException {
        List<Object> list = (List<Object>) targetMethodParamValues[element];
        int count = 0;
        for (Object object : list) {
            Object[] objects = new Object[1];
            objects[0] = object;
            targetMethodParamValues = objects;

            Object[] selectionValues = extractSelectionValues(targetMethodParamNames, targetMethodParamValues,
                    selectionColumnNames);

            Map<String, Map<String, String>> afterUpdateResults = fetchPersistedValues(
                    auditAnnotation.schemaName(), auditAnnotation.tableName(), auditAnnotation.columnNames(), selectionColumnNames,
                    selectionValues, auditAnnotation.pkColumnName(), isKeyFieldValuePresent);

            Map<String, Map<String, String>>[] beforeResultsTempArr = beforeResults.get();

            Map<String, Map<String, String[]>> recordsDiff = getRecordsDiff(beforeResultsTempArr[count], afterUpdateResults);
            if (!CollectionUtils.isEmpty(recordsDiff)) {
                List<AuditTrail> auditTrails = getAuditTrails(auditAnnotation.tableName(), recordsDiff);
                if (!CollectionUtils.isEmpty(auditTrails)) {
                    auditTrailDAO.addAuditEntries(auditTrails);
                }
            }
            count++;
        }
    }

    public Map<String, Map<String, String[]>> getRecordsDiff(Map<String, Map<String, String>> beforeUpdateResults,
                                                             Map<String, Map<String, String>> afterUpdateResults) {

        int size = beforeUpdateResults.size();
        Map<String, Map<String, String[]>> auditDiffList = new HashMap<>(size);

        for (Map.Entry<String, Map<String, String>> beforeUpdateRow : beforeUpdateResults.entrySet()) {

            String primaryKeyValue = beforeUpdateRow.getKey();

            Map<String, String> beforeUpdateRowData = beforeUpdateRow.getValue();
            Map<String, String> afterUpdateRowData = afterUpdateResults.get(primaryKeyValue);

            if (beforeUpdateRowData == null || afterUpdateRowData == null) {
                continue;
            }

            Map<String, String[]> rowDiff = new HashMap<>(beforeUpdateRowData.size());

            for (Map.Entry<String, String> beforeUpdateCell : beforeUpdateRowData.entrySet()) {

                String columnName = beforeUpdateCell.getKey();

                String beforeValue = beforeUpdateCell.getValue();
                String afterValue = afterUpdateRowData.get(columnName);

                if (!Objects.equals(beforeValue, afterValue)) {

                    rowDiff.put(columnName, new String[]{beforeValue, afterValue});
                }
            }
            if (!CollectionUtils.isEmpty(rowDiff)) {
                auditDiffList.put(primaryKeyValue, rowDiff);
            }
        }

        return auditDiffList;
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

    private List<AuditTrail> getAuditTrails(String tableName, Map<String, Map<String, String[]>> recordsDiff) {

        List<AuditTrail> auditTrails = new ArrayList<>();

        for (Map.Entry<String, Map<String, String[]>> diffRow : recordsDiff.entrySet()) {

            String primaryKeyValue = diffRow.getKey();

            for (Map.Entry<String, String[]> rowDiff : diffRow.getValue().entrySet()) {

                AuditTrail auditTrail = new AuditTrail();
                auditTrail.setTableName(tableName);
                auditTrail.setColumnName(rowDiff.getKey());
                auditTrail.setOldValue(String.valueOf(rowDiff.getValue()[0]));
                auditTrail.setNewValue(String.valueOf(rowDiff.getValue()[1]));
                auditTrail.setPkColumnValue(primaryKeyValue);
                auditTrails.add(auditTrail);
            }
        }

        return auditTrails;
    }
}
