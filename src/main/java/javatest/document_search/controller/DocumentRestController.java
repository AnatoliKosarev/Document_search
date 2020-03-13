package javatest.document_search.controller;

import javatest.document_search.entity.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@RestController
@RequestMapping("/")
public class DocumentRestController {
    @Value("${download.path}")
    private String downloadPath;
    private List<String> documentIdList;
    private String content;
    private Document document;

    // reading file names form local folder and adding them to documentIdList
    @PostConstruct
    public void populateDocumentIdList() {
        documentIdList = new ArrayList<>();
        File folder = new File(downloadPath);
        String[] files = folder.list();
        for (String s : files) {
            documentIdList.add(removeFileExtension(s));
        }
    }

    // reading file name and content
    public Document getDocumentNameContent(String queryDocumentName) throws FileNotFoundException {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();

        for (File file : files) {
            String fileName = file.getName();
            if(removeFileExtension(fileName).equals(queryDocumentName)) {
                File queryFile = new File(file.getPath());
                Scanner scanner = new Scanner(queryFile);
                while (scanner.hasNext()) {
                    content = scanner.nextLine();
                }
                scanner.close();
                break;
            }
        }

        document = new Document(queryDocumentName, content);
        return document;
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

    // endpoint to return document by name
    @GetMapping("/documents/{documentName}")
    public Document getDocumentByName(@PathVariable String documentName) throws FileNotFoundException {
        return getDocumentNameContent(documentName);
    }
}


