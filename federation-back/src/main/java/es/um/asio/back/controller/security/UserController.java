package es.um.asio.back.controller.security;

import es.um.asio.audit.abstractions.exception.NoSuchEntityException;
import es.um.asio.service.dto.UserDto;
import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.mapper.UserMapper;
import es.um.asio.service.model.Role;
import es.um.asio.service.model.User;
import es.um.asio.service.proxy.UserProxy;
import es.um.asio.service.validation.group.Create;
import es.um.asio.service.validation.group.Update;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * User controller.
 */
@RestController
@RequestMapping(UserController.Mappings.BASE)
public class UserController {

    /**
     * Proxy service implementation for {@link User}.
     */
    @Autowired
    private UserProxy proxy;

    /**
     * DTO to entity mapper.
     */
    @Autowired
    private UserMapper mapper;

    /**
     * Gets the current user details.
     *
     * @param authentication
     *            the authentication
     * @return the current user details
     */
    @GetMapping
    public Object getCurrentUserDetails(final Authentication authentication) {
        if (authentication.getPrincipal() instanceof User) {
            return this.mapper.convertToDto((User) authentication.getPrincipal());
        } else {
            return authentication.getPrincipal();
        }

    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    @GetMapping(UserController.Mappings.LIST)
    public List<UserDto> getUsers() {
        return this.proxy.findAll();
    }

    /**
     * Search users.
     *
     * @param filter
     *            the user filter
     * @param pageable
     *            Pagination configuration
     * @return page containing results
     */
    @GetMapping(UserController.Mappings.SEARCH)
    public Page<UserDto> searchUsers(final UserFilter filter, final Pageable pageable) {
        return this.proxy.findPaginated(filter, pageable);
    }

    /**
     * Gets the user.
     *
     * @param id
     *            the user id
     * @return the user
     */
    @GetMapping(UserController.Mappings.GET)
    public UserDto getUser(@PathVariable("id") final String id) {
        return this.proxy.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Lock user account.
     *
     * @param id
     *            the user id
     */
    @Secured(Role.ADMINISTRATOR_ROLE)
    @PutMapping(UserController.Mappings.DISABLE)
    public void disable(@PathVariable("id") final String id) {
        this.proxy.delete(id);
    }

    /**
     * Unlock user account.
     *
     * @param userId
     *            the user id
     */
    @Secured(Role.ADMINISTRATOR_ROLE)
    @PutMapping(UserController.Mappings.ENABLE)
    public void enable(@PathVariable("id") final String id) {
        this.proxy.undelete(id);
    }

    /**
     * Save.
     *
     * @param userDto
     *            the user dto
     * @return the application user dto
     */
    @Secured(Role.ADMINISTRATOR_ROLE)
    @PostMapping
    public UserDto save(@RequestBody @Validated(Create.class) final UserDto userDto) {
        return this.proxy.save(userDto);
    }

    /**
     * Update.
     *
     * @param userDto
     *            the user dto
     * @return the application user dto
     */
    @Secured(Role.ADMINISTRATOR_ROLE)
    @PutMapping
    public UserDto update(@RequestBody @Validated(Update.class) final UserDto userDto) {
        try {
            return this.proxy.update(userDto);
        } catch (final NoSuchEntityException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Mappgins.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Mappings {
        /**
         * Controller request mapping.
         */
        protected static final String BASE = "/user";

        /**
         * Mapping for list.
         */
        protected static final String LIST = "/list";

        /**
         * Mapping for search.
         */
        protected static final String SEARCH = "/search";

        /**
         * Mapping for get.
         */
        protected static final String GET = "/{id}";

        /**
         * Mapping for disabling an user.
         */
        protected static final String DISABLE = GET + "/disable";

        /**
         * Mapping for enabling an user.
         */
        protected static final String ENABLE = GET + "/enable";
    }
}
