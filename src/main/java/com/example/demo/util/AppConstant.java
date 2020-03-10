package com.example.demo.util;


import java.time.LocalDateTime;

public class AppConstant {

    public static final long INVOICE_PAYMENT_DATE_BUFFER_IN_DAYS = 10;

    //Symbols
    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String EQUAL_SIGN = "=";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";
    public static final String PIPE = "|";
    public static final String ALL = " *";

    //Query Constants
    public static final String SELECT_CLAUSE = "select ";
    public static final String FROM_CLAUSE = " from ";
    public static final String WHERE_CLAUSE = " where ";
    public static final String AND_CLAUSE = " and ";
    public static final String IN_CLAUSE = " in ";
    public static final String DEFAULT_CONDITION_CLAUSE = " 1=1 ";
    public static final String FOR_UPDATE_CLAUSE = " for update";
    public static final String ORDER_BY_CLAUSE = " order by ";

    //Response Constants
    public static final String FAILED_ORDER_LINES = "failedLines";
    public static final String CANCELLED_ORDER_LINES = "cancelledLines";
    public static final String ALREADY_PROCESSED_ORDER_LINES = "alreadyProcessedLines";
    public static final String SUCCESSFUL_ORDER_LINES = "successfulOrderLines";
    public static final String ERROR_ORDER_LINES = "errorOrderLines";
    public static final String UNSUPPORTED_ORDER_LINES = "unsupportedOrderLines";
    public static final String INVALID_ORDER_LINES = "invalidOrderLines";
    public static final String QTY_INVALID_ORDER_LINES = "qtyInvalidOrderLines";
    public static final String INVOICE_IDS = "invoiceIds";

    // Date Constants
    public static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

    public static final double DEFAULT_EXCHANGE_RATE = 1.0;

    private AppConstant() {

    }
}
