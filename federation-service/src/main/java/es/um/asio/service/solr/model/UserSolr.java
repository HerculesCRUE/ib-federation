package es.um.asio.service.solr.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.izertis.libraries.solr.model.IndexableDocument;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User solr document definition.
 */
@Getter
@Setter
@SolrDocument(collection = "#{@solrPropertiesHelper.getSolrProperties().getCollectionName()}")
public class UserSolr extends IndexableDocument implements Serializable {
    /**
     * Version ID.
     */
    private static final long serialVersionUID = 3458672290471830753L;

    /**
     * The id.
     */
    @Id
    @Indexed(name = Fields.ID)
    private String id;

    /**
     * User real name.
     */
    @Indexed(name = Fields.NAME)
    private String name;

    /**
     * Email.
     */
    @Indexed(name = Fields.EMAIL)
    private String email;

    /**
     * Flag that indicates whether user is enabled or not.
     */
    @Indexed(name = Fields.ENABLED)
    private boolean enabled;

    /**
     * Flag that indicates whether credentials are expired or not.
     */
    @Indexed(name = Fields.CREDENTIALS_NON_EXPIRED)
    private boolean credentialsNonExpired;

    /**
     * Flag that indicates whether account are expired or not.
     */
    @Indexed(name = Fields.ACCOUNT_NON_EXPIRED)
    private boolean accountNonExpired;

    /**
     * Flag that indicates whether account is expired or not.
     */
    @Indexed(name = Fields.ACCOUNT_NON_LOCKED)
    private boolean accountNonLocked;

    /**
     * User name
     */
    @Indexed(name = Fields.USERNAME)
    private String username;

    /**
     * Field names constants.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Fields {

        /**
         * The id field in Solr.
         */
        public static final String ID = "id";

        /**
         * The name field in Solr.
         */
        public static final String NAME = "name_t";

        /**
         * The email field in Solr.
         */
        public static final String EMAIL = "email_t";

        /**
         * The enabled field in Solr.
         */
        public static final String ENABLED = "enabled_b";

        /**
         * The credentials non expired field in Solr.
         */
        public static final String CREDENTIALS_NON_EXPIRED = "credentialsNonExpired_b";

        /**
         * The account non expired field in Solr.
         */
        public static final String ACCOUNT_NON_EXPIRED = "accountNonExpired_b";

        /**
         * The account non locked field in Solr.
         */
        public static final String ACCOUNT_NON_LOCKED = "accountNonLocked_b";

        /**
         * The username field in Solr.
         */
        public static final String USERNAME = "username_t";
    }
}
