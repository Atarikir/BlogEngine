package main.advice;

import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.ImageBadRequestException;
import main.exceptions.TextCommentBadRequestException;
import main.service.UtilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final UtilityService utilityService;

    public CustomGlobalExceptionHandler(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    @ExceptionHandler(TextCommentBadRequestException.class)
    public ResponseEntity<ResultErrorResponse> textCommentBadRequest(Exception ex) {
        ResultErrorResponse errorResponse = utilityService.errorsRequest(
                ErrorResponse.builder()
                        .text(ex.getMessage())
                        .build()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ImageBadRequestException.class)
    public ResponseEntity<ResultErrorResponse> imageBadRequest(Exception ex) {
        ResultErrorResponse errorResponse = utilityService.errorsRequest(
                ErrorResponse.builder()
                        .image(ex.getMessage())
                        .build()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
