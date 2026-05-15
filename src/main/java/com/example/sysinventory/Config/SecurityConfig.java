package com.example.sysinventory.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String TENANT_ID = "f33bed9f-22bd-4350-b7da-66bd88fc6458";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .tokenEndpoint(token -> token
                                .accessTokenResponseClient(accessTokenResponseClient())
                        )
                        .defaultSuccessUrl("/auth/success", true)
                        .failureUrl("/auth/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            // Detecta automáticamente si estás en localhost o en Render (o cualquier otro dominio)
            String baseUrl = getBaseUrl(request);
            String postLogoutRedirectUri = baseUrl + "/auth/login";

            String logoutUrl = "https://login.microsoftonline.com/" + TENANT_ID + "/oauth2/v2.0/logout"
                    + "?post_logout_redirect_uri=" + postLogoutRedirectUri;

            response.sendRedirect(logoutUrl);
        };
    }

    /**
     * Construye la URL base de forma dinámica según la petición entrante.
     * Funciona tanto en localhost:8080 como en Render (https://tu-app.onrender.com).
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();             // http o https
        String serverName = request.getServerName();     // localhost o tu-app.onrender.com
        int serverPort = request.getServerPort();

        // En producción (Render usa HTTPS en el puerto 443 estándar), omitir el puerto
        boolean isStandardPort = (scheme.equals("http") && serverPort == 80)
                || (scheme.equals("https") && serverPort == 443);

        if (isStandardPort) {
            return scheme + "://" + serverName;
        } else {
            return scheme + "://" + serverName + ":" + serverPort;
        }
    }
}