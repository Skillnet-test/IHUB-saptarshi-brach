/**
 * 
 */
package com.oms.order.service;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.common.OrderConstantIfc;
import com.oms.order.connector.OMSOrderConnector;
import com.oms.order.formatter.OMSOrderRequest;
import com.oms.order.formatter.OMSOrderResponse;
import com.oms.order.formatter.PickOutOrderFormatter;

/**
 * @author Jigar
 */
@Service
public class PickOutOrderService
{

    private static final Logger logger = Logger.getLogger(PickOutOrderService.class);

    @Autowired
    OMSOrderConnector omsOrderConnector;

    @Autowired
    PickOutOrderFormatter pickOutOrderFormatter;

    @Value("${oms.getfromQueue.pickout.queuename}")
    private String omsPickOutQueueName;
    
    @Value("${oms.getfromQueue.url}")
    private String omsGetFromQueueEndPoint;

    @Value("${oms.getfromQueue.authorization.value}")
    private String omsAuthorizationValue;

    @Value("${oms.getfromQueue.contentType}")
    private String omsgetfromQueueContentType;

    /**
     * 
     */
    public PickOutOrderService()
    {

    }

    public OMSOrderResponse getOrderFromOMS() throws OMSException
    {
        OMSOrderResponse omsPickOutResponse = null;
        String responseStr = "";
        OMSOrderRequest omsOrderRequest = new OMSOrderRequest();
        omsOrderRequest.setEndpoint(omsGetFromQueueEndPoint);
        omsOrderRequest.setAuthorizationValue(omsAuthorizationValue);
        omsOrderRequest.setContentTypeValue(omsgetfromQueueContentType);

        JSONObject invoiceOutJSONObjectRequest = new JSONObject();
        invoiceOutJSONObjectRequest.put(OrderConstantIfc.INVOICE_OUT_JSON_QUEUE_NAME_KEY,
        		omsPickOutQueueName);

        omsOrderRequest.setRequest(invoiceOutJSONObjectRequest.toString());

        responseStr = (String)omsOrderConnector.processRequest(omsOrderRequest);

        omsPickOutResponse = pickOutOrderFormatter.formatPickOutToResponseObject(responseStr);

        if (omsPickOutResponse.getStatus().equalsIgnoreCase(OrderConstantIfc.INVOICE_OUT_RESPONSE_STATUS_EOF))
        {
            throw new OMSException(OMSErrorCodes.NO_ORDER_PROCESS.getCode(), OMSErrorCodes.NO_ORDER_PROCESS.getDescription());
        }

        return omsPickOutResponse;
    }

}
