package com.jmarfildev.rockalendar.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.config.AuthCookieHelper;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestConstants;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiContractTest extends AbstractPostgresTest {

    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String apiAuthLogin = "/api/auth/login";
    private final String apiAuthRegister = "/api/auth/register";
    private final String apiAuthLogout = "/api/auth/logout";

    @Test
    @DisplayName("POST /api/auth/login con credenciales válidas -> 200, cookie httpOnly y expiresAt")
    void login_validCredentials_returnsCookieAndExpiresAt() throws Exception {
        mockMvc.perform(post(apiAuthLogin).contentType(MediaType.APPLICATION_JSON)
                                            .content("""
                                                     { "email": "%s", "password": "%s" }
                                                     """.formatted(TestConstants.MOCK_USER_EMAIL, TestConstants.MOCK_USER_PASSWORD)))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.expiresAt").isString())
               .andExpect(jsonPath("$.expiresAt").isNotEmpty())
               .andExpect(cookie().exists(AuthCookieHelper.COOKIE_NAME))
               .andExpect(cookie().httpOnly(AuthCookieHelper.COOKIE_NAME, true))
               .andExpect(cookie().path(AuthCookieHelper.COOKIE_NAME, "/"));
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales inválidas -> 401 ProblemDetail")
    void login_badCredentials_returns401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(post(apiAuthLogin).contentType(MediaType.APPLICATION_JSON)
                                                     .content("""
                                                              { "email": "nope@rockalendar.test", "password": "wrong" }
                                                              """));

        contractUtils.expectProblemDetail(ra, 401, apiAuthLogin);
    }

    @Test
    @DisplayName("POST /api/auth/register -> 200, cookie httpOnly y expiresAt")
    void register_asAnon_returnsCookieAndExpiresAt() throws Exception {
        mockMvc.perform(post(apiAuthRegister).contentType(MediaType.APPLICATION_JSON)
                                               .content("""
                                                        { "email": "user01@test.com", "password": "Test@1234" }
                                                        """))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.expiresAt").isString())
               .andExpect(jsonPath("$.expiresAt").isNotEmpty())
               .andExpect(cookie().exists(AuthCookieHelper.COOKIE_NAME))
               .andExpect(cookie().httpOnly(AuthCookieHelper.COOKIE_NAME, true))
               .andExpect(cookie().path(AuthCookieHelper.COOKIE_NAME, "/"));
    }

    @Test
    @DisplayName("POST /api/auth/register con datos inválidos -> 400 ProblemDetail")
    void register_asAnon_invalidRequest_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post(apiAuthRegister).contentType(MediaType.APPLICATION_JSON)
                                               .content("""
                                                        { "email": "not-an-email","password": "short" }
                                                        """))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register con email ya registrado (case-insensitive) -> 409 ProblemDetail")
    void register_asAnon_duplicateEmail_returns409ProblemDetail() throws Exception {
        mockMvc.perform(post(apiAuthRegister).contentType(MediaType.APPLICATION_JSON)
                                               .content("""
                                                        { "email": "User02@Test.com", "password": "%s" }
                                                        """.formatted(TestConstants.MOCK_USER_PASSWORD)))
               .andExpect(status().isOk());

        mockMvc.perform(post(apiAuthRegister).contentType(MediaType.APPLICATION_JSON)
                                               .content("""
                                                        { "email": "user02@test.com", "password": "%s" }
                                                        """.formatted(TestConstants.MOCK_USER_PASSWORD)))
               .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/logout -> 204 y cookie limpiada (maxAge=0)")
    void logout_clearsCookie() throws Exception {
        mockMvc.perform(post(apiAuthLogout))
               .andExpect(status().isNoContent())
               .andExpect(cookie().maxAge(AuthCookieHelper.COOKIE_NAME, 0));
    }
}
