package org.baeldung.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;


@Configuration
@PropertySource({ "classpath:persistence.properties" })
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Value("classpath:schema.sql")
    private Resource schemaScript;

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        /*
         *  AuthorizationServerSecurityConfigurer: defines the security constraints on the token endpoint.
         */
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }


    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {// @formatter:off
        /*
         *  The ClientDetailsServiceConfigurer (a callback from your AuthorizationServerConfigurer)
         *  can be used to define an in-memory or JDBC implementation of the client details service.
         *
         *  Important attributes of a client are
         *  clientId: (required) the client id.
         *  secret: (required for trusted clients) the client secret, if any.
         *  scope: The scope to which the client is limited. If scope is undefined or empty (the default) the client is not limited by scope.
         *  authorizedGrantTypes: Grant types that are authorized for the client to use. Default value is empty.
         *  authorities: Authorities that are granted to the client (regular Spring Security authorities).
         *  */

        clients.jdbc(dataSource())

                // Sample Client with Implicit flow
                .withClient("sampleClientId")
                .authorizedGrantTypes("implicit")
				.scopes("read", "write", "foo", "bar")
                .accessTokenValiditySeconds(3600)
                .autoApprove(true)

                // Sample Client, Password flow, custom scope foo
                .and().withClient("fooClientIdPassword")
                .secret("secret")
				.authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .scopes("read", "write", "foo")
				.accessTokenValiditySeconds(3600) // 1 hour
				.refreshTokenValiditySeconds(2592000) // 30 days
                .autoApprove(true) // Auto approve to skip additional authorise prompt

                // Sample Client, Password flow, custom scope bar
				.and().withClient("barClientIdPassword")
                .secret("secret")
				.authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .scopes("read", "write", "bar")
				.accessTokenValiditySeconds(3600) // 1 hour
				.refreshTokenValiditySeconds(2592000) // 30 days
                .autoApprove(true)  // Auto approve to skip additional authorise prompt
		;
	} // @formatter:on

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        /*
         *   AuthorizationServerEndpointsConfigurer: defines the authorization and token endpoints and the token services.
         *
         *  Grant Types:
         *
         *  authenticationManager: By default all grant types are supported except password,
         *                         password grants are switched on by injecting an AuthenticationManager.
         *
         *  userDetailsService: if you inject a UserDetailsService or if one is configured globally anyway
         *                      (e.g. in a GlobalAuthenticationManagerConfigurer) then a refresh token grant will contain
         *                      a check on the user details, to ensure that the account is still active
         *
         *  authorizationCodeServices: defines the authorization code services (instance of AuthorizationCodeServices)
         *                              for the auth code grant.
         *
         *  implicitGrantService: manages state during the implicit grant.
         *
         *  tokenGranter: the TokenGranter (taking full control of the granting and ignoring the other properties above)
         */

//		final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
//		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
 //       tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer()));
		endpoints
                // Enable JDBC Token Store
                .tokenStore(tokenStore())

                // Disable Approval Store
                //.approvalStoreDisabled()

                // Enable JDBC Approval Store
                .approvalStore(approvalStore())

                // Set CustomTokenEnhancer
                .tokenEnhancer(tokenEnhancer())

                // Enable JDBC Authorisation code services
                // for issuing and storing authorization codes
                .authorizationCodeServices(authorizationCodeServices())

                // Inject authenticationManager to enable Client Password Flow
                .authenticationManager(authenticationManager);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource());
    }

    @Bean
    public ApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource());
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }

    @Bean
    protected AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource());
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }



    // JDBC token store configuration
    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(schemaScript);
        return populator;
    }

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
        return dataSource;
    }

}
