package com.htc.orderhivelocus.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.htc.orderhivelocus.orderhivemodel.OrderHive;


/**
 * Represents a CancelOrderImpl service class
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */
public class CancelOrderImpl {

	static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderHiveLocusRequestConverterImpl.class);
	String jsonResponseForLocus = null;
	public String cancelOrder(OrderHive orderHive) 
	{
		CancelOrderHiveLocusRequestConverterImpl orderConverterImpl = new CancelOrderHiveLocusRequestConverterImpl();
		
		try {
			jsonResponseForLocus = orderConverterImpl.convertOrderHiveRequestToLocusRequest(orderHive);
			LOGGER.info("JSON Response successfully converted for locus"+jsonResponseForLocus);
		}catch(Exception e) {
			LOGGER.error("Error: "+e);
		}
		return jsonResponseForLocus;
	}
} 
