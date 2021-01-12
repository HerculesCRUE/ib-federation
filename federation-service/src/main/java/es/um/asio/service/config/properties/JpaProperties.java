package es.um.asio.service.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/**
 * JPA related properties.
 */
@Validated
@Getter
@Setter
public class JpaProperties {
    /**
     * Additional native properties to set on the JPA provider.
     */
    private Map<String, String> properties = new HashMap<>();

    /**
     * Whether to initialize the schema on startup.
     */
    private boolean generateDdl = false;

    /**
     * Whether to enable logging of SQL statements.
     */
    private boolean showSql = false;

    /**
     * JPA dialect for the database.
     */
    private String dialect;
}
