/**
 * 
 */
package com.oms.order.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.oms.exception.OMSException;
import com.oms.order.connector.OMSOrderConnector;
import com.oms.order.formatter.OMSCustHistFormatter;
import com.oms.order.formatter.OMSCustHistRequest;
import com.oms.order.formatter.OMSCustHistResponse;

/**
 * @author Jigar
 */
@Service
public class CustHistInOrderService
{

    private static final Logger logger = Logger.getLogger(CustHistInOrderService.class);

    @Autowired
    OMSOrderConnector omsOrderConnector;

    @Autowired
    OMSCustHistFormatter omsCustHistFormatter;

    @Value("${oms.cwMessageIn.url}")
    private String omscwMessageEndPoint;

    @Value("${oms.cwMessageIn.authorization.value}")
    private String omsAuthorizationValue;

    @Value("${oms.cwMessageIn.contentType}")
    private String omscwMessageInContentType;

    /**
     * 
     */
    public CustHistInOrderService()
    {

    }

    public OMSCustHistResponse getOrderFromOMS(String orderID) throws OMSException
    {
        OMSCustHistResponse omsCustHistResponse = null;
        OMSCustHistRequest omsCustHistRequest = new OMSCustHistRequest();
        omsCustHistRequest.setEndpoint(omscwMessageEndPoint);
        omsCustHistRequest.setAuthorizationValue(omsAuthorizationValue);
        omsCustHistRequest.setContentTypeValue(omscwMessageInContentType);

        omsCustHistRequest.setOrderNumber(orderID);
        ;
        String request = omsCustHistFormatter.prepareRequest(omsCustHistRequest);
        omsCustHistRequest.setRequest(request);
        String response = (String)omsOrderConnector.processRequest(omsCustHistRequest);
        omsCustHistResponse = omsCustHistFormatter.translateResponse(response);

        return omsCustHistResponse;

    }

}
