package se.iths.lw.microprojectproductservice.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "accessToken";
    // The name of the cookie that holds the JWT
    // a constant: never changes, shared by all instances

    private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

    @Override // Find the token, header first, then cookie
    public String resolve(HttpServletRequest request) {

        // First check the normal Authorization header ( same behavior as before)
        // Ask the default resolver for a token( return null if there is no Bearer header)
        String token = defaultResolver.resolve(request);

        if( token != null ) {
            return token;
        } // If a token was found in the header,use it immediately; the cookie is not checked.


        // Otherwise look for the JWT in the cookie
        // getCookies() returns null when the request has no cookies, so check first
        if(request.getCookies() != null) {
            // Go through every cookie that came with the request
            for (Cookie cookie : request.getCookies()) {
                //Ignore an empty cookie value: an empty token would be rejected as invalid (401)
                //instead of being treated as "not logged in".

                //The cookie must have the right name...
                if (COOKIE_NAME.equals(cookie.getName())
                && cookie.getValue() != null // ... must have a value ( not null)...
                && !cookie.getValue().isBlank()) { // ...and the value must not be empty or only spaces
                    return cookie.getValue(); // Use the cookie value as the token
                }
            }
        }

        // No token found: the request continues as anonymous ( fine for public endpoints)
        return null;
    }
}
