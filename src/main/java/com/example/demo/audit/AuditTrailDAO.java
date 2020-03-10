package com.example.demo.audit;

import com.example.demo.util.AppConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class AuditTrailDAO {
    private static final String PARAM_NAME_TABLE_NAME = "tableName";
    private static final String PARAM_NAME_COLUMN_NAME = "columnName";
    private static final String PARAM_NAME_OLD_VALUE = "oldValue";
    private static final String PARAM_NAME_CHANGED_KEY_VALUE = "changedKeyValue";
    private static final String PARAM_NAME_CREATED_USER = "createdUser";
    private static final String PARAM_NAME_CREATED_TIME = "createdTime";
    private static final String PARAM_NAME_NEW_VALUE = "newValue";
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public void addAuditEntries(List<AuditTrail> auditTrails) {

        String sql = "insert into mdm.audit_trail(table_name, column_name, old_value, changed_key_value, " +
                "date_created, new_value) values(" +
                ":" + PARAM_NAME_TABLE_NAME + ", " +
                ":" + PARAM_NAME_COLUMN_NAME + ", " +
                ":" + PARAM_NAME_OLD_VALUE + ", " +
                ":" + PARAM_NAME_CHANGED_KEY_VALUE + ", " +
                ":" + PARAM_NAME_CREATED_TIME + ", " +
                ":" + PARAM_NAME_NEW_VALUE + ") ";

        Map[] parameterMapArray = new Map[auditTrails.size()];

        for (int i = 0; i < auditTrails.size(); i++) {
            AuditTrail auditTrail = auditTrails.get(i);
            parameterMapArray[i] = getTransactionParamMap(auditTrail);
        }

        SqlParameterSource[] auditEntryBatch = SqlParameterSourceUtils.createBatch(parameterMapArray);

        namedJdbcTemplate.batchUpdate(sql, auditEntryBatch);
    }

    private Map getTransactionParamMap(AuditTrail auditTrail) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PARAM_NAME_TABLE_NAME, auditTrail.getTableName());
        parameters.put(PARAM_NAME_COLUMN_NAME, auditTrail.getColumnName());
        parameters.put(PARAM_NAME_OLD_VALUE, auditTrail.getOldValue());
        parameters.put(PARAM_NAME_NEW_VALUE, auditTrail.getNewValue());
        parameters.put(PARAM_NAME_CHANGED_KEY_VALUE, auditTrail.getPkColumnValue());
        parameters.put(PARAM_NAME_CREATED_TIME, LocalDateTime.now());

        return parameters;
    }

    public Map<String, Map<String, String>> loadExistingValues(String schemaName, String tableName,
                                                               String[] auditableColumnNames, String[] selectionColumnNames,
                                                               Object[] selectionValues, String pkColumnName) {

        String sql = generateQueryToExtractValue(schemaName, tableName, selectionColumnNames,
                selectionValues, pkColumnName);

        MapSqlParameterSource parameters = generateMapSqlParameterSource(selectionColumnNames, selectionValues);

        SqlRowSet sqlRowSet = namedJdbcTemplate.queryForRowSet(sql, parameters);

        int noOfRows = getRowCount(sqlRowSet);

        Map<String, Map<String, String>> results = new HashMap<>();

        if (noOfRows > 0) {

            while (sqlRowSet.next()) {

                Map<String, String> rowData = new HashMap<>();

                String primaryKeyValue = sqlRowSet.getString(pkColumnName);

                for (int i = 0; i < auditableColumnNames.length; i++) {
                    String columnName = auditableColumnNames[i];
                    rowData.put(columnName, sqlRowSet.getString(columnName));
                }

                results.put(primaryKeyValue, rowData);
            }
        }

        return results;
    }

    private int getRowCount(SqlRowSet sqlRowSet) {
        sqlRowSet.last();
        int rowCount = sqlRowSet.getRow();
        sqlRowSet.beforeFirst();
        return rowCount;
    }

    private String generateQueryToExtractValue(String schemaName, String tableName, String[]
            selectionColumnNames, Object[] selectionValues, String pkColumnName) {

        StringBuilder sql = new StringBuilder(AppConstant.SELECT_CLAUSE);
        sql.append(AppConstant.ALL);

        sql.append(AppConstant.FROM_CLAUSE).append(schemaName).append(AppConstant.DOT).append(tableName);
        sql.append(AppConstant.WHERE_CLAUSE);
        sql.append(AppConstant.DEFAULT_CONDITION_CLAUSE);

        int size = selectionColumnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(AppConstant.AND_CLAUSE);
            boolean isList = List.class.isAssignableFrom(selectionValues[i].getClass());
            sql.append(selectionColumnNames[i]);
            if (isList) {
                sql.append(AppConstant.IN_CLAUSE).append(AppConstant.LEFT_PARENTHESIS).append(AppConstant.COLON)
                        .append(selectionColumnNames[i]).append(AppConstant.RIGHT_PARENTHESIS);
            } else {
                sql.append(AppConstant.EQUAL_SIGN).append(AppConstant.COLON).append(selectionColumnNames[i]);
            }
        }

        sql.append(AppConstant.ORDER_BY_CLAUSE).append(pkColumnName);

        return sql.toString();
    }

    private MapSqlParameterSource generateMapSqlParameterSource(String[] selectionColumnNames, Object[]
            selectionValues) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        int size = selectionColumnNames.length;
        for (int i = 0; i < size; i++) {
            parameters.addValue(selectionColumnNames[i], selectionValues[i]);
        }
        return parameters;
    }
}
