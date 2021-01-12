package es.um.asio.service.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("data-sources") // prefix app, find app.* values
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceRepository {

    private List<Node> nodes = new ArrayList<>();

    public Node getNodeByName(String nodeName) {
        for (Node node: nodes) {
            if (node.getNodeName().equalsIgnoreCase(nodeName))
                return node;
        }
        return null;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Node {

        private String nodeName;
        private List<Service> services = new ArrayList<>();

        public Service getServiceByName(String name) {
            for (Service s: services) {
                if (s.getName().trim().equalsIgnoreCase(name.trim())) {
                    return s;
                }
            }
            return null;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        public static class Service {
            private String name;
            private String baseUri;
            private String port;
            private String user;
            private String password;
            private List<Type> types = new ArrayList<>();

            public Type getTypeByName(String name) {
                for (Type t: types) {
                    if (t.getName().trim().equalsIgnoreCase(name.trim())) {
                        return t;
                    }
                }
                return null;
            }

            public String buildBaseURL() {
                if (port == null || port.equals("")) {
                    return baseUri;
                } else
                    return baseUri + ":" + port;
            }


            @AllArgsConstructor
            @NoArgsConstructor
            @Getter
            @Setter
            public static class Type {
                private String name;
                private String suffixURL;
                private String user;
                private String password;

            }

        }
    }
}
