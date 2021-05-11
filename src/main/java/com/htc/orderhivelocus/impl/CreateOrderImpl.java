package com.htc.orderhivelocus.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.htc.orderhivelocus.orderhivemodel.OrderHive;

/**
 * Represents a CreateOrderImpl class.
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */
public class CreateOrderImpl {
	String jsonResponseForLocus = null;
	static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderHiveLocusRequestConverterImpl.class);
	public String createOrder(OrderHive orderhive) {
		CreateOrderHiveLocusRequestConverterImpl orderHiveLocusRequestConverterImpl = new CreateOrderHiveLocusRequestConverterImpl();

		try {
			jsonResponseForLocus = orderHiveLocusRequestConverterImpl.convertOrderHiveRequestToLocusRequest(orderhive);
			LOGGER.info("JSON Response successfully converted for locus"+jsonResponseForLocus);
		} catch (Exception e) {
			LOGGER.error("Error: "+e);
			
		}
		return jsonResponseForLocus;
	}
}
