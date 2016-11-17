package org.baeldung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public void globalUserDetails(final AuthenticationManagerBuilder auth) throws Exception {

        //TODO: see if can read this from database?
        //TODO: Add to a repository since it's largely working now so don't mess it up.
/*        auth
                .jdbcAuthentication()
                .dataSource(restDataSource)
                .usersByUsernameQuery(getUserQuery())
                .authoritiesByUsernameQuery(getAuthoritiesQuery());
               //TODO: see
               https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
               http://stackoverflow.com/questions/22749767/using-jdbcauthentication-in-spring-security-with-hibernate
*/

        auth.inMemoryAuthentication()
                .withUser("john")
                .password("123")
                .roles("USER")

                .and().withUser("tom")
                .password("111")
                .roles("ADMIN");
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()

                // TODO: Not necessary for password flow, only needed for implicit flow
                .and()
                .formLogin().permitAll();
    }

}
