/**
 * 
 */
package com.htc.connector.util;

/**
 * Represents a Constant class for all the constant values.
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */
public class Constant {
	
	public static final int ORDER_CANCELCREATE_COUNT=4;
	public static final String SUCCESS = "Success";
	public static final String FAILURE = "Failure";
	public static final String QUANTITY_UNIT ="PC"; 
	public static final String UNIT ="CM";
	public static final String VOLUME_UNIT ="CM";
	public static final String WEIGHT_UNIT ="KG";
	public static final String CURRENCY_SYMBOL="AUD";
	public static final String  DATE_FORMAT="yyyy-MM-dd";
	public static final String SUCCESS_STATUS_CODE="Success Status Code : ";
	public static final String ERROR_STATUS_CODE="Error Status Code : ";
	public static final String  ORDER_CREATE_SUCCESS_MESSAGE="Order created successfully!!!";
	public static final String  ORDER_CREATE_FAILED_MESSAGE="Order creation failed!!!";
	// Select any one for exchange type [NONE/COLLECT/GIVE]
	public static final String EXCHANGE_TYPE="NONE";
	public static final int CONNECTION_TIME_OUT=20_000;
	public static final int READING_TIME_OUT=20_000;
	public static final String CLIENT_ID="arki-devo";
	public static final String CLIENT_AUTHENTICATION="167eb8d0-aab4-44e0-99c4-0469945d2bae";
	public static final String  CONVERSION_FAILED="OrderHive to Locus conversion failed";

}
