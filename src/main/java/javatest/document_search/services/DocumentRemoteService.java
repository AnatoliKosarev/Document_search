package javatest.document_search.services;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javatest.document_search.entity.Document;
import javatest.document_search.exception_handler.DocumentNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile("remote")
public class DocumentRemoteService implements DocumentServiceInterface {
    private List<File> filesList;
    private Drive service;

    @PostConstruct
    public void getRemoteFilesList() throws IOException, GeneralSecurityException {

        RemoteSearchAuthentication remoteSearchAuthentication = new RemoteSearchAuthentication();
        service = remoteSearchAuthentication.authenticateUser();
        filesList = new ArrayList<>();
        Files.List request = service.files().list()
                .setQ("'1Hk41UAJGgpV3IhrQoncShnbjmRbNlsOJ' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false");

        do {
            try {
                FileList files = request.execute();

                filesList.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

    }

//        Drive.Files.List request = service
//                .files()
//                .list()
//                .setQ("'1Hk41UAJGgpV3IhrQoncShnbjmRbNlsOJ' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
//                .setSpaces("drive")
//                .setFields("nextPageToken, files(id, name, parents)");
//
//        do {
//            try {
//                FileList files = request.execute();
//
//                filesList.addAll(files.getFiles());
//                request.setPageToken(files.getNextPageToken());
//            } catch (IOException e) {
//                System.out.println("An error occurred: " + e);
//                request.setPageToken(null);
//            }
//        } while (request.getPageToken() != null &&
//                request.getPageToken().length() > 0);
//
//        for (File file : filesList)
//            service.files().get(file.getId()).execute().getInputStream();

    @Override
    public List<String> getDocumentIdList() {
        return filesList.stream()
                .map(file -> removeFileExtension(file.getTitle()))
                .collect(Collectors.toList());
    }

    @Override
    public Document getDocumentNameContent(String queryDocumentName) {

        Optional<String> content = filesList.stream()
                .filter(file -> removeFileExtension(file.getTitle()).equals(queryDocumentName))
                .map(this::readFileContent)
                .findAny()
                .map(Object::toString);

        if (!content.isPresent())
            throw new DocumentNotFoundException("Document not found: " + queryDocumentName);
        else if (content.get().length() == 0)
            throw new DocumentNotFoundException("Document is empty: " + queryDocumentName);

        return Document.builder()
                .documentName(queryDocumentName)
                .documentContent(content.get())
                .build();
    }

    @Override
    public List<Map.Entry<String, Integer>> getDocumentsByKeyPhrase(String keyPhrase) {
        HashMap<String, Integer> map = new HashMap<>();
        String content;
        String[] splitKeyPhrase = keyPhrase.split(" ");

        for (File file : filesList) {
            String fileName = removeFileExtension(file.getTitle());
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

        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    //reads and saves file content
    private InputStream readFileContent(File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                HttpResponse resp =
                        service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
                                .execute();
                return resp.getContent();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
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
