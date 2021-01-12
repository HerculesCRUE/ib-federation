package es.um.asio.service.service;

import es.um.asio.service.model.TripleObjectSimplified;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface DataFetcherService {

    Set<String> getObjectsUris(String nodeName, String service, String tripleStore) throws URISyntaxException, IOException;

    Set<TripleObjectSimplified> getTripleObjectSimplified(String nodeName, String service, String tripleStore, String className) throws URISyntaxException, IOException;

    TripleObjectSimplified findTripleObjectSimplifiedByURI(String nodeName, String service, String tripleStore, String className, String uri) throws URISyntaxException, IOException;
}
