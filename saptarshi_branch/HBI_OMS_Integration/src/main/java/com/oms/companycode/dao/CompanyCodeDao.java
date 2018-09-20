package com.oms.companycode.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oms.companycode.constants.CompanyCodeConstantIfc;
import com.oms.companycode.data.CompanyCodeData;
import com.payment.constant.PaymentConstantIfc;

@Component
public class CompanyCodeDao implements ApplicationListener<ApplicationReadyEvent>, CompanyCodeConstantIfc
{
    /**
     * @author Vishal M
     */

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */

    public List<CompanyCodeData> companyCodes = null;

    public Map<String, CompanyCodeData> companyCodeMap = null;

    private CompanyCodeData companyData;

    private String currencycode;

    private String paymentIntegration;

    private String warehouseNumber;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    CompanyCodeData comapanyData;
    
    @Autowired
    CompanyCodeManager companyCodeManager;

    private static final Logger logger = Logger.getLogger(CompanyCodeDao.class.getName());

    @Transactional(readOnly = true)

    /*
     * onApplicationEvent() will be called as soon as application is up and
     * before any scheduler start running. method will load the country codes
     * from DB at startup to be used by application.
     */
    @Override

    public void onApplicationEvent(final ApplicationReadyEvent event)
    {

        companyCodes = jdbcTemplate.query(SQL_READ_ALL_COMPANY_CODE,
                new BeanPropertyRowMapper(CompanyCodeData.class));

        companyCodeMap = new HashMap<String, CompanyCodeData>(companyCodes.size());

        for (final CompanyCodeData companyData : companyCodes)
        {

            companyCodeMap.put(companyData.getCompany_Code(), companyData);
        }

        logger.info("Loading Company details" + companyCodeMap);
        
        companyCodeManager.getCompanyCode();

        //getWareHouseNumber("3");
        //getPaymentIntegrationCompany("3");
        //getCurrencyCode("3");

    }
    
    public Map<String, CompanyCodeData> getCompanyCodeMap(){
    	return companyCodeMap;
    }

    
}