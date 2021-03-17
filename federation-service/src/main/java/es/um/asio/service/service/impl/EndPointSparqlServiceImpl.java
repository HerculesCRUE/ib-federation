package es.um.asio.service.service.impl;

import com.google.gson.JsonObject;
import es.um.asio.service.config.properties.Endpoint;
import es.um.asio.service.exceptions.CustomFederationException;
import es.um.asio.service.service.EndPointSparqlService;
import es.um.asio.service.service.FederationServiceHelper;
import es.um.asio.service.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Service
public class EndPointSparqlServiceImpl implements EndPointSparqlService  {

    @Autowired
    FederationServiceHelper federationServiceHelper;

    @Autowired
    Endpoint endpoint;

    private final Logger logger = LoggerFactory.getLogger(DataFetcherServiceImpl.class);

    @Override
    public JsonObject executeQuery(String authorization, String query, String tripleStore, Integer pageSize, Integer nodeTimeout) throws URISyntaxException, IOException {

        Endpoint.Connector connector = endpoint.getConnectorByType(tripleStore);
        logger.info("Connector: {}", connector.toString());
        Integer limit = null;
        if (connector == null) {
            throw new CustomFederationException("Type of triplestore "+tripleStore+" not is a valid tyoe");
        } else {
            if (pageSize != null) {
                limit = Utils.extractLimitInSPARQL(query);
                query = query.replaceAll("(?i)limit\\s+(\\d+)", "");
                query = query.replaceAll("(?i)offset\\s+(\\d+)", "");
            }
            //query = connector.getQueryParamName()+"=" + query;
            JsonObject jFederatedResponse = doExecuteQuery(authorization,connector, query, pageSize, nodeTimeout, limit);
            return jFederatedResponse;
        }
    }

    private JsonObject doExecuteQuery(String authorization, Endpoint.Connector connector, String query, Integer pageSize, Integer nodeTimeout, Integer limit) throws IOException {
        // Populate futures
        CompletableFuture<JsonObject> future;
        String url = connector.buildQueryURL(true);

        if (pageSize!=null) //(String nodeName,URL url, String q, Integer pageSize, Integer timeout)
            future = federationServiceHelper.executeQueryPaginated("EndPoint",authorization, endpoint.getNodeName(),new URL(url),query,pageSize,nodeTimeout,limit);
        else
            future = federationServiceHelper.executeQuery("EndPoint",authorization, endpoint.getNodeName(),new URL(url),query,nodeTimeout,limit);

        JsonObject jResponse = future.join();
        logger.info("SPARQL Endpoint Response: {}", jResponse.toString());
        return jResponse;
    }

}
