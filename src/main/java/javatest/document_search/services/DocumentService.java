package javatest.document_search.services;

import javatest.document_search.entity.Document;
import javatest.document_search.exception_handler.DocumentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class DocumentService implements DocumentServiceInterface {
    @Value("${localDownload.path}")
    private String downloadPath;
    private List<File> filesList;
    Logger log = LoggerFactory.getLogger(this.getClass());

    //returns the array of files from local repository
    @PostConstruct
    private void getLocalFilesList() throws FileNotFoundException {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();

        if (!folder.exists())
            throw new FileNotFoundException("Wrong download path: " + downloadPath);

        if (files != null && files.length != 0)
            filesList = Arrays.asList(files);
        else
            throw new FileNotFoundException(downloadPath + " folder exists but no documents where found");
    }

    @Override
    public List<String> getDocumentIdList() {
        return filesList.stream()
                .map(file -> removeFileExtension(file.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Document getDocumentNameContent(String queryDocumentName) {
        Optional<String> content = filesList.stream()
                .filter(file -> removeFileExtension(file.getName()).equals(queryDocumentName))
                .map(this::readFileContent)
                .findAny()
                .map(Object::toString);

        if (!content.isPresent())
            throw new DocumentNotFoundException("Document not found: " + queryDocumentName);
        else if (content.get().length() == 0)
            throw new DocumentNotFoundException("Document is empty: " + queryDocumentName);

        return new Document(queryDocumentName, content.get());
    }

    @Override
    public List<Map.Entry<String, Integer>> getDocumentsByKeyPhrase(String keyPhrase) {
        HashMap<String, Integer> map = new HashMap<>();
        String content;
        String[] splitKeyPhrase = keyPhrase.split(" ");

        for (File file : filesList) {
            String fileName = removeFileExtension(file.getName());
            content = readFileContent(file).toString();
            for (int ngramLength = 1; ngramLength <= splitKeyPhrase.length; ngramLength++) {
                for (String ngram : getNgramList(ngramLength, splitKeyPhrase)) {
                    int startIndex = 0;
                    if (!ngramFound(content, ngram, startIndex))
                        continue;
                    while (ngramFound(content, ngram, startIndex)) {
                        populateMapWithDocumentMatchData(map, fileName, ngramLength * ngramLength);
                        startIndex = content.indexOf(ngram, startIndex) + ngram.length();
                    }
                }
            }
            if (file == filesList.get(filesList.size() - 1) && map.isEmpty())
                throw new DocumentNotFoundException("Searched key phrase not found:" + keyPhrase);
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        return list;
    }

    //reads and saves file content
    private StringBuilder readFileContent(File file) {
        StringBuilder content = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
            stream.forEach(content::append);
        } catch (IOException e) {
            log.error("Error occurred in readFileContent method while reading file "
                    + file.getName() + " content", e);
        }

        return content;
    }

    //removes extensions from file names
    private String removeFileExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    //populates map list with document name and number of matches of key phrase in it
    private void populateMapWithDocumentMatchData(HashMap<String, Integer> map,
                                                  String fileName, int matchNumber) {
        // if map list already contains current file
        if (map.containsKey(fileName))
            // add number of matches for current file
            map.put(fileName, map.get(fileName) + matchNumber);
            // if map list doesn't contain data for current file -
            // add file key and match number
        else
            map.put(fileName, matchNumber);
    }

    //returns list of ngrams of certain length from key phrase
    private List<String> getNgramList(int ngramLength, String[] splitKeyPhrase) {

        return IntStream.range(0, splitKeyPhrase.length - ngramLength + 1)
                .mapToObj(index -> concatNgram(splitKeyPhrase, index, index + ngramLength))
                .collect(Collectors.toList());
    }

    //returns ngram string of certain length within start and end index from key phrase string array
    private String concatNgram(String[] splitKeyPhrase, int startIndex, int endIndex) {

        return Arrays.stream(splitKeyPhrase, startIndex, endIndex)
                .collect(Collectors.joining(" "));
    }

    //returns true if ngram found, false if ngram not found
    private boolean ngramFound(String content, String ngram, int startIndex) {

        return content.indexOf(ngram, startIndex) != -1;
    }
}
