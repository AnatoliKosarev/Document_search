package javatest.document_search.exception_handler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentErrorResponse {

    private int status;
    private String message;
    private long timeStamp;

}
