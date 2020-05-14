package javatest.document_search.rest;

import javatest.document_search.entity.Document;
import javatest.document_search.services.DocumentServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class DocumentRestController {

    private static final String VALIDATION_MESSAGE = "Document name path variable must have the following format: doc (Number)";

    private final DocumentServiceInterface documentService;

    public DocumentRestController(DocumentServiceInterface documentService) {
        this.documentService = documentService;
    }

    /**
     * displays list of all document ids from storage
     *
     * @return documentIdList with added file names as List<String>
     */
    @GetMapping("/documents")
    public ResponseEntity<List<String>> showAllDocuments() {
        return ResponseEntity.ok(documentService.getDocumentIdList());
    }

    /**
     * displays query document id and its' content
     *
     * @param documentName query document id entered by a user
     * @return Document entity with query document id and its' content
     */
    @Validated
    @GetMapping("/documents/{documentName}")
    public ResponseEntity<Document> showDocumentByName(@PathVariable @Pattern(regexp = "^doc \\([0-9]+\\)$",
            message = VALIDATION_MESSAGE) String documentName) {
        return ResponseEntity.ok(documentService.getDocumentNameContent(documentName));
    }

    /**
     * displays list of document ids sorted by key phrase occurrences found in document
     *
     * @param keyPhrase query search phrase entered by a user
     * @return list of document ids sorted by key phrase occurrences found in document
     */
    @GetMapping("/document_search/{keyPhrase}")
    public ResponseEntity<List<Map.Entry<String, Integer>>> showDocumentsByKeyPhrase(@PathVariable String keyPhrase) {
        return ResponseEntity.ok(documentService.getDocumentsByKeyPhrase(keyPhrase));
    }
}