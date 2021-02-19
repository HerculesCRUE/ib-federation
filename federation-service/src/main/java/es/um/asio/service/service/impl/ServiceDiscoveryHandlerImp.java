package es.um.asio.service.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.um.asio.service.util.Utils;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

@Service
public class ServiceDiscoveryHandlerImp {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryHandlerImp.class);
    private static Timer timer = new Timer(false);

    @Value("${app.host}")
    private String host;

    @Value("${app.port}")
    private String port;

    @Value("${app.node}")
    private String node;

    @Value("${app.name}")
    private String name;

    @Value("${app.healthEndpoint}")
    private String healthEndpoint;

    @Value("${app.tripleStores}")
    private String[] tripleStores;

    @Value("${app.service-discovery-host}")
    private String serviceDiscoveryHost;


    public static void schedule() {
/*        Calendar due = Calendar.getInstance();
        due.set(Calendar.MINUTE,10);
        if( due.before(Calendar.getInstance()) ) {
            due.add(Calendar.HOUR, 1);
        }

        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                System.out.println("due");
                registerService();
            }

        }, due.getTime());*/
    }

    public boolean registerService() {
        Map<String,String> queryParam = new HashMap<>();
        queryParam.put("host",host);
        queryParam.put("port",port);
        queryParam.put("nodeName",node);
        queryParam.put("serviceName",name);
        queryParam.put("healthEndpoint",healthEndpoint);
        boolean isDone = false;
        try {
            JsonElement jResponse = Utils.doRequest(new URL(serviceDiscoveryHost+"/service-discovery/service"), Connection.Method.POST,null, null, queryParam,true);
            isDone = (jResponse!=null && jResponse.isJsonObject() && jResponse.getAsJsonObject().size()>0);
            if (isDone) {
                // Para cada triplestore definido
                for (String tripleStore : tripleStores) {
                    Map<String,String> queryParamsType = new HashMap<>();
                    queryParamsType.put("nodeName",node);
                    queryParamsType.put("serviceName",name);
                    queryParamsType.put("suffixURL",String.format("/endpoint-sparql/%s",tripleStore));
                    queryParamsType.put("typeName",tripleStore);
                    JsonElement jTripleResponse = Utils.doRequest(new URL(serviceDiscoveryHost+"/service-discovery/type"), Connection.Method.POST,null, null, queryParamsType,true);
                    isDone = isDone && (jTripleResponse!=null && jTripleResponse.isJsonObject() && jTripleResponse.getAsJsonObject().size()>0);
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return isDone;
    }

}
