/**
 * 
 */
package com.oms.order.formatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.companycode.dao.CompanyCodeManager;
import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.common.OrderConstantIfc;
import com.oms.order.common.PaymentMethodEnum;
import com.oms.order.dao.OMSOrderDao;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.CustomerSoldTo;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoicePaymentMethods.InvoicePaymentMethod;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoiceShipTos.InvoiceShipTo;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoiceShipTos.InvoiceShipTo.InvoiceDetails.InvoiceDetail;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoiceShipTos.InvoiceShipTo.InvoiceDetails.InvoiceDetail.OrderDetail;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoiceShipTos.InvoiceShipTo.OrderShipTo;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoiceShipTos.InvoiceShipTo.OrderShipTo.OrderMessages.OrderMessage;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.OrderHeader;
//import com.payment.dao.CompanyCodeDao;


import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.financial.ShippingMethodConstantsIfc;
import oracle.retail.stores.domain.financial.ShippingMethodIfc;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.OrderItemStatusIfc;
import oracle.retail.stores.domain.lineitem.OrderLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.lineitem.SendPackageLineItemIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc.AuthorizationMethod;
import oracle.retail.stores.domain.order.OrderStatusIfc;
import oracle.retail.stores.domain.stock.ItemClassificationIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.DepartmentIfc;
import oracle.retail.stores.domain.tender.TenderChargeIfc;
import oracle.retail.stores.domain.tender.TenderCheckIfc;
import oracle.retail.stores.domain.tender.TenderDebitIfc;
import oracle.retail.stores.domain.tender.TenderGiftCertificateIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.tender.TenderTypeMapIfc;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIDIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.utility.AddressConstantsIfc;
import oracle.retail.stores.domain.utility.AddressIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EntryMethod;
import oracle.retail.stores.domain.utility.PhoneConstantsIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;



/**
 * @author Jigar
 *
 */
@Component
public class OMSOrderFormatter
{
    @Value("${oms.workstationId}")
    public String WORKSTATION_ID;
    
    @Value("${oms.tillId}")
    public String TILL_ID;
    
    @Value("${oms.employeeId}")
    public String EMPLOYEE_ID;
    
    @Value("${oms.orderShippingCarrier}")
    public String ORDER_SHIPPING_CARRIER;
    
    @Autowired
    OMSOrderDao omsOrderDao;
    
    /*@Autowired
    CompanyCodeDao companyCodeDao;*/
    
    @Autowired
    CompanyCodeManager companyCodeManager;
    
	private static final Logger logger = Logger.getLogger(OMSOrderFormatter.class);
    /**
     * 
     */
    public OMSOrderFormatter()
    {
        // TODO Auto-generated constructor stub
    }
    
    
    
    public OMSOrderResponse formatInvoiceOutToResponseObject(String invoiceOutResponseStr) 
	{
      OMSOrderResponse invoiceOutResponse = new OMSOrderResponse();
	  ObjectMapper mapper =  new ObjectMapper();
      try 
      {
    	  if(StringUtils.isNotEmpty(invoiceOutResponseStr))
    	  {
	          JSONObject invoiceOutJSONObject = new JSONObject(invoiceOutResponseStr);
	         
	          String invoiceOutResponseStatus=invoiceOutJSONObject.getString(OrderConstantIfc.INVOICE_OUT_JSON_STATUS_KEY);
	          invoiceOutResponse.setStatus(invoiceOutResponseStatus);
	 
	          if(invoiceOutResponseStatus.equalsIgnoreCase(OrderConstantIfc.INVOICE_OUT_RESPONSE_STATUS_OK))
	          {
	            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	            InvoiceOutMessage invoiceOutMessage=mapper.readValue( XML.toJSONObject(StringEscapeUtils.unescapeJava(invoiceOutResponseStr)).get(OrderConstantIfc.INVOICE_OUT_JSON_MESSAGE_KEY).toString(),InvoiceOutMessage.class);
	            invoiceOutResponse.setInvoiceOutMessage(invoiceOutMessage);
	          }
    	  }
      } 
      catch (Exception e) 
      {
    	  invoiceOutResponse.setStatus(OrderConstantIfc.INVOICE_OUT_RESPONSE_STATUS_FAILED);
    	  logger.error("Exception caught while reading Invoice Out Queue "+e.getStackTrace());
      }
	  return invoiceOutResponse;
	}



	public OrderTransactionIfc formatInvoicOutResponseOrderTransaction(OMSOrderResponse omsInvoiceOutResponse) 
	{
		InvoiceOutMessage invoiceOutMessage=omsInvoiceOutResponse.getInvoiceOutMessage();
		OrderTransactionIfc orderTransaction =null;
		
		if(invoiceOutMessage!=null && invoiceOutMessage.getInvoiceHeader()!=null && invoiceOutMessage.getInvoiceHeader().getOrderHeader()!=null)
		{
			InvoiceHeader invoiceHeader=invoiceOutMessage.getInvoiceHeader();
			CustomerSoldTo omsCustomer=invoiceHeader.getCustomerSoldTo();
			OrderHeader omsOrderHeader=invoiceHeader.getOrderHeader();
			
			List<InvoicePaymentMethod> omsInvoicePaymentMethodList=null;
			if(invoiceHeader.getInvoicePaymentMethods()!=null)
				omsInvoicePaymentMethodList = invoiceHeader.getInvoicePaymentMethods().getInvoicePaymentMethod();
			
			List<InvoiceShipTo> omsInvoiceShipToList=null;
			if(invoiceHeader.getInvoiceShipTos()!=null)
				omsInvoiceShipToList = invoiceHeader.getInvoiceShipTos().getInvoiceShipTo();
	
			orderTransaction= DomainGateway.getFactory().getOrderTransactionInstance();
			
			setOrderDetails(orderTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
			
			if(omsCustomer!=null)
			{
				setCustomerDetails(orderTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
			}
			
			if(omsInvoiceShipToList!=null)
			{
				setSaleReturnLineItemDetails(orderTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
			}
			
			if(omsInvoicePaymentMethodList!=null)
			{
				setFreightLineItemDetails(orderTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
				setTenderDetails(orderTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
			}
			
			orderTransaction.getOrderStatus().setSaleAmount(orderTransaction.getTransactionTotals().getGrandTotal());
		}

		return orderTransaction;
	}

	private void setOrderDetails(OrderTransactionIfc orderTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
			CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
			List<InvoiceShipTo> omsInvoiceShipToList) 
	 {
			/* Initialise Transaction ID */
			TransactionIDIfc transId= DomainGateway.getFactory().getTransactionIDInstance();
			transId.setBusinessDate(DomainGateway.getFactory().getEYSDateInstance());
			//transId.setStoreID(companyCodeDao.getWareHouseNumber(invoiceHeader.getIhdCompany()));companyCodeManager
			transId.setStoreID(companyCodeManager.getWareHouseNumber(invoiceHeader.getIhdCompany()));
			transId.setWorkstationID(WORKSTATION_ID);
			transId.setSequenceNumber(Long.parseLong(omsOrderDao.getTransactionSequenceNo(transId.getStoreID())));
		
		 
		 	/* Set Order Details */
			orderTransaction.initialize(transId);
			orderTransaction.setOrderID(invoiceHeader.getIhdOrderNbr());
			orderTransaction.setOrderType(OrderConstantIfc.ORDER_TYPE_WEB);
			orderTransaction.setTillID(TILL_ID);
			orderTransaction.setTransactionStatus(TransactionConstantsIfc.STATUS_COMPLETED);
			
			OrderStatusIfc orderStatus= DomainGateway.getFactory().getOrderStatusInstance();
			orderStatus.initializeStatus();

			EYSDate orderDate= convertToEYSDate(omsOrderHeader.getOhdOrderDate());
	        orderStatus.setTimestampBegin(orderDate);
	        orderStatus.setTimestampCreated();
	        orderStatus.setInitialTransactionBusinessDate(DomainGateway.getFactory().getEYSDateInstance());
	        orderStatus.setInitialTransactionID(transId);
	        orderStatus.setRecordingTransactionID(transId);
	        
			orderStatus.setOrderID(invoiceHeader.getIhdOrderNbr());
			
			int intiatingChannel=OrderConstantIfc.ORDER_CHANNEL_WEB;
			String[] orderChannelList=OrderConstantIfc.ORDER_CHANNEL_DESCRIPTORS;
			for(int i=0;i<orderChannelList.length;i++)
			{
				if(StringUtils.isNotEmpty(omsOrderHeader.getOhdOrderChannel()) && orderChannelList[i].equalsIgnoreCase(omsOrderHeader.getOhdOrderChannel()))
				{
					intiatingChannel=i;
					break;
				}
			}
			orderStatus.setInitiatingChannel(intiatingChannel);
			
			orderStatus.setOrderType(OrderConstantIfc.ORDER_TYPE_WEB);
			orderStatus.getStatus().setStatus(OrderConstantIfc.ORDER_STATUS_NEW);
			orderStatus.getStatus().setLastStatusChange(orderStatus.getTimestampCreated());
			
			EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
			employee.setEmployeeID(EMPLOYEE_ID);
			orderTransaction.setCashier(employee);
			orderTransaction.setSalesAssociate(employee);
			
	        orderTransaction.setTimestampBegin();
	        orderTransaction.setTimestampEnd();
			orderTransaction.setOrderStatus(orderStatus);
			orderTransaction.setTransactionType(TransactionIfc.TYPE_ORDER_INITIATE);
			orderTransaction.setOrderType(OrderConstantIfc.ORDER_TYPE_WEB);
	}
	
	
	 private void setCustomerDetails(SaleReturnTransactionIfc orderTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
				CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
				List<InvoiceShipTo> omsInvoiceShipToList) 
	 {
			 /* Set Customer Details */

			CustomerIfc customer = DomainGateway.getFactory().getCustomerInstance();
				
			if(StringUtils.isNotEmpty(omsCustomer.getSoldToEvePhone()))
			{
				customer.setCustomerID(omsCustomer.getSoldToEvePhone());
			}
			else
			{
				customer.setCustomerID(omsCustomer.getSoldToNbr());
			}
			
			customer.setSalutation(omsCustomer.getSoldToPrefix());
			customer.setFirstName(omsCustomer.getSoldToFname());
			customer.setMiddleName(omsCustomer.getSoldToInitial());
			customer.setLastName(omsCustomer.getSoldToLname());
			
			
			AddressIfc address = DomainGateway.getFactory().getAddressInstance();
			address.setAddressType(AddressConstantsIfc.ADDRESS_TYPE_HOME);
			
			address.setPostalCode(omsCustomer.getSoldToPoBox());
			
			Vector<String> addressLines = new Vector<String>();
			addressLines.add(omsCustomer.getSoldToAddress1());
			addressLines.add(omsCustomer.getSoldToAddress2());
			address.setLines(addressLines);

			address.setCity(omsCustomer.getSoldToCity());
			address.setState(omsCustomer.getSoldToStateName());
			address.setCountry(omsCustomer.getSoldToCountryName());

			customer.addAddress(address);
				
			customer.setEMailAddress(omsCustomer.getSoldToEmailAddress());
				
			Vector<PhoneIfc> phoneVector = new Vector<PhoneIfc>();
				
			if(omsCustomer.getSoldToDayPhone()!=null)
			{
				PhoneIfc workPhone= DomainGateway.getFactory().getPhoneInstance();
				workPhone.setPhoneType(PhoneConstantsIfc.PHONE_TYPE_WORK);
				workPhone.setExtension(omsCustomer.getSoldToDayExt());
				workPhone.setPhoneNumber(omsCustomer.getSoldToDayPhone());
				phoneVector.addElement(workPhone);
			}

			customer.setPhones(phoneVector);
				
			orderTransaction.setCustomer(customer);	
			
	}
	 
	 
	private void setSaleReturnLineItemDetails(OrderTransactionIfc orderTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
				CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
				List<InvoiceShipTo> omsInvoiceShipToList) 
	{
			Vector<TransactionDiscountStrategyIfc> discountLineItemsVector = new Vector<TransactionDiscountStrategyIfc>();
			
			/* Set Line Item Details */
			int lineItemCnt=0;
			Vector<OrderLineItemIfc> orderLineItemsVector = new Vector<OrderLineItemIfc>();
			for(int i=0;i<omsInvoiceShipToList.size();i++)
			{
				InvoiceShipTo invoiceShipTo=omsInvoiceShipToList.get(i);
				
				/* Set Transaction Discounts*/
				OrderShipTo orderShipTo= invoiceShipTo.getOrderShipTo();
				if(orderShipTo.getOrderMessages()!=null)
				{
					List<OrderMessage> orderMessageList= orderShipTo.getOrderMessages().getOrderMessage();
					for(int j=0;j<orderMessageList.size();j++)
					{
						OrderMessage ordMessage= orderMessageList.get(j);
				
						
						CurrencyIfc rewardcurrency=DomainGateway.getBaseCurrencyInstance();
				        TransactionDiscountStrategyIfc RfllineItem = DomainGateway.getFactory().getTransactionDiscountByAmountInstance();
				        StringTokenizer st = new StringTokenizer(ordMessage.getOrderMessageText(), "|");
				        List<String> elements = new ArrayList<String>();
				        while(st.hasMoreTokens()) 
				        {
				           elements.add(st.nextToken());
				        }
				        
				        LocalizedCodeIfc reasonCode = DomainGateway.getFactory().getLocalizedCode();
				        reasonCode.setCode(OrderConstantIfc.RFL_REASON_CODE);
			            reasonCode.setCodeName(OrderConstantIfc.RFL_REASON_CODE_NAME);
			            
			            
				        RfllineItem.setReason(reasonCode);
				        RfllineItem.setReferenceID(elements.get(3));
				        RfllineItem.setAssignmentBasis(DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);
	
				        rewardcurrency.setDecimalValue(rewardcurrency.getDecimalValue().add(BigDecimal.valueOf(Double.parseDouble(elements.get(2)))));
				        RfllineItem.setDiscountAmount(rewardcurrency);
				        discountLineItemsVector.addElement(RfllineItem);
					}
				
				}
	
				

			    List<InvoiceDetail> invoiceDetailList= invoiceShipTo.getInvoiceDetails().getInvoiceDetail();	
			    
				for(int k=0;k<invoiceDetailList.size();k++)
				{
					InvoiceDetail invoiceDetail= invoiceDetailList.get(k);
					OrderDetail itemOrderDetail=invoiceDetail.getOrderDetail();
					OrderLineItemIfc orderLineItem = DomainGateway.getFactory().getOrderLineItemInstance();
					orderLineItem.setLineNumber(lineItemCnt++);
					
					PLUItemIfc pluItem = DomainGateway.getFactory().getPLUItemInstance();
					pluItem.setItemID(itemOrderDetail.getOdtItem());
					pluItem.setDescription(LocaleMap.getDefaultLocale(),itemOrderDetail.getOdtItemDescription());
					DepartmentIfc itmDepartment= DomainGateway.getFactory().getDepartmentInstance();
					itmDepartment.setDepartmentID(OrderConstantIfc.ORDER_ITEM_DEPT);

					ItemClassificationIfc itmClassification = DomainGateway.getFactory().getItemClassificationInstance();
					itmClassification.setMerchandiseHierarchyGroup(OrderConstantIfc.ORDER_ITEM_MERCH_HRCHY_GROUP);
					
					pluItem.setItemClassification(itmClassification);
					
					pluItem.setDepartment(itmDepartment);
					
					CurrencyIfc itemOriginalAmount = DomainGateway.getBaseCurrencyInstance(itemOrderDetail.getOdtOriginalPrice());
					
					CurrencyIfc itemAmount = DomainGateway.getBaseCurrencyInstance(itemOrderDetail.getOdtPrice());		
					pluItem.setPrice(itemAmount);
					
					
					
	                ItemPriceIfc itemPrice = DomainGateway.getFactory().getItemPriceInstance();
	                
	                //Set Discount Eligible to False if the Ship To does not contains the Order Message ( RFL Desc)
	                if(discountLineItemsVector!=null && discountLineItemsVector.size()<=0)
	                {
	                	itemPrice.setDiscountEligible(false);
	                }

	                itemPrice.setPermanentSellingPrice(itemOriginalAmount.abs());
	                itemPrice.setSellingPrice(itemAmount.abs());
	                itemPrice.setExtendedSellingPrice(itemAmount.abs().multiply(new BigDecimal(itemOrderDetail.getOdtOrderedQty())));

	                //itemPrice.setExtendedDiscountedSellingPrice(itemAmount.abs().abs().multiply(new BigDecimal(itemOrderDetail.getOdtOrderedQty())));
	                itemPrice.setItemQuantity(new BigDecimal(itemOrderDetail.getOdtOrderedQty()));
	                orderLineItem.setItemPrice(itemPrice);
					
					orderLineItem.setPLUItem(pluItem);
					orderLineItem.setItemQuantity(new BigDecimal(itemOrderDetail.getOdtOrderedQty()));
					orderLineItem.setOrderID(invoiceHeader.getIhdOrderNbr());
					orderLineItem.setOrderLineReference(Integer.valueOf(invoiceDetail.getIdtSeqNbr()));
					
					OrderItemStatusIfc orderItemStatus = DomainGateway.getFactory().getOrderItemStatusInstance();
					orderItemStatus.getStatus().setStatus(OrderConstantIfc.ORDER_STATUS_NEW);
					orderLineItem.setOrderItemStatus(orderItemStatus);
					
					orderLineItemsVector.add(orderLineItem);
				}
			}
			
			TransactionDiscountStrategyIfc[] transactionDiscounts = new TransactionDiscountStrategyIfc[discountLineItemsVector.size()];
		    discountLineItemsVector.toArray(transactionDiscounts);
		    orderTransaction.setTransactionDiscounts(transactionDiscounts);
			
			OrderLineItemIfc[] orderLineItems = new OrderLineItemIfc[orderLineItemsVector.size()];
			orderLineItemsVector.toArray(orderLineItems);
			orderTransaction.setLineItems(orderLineItems);
			
			recalculateItemPrice(orderTransaction);

			
	}
	 
	private void setFreightLineItemDetails(SaleReturnTransactionIfc orderTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
				CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
				List<InvoiceShipTo> omsInvoiceShipToList) 
	{
		
		CurrencyIfc freightAmt=null;
 		for(int i=0;i<omsInvoicePaymentMethodList.size();i++)
		{
			InvoicePaymentMethod invoicePayment=omsInvoicePaymentMethodList.get(i);
			if(StringUtils.isNotEmpty(invoicePayment.getIpmOrdLvlFrtAmt()))
			{
				freightAmt=DomainGateway.getBaseCurrencyInstance(invoicePayment.getIpmOrdLvlFrtAmt());
			}
		}
		if(freightAmt!=null && !freightAmt.equals(DomainGateway.getBaseCurrencyInstance()))
		{
			SendPackageLineItemIfc shippingLineItem=DomainGateway.getFactory().getSendPackageLineItemInstance();
			
			shippingLineItem.setCustomer(orderTransaction.getCustomer());
			shippingLineItem.setFromTransaction(true);
			
			ShippingMethodIfc shippingMethod= DomainGateway.getFactory().getShippingMethodInstance();
			shippingMethod.setTaxable(false);
			shippingMethod.setCalculatedShippingCharge(freightAmt);
			shippingMethod.setShippingCarrier(LocaleMap.getDefaultLocale(), ORDER_SHIPPING_CARRIER);
			shippingMethod.setShippingMethodID(0);
			shippingMethod.setShippingType(LocaleMap.getDefaultLocale(), ShippingMethodConstantsIfc.FLAT_RATE);
			
			shippingLineItem.setShippingMethod(shippingMethod);
			
			ItemTaxIfc shippingItemTax= DomainGateway.getFactory().getItemTaxInstance();
			shippingItemTax.setTaxable(false);
			
			shippingLineItem.setItemTax(shippingItemTax);
			
			orderTransaction.getTransactionTotals().addSendPackage(shippingLineItem);
		}
	} 
	 
	 
	private void recalculateItemPrice(SaleReturnTransactionIfc orderTransaction) 
	{
		SaleReturnLineItemIfc[] orderLineItems = (SaleReturnLineItemIfc[]) orderTransaction.getLineItems();
		for(int i=0;i<orderLineItems.length;i++)
		{
            ItemPriceIfc itemPrice = orderLineItems[i].getItemPrice();

            itemPrice.setPermanentSellingPrice(itemPrice.getPermanentSellingPrice().add(itemPrice.getItemTransactionDiscountAmount()));
            itemPrice.setSellingPrice(itemPrice.getSellingPrice().add(itemPrice.getItemTransactionDiscountAmount()));
            itemPrice.setExtendedSellingPrice(itemPrice.getExtendedSellingPrice().add(itemPrice.getItemTransactionDiscountAmount()));
            orderLineItems[i].setItemPrice(itemPrice);
		}
		
		orderTransaction.setLineItems(orderLineItems);
		orderTransaction.setTransactionDiscounts(orderTransaction.getTransactionDiscounts());
		
	}



	private void setTenderDetails(SaleReturnTransactionIfc orderTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
				CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
				List<InvoiceShipTo> omsInvoiceShipToList) 
	{
		
			/* Set Tender Details */
			
			Vector<TenderLineItemIfc> tenderLineItems = new Vector<TenderLineItemIfc>();
			for(int i=0;i<omsInvoicePaymentMethodList.size();i++)
			{
				InvoicePaymentMethod invoicePayment=omsInvoicePaymentMethodList.get(i);
				TenderLineItemIfc tenderLineItem = null;
				tenderLineItem=instantiateTenderLineItem(PaymentMethodEnum.fromValue(Integer.parseInt(invoicePayment.getIpmPayType())),invoicePayment);
				tenderLineItem.setLineNumber(i+1);
				CurrencyIfc merchandiseAmt=DomainGateway.getBaseCurrencyInstance(invoicePayment.getIpmMerchandiseAmt());
				CurrencyIfc orderLevelFreightAmt=DomainGateway.getBaseCurrencyInstance(invoicePayment.getIpmOrdLvlFrtAmt());
				tenderLineItem.setAmountTender(merchandiseAmt.add(orderLevelFreightAmt));
				tenderLineItems.add(tenderLineItem);
			}
			
		    TenderLineItemIfc[] tenderItems = new TenderLineItemIfc[tenderLineItems.size()];
		    tenderLineItems.toArray(tenderItems);
			orderTransaction.setTenderLineItems((TenderLineItemIfc[]) tenderItems);
			
	}
	 
	 
	public OrderTransactionIfc getCompletedOrderTransaction(OrderTransactionIfc orderTransaction) 
	{
			OrderTransactionIfc completedOrderTransaction=null;
		
			if(orderTransaction!=null)
			{
				completedOrderTransaction = DomainGateway.getFactory().getOrderTransactionInstance();
			    completedOrderTransaction=(OrderTransactionIfc) orderTransaction.clone();
	
				
				TransactionIDIfc completeTransId= orderTransaction.getTransactionIdentifier();
				completeTransId.setBusinessDate(DomainGateway.getFactory().getEYSDateInstance());
				completeTransId.setSequenceNumber(Long.parseLong(omsOrderDao.getTransactionSequenceNo(completeTransId.getStoreID())));
				completedOrderTransaction.initialize(completeTransId);
	
				completedOrderTransaction.getOrderStatus().getStatus().setStatus(OrderConstantIfc.ORDER_STATUS_COMPLETED);
				completedOrderTransaction.setTransactionType(TransactionIfc.TYPE_ORDER_COMPLETE);
				SaleReturnLineItemIfc[] completedOrderLineItemsList = (SaleReturnLineItemIfc[]) completedOrderTransaction.getLineItems();
				
				for(int i=0;i<completedOrderLineItemsList.length;i++)
				{
					completedOrderLineItemsList[i].getOrderItemStatus().getStatus().setStatus(OrderLineItemIfc.ORDER_ITEM_STATUS_PICKED_UP);
				}
				
				completedOrderTransaction.setLineItems(completedOrderLineItemsList);
			}
		
			return completedOrderTransaction;
	}
	 

	protected TenderLineItemIfc instantiateTenderLineItem(PaymentMethodEnum paymentMethodEnum, InvoicePaymentMethod invoicePayment) 
	{
		 String payType=paymentMethodEnum.getPayMethodType();
		 if (logger.isDebugEnabled())
	            logger.debug("JdbcReadTransaction.instantiateTenderLineItem()");

	        TenderLineItemIfc tenderLineItem = null;
	        TenderTypeMapIfc tenderTypeMap = DomainGateway.getFactory().getTenderTypeMapInstance();

	        if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_CASH)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderCash");
	            tenderLineItem = DomainGateway.getFactory().getTenderCashInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_CHARGE)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderCharge");
	            tenderLineItem = DomainGateway.getFactory().getTenderChargeInstance();
				((TenderChargeIfc)tenderLineItem).setEntryMethod(EntryMethod.Unknown);
				((TenderChargeIfc)tenderLineItem).setAuthorizationMethod(AuthorizationMethod.Automatic.name());
				((TenderChargeIfc)tenderLineItem).setCardType(paymentMethodEnum.getSubType());
				((TenderChargeIfc)tenderLineItem).setCardNumber(invoicePayment.getIpmCreditCardNbr());
				((TenderChargeIfc)tenderLineItem).setAuthorizationCode(invoicePayment.getIpmAuthNumber());
				((TenderChargeIfc)tenderLineItem).setExpirationDateString(invoicePayment.getIpmCcExpirationDate());
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_CHECK)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderCheck");
	            tenderLineItem = DomainGateway.getFactory().getTenderCheckInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_TRAVELERS_CHECK)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderTravelersCheck");
	            tenderLineItem = DomainGateway.getFactory().getTenderTravelersCheckInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_GIFT_CARD)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderGiftCard");
	            tenderLineItem = DomainGateway.getFactory().getTenderGiftCardInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_GIFT_CERTIFICATE)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderGiftCertificate");
	            tenderLineItem = DomainGateway.getFactory().getTenderGiftCertificateInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_MAIL_BANK_CHECK)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderMailBankCheck");
	            tenderLineItem = DomainGateway.getFactory().getTenderMailBankCheckInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_DEBIT)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderDebit");
	            tenderLineItem = DomainGateway.getFactory().getTenderDebitInstance();
				((TenderDebitIfc)tenderLineItem).setEntryMethod(EntryMethod.Unknown);
				((TenderDebitIfc)tenderLineItem).setAuthorizationMethod(AuthorizationMethod.Automatic.name());
	            ((TenderDebitIfc)tenderLineItem).setCardType(paymentMethodEnum.getSubType());
				((TenderDebitIfc)tenderLineItem).setCardNumber(invoicePayment.getIpmCreditCardNbr());
				((TenderDebitIfc)tenderLineItem).setAuthorizationCode(invoicePayment.getIpmAuthNumber());
				((TenderDebitIfc)tenderLineItem).setExpirationDateString(invoicePayment.getIpmCcExpirationDate());
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_COUPON)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderCoupon");
	            tenderLineItem = DomainGateway.getFactory().getTenderCouponInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_STORE_CREDIT)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderStoreCredit");
	            tenderLineItem = DomainGateway.getFactory().getTenderStoreCreditInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_MALL_GIFT_CERTIFICATE)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderMallCertificate");
	            tenderLineItem = DomainGateway.getFactory().getTenderGiftCertificateInstance();
	            ((TenderGiftCertificateIfc)tenderLineItem).setTypeCode(TenderLineItemIfc.TENDER_TYPE_MALL_GIFT_CERTIFICATE);
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_PURCHASE_ORDER)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderPurchaseOrder");
	            tenderLineItem = DomainGateway.getFactory().getTenderPurchaseOrderInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_MONEY_ORDER)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderMoneyOrder");
	            tenderLineItem = DomainGateway.getFactory().getTenderMoneyOrderInstance();
	        }
	        else if (payType.equals(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_E_CHECK)))
	        {
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TenderECheck");
	            tenderLineItem = DomainGateway.getFactory().getTenderCheckInstance();
	            ((TenderCheckIfc)tenderLineItem).setTypeCode(TenderLineItemIfc.TENDER_TYPE_E_CHECK);
	        }
	        else if(payType.equalsIgnoreCase(tenderTypeMap.getCode(TenderLineItemIfc.TENDER_TYPE_PAYPAL))){
	            if (logger.isInfoEnabled())
	                logger.info("Instantiating TendePayPal");
	            tenderLineItem = DomainGateway.getFactory().getTenderPayPalInstance();
	        }
	        else
	        {
	            logger.error("don't know how to instantiate tender type: " + paymentMethodEnum + "");
	        }


	        return (tenderLineItem);
	 }
	
	
	 private static EYSDate convertToEYSDate(String date)
	 {
	     EYSDate eysDate = null;
	     if(StringUtils.isNotEmpty(date))
	     {
		      String entereddate=date;
		      Date dateObj;
		      try 
		      {
		    	  dateObj = new SimpleDateFormat(OrderConstantIfc.DATE_FORMAT_YYYY_MM_DD).parse(entereddate);
		    	  eysDate = new EYSDate(dateObj);
		      } 
		      catch (ParseException e) 
		      {
		    	  logger.info("Exception caught in OMSOrderFormatter class in method convertToEYSDate while parsing Date.");
		    	  eysDate=DomainGateway.getFactory().getEYSDateInstance();
		      } 
	     }
	     else
	     {
	    	 eysDate=DomainGateway.getFactory().getEYSDateInstance();
	     }
	     return eysDate;
	 }



	public int getTransactionType(OMSOrderResponse omsInvoiceOutResponse) 
	{
		InvoiceOutMessage invoiceOutMessage=omsInvoiceOutResponse.getInvoiceOutMessage();
		
		if(invoiceOutMessage!=null && invoiceOutMessage.getInvoiceHeader()!=null)
		{
			InvoiceHeader invoiceHeader=invoiceOutMessage.getInvoiceHeader();
		
			if(invoiceHeader.getIhdInvoiceType().equalsIgnoreCase(OrderConstantIfc.INVOICE_TYPE_SALES))
			{
				return TransactionConstantsIfc.TYPE_SALE;
			}
			else if(invoiceHeader.getIhdInvoiceType().equalsIgnoreCase(OrderConstantIfc.INVOICE_TYPE_RETURN))
			{
				return TransactionConstantsIfc.TYPE_RETURN;
			}
		}
		return TransactionConstantsIfc.TYPE_UNKNOWN;
	}



	public SaleReturnTransactionIfc formatInvoicOutResponseToSaleReturnTransaction(
			OMSOrderResponse omsInvoiceOutResponse) 
	{

		InvoiceOutMessage invoiceOutMessage=omsInvoiceOutResponse.getInvoiceOutMessage();
		InvoiceHeader invoiceHeader=invoiceOutMessage.getInvoiceHeader();
		CustomerSoldTo omsCustomer=invoiceHeader.getCustomerSoldTo();
		OrderHeader omsOrderHeader=invoiceHeader.getOrderHeader();
		List<InvoicePaymentMethod> omsInvoicePaymentMethodList = invoiceHeader.getInvoicePaymentMethods().getInvoicePaymentMethod();
		List<InvoiceShipTo> omsInvoiceShipToList = invoiceHeader.getInvoiceShipTos().getInvoiceShipTo();

		SaleReturnTransactionIfc orderReturnTransaction= DomainGateway.getFactory().getSaleReturnTransactionInstance();
		
		setOrderDetails(orderReturnTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
		setCustomerDetails(orderReturnTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
		setSaleReturnLineItemDetails(orderReturnTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
		setFreightLineItemDetails(orderReturnTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
		setTenderDetails(orderReturnTransaction,omsOrderHeader,invoiceHeader,omsCustomer,omsInvoicePaymentMethodList,omsInvoiceShipToList);
		return orderReturnTransaction;
	}



	private void setOrderDetails(SaleReturnTransactionIfc orderReturnTransaction, OrderHeader omsOrderHeader,
			InvoiceHeader invoiceHeader, CustomerSoldTo omsCustomer,
			List<InvoicePaymentMethod> omsInvoicePaymentMethodList, List<InvoiceShipTo> omsInvoiceShipToList) 
	{
		/* Initialise Transaction ID */
		TransactionIDIfc transId= DomainGateway.getFactory().getTransactionIDInstance();
		transId.setBusinessDate(DomainGateway.getFactory().getEYSDateInstance());
		//transId.setStoreID(companyCodeDao.getWareHouseNumber(invoiceHeader.getIhdCompany()));
		transId.setStoreID(companyCodeManager.getWareHouseNumber(invoiceHeader.getIhdCompany()));
		transId.setWorkstationID(WORKSTATION_ID);
		transId.setSequenceNumber(Long.parseLong(omsOrderDao.getTransactionSequenceNo(transId.getStoreID())));
	
	 
	 	/* Set Order Details */
		orderReturnTransaction.initialize(transId);
		orderReturnTransaction.setTillID(TILL_ID);
		orderReturnTransaction.setTransactionType(TransactionConstantsIfc.TYPE_RETURN);
		orderReturnTransaction.setTransactionStatus(TransactionConstantsIfc.STATUS_COMPLETED);

		

		
		EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
		employee.setEmployeeID(EMPLOYEE_ID);
		orderReturnTransaction.setCashier(employee);
		orderReturnTransaction.setSalesAssociate(employee);
		
		orderReturnTransaction.setTimestampBegin();
		orderReturnTransaction.setTimestampEnd();
		
	}
	
	private void setSaleReturnLineItemDetails(SaleReturnTransactionIfc orderReturnTransaction, OrderHeader omsOrderHeader, InvoiceHeader invoiceHeader,
			CustomerSoldTo omsCustomer, List<InvoicePaymentMethod> omsInvoicePaymentMethodList,
			List<InvoiceShipTo> omsInvoiceShipToList) 
	{
		Vector<TransactionDiscountStrategyIfc> discountLineItemsVector = new Vector<TransactionDiscountStrategyIfc>();
		
		/* Set Line Item Details */
		int lineItemCnt=0;
		Vector<SaleReturnLineItemIfc> orderLineItemsVector = new Vector<SaleReturnLineItemIfc>();
		for(int i=0;i<omsInvoiceShipToList.size();i++)
		{
			InvoiceShipTo invoiceShipTo=omsInvoiceShipToList.get(i);
			
			/* Set Transaction Discounts*/
			OrderShipTo orderShipTo= invoiceShipTo.getOrderShipTo();
			if(orderShipTo.getOrderMessages()!=null)
			{
				List<OrderMessage> orderMessageList= orderShipTo.getOrderMessages().getOrderMessage();
				for(int j=0;j<orderMessageList.size();j++)
				{
					OrderMessage ordMessage= orderMessageList.get(j);
			
					
					CurrencyIfc rewardcurrency=DomainGateway.getBaseCurrencyInstance();
			        TransactionDiscountStrategyIfc RfllineItem = DomainGateway.getFactory().getTransactionDiscountByAmountInstance();
			        StringTokenizer st = new StringTokenizer(ordMessage.getOrderMessageText(), "|");
			        List<String> elements = new ArrayList<String>();
			        while(st.hasMoreTokens()) 
			        {
			           elements.add(st.nextToken());
			        }
			        
			        LocalizedCodeIfc reasonCode = DomainGateway.getFactory().getLocalizedCode();
			        reasonCode.setCode(OrderConstantIfc.RFL_REASON_CODE);
		            reasonCode.setCodeName(OrderConstantIfc.RFL_REASON_CODE_NAME);
		            
		            
			        RfllineItem.setReason(reasonCode);
			        RfllineItem.setReferenceID(elements.get(3));
			        RfllineItem.setAssignmentBasis(DiscountRuleConstantsIfc.ASSIGNMENT_MANUAL);

			        rewardcurrency.setDecimalValue(rewardcurrency.getDecimalValue().add(BigDecimal.valueOf(Double.parseDouble(elements.get(2)))));
			        RfllineItem.setDiscountAmount(rewardcurrency);
			        discountLineItemsVector.addElement(RfllineItem);
				}
			
			}

			

		    List<InvoiceDetail> invoiceDetailList= invoiceShipTo.getInvoiceDetails().getInvoiceDetail();	
		    
			for(int k=0;k<invoiceDetailList.size();k++)
			{
				InvoiceDetail invoiceDetail= invoiceDetailList.get(k);
				OrderDetail itemOrderDetail=invoiceDetail.getOrderDetail();
				SaleReturnLineItemIfc orderReturnLineItem = DomainGateway.getFactory().getSaleReturnLineItemInstance();
				orderReturnLineItem.setLineNumber(lineItemCnt++);
				
				PLUItemIfc pluItem = DomainGateway.getFactory().getPLUItemInstance();
				pluItem.setItemID(itemOrderDetail.getOdtItem());
				pluItem.setDescription(LocaleMap.getDefaultLocale(),itemOrderDetail.getOdtItemDescription());
				DepartmentIfc itmDepartment= DomainGateway.getFactory().getDepartmentInstance();
				itmDepartment.setDepartmentID("0");

				ItemClassificationIfc itmClassification = DomainGateway.getFactory().getItemClassificationInstance();
				itmClassification.setMerchandiseHierarchyGroup("0");
				
				pluItem.setItemClassification(itmClassification);
				
				pluItem.setDepartment(itmDepartment);
				
				CurrencyIfc itemOriginalAmount = DomainGateway.getBaseCurrencyInstance(itemOrderDetail.getOdtOriginalPrice());
				
				CurrencyIfc itemAmount = DomainGateway.getBaseCurrencyInstance(itemOrderDetail.getOdtPrice());		
				pluItem.setPrice(itemAmount);
				
				
				
                ItemPriceIfc itemPrice = DomainGateway.getFactory().getItemPriceInstance();
                
                //Set Discount Eligible to False if the Ship To does not contains the Order Message ( RFL Desc)
                if(discountLineItemsVector!=null && discountLineItemsVector.size()<=0)
                {
                	itemPrice.setDiscountEligible(false);
                }

                BigDecimal itmReturnedQty= new BigDecimal(itemOrderDetail.getOdtReturnedQty()).negate();
                itemPrice.setPermanentSellingPrice(itemOriginalAmount);
                itemPrice.setSellingPrice(itemAmount);
                itemPrice.setExtendedSellingPrice(itemAmount.multiply(itmReturnedQty));

                //itemPrice.setExtendedDiscountedSellingPrice(itemAmount.abs().abs().multiply(new BigDecimal(itemOrderDetail.getOdtOrderedQty())));
                itemPrice.setItemQuantity(itmReturnedQty);
                orderReturnLineItem.setItemPrice(itemPrice);
                
				
                orderReturnLineItem.setPLUItem(pluItem);
                orderReturnLineItem.setItemQuantity(itmReturnedQty);
                
                orderReturnLineItem.setWebOrderNumber(invoiceHeader.getIhdOrderNbr());
                orderReturnLineItem.setWebOrderItemSeqNo(Integer.valueOf(invoiceDetail.getIdtSeqNbr()));
    			EYSDate orderBusinessDate= convertToEYSDate(omsOrderHeader.getOhdOrderDate());
    			orderReturnLineItem.setWebOrderBusinessDate(orderBusinessDate);
    			orderReturnLineItem.setWebOrderReturnItem(true);
				
    			orderReturnLineItem.calculateLineItemPrice();
				orderLineItemsVector.add(orderReturnLineItem);
			}
		}
		
		TransactionDiscountStrategyIfc[] transactionDiscounts = new TransactionDiscountStrategyIfc[discountLineItemsVector.size()];
	    discountLineItemsVector.toArray(transactionDiscounts);
	    orderReturnTransaction.setTransactionDiscounts(transactionDiscounts);
		
		SaleReturnLineItemIfc[] orderReturnLineItems = new SaleReturnLineItemIfc[orderLineItemsVector.size()];
		orderLineItemsVector.toArray(orderReturnLineItems);
		orderReturnTransaction.setLineItems(orderReturnLineItems);

		recalculateItemPrice(orderReturnTransaction);

		
	}


}
