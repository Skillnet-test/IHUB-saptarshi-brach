--------------------------------------------------------
--  DDL for Table Comnpany Details
--------------------------------------------------------
Drop table company_details;
CREATE TABLE company_details
 (
    "COMPANY_CODE"       NUMBER(10,0)
        NOT NULL ENABLE,
    "COMPANY_DESC"       VARCHAR2(50 BYTE),
    "CURRENCY_CODE"      VARCHAR2(50 BYTE),
    "COMPANY_NAME"       VARCHAR2(50 BYTE),
    "WAREHOUSE_NUMBER"   VARCHAR2(50 BYTE)
);

 COMMENT ON COLUMN "company_details"."COMPANY_CODE" IS 'company code';
 
 COMMENT ON COLUMN "company_details"."COMPANY_DESC" IS 'company  description';
 
 COMMENT ON COLUMN "company_details"."CURRENCY_CODE" IS 'currency code';
 
 COMMENT ON COLUMN "company_details"."COMPANY_NAME" IS 'payment integration company name';
 
 COMMENT ON COLUMN "company_details"."WAREHOUSE_NUMBER" IS 'warehouse number';

 