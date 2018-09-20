
create sequence PAYMENT_SEQUENCE start with 1 increment by 1 nocache;

CREATE TABLE PAYMENT
(
PAYMENT_ID number default PAYMENT_SEQUENCE.nextval,
COMPANY_CODE VARCHAR(20),
AMOUNT VARCHAR2(20),
CURRENCY VARCHAR2(20),
OPERATION VARCHAR2(20),
ORDERID VARCHAR2(20),
PAYID VARCHAR2(20),
PSPID VARCHAR2(20),
PSWD  VARCHAR2(20),
USERID VARCHAR2(20),
ID_STATUS INT,
RESPONSe VARCHAR2(1000),
CREATED_DATE timestamp,
MODIFIED_DATE timestamp
);