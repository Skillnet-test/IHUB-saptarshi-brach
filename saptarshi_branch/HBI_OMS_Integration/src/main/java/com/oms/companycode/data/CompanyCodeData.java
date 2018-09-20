package com.oms.companycode.data;

import java.io.Serializable;
import org.springframework.stereotype.Component;

@Component
public class CompanyCodeData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String company_Code;
    
	

	
	private String company_Name;

    private String currency_Code;

    private String company_Desc;

    private String warehouse_Number;

    
    public String getCompany_Code() {
		return company_Code;
	}

	public void setCompany_Code(String company_Code) {
		this.company_Code = company_Code;
	}

	public String getCompany_Name() {
		return company_Name;
	}

	public void setCompany_Name(String company_Name) {
		this.company_Name = company_Name;
	}

	public String getCurrency_Code() {
		return currency_Code;
	}

	public void setCurrency_Code(String currency_Code) {
		this.currency_Code = currency_Code;
	}

	public String getCompany_Desc() {
		return company_Desc;
	}

	public void setCompany_Desc(String company_Desc) {
		this.company_Desc = company_Desc;
	}

	public String getWarehouse_Number() {
		return warehouse_Number;
	}

	public void setWarehouse_Number(String warehouse_Number) {
		this.warehouse_Number = warehouse_Number;
	}

    
	@Override
	public String toString() {
		return "CompanyCodeData [company_Code=" + company_Code
				+ ", company_Name=" + company_Name + ", currency_Code="
				+ currency_Code + ", company_Desc=" + company_Desc
				+ ", warehouse_Number=" + warehouse_Number + "]";
	}
    	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((company_Code == null) ? 0 : company_Code.hashCode());
		result = prime * result
				+ ((company_Desc == null) ? 0 : company_Desc.hashCode());
		result = prime * result
				+ ((company_Name == null) ? 0 : company_Name.hashCode());
		result = prime * result
				+ ((currency_Code == null) ? 0 : currency_Code.hashCode());
		result = prime
				* result
				+ ((warehouse_Number == null) ? 0 : warehouse_Number.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompanyCodeData other = (CompanyCodeData) obj;
		if (company_Code == null) {
			if (other.company_Code != null)
				return false;
		} else if (!company_Code.equals(other.company_Code))
			return false;
		if (company_Desc == null) {
			if (other.company_Desc != null)
				return false;
		} else if (!company_Desc.equals(other.company_Desc))
			return false;
		if (company_Name == null) {
			if (other.company_Name != null)
				return false;
		} else if (!company_Name.equals(other.company_Name))
			return false;
		if (currency_Code == null) {
			if (other.currency_Code != null)
				return false;
		} else if (!currency_Code.equals(other.currency_Code))
			return false;
		if (warehouse_Number == null) {
			if (other.warehouse_Number != null)
				return false;
		} else if (!warehouse_Number.equals(other.warehouse_Number))
			return false;
		return true;
	}


	
	

}
