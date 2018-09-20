/**
 * 
 */
package com.oms.order.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.common.OrderConstantIfc;
import com.oms.order.connector.OMSOrderConnector;
import com.oms.order.dao.OMSOrderManager;
import com.oms.order.formatter.OMSOrderFormatter;
import com.oms.order.formatter.OMSOrderRequest;
import com.oms.order.formatter.OMSOrderResponse;

/**
 * @author Jigar
 *
 */
@Service
public class OMSOrderService 
{
	
	private static final Logger logger = Logger.getLogger(OMSOrderService.class);
	
	@Autowired
	OMSOrderConnector omsOrderConnector;
	
	@Autowired
	OMSOrderFormatter omsOrderFormatter;
	
	@Autowired
	OMSOrderManager omsOrderManager;
	
    @Value("${oms.getfromQueue.invoiceout.queuename}")
    private String omsInvoiceOutQueueName;
	
    @Value("${oms.getfromQueue.url}")
    private String omsGetFromQueueEndPoint;
    
    @Value("${oms.getfromQueue.authorization.value}")
    private String omsAuthorizationValue;

    @Value("${oms.getfromQueue.contentType}")
    private String omsgetfromQueueContentType;
    
    
    

	/**
	 * 
	 */
	public OMSOrderService() 
	{

	}
	
	/*
	 * Get Order Invoice Out From OMS
	 */
	public String getOrderInvoiceOutFromOMS()
	{
		String responseStr="";
		try 
		{
			OMSOrderRequest omsOrderRequest= new OMSOrderRequest();
			omsOrderRequest.setEndpoint(omsGetFromQueueEndPoint);
			omsOrderRequest.setAuthorizationValue(omsAuthorizationValue);
			omsOrderRequest.setContentTypeValue(omsgetfromQueueContentType);
			
			
			JSONObject invoiceOutJSONObjectRequest = new JSONObject();
			invoiceOutJSONObjectRequest.put(OrderConstantIfc.INVOICE_OUT_JSON_QUEUE_NAME_KEY, omsInvoiceOutQueueName ); 
			
			omsOrderRequest.setRequest(invoiceOutJSONObjectRequest.toString());

			responseStr=(String) omsOrderConnector.processRequest(omsOrderRequest);
		} 
		catch (Exception e) 
		{
			logger.error("Exception caused while getting Order Invoice Out Request :  " +e.getMessage());
		}

		return responseStr;
	}
	
	
	/*
	 * Formatting the Order Invoice Out Request
	 */
	public OMSOrderResponse formatOrderInvoiceOut(String responseStr)
	{
		OMSOrderResponse omsInvoiceOutResponse=null;
		if(StringUtils.isNotEmpty(responseStr))
			omsInvoiceOutResponse=omsOrderFormatter.formatInvoiceOutToResponseObject(responseStr);
		return omsInvoiceOutResponse;
	}
	
	
	/*
	 * Persisting Order Invoice Out To Database.
	 */
	public String persistOrderInvoiceOutToDB(OMSOrderResponse omsInvoiceOutResponse) throws OMSException
	{
		if(omsInvoiceOutResponse==null)
		{
			logger.error("Order Invoice Out Response found to be null!");
			return "Failed!";
		}
		
		if(StringUtils.isNotEmpty(omsInvoiceOutResponse.getStatus()) && omsInvoiceOutResponse.getStatus().equalsIgnoreCase(OrderConstantIfc.INVOICE_OUT_RESPONSE_STATUS_EOF))
		{
            // return "No Order Found to Process!";
            throw new OMSException(OMSErrorCodes.NO_ORDER_PROCESS.getCode(),
                    OMSErrorCodes.NO_ORDER_PROCESS.getDescription());
		}
		else
			omsOrderManager.persistOMSInvoiceOut(omsInvoiceOutResponse);

		return omsInvoiceOutResponse.getStatus().toString();
	}
	
	
	

}
