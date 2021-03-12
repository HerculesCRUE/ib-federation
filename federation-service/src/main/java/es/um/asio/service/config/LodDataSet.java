package es.um.asio.service.config;

import es.um.asio.service.model.TripleObjectSimplified;
import lombok.*;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties("lod-data-sources") // prefix app, find app.* values
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LodDataSet implements Cloneable{

    private List<Dataset> datasets = new ArrayList<>();

    public Optional<Dataset> getDatasetByName(String name) {
        Dataset dataset = null;
        for (Dataset ds : datasets) {
            if (ds.getName().equalsIgnoreCase(name)) {
                dataset = ds;
                break;
            }
        }
        return Optional.ofNullable(dataset);
    }

    public List<String> getDatasetNames() {
        Set<String> datasetsNames = new HashSet<>();
        for (Dataset dataset : datasets) {
            datasetsNames.add(dataset.getName());
        }
        return new ArrayList<>(datasetsNames);
    }

    public List<String> getClassNames() {
        Set<String> classes = new HashSet<>();
        for (Dataset dataset : datasets) {
            for (Dataset.Connection connection : dataset.getConnections()) {
                for (Dataset.Connection.Mapping mapping: connection.getMappings()) {
                    for (Dataset.Connection.Mapping.LocalClass localClass : mapping.getLocalClasses()) {
                        classes.add(localClass.getName());
                    }
                }
            }
        }
        return new ArrayList<>(classes);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Dataset implements Cloneable{

        private String name;
        private List<Connection> connections = new ArrayList<>();

        public Optional<Connection> getDefaultConnection() {
            Connection connection = (connections.size()>0)?connections.get(0):null;
            for (Connection con : connections) {
                if (con.isDef()) {
                    connection = con;
                    break;
                }
            }
            return Optional.ofNullable(connection);
        }

        public Dataset getPrunedDatasetByClassName(String className) throws CloneNotSupportedException {
            Dataset dataset = (Dataset) this.clone();
            int indexConnection = 0;
            for (Connection connection : connections) {
                boolean foundInConnection = false;
                int indexMapping = 0;
                for (Connection.Mapping mapping : connection.getMappings()) {
                    boolean foundInMapping = false;
                    int indexLocalClass = 0;
                    for (Connection.Mapping.LocalClass localClass :mapping.localClasses) {
                        if (localClass.getName().equals(className)) {
                            foundInConnection = true;
                            foundInMapping = true;
                            indexLocalClass++;
                        } else {
                            dataset.getConnections().get(indexConnection).getMappings().get(indexMapping).getLocalClasses().remove(indexLocalClass);
                        }
                    }
                    if (!foundInMapping) {
                        dataset.getConnections().get(indexConnection).getMappings().remove(indexMapping);
                    } else {
                        indexMapping++;
                    }
                }
                if (!foundInConnection) {
                    dataset.getConnections().remove(indexConnection);
                } else {
                    indexConnection ++;
                }

            }
            return dataset;
        }

        public Dataset getPrunedDatasetSortedFilteredByConnectionType(List<Connection.ConnectionType> types) throws CloneNotSupportedException {
            Dataset dataset = (Dataset) this.clone();
            dataset.connections = connections.stream().filter(c -> types.contains(c.getConnectionType())).collect(Collectors.toList());
            dataset.connections = sortedConnections();
            return dataset;
        }

        public List<Connection> sortedConnections() {
            List<Connection> connectionsAux = new ArrayList<>();
            for (Connection con:connections) {
                if (con.isDef()) {
                    connectionsAux.add(con);
                }
            }
            for (Connection con:connections) {
                if (!connectionsAux.contains(con)) {
                    connectionsAux.add(con);
                }
            }
            return connectionsAux;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            Dataset ds = new Dataset();
            ds.name = getName();
            ds.connections = new ArrayList();
            for (Connection con : connections) {
                ds.connections.add((Connection) con.clone());
            }
            return ds;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        public static class Connection implements Cloneable{
            private ConnectionType connectionType;
            private boolean def;
            private String baseURL;
            private String user;
            private String password;
            private String apiKey;
            private List<Mapping> mappings = new ArrayList<>();

            public void setConnectionType(String type) {
                this.connectionType = ConnectionType.valueOf(type);
            }

            public Connection getPrunedConnectionByTripleObjectSimplified(TripleObjectSimplified to) throws CloneNotSupportedException {
                Connection connection = (Connection) this.clone();

                int indexMapping = 0;
                for (Mapping mapping : this.mappings) {
                    boolean foundInMapping = false;
                    int indexLocalClass = 0;
                    for (Mapping.LocalClass localClass : mapping.getLocalClasses()) {
                        Object value = to.getAttributeValue(to.getAttributes(),localClass.getAttributes());
                        if (value!=null && (value instanceof String && Utils.isValidString(value.toString()))) {
                            foundInMapping = true;
                            indexLocalClass++;
                        } else {
                            connection.getMappings().get(indexMapping).getLocalClasses().remove(indexLocalClass);
                        }
                    }
                    if (!foundInMapping) {
                        connection.getMappings().remove(indexMapping);
                    } else {
                        indexMapping ++;
                    }
                }
                return connection;
            }

            public List<Mapping> getSortedMappings() {
                Comparator<Mapping> comparator = (m1,m2) -> Integer.compare(m1.getOrder(),m2.getOrder());
                Collections.sort(mappings,comparator);
                return mappings;
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                Connection connection = new Connection();
                connection.connectionType = getConnectionType();
                connection.def = isDef();
                connection.baseURL = getBaseURL();
                connection.user = getUser();
                connection.password = getPassword();
                connection.apiKey = getApiKey();
                connection.mappings = new ArrayList<>();
                for ( Mapping m : getMappings()) {
                    connection.mappings.add((Mapping) m.clone());
                }
                return connection;
            }

            @AllArgsConstructor
            @Getter
            public static enum ConnectionType implements Cloneable{
                API,SPARQL
            }

            @AllArgsConstructor
            @NoArgsConstructor
            @Getter
            @Setter
            public static class Mapping implements Cloneable{
                private String remoteName;
                private String remoteAttribute;
                private String suffixURL;
                private String query;
                private boolean identifier;
                private boolean removeStopWords = false;
                private int order;
                private ParamType paramType;
                private List<LocalClass> localClasses = new ArrayList<>();

                public void setParamType(String paramType) {
                    this.paramType = ParamType.valueOf(paramType);
                }

                public Optional<LocalClass> getLocalClassByName(String localClassName) {
                    LocalClass localClass = null;
                    for (LocalClass lc : localClasses) {
                        if (lc.getName().equalsIgnoreCase(localClassName)) {
                            localClass = lc;
                            break;
                        }
                    }
                    return Optional.ofNullable(localClass);
                }


                @Override
                public Object clone() throws CloneNotSupportedException {
                    Mapping mapping = new Mapping();
                    mapping.remoteName = getRemoteName();
                    mapping.remoteAttribute = getRemoteAttribute();
                    mapping.suffixURL = getSuffixURL();
                    mapping.query = getQuery();
                    mapping.identifier = identifier;
                    mapping.removeStopWords = removeStopWords;
                    mapping.order = order;
                    mapping.paramType = paramType;
                    mapping.localClasses = new ArrayList<>();
                    for ( LocalClass lc : getLocalClasses()) {
                        mapping.localClasses.add((LocalClass) lc.clone());
                    }
                    return mapping;
                }

                @AllArgsConstructor
                @Getter
                public static enum ParamType implements Cloneable{
                    URI,QUERY,SPARQL
                }

                @AllArgsConstructor
                @NoArgsConstructor
                @Getter
                @Setter
                public static class LocalClass implements Cloneable{
                    private String name;
                    private List<String> attributes = new ArrayList<>();
                    private List<Mapper> mappers = new ArrayList<>();

                    @Override
                    public Object clone() throws CloneNotSupportedException {
                        LocalClass localClass = new LocalClass();
                        localClass.name = getName();
                        localClass.attributes = new ArrayList<>();
                        localClass.mappers = new ArrayList<>();
                        for (String att:attributes) {
                            localClass.attributes.add(String.valueOf(att));
                        }
                        for (Mapper ma:mappers) {
                            localClass.mappers.add(ma);
                        }
                        return localClass;
                    }

                    @AllArgsConstructor
                    @NoArgsConstructor
                    @Getter
                    @Setter
                    public static class Mapper implements Cloneable {
                        private String remoteAttribute;
                        private String localAttribute;

                        @Override
                        public Object clone() throws CloneNotSupportedException {
                            Mapper mapper = new Mapper();
                            mapper.remoteAttribute = String.valueOf(getRemoteAttribute());
                            mapper.localAttribute = String.valueOf(getLocalAttribute());
                            return mapper;
                        }
                    }


                }

            }
        }

    }
}
