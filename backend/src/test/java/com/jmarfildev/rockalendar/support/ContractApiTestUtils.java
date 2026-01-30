package com.jmarfildev.rockalendar.support;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
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
