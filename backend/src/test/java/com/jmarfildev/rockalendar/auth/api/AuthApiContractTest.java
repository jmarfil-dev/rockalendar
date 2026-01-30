package com.jmarfildev.rockalendar.auth.api;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthApiContractTest extends AbstractPostgresTest {

    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String API_AUTH_LOGIN = "/api/auth/login";

    @Test
    @DisplayName("POST /api/auth/login con credenciales inválidas -> 401 ProblemDetail (sin user-enumeration)")
    void login_badCredentials_returns401ProblemDetail() throws Exception {
        var body = """
                { "email": "nope@rockalendar.test", "password": "wrong" }
                """;

        var ra = mockMvc.perform(post(API_AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 401, API_AUTH_LOGIN);

        // No revelar "user not found".
        ra.andExpect(jsonPath("$.detail", not(containsStringIgnoringCase("not found"))));
    }
}
