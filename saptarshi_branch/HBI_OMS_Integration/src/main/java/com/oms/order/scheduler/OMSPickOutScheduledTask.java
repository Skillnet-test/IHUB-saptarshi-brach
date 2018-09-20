/**
 * 
 */
package com.oms.order.scheduler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.common.OrderConstantIfc;
import com.oms.order.formatter.OMSCustHistResponse;
import com.oms.order.formatter.OMSOrderResponse;
import com.oms.order.service.CustHistInOrderService;
import com.oms.order.service.PickOutOrderService;
import com.payment.dao.PaymentManager;
import com.payment.data.PaymentData;

/**
 * @author Jigar
 */
@Component
public class OMSPickOutScheduledTask
{
    protected static final Logger logger = Logger.getLogger(OMSPickOutScheduledTask.class);

    @Autowired
    PickOutOrderService pickOutOrderService;

    @Autowired
    CustHistInOrderService custHistInOrderService;

    @Autowired
    PaymentManager paymentManager;

    /**
     * 
     */
    public OMSPickOutScheduledTask()
    {
        logger.info("OMSPickOutScheduledTask Initialised");
    }

    @Scheduled(cron = "${pickout.cron.expression}")
    public void getOMSInvoiceOut()
    {
        logger.info("Reading OMS Pick OUT Message");
        OMSOrderResponse orderFromOMS = null;
        try
        {
            orderFromOMS = pickOutOrderService.getOrderFromOMS();
        }
        catch (OMSException oe)
        {
            int errorCode = oe.getCode();
            if (errorCode == OMSErrorCodes.RESPONSE_ERROR.getCode())
            {
                // To do Need to check how to get back this transaction or
                // create error table and save there
                logger.info(oe.getMessage());
            }
            else
            {
                logger.info(oe.getMessage());
            }

        }
        if (StringUtils.isNotBlank(orderFromOMS.getStatus()))
        {
            if (!StringUtils.equalsIgnoreCase(orderFromOMS.getStatus(),
                    OrderConstantIfc.INVOICE_OUT_RESPONSE_STATUS_EOF))
            {
                PaymentData paymentData = paymentManager.parsePaymentData(orderFromOMS);
                // PayId will get from CUSTHistory
                OMSCustHistResponse omsCustHistResponse;
                try
                {
                    omsCustHistResponse = custHistInOrderService.getOrderFromOMS(paymentData.getOrderid());

                    String creditCardAuthNumber = omsCustHistResponse.getCreditCardAuthNumber();
                    if (creditCardAuthNumber != null)
                    {
                        paymentData.setPayid(creditCardAuthNumber);
                    }
                    else
                    {
                        paymentData.setPayid("NOT");
                    }
                    // paymentData.setPayid("3027070765");
                    paymentManager.insertPaymentData(paymentData);
                }
                catch (OMSException e)
                {
                    int errorCode = e.getCode();
                    if (errorCode == OMSErrorCodes.CONNECT_ERROR.getCode()
                            || errorCode == OMSErrorCodes.REQUEST_PARSE_ERROR.getCode())
                    {
                        // To do retry logic for cust hist service
                        logger.info(e.getMessage());
                    }
                    else if (errorCode == OMSErrorCodes.RESPONSE_ERROR.getCode())
                    {
                        // To do Need to check how to get back this transaction
                        // or
                        // create error table and save there
                        logger.info(e.getMessage());
                    }
                    else
                    {
                        logger.info(e.getMessage());
                    }
                }
            }
        }
        logger.info("OMS Pick OUT Ended");

    }

}
