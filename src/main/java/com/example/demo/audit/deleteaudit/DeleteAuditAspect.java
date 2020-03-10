package com.example.demo.audit.deleteaudit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DeleteAuditAspect {


    private final DeleteAuditService deleteAuditService;

    @Before("@annotation(deleteAudit)")
    public void beforeUpdate(JoinPoint joinPoint, DeleteAudit deleteAudit) throws Exception {

        Object[] targetMethodParamValues = joinPoint.getArgs();
        CodeSignature methodSignature = (CodeSignature) joinPoint.getSignature();
        String[] targetMethodParamNames = deleteAudit.selectionValueNames();

        deleteAuditService.populateAuditEntryBeforeUpdate(targetMethodParamNames, targetMethodParamValues, deleteAudit, false);

    }

}