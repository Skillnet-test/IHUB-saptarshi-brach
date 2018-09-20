package com.payment.constant;

public interface PaymentConstantIfc
{

    public static final String AMOUNT = "AMOUNT";

    public static final String CURRENCY = "CURRENCY";

    public static final String OPERATION = "OPERATION";

    public static final String ORDERID = "ORDERID";

    public static final String PAYID = "PAYID";

    public static final String PSPID = "PSPID";

    public static final String PSWD = "PSWD";

    public static final String USERID = "USERID";
    
    public static final String SAL = "SAL";
    
    public static final String RFD = "RFD";
    
    public static final String CONTEXT = "com.payment.ncresponse";
    
    public static final String SQL_READ_ALL_PAYMENT= "SELECT * FROM PAYMENT WHERE ID_STATUS = -1 ORDER BY CREATED_DATE ASC";
    
    public static final String SQL_UPDATE_PAYMENT= "UPDATE PAYMENT SET RESPONSE = ? , ID_STATUS = ? , MODIFIED_DATE = CURRENT_TIMESTAMP WHERE PAYMENT_ID = ?";
    
    public static final String SQL_INSERT_PAYMENT= "INSERT INTO PAYMENT( COMPANY_CODE, AMOUNT , CURRENCY, OPERATION, ORDERID, PAYID, PSPID, PSWD, USERID, ID_STATUS,CREATED_DATE, MODIFIED_DATE) values "
            + "( ?,?,?,?,?,?,?,?,?,-1, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
    
    public static final String SQL_READ_ALL_COMPANY_DETAILS = "SELECT * FROM COMPANY_DETAILS";
    
    public static final String INGENICO = "Ingenico";
    
    public static final String ADYEN = "Adyen";
    
    public static final String FLAG_NOT_PROCESSED = "-1";
    
    public static final String SCO_GREEN_CATEGORY = "G";
    
    public static final String SCO_ORANGE_CATEGORY = "O";

    

}
