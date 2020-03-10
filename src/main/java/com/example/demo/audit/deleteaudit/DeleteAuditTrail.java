package com.example.demo.audit.deleteaudit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class DeleteAuditTrail {

    private String tableName;
    private String savedValues;
    private String deletedObjId;


}

