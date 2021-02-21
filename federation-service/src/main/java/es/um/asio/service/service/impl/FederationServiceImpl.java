package es.um.asio.service.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.model.CustomObject;
import es.um.asio.service.model.Node;
import es.um.asio.service.model.constants.Constants;
import es.um.asio.service.service.FederationService;
import es.um.asio.service.service.FederationServiceHelper;
import es.um.asio.service.service.ServiceDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FederationServiceImpl implements FederationService {

    @Autowired
    FederationServiceHelper federationServiceHelper;

    @Autowired
    EndPointSparqlServiceImpl endPointSparqlService;

    @Autowired
    ServiceDiscoveryService serviceDiscoveryService;

    @Override
    public JsonObject executeQueryInNodesList(String query, String tripleStore,List<String> nodeList, Integer pageSize, Integer nodeTimeout, Integer limit) throws URISyntaxException, IOException {
        Map<String, URL> uris = serviceDiscoveryService.getNodesByNameAndServiceAndType(nodeList,Constants.SPARQL_ENDPOINT_SERVICE,tripleStore);
        if (pageSize!=null) {
                query = query.replaceAll("(?i)limit\\s+(\\d+)", "");
                query = query.replaceAll("(?i)offset\\s+(\\d+)", "");
        }
        JsonObject jFederatedResponse = doExecuteQuery(uris,query,pageSize,nodeTimeout,limit);
        return jFederatedResponse;
    }

    @Override
    public JsonObject executeQueryInAllNodes(String query, String tripleStore, Integer pageSize, Integer nodeTimeout, Integer limit) throws URISyntaxException, IOException {

        Map<String, URL> uris = serviceDiscoveryService.getAllNodesByServiceAndType(Constants.SPARQL_ENDPOINT_SERVICE,tripleStore);
        if (pageSize!=null) {
                query = query.replaceAll("(?i)limit\\s+(\\d+)", "");
                query = query.replaceAll("(?i)offset\\s+(\\d+)", "");
        }
        JsonObject jFederatedResponse = doExecuteQuery(uris,query,pageSize,nodeTimeout,limit);
        return jFederatedResponse;

    }


    private JsonObject doExecuteQuery(Map<String, URL> uris,String query, Integer pageSize, Integer nodeTimeout, Integer limit) throws IOException {
        Map<Integer, CustomObject> objects = new HashMap<>();
        List<String> variables = new ArrayList<>();
        JsonArray jStatsArray = new JsonArray();
        JsonObject jFederatedResponse = new JsonObject();
        // Populate futures
        Map<String,List<CompletableFuture<JsonObject>>> results = new HashMap<>();
        for (Map.Entry<String, URL> uriEntry :uris.entrySet()) {
            if (!results.containsKey(uriEntry.getKey()))
                results.put(uriEntry.getKey(), new ArrayList<>());
            if (pageSize!=null)  {
                CompletableFuture<JsonObject> future = federationServiceHelper.executeQueryPaginated(null,uriEntry.getKey(),uriEntry.getValue(),query, pageSize,nodeTimeout,limit);
                JsonObject jResult = future.join();
                results.get(uriEntry.getKey()).add(future);
                //futures.get(uriEntry.getKey()).add(federationServiceHelper.executeQueryPaginated(null,uriEntry.getKey(),uriEntry.getValue(),query, pageSize,nodeTimeout,limit));
            } else {
                CompletableFuture<JsonObject> future = federationServiceHelper.executeQuery(null,uriEntry.getKey(),uriEntry.getValue(),query,nodeTimeout,limit);
                JsonObject jResult = future.join();
                results.get(uriEntry.getKey()).add(future);
            }
        }
        for (Map.Entry<String, List<CompletableFuture<JsonObject>>> nodeEntry : results.entrySet()) { // For all nodes
            for (CompletableFuture<JsonObject> f : nodeEntry.getValue()) { // For all futures
                Node node = new Node(nodeEntry.getKey(),uris.get(nodeEntry.getKey()));
                JsonObject jResponse = f.join();
                if (jResponse.has("stats")) // Añado la estadistica
                    jStatsArray.add(jResponse.get("stats"));

                if (jResponse.has("results") && jResponse.get("results").getAsJsonObject().has("bindings")) {
                    for (JsonElement jeTriple : jResponse.get("results").getAsJsonObject().get("bindings").getAsJsonArray()) {
                        JsonObject jTriple = jeTriple.getAsJsonObject();
                        CustomObject co = new CustomObject(jTriple,node);
                        for (String variable : co.getVariables()) {
                            if (!variables.contains(variable))
                                variables.add(variable);
                        }
                        if (objects.containsKey(co.hashCode())) {
                            co = objects.get(co.hashCode());
                            co.addTriplet(jTriple,node);
                        } else {
                            objects.put(co.hashCode(),co);
                        }
                    }
                }
            }
        }
        if (variables!=null) {
            JsonArray jVariables = new JsonArray();
            for (String var : variables) {
                jVariables.add(var);
            }
            JsonObject jHead = new JsonObject();
            jHead.add("vars",jVariables);
            jFederatedResponse.add("head",jHead);
        }
        if (jStatsArray!=null) {
            jFederatedResponse.add("stats",jStatsArray);
        }
        if (objects!=null) {
            JsonObject jBindings = new JsonObject();
            JsonArray jBindingsArray = new JsonArray();
            for (CustomObject co : objects.values()) {
                jBindingsArray.addAll(co.generateJsonResults());
            }
            jBindings.add("bindings",jBindingsArray);
            jFederatedResponse.add("results",jBindings);
        }

        return jFederatedResponse;
    }

}
