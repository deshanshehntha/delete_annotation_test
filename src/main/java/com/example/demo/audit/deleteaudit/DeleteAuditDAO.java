package com.example.demo.audit.deleteaudit;

import com.example.demo.audit.AuditTrail;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DeleteAuditDAO {
    private static final String PARAM_NAME_TABLE_NAME = "tableName";
    private static final String PARAM_NAME_COLUMN_NAME = "columnName";
    private static final String PARAM_NAME_OLD_VALUE = "oldValue";
    private static final String PARAM_NAME_CHANGED_KEY_VALUE = "changedKeyValue";
    private static final String PARAM_NAME_CREATED_USER = "createdUser";
    private static final String PARAM_NAME_CREATED_TIME = "createdTime";
    private static final String PARAM_NAME_NEW_VALUE = "newValue";
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public void saveDeletedTrail(Map<String, Map<String, String>>[] existingValues, String tableName) {

        System.out.println(existingValues);

        ArrayList<DeleteAuditTrail> auditTrails = new ArrayList<>();
        for (int i = 0; i < existingValues.length; i++) {
            for (Map.Entry<String, Map<String, String>> beforeDeleteRow : existingValues[i].entrySet()) {
                Map<String, Object> parameters = new HashMap<>();
                String primaryKeyValue = beforeDeleteRow.getKey();

                for (Map.Entry<String, String> valueMap : existingValues[i].get(primaryKeyValue).entrySet()) {
                    parameters.put(valueMap.getKey(), valueMap.getValue());
                }

                DeleteAuditTrail deleteAuditTrail = new DeleteAuditTrail();
                deleteAuditTrail.setTableName(tableName);
                deleteAuditTrail.setSavedValues(parameters.toString());
                deleteAuditTrail.setDeletedObjId(primaryKeyValue);
                auditTrails.add(deleteAuditTrail);
            }
        }


        String sql = "insert into mdm.delete_audit_trail(table_name,saved_values, deleted_object_id, " +
                "date_deleted) values(" +
                ":" + "table_name" + ", " +
                ":" + "saved_values" + ", " +
                ":" + "deleted_object_id" + ", " +
                ":" + "date_deleted" + ") ";

        Map[] parameterMapArray = new Map[auditTrails.size()];

        for (int i = 0; i < auditTrails.size(); i++) {
            DeleteAuditTrail auditTrail = auditTrails.get(i);
            parameterMapArray[i] = getTransactionParamMap(auditTrail);
        }

        SqlParameterSource[] auditEntryBatch = SqlParameterSourceUtils.createBatch(parameterMapArray);

        namedJdbcTemplate.batchUpdate(sql, auditEntryBatch);
    }


    private Map getTransactionParamMap(DeleteAuditTrail auditTrail) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("table_name", auditTrail.getTableName());
        parameters.put("saved_values", auditTrail.getSavedValues());
        parameters.put("date_deleted", LocalDateTime.now().toString());
        parameters.put("deleted_object_id", auditTrail.getDeletedObjId());

        return parameters;
    }
}
