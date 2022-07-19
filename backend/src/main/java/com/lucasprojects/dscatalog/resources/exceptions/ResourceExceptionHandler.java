package com.lucasprojects.dscatalog.resources.exceptions;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.lucasprojects.dscatalog.services.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class ResourceExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException err, HttpServletRequest request) {
		Instant timestamp = Instant.now();
		Integer status = HttpStatus.NOT_FOUND.value();
		String error = err.getMessage();
		String path = request.getRequestURI();
		
		return ResponseEntity.status(status).body(new StandardError(timestamp, status, error, path));
	}
}
