package se.iths.lw.microprojectproductservice.config;


import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.lw.microprojectproductservice.dto.CategoryRequestDTO;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.service.CategoryService;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CookieAuthenticationIntegrationTest {

    private static final String CATEGORY_JSON = "{\"name\":\"Shoes\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CategoryService categoryService;

    @BeforeEach
    void setUp(){
        when(jwtDecoder.decode("admin-token")).thenReturn(jwtWithRole("ROLE_ADMIN"));
        when(jwtDecoder.decode("user-token")).thenReturn(jwtWithRole("ROLE_USER"));
        when(jwtDecoder.decode("bad-token")).thenThrow(new BadJwtException("Invalid token"));

        when(categoryService.create(any(CategoryRequestDTO.class)))
                .thenReturn(new CategoryResponseDTO(1L, "Shoes"));

        }


        // Builds a fake JWT whose "roles" claim contains the given role ( same claim the real auth-service uses)

        private Jwt jwtWithRole(String role) {
            Instant now  = Instant.now();
            return Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .subject("tester")
                    .claim("roles", List.of(role))
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();

        }

        @Test
        void adminCookie_shouldBeAllowed_toCreateCategory() throws Exception {
            mockMvc.perform(post("/categories/new")
                    .cookie(new Cookie("accessToken", "admin-token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CATEGORY_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated());

            verify(categoryService, times(1)).create(any(CategoryRequestDTO.class));
        }

        @Test
        void userCookie_BeForbidden_toCreateCategory() throws Exception {
            mockMvc.perform(post("/categories/new")
                    .cookie(new Cookie("accessToken","user-token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CATEGORY_JSON))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).create(any(CategoryRequestDTO.class));
        }

        @Test
        void noCookie_shouldBeUnauthorized_toCreateCategory() throws Exception {
        mockMvc.perform(post("/categories/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CATEGORY_JSON))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).create(any(CategoryRequestDTO.class));
        }

        @Test
        void invalidCookieToken_shouldBeUnauthorized() throws Exception {
            mockMvc.perform(post("/categories/new")
                    .cookie(new Cookie("accessToken", "bad-token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CATEGORY_JSON))
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).create(any(CategoryRequestDTO.class));
        }

        @Test
        void emptyCookie_shouldBeTreatedAsAnonymous() throws Exception {

        // An empty cookie must not be decoded as a token: the request is simply "not logged in" ( 401)
            mockMvc.perform(post("/categories/new")
                    .cookie(new Cookie("accessToken", ""))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CATEGORY_JSON))
                    .andExpect(status().isUnauthorized());

            verify(jwtDecoder, never()).decode(anyString());
        }

        @Test
        void bearerHeader_shouldStillWork_forCallersWithoutCookies() throws Exception {

            mockMvc.perform(post("/categories/new")
                    .header("Authorization", "Bearer admin-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CATEGORY_JSON))
                    .andExpect(status().isCreated());
        }


        @Test
        void publicEndpoint_shouldWork_withoutAnyCookie() throws Exception {
            mockMvc.perform(get("/categories/all"))
                    .andExpect(status().isOk());
        }

        @Test
        void publicEndpoint_shouldWork_withEmptyCookie() throws Exception {
                mockMvc.perform(get("/categories/all")
                        .cookie(new Cookie("accessToken", "")))
                        .andExpect(status().isOk());
        }


}
