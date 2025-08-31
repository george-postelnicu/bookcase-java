package ro.georgepostelnicu.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.georgepostelnicu.app.dto.ErrorDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.georgepostelnicu.app.controller.GlobalControllerAdvice.APPLICATION_ERROR_TYPE;

/**
 * Covers the generic exception path in GlobalControllerAdvice (500 error).
 */
class GlobalControllerAdviceTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalControllerAdvice())
                .build();
    }

    @Test
    void handleUncaughtException_returnsProblemJson500() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ErrorDto dto = objectMapper.readValue(json, ErrorDto.class);

        assertNotNull(dto.getTraceId());
        assertEquals(APPLICATION_ERROR_TYPE, dto.getTitle());
        assertEquals("boom", dto.getDetail());
        assertEquals(500, dto.getStatus().value());
    }

    // Minimal controller used only for triggering an uncaught exception handled by advice
    @org.springframework.web.bind.annotation.RestController
    static class ThrowingController {
        @org.springframework.web.bind.annotation.GetMapping("/api/test/boom")
        public String boom() {
            throw new RuntimeException("boom");
        }
    }
}
