package com.ticket_booking.admin;

import java.util.Set;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.admin.requests.AdminRequests.AssignRolesRequest;
import com.ticket_booking.admin.responses.AdminResponses.UserInfoResponse;
import com.ticket_booking.common.AppRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin endpoints")
public class AdminController {
	
	private final UserAdministrationService userAdminService;
	private final Validator validator;
	
	public AdminController(
			UserAdministrationService userAdminService,
			Validator validator) {
		
		this.userAdminService = userAdminService;
		this.validator = validator;
	}

	@GetMapping("/users")
	@Operation(summary = "Get all application users' info")
	public ResponseEntity<Page<UserInfoResponse>> getUsersInfo(
			@ParameterObject
			@PageableDefault(size = 15, sort = "createdAt", direction = Direction.ASC)
			@SortDefault.SortDefaults({
				@SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
				@SortDefault(sort = "name", direction = Sort.Direction.ASC)
			})
			Pageable pageable){
		
		return ResponseEntity.ok(
				userAdminService.getUsersInfo(pageable));
	}
	
	@GetMapping("/users/{id}")
	@Operation(summary = "Get a specif application user's info")
	public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable("id") UUID userId){

		return ResponseEntity.ok(
				userAdminService.getUserInfo(userId));
	}
	
	@PostMapping("/users/{id}/roles")
	@Operation(summary = "Assign a role to a specif application user")
	public ResponseEntity<Void> assignRole(
			@PathVariable("id") UUID userId,
			@RequestBody AssignRolesRequest request){
		validateRequest(request);
		
		userAdminService.assignRoleToUser(userId, request.roleName());
		
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/users/{id}/roles/{role}")
	@Operation(summary = "Revoke a specif user's role")
	public ResponseEntity<Void> revokeRole(
			@PathVariable("id") UUID userId,
			@PathVariable AppRole role){
		
		userAdminService.revokeRoleFromUser(userId, role);
		
		return ResponseEntity.noContent().build();
	}
	
	private <T> void validateRequest(T request) {
		Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); // Triggers  400 Bad Request handler
        }
	}
}
