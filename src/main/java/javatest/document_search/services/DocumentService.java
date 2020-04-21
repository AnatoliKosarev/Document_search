package javatest.document_search.services;

import javatest.document_search.entity.Document;
import javatest.document_search.exception_handler.DocumentNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Component
public class DocumentService implements DocumentServiceInterface {
    @Value("${localDownload.path}")
    private String downloadPath;
    private String fileName;
    private StringBuilder content;
    private File[] filesList;


    //returns the array of files from local repository
    @PostConstruct
    private void getLocalFilesList() throws FileNotFoundException {
        File folder = new File(downloadPath);
        if (folder.exists()) {
            filesList = folder.listFiles();
        } else
            throw new FileNotFoundException("Wrong download path: " + downloadPath);
    }

    //reads and saves file content
    private void readFileContent(File file) {
        content = new StringBuilder();
        // 1 variant
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            assert scanner != null;
            if (!scanner.hasNextLine()) break;
            content.append(scanner.nextLine());
        }
        scanner.close();
//         2 variant
//      try (Stream<String> stream = Files.lines( Paths.get(file.getPath()))) {
//          stream.forEach(s -> content.append(s));
//
//       } catch (IOException e) {
//          e.printStackTrace();
//       }
    }

    //removes extensions from file names
    private String removeFileExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    @Override
    public List<String> getDocumentIdList() {
        List<String> documentIdList = new ArrayList<>();
        for (File file : filesList) {
            documentIdList.add(removeFileExtension(file.getName()));
        }
        return documentIdList;
    }

    @Override
    public Document getDocumentNameContent(String queryDocumentName) {
        content = new StringBuilder();
        for (File file : filesList) {
            // get file name from file list
            fileName = file.getName();
            // check file name - if it = to query file - read file line by line and save to StringBuilder
            if (removeFileExtension(fileName).equals(queryDocumentName)) {
                readFileContent(file);
                break;
                // check if it's the last file in array and no content was returned - throw DocumentNotFoundException
            } else if (file == filesList[filesList.length - 1]) {
                throw new DocumentNotFoundException("Document not found:" + queryDocumentName);
            }
        }
        // returns new Document object with mandatory parameters, changing content type from StringBuilder to String
        return new Document(queryDocumentName, content.toString());
    }

    //populates map list with document name and number of matches of key phrase in it
    private void populateMapWithDocumentMatchData(TreeMap<String, Integer> map,
                                                  String fileName, int matchNumber) {
        // if map list already contains current file
        if (map.containsKey(fileName)) {
            // add number of matches for current file
            map.put(fileName, map.get(fileName) + matchNumber);
            // if map list doesn't contain data for current file -
            // add file key and match number
        } else {
            map.put(fileName, matchNumber);
        }
    }

    //returns list of ngrams of certain length from key phrase
    private List<String> getNgramList(int ngramLength, String keyPhrase) {
        List<String> ngramList = new ArrayList<>();
        // split key phrase by spaces
        String[] splitKeyPhrase = keyPhrase.split(" ");
        // iterate the number of times = ngram quantity in key phrase
        for (int i = 0; i <= splitKeyPhrase.length - ngramLength; i++) {
            // add ngram to ngramList evoking concatinate method which returns ngram string
            ngramList.add(concatNgram(splitKeyPhrase, i, i + ngramLength));
        }
        return ngramList;
    }

    //returns ngram string of certain length within start and end index from key phrase string array
    private String concatNgram(String[] splitKeyPhrase, int start, int end) {
        StringBuilder ngramStringBuilder = new StringBuilder();
        // iterate the number of times = to passed start and end key phrase array length
        for (int i = start; i < end; i++) {
            // concatinate ngram with check -  if it's a starting string in ngram - no space before it,
            // otherwise - space before
            ngramStringBuilder.append(i > start ? " " : "").append(splitKeyPhrase[i]);
        }
        return ngramStringBuilder.toString();
    }

    @Override
    public List<Map.Entry<String, Integer>> getDocumentsByKeyPhrase(String keyPhrase) {
        TreeMap<String, Integer> map = new TreeMap<>();
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
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        //sort array list in reverse natural order (DESC)
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        return list;
    }
}
