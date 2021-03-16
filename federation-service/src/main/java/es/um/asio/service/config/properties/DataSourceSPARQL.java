package es.um.asio.service.config.properties;

import es.um.asio.service.util.Utils;
import lombok.*;
import org.apache.catalina.util.URLEncoder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("datasource-sparql") // prefix app, find app.* values
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataSourceSPARQL {

    private String nodeName;
    private List<Connector> connectors = new ArrayList<>();

    public Connector getConnectorByType(String type) {
        for (Connector connector: connectors) {
            if (connector.getType().equalsIgnoreCase(type))
                return connector;
        }
        return null;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Connector {

        private String type;
        private String host;
        private String port;
        private String suffixURL;
        private String queryParamName;

        public String buildQueryURL(String q,boolean encode) {
            StringBuffer buf = new StringBuffer();
            if (host.endsWith("/")) {
                buf.append(host, 0, host.length()-1);
            } else {
                buf.append(host);
            }
            buf.append(":"+port);

            if (!suffixURL.startsWith("/")) {
                suffixURL = "/"+suffixURL;
            }
            if (suffixURL.endsWith("/")) {
                buf.append(suffixURL, 0, suffixURL.length()-1);
            } else {
                buf.append(suffixURL);
            }
            if (Utils.isValidString(queryParamName) && Utils.isValidString(q)) {
                buf.append("?"+queryParamName+"=");
                if (encode)
                    buf.append(new URLEncoder().encode(q, Charset.defaultCharset()));
                else
                    buf.append(q);
            }
            return buf.toString();
        }

        public String buildQueryURL(boolean encode) {
            StringBuffer buf = new StringBuffer();
            if (host.endsWith("/")) {
                buf.append(host, 0, host.length()-1);
            } else {
                buf.append(host);
            }
            buf.append(":"+port);

            if (!suffixURL.startsWith("/")) {
                suffixURL = "/"+suffixURL;
            }
            if (suffixURL.endsWith("/")) {
                buf.append(suffixURL, 0, suffixURL.length()-1);
            } else {
                buf.append(suffixURL);
            }
            return buf.toString();
        }

    }

}

