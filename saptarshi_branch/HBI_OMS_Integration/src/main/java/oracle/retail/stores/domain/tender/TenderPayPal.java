
/*
------------------------------------------------------------------------------------------- FILE HISTORY --------------------------------------------------------------------------
FILE VER.   DATE        DEVELOPER     DEEFECT ID/FSD MOD.                                             DESCRIPTION
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    1.    07/05/2015    Jigar N       CR #17 PayPal Redfund			Initial Version- A Tender PayPal Class.
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 */

package oracle.retail.stores.domain.tender;

import java.util.Locale;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.tender.AbstractTenderLineItem;
import oracle.retail.stores.domain.tender.TenderLineItemConstantsIfc;
import oracle.retail.stores.utility.I18NHelper;


public class TenderPayPal extends AbstractTenderLineItem implements TenderPayPalIfc
{

    private static final long serialVersionUID = 1L;
    protected String  referenceID = null;

    /**
     * Constructs TenderPayPal object.
     **/
    public TenderPayPal() {

        typeCode = TenderLineItemConstantsIfc.TENDER_TYPE_PAYPAL;
        setHasDenominations(true);
    }

    /**
     * Constructs TenderSupplierCoupon object with tender amount.
     * 
     * @param tender
     *            amount tendered
     **/
    public TenderPayPal(CurrencyIfc tender) {

        this();
        setAmountTender(tender);
    }

    /**
	 * @return the referenceID
	 */
	public String getReferenceID() 
	{
		return referenceID;
	}

	/**
	 * @param referenceID the referenceID to set
	 */
	public void setReferenceID(String referenceID) 
	{
		this.referenceID = referenceID;
	}

	/**
     * Clone TenderSupplierCoupon object.
     * <P>
     * 
     * @return instance of TenderSupplierCoupon object
     **/
    @Override
    public Object clone() {

        TenderPayPal tc = new TenderPayPal();

        this.setCloneAttributes(tc);
        return tc;
    }



    protected void setCloneAttributes(TenderPayPal newClass) 
    {
    	super.setCloneAttributes(newClass);
        newClass.setReferenceID(getReferenceID());
    }



    /**
     * Returns journal string describing object. May include alternate tender.
     * <P>
     * 
     * @return journal string describing object
     * @deprecated since 7.0, logic moved into ADO layer.
     **/
    @Deprecated
    @Override
    public String toJournalString(Locale journalLocale) 
    {

        String journalString = abstractTenderLineItemAttributesToJournalString(journalLocale);
        StringBuffer sb = new StringBuffer();

        sb.append(journalString);
        String transTypeString;
        if (this.amountTender.signum() == -1) 
        {
            transTypeString = JOURNAL_REVERSED;
        } 
        else 
        {
            transTypeString = JOURNAL_RECEIVED;
        }
        
        Object[] dataArgs = { getTransactionTypeString(), transTypeString };
        sb.append(oracle.retail.stores.common.utility.Util.EOL)
                .append(I18NHelper.getString("EJournal", "JournalEntry.TenderPayPalAmountLabel", dataArgs,
                        journalLocale));

        

        return sb.toString();
    }

    
}
