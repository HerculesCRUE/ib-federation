package es.um.asio.service.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import es.um.asio.audit.abstractions.search.PageImplHelper;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.model.User;
import es.um.asio.service.repository.UserRepository;
import es.um.asio.service.service.UserService;
import es.um.asio.service.service.impl.UserServiceImpl;

@RunWith(SpringRunner.class)
public class UserServiceTest {
    /**
     * User service
     */
    @Autowired
    private UserService service;

    /**
     * User repository mock bean.
     */
    @MockBean
    private UserRepository repository;

    @TestConfiguration
    static class UserServiceTestConfiguration {
        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }
    }

    @Before
    public void setUp() {
        // Sample data
        final User user1 = new User();
        user1.setId("1");
        user1.setUsername("user1");

        final User user2 = new User();
        user2.setId("2");
        user2.setUsername("user2");
        
        // Mock save
        Mockito.when(this.repository.save(any())).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            user.setId("1");
            return user;
        });
        
        // Mock saveAll
        Mockito.when(this.repository.saveAll(any())).thenAnswer(invocation -> {
            final Iterable<User> users = invocation.getArgument(0);
            
            
            for(User user : users) {
                user.setId(user.getName());
            }
            
            return users;
        });

        // Mock findById
        Mockito.when(this.repository.findById(any())).thenAnswer(invocation -> {
            final String id = invocation.getArgument(0);
            if ("1".equals(id)) {
                return Optional.of(user1);
            } else if ("2".equals(id)) {
                return Optional.of(user2);
            } else {
                return Optional.empty();
            }
        });

        // Mock findByUsername
        Mockito.when(this.repository.findByUsername(any())).thenAnswer(invocation -> {
            final String username = invocation.getArgument(0);
            if ("user1".equals(username)) {
                return Optional.of(user1);
            } else if ("user2".equals(username)) {
                return Optional.of(user2);
            } else {
                return Optional.empty();
            }
        });

        // Mock findAll - page
        Mockito.when(this.repository.findAll(any(UserFilter.class), any(Pageable.class))).thenAnswer(invocation -> {
            final List<User> elements = Lists.newArrayList(user1, user2);
            final Pageable page = invocation.getArgument(1);
            return new PageImplHelper<User>(elements, page, elements.size());
        });
        
        // Mock findAll
        Mockito.when(this.repository.findAll()).thenAnswer(invocation -> {
            return Lists.newArrayList(user1, user2);
        });
    }

    @Test
    public void whenSaveNewUser_thenUserHasId() {
        final User user = new User();
        user.setUsername("test");
        final User newUser = this.service.save(user);

        assertThat(newUser.getId()).isEqualTo("1");
        assertThat(newUser.getUsername()).isEqualTo(user.getUsername());
    }
    
    @Test
    public void whenSaveListOfUsers_thenReturnList() {
        final User user = new User();
        user.setUsername("test");
        final User user2 = new User();
        user.setUsername("test2");
        final List<User> users = this.service.save(Lists.newArrayList(user, user2));
        
        assertThat(users).isNotEmpty();
    }
    
    @Test
    public void whenUpdateuser_thenUserHasId() {
        final User user = new User();
        user.setUsername("test");
        final User newUser = this.service.save(user);

        assertThat(newUser.getId()).isEqualTo("1");
        assertThat(newUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void whenLoadByUsernameNotFound_thenThrowException() {
        ((UserDetailsService) this.service).loadUserByUsername("user3");
    }

    @Test
    public void whenFindById_thenReturnUser() {
        final Optional<User> user1 = this.service.find("1");
        assertThat(user1).isNotEmpty();
        assertThat(user1.get().getId()).isEqualTo("1");

        final Optional<User> user2 = this.service.find("2");
        assertThat(user2).isNotEmpty();
        assertThat(user2.get().getId()).isEqualTo("2");

        final Optional<User> user3 = this.service.find("3");
        assertThat(user3).isEmpty();
    }

    @Test
    public void whenFindPaginated_thenReturnPage() {
        final UserFilter filter = new UserFilter();
        final Page<User> page = this.service.findPaginated(filter, PageRequest.of(0, 10));
        assertThat(page.getNumberOfElements()).isNotEqualTo(0);
    }
    
    @Test
    public void whenFindAll_thenReturnList() {
        assertThat(service.findAll()).isNotEmpty();
    }
    
    @Test
    public void whenLoadByUsername_thenReturmUser() {
        final UserDetails user1 = ((UserDetailsService) this.service).loadUserByUsername("user1");
        assertThat(user1.getUsername()).isEqualTo("user1");

        final UserDetails user2 = ((UserDetailsService) this.service).loadUserByUsername("user2");
        assertThat(user2.getUsername()).isEqualTo("user2");
    }
}
