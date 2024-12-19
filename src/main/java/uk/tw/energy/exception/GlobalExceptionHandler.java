package uk.tw.energy.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.tw.energy.domain.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRequestValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> requestErrors = new HashMap<>();
        List<ObjectError> objectErrors = exception.getAllErrors();
        objectErrors.forEach(error -> {
            FieldError fieldError = (FieldError) error;
            requestErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return requestErrors;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return new ExceptionResponse(exception.getMessage());
    }
}
