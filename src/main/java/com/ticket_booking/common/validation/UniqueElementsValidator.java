package com.ticket_booking.common.validation;

import java.util.Collection;
import java.util.HashSet;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueElementsValidator implements ConstraintValidator<UniqueElements, Collection<?>>{

	@Override
	public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
		if(value == null) {
			return true;
		}
		
		return value.size() == new HashSet<>(value).size();
	}
}
