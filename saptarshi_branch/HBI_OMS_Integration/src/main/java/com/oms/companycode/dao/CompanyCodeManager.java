package com.oms.companycode.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.oms.companycode.dao.CompanyCodeDao;
import com.oms.companycode.data.CompanyCodeData;
import com.oms.message.Messages;
import com.oms.order.connector.OMSOrderConnector;
import java.util.logging.*;

@Component
public class CompanyCodeManager {
	
		
	@Autowired
    CompanyCodeDao companyDao;	
	
	List<CompanyCodeData> companyDataList;
	Map<String, CompanyCodeData> hMapCompanyCode;
	
	private static final Logger logger = Logger.getLogger(CompanyCodeManager.class.getName());
	
	/*@Value("${message}")
	public String message;*/
	
	@Autowired
    Messages messages;
	
	@Autowired
	CompanyCodeData companyCodeData;
	
	
	
	public CompanyCodeManager(){
		
	}
	
	

	public void getCompanyCode() 
    {
		
		//System.out.println("saptarshi here in companycode manager at start");
		
		hMapCompanyCode=new HashMap<String, CompanyCodeData>();
		companyCodeData=new CompanyCodeData();
		if(hMapCompanyCode.size()==0){
			hMapCompanyCode.putAll(companyDao.getCompanyCodeMap());
		}
		
		
		
			
    }
	
	// for getting company description
	public String getCompanyDescription(String comCode){
		
		if(hMapCompanyCode.get(comCode)instanceof CompanyCodeData && hMapCompanyCode.size()>0){
			return hMapCompanyCode.get(comCode).getCompany_Desc();
		}else
		
		return messages.get("default.title");
		
	}
	
	// for getting company currency code
	public String getCurrencyCode(String comCode){
		
		if(hMapCompanyCode.get(comCode)instanceof CompanyCodeData && hMapCompanyCode.size()>0){
			return ((CompanyCodeData)hMapCompanyCode.get(comCode)).getCurrency_Code();
		}else
		
		return messages.get("default.title");
	}
	
	// for getting payment integration
	public String getPaymentIntegration(String comCode){
		
		if(hMapCompanyCode.get(comCode)instanceof CompanyCodeData && hMapCompanyCode.size()>0){
			return ((CompanyCodeData)hMapCompanyCode.get(comCode)).getCompany_Name();
		}else
		
		return messages.get("default.title");
		
	}
	
	// for getting ware house number
	public String getWareHouseNumber(String comCode){
		
		if(hMapCompanyCode.get(comCode)instanceof CompanyCodeData && hMapCompanyCode.size()>0){
			return ((CompanyCodeData)hMapCompanyCode.get(comCode)).getWarehouse_Number();
		}else
		
		return messages.get("default.title");
	}
	
	

}
