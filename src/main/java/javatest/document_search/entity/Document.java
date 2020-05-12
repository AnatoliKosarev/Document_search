package javatest.document_search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Document {


    private String documentName;
    private String documentContent;

    public Document(String documentName, String documentContent) {
        this.documentName = documentName;
        this.documentContent = documentContent;
    }
}
