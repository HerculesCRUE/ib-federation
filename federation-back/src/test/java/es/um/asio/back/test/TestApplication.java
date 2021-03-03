package es.um.asio.back.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
// import org.springframework.security.config.annotation.web.builders.WebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.um.asio.service.mapper.MapperConfig;

@SpringBootApplication
@EnableAutoConfiguration
@Import(MapperConfig.class)
public class TestApplication /*extends WebSecurityConfigurerAdapter*/ {
    /**
     * Main method for embedded deployment.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
    
    /**
     * Creates the password encoder BCrypt.
     *
     * @return The password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.
     * springframework.security.config.annotation.web.builders.WebSecurity)
     */
/*    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }*/
}
