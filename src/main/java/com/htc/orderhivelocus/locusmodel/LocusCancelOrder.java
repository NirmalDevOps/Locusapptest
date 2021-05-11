/**
 * 
 */
package com.htc.orderhivelocus.locusmodel;

/**
 * Represents a LocusCancelOrder  model class
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 30-03-2021
 * 
 */
public class LocusCancelOrder {

	private String orderStatus;
	private OrderSelectRequest orderSelectRequest;

	public LocusCancelOrder() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the orderStatus
	 */
	public String getOrderStatus() {
		return orderStatus;
	}

	/**
	 * @param orderStatus the orderStatus to set
	 */
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	/**
	 * @return the orderSelectRequest
	 */
	public OrderSelectRequest getOrderSelectRequest() {
		return orderSelectRequest;
	}

	/**
	 * @param orderSelectRequest the orderSelectRequest to set
	 */
	public void setOrderSelectRequest(OrderSelectRequest orderSelectRequest) {
		this.orderSelectRequest = orderSelectRequest;
	}

	@Override
	public String toString() {
		return "LocusCancelOrder [orderStatus=" + orderStatus + ", orderSelectRequest=" + orderSelectRequest + "]";
	}

}
