package com.payment.formatter;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.payment.constant.PaymentConstantIfc;
import com.payment.data.PaymentData;
import com.payment.ncresponse.Ncresponse;

@Component
public class PaymentFormatter implements PaymentConstantIfc
{
    protected static final Logger logger = Logger.getLogger(PaymentFormatter.class);

    public PaymentRequest prepareRequest(PaymentData paymentData)
    {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(paymentData.getAmount());
        paymentRequest.setCurrency(paymentData.getCurrency());
        paymentRequest.setOperation(paymentData.getOperation());
        paymentRequest.setOrderid(paymentData.getOrderid());
        paymentRequest.setPayid(paymentData.getPayid());
        paymentRequest.setPspid(paymentData.getPspid());
        paymentRequest.setUser(paymentData.getUserid());
        paymentRequest.setPassword(paymentData.getPswd());

        return paymentRequest;
    }

    public PaymentResponse translateResponse(String response, PaymentData paymentData) throws OMSException
    {
        PaymentResponse paymentResponse = new PaymentResponse();
        try
        {
            paymentResponse.setResponse(response);
            System.out.println(paymentResponse.getResponse());

            JAXBContext jContext = JAXBContext.newInstance(CONTEXT);
            StringReader reader = new StringReader(paymentResponse.getResponse());
            Ncresponse ncresponse = (Ncresponse)jContext.createUnmarshaller().unmarshal(reader);
            System.out.println("NCStatus :" + ncresponse.getNCSTATUS());
            paymentResponse.setNcresponse(ncresponse.getNCSTATUS());
            paymentResponse.setPayment_id(paymentData.getPayment_id());

        }
        catch (Exception ex)
        {
            logger.error(ex);
            throw new OMSException(OMSErrorCodes.RESPONSE_ERROR.getCode(), OMSErrorCodes.RESPONSE_ERROR.getDescription());
        }
        return paymentResponse;
    }
    
    public PaymentResponse translateFraudResponse(String response, PaymentData paymentData)
    {
        PaymentResponse paymentResponse = new PaymentResponse();
        try
        {
            paymentResponse.setResponse(response);
            System.out.println(paymentResponse.getResponse());

            JAXBContext jContext = JAXBContext.newInstance(CONTEXT);
            StringReader reader = new StringReader(paymentResponse.getResponse());
            Ncresponse ncresponse = (Ncresponse)jContext.createUnmarshaller().unmarshal(reader);
            System.out.println("SCOCATEGORY() :" + ncresponse.getSCOCATEGORY());
            paymentResponse.setNcresponse(FLAG_NOT_PROCESSED);
            paymentResponse.setScoCategory( ncresponse.getSCOCATEGORY());
            //Hard Code "O".
            //paymentResponse.setScoCategory( SCO_ORANGE_CATEGORY);
            paymentResponse.setPayment_id(paymentData.getPayment_id());

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return paymentResponse;
    }


}
