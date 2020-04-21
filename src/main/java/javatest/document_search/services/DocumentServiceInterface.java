package javatest.document_search.services;

import javatest.document_search.entity.Document;

import java.util.List;
import java.util.Map;

public interface DocumentServiceInterface {
    /**
     * reads file names form local folder and adds them to documentIdList
     *
     * @return documentIdList with added file names form local folder as List<String>
     */
    List<String> getDocumentIdList();

    /**
     * reads and returns query file name and its' content
     *
     * @param queryDocumentName name of a file which name and content needs to be read
     * @return Document entity of read file
     */
    Document getDocumentNameContent(String queryDocumentName);

    /**
     * returns document id list sorted by number of matches of key phrase found in document
     * the list is sorted by match index with DESC order from the highest to the lowest index of matches found
     * match index equals the squared length of a key phrase or part of a key phrase (ngram) found
     * such sorting rules ensures that the more accurate is the match to a key phrase - the larger the index such match gets
     *
     * @param keyPhrase query key phrase entered by user
     * @return list of sorted by DESC match occurrences document ids as ArrayList()
     */
    List<Map.Entry<String, Integer>> getDocumentsByKeyPhrase(String keyPhrase);
}
