package com.ticket_booking.domain.models.enums;

import java.util.Collection;
import java.util.List;

public enum PaymentStatus {
	PENDING,
	REQUIRES_ACTION,
    PROCESSING,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    CANCELED,
    UNKNOWN,
    REFUNDED;
    
    public static Collection<PaymentStatus> getActiveStatus(){
		return List.of(
				PENDING, 
				REQUIRES_ACTION,
				PROCESSING,
				AUTHORIZED,
				UNKNOWN
		);
	}
}
