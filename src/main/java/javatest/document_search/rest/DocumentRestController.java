package javatest.document_search.rest;

import javatest.document_search.entity.Document;
import javatest.document_search.exception_handler.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@RestController
@RequestMapping("/")
public class DocumentRestController {
    @Value("${localDownload.path}")
    private String downloadPath;
    private String fileName;
    private List<String> documentIdList;
    private StringBuilder content;
    private Document document;
    private File folder;
    private File[] filesList;
    private TreeMap<String, Integer> map;

   @PostConstruct
    // return the array of files from local repository
    public void getLocalFilesList() throws FileNotFoundException {
        folder = new File(downloadPath);
        if (folder.exists()) {
            filesList = folder.listFiles();
        } else
            throw new FileNotFoundException("Wrong download path: " + downloadPath);
    }


    // reading file names form local folder and adding them to documentIdList
    public List<String> getDocumentIdList() {
        documentIdList = new ArrayList<>();
        for (File file : filesList) {
            documentIdList.add(removeFileExtension(file.getName()));
        }
        return documentIdList;
    }


    // read file content
    public StringBuilder readFileContent(File file) {
        content = new StringBuilder();
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
//      try (Stream<String> stream = Files.lines( Paths.get(file.getPath()))) {
//          stream.forEach(s -> content.append(s));
//
//       } catch (IOException e) {
//          e.printStackTrace();
//       }

        return content;
    }


    // reading file name and content
    public Document getDocumentNameContent(String queryDocumentName) {
        content = new StringBuilder();

        for (File file : filesList) {
            // get file name from file list
            fileName = file.getName();
            // check file name - if it = to query file - read file line by line and save to StringBuilder
            if(removeFileExtension(fileName).equals(queryDocumentName)) {
                readFileContent(file);
                break;
                // check if it's the last file in array and no content was returned - throw DocumentNotFoundException
            } else if (file == filesList[filesList.length-1]) {
                throw new DocumentNotFoundException("Document not found:" + queryDocumentName);
            }
        }

        // create new Document object with mandatory parameters, changing content type from StringBuilder to String
        document = new Document(queryDocumentName, content.toString());
        return document;
    }


    // populate map list with document name and number of matches of key phrase in it
    public TreeMap<String, Integer> populateMapWithDocumentMatchData(TreeMap<String, Integer> map, String fileName, int matchNumber) {

        // if map list already contains current file
        if (map.containsKey(fileName)) {
            // add number of matches for current file
            map.put(fileName, map.get(fileName) + matchNumber);
            // if map list doesn't contain data for current file - add file key and value multiplied on full match coefficient
        } else {
            map.put(fileName, matchNumber);
        }
        return map;
    }


    // returns list of ngrams from key phrase according to passed ngram length
    public List<String> getNgramList(int ngramLength, String keyPhrase) {
        List<String> ngramList = new ArrayList<>();
        // split key phrase by spaces
        String[] splitKeyPhrase = keyPhrase.split(" ");
        // iterate the number of times = ngram quantity in key phrase
        for (int i = 0; i <= splitKeyPhrase.length - ngramLength; i++) {
            // add ngram to ngramList evoking concatinate method which returns ngram string
            ngramList.add(concatNgram(splitKeyPhrase, i, i+ngramLength));
        }
        return ngramList;
    }

    // returns ngram string within start and end index from key phrase string array
    private String concatNgram(String[] splitKeyPhrase, int start, int end ) {
        StringBuilder ngramStringBuilder = new StringBuilder();
        // iterate the number of times = to passed start and end key phrase array length
        for (int i = start; i < end; i++) {
            // concatinate ngram with check -  if it's a starting string in ngram - no space before it, otherwise - space before
            ngramStringBuilder.append((i > start ? " " : "") + splitKeyPhrase[i]);
        }
        return ngramStringBuilder.toString();
    }

    public List<String> getDocumentsByKeyPhrase(String keyPhrase) {
        map = new TreeMap<>();
        content = new StringBuilder();
        String[] splitKeyPhrase = keyPhrase.split(" ");

        for (File file : filesList) {
            // get file name without extension
            fileName = removeFileExtension(file.getName());
            readFileContent(file);
            // iterate the number of times = number of words in key phrase
            for (int i = 1; i <= splitKeyPhrase.length; i++) {
                // iterate within returned ngram list
                for (String ngram : getNgramList(i, keyPhrase)) {
                    int startIndex = 0;
                    // if ngram not found in file - iterate to the next ngram
                    // indexOf() returns the index of the first occurrence of ngram in file or "-1" if ngram is not found
                    if (content.toString().indexOf(ngram, startIndex) == -1) {
                        continue;
                    }
                    // while file contains values = ngram, search performed starting from indexed character
                    while (content.toString().indexOf(ngram, startIndex) != -1) {
                        // add filename and match index according to ngram length to map list
                        populateMapWithDocumentMatchData(map, fileName, i * i);
                        // increase index value by found ngram length
                        startIndex = content.toString().indexOf(ngram, startIndex) + ngram.length();
                    }
                }
            }
            // if it's the last file and map list is empty - throw custom exception
            if (file == filesList[filesList.length - 1] && map.isEmpty()) {
                throw new DocumentNotFoundException("Searched key phrase not found:" + keyPhrase);
            }
        }
        //add map data to array list
        List list = new ArrayList(map.entrySet());
        //sort array list in reverse natural order (DESC)
        Collections.sort(list, (Comparator<Map.Entry<String, Integer>>) (o1, o2) -> o2.getValue() - o1.getValue());

        return list;
    }

    // removing extensions from file names
    public String removeFileExtension(String fileName) {
        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
        return fileNameWithoutExtension;
    }

    // endpoint to return all documents
    @GetMapping("/documents")
    public List<String> showAllDocuments() {
            return getDocumentIdList();
    }

    // endpoint to return document by name
    @GetMapping("/documents/{documentName}")
    public Document showDocumentByName(@PathVariable String documentName) {
        return getDocumentNameContent(documentName);
    }

    // endpoint to return document list sorted by key phrase occurance
    @GetMapping("/document_search/{keyPhrase}")
    public List showDocumentsByKeyPhrase(@PathVariable String keyPhrase) {
        return getDocumentsByKeyPhrase(keyPhrase);
    }
}


