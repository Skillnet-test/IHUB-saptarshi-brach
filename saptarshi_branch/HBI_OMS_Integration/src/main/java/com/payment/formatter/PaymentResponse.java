package com.payment.formatter;

public class PaymentResponse
{

    private String response;
    
    private int payment_id;
    
    private String ncresponse;
    
    private String scoCategory;

    public String getResponse()
    {
        return response;
    }

    public void setResponse(String response)
    {
        this.response = response;
    }

    public int getPayment_id()
    {
        return payment_id;
    }

    public void setPayment_id(int payment_id)
    {
        this.payment_id = payment_id;
    }

    public String getNcresponse()
    {
        return ncresponse;
    }

    public void setNcresponse(String ncresponse)
    {
        this.ncresponse = ncresponse;
    }
    
    public String getScoCategory()
    {
        return scoCategory;
    }

    public void setScoCategory(String scoCategory)
    {
        this.scoCategory = scoCategory;
    }


}
