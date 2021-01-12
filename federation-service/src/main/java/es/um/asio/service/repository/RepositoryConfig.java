package es.um.asio.service.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Service Spring configuration.
 */
@Configuration
@ComponentScan(basePackageClasses = UserRepository.class)
public class RepositoryConfig {

}
