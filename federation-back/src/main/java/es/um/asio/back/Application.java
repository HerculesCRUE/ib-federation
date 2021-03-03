package es.um.asio.back;

import es.um.asio.service.service.impl.ServiceDiscoveryHandlerImp;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import es.um.asio.service.ServiceConfig;
import org.springframework.context.event.EventListener;

import java.util.Collections;

@SpringBootApplication
@EnableAutoConfiguration
@Import({ ServiceConfig.class })
@ComponentScan(excludeFilters = { @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "*..solr..*") })
public class Application {


    @Autowired
    ServiceDiscoveryHandlerImp serviceDiscoveryHandlerImp;

    /**
     * Main method for embedded deployment.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doAfterStartup() {
        serviceDiscoveryHandlerImp.registerService();
    }

/*
    @Bean
    InitializingBean registerService() {
        return () ->
                serviceDiscoveryHandlerImp.registerService();
    }
*/

}
