package com.payment.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.oms.companycode.dao.CompanyCodeManager;
import com.oms.exception.OMSErrorCodes;
import com.oms.exception.OMSException;
import com.oms.order.formatter.OMSOrderResponse;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoicePaymentMethods;
import com.oms.order.formatter.schema.invoiceout.InvoiceOutMessage.InvoiceHeader.InvoicePaymentMethods.InvoicePaymentMethod;
import com.oms.order.formatter.schema.pickout.PickOutMessage;
import com.oms.order.formatter.schema.pickout.PickOutMessage.PickHeader;
import com.oms.order.formatter.schema.pickout.PickOutMessage.PickHeader.PickDetails;
import com.oms.order.formatter.schema.pickout.PickOutMessage.PickHeader.PickDetails.PickDetail;
import com.payment.constant.PaymentConstantIfc;
import com.payment.data.PaymentData;
import com.payment.formatter.PaymentResponse;
import com.payment.service.PaymentService;

@Component
public class PaymentManager
{

    private static final Logger logger = Logger.getLogger(PaymentManager.class);

    @Autowired
    PaymentDao paymentDao;

    @Autowired
    PaymentService paymentService;

    @Value("${ingenico.pspid}")
    private String ingenicoPspid;

    @Value("${ingenico.userid}")
    private String ingenicoUserid;

    @Value("${ingenico.password}")
    private String ingenicoPassword;

   // @Autowired
    //CompanyCodeDao companyCodes;
    
    @Autowired
    CompanyCodeManager companyCodeManager;

    public void paymentSettlement()
    {
        List<PaymentData> paymentDataList = null;
        try
        {
            paymentDataList = paymentDao.getPaymentDatabyStatus();
        }
        catch (OMSException oe)
        {
            logger.error(((OMSException)oe).getMessage());
        }
        PaymentResponse paymentResponse = null;

        for (PaymentData paymentData : paymentDataList)
        {
            try
            {
                paymentResponse = paymentService.processPayment(paymentData);
                paymentDao.updateResponse(paymentResponse);
            }
            catch (Exception e)
            {
                if (e instanceof OMSException)
                {
                    int errorCode = ((OMSException)e).getCode();

                    if (errorCode == OMSErrorCodes.CONNECT_ERROR.getCode())
                    {
                        logger.error(OMSErrorCodes.CONNECT_ERROR.getDescription());
                    }
                    else if (errorCode == OMSErrorCodes.RESPONSE_ERROR.getCode())
                    {
                        logger.error(OMSErrorCodes.RESPONSE_ERROR.getDescription());
                        paymentResponse = new PaymentResponse();
                        paymentResponse.setResponse("Failed");
                        paymentResponse.setNcresponse("-2");
                        paymentResponse.setPayment_id(paymentData.getPayment_id());
                        paymentDao.updateResponse(paymentResponse);
                        // continue;
                    }
                }
                else
                {
                    logger.error("Unknown Exception Occurred ", e);
                    paymentResponse = new PaymentResponse();
                    paymentResponse.setResponse("Failed");
                    paymentResponse.setNcresponse("-2");
                    paymentResponse.setPayment_id(paymentData.getPayment_id());
                    paymentDao.updateResponse(paymentResponse);
                }
            }
        }
    }

    public PaymentData parsePaymentData(OMSOrderResponse orderFromOMS)
    {

        PaymentData paymentData = new PaymentData();
        PickOutMessage pickOutMessage = orderFromOMS.getPickOutMessage();
        PickHeader pickHeader = null;
        double sellingPriceExtended = 0.0;
        double freightamt = 0.0;
        String freightAmountString = null;
        if (pickOutMessage != null)
        {
            pickHeader = pickOutMessage.getPickHeader();
            if (pickHeader != null)
            {
                String orderNbr = pickHeader.getOrderNbr();
                String company_Code = pickHeader.getCompany();
                if (StringUtils.isNotBlank(orderNbr))
                {
                    paymentData.setOrderid(orderNbr);
                }
                if (StringUtils.isNotBlank(company_Code))
                {
                    paymentData.setCompany_Code(company_Code);
                   // paymentData.setCurrency(companyCodes.getCurrencyCode(company_Code));
                    paymentData.setCurrency(companyCodeManager.getCurrencyCode(company_Code));
                }
                freightAmountString = pickHeader.getFreightAmt();
                if (StringUtils.isNotBlank(freightAmountString))
                {
                    freightamt += Double.parseDouble(freightAmountString);
                }

                PickDetails pickDetails = pickHeader.getPickDetails();
                if (pickDetails != null)
                {
                    List<PickDetail> pickDetailList = pickDetails.getPickDetail();
                    sellingPriceExtended = 0.0;
                    for (PickDetail pickDetail : pickDetailList)
                    {
                        sellingPriceExtended += Double.parseDouble(pickDetail.getSellingPriceExtended());

                    }
                }

            }

        }

        paymentData.setPspid(ingenicoPspid);
        paymentData.setUserid(ingenicoUserid);
        paymentData.setPswd(ingenicoPassword);
        paymentData.setOperation(PaymentConstantIfc.SAL);
        double amount = sellingPriceExtended + freightamt;
        paymentData.setAmount(String.valueOf(amount));

        return paymentData;

    }

    public void insertPaymentData(PaymentData paymentData)
    {
        paymentDao.insertPaymentData(paymentData);

    }

    public PaymentData parseRefundData(OMSOrderResponse omsInvoiceOutResponse)
    {

        String company_Code = null;
        PaymentData paymentData = new PaymentData();
        paymentData.setPspid(ingenicoPspid);
        paymentData.setUserid(ingenicoUserid);
        paymentData.setPswd(ingenicoPassword);
        paymentData.setOperation(PaymentConstantIfc.RFD);

        InvoiceOutMessage invoiceOutMessage = omsInvoiceOutResponse.getInvoiceOutMessage();
        double freightamt = 0.0;
        double sellingPriceExtended = 0.0;
        if (invoiceOutMessage != null)
        {
            InvoiceHeader invoiceHeader = invoiceOutMessage.getInvoiceHeader();

            if (invoiceHeader != null)
            {
                String orderNbr = invoiceHeader.getIhdOrderNbr();
                company_Code = invoiceHeader.getIhdCompany();

                if (StringUtils.isNotBlank(orderNbr))
                {
                    paymentData.setOrderid(orderNbr);
                }

                if (StringUtils.isNotBlank(company_Code))
                {
                    paymentData.setCompany_Code(company_Code);
                   // paymentData.setCurrency(companyCodes.getCurrencyCode(company_Code));
                    paymentData.setCurrency(companyCodeManager.getCurrencyCode(company_Code));
                }

                InvoicePaymentMethods invoicePaymentMethods = invoiceHeader.getInvoicePaymentMethods();
                if (invoicePaymentMethods != null)
                {
                    List<InvoicePaymentMethod> invoicePaymentMethodList = invoicePaymentMethods
                            .getInvoicePaymentMethod();

                    for (InvoicePaymentMethod invoicePaymentMethod : invoicePaymentMethodList)
                    {
                        freightamt += Double.parseDouble(invoicePaymentMethod.getIpmFreightAmt());
                        sellingPriceExtended += Double.parseDouble(invoicePaymentMethod.getIpmDepositAmount());
                    }
                }
            }
        }

        double amount = sellingPriceExtended + freightamt;
        paymentData.setAmount(String.valueOf(amount));

        return paymentData;
    }

}
