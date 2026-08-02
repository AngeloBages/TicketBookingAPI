package com.ticket_booking.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/test")
@Tag(name = "Test", description = "Protected endpoints")
public class TestController {

	@GetMapping("auth")
	@Operation(
			summary = "Authentication test",
			description = "Returns a message if JWT authentication is working.")
    public String testAuthentication() {
        return "JWT authentication is working!";
    }
}
