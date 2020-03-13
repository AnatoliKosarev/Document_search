package javatest.document_search.entity;

public class Document {
    private String documentName;
    private String documentContent;

    public Document(String documentName, String documentContent) {
        this.documentName = documentName;
        this.documentContent = documentContent;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }
}
