package com.nectcracker.studyproject.config;

import com.nectcracker.studyproject.service.CustomUserInfoTokenServices;
import com.nectcracker.studyproject.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.Filter;
import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OAuth2ClientContext oAuth2ClientContext;

    private final AuthProvider authProvider;

    private final UserService userService;

    private final DataSource dataSource;

    private final PasswordEncoder passwordEncoder;

    public WebSecurityConfig(UserService userService, DataSource dataSource, @Qualifier("oauth2ClientContext") OAuth2ClientContext oAuth2ClientContext, AuthProvider authProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.dataSource = dataSource;
        this.oAuth2ClientContext = oAuth2ClientContext;
        this.authProvider = authProvider;

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/", "/registration/**", "/home", "/registration/activate/*","/vk/**",
                            "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html",
                            "/**/*.css", "/**/*.js", "/static/**", "/friend_page/**").permitAll()
                    .anyRequest()
                    .authenticated()
                .and()
                    .formLogin()
                    .loginPage("/login")
                    .permitAll()
                .and()
                    .logout()
                    .permitAll()
                .and()
                    .csrf().disable();
        http.
                addFilterBefore(ssoFilter(), UsernamePasswordAuthenticationFilter.class);

    }

//    @Autowired
//    protected void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
//        auth.jdbcAuthentication()
//                .dataSource(dataSource)
//                .passwordEncoder(passwordEncoder)
//                .usersByUsernameQuery(
//                        "select username, password, 1 from users where username=?")
//                .authoritiesByUsernameQuery("select u.username, ur.roles from users u inner join user_role ur on u.id = ur.user_id where u.username=?");
//    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Bean
    @ConfigurationProperties("spring.security.oauth2.vk.client")
    public AuthorizationCodeResourceDetails vk() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("spring.security.oauth2.vk.resource")
    public ResourceServerProperties vkResource() {
        return new ResourceServerProperties();
    }

    @Bean
    public FilterRegistrationBean oAuth2ClientFilterRegistration(OAuth2ClientContextFilter oAuth2ClientContextFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(oAuth2ClientContextFilter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    private Filter ssoFilter() {
        OAuth2ClientAuthenticationProcessingFilter vkFilter = new OAuth2ClientAuthenticationProcessingFilter("/vk/logins");
        OAuth2RestTemplate vkTemplate = new OAuth2RestTemplate(vk(), oAuth2ClientContext);
        vkFilter.setRestTemplate(vkTemplate);
        CustomUserInfoTokenServices tokenServices = new CustomUserInfoTokenServices(vkResource().getUserInfoUri(), vk().getClientId());
        tokenServices.setRestTemplate(vkTemplate);
        vkFilter.setTokenServices(tokenServices);
        tokenServices.setUserService(userService);
        log.info("Token Service started", tokenServices);
        return vkFilter;
    }
}
