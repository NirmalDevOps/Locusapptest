package com.htc.orderhivelocus.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.orderhivelocus.locusmodel.LocusCancelOrder;
import com.htc.orderhivelocus.locusmodel.CancelOrderStatusMessage;
import com.htc.orderhivelocus.locusmodel.Filters;
import com.htc.orderhivelocus.locusmodel.OrderSelectRequest;
import com.htc.orderhivelocus.orderhivemodel.OrderHive;



/**
 * Represents a CancelOrderHiveLocusRequestConverterImpl service class
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */

public class CancelOrderHiveLocusRequestConverterImpl {

	static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderHiveLocusRequestConverterImpl.class);

	public String convertOrderHiveRequestToLocusRequest(OrderHive orderhive) {

		LocusCancelOrder cancelOrder = null;
		CancelOrderStatusMessage cancelOrderStatusMessage = null;
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		// Converting the Object to JSONString
		String cancelOrderJSONString = null;

		
		// To cancel the order checking the order status and order id
		if (!orderhive.getData().getChannel_order_id().isEmpty()
				&& (orderhive.getData().getOrder_status().equalsIgnoreCase("cancel"))) {
			cancelOrder = new LocusCancelOrder();
			cancelOrder.setOrderStatus(orderhive.getData().getOrder_status().toUpperCase().concat("LED"));

			OrderSelectRequest orderSelectRequest = new OrderSelectRequest();

			List<Filters> listOfFilters = new ArrayList<Filters>();
			//setting filters
			Filters filters = setFilters(orderhive);
			listOfFilters.add(filters);
			orderSelectRequest.setFilters(listOfFilters);
			cancelOrder.setOrderSelectRequest(orderSelectRequest);
			try {
				cancelOrderJSONString = mapper.writeValueAsString(cancelOrder);
				LOGGER.info("CancelOrderJSONString successfully converted: "+cancelOrderJSONString);
			} catch (JsonProcessingException e) {
				LOGGER.error("Error: "+e);
			}
			return cancelOrderJSONString;
		} else {
			cancelOrderStatusMessage = new CancelOrderStatusMessage();
			cancelOrderStatusMessage.setMessage("Order Id should not be empty and status should have cancel");
			LOGGER.error("Error: "+cancelOrderStatusMessage);
			try {
				cancelOrderJSONString = mapper.writeValueAsString(cancelOrderStatusMessage);

			} catch (JsonProcessingException e) {
				LOGGER.error("Error: "+e);
			}
			return cancelOrderJSONString;
		}
	}

	/**
	 * This method is used to set the filter
	 * 
	 * @param orderhive
	 * @return
	 */
	private Filters setFilters(OrderHive orderhive) {
		Filters filters = new Filters();
		filters.setName("id");
		filters.setOperation("EQUALS");
		String[] values = new String[1];
		values[0] = orderhive.getData().getChannel_order_id();
		filters.setValues(values);
		return filters;
	}

}
