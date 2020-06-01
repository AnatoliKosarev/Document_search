package javatest.document_search.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Document {

    private String documentName;
    private String documentContent;
}
