package javatest.document_search.exception_handler;

public class DocumentErrorResponse {

    private int status;
    private String message;
    private long timeStamp;

    public DocumentErrorResponse() {
    }

    public DocumentErrorResponse(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timeStamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
