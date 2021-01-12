package es.um.asio.back.solr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.um.asio.service.model.Role;
import es.um.asio.service.model.User;
import com.izertis.libraries.solr.service.IndexService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Indexation controller.
 */
@RestController
@RequestMapping(IndexController.Mappings.BASE)
@Secured(Role.ADMINISTRATOR_ROLE)
public class IndexController {

    /**
     * Index service.
     */
    @Autowired
    private IndexService indexService;

    /**
     * Reindex all users.
     */
    @GetMapping(IndexController.Mappings.REINDEX_USERS)
    public void reindexUsers() {
        this.indexService.reIndex(User.class);
    }

    /**
     * Reindex all documents.
     */
    @GetMapping(IndexController.Mappings.REINDEX_FULL)
    public void reindexFull() {
        this.indexService.reIndex();
    }

    /**
     * Mappgins.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Mappings {
        /**
         * Controller request mapping.
         */
        protected static final String BASE = "/index";

        /**
         * Mapping for list.
         */
        protected static final String REINDEX_USERS = "/reindexUsers";

        /**
         * Mapping for search.
         */
        protected static final String REINDEX_FULL = "/reindexFull";
    }
}
