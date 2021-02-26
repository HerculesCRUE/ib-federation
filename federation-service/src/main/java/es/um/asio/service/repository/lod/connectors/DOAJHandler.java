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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Repository
public class DOAJHandler implements LODHandler {

    public static final String DATASET = "DOAJ";

    @Autowired
    TextHandlerServiceImp textHandlerServiceImp;

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
            for (LodDataSet.Dataset.Connection.Mapping mapping : connection.getSortedMappings()) {
                URL url = buildURL(Arrays.asList(new String[] {
                        connection.getBaseURL(),
                        mapping.getSuffixURL(),
                        mapping.getRemoteAttribute()
                }));
                for (LodDataSet.Dataset.Connection.Mapping.LocalClass lc : mapping.getLocalClasses()) {
                    List<String> localAttributes = lc.getAttributes();
                    Object value = tos.getAttributeValue(tos.getAttributes(),localAttributes);
                    if (Utils.isPrimitive(value)) {
                        if (!mapping.isIdentifier())
                            value = textHandlerService.removeStopWords(value.toString());
                        if (mapping.getParamType() == LodDataSet.Dataset.Connection.Mapping.ParamType.URI) {
                            url = new URL(url.toURI().toString().replaceAll("\\$var\\$", URLEncoder.encode(value.toString())));
                            try {
                                JsonElement jResponse = Utils.doRequest(url, Connection.Method.GET, null, null, null, true);
                                if (jResponse!=null) {
                                    results = parseResult(tos,DATASET,con.getBaseURL(),mapping.getRemoteName(),lc.getName(),jResponse.getAsJsonObject(),lc.getMappers());
                                    if (results!=null && results.size()>0) {
                                        return results;
                                    }
                                }
                            } catch (Exception e) {
                                continue;
                            }
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


    private String buildContentQuery(String remoteAttribute, String value, boolean isIdentifier, boolean removeStopwords,TextHandlerServiceImp textHandlerService) throws UnsupportedEncodingException {
        if (!isIdentifier && removeStopwords) {
            value = value.replaceAll("-"," ");
            value = textHandlerService.removeStopWords(value);
            String [] chunks  = value.split("  *");
            value = String.join(" AND ",value.split("  *"));
            //value = URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
        String formatStr = String.format("%s(%s)", remoteAttribute, value);
        return formatStr;
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

    private Set<TripleObjectLink> parseResult(TripleObjectSimplified tos,String datasetName, String baseURL, String remoteName, String localClassName, JsonObject jResponse, List<LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper> mappers) {
        Set<TripleObjectLink> tripleObjectLinks = new HashSet<>();
        if (jResponse.has("results")) {

            JsonArray jResponses = jResponse.get("results").getAsJsonArray();

            for (JsonElement jeItem : jResponses) {
                if (jeItem.isJsonObject() && jeItem.getAsJsonObject().has("bibjson")) {
                    JsonObject jMessage = jeItem.getAsJsonObject().get("bibjson").getAsJsonObject();
                    JsonObject jPrefixes = new JsonObject();
                    jPrefixes.addProperty("default", "https://doaj.org/api/v2/");
                    JsonArray jLinks = new JsonArray();
                    if (jMessage != null) {
                        if (jMessage.has("link")) {
                            for (JsonElement jeLinks : jMessage.get("link").getAsJsonArray()) {
                                JsonObject jLink = new JsonObject();
                                if (jeLinks.getAsJsonObject().has("type")) {
                                    jLink.addProperty("type", jeLinks.getAsJsonObject().get("type").getAsString());
                                }
                                if (jeLinks.getAsJsonObject().has("url")) {
                                    jLink.addProperty("link", jeLinks.getAsJsonObject().get("url").getAsString());
                                }
                                if (jLink.size() > 0) {
                                    jLinks.add(jLink);
                                }
                            }
                        }
                    }
                    jMessage.remove("link");
                    TripleObjectLink tol = new TripleObjectLink(datasetName, baseURL, remoteName, localClassName);
                    tol.setOrigin(tos);
                    tol.populatePrefixes(jPrefixes);
                    if (jeItem.getAsJsonObject().has("id"))
                        tol.setId(jeItem.getAsJsonObject().get("id").getAsString());
                    tol.populateMapper(mappers);
                    tol.populateLinks(jLinks);
                    tol.populateAttributes(removeDateParts(jMessage));
                    tripleObjectLinks.add(tol);
                }
            }

        }
        return tripleObjectLinks;
    }

    private JsonObject removeDateParts(JsonObject jObject) {
        JsonObject jObjectCopy = new JsonObject();
        for (Map.Entry<String, JsonElement> jAttr :jObject.entrySet()) {
            if (jAttr.getValue().isJsonPrimitive()) {
                jObjectCopy.add(jAttr.getKey(),jAttr.getValue());
            } else {
                if (jAttr.getKey().contains("date-parts")) {
                    continue;
                } else if (jAttr.getValue().isJsonObject()) {
                    jObjectCopy.add(jAttr.getKey(),removeDateParts(jAttr.getValue().getAsJsonObject()));
                } else { // Es una lista
                    jObjectCopy.add(jAttr.getKey(),jAttr.getValue());
                }
            }
        }
        return jObjectCopy;
    }


}
