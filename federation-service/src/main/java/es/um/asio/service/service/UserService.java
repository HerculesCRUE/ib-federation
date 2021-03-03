package es.um.asio.service.service;

import es.um.asio.audit.abstractions.service.DeleteService;
import es.um.asio.audit.abstractions.service.QueryService;
import es.um.asio.audit.abstractions.service.SaveService;
import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.model.User;

/**
 * Service to handle {@link User} entity related operations
 */
public interface UserService
        extends QueryService<User, String, UserFilter>, SaveService<User>, DeleteService<User, String> {
    /**
     * Unlocks a user account.
     *
     * @param identifier
     *            The identifier
     */
    void undelete(final String identifier);
}
