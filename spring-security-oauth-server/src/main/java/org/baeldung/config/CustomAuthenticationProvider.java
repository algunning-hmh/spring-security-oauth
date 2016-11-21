package org.baeldung.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.ArrayList;

/**
 * Created by gunninga on 18/11/2016.
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

       // if (shouldAuthenticateAgainstThirdPartySystem()) {
            // use the credentials and authenticate against the third-party system
            return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
       // } else {
       //     return null;
       // }


        /*
        * Option 1: Add a new CompositeTokenGranter
        * Option 2: Subclass UsernamePasswordAuthenticationToken and return it here.
        *
        * */
//http://www.programcreek.com/java-api-examples/index.php?api=org.springframework.security.authentication.UsernamePasswordAuthenticationToken

    /*
    * THink i need to set something up in DefaultToken services
    *
        public class DefaultTokenServices
        extends Object
        implements AuthorizationServerTokenServices, ResourceServerTokenServices, ConsumerTokenServices, InitializingBean

        Base implementation for token services using random UUID values for the access token and refresh token values. The main extension point for customizations is the TokenEnhancer which will be called after the access and refresh tokens have been generated but before they are stored.
        Persistence is delegated to a TokenStore implementation and customization of the access token to a TokenEnhancer.

        Friday leaving note:
        Looks like tokenEnhancer does everything i need

        Left to do Monday:
        POC out the /authorise code flow to see if openid conenct will work for use
        using TokenEnhancer etc...


    * */

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}