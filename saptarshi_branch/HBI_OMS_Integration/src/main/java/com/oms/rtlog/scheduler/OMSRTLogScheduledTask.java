/**
 * 
 */
package com.oms.rtlog.scheduler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import oracle.retail.stores.domain.manager.rtlog.RTLogExportBatchGeneratorIfc;
import oracle.retail.stores.domain.manager.rtlog.RTLogExportException;

/**
 * @author Jigar
 */
@Component
public class OMSRTLogScheduledTask
{
    protected static final Logger logger = Logger.getLogger(OMSRTLogScheduledTask.class);
    
    @Autowired
    RTLogExportBatchGeneratorIfc batchGenerator;
    
    /**
     * 
     */
    public OMSRTLogScheduledTask()
    {
        logger.info("OMSRTLogScheduledTask Initialised");
    }
    


    @Scheduled(cron = "${rtlog.cron.expression}")
    public void generateRTLog()
    {
        logger.info("RTLOG Generation Started");
        
        try
        {
            batchGenerator.generateBatch();
            logger.info("RTLOG Generation Completed");
        }
        catch (RTLogExportException e)
        {
            logger.error("Error while RTLog Generation");
        }

    }



}
