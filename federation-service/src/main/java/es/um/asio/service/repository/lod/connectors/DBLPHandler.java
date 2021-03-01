package es.um.asio.service.repository.lod.connectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.config.LodDataSet;
import es.um.asio.service.model.TripleObjectLink;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.service.impl.TextHandlerServiceImp;
import es.um.asio.service.util.Utils;
import org.jsoup.Connection;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Repository
public class DBLPHandler implements LODHandler {

    public static final String DATASET = "DBLP";

    @Override
    public Set<TripleObjectLink> findLink(TripleObjectSimplified tos, LodDataSet.Dataset dataset, TextHandlerServiceImp textHandlerService) {
        Set<TripleObjectLink> result = new HashSet<>();
        try {
            boolean isCompleted = false;
            LodDataSet.Dataset prunedDataset =
                    dataset.getPrunedDatasetSortedFilteredByConnectionType(
                            Arrays.asList(new LodDataSet.Dataset.Connection.ConnectionType [] {
                                            LodDataSet.Dataset.Connection.ConnectionType.API
                                    }
                            )
                    );
            for (LodDataSet.Dataset.Connection con : prunedDataset.getConnections()) {
                result = handleAPIRequest(con,tos, textHandlerService);
                return result;
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Set<TripleObjectLink> handleAPIRequest(LodDataSet.Dataset.Connection con, TripleObjectSimplified tos,TextHandlerServiceImp textHandlerService) {
        Set<TripleObjectLink> results = new HashSet<>();
        try {
            LodDataSet.Dataset.Connection connection = con.getPrunedConnectionByTripleObjectSimplified(tos);
            String apiKey = connection.getApiKey();
            String fistNameStr = null, surnameStr = null;
            for (LodDataSet.Dataset.Connection.Mapping mapping : connection.getSortedMappings()) {
                URL url = buildURL(Arrays.asList(new String[] {
                        connection.getBaseURL(),
                        mapping.getSuffixURL()
                }));
                for (LodDataSet.Dataset.Connection.Mapping.LocalClass lc : mapping.getLocalClasses()) {
                    List<String> localAttributes = lc.getAttributes();
                    Object value = "";
                    if (tos.getAttributeValue(tos.getAttributes(), "name") != null && false) {
                        value = tos.getAttributeValue(tos.getAttributes(), "name");
                    } else {
                        Object firstName = tos.getAttributeValue(tos.getAttributes(), "firstName");
                        if (firstName!=null)
                            fistNameStr = firstName.toString();
                        Object lastName = tos.getAttributeValue(tos.getAttributes(), "surname");
                        if (lastName!=null)
                            surnameStr = lastName.toString();
                        if (firstName != null) {
                            value = firstName.toString();
                        }
                        if (lastName != null) {
                            if (Utils.isValidString(firstName.toString())) {
                                value = value.toString() + " " + lastName.toString();
                            } else {
                                value = lastName.toString();
                            }
                        }
                        value = firstName + " " + lastName;
                    }
                    if (Utils.isValidString(value.toString())) {
                        try {
                            Map<String, String> qParams = new HashMap<>();
                            qParams.put(mapping.getRemoteAttribute(), value.toString());
                            qParams.put("h", "3");
                            qParams.put("format", "json");
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Accept", "application/json");
                            JsonElement jResponse = Utils.doRequest(url, Connection.Method.GET, headers, null, qParams, true);
                            if (
                                    jResponse != null && jResponse.isJsonObject() &&
                                    jResponse.getAsJsonObject().has("result") &&
                                    jResponse.getAsJsonObject().get("result").getAsJsonObject().has("hits") &&
                                    jResponse.getAsJsonObject().get("result").getAsJsonObject().get("hits").getAsJsonObject().has("hit")
                            ) {

                                for (JsonElement jeResponseItem : jResponse.getAsJsonObject().get("result").getAsJsonObject().get("hits").getAsJsonObject().get("hit").getAsJsonArray()) {
                                    JsonObject jResponseItem = jeResponseItem.getAsJsonObject().get("info").getAsJsonObject();
                                    String urlArticles = jResponseItem.get("url").getAsString();
                                    JsonElement jeResponseArticles = Utils.doRequestXML(new URL(urlArticles+".xml"), Connection.Method.GET, null, null, null, true);
                                    if (jeResponseArticles!=null && jeResponseArticles.isJsonObject() && jeResponseArticles.getAsJsonObject().has("dblpperson")) {
                                        JsonObject jPersonResponse = jeResponseArticles.getAsJsonObject().get("dblpperson").getAsJsonObject();
                                        String id = jPersonResponse.get("pid").getAsString();
                                        String name = jPersonResponse.get("name").getAsString();
                                        if (Utils.isValidString(fistNameStr) && Utils.stringNormalized(name).toLowerCase().contains(Utils.stringNormalized(fistNameStr).toLowerCase()))
                                            jPersonResponse.addProperty("fistName",fistNameStr);
                                        if (Utils.isValidString(surnameStr) && Utils.stringNormalized(name).toLowerCase().contains(Utils.stringNormalized(surnameStr).toLowerCase()))
                                            jPersonResponse.addProperty("surname",surnameStr);
                                        if (jPersonResponse.has("r")) {
                                            JsonArray jArticles = new JsonArray();
                                            for (JsonElement jeArticle : jPersonResponse.get("r").getAsJsonArray()) {
                                                jArticles.add(jeArticle.getAsJsonObject());
                                            }
                                            jPersonResponse.remove("r");
                                            jPersonResponse.add("articles",jArticles);
                                        }

                                        Set<TripleObjectLink> resultsAux = parseResult(id,urlArticles,tos,DATASET, con.getBaseURL(),mapping.getRemoteName(),lc.getName(),jPersonResponse, lc.getMappers() );
                                        if (resultsAux == null || resultsAux.size() == 0)
                                            continue;
                                        else {
                                            results.addAll(resultsAux);
                                        }
                                    }

                                }
                                return results;
                            }

                        } catch (Exception e) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    private URL buildURL(List<String> urlChunks) throws MalformedURLException {
        StringBuffer sb = new StringBuffer();
        List<String> urlCheckedChunks = new ArrayList<>();
        for (String chunk: urlChunks) {
            if (!Utils.isValidString(chunk))
                continue;
            if (chunk.startsWith("/")) {
                urlCheckedChunks.add(chunk.substring(1));
            } else if (chunk.endsWith("/")) {
                urlCheckedChunks.add(chunk.substring(0,chunk.length()-1));
            } else {
                urlCheckedChunks.add(chunk);
            }

        }
        return new URL(String.join("/",urlCheckedChunks));
    }

    private Set<TripleObjectLink> parseResult(String id,String selfLink,TripleObjectSimplified tos,String datasetName, String baseURL, String remoteName, String localClassName, JsonObject jResponse, List<LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper> mappers) {
        Set<TripleObjectLink> tripleObjectLinks = new HashSet<>();

        JsonObject jMessage = jResponse;

        JsonObject jPrefixes = new JsonObject();
        jPrefixes.addProperty("default",baseURL+"/");
        JsonArray jLinks = new JsonArray();
        if (jMessage!=null) {
            JsonObject jLinkSelf = new JsonObject();
            jLinkSelf.addProperty("type","self");
            jLinkSelf.addProperty("link",selfLink);
            jLinks.add(jLinkSelf);
            if (jMessage.has("person") && jMessage.get("person").isJsonObject() && jMessage.get("person").getAsJsonObject().has("url")) {
                for (JsonElement jeLinks : jMessage.get("person").getAsJsonObject().get("url").getAsJsonArray()) {
                    JsonObject jLink = new JsonObject();
                    jLink.addProperty("type","personal-link");
                    if (jeLinks.isJsonPrimitive()) {
                        jLink.addProperty("link",jeLinks.getAsString());
                    }
                    jLinks.add(jLink);
                }
            }
        }
        TripleObjectLink tol = new TripleObjectLink(datasetName,baseURL,remoteName,localClassName);
        tol.setOrigin(tos);
        tol.populatePrefixes(jPrefixes);
        if (Utils.isValidString(id))
            tol.setId(id);
        tol.populateMapper(mappers);
        tol.populateLinks(jLinks);
        tol.populateAttributes(jMessage);
        tripleObjectLinks.add(tol);
        return tripleObjectLinks;
    }


}
