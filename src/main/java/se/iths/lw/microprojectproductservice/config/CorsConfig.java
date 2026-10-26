package se.iths.lw.microprojectproductservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;

// Configure CORS ( Cross-Origin Resource Sharing) for the application.
// CORS decides which frontends (origins) are allowed to call this backend
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // Holds all CORS rules: origins, methods, headers and credentials
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(",")); // Split the comma-separated property value into a list of origins
        configuration.setAllowedOrigins( origins );// Only these origins may call the API from a browser
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // HTTP methods the frontend is allowed to use (OPTIONS is needed for preflight requests)
        // Preflight request: always an OPTIONS request. The browser sends it automatically, before a real request, only to ask permission.


        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);// Allow cookies and authorization headers to be sent with requests.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // Maps URL patterns to CORS configurations
        source.registerCorsConfiguration("/**", configuration);
        // "/**" means that rule applies to every path in the product service api,
        // so a browser page from an origin in allowedOrigins is allowed to call any of them.
        return source;
    }
}
