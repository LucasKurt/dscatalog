package com.lucasprojects.dscatalog.resources.exceptions;

import java.time.Instant;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceExceptionHandler {
	
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<StandardError> entityNotFoundException(EntityNotFoundException err, HttpServletRequest request) {
		Instant timestamp = Instant.now();
		Integer status = HttpStatus.NOT_FOUND.value();
		String error = err.getMessage();
		String path = request.getRequestURI();
		
		return ResponseEntity.status(status).body(new StandardError(timestamp, status, error, path));
	}
}
