package javatest.document_search.rest;

import javatest.document_search.entity.Document;
import javatest.document_search.services.DocumentService;
import javatest.document_search.services.DocumentServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@RestController
@RequestMapping("/")
public class DocumentRestController {

    /**
     * creates DocumentServiceInterface entity containing methods for endpoints
     */
    @Autowired
    DocumentServiceInterface documentService;

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
    public Document showDocumentByName(@PathVariable String documentName) {
        return documentService.getDocumentNameContent(documentName);
    }

    /**
     * displays list of document ids sorted by key phrase occurrences found in document
     *
     * @param keyPhrase query search phrase entered by a user
     * @return list of document ids sorted by key phrase occurrences found in document
     */
    @GetMapping("/document_search/{keyPhrase}")
    public List showDocumentsByKeyPhrase(@PathVariable String keyPhrase) {
        return documentService.getDocumentsByKeyPhrase(keyPhrase);
    }
}


