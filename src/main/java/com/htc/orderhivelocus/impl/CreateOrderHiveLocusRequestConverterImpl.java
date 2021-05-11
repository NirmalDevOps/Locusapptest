package com.htc.orderhivelocus.impl;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.connector.util.Constant;
import com.htc.orderhivelocus.locusmodel.Amount;
import com.htc.orderhivelocus.locusmodel.Body;
import com.htc.orderhivelocus.locusmodel.ContactPoint;
import com.htc.orderhivelocus.locusmodel.DropAmount;
import com.htc.orderhivelocus.locusmodel.LineItems;
import com.htc.orderhivelocus.locusmodel.LocationAddress;
import com.htc.orderhivelocus.locusmodel.Parts;
import com.htc.orderhivelocus.locusmodel.PatchBody;
import com.htc.orderhivelocus.locusmodel.PickupSlot;
import com.htc.orderhivelocus.locusmodel.Price;
import com.htc.orderhivelocus.locusmodel.Skills;
import com.htc.orderhivelocus.locusmodel.Slot;
import com.htc.orderhivelocus.locusmodel.Volume;
import com.htc.orderhivelocus.locusmodel.Weight;
import com.htc.orderhivelocus.orderhivemodel.CustomFieldsListing;
import com.htc.orderhivelocus.orderhivemodel.OrderHive;
import com.htc.orderhivelocus.orderhivemodel.OrderItems;

/**
 * CreateOrderHiveLocusRequestConverterImpl is used to convert the OrderHive
 * request for Locus.
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */

public class CreateOrderHiveLocusRequestConverterImpl {
	
	static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderHiveLocusRequestConverterImpl.class);

	// This method is used to convert the JSON Structure from orderhive to Locus
	public String convertOrderHiveRequestToLocusRequest(OrderHive orderhive) {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		// Converting the Object to JSONString
		
		Body body = new Body();
		PatchBody patchBody = new PatchBody();
		List<LineItems> listOfLineItems = new ArrayList<>();

		// Iterating the OrderItems and setting it to LineItems to Locus
		for (OrderItems orderItems : orderhive.getData().getOrder_items()) {
			LineItems lineItems = new LineItems();
			List<Parts> listOfParts = new ArrayList<Parts>();
			Price price = setPrice(orderItems);
			setLineIems(orderItems, lineItems, listOfParts, price);
			System.out.println("passed 1");
			listOfLineItems.add(lineItems);

		}
		Map<String, String> customProperty = null;
		List<CustomFieldsListing> listOfCustomFieldsListing = orderhive.getData().getCustom_fields_listing();

		// Find the size of CustomFieldsListing if greater then zero then set to custom
		// property
		if (listOfCustomFieldsListing.size() > 0) {
			customProperty = new HashMap<>();
			for (CustomFieldsListing customFieldsListing : listOfCustomFieldsListing) {
				customProperty.put(customFieldsListing.getName(), customFieldsListing.getValue());
			}
			patchBody.setCustomProperties(customProperty);
			System.out.println("passed 2");
		}

		System.out.println("passed 3");
		patchBody.setLineItems(listOfLineItems);
		// Setting the skills
		List<Skills> listOfSkills = new ArrayList<>();
		patchBody.setSkills(listOfSkills);

		patchBody.setPickupLocationId(orderhive.getData().getWarehouse_id());
		patchBody.setPickupVisitName(orderhive.getData().getWarehouse_id());
		System.out.println("passed 4");
		// Setting pickupSlots into the array
		List<PickupSlot> pickupSlots = new ArrayList<>();
		patchBody.setPickupSlots(pickupSlots);
		System.out.println("passed 5");
		// Setting Visit name
		patchBody.setDropVisitName(orderhive.getData().getBilling_address().getName());
		patchBody.setPickupVisitName(orderhive.getData().getShipping_address().getName());

		System.out.println("passed 6");
		// Setting dropContactPoint to locus from billing address of OrderHive
		ContactPoint dropContactPoint = setDropContactPoint(orderhive);
		patchBody.setDropContactPoint(dropContactPoint);

		System.out.println("passed 7");
		// Setting PickupContactPoint to locus from shipping address of OrderHive
		ContactPoint dropPickupContactPoint = setPickupContactPoint(orderhive);
		patchBody.setPickupContactPoint(dropPickupContactPoint);

		System.out.println("passed 8");
		// Setting PickupLocationAddress to locus from shipping address of OrderHive
		LocationAddress pickupLocatonAddress = setPickupLocationAddress(orderhive);
		patchBody.setPickupLocationAddress(pickupLocatonAddress);

		System.out.println("passed 9");
		// Setting dropLocationAddress to locus from billing address of OrderHive
		LocationAddress dropLocatonAddress = setDropLocationAddress(orderhive);
		patchBody.setDropLocationAddress(dropLocatonAddress);

		System.out.println("passed 10");
		// find the current date of system and set it to drop date
		String dropDate = findSystemDate();
		patchBody.setDropDate(dropDate);

		System.out.println("passed 11");
		// Setting the drop slot
		Slot dropSlot = setDropSlot();
		patchBody.setDropSlot(dropSlot);

		// set the drop slots
		List<Slot> dropSlots = setListOfDropSlots(dropSlot);
		patchBody.setDropSlots(dropSlots);

		// set drop amount
		DropAmount dropAmount = setDropAmount(orderhive);
		patchBody.setDropAmount(dropAmount);

		System.out.println("passed 12");
		// set ordered and created date
		String currentDate = findSystemDate();
		patchBody.setOrderedOn(currentDate);
		patchBody.setCreatedOn(currentDate);

		/*
		 * // Checking volume or weight from JSON Structure and set the data
		 * List<OrderItems> order_items = orderhive.getData().getOrder_items(); if
		 * (order_items.size() > 0) { System.out.println("passed 13"); for (OrderItems
		 * orderItems : order_items) { if (orderItems.getVolume() != null) {
		 * System.out.println("passed 14"); Volume volume = new Volume();
		 * volume.setValue(orderItems.getVolume().toString());
		 * volume.setUnit(Constant.VOLUME_UNIT); patchBody.setVolume(volume); break; }
		 * else { System.out.println("passed 15"); Weight weight = new Weight();
		 * weight.setValue((orderItems.getWeight().toString()));
		 * weight.setUnit(Constant.WEIGHT_UNIT); patchBody.setWeight(weight); break; } }
		 * }
		 */
		System.out.println("passed 16");
		body.setClientId(Constant.CLIENT_ID);
		body.setId(orderhive.getData().getChannel_order_id());

		body.setPatchBody(patchBody);
		String createOrderJSONString = null;
		try {
			System.out.println("passed 17");
			createOrderJSONString = mapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			System.out.println("passed 18");
			LOGGER.error("Error: " + e);
		}
		System.out.println("passed 19");
		System.out.println("Converted response for locus by orderhive" + createOrderJSONString);
		return createOrderJSONString;

	}

	/**
	 * @param orderItems
	 * @param lineItems
	 * @param listOfParts
	 * @param price
	 */
	private void setLineIems(OrderItems orderItems, LineItems lineItems, List<Parts> listOfParts, Price price) {
		lineItems.setPrice(price);
		lineItems.setId(orderItems.getSku());
		lineItems.setNote(orderItems.getNote());
		lineItems.setLineItemId(orderItems.getItem_id().toString());
		lineItems.setName(orderItems.getName());
		lineItems.setQuantity(Double.parseDouble(orderItems.getQuantity_ordered().toString()));
		lineItems.setQuantityUnit(Constant.QUANTITY_UNIT);
		lineItems.setParts(listOfParts);
	}

	/**
	 * @param : This method receives the orderItems object as args.
	 * @return : price as obj
	 */
	private Price setPrice(OrderItems orderItems) {
		Price price = new Price();
		price.setAmount((double) orderItems.getPrice());
		price.setCurrency(Constant.CURRENCY_SYMBOL);
		price.setSymbol(Constant.CURRENCY_SYMBOL);
		return price;
	}

	/**
	 * Note : Exchange Type allowed only : [NONE/COLLECT/GIVE]
	 * 
	 * @param : This method receives the orderhive object as args.
	 * @return : dropAmount
	 */
	private DropAmount setDropAmount(OrderHive orderhive) {
		DropAmount dropAmount = new DropAmount();
		setAmount(orderhive);

		dropAmount.setExchangeType(Constant.EXCHANGE_TYPE);
		return dropAmount;
	}

	/**
	 * @param : This method receives the orderhive object as args.
	 */
	private void setAmount(OrderHive orderhive) {
		Amount amount = new Amount();
		amount.setAmount(orderhive.getData().getTotal());
		amount.setCurrency(Constant.CURRENCY_SYMBOL);
		amount.setSymbol(Constant.CURRENCY_SYMBOL);
		DropAmount dropAmount = new DropAmount();
		dropAmount.setAmount(amount);
	}

	/**
	 * @param : This method receives the dropSlotDateTime object as args.
	 * @return : dropSlots as array list
	 */
	private List<Slot> setListOfDropSlots(Slot dropSlotDateTime) {
		List<Slot> dropSlots = new ArrayList<Slot>();
		dropSlots.add(dropSlotDateTime);
		dropSlots.add(dropSlotDateTime);
		return dropSlots;
	}

	/**
	 * @return : dropSlot as obj.
	 */
	private Slot setDropSlot() {
		Slot dropSlot = new Slot();
		dropSlot.setStart(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		dropSlot.setEnd(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		return dropSlot;
	}

	/**
	 * Setting Pickup location address from shipping to orderhive
	 * 
	 * @param :This method receives the orderhive object as args.
	 * @return : pickupLocationAddress
	 */
	private LocationAddress setPickupLocationAddress(OrderHive orderhive) {
		LocationAddress pickupLocationAddress = new LocationAddress();
		pickupLocationAddress.setPlaceName(orderhive.getData().getShipping_address().getCompany());
		pickupLocationAddress.setLocalityName(orderhive.getData().getShipping_address().getAddress2());
		pickupLocationAddress.setFormattedAddress(orderhive.getData().getShipping_address().getAddress1());
		pickupLocationAddress.setPincode(orderhive.getData().getShipping_address().getZipcode());
		pickupLocationAddress.setCity(orderhive.getData().getShipping_address().getCity());
		pickupLocationAddress.setState(orderhive.getData().getShipping_address().getState());
		pickupLocationAddress.setCountryCode(orderhive.getData().getShipping_address().getCountry_code());
		return pickupLocationAddress;
	}

	/**
	 * Setting Drop Location Address from shipping to orderhive
	 * 
	 * @param : This method receives the orderhive object as args.
	 * @return : dropLocationAddress
	 */
	private LocationAddress setDropLocationAddress(OrderHive orderhive) {
		LocationAddress dropLocationAddress = new LocationAddress();
		dropLocationAddress.setPlaceName(orderhive.getData().getBilling_address().getCompany());
		dropLocationAddress.setLocalityName(orderhive.getData().getBilling_address().getAddress2());
		dropLocationAddress.setFormattedAddress(orderhive.getData().getBilling_address().getAddress1());
		dropLocationAddress.setPincode(orderhive.getData().getBilling_address().getZipcode());
		dropLocationAddress.setCity(orderhive.getData().getBilling_address().getCity());
		dropLocationAddress.setState(orderhive.getData().getBilling_address().getState());
		dropLocationAddress.setCountryCode(orderhive.getData().getBilling_address().getCountry_code());
		return dropLocationAddress;
	}

	/**
	 * Find out current date in the dd/MM/yyyy format.
	 * 
	 * @return : deliveryDate
	 */
	private String findSystemDate() {
		Date date = new Date();
		String currentDate = new SimpleDateFormat(Constant.DATE_FORMAT).format(date);
		return currentDate;
	}

	/**
	 * @param :This method receives the orderhive object as args.
	 * @return : contactPoint
	 */
	private ContactPoint setPickupContactPoint(OrderHive orderhive) {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setName(orderhive.getData().getShipping_address().getName());
		contactPoint.setNumber(orderhive.getData().getShipping_address().getContact_number());
		return contactPoint;
	}

	/**
	 * @param :This method receives the orderhive object as args.
	 * @return : contactPoint
	 */
	private ContactPoint setDropContactPoint(OrderHive orderhive) {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setName(orderhive.getData().getBilling_address().getName());
		contactPoint.setNumber(orderhive.getData().getBilling_address().getContact_number());
		return contactPoint;
	}

}
