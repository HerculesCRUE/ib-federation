package es.um.asio.back.test.controller.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izertis.abstractions.exception.NoSuchEntityException;
import com.izertis.abstractions.search.PageImplHelper;
import es.um.asio.back.controller.security.UserController;
import es.um.asio.service.dto.UserDto;
import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.proxy.UserProxy;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    /**
     * MVC test support
     */
    @Autowired
    private MockMvc mvc;

    /**
     * User service
     */
    @MockBean
    private UserProxy proxy;

    /**
     * JSON Object mapper
     */
    @Autowired
    private ObjectMapper objectMapper;
    
    @TestConfiguration
    static class UserProxyTestConfiguration {
        @Bean
        public UserController userController() {
            return new UserController();
        }
    }

    @Before
    public void setUp() throws NoSuchEntityException {
        // Mock data
        final UserDto user1 = new UserDto();
        user1.setId("1");
        user1.setUsername("user1");

        final UserDto user2 = new UserDto();
        user2.setId("2");
        user2.setUsername("user2");

        // Mock findAll
        Mockito.when(this.proxy.findAll()).thenAnswer(invocation -> {
            return Lists.newArrayList(user1, user2);
        });

        // Mock findPaginated
        Mockito.when(this.proxy.findPaginated(any(UserFilter.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    return new PageImplHelper<UserDto>(Lists.newArrayList(user1, user2));
                });

        // Mock find
        Mockito.when(this.proxy.find(any())).thenAnswer(invocation -> {
            final String id = invocation.getArgument(0);
            if ("1".equals(id)) {
                return Optional.of(user1);
            } else if ("2".equals(id)) {
                return Optional.of(user2);
            } else {
                return Optional.empty();
            }
        });

        // Mock delete
        Mockito.doNothing().when(this.proxy).delete(any(String.class));

        // Mock undelete
        Mockito.doNothing().when(this.proxy).undelete(any(String.class));

        // Mock save
        Mockito.when(this.proxy.save(any(UserDto.class))).thenAnswer(invocation -> {
            final UserDto user = invocation.getArgument(0);
            user.setId("1");
            return user;
        });

        // Mock update
        Mockito.when(this.proxy.update(any(UserDto.class))).thenAnswer(invocation -> {

            final UserDto user = invocation.getArgument(0);

            if ("1".equals(user.getId()) || "2".equals(user.getId())) {
                return user;
            }

            throw new NoSuchEntityException(String.format("User with id %s does not exists", user.getId()));
        });
    }

    @Test
    public void whenGetUsers_ThenReturnJsonArray() throws Exception {

        // @formatter:off

        this.mvc.perform(get("/user/list").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].username", is("user1")))
            .andExpect(jsonPath("$[1].username", is("user2")));

        // @formatter:on

    }

    @Test
    public void whenSearchUsers_ThenReturnJsonObject() throws Exception {
        // @formatter:off

        this.mvc.perform(get("/user/search").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].username", is("user1")))
            .andExpect(jsonPath("$.content[1].username", is("user2")));

        // @formatter:on
    }

    @Test
    public void whenGetUser_ThenReturnJsonObject() throws Exception {
        // @formatter:off

        this.mvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user1")));

        this.mvc.perform(get("/user/2").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user2")));

        // @formatter:on
    }

    @Test
    public void whenGetUserAndNotFound_ThenStatus404() throws Exception {
        // @formatter:off

        this.mvc.perform(get("/user/3").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        // @formatter:on
    }

    @Test
    public void whenDisableUser_ThenStatus200() throws Exception {
        // @formatter:off

        this.mvc.perform(put("/user/1/disable").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());

        // @formatter:on
    }

    @Test
    public void whenEnableUser_ThenStatus200() throws Exception {
        // @formatter:off

        this.mvc.perform(put("/user/1/enable").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());

        // @formatter:on
    }

    @Test
    public void whenSaveUser_ThenReturnJsonObject() throws Exception {
        final UserDto user = new UserDto();
        user.setUsername("newuser");

        // @formatter:off

        this.mvc.perform(post("/user")
                .content(this.objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("1")))
            .andExpect(jsonPath("$.username", is(user.getUsername())));

        // @formatter:on
    }

    @Test
    public void whenUpdateUser_ThenReturnJsonObject() throws Exception {
        // @formatter:off

        final UserDto user = new UserDto();
        user.setId("1");
        user.setUsername("newuser");

        this.mvc.perform(put("/user")
                .content(this.objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(user.getId())))
            .andExpect(jsonPath("$.username", is(user.getUsername())));

        // @formatter:on
    }

    @Test
    public void whenUpdateUserAndNotExists_ThenStatus404() throws Exception {
        // @formatter:off

        final UserDto user = new UserDto();
        user.setId("3");
        user.setUsername("newuser");

        this.mvc.perform(put("/user")
                .content(this.objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        // @formatter:on
    }

}
