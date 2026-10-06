package se.iths.lw.microprojectproductservice.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "accessToken";

    private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {

        // First check the normal Authorization header ( same behavior as before)
        String token = defaultResolver.resolve(request);

        if( token != null ) {
            return token;
        }


        // Otherwise look for the JWT in the cookie
        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                //Ignore an empty cookie value: an empty token would be rejected as invalid (401)
                //instead of being treated as "not logged in".
                if (COOKIE_NAME.equals(cookie.getName())
                && cookie.getValue() != null
                && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        // No token found: the request continues as anonymous ( fine for public endpoints)
        return null;
    }
}
