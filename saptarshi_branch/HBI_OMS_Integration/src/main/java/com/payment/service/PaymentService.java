package com.payment.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.oms.companycode.dao.CompanyCodeManager;
import com.oms.exception.OMSException;
import com.oms.order.connector.OMSOrderConnector;
import com.oms.order.formatter.OMSOrderRequest;
import com.payment.constant.PaymentConstantIfc;
//import com.payment.dao.CompanyCodeDao;
import com.payment.data.PaymentData;
import com.payment.formatter.AdyenPaymentFormatter;
import com.payment.formatter.PaymentFormatter;
import com.payment.formatter.PaymentRequest;
import com.payment.formatter.PaymentResponse;

import okhttp3.FormBody;

@Component
public class PaymentService implements PaymentConstantIfc
{

    private static final Logger logger = Logger.getLogger(PaymentService.class);

    @Autowired
    OMSOrderConnector omsOrderConnector;

    @Autowired
    PaymentFormatter paymentFormatter;

    @Autowired
    AdyenPaymentFormatter adyenPaymentFormatter;

    @Value("${ingenico.maintenancedirect.url}")
    private String ingenicoMaintenancedirectUrl;

    @Value("${ingenico.contentType}")
    private String ingenicoContentType;

    @Value("${ingenico.querydirect.url}")
    private String ingenicoQuerydirectUrl;

   /* @Autowired
    CompanyCodeDao companyCodes;*/
    @Autowired
    CompanyCodeManager companyCodeManager;

    /**
     * 
     */
    public PaymentService()
    {
    }

    public PaymentResponse processFraudCheck(PaymentRequest paymentRequest, PaymentData paymentData) throws OMSException
    {
        PaymentResponse paymentResponse = null;

        FormBody formBody = new FormBody.Builder().add(PAYID, paymentRequest.getPayid())
                .add(PSPID, paymentRequest.getPspid()).add(PSWD, paymentRequest.getPassword())
                .add(USERID, paymentRequest.getUser()).build();

        OMSOrderRequest omsOrderRequest = new OMSOrderRequest();
        omsOrderRequest.setEndpoint(ingenicoQuerydirectUrl);
        omsOrderRequest.setContentTypeValue(ingenicoContentType);
        omsOrderRequest.setFormBody(formBody);
        String response = (String)omsOrderConnector.processRequest(omsOrderRequest);
        paymentResponse = paymentFormatter.translateFraudResponse(response, paymentData);
        return paymentResponse;
    }

    public PaymentResponse processPayment(PaymentData paymentData) throws OMSException
    {
        PaymentResponse paymentResponse = null;

        String response = null;

        String paymentIntegration = null;

        if (StringUtils.isNotBlank(paymentData.getCompany_Code()))
        {

            //paymentIntegration = companyCodes.getPaymentIntegrationCompany(paymentData.getCompany_Code());companyCodeManager
        	paymentIntegration = companyCodeManager.getPaymentIntegration(paymentData.getCompany_Code());
        }

        if (StringUtils.isNotBlank(paymentIntegration)
                && StringUtils.equalsIgnoreCase(paymentIntegration, PaymentConstantIfc.INGENICO))
        {
            PaymentRequest paymentRequest = paymentFormatter.prepareRequest(paymentData);
            if (paymentRequest.getOperation().equalsIgnoreCase(SAL))
                if (StringUtils.equalsIgnoreCase(paymentRequest.getOperation(), SAL))
                {
                    paymentResponse = processFraudCheck(paymentRequest, paymentData);
                }

            if (StringUtils.equalsIgnoreCase(paymentRequest.getOperation(), RFD)
                    || ((StringUtils.equalsIgnoreCase(paymentResponse.getScoCategory(), SCO_GREEN_CATEGORY))
                            && (StringUtils.equalsIgnoreCase(paymentRequest.getOperation(), SAL))))
            {
                FormBody formBody = new FormBody.Builder().add(AMOUNT, paymentRequest.getAmount())
                        .add(CURRENCY, paymentRequest.getCurrency()).add(OPERATION, paymentRequest.getOperation())
                        .add(ORDERID, paymentRequest.getOrderid()).add(PAYID, paymentRequest.getPayid())
                        .add(PSPID, paymentRequest.getPspid()).add(PSWD, paymentRequest.getPassword())
                        .add(USERID, paymentRequest.getUser()).build();

                OMSOrderRequest omsOrderRequest = new OMSOrderRequest();
                omsOrderRequest.setEndpoint(ingenicoMaintenancedirectUrl);
                omsOrderRequest.setContentTypeValue(ingenicoContentType);
                omsOrderRequest.setFormBody(formBody);
                response = (String)omsOrderConnector.processRequest(omsOrderRequest);
                System.out.println(response);
                paymentResponse = paymentFormatter.translateResponse(response, paymentData);
            }

        }
        else if (StringUtils.equalsIgnoreCase(paymentIntegration, ADYEN))
        {
            OMSOrderRequest omsOrderRequest = adyenPaymentFormatter.prepareRequest(paymentData);
            response = (String)omsOrderConnector.processRequest(omsOrderRequest);
            System.out.println(response);
            paymentResponse = adyenPaymentFormatter.translateResponse(response, paymentData);
        }
        else
        {
            logger.info("Unable to map company code with payment integration company name \n");
            logger.info("paymentIntegration :" + paymentData.getCompany_Code());

        }

        return paymentResponse;

    }
}
