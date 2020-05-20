package javatest.document_search.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class Document {

    public Document(String documentName, String documentContent) {
        this.documentName = documentName;
        this.documentContent = documentContent;
    }

    private String documentName;
    private String documentContent;
}
