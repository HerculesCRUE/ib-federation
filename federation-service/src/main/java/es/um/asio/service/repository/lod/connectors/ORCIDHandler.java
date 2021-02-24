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
public class ORCIDHandler implements LODHandler {

    public static final String DATASET = "ORCID";

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
                    if (mapping.isIdentifier()) {
                        Object value = tos.getAttributeValue(tos.getAttributes(), lc.getAttributes());
                        if (Utils.isValidString(value.toString())) {
                            String orcid = value.toString();
                            JsonObject jMasterAttributes = new JsonObject();
                            jMasterAttributes.addProperty("mOrcid",orcid);
                            jMasterAttributes.addProperty("baseURL",connection.getBaseURL());
                            Set<TripleObjectLink> resultsAux = getDataWithIdentifier(tos,con,mapping,lc,orcid,jMasterAttributes);
                            if (resultsAux == null || resultsAux.size() == 0)
                                continue;
                            else {
                                results.addAll(resultsAux);
                                return results;
                            }
                        } else
                            continue;
                    } else {
                        List<String> localAttributes = lc.getAttributes();
                        Object value = "";
                        if (tos.getAttributeValue(tos.getAttributes(), "name") != null) {
                            value = tos.getAttributeValue(tos.getAttributes(), localAttributes);
                        } else {
                            Object firstName = tos.getAttributeValue(tos.getAttributes(), localAttributes);
                            Object lastName = tos.getAttributeValue(tos.getAttributes(), localAttributes);
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
                                qParams.put("rows", "1");
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Accept", "application/json");
                                JsonElement jResponse = Utils.doRequest(url, Connection.Method.GET, headers, null, qParams, true);
                                if (jResponse != null && jResponse.isJsonObject() && jResponse.getAsJsonObject().has("expanded-result") && jResponse.getAsJsonObject().get("expanded-result").isJsonArray()) {


                                    for (JsonElement jeResponseItem : jResponse.getAsJsonObject().get("expanded-result").getAsJsonArray()) {
                                        JsonObject jResponseItem = jeResponseItem.getAsJsonObject();
                                        if (jResponseItem.has("orcid-id")) {
                                            JsonObject jMasterAttributes = new JsonObject();
                                            String orcid = jResponseItem.get("orcid-id").getAsString();
                                            jMasterAttributes.addProperty("mORCID",orcid);
                                            jMasterAttributes.addProperty("baseURL",connection.getBaseURL());
                                            if (jResponseItem.has("given-names"))
                                                jMasterAttributes.addProperty("mFirstName",jResponseItem.get("given-names").getAsString());
                                            if (jResponseItem.has("family-names"))
                                                jMasterAttributes.addProperty("mSurname",jResponseItem.get("family-names").getAsString());
                                            if (jResponseItem.has("credit-name"))
                                                jMasterAttributes.addProperty("mName",jResponseItem.get("credit-name").getAsString());
                                            Set<TripleObjectLink> resultsAux = getDataWithIdentifier(tos, con, mapping, lc, orcid,jMasterAttributes);
                                            if (resultsAux == null || resultsAux.size() == 0)
                                                continue;
                                            else {
                                                results.addAll(resultsAux);
                                                return results;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } // Fin else
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    private Set<TripleObjectLink> getDataWithIdentifier(TripleObjectSimplified tos,LodDataSet.Dataset.Connection con, LodDataSet.Dataset.Connection.Mapping mapping, LodDataSet.Dataset.Connection.Mapping.LocalClass lc, String orcid, JsonObject jMasterAttributes) {
        JsonObject jPersonResponse = doRequestByORCID(con.getBaseURL(),orcid);
        for (Map.Entry<String, JsonElement> jMasterAtt: jMasterAttributes.entrySet()) {
            jPersonResponse.add(jMasterAtt.getKey(),jMasterAtt.getValue());
        }
        if (jPersonResponse==null) {
            return null;
        } else {
            Set<TripleObjectLink> triplesLinks = parseResult(orcid,tos,DATASET, con.getBaseURL(), mapping.getRemoteName(), lc.getName(), jPersonResponse, lc.getMappers());
            return triplesLinks;
        }
    }



    private JsonObject doRequestByORCID(String baseURL,String orcid) {
        JsonObject jResponsePerson = null;
        Map<String,String> headers = new HashMap<>();
        headers.put("Accept","application/json");
        JsonArray jLinks = new JsonArray();
        try {
            String personUri = baseURL + "/" + orcid + "/person";
            JsonElement jeResponsePerson = Utils.doRequest(new URL(personUri), Connection.Method.GET, headers, null, null, true);
            if (jeResponsePerson != null && jeResponsePerson.isJsonObject()) {
                JsonObject jLink = new JsonObject();
                jLink.addProperty("type","person");
                jLink.addProperty("link",personUri);
                jLink.addProperty("content-type","application/json");
                jLinks.add(jLink);
                jResponsePerson = jeResponsePerson.getAsJsonObject();
                String worksUri = baseURL + "/" + orcid + "/works";
                JsonElement jeResponseWorks = Utils.doRequest(new URL(worksUri), Connection.Method.GET, headers, null, null, true);
                if (jeResponseWorks != null && jeResponseWorks.isJsonObject() && jeResponseWorks.getAsJsonObject().has("group")) {
                    JsonObject jLinkWork = new JsonObject();
                    jLinkWork.addProperty("type","work");
                    jLinkWork.addProperty("link",worksUri);
                    jLinkWork.addProperty("content-type","application/json");
                    jLinks.add(jLinkWork);
                    jResponsePerson.add("works", jeResponseWorks.getAsJsonObject().get("group"));
                }
            }
            jResponsePerson.add("link",jLinks);
            return jResponsePerson;
        } catch (Exception e) {

        }
        return jResponsePerson;
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

    private Set<TripleObjectLink> parseResult(String id,TripleObjectSimplified tos,String datasetName, String baseURL, String remoteName, String localClassName, JsonObject jResponse, List<LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper> mappers) {
        Set<TripleObjectLink> tripleObjectLinks = new HashSet<>();

        JsonObject jMessage = removeValueAttributesAndAddLinks(jResponse);

        JsonObject jPrefixes = new JsonObject();
        jPrefixes.addProperty("default",baseURL+"/");
        JsonArray jLinks = new JsonArray();
        if (jMessage!=null) {
            if (jMessage.has("link")) {
                for (JsonElement jeLinks :jMessage.get("link").getAsJsonArray()) {
                    JsonObject jLink = new JsonObject();
                    if (jeLinks.getAsJsonObject().has("type")) {
                        jLink.addProperty("type",jeLinks.getAsJsonObject().get("type").getAsString());
                    }
                    if (jeLinks.getAsJsonObject().has("link")) {
                        jLink.addProperty("link",jeLinks.getAsJsonObject().get("link").getAsString());
                    }
                    if (jeLinks.getAsJsonObject().has("content-type")) {
                        jLink.addProperty("content-type",jeLinks.getAsJsonObject().get("content-type").getAsString());
                    }
                    if (jeLinks.getAsJsonObject().has("content-version")) {
                        jLink.addProperty("content-version",jeLinks.getAsJsonObject().get("content-version").getAsString());
                    }
                    if (jLink.size()>0) {
                        jLinks.add(jLink);
                    }
                }
            }
        }
        jMessage.remove("link");
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

    private JsonObject removeValueAttributesAndAddLinks(JsonObject jObject) {
        JsonObject jObjectCopy = new JsonObject();
        for (Map.Entry<String, JsonElement> jAttr :jObject.entrySet()) { // para todos los atributos
            if (jAttr.getValue().isJsonPrimitive()) {
                jObjectCopy.add(jAttr.getKey(),jAttr.getValue());
            } else {
                if (jAttr.getValue().isJsonObject()) {
                    if (jAttr.getValue().getAsJsonObject().has("value")) {
                        jObjectCopy.add(jAttr.getKey(),jAttr.getValue().getAsJsonObject().get("value"));
                    } else { // Si es un objeto anidado
                        jObjectCopy.add(jAttr.getKey(), removeValueAttributesAndAddLinks(jAttr.getValue().getAsJsonObject()));
                    }
                } else if (jAttr.getValue().isJsonArray()) { // Es una lista
                    JsonArray jArray = new JsonArray();
                    for (JsonElement jeItem : jAttr.getValue().getAsJsonArray()) {
                        if (jeItem.isJsonPrimitive()) {
                            jArray.add(jeItem);
                        } else {
                            jArray.add(removeValueAttributesAndAddLinks(jeItem.getAsJsonObject()));
                        }
                    }
                    jObjectCopy.add(jAttr.getKey(),jArray);
                } else if (!jAttr.getValue().isJsonNull()){
                    System.out.println("que coño es?");
                }
            }
        }
        return jObjectCopy;
    }


}
