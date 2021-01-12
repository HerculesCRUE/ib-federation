package es.um.asio.service.service;

import es.um.asio.service.config.DataSourceRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ServiceDiscoveryService {

    DataSourceRepository.Node getNode(String nodeName);

    DataSourceRepository.Node.Service getService(String nodeName, String service);

    Map<String, URL> getNodesByNameAndServiceAndType(List<String> nodes, String service, String type) throws URISyntaxException, MalformedURLException;

    Map<String, URL> getAllNodesByServiceAndType(String service, String type) throws URISyntaxException, MalformedURLException;
}
