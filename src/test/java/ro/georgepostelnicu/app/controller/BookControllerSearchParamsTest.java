package ro.georgepostelnicu.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.georgepostelnicu.app.AbstractIntegrationTest;
import ro.georgepostelnicu.app.dto.book.BookDto;
import ro.georgepostelnicu.app.dto.book.BookResponseDto;
import ro.georgepostelnicu.app.model.CoverType;
import ro.georgepostelnicu.app.service.BookService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.georgepostelnicu.app.DataCommon.*;
import static ro.georgepostelnicu.app.controller.ApiPrefix.BOOKS;

class BookControllerSearchParamsTest extends AbstractIntegrationTest {

    private MockMvc mockMvc;
    private final BookController controller;
    private final BookService service;
    private final ObjectMapper objectMapper;

    @Autowired
    BookControllerSearchParamsTest(BookController controller, BookService service, ObjectMapper objectMapper) {
        this.controller = controller;
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalControllerAdvice()).build();
        // Seed
        service.create(landscapesOfIdentity());
        service.create(conflictsAndAdaptations());
    }

    @Test
    void search_withAllParams_returns200() throws Exception {
        BookDto other = oneHundredStepsThrough20thCenturyEstonianArchitecture();
        service.create(other);

        String responseString = mockMvc.perform(
                        get(BOOKS)
                                .queryParam("page", "0")
                                .queryParam("size", "10")
                                .queryParam("name", "*AND*")
                                .queryParam("full_title", "*Soviet Era*")
                                .queryParam("description", "LOREM*")
                                .queryParam("isbn", "ISBN 978-9949*")
                                .queryParam("barcode", "9789949*")
                                .queryParam("authors", KAJA)
                                .queryParam("keywords", ART)
                                .queryParam("languages", ENGLISH)
                                .queryParam("publisher", ART_MUSEUM_OF_ESTONIA)
                                .queryParam("cover_type", CoverType.SOFTCOVER_WITH_DUST_JACKET.name())
                                .queryParam("min_year", String.valueOf(2021))
                                .queryParam("max_year", String.valueOf(2023))
                                .queryParam("min_pages", String.valueOf(ESTONIAN_ART_BOOKS_PAGE_NR))
                                .queryParam("max_pages", String.valueOf(ESTONIAN_ART_BOOKS_PAGE_NR))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Page<BookResponseDto> page = objectMapper.readValue(responseString, new TypeReference<>() {});
        assertTrue(page.getTotalElements() >= 1);
        assertTrue(page.getContent().stream().anyMatch(b -> b.getName().equals(CONFLICTS_AND_ADAPTATIONS)));
    }
}
