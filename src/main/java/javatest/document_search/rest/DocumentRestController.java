package javatest.document_search.rest;

import javatest.document_search.entity.Document;
import javatest.document_search.services.DocumentServiceInterface;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@Validated
public class DocumentRestController {

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
    public List<String> showAllDocuments() {
        return documentService.getDocumentIdList();
    }

    /**
     * displays query document id and its' content
     *
     * @param documentName query document id entered by a user
     * @return Document entity with query document id and its' content
     */
    @GetMapping("/documents/{documentName}")
    public Document showDocumentByName(@PathVariable @Pattern(regexp = "^doc \\([0-9]+\\)$",
            message = "Document name path variable must have the following format: doc (Number)")
                                               String documentName) {
        return documentService.getDocumentNameContent(documentName);
    }

    /**
     * displays list of document ids sorted by key phrase occurrences found in document
     *
     * @param keyPhrase query search phrase entered by a user
     * @return list of document ids sorted by key phrase occurrences found in document
     */
    @GetMapping("/document_search/{keyPhrase}")
    public List<Map.Entry<String, Integer>> showDocumentsByKeyPhrase(@PathVariable String keyPhrase) {
        return documentService.getDocumentsByKeyPhrase(keyPhrase);
    }
}