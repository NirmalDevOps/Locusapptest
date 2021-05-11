package com.htc.orderhivelocus.order;

/**
 * Represents a model class for CreateOrder.
 * 
 * @author Nirmal
 * @version 1.0
 * @since 30-03-2021
 * 
 */
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.connector.util.Constant;
import com.htc.orderhivelocus.impl.CancelOrderImpl;
import com.htc.orderhivelocus.impl.CreateOrderImpl;
import com.htc.orderhivelocus.locusmodel.Body;
import com.htc.orderhivelocus.locusmodel.LocusCancelOrder;
import com.htc.orderhivelocus.orderhivemodel.OrderHive;

public class CreateOrder implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	static final Logger LOGGER = LoggerFactory.getLogger(CreateOrder.class);

	@Override
	public synchronized APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		String locusRequestBuildResponse = null;
		OrderHive orderHive = null;

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			orderHive = objectMapper.readValue(input.getBody(), OrderHive.class);
			LOGGER.info("JSON Structure from WebHook: " + orderHive);
			System.out.println("JSON Structure from WebHook: " + orderHive);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error: " + e);
		}

		// Logic for find out order status
		if (orderHive.getData().getOrder_status().equalsIgnoreCase("cancel")) {
			CancelOrderImpl cancelOrderImpl = new CancelOrderImpl();
			String orderId = orderHive.getData().getChannel_order_id();
			locusRequestBuildResponse = cancelOrderImpl.cancelOrder(orderHive);

			LOGGER.info("locusRequestBuildResponse ======> " + locusRequestBuildResponse);
			if (null != locusRequestBuildResponse) {
				ResponseEntity<String> locusResponse = getResponseFromLocusCancelOrderAPI(locusRequestBuildResponse);
				LOGGER.info("Got the response in main method" + locusResponse);

				if (locusResponse.getStatusCode().value() == (HttpStatus.OK.value())) {
					response = buildLocusCancelOrderResponse(locusResponse, Constant.SUCCESS, orderId);
				} else {
					boolean successFlag = false;
					for (int count = 2; count < Constant.ORDER_CANCELCREATE_COUNT; count++) {

						LOGGER.info("Inside Else:: Count::" + count);
						locusResponse = getResponseFromLocusCancelOrderAPI(locusRequestBuildResponse);
						if (locusResponse.getStatusCode().equals(HttpStatus.OK))
							successFlag = true;
						if (successFlag == true) {
							response = buildLocusCancelOrderResponse(locusResponse, Constant.SUCCESS, orderId);
							break;
						} else {
							if (count == 3) {
								LOGGER.info("Going to build Final Response after hitting three times:");
								response = buildLocusCancelOrderResponse(locusResponse, Constant.FAILURE, orderId);
								break;
							}
						}
					}
				}
			} else {
				response.withStatusCode(HttpStatus.BAD_REQUEST.value());
				response.setBody(Constant.CONVERSION_FAILED);
			}
			return response;

		} else if (orderHive.getData().getOrder_status().equalsIgnoreCase("confirm")) {
			
			System.out.println("Start Time to json conversion :"+System.currentTimeMillis());
			CreateOrderImpl createOrderImpl = new CreateOrderImpl();
			locusRequestBuildResponse = createOrderImpl.createOrder(orderHive);
			System.out.println("End Time to json conversion :"+System.currentTimeMillis());
			
			LOGGER.info("locusRequestBuildResponse===>" + locusRequestBuildResponse);
			System.out.println("locusRequestBuildResponse===>" + locusRequestBuildResponse);

			if (null != locusRequestBuildResponse) {
				ResponseEntity<String> locusResponse = getResponseFromLocusCreateOrderAPI(locusRequestBuildResponse);
				LOGGER.info("Got the response in main method");

				if (locusResponse.getStatusCode().value() == (HttpStatus.OK.value())) {
					LOGGER.info("locusResponse.getStatusCode().value()" + locusResponse.getStatusCode().value() + "\t"
							+ (HttpStatus.OK.value()));
					response = buildLocusCreateOrderResponse(locusResponse, Constant.SUCCESS);
				} else {
					boolean successFlag = false;
					for (int count = 2; count < Constant.ORDER_CANCELCREATE_COUNT; count++) {

						locusResponse = getResponseFromLocusCreateOrderAPI(locusRequestBuildResponse);
						if (locusResponse.getStatusCode().equals(HttpStatus.OK))
							successFlag = true;

						LOGGER.info("Success Flag::" + successFlag);

						if (successFlag == true) {
							response = buildLocusCreateOrderResponse(locusResponse, Constant.SUCCESS);
							break;
						} else {
							if (count == 3) {
								LOGGER.info("Going to build Final Response after hitting three times:");
								response = buildLocusCreateOrderResponse(locusResponse, Constant.FAILURE);
								break;
							}
						}
					}
				}
			} else {
				response.setStatusCode(HttpStatus.CONFLICT.value());
				response.setBody("Error in processing Locus Request Body");
			}
		} else {
			response.setStatusCode(HttpStatus.CONFLICT.value());
			response.setBody("Invalid Order Status");
		}
		System.out.println("Final Response  :" + response);
		return response;
	}

	private APIGatewayProxyResponseEvent buildLocusCreateOrderResponse(ResponseEntity<String> locusResponse,
			String status) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		JSONObject locusAPIResponseJsonObj = null;

		if (status.equalsIgnoreCase(Constant.SUCCESS)) {
			response.setStatusCode(HttpStatus.OK.value());
			// response.setBody(Constant.ORDER_EDIT_SUCCESS_MESSAGE);

			LOGGER.info(Constant.SUCCESS_STATUS_CODE + HttpStatus.OK.value());
			// LOGGER.info(Constant.ORDER_EDIT_SUCCESS_MESSAGE);

		} else {
			response.setStatusCode(locusResponse.getStatusCodeValue());
			// response.setBody(Constant.ORDER_EDIT_FAILED_MESSAGE);
			LOGGER.info(Constant.ERROR_STATUS_CODE + locusResponse.getStatusCodeValue());
			// LOGGER.info(Constant.ORDER_EDIT_FAILED_MESSAGE);
		}

		locusAPIResponseJsonObj = new JSONObject(locusResponse.getBody());
		response.setBody(locusAPIResponseJsonObj.toString());
		return response;
	}

	private APIGatewayProxyResponseEvent buildLocusCancelOrderResponse(ResponseEntity<String> locusResponse,
			String status, String orderId) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		if (status.equalsIgnoreCase(Constant.SUCCESS)) {
			response.setStatusCode(HttpStatus.OK.value());
			response.setBody("Order ID: " + orderId + " Cancelled Successfully");
			LOGGER.info("Success Code : " + HttpStatus.OK.value());
			LOGGER.info("Order ID: " + orderId + " Cancelled Successfully");
		} else {
			response.setStatusCode(locusResponse.getStatusCodeValue());
			response.setBody("Order ID: " + orderId + "cancellation failed");
			LOGGER.info("Error Status Code : " + locusResponse.getStatusCodeValue());
			LOGGER.info("Order ID: " + orderId + " cancellation failed");
		}
		return response;
	}

	private ResponseEntity<String> getResponseFromLocusCancelOrderAPI(String locusRequestJson) {
		ResponseEntity<String> locusResponse = null;

		LocusCancelOrder locusRequestModelObj = null;

		System.out.println("locusRequestJson====>" + locusRequestJson);
		try {
			locusRequestModelObj = objectMapper.readValue(locusRequestJson, LocusCancelOrder.class);
			System.out.println("locusRequestModelObj =====>" + locusRequestModelObj);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error : " + e);
		}
		String requestJson = "";
		try {
			requestJson = objectMapper.writeValueAsString(locusRequestModelObj);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error : " + e);
		}

		StringBuilder locusUrlBuilder = buildCancelOrderURL();

		String finalLocusURL = locusUrlBuilder.toString();

		LOGGER.info("finalLocusURL====>" + finalLocusURL);
		HttpHeaders headers = setHeaderContent();
		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
		HttpEntity<String> entity = new HttpEntity<String>(requestJson.toString(), headers);
		LOGGER.info("HttpEntity Body As aString::" + entity.getBody().toString());
		LOGGER.info("Going to call Locus API::" + locusUrlBuilder);
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(Constant.CLIENT_ID, Constant.CLIENT_AUTHENTICATION));

		// send request and parse result
		locusResponse = restTemplate.exchange(finalLocusURL, HttpMethod.POST, entity, String.class);
		LOGGER.info("Locus response after invoing method" + locusResponse);
		return locusResponse;
	}

	/**
	 * @return
	 */
	private StringBuilder buildCancelOrderURL() {
		StringBuilder locusUrlBuilder = new StringBuilder("https://oms.locus-api.com/v1/client/").append("arki-devo")
				.append("/order-status-update");
		return locusUrlBuilder;
	}

	private ResponseEntity<String> getResponseFromLocusCreateOrderAPI(String locusRequestJson) {
		ResponseEntity<String> locusResponse = null;

		Body locusEditModelObjectBody = null;
		try {
			locusEditModelObjectBody = objectMapper.readValue(locusRequestJson, Body.class);
			LOGGER.info("locusEditModelObjectBody :" + locusEditModelObjectBody);
			System.out.println("locusEditModelObjectBody :" + locusEditModelObjectBody);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error: " + e);
		}

		String requestJson = "";
		try {
			requestJson = objectMapper.writeValueAsString(locusEditModelObjectBody);
			System.out.println("requestJson :" + requestJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		LOGGER.info("locusEditModelObjectBody : " + locusEditModelObjectBody.toString());

		LOGGER.info("Order id:" + locusEditModelObjectBody.getId() + "\t Client Id: "
				+ locusEditModelObjectBody.getClientId());

		System.out.println("Order id:" + locusEditModelObjectBody.getId() + "\t Client Id: "
				+ locusEditModelObjectBody.getClientId());

		StringBuilder locusUrlBuilder = buildCreateOrderURL(locusEditModelObjectBody);

		String locusURL = locusUrlBuilder.toString();
		HttpHeaders headers = setHeaderContent();

		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

		LOGGER.info("Rest Template Obj initialized");

		HttpEntity<String> entity = new HttpEntity<String>(requestJson.toString(), headers);

		LOGGER.info("HttpEntity Body As aString::" + entity.getBody().toString());

		LOGGER.info("Going to call Locus API::" + locusURL);

		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(Constant.CLIENT_ID, Constant.CLIENT_AUTHENTICATION));

		// send request and parse result
		locusResponse = restTemplate.exchange(locusURL, HttpMethod.PUT, entity, String.class);
		return locusResponse;
	}

	/**
	 * @param locusEditModelObjectBody
	 * @return
	 */
	private StringBuilder buildCreateOrderURL(Body locusEditModelObjectBody) {
		StringBuilder locusUrlBuilder = new StringBuilder("https://oms.locus-api.com/v1/client/")
				.append(locusEditModelObjectBody.getClientId()).append("/order/")
				.append(locusEditModelObjectBody.getId()).append("?overwrite=true");
		return locusUrlBuilder;
	}

	private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(Constant.CONNECTION_TIME_OUT);

		// Read timeout
		clientHttpRequestFactory.setReadTimeout(Constant.READING_TIME_OUT);
		return clientHttpRequestFactory;
	}

	private HttpHeaders setHeaderContent() {
		HttpHeaders headers = new HttpHeaders();
		// Base64.Encoder encoder = Base64.getEncoder();

		// String clientIdAndSecret = "AI8VB2XP22X8ZVNWTOWYRZ2BNUDIWF24" + ":" +
		// "46YE18NHS8NKX8XWRCYELN4KVCALC8EA";
		// String clientIdAndSecretBase64 =
		// encoder.encodeToString(clientIdAndSecret.getBytes());

		// System.out.println("Base64 converted value::"+clientIdAndSecretBase64);

		// headers.add("Authorization", "Basic " + clientIdAndSecretBase64);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return headers;
	}
}
