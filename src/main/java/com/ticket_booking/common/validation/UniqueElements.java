package com.ticket_booking.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueElementsValidator.class)
@Documented
public @interface UniqueElements {
	String message() default "must not contain duplicate elements";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
