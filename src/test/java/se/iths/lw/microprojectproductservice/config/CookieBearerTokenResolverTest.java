package se.iths.lw.microprojectproductservice.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

public class CookieBearerTokenResolverTest {
    private final CookieBearerTokenResolver resolver = new CookieBearerTokenResolver();

    @Test
    void resolve_shouldReturnToken_fromAuthorizationHeader(){
        //Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");

        //Act & Assert
        assertEquals("header-token", resolver.resolve(request));

    }

    @Test
    void resolve_shouldReturnToken_fromCookie_whenNoHeader(){
        //Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "cookie-token"));


        //Act & Assert
        assertEquals("cookie-token", resolver.resolve(request));
    }

    @Test
    void resolve_shouldPreferHeader_whenHeaderAndCookieExist(){
        //Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("accessToken", "cookie-token"));

        //Act & Assert
        assertEquals ("header-token", resolver.resolve(request));
    }


    @Test
    void resolve_shouldFindAccessTokenCookie_amongOtherCookies() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("theme", "dark"), new Cookie("accessToken", "cookie-token"));

        // Act & Assert
        assertEquals("cookie-token", resolver.resolve(request));
    }

    @Test
    void resolve_shouldReturnNull_whenNoHeaderAndNoCookies(){
        //Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("theme", "dark"), new Cookie("consent", "yes"));

        //Act & Assert
        assertNull(resolver.resolve(request));
    }

    @Test
    void resolve_shouldReturnNull_whenAccessTokenCookieIsBlank(){
        // Arrange: this is what a cleared cookie can look like
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies( new Cookie("accessToken", ""));

        // Act & Assert
        assertNull(resolver.resolve(request));
    }
}
