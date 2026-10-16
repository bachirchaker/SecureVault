package com.securevault.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedAccess(UnauthorizedAccessException e, Model model) {
        log.warn("Access control violation attempt detected: {}", e.getMessage());
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "Access Denied (IDOR Protection)");
        model.addAttribute("errorMessage", "You do not have authorization to view, modify, or delete this protected vault resource.");
        return "error/403";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        log.info("Resource not found: {}", e.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(CryptoException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleCryptoException(CryptoException e, Model model) {
        // High severity: potential data corruption, invalid key or tampered ciphertext
        log.error("Cryptographic error occurred: {}", e.getMessage());
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Cryptographic Authentication Failure");
        model.addAttribute("errorMessage", "The requested secret could not be decrypted or its authentication tag failed verification.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model) {
        log.error("Unhandled application exception", e);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again later.");
        return "error/500";
    }
}
