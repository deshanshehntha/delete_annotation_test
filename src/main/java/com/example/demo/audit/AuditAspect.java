package com.example.demo.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;


@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditService auditService;

    @Before("@annotation(audit)")
    public void beforeUpdate(JoinPoint joinPoint, Audit audit) {
        LOGGER.debug("Audit annotation advice is triggered before method execution...");

        Object[] targetMethodParamValues = joinPoint.getArgs();
        String[] targetMethodParamNames = new String[1];
        boolean isKeyFieldValuePresent = false;
        if (!audit.keyFieldValue().equals("")) {
            Array.set(targetMethodParamNames, 0, audit.keyFieldValue());
            isKeyFieldValuePresent = true;
        } else {
            targetMethodParamNames = audit.selectionColumnNames();
        }

        try {
            auditService.populateAuditEntryBeforeUpdate(targetMethodParamNames, targetMethodParamValues, audit, isKeyFieldValuePresent);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            LOGGER.error("Exception thrown from Audit Framework - beforeUpdate - {} ", ex);
        }

        LOGGER.debug("Execution of the Audit annotation advice before method execution is completed.");
    }

    @After("@annotation(audit)")
    public void afterUpdate(JoinPoint joinPoint, Audit audit) {
        LOGGER.debug("Audit annotation advice is triggered after method execution...");

        Object[] targetMethodParamValues = joinPoint.getArgs();
        String[] targetMethodParamNames = new String[1];
        boolean isKeyFieldValuePresent = false;

        if (!audit.keyFieldValue().equals("")) {
            isKeyFieldValuePresent = true;
            Array.set(targetMethodParamNames, 0, audit.keyFieldValue());
        } else {
            targetMethodParamNames = audit.selectionColumnNames();
        }

        try {
            auditService.populateAuditEntryAfterUpdate(targetMethodParamNames, targetMethodParamValues, audit, isKeyFieldValuePresent);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.error("Exception thrown from Audit Framework - afterUpdate - {}", ex);
        }

        LOGGER.debug("Execution of the Audit annotation advice after method execution is completed.");
    }
}
