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
public class WikidataHandler implements LODHandler {

    public static final String DATASET = "WIKIDATA";

    @Override
    public Set<TripleObjectLink> findLink(TripleObjectSimplified tos, LodDataSet.Dataset dataset, TextHandlerServiceImp textHandlerService) {
        Set<TripleObjectLink> result = new HashSet<>();
        try {
            boolean isCompleted = false;
            LodDataSet.Dataset prunedDataset =
                    dataset.getPrunedDatasetSortedFilteredByConnectionType(
                            Arrays.asList(new LodDataSet.Dataset.Connection.ConnectionType [] {
                                    LodDataSet.Dataset.Connection.ConnectionType.SPARQL
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
                        if (mapping.getParamType() == LodDataSet.Dataset.Connection.Mapping.ParamType.SPARQL) {
                            try {
                                Map<String,String> queryParam = new HashMap<>();
                                String regex = "\\$"+mapping.getRemoteAttribute()+"\\$";
                                String q = mapping.getQuery().replaceAll(regex,value.toString());
                                queryParam.put("query", q);
                                Map<String,String> headers = new HashMap<>();
                                headers.put("Accept","application/json");
                                headers.put("Host","query.wikidata.org");
                                JsonElement jResponse = Utils.doRequest(url, Connection.Method.GET, headers, null, queryParam, false);
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

        final String DEFAULT_URI = "http://www.wikidata.org/entity/";
        final String DEFAULT_PREFIX = "wde";

        Set<TripleObjectLink> tripleObjectLinks = new HashSet<>();
        if (jResponse.has("results")) {

            JsonObject jResults = jResponse.get("results").getAsJsonObject();

            JsonObject jPrefixes = new JsonObject();
            jPrefixes.addProperty(DEFAULT_PREFIX,DEFAULT_URI);
            jPrefixes.addProperty("skos",DEFAULT_URI);
            Set<String> objectLinks = new HashSet<>();

            // Atributos de la entidad
            JsonObject jAttributes = new JsonObject();

            if (jResults.has("bindings") && jResults.get("bindings").isJsonArray()) {
                for (JsonElement jeAtt: jResults.get("bindings").getAsJsonArray()) {
                    JsonObject jAtt = jeAtt.getAsJsonObject();
                    // Añado el enlace
                    if (jAtt.has("company") && jAtt.get("company").isJsonObject() && jAtt.get("company").getAsJsonObject().has("value")) {
                        objectLinks.add(jAtt.get("company").getAsJsonObject().get("value").getAsString());
                    }
                    // Propiedad
                    String uri = null;
                    if (jAtt.has("wd") && jAtt.get("wd").isJsonObject() && jAtt.get("wd").getAsJsonObject().has("value")) {
                        uri = jAtt.get("wd").getAsJsonObject().get("value").getAsString().replace(DEFAULT_URI,DEFAULT_PREFIX+":");
                    }
                    String value = null;
                    if (jAtt.has("ps_Label") && jAtt.get("ps_Label").isJsonObject() && jAtt.get("ps_Label").getAsJsonObject().has("value")) {
                        value = jAtt.get("ps_Label").getAsJsonObject().get("value").getAsString();
                    }
                    if (Utils.isValidString(uri) && Utils.isValidString(value)) {
                        if (jAttributes.has(uri)) {
                            String oldValue = jAttributes.get(uri).getAsString();
                            jAttributes.addProperty(uri,oldValue+","+value);
                        } else {
                            jAttributes.addProperty(uri,value);
                        }
                    }
                    // Valor
                }
            }



            JsonArray jLinks = new JsonArray();
            for (String link : objectLinks) {
                JsonObject jLink = new JsonObject();
                jLink.addProperty("type","self");
                jLink.addProperty("link",link);
                jLinks.add(jLink);
            }
            TripleObjectLink tol = new TripleObjectLink(datasetName,baseURL,remoteName,localClassName);
            tol.setOrigin(tos);
            tol.populatePrefixes(jPrefixes);
            if (jLinks.size()>0) {
                String uriId = jLinks.get(0).getAsJsonObject().get("link").getAsString();
                String[] uriIdParts = uriId.split("/");
                String id = null;
                for (int i = uriIdParts.length-1; i >= 0 ; i--) {
                    if (Utils.isValidString(uriIdParts[i])) {
                        id = uriIdParts[i];
                        break;
                    }
                }
                tol.setId(id);
            }
            tol.populateMapper(mappers);
            tol.populateLinks(jLinks);
            tol.populateAttributes(jAttributes);
            tripleObjectLinks.add(tol);

        }
        return tripleObjectLinks;
    }


}
