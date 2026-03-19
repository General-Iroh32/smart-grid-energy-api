package at.wien.smartgrid.controller;

import at.wien.smartgrid.service.DuplicateReadingException;
import at.wien.smartgrid.service.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateReadingException.class)
    ProblemDetail handleDuplicate(DuplicateReadingException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Duplicate meter reading", exception.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    ProblemDetail handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Request cannot be processed", exception.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = problem(
                HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid", request);
        Map<String, String> violations = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage(),
                        (first, ignored) -> first));
        detail.setProperty("violations", violations);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleMalformedBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                "The JSON body does not match the published API contract",
                request);
    }

    private ProblemDetail problem(
            HttpStatus status, String title, String message, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setType(URI.create("https://api.dzovani.codes/problems/" + status.value()));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }
}
