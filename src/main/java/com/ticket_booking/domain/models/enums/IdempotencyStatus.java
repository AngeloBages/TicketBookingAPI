package com.ticket_booking.domain.models.enums;

public enum IdempotencyStatus {
	/**
	 * This key is currently associated with an execution
	 */
	IN_PROGRESS,
	
	/**
	 * The operation successfully committed and its result was persisted
	 */
	COMPLETED
}
