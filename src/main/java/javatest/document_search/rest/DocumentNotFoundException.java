package javatest.document_search.rest;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentNotFoundException(Throwable cause) {
        super(cause);
    }
}
