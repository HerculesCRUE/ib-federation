package es.um.asio.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.jsoup.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

    public static JsonElement doRequest(URL url, Connection.Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> queryParams) throws IOException {
        if (queryParams!=null) {
            url = buildQueryParams(url,queryParams);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method.toString());
        if (headers!=null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                con.setRequestProperty(headerEntry.getKey(),headerEntry.getValue());
            }
        }
        if (params!=null) {
            for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                con.setRequestProperty(paramEntry.getKey(),paramEntry.getValue());
            }
        }
        con.setDoOutput(true);
        StringBuilder response;
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        JsonElement jResponse = new Gson().fromJson(response.toString(), JsonElement.class);
        return jResponse;
    }

    private static URL buildQueryParams(URL baseURL, Map<String,String> queryParams) throws MalformedURLException, UnsupportedEncodingException {
        StringBuffer base = new StringBuffer();
        base.append(baseURL.toString());
        if (queryParams!=null && queryParams.size()>0) {
            base.append("?");
            List<String> qpList = new ArrayList<>();
            for (Map.Entry<String, String> qpEntry : queryParams.entrySet()) {
                qpList.add(qpEntry.getKey()+"="+ URLEncoder.encode(qpEntry.getValue(), StandardCharsets.UTF_8.toString()));
            }
            base.append(String.join("&",qpList));
        }
        return new URL(base.toString());
    }

    public static  URL buildURL(String baseURL, String port, String suffix) throws MalformedURLException {
        StringBuffer sb = new StringBuffer();
        if (baseURL!=null && !baseURL.equals(""))
            sb.append(baseURL);
        if (port!=null && !port.equals(""))
            sb.append(":"+port);
        if (suffix!=null && !suffix.equals("")) {
            if (suffix.startsWith("/"))
                sb.append(suffix);
            else
                sb.append("/" + suffix);
        }
        return new URL(sb.toString());
    }
}
