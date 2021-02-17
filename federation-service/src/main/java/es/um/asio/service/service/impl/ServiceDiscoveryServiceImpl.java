package es.um.asio.service.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.config.DataSourceRepository;
import es.um.asio.service.model.constants.Constants;
import es.um.asio.service.service.ServiceDiscoveryService;
import es.um.asio.service.util.Utils;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceDiscoveryServiceImpl implements ServiceDiscoveryService {

    @Autowired
    DataSourceRepository dataSource;

    @Value("${service-discovery-host}")
    private String serviceDiscoveryHost;

    @Override
    public DataSourceRepository.Node getNode(String nodeName) {
        return dataSource.getNodeByName(nodeName);
    }

    @Override
    public DataSourceRepository.Node.Service getService(String nodeName, String service) {
        DataSourceRepository.Node node = getNode(nodeName);
        if (node!=null) {
            return node.getServiceByName(service);
        } else
            return null;
    }

    @Override
    public Map<String, URL> getNodesByNameAndServiceAndType(List<String> nodes, String service, String type) throws URISyntaxException, MalformedURLException {
        Map<String,URL> uris = new HashMap<>();
        try {
            Map<String,String> queryParams = new HashMap<>();
            queryParams.put("serviceName",service);
            queryParams.put("typeName",type);
            JsonElement jeResponse = Utils.doRequest(new URL(serviceDiscoveryHost+"/service-discovery/service/type"), Connection.Method.GET,null,null,queryParams, true);
            if (jeResponse!=null) {
                JsonArray jResponseArray = jeResponse.getAsJsonArray();
                for (JsonElement jeNode : jResponseArray) {
                    String node = jeNode.getAsJsonObject().get("name").getAsString().trim();
                    if (nodes.contains(node)) {
                        for (JsonElement jeService : jeNode.getAsJsonObject().get("services").getAsJsonArray()) {
                            String baseURL = jeService.getAsJsonObject().get("baseURL").getAsString();
                            String port = jeService.getAsJsonObject().get("port").getAsString();
                            for (JsonElement jeType : jeService.getAsJsonObject().get("types").getAsJsonArray()) {
                                String suffix = jeType.getAsJsonObject().get("suffixURL").getAsString();
                                uris.put(node, Utils.buildURL(baseURL, port, suffix));
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uris;
    }

    @Override
    public Map<String, URL> getAllNodesByServiceAndType(String service, String type) throws URISyntaxException, MalformedURLException {
        Map<String,URL> uris = new HashMap<>();
        try {
            Map<String,String> queryParams = new HashMap<>();
            queryParams.put("serviceName",service);
            queryParams.put("typeName",type);
            JsonElement jeResponse = Utils.doRequest(new URL(serviceDiscoveryHost+"/service-discovery/service/type"), Connection.Method.GET,null,null,queryParams, true);
            if (jeResponse!=null) {
                JsonArray jResponseArray = jeResponse.getAsJsonArray();
                for (JsonElement jeNode : jResponseArray) {
                    String node = jeNode.getAsJsonObject().get("name").getAsString();
                    for (JsonElement jeService : jeNode.getAsJsonObject().get("services").getAsJsonArray()) {
                        String baseURL = jeService.getAsJsonObject().get("baseURL").getAsString();
                        String port = jeService.getAsJsonObject().get("port").getAsString();
                        for (JsonElement jeType : jeService.getAsJsonObject().get("types").getAsJsonArray()) {
                            String suffix = jeType.getAsJsonObject().get("suffixURL").getAsString();
                            uris.put(node,Utils.buildURL(baseURL,port,suffix));
                        }
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uris;
    }

}
