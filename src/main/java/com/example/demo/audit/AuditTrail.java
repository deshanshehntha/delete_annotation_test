package com.example.demo.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AuditTrail {

    private String tableName;
    private String columnName;
    private String oldValue;
    private String newValue;
    private String pkColumnValue;

}
