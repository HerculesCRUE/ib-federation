package es.um.asio.service;

import org.springframework.context.annotation.*;

import es.um.asio.service.repository.RepositoryConfig;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service Spring configuration.
 */
@Configuration
@ComponentScan(excludeFilters = { @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "*..solr..*") })
@Import({RepositoryConfig.class})
public class ServiceConfig {

    /**
     * Creates the password encoder BCrypt.
     *
     * @return The password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
