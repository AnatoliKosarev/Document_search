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
public class DocumentService implements DocumentServiceInterface{
    @Value("${localDownload.path}")
    private String downloadPath;
    private File folder;
    private String fileName;
    private List<String> documentIdList;
    private StringBuilder content;
    private Document document;
    private TreeMap<String, Integer> map;
    private File[] filesList;

    /**
     * returns the array of files from local repository
     * @throws FileNotFoundException if folder is not found
     */
    @PostConstruct
    public void getLocalFilesList() throws FileNotFoundException {
        folder = new File(downloadPath);
        if (folder.exists()) {
            filesList = folder.listFiles();
        } else
            throw new FileNotFoundException("Wrong download path: " + downloadPath);
    }

    /**
     * reads file content
     * @param file file which needs to be read
     * @return file content as StringBuilder
     */
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
//         2 variant
//      try (Stream<String> stream = Files.lines( Paths.get(file.getPath()))) {
//          stream.forEach(s -> content.append(s));
//
//       } catch (IOException e) {
//          e.printStackTrace();
//       }
        return content;
    }

    /**
     * removes extensions from file names
     * @param fileName file name for which extension needs to be removed
     * @return file name without extension as String
     */
    public String removeFileExtension(String fileName) {
        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
        return fileNameWithoutExtension;
    }

    /**
     * reads file names form local folder and adds them to documentIdList
     * @return documentIdList with added file names form local folder as List<String>
     */
    @Override
    public List<String> getDocumentIdList() {
        documentIdList = new ArrayList<>();
        for (File file : filesList) {
            documentIdList.add(removeFileExtension(file.getName()));
        }
        return documentIdList;
    }

    /**
     * reads and returns query file name and its' content
     * @param queryDocumentName name of a file which name and content needs to be read
     * @return Document entity of read file
     */
    @Override
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

    /**
     * populates map list with document name and number of matches of key phrase in it
     * @param map map list that needs to be populated with data
     * @param fileName name of the file in which query by key phrase is performed
     * @param matchNumber match coefficient = number of words in ngram squared
     * @return map list populated with query file data as TreeMap<String, Integer>
     */
    public TreeMap<String, Integer> populateMapWithDocumentMatchData(TreeMap<String, Integer> map,
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
        return map;
    }

    /**
     * returns list of ngrams of certain length from key phrase
     * @param ngramLength number of words in ngram
     * @param keyPhrase key phrase from which ngram is created
     * @return list of created ngrams of certain length as List<String>
     */
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

    /**
     * returns ngram string of certain length within start and end index from key phrase string array
     * @param splitKeyPhrase key phrase split by spaces as String[]
     * @param start index of key phrase array from which ngram starts
     * @param end  index of key phrase array before which ngram ends
     * @return created ngram as String
     */
    private String concatNgram(String[] splitKeyPhrase, int start, int end ) {
        StringBuilder ngramStringBuilder = new StringBuilder();
        // iterate the number of times = to passed start and end key phrase array length
        for (int i = start; i < end; i++) {
            // concatinate ngram with check -  if it's a starting string in ngram - no space before it,
            // otherwise - space before
            ngramStringBuilder.append((i > start ? " " : "") + splitKeyPhrase[i]);
        }
        return ngramStringBuilder.toString();
    }

    /**
     * returns document id list sorted by number of matches of key phrase found in document
     * the list is sorted by match index with DESC order from the highest to the lowest index of matches found
     * match index equals the squared length of a key phrase or part of a key phrase (ngram) found
     * such sorting rules ensures that the more accurate is the match to a key phrase - the larger the index such match gets
     * @param keyPhrase query key phrase entered by user
     * @return list of sorted by DESC match occurrences document ids as ArrayList()
     */
    @Override
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
}
