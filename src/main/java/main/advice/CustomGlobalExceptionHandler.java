package main.advice;

import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.TextCommentNotFoundException;
import main.service.GeneralService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final GeneralService generalService;

    public CustomGlobalExceptionHandler(GeneralService generalService) {
        this.generalService = generalService;
    }

    @ExceptionHandler(TextCommentNotFoundException.class)
    public ResponseEntity<ResultErrorResponse> customHandleNotFound(Exception ex) {
        ResultErrorResponse errorResponse = generalService.errorsRequest(
                ErrorResponse.builder()
                        .text(ex.getMessage())
                        .build()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
