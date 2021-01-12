package es.um.asio.back.solr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.solr", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan
public class SolrBackConfig {

}
