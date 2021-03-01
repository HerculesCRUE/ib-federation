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
public class PubMedHandler implements LODHandler {

    public static final String DATASET = "PUBMED";

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
                        mapping.getSuffixURL()
                }));
                for (LodDataSet.Dataset.Connection.Mapping.LocalClass lc : mapping.getLocalClasses()) {
                    List<String> localAttributes = lc.getAttributes();
                    Object value = tos.getAttributeValue(tos.getAttributes(),localAttributes);
                    if (Utils.isPrimitive(value)) {
                        if (!mapping.isIdentifier())
                            value = textHandlerService.removeStopWords(value.toString());
                        if (mapping.getParamType() == LodDataSet.Dataset.Connection.Mapping.ParamType.QUERY) {
                            try {
                                Map<String,String> qParams = new HashMap<>();
                                qParams.put("term",value.toString());
                                qParams.put("field",mapping.getRemoteAttribute());
                                qParams.put("sort","relevance");
                                qParams.put("retmax","3");
                                qParams.put("api_key",con.getApiKey());
                                JsonElement jResponse = Utils.doRequestXML(url, Connection.Method.GET, null, null, qParams, true);
                                if (
                                        jResponse!=null &&
                                        jResponse.isJsonObject() &&
                                        jResponse.getAsJsonObject().has("eSearchResult") &&
                                        jResponse.getAsJsonObject().get("eSearchResult").getAsJsonObject().has("IdList")
                                ) {
                                    List<String> ids = new ArrayList<>();
                                    if (jResponse.getAsJsonObject().get("eSearchResult").getAsJsonObject().get("IdList").isJsonArray()) {
                                        for (JsonElement jeItem : jResponse.getAsJsonObject().get("eSearchResult").getAsJsonObject().get("IdList").getAsJsonArray()) {
                                            if (!jeItem.isJsonNull() && jeItem.isJsonObject() && jeItem.getAsJsonObject().has("id")) {
                                                String id = jeItem.getAsJsonObject().get("Id").getAsString();
                                                ids.add(id);
                                            }
                                        }
                                    } else {
                                        ids.add(jResponse.getAsJsonObject().get("eSearchResult").getAsJsonObject().get("IdList").getAsJsonObject().get("Id").getAsString());
                                    }
                                    if (!ids.isEmpty()) {
                                        for (String id : ids) {
                                            url = buildURL(Arrays.asList(new String[] {
                                                    connection.getBaseURL(),
                                                    "/efetch.fcgi"
                                            }));
                                            qParams = new HashMap<>();
                                            qParams.put("db","pubmed");
                                            qParams.put("id",id);
                                            qParams.put("retmode","xml");
                                            qParams.put("api_key",con.getApiKey());
                                            jResponse = Utils.doRequestXML(url, Connection.Method.GET, null, null, qParams, true);
                                            Set<TripleObjectLink> resultsAux = parseResult(id,url.toString()+"?db=pubmed&id="+id+"&retmode=xml",tos, DATASET, con.getBaseURL(), mapping.getRemoteName(), lc.getName(), jResponse.getAsJsonObject(), lc.getMappers());
                                            if (resultsAux != null && resultsAux.size() > 0) {
                                                results.addAll(resultsAux);
                                            }
                                        }
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

    private Set<TripleObjectLink> parseResult(String id , String url,TripleObjectSimplified tos,String datasetName, String baseURL, String remoteName, String localClassName, JsonObject jResponse, List<LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper> mappers) {
        Set<TripleObjectLink> tripleObjectLinks = new HashSet<>();
        if (jResponse.has("PubmedArticleSet")) {

            JsonObject jResponses = jResponse.get("PubmedArticleSet").getAsJsonObject();
            jResponses = jResponses.get("PubmedArticle").getAsJsonObject();

            JsonObject jMessage = jResponses;
            if (
                    jMessage.has("MedlineCitation") &&
                    jMessage.get("MedlineCitation").getAsJsonObject().has("Article") &&
                    jMessage.get("MedlineCitation").getAsJsonObject().get("Article").getAsJsonObject().has("ArticleTitle")
            ) {
                jMessage.addProperty("title",jMessage.get("MedlineCitation").getAsJsonObject().get("Article").getAsJsonObject().get("ArticleTitle").getAsString());
            }
            JsonObject jPrefixes = new JsonObject();
            jPrefixes.addProperty("default", baseURL+"/");
            JsonArray jLinks = new JsonArray();
            JsonObject jLink = new JsonObject();
            jLink.addProperty("type", "self");
            jLink.addProperty("link", url);
            jLinks.add(jLink);
            TripleObjectLink tol = new TripleObjectLink(datasetName, baseURL, remoteName, localClassName);
            tol.setOrigin(tos);
            tol.populatePrefixes(jPrefixes);
            tol.setId(id);
            tol.populateMapper(mappers);
            tol.populateLinks(jLinks);
            tol.populateAttributes(removeDateParts(jMessage));
            tripleObjectLinks.add(tol);
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
