package com.ticket_booking.domain.models.enums;

import java.util.Collection;
import java.util.List;

public enum RefundStatus {
		PENDING,
	    PROCESSING,
	    REQUIRES_ACTION,
	    SUCCEEDED,
	    FAILED,
	    CANCELED,
	    UNKNOWN;
	
	public static Collection<RefundStatus> getActiveStatus(){
		return List.of(
				PENDING, 
				PROCESSING,
				REQUIRES_ACTION,
				UNKNOWN
		);
	}
}
