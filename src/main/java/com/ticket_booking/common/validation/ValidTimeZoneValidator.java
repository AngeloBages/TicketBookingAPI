package com.ticket_booking.common.validation;

import java.time.DateTimeException;
import java.time.ZoneId;

import org.apache.logging.log4j.util.Strings;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTimeZoneValidator implements ConstraintValidator<ValidTimeZone, String>{

	@Override
	public boolean isValid(String timeZone, ConstraintValidatorContext context) {
		if(Strings.isBlank(timeZone)) {
			return false;
		}
		
		try {
			ZoneId.of(timeZone);
			return true;
			
		} catch(DateTimeException ex) {
			return false;
		}
	}
}
