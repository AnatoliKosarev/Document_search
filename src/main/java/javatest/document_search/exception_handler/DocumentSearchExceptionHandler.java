package javatest.document_search.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DocumentSearchExceptionHandler {

    /**
     * exception handler method with DocumentErrorResponse type of the response body,
     * handling/catching DocumentNotFoundException
     * @param exc caught exception of DocumentNotFoundException type
     * @return ResponseEntity with DocumentErrorResponse error as body and HTTP status
     */
    @ExceptionHandler
    public ResponseEntity<DocumentErrorResponse> handleException(DocumentNotFoundException exc) {
        // create DocumentErrorResponse
        DocumentErrorResponse error = new DocumentErrorResponse();

        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(exc.getMessage());
        error.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * exception handler method with DocumentErrorResponse type of the response body,
     * handling/catching generic Exception
     * @param exc caught exception of Exception type
     * @return ResponseEntity with DocumentErrorResponse error as body and HTTP status
     */
    @ExceptionHandler
    public ResponseEntity<DocumentErrorResponse> handleException(Exception exc) {
        // create DocumentErrorResponse
        DocumentErrorResponse error = new DocumentErrorResponse();

        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(exc.getMessage());
        error.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
