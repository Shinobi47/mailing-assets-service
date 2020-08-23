package com.benayed.mailing.assets.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.benayed.mailing.assets.exception.TechnicalException;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ExceptionHandler(value = { IllegalArgumentException.class, TechnicalException.class})
	protected void handleBadRequest(RuntimeException e, HttpServletResponse response) throws IOException {
		
		response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}

	@ExceptionHandler(value = { UncheckedIOException.class})
	protected void handleUncheckedIOException(RuntimeException e, HttpServletResponse response) throws IOException{
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "File processing error : " + e.getMessage());

	}

	@ExceptionHandler(value = {NoSuchElementException.class})
	protected void handleNoSuchElementException(RuntimeException e, HttpServletResponse response) throws IOException {
		
		response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
	}
	
	
	@ExceptionHandler(value = {Exception.class})
	protected void handlGenericException(Exception e,  HttpServletResponse response) throws IOException {
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error happened, please contact your administrator");
	}
}
