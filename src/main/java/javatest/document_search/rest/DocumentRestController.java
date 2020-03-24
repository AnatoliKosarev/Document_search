package javatest.document_search.rest;

import javatest.document_search.entity.Document;
import javatest.document_search.exception_handler.DocumentNotFoundException;
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
    private StringBuilder content;
    private Document document;
    private File folder;
    private File[] files;

    // reading file names form local folder and adding them to documentIdList
    @PostConstruct
    public void populateDocumentIdList() throws FileNotFoundException {
        documentIdList = new ArrayList<>();
        folder = new File(downloadPath);
        if (folder.exists()) {
            files = folder.listFiles();
            for (File file : files) {
                documentIdList.add(removeFileExtension(file.getName()));
            }
        } else {throw new FileNotFoundException("Wrong download path: " + downloadPath);}
    }

    // reading file name and content
    public Document getDocumentNameContent(String queryDocumentName) {
        content = new StringBuilder();

        for (File file : files) {
            // get file name from file list
            String fileName = file.getName();
            // check file name - if it = to query file - read file line by line and save to StringBuilder
            if(removeFileExtension(fileName).equals(queryDocumentName)) {
                // 1 variant
                Scanner scanner = null;
                try {
                    scanner = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine());
                }
                scanner.close();

                // 2 variant
//                try (Stream<String> stream = Files.lines( Paths.get(file.getPath()))) {
//                    stream.forEach(s -> content.append(s));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
                // check if it's the last file in array and no content was returned - throw DocumentNotFoundException
            } else if (file == files[files.length-1]) {
                throw new DocumentNotFoundException("Document not found:" + queryDocumentName);
            }
        }

        // create new Document object with mandatory parameters, changing content type from StringBuilder to String
        document = new Document(queryDocumentName, content.toString());
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
    public Document getDocumentByName(@PathVariable String documentName) {
        return getDocumentNameContent(documentName);
    }
}


