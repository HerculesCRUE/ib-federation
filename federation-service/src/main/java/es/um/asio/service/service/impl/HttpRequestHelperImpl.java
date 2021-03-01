package es.um.asio.service.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.um.asio.service.service.HttpRequestHelper;
import org.jsoup.Connection;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
public class HttpRequestHelperImpl implements HttpRequestHelper {

    @Override
    public JsonObject doQueryRequest(URL url, String query, Connection.Method method, Map<String,String> headers, int timeout)  throws IOException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setConnectTimeout(timeout);
        con.setRequestMethod(method.toString());
        if (headers!=null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                con.setRequestProperty(headerEntry.getKey(),headerEntry.getValue());
            }
        }
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = query.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        con.disconnect();
        JsonObject jResponse = new Gson().fromJson(response.toString(), JsonObject.class);
        return jResponse;
    }

}
