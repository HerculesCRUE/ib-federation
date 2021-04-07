package es.um.asio.service.service.impl;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.config.DataSourceRepository;

import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.model.URIComponent;
import es.um.asio.service.repository.SparqlProxyHandler;
import es.um.asio.service.service.DataFetcherService;
import es.um.asio.service.service.HttpRequestHelper;
import es.um.asio.service.service.SchemaService;
import es.um.asio.service.service.ServiceDiscoveryService;
import es.um.asio.service.util.Utils;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;


@Service
public class DataFetcherServiceImpl implements DataFetcherService {

    @Autowired
    ServiceDiscoveryService serviceDiscoveryService;

    @Autowired
    SchemaServiceImp serviceImp;

    @Autowired
    SchemaService schemaService;

    @Autowired
    HttpRequestHelper httpRequestHelper;

    @Value("${app.default-request-timeout}")
    Integer defaultTimeout;

    @Value("${app.trellis-host}")
    String trellisHost;

    private final Logger logger = LoggerFactory.getLogger(DataFetcherServiceImpl.class);

    @Override
    public Set<String> getObjectsUris(String domain,String nodeName, String service, String tripleStore) throws IOException {
        logger.info("Request Object URIs. Node: {}, Service: {}, TripleStore {}", nodeName,service,tripleStore);
        Set<String> objects = new HashSet<>();
        DataSourceRepository.Node node = serviceDiscoveryService.getNode(nodeName);
        logger.info("Node in DataSourceRepository: {}", node.toString());
        if (node!=null) {
            DataSourceRepository.Node.Service serv = node.getServiceByName(service);
            logger.info("Service in DataSourceRepository: {}", serv.toString());
            if (serv!=null) {
                DataSourceRepository.Node.Service.Type type = serv.getTypeByName(tripleStore);
                logger.info("Type in DataSourceRepository: {}", type.toString());
                if (type != null) {
                    URL url = Utils.buildURL(serv.getBaseURL(),serv.getPort(),type.getSuffixURL());
                    logger.info("URL: {}", url);
                    String query = "SELECT DISTINCT ?object " +
                            "WHERE { " +
                            "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  ?object . " +
                            " FILTER regex(str(?object),\"^http[s]*://"+domain+"/"+node.getName()+"/*/*/\")" +
                            "}";
                    Map<String,String> queryParam = new HashMap<>();
                    queryParam.put("nodeTimeout",String.valueOf(defaultTimeout));
                    queryParam.put("pageSize","50000");
                    queryParam.put("query",query);
                    logger.info("queryParam: {}", queryParam);
                    JsonElement jeResponse = Utils.doRequest(url, Connection.Method.GET,null,null,queryParam,true);
                    logger.info("Response: {}", jeResponse.toString());
                    if (jeResponse != null && !jeResponse.isJsonNull() && jeResponse.isJsonObject()) {
                        JsonObject jResponse = jeResponse.getAsJsonObject();
                        for (JsonElement jeItem : getJsonItems(jResponse)) {
                            JsonObject jItem = jeItem.getAsJsonObject();
                            if (jItem.has("object")) {
                                JsonObject jObject = jItem.get("object").getAsJsonObject();
                                if (jObject.has("value")) {
                                    objects.add(jObject.get("value").getAsString());
                                }
                            }
                        }
                    }
                }
            }
        }
        return objects;
    }


    @Override
    public Set<TripleObjectSimplified> getTripleObjectSimplified(String domain,String nodeName, String service, String tripleStore, String className) throws URISyntaxException, IOException {
        Map<String,TripleObjectSimplified> objects = new HashMap<>();
        DataSourceRepository.Node node = serviceDiscoveryService.getNode(nodeName);
        if (node!=null) {
            DataSourceRepository.Node.Service serv = node.getServiceByName(service);
            DataSourceRepository.Node.Service serviceTrellis = node.getServiceByName("trellis");
            if (serv!=null) {
                DataSourceRepository.Node.Service.Type type = serv.getTypeByName(tripleStore);
                if (type != null) {
                    // Get data
                    URL url = Utils.buildURL(serv.getBaseURL(),serv.getPort(),type.getSuffixURL());
                    String query = "SELECT ?s ?p ?o WHERE { ?s ?p  ?o . FILTER ( ( regex(str(?s),\"^http[s]*://.*/"+normalizeClassName(className)+"/.*\" ))  && ( regex(str(?s),\"^http[s]*://.*/rec/.*\" ))&& ( ?p != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ) ) }";
                    String s = normalizeClassName(className);

                    Map<String,String> queryParam = new HashMap<>();
                    queryParam.put("nodeTimeout",String.valueOf(defaultTimeout));
                    queryParam.put("pageSize","50000");
                    queryParam.put("query",query);
                    JsonElement jeResponse = Utils.doRequest(url, Connection.Method.GET,null,null,queryParam,true);


                    if (jeResponse != null && !jeResponse.isJsonNull() && jeResponse.isJsonObject()) {
                        JsonObject jResponse = jeResponse.getAsJsonObject();
                        JsonArray jVarsArray = jResponse.get("head").getAsJsonObject().get("vars").getAsJsonArray();
                        String subjectVar = jVarsArray.get(0).getAsString();
                        String predicateVar = jVarsArray.get(1).getAsString();
                        String objectVar = jVarsArray.get(2).getAsString();
                        for (JsonElement jeItem : getJsonItems(jResponse)) {
                            JsonObject jItem = jeItem.getAsJsonObject();
                            JsonObject jSubject = jItem.get(subjectVar).getAsJsonObject();
                            JsonObject jPredicate = jItem.get(predicateVar).getAsJsonObject();
                            JsonObject jObject = jItem.get(objectVar).getAsJsonObject();
                            URIComponent uriComponent = schemaService.getURIComponentFromCanonicalLocalURI(jSubject.get("value").getAsString());
                            TripleObjectSimplified tripleObjectSimplified;
                            if (objects.containsKey(uriComponent.getReference())) {
                                tripleObjectSimplified = objects.get(uriComponent.getReference());
                            } else {
                                tripleObjectSimplified = new TripleObjectSimplified(nodeName,tripleStore,className,uriComponent.getReference());
                                objects.put(tripleObjectSimplified.getId(),tripleObjectSimplified);
                            }
                            String predicate = schemaService.getURIComponentFromCanonicalLocalURI(jPredicate.get("value").getAsString()).getConcept();
                            String value = jObject.get("value").getAsString();
                            tripleObjectSimplified.addAttribute(predicate,value);
                        }
                    }

                    // Get Metadata and merge
                    Map<String, Map<String,String>> metadata = getMetadata(nodeName,service,tripleStore,className);
                    for (Map.Entry<String, Map<String, String>> mdEntry : metadata.entrySet()) {
                        String id = mdEntry.getKey();
                        String uri = String.valueOf(mdEntry.getValue().get("uri"));
                        if (uri.contains("trellis:data") && trellisHost !=null) {
                            uri = uri.replace("trellis:data",serviceTrellis.buildBaseURL());
                        }
                        Long lastModification = Long.valueOf(mdEntry.getValue().get("lastModification"));
                        TripleObjectSimplified tos = objects.get(id);
                        if (tos!=null) {
                            tos.setLocalURI(uri);
                            tos.setLastModification(lastModification);
                        }
                    }
                }
            }
        }
        return new HashSet<>(objects.values());
    }

    @Override
    public TripleObjectSimplified findTripleObjectSimplifiedByURI(String domain,String nodeName, String service, String tripleStore, String className, String uri) throws URISyntaxException, IOException {
        TripleObjectSimplified tos = null;
        DataSourceRepository.Node node = serviceDiscoveryService.getNode(nodeName);
        if (node!=null) {
            DataSourceRepository.Node.Service serv = node.getServiceByName(service);
            DataSourceRepository.Node.Service serviceTrellis = node.getServiceByName("trellis");
            if (serv!=null) {
                DataSourceRepository.Node.Service.Type type = serv.getTypeByName(tripleStore);
                if (type != null) {
                    // Get data
                    URL url = Utils.buildURL(serv.getBaseURL(),serv.getPort(),type.getSuffixURL());
                    String query = "SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object . FILTER regex(str(?subject),\""+uri+"\") }";

                    Map<String,String> queryParam = new HashMap<>();
                    queryParam.put("nodeTimeout",String.valueOf(defaultTimeout));
                    queryParam.put("pageSize","50000");
                    queryParam.put("query",query);
                    JsonElement jeResponse = Utils.doRequest(url, Connection.Method.GET,null,null,queryParam,true);


                    if (jeResponse != null && !jeResponse.isJsonNull() && jeResponse.isJsonObject()) {
                        JsonObject jResponse = jeResponse.getAsJsonObject();
                        JsonArray jVarsArray = jResponse.get("head").getAsJsonObject().get("vars").getAsJsonArray();
                        String subjectVar = jVarsArray.get(0).getAsString();
                        String predicateVar = jVarsArray.get(1).getAsString();
                        String objectVar = jVarsArray.get(2).getAsString();
                        for (JsonElement jeItem : getJsonItems(jResponse)) {
                            JsonObject jItem = jeItem.getAsJsonObject();
                            JsonObject jSubject = jItem.get(subjectVar).getAsJsonObject();
                            JsonObject jPredicate = jItem.get(predicateVar).getAsJsonObject();
                            JsonObject jObject = jItem.get(objectVar).getAsJsonObject();
                            URIComponent uriComponent = schemaService.getURIComponentFromCanonicalLocalURI(jSubject.get("value").getAsString());
                            if (tos == null) {
                                tos = new TripleObjectSimplified(nodeName,tripleStore,className,uriComponent.getReference());
                            }
                            String predicate = schemaService.getURIComponentFromCanonicalLocalURI(jPredicate.get("value").getAsString()).getConcept();
                            String value = jObject.get("value").getAsString();
                            if (predicate!=null)
                                tos.addAttribute(predicate,value);
                        }
                    }

                    // Get Metadata and merge
                    if (tos!=null) {
                        Map<String, Map<String, String>> metadata = getMetadataByInstance(nodeName, service, tripleStore, className, tos.getId());
                        if (metadata!=null && metadata.size()>0) {
                            for (Map.Entry<String, Map<String, String>> mdEntry : metadata.entrySet()) {
                                String id = mdEntry.getKey();
                                if (id.equals(tos.getId())) {
                                    String uriAux = String.valueOf(mdEntry.getValue().get("uri"));
                                    if (uriAux.contains("trellis:data") && serviceTrellis != null) {
                                        uriAux = uriAux.replace("trellis:data", serviceTrellis.buildBaseURL());
                                    }
                                    Long lastModification = Long.valueOf(mdEntry.getValue().get("lastModification"));
                                    tos.setLocalURI(uriAux);
                                    tos.setLastModification(lastModification);
                                }
                            }
                        }
                    }
                }
            }
        }
        return tos;
    }

    private Map<String, Map<String,String>> getMetadata(String nodeName, String service, String tripleStore, String className) throws IOException {
        Map<String, Map<String,String>>  metadata  = new HashMap<>();
        DataSourceRepository.Node node = serviceDiscoveryService.getNode(nodeName);
        if (node!=null) {
            DataSourceRepository.Node.Service serv = node.getServiceByName(service);
            if (serv!=null) {
                DataSourceRepository.Node.Service.Type type = serv.getTypeByName(tripleStore);
                if (type != null) {
                    // Get data
                    URL url = Utils.buildURL(serv.getBaseURL(),serv.getPort(),type.getSuffixURL());
                    String query = "SELECT ?subject ?object\n" +
                            "WHERE {\n" +
                            "?subject ?p <http://www.w3.org/ns/ldp#RDFSource> .\n" +
                            "?subject <http://purl.org/dc/terms/modified> ?object .\n" +
                            "FILTER regex(str(?subject),\"^.*/"+normalizeClassName(className)+"/.*\")\n" +
                            "}";
                    Map<String,String> queryParam = new HashMap<>();
                    queryParam.put("nodeTimeout",String.valueOf(defaultTimeout));
                    queryParam.put("pageSize","50000");
                    queryParam.put("query",query);
                    JsonElement jeResponse = Utils.doRequest(url, Connection.Method.GET,null,null,queryParam,true);

                    if (jeResponse != null && !jeResponse.isJsonNull() && jeResponse.isJsonObject()) {
                        JsonObject jResponse = jeResponse.getAsJsonObject();
                        JsonArray jVarsArray = jResponse.get("head").getAsJsonObject().get("vars").getAsJsonArray();
                        String subjectVar = jVarsArray.get(0).getAsString();
                        String objectVar = jVarsArray.get(1).getAsString();
                        for (JsonElement jeItem : getJsonItems(jResponse)) {
                            JsonObject jItem = jeItem.getAsJsonObject();
                            JsonObject jSubject = jItem.get(subjectVar).getAsJsonObject();
                            JsonObject jObject = jItem.get(objectVar).getAsJsonObject();
                            String uri = jSubject.get("value").getAsString();
                            String[] uriChunks = uri.split("/");
                            String id = uriChunks[uriChunks.length-1];
                            Date modified = util.Utils.getDate(jObject.get("value").getAsString());
                            if (!metadata.containsKey(id))
                                metadata.put(id,new HashMap<>());
                            metadata.get(id).put("uri",uri);
                            metadata.get(id).put("lastModification",String.valueOf(modified.getTime()));
                        }
                    }
                }
            }
        }
        return metadata;
    }

    private Map<String, Map<String,String>> getMetadataByInstance(String nodeName, String service, String tripleStore, String className, String idInstance) throws IOException {
        Map<String, Map<String,String>>  metadata  = new HashMap<>();
        DataSourceRepository.Node node = serviceDiscoveryService.getNode(nodeName);
        if (node!=null) {
            DataSourceRepository.Node.Service serv = node.getServiceByName(service);
            if (serv!=null) {
                DataSourceRepository.Node.Service.Type type = serv.getTypeByName(tripleStore);
                if (type != null) {
                    // Get data
                    URL url = Utils.buildURL(serv.getBaseURL(),serv.getPort(),type.getSuffixURL());
                    String query = "SELECT ?subject ?object\n" +
                            "WHERE {\n" +
                            "?subject ?p <http://www.w3.org/ns/ldp#RDFSource> .\n" +
                            "?subject <http://purl.org/dc/terms/modified> ?object .\n" +
                            "  FILTER ((regex(str(?subject),\"^.*"+className+".*\")) && (regex(str(?subject),\"^.*"+idInstance+".*\")))\n" +
                            "}";

                    Map<String,String> queryParam = new HashMap<>();
                    queryParam.put("nodeTimeout",String.valueOf(defaultTimeout));
                    queryParam.put("pageSize","50000");
                    queryParam.put("query",query);
                    JsonElement jeResponse = Utils.doRequest(url, Connection.Method.GET,null,null,queryParam,true);

                    if (jeResponse != null && !jeResponse.isJsonNull() && jeResponse.isJsonObject()) {
                        JsonObject jResponse = jeResponse.getAsJsonObject();
                        JsonArray jVarsArray = jResponse.get("head").getAsJsonObject().get("vars").getAsJsonArray();
                        String subjectVar = jVarsArray.get(0).getAsString();
                        String objectVar = jVarsArray.get(1).getAsString();
                        for (JsonElement jeItem : getJsonItems(jResponse)) {
                            JsonObject jItem = jeItem.getAsJsonObject();
                            JsonObject jSubject = jItem.get(subjectVar).getAsJsonObject();
                            JsonObject jObject = jItem.get(objectVar).getAsJsonObject();
                            String uri = jSubject.get("value").getAsString();
                            String[] uriChunks = uri.split("/");
                            String id = uriChunks[uriChunks.length-1];
                            Date modified = util.Utils.getDate(jObject.get("value").getAsString());
                            if (!metadata.containsKey(id))
                                metadata.put(id,new HashMap<>());
                            metadata.get(id).put("uri",uri);
                            metadata.get(id).put("lastModification",String.valueOf(modified.getTime()));
                        }
                    }
                }
            }
        }
        return metadata;
    }

/*    private JsonObject doRequest(URL url, String query) throws IOException {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        return httpRequestHelper.doQueryRequest(url,query, Connection.Method.GET, headers, defaultTimeout);
    }*/

    private JsonArray getJsonItems(JsonObject jResults) {
        if (jResults!=null && jResults.has("results"))
            if (jResults.get("results").getAsJsonObject().has("bindings"))
                return jResults.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
        return new JsonArray();
    }

    private String normalizeClassName(String className) {
        String [] chunkedClassName = className.split("\\-");
        String [] chunkedClassNameRegex = new String[chunkedClassName.length];
        for (int i = 0; i < chunkedClassName.length ; i++) {
            char first = chunkedClassName[i].charAt(0);
            chunkedClassNameRegex[i] = String.format("[%s|%s]%s",Character.toLowerCase(first),Character.toUpperCase(first),chunkedClassName[i].substring(1));
        }
        return String.join("(\\\\-)*",chunkedClassNameRegex);
    }

}
