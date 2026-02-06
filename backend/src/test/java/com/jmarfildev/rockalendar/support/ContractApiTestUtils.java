package com.jmarfildev.rockalendar.support;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;


/**
 * @author jmarfil
 *
 */
@Component
@Profile("test")
public class ContractApiTestUtils {

    /*
     * Auth
     */

    private JwtRequestPostProcessor authJwt(String uuid, String email, String role) {
        return jwt()
                .authorities(new SimpleGrantedAuthority(role))
                .jwt(j -> j
                        .subject(uuid)
                        .claim("email", email)
                        .claim("roles", List.of(role))
                );
    }

    public JwtRequestPostProcessor authJwt() {
        return authJwt(TestConstants.MOCK_USER_ID, TestConstants.MOCK_USER_ID, "ROLE_USER");
    }

    public JwtRequestPostProcessor authJwtModerator() {
        return authJwt(TestConstants.MOCK_MODERATOR_ID, TestConstants.MOCK_MODERATOR_ID, "ROLE_MODERATOR");
    }

    public JwtRequestPostProcessor authJwtAdmin() {
        return authJwt(TestConstants.MOCK_ADMIN_ID, TestConstants.MOCK_ADMIN_ID, "ROLE_ADMIN");
    }

    /*
     * Assertions
     */

    public void expectProblemDetail(ResultActions ra, int status, String instance) throws Exception {
        ra.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.instance").value(instance))
                .andExpect(jsonPath("$.title", not(emptyOrNullString())))
                .andExpect(jsonPath("$.detail", notNullValue()));
    }
}
