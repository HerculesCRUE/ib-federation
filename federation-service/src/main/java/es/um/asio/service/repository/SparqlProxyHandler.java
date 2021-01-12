package es.um.asio.service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SparqlProxyHandler {

    private final Logger logger = LoggerFactory.getLogger(SparqlProxyHandler.class);

    private static final String OBJECTS_QUERY = "SELECT DISTINCT ?object\n" +
            "WHERE {\n" +
            "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  ?object .\n" +
            "  FILTER regex(str(?object),\"^http://hercules.org/um/*/*/\")\n" +
            "}";

    public static List<String> getObjects(URI baseUrl,String tripleStore) {
        List<String> objects = new ArrayList<>();
        return objects;
    }
}
