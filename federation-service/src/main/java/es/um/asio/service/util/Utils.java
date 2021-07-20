package es.um.asio.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import es.um.asio.service.repository.SparqlProxyHandler;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.XML;
import org.jsoup.Connection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.util.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static JsonElement doRequest(URL url, Connection.Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> queryParams,boolean encode) throws IOException {
        if (queryParams!=null) {
            url = buildQueryParams(url,queryParams, encode);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method.toString());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
        JsonReader r = null;
        JsonElement jResponse = new JsonObject();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            r = new JsonReader(reader);
            jResponse = new Gson().fromJson(r,JsonElement.class);
        } finally {
            if (r != null){
                r.close();
            }
        }
 /*       try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
                    con.disconnect();
        JsonElement jResponse = new Gson().fromJson(response.toString(), JsonElement.class);
        }*/
        return jResponse;
    }


    public static JsonElement doRequestXML(URL url, Connection.Method method, Map<String,String> headers, Map<String,String> params, Map<String,String> queryParams,boolean encode) throws IOException {
        if (queryParams!=null) {
            url = buildQueryParams(url,queryParams, encode);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method.toString());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
        con.disconnect();
        JsonElement jResponse = castXMLtoJsonElement(response.toString());
        return jResponse;
    }

    private static JsonElement castXMLtoJsonElement(String xml) {
        try {
            JsonElement jObject = new Gson().fromJson(XML.toJSONObject(xml).toString(),JsonElement.class);
            return jObject;
        } catch (JSONException je) {
            return null;
        }
    }

    private static URL buildQueryParams(URL baseURL, Map<String,String> queryParams,boolean encode) throws MalformedURLException, UnsupportedEncodingException {
        StringBuffer base = new StringBuffer();
        base.append(baseURL.toString());
        if (queryParams!=null && queryParams.size()>0) {
            base.append("?");
            List<String> qpList = new ArrayList<>();
            for (Map.Entry<String, String> qpEntry : queryParams.entrySet()) {
                if (encode)
                    qpList.add(qpEntry.getKey()+"="+ new URLEncoder().encode(qpEntry.getValue(), Charset.defaultCharset()));
                else
                    qpList.add(qpEntry.getKey()+"="+qpEntry.getValue());
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

    public static boolean isPrimitive(Object o) {
        if (o == null)
            return true;
        return ClassUtils.isPrimitiveOrWrapper(o.getClass()) || o instanceof String;
    }

    public static boolean isValidString(String s) {
        return s != null && !s.equals("");
    }

    public static String getPublicIP() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            logger.info("Find IP: {}",ip);
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Integer extractLimitInSPARQL(String query) {
        String pattern = "(?i)limit\\s+(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(query);

        while (m.find()) {
            return Integer.valueOf(m.group(1));
        }
        return null;
    }

    public static String stringNormalized(String str) {
        return StringUtils.stripAccents(str);
    }


}
