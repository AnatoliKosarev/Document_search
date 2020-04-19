package javatest.document_search.services;

import javatest.document_search.entity.Document;

import java.util.List;

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
     *
     * @param keyPhrase query key phrase entered by user
     * @return list of sorted by DESC match occurrences document ids as ArrayList()
     */
    List<String> getDocumentsByKeyPhrase(String keyPhrase);
}
