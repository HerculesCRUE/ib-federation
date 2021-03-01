package es.um.asio.service.service;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface EndPointSparqlService {

    JsonObject executeQuery(String authorization, String query, String tripleStore, Integer pageSize, Integer nodeTimeout) throws URISyntaxException, IOException;
}
