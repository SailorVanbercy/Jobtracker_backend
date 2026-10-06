package com.portfolio.jobtracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.portfolio.jobtracker.config.JwtAuthenticationFilter;
import com.portfolio.jobtracker.config.SecurityConfig;
import com.portfolio.jobtracker.dto.AuthRequest;
import com.portfolio.jobtracker.dto.AuthResponse;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.repository.UserRepository;
import com.portfolio.jobtracker.service.AuthenticationService;
import com.portfolio.jobtracker.service.JwtService;
import com.portfolio.jobtracker.service.PdfService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;

/**
 * Web-layer tests running the real SecurityConfig and JWT filter against
 * AuthController and UserController, with persistence and external services mocked.
 */
@SpringJUnitWebConfig(SecuredControllersTest.TestConfig.class)
@TestPropertySource(properties = {
    "security.jwt.cookie-secure=true",
    "security.jwt.expiration-time=3600000",
    "app.cors.allowed-origins=http://localhost:3000"
})
class SecuredControllersTest {

    private static final String USER_EMAIL = "jane.doe@example.com";
    private static final String JWT_COOKIE_NAME = "jwt_token";
    private static final int EXPECTED_COOKIE_MAX_AGE_SECONDS = 3600;

    @Configuration
    @EnableWebMvc
    @Import({ SecurityConfig.class, JwtAuthenticationFilter.class, AuthController.class, UserController.class })
    static class TestConfig {
    }

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PdfService pdfService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private static Users buildUser(String resumeText) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setEmail(USER_EMAIL);
        user.setPasswordHash("$2a$10$hashedPasswordValue");
        user.setResumeText(resumeText);
        return user;
    }

    // --- POST /api/auth/logout ---

    @Test
    void logout_returnsNoContentAndExpiresJwtCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(cookie().exists(JWT_COOKIE_NAME))
            .andExpect(cookie().value(JWT_COOKIE_NAME, ""))
            .andExpect(cookie().maxAge(JWT_COOKIE_NAME, 0))
            .andExpect(cookie().path(JWT_COOKIE_NAME, "/"))
            .andExpect(cookie().httpOnly(JWT_COOKIE_NAME, true))
            .andExpect(cookie().secure(JWT_COOKIE_NAME, true));
    }

    // --- POST /api/auth/login ---

    @Test
    void login_setsJwtCookieFromConfiguration() throws Exception {
        when(authenticationService.authenticate(any(AuthRequest.class)))
            .thenReturn(new AuthResponse("signed.jwt.token"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + USER_EMAIL + "\",\"password\":\"Str0ngP@ssword!\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().value(JWT_COOKIE_NAME, "signed.jwt.token"))
            .andExpect(cookie().maxAge(JWT_COOKIE_NAME, EXPECTED_COOKIE_MAX_AGE_SECONDS))
            .andExpect(cookie().path(JWT_COOKIE_NAME, "/"))
            .andExpect(cookie().httpOnly(JWT_COOKIE_NAME, true))
            .andExpect(cookie().secure(JWT_COOKIE_NAME, true));
    }

    // --- Invalid JWT cookie (e.g. signed with a rotated key) ---

    private static final String STALE_TOKEN = "stale.jwt.token";

    @Test
    void login_succeeds_whenBrowserStillSendsTokenSignedWithOldKey() throws Exception {
        when(jwtService.extractUsername(STALE_TOKEN))
            .thenThrow(new SignatureException("JWT signature does not match"));
        when(authenticationService.authenticate(any(AuthRequest.class)))
            .thenReturn(new AuthResponse("fresh.jwt.token"));

        mockMvc.perform(post("/api/auth/login")
                .cookie(new Cookie(JWT_COOKIE_NAME, STALE_TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + USER_EMAIL + "\",\"password\":\"Str0ngP@ssword!\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().value(JWT_COOKIE_NAME, "fresh.jwt.token"));
    }

    @Test
    void protectedRoute_isRejected_notServerError_whenTokenSignatureIsInvalid() throws Exception {
        when(jwtService.extractUsername(STALE_TOKEN))
            .thenThrow(new SignatureException("JWT signature does not match"));

        mockMvc.perform(get("/api/users/me").cookie(new Cookie(JWT_COOKIE_NAME, STALE_TOKEN)))
            .andExpect(status().isForbidden());
    }

    @Test
    void protectedRoute_isRejected_notServerError_whenTokenIsExpired() throws Exception {
        when(jwtService.extractUsername(STALE_TOKEN))
            .thenThrow(new ExpiredJwtException(null, null, "JWT expired"));

        mockMvc.perform(get("/api/users/me").cookie(new Cookie(JWT_COOKIE_NAME, STALE_TOKEN)))
            .andExpect(status().isForbidden());
    }

    @Test
    void protectedRoute_isRejected_notServerError_whenTokenUserNoLongerExists() throws Exception {
        when(jwtService.extractUsername(STALE_TOKEN)).thenReturn("deleted@example.com");
        when(userDetailsService.loadUserByUsername("deleted@example.com"))
            .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/me").cookie(new Cookie(JWT_COOKIE_NAME, STALE_TOKEN)))
            .andExpect(status().isForbidden());
    }

    // --- CORS ---

    @Test
    void cors_allowsConfiguredOriginWithCredentials() throws Exception {
        mockMvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void cors_rejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/users/me")
                .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // --- GET /api/users/me ---

    @Test
    void getCurrentUser_returnsProfileWithHasResumeTrue_whenResumePresent() throws Exception {
        Users user = buildUser("Senior Java developer with 10 years of experience");
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me").with(user(USER_EMAIL)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId().toString()))
            .andExpect(jsonPath("$.email").value(USER_EMAIL))
            .andExpect(jsonPath("$.hasResume").value(true))
            .andExpect(jsonPath("$.resumeText").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getCurrentUser_returnsHasResumeFalse_whenResumeIsNull() throws Exception {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buildUser(null)));

        mockMvc.perform(get("/api/users/me").with(user(USER_EMAIL)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasResume").value(false));
    }

    @Test
    void getCurrentUser_returnsHasResumeFalse_whenResumeIsEmpty() throws Exception {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buildUser("")));

        mockMvc.perform(get("/api/users/me").with(user(USER_EMAIL)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasResume").value(false));
    }

    @Test
    void getCurrentUser_isRejected_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isForbidden());

        verify(userRepository, never()).findByEmail(any());
    }

    // --- POST /api/users/me/resume ---

    @Test
    void uploadResume_isRejected_whenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "cv.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[] { 1, 2, 3 });

        mockMvc.perform(multipart("/api/users/me/resume").file(file))
            .andExpect(status().isForbidden());

        verify(pdfService, never()).extractTextFromPdf(any());
    }

    // --- POST /api/users (registration) ---

    @Test
    void createUser_remainsPublic() throws Exception {
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new.user@example.com\",\"passwordHash\":\"Str0ngP@ssword!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new.user@example.com"));

        Mockito.verify(userRepository).save(any(Users.class));
    }
}
