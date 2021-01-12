package es.um.asio.service.proxy;

import com.izertis.abstractions.service.DeleteService;
import com.izertis.abstractions.service.QueryService;
import com.izertis.abstractions.service.SaveService;
import es.um.asio.service.dto.UserDto;
import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.model.User;

/**
 * Proxy service for {@link User}. Performs DTO conversion and permission checks.
 */
public interface UserProxy
        extends QueryService<UserDto, String, UserFilter>, SaveService<UserDto>, DeleteService<UserDto, String> {
    /**
     * Unlocks a user account.
     *
     * @param identifier
     *            The identifier
     */
    void undelete(final String identifier);
}
