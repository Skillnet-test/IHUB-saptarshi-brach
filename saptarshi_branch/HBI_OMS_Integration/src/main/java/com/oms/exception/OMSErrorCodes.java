package com.oms.exception;

public enum OMSErrorCodes
{
    DEFAULT(0, "Some error has occured"), 
    SUCCESS(1, "Success"), 
    DATABASE(2,"A database error has occured."),
    CONNECT_ERROR(3,"Network connection error has occured"),
    NO_ORDER_PROCESS(4, "No order to process"),
    JSON_ERROR(5, "Error occured in parsing JSON response object"),
    NO_RECORDS_FOUND(6, "No records found"),
    RESPONSE_ERROR(7, "Error occurred while parsing response"),
    REQUEST_PARSE_ERROR(8, "Error occurred while parsing/creating request object");
    /*DUPLICATE_USER(3, "This user already exists."), 
    INVALID_USER_PASSWORD(4,"Invalid username or password."), 
    UPLOAD_ERROR(5,"Error occurred while uploading data to Amazon S3"), 
    RECORD_EXIST(6,"Record already exists"), 
    NO_RECORDS_MODIFIED(7,"No Record inserted or modified."), 
    NO_USER_FOUND(8,"No User Found."), 
    INVALID_TOPIC_ID(9,"Invalid Topic ID Found"),
    NO_RECORDS_FOUND(10,"No Records Found."),
    IMAGE_CROP_ERROR(11,"Image Selection is going Out of Image Size."),
    INVALID_FAV_ID(12, "Invalid Fav ID Found"),
    EMAIL_ERROR(13,"Email was not send due to smtp error.");*/

    private final int code;

    private final String description;

    private OMSErrorCodes(int code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public int getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return code + ": " + description;
    }
}

