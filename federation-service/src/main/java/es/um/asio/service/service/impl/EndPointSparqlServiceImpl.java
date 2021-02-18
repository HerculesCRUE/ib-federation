package es.um.asio.service.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.config.properties.DataSourceSPARQL;
import es.um.asio.service.exceptions.CustomFederationException;
import es.um.asio.service.model.CustomObject;
import es.um.asio.service.model.Node;
import es.um.asio.service.model.constants.Constants;
import es.um.asio.service.service.EndPointSparqlService;
import es.um.asio.service.service.FederationServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EndPointSparqlServiceImpl implements EndPointSparqlService  {

    @Autowired
    FederationServiceHelper federationServiceHelper;

    @Autowired
    DataSourceSPARQL dataSourceSPARQL;

    @Override
    public JsonObject executeQuery(String authorization, String query, String tripleStore, Integer pageSize, Integer nodeTimeout) throws URISyntaxException, IOException {

        DataSourceSPARQL.Connector connector = dataSourceSPARQL.getConnectorByType(tripleStore);
        if (connector == null) {
            throw new CustomFederationException("Type of triplestore "+tripleStore+" not is a valid tyoe");
        } else {
            if (pageSize != null) {
                query = query.replaceAll("limit d+", "");
                query = query.replaceAll("offset d+", "");
            }
            query = connector.getQueryParamName()+"=" + query;
            JsonObject jFederatedResponse = doExecuteQuery(authorization,connector, query, pageSize, nodeTimeout);
            return jFederatedResponse;
        }
    }

    private JsonObject doExecuteQuery(String authorization, DataSourceSPARQL.Connector connector, String query, Integer pageSize, Integer nodeTimeout) throws IOException {
        // Populate futures
        CompletableFuture<JsonObject> future;
        String url = connector.buildQueryURL(true);

        if (pageSize!=null) //(String nodeName,URL url, String q, Integer pageSize, Integer timeout)
            future = federationServiceHelper.executeQueryPaginated(authorization,dataSourceSPARQL.getNodeName(),new URL(url),query,pageSize,nodeTimeout);
        else
            future = federationServiceHelper.executeQuery(authorization,dataSourceSPARQL.getNodeName(),new URL(url),query,nodeTimeout);

        JsonObject jResponse = future.join();

        return jResponse;
    }

}
