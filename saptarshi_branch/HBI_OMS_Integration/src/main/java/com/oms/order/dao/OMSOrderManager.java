package com.oms.order.dao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oms.order.formatter.OMSOrderFormatter;
import com.oms.order.formatter.OMSOrderResponse;
import com.payment.dao.PaymentManager;
import com.payment.data.PaymentData;

import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;

@Component
public class OMSOrderManager
{
	private static final Logger logger = Logger.getLogger(OMSOrderManager.class);
	
    @Autowired
    PaymentManager paymentManager;
    
    @Autowired
    OMSOrderFormatter omsOrderFormatter;
    
    @Autowired
    OMSOrderDao omsOrderDao;
    


	public void persistOMSInvoiceOut(OMSOrderResponse omsInvoiceOutResponse)
	{
		int transactionType = omsOrderFormatter.getTransactionType(omsInvoiceOutResponse);
		
		if(transactionType==TransactionConstantsIfc.TYPE_SALE)
		{
			OrderTransactionIfc orderTransaction = omsOrderFormatter.formatInvoicOutResponseOrderTransaction(omsInvoiceOutResponse);
			omsOrderDao.persistOMSInvoiceOut(orderTransaction);
			
			OrderTransactionIfc completedOrderTransaction= omsOrderFormatter.getCompletedOrderTransaction(orderTransaction);
			omsOrderDao.persistOMSInvoiceOut(completedOrderTransaction);
		}
		else if (transactionType==TransactionConstantsIfc.TYPE_RETURN)
		{
			SaleReturnTransactionIfc orderReturnTransaction = omsOrderFormatter.formatInvoicOutResponseToSaleReturnTransaction(omsInvoiceOutResponse);
			omsOrderDao.persistOMSInvoiceOut(orderReturnTransaction);
			PaymentData paymentData = paymentManager.parseRefundData(omsInvoiceOutResponse);
			paymentManager.insertPaymentData(paymentData);
		}
		else
		{
			logger.info("Unknow Transaction type found in Order Invoice Out.");
		}
		
	}
}
