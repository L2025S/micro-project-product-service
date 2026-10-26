package se.iths.lw.microprojectproductservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { // Security setup for product-service: validates JWTs from the auth service and protects endpoints by role

    @Value("${AUTH_SERVER_URL:http://localhost:8080}")
    private String authServerUrl;


    // Verifies JWT signatures using the auth service's public keys (JWK set)
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(authServerUrl + "/auth/jwks").build();

        // Also check expiry and that the token was issued by our auth service
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(authServerUrl));

        return jwtDecoder;
    }


    // Turns the "roles" claim in the JWT into Spring authorities ( used by hasRole / hasAnyRole)
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt ->
        {
            List<String> roles = jwt.getClaimAsStringList("roles");
            // No roles in the token: the user gets no authorities
            if (roles == null) {
                return List.of();
            }


            // Wrap each role name in an authority object
            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return jwtAuthenticationConverter;
    }


    // Defines who may call which endpoint and how requests are authenticated
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        httpSecurity
                // Use the CORS rules from CorsConfig
                .cors(cors ->cors.configurationSource(corsConfigurationSource))
                // CSRF protection off ( the API is stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // No server-side sessions: every request must carry its own token
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Browser preflight requests are always allowed
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/categories/new").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        //Add an endpoint allowing order-service to use it.
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/products/stock/decrease").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/{uuid}/stock/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/{uuid}/basic-info").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,"/products/{uuid}/image").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,"/products/{uuid}/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated())// Everything else requires a valid login
                // Read the JWT from the Authorization header or the cookie, then validate it.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieBearerTokenResolver())
                        .jwt(jwt ->
                                jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)));
        // Allow pages from the same origin to show this app in a frame
        httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return httpSecurity.build();
    }
}

