package es.um.asio.service.service;

import com.google.gson.JsonObject;
import org.jsoup.Connection;
import org.springframework.http.MediaType;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface HttpRequestHelper {

    JsonObject doQueryRequest(URL url, String query, Connection.Method method, Map<String,String> headers, int timeout)  throws IOException;

}
