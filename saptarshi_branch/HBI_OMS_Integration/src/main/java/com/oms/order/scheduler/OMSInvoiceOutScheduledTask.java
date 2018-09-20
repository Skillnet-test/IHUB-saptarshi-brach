/**
 * 
 */
package com.oms.order.scheduler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.oms.order.formatter.OMSOrderResponse;
import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.service.OMSOrderService;

/**
 * @author Jigar
 */
@Component
public class OMSInvoiceOutScheduledTask
{
    protected static final Logger logger = Logger.getLogger(OMSInvoiceOutScheduledTask.class);

    @Autowired
    OMSOrderService omsOrderService;

    /**
     * 
     */
    public OMSInvoiceOutScheduledTask()
    {
        logger.info("OMSInvoiceOutScheduledTask Initialised");
    }

    @Scheduled(cron = "${invoiceout.cron.expression}")
    public void getOMSInvoiceOut()
    {
        logger.info("START-Reading message from OMS INVOIC_OUT_OUT Queue");
        try
        {
            String responseStr= omsOrderService.getOrderInvoiceOutFromOMS();
            OMSOrderResponse omsInvoiceOutResponse = omsOrderService.formatOrderInvoiceOut(responseStr);
            omsOrderService.persistOrderInvoiceOutToDB(omsInvoiceOutResponse);
        }
        catch (OMSException e)
        {
            int errorCode = e.getCode();
            if (errorCode == OMSErrorCodes.RESPONSE_ERROR.getCode())
            {
                // To do Need to check how to get back this transaction or
                // create error table and save there
                logger.info(e.getMessage());
            }
            else
            {
                logger.info(e.getMessage());
            }
        }
        logger.info("END-Reading message from OMS INVOIC_OUT_OUT Queue");

    }

}
