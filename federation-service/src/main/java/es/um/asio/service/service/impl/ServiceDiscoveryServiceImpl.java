package es.um.asio.service.service.impl;

import es.um.asio.service.config.DataSourceRepository;
import es.um.asio.service.service.ServiceDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceDiscoveryServiceImpl implements ServiceDiscoveryService {

    @Autowired
    DataSourceRepository dataSource;

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
        List<DataSourceRepository.Node> nodesList = new ArrayList<>();
        for (String node: nodes) {
            DataSourceRepository.Node n = dataSource.getNodeByName(node);
            if (n!=null)
                nodesList.add(n);
        }
        for (DataSourceRepository.Node n: nodesList) {
            DataSourceRepository.Node.Service s = n.getServiceByName(service);
            DataSourceRepository.Node.Service.Type t;
            if (service!=null) {
                t = s.getTypeByName(type);
                StringBuffer buffer = new StringBuffer();
                buffer.append(s.buildBaseURL());
                if (t!=null && t.getSuffixURL()!=null) {
                    buffer.append(t.getSuffixURL());
                }
                String str = buffer.toString();
                uris.put(n.getNodeName(),new URL(buffer.toString()));
            }
        }
        return uris;
    }

    @Override
    public Map<String, URL> getAllNodesByServiceAndType(String service, String type) throws URISyntaxException, MalformedURLException {
        Map<String,URL> uris = new HashMap<>();
        for (DataSourceRepository.Node n: dataSource.getNodes()) {
            DataSourceRepository.Node.Service s = n.getServiceByName(service);
            DataSourceRepository.Node.Service.Type t;
            if (service!=null) {
                t = s.getTypeByName(type);
                StringBuffer buffer = new StringBuffer();
                buffer.append(s.buildBaseURL());
                if (t!=null && t.getSuffixURL()!=null) {
                    buffer.append(t.getSuffixURL());
                }
                String str = buffer.toString();
                uris.put(n.getNodeName(),new URL(buffer.toString()));
            }
        }
        return uris;
    }
}
