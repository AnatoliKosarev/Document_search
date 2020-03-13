package javatest.document_search.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class DocumentRestController {
    @Value("${download.path}")
    private String downloadPath;

    private List<String> documentIdList;

    // reading file names form local folder and adding them to documentIdList
    @PostConstruct
    public void populateDocumentIdList() {
        documentIdList = new ArrayList<>();
        File file = new File(downloadPath);
        String[] files = file.list();
        for (String s : files) {
            documentIdList.add(removeFileExtension(s));
        }
    }

    // removing extensions from file names
    public String removeFileExtension(String fileName) {
        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
        return fileNameWithoutExtension;
    }

    // endpoint to return all documents from local folder
    @GetMapping("/documents")
    public List<String> getAllDocuments() {
            return documentIdList;
    }
}
