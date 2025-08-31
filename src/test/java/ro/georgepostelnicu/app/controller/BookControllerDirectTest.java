package ro.georgepostelnicu.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import ro.georgepostelnicu.app.AbstractIntegrationTest;
import ro.georgepostelnicu.app.dto.book.BookDto;
import ro.georgepostelnicu.app.dto.book.BookResponseDto;
import ro.georgepostelnicu.app.model.Book;
import ro.georgepostelnicu.app.service.BookService;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static ro.georgepostelnicu.app.DataCommon.*;

class BookControllerDirectTest extends AbstractIntegrationTest {

    private final BookController controller;
    private final BookService service;

    @Autowired
    BookControllerDirectTest(BookController controller, BookService service) {
        this.controller = controller;
        this.service = service;
    }

    @BeforeEach
    void seed() {
        // Each test starts with a clean DB via @Sql in AbstractIntegrationTest
    }

    @Test
    @Transactional
    void search_create_read_update_delete_cover_mapping_lines() {
        // Seed some books via service so search has data
        BookDto b1 = landscapesOfIdentity();
        BookDto b2 = conflictsAndAdaptations();
        Book saved1 = service.create(b1);
        Book saved2 = service.create(b2);

        // 1) searchBooks mapping and return
        ResponseEntity<Page<BookResponseDto>> searchResp = controller.searchBooks(
                0, 10,
                "*AND*", // name
                null, // full title
                null, // description
                null, null, // isbn, barcode
                null, null, null, // authors, keywords, languages
                null, // publisher
                null, // cover
                null, null, // min/max year
                null, null // min/max pages
        );
        assertEquals(200, searchResp.getStatusCode().value());
        assertNotNull(searchResp.getBody());
        assertTrue(searchResp.getBody().getTotalElements() >= 1);
        // Force evaluation of mapping function by touching content
        searchResp.getBody().getContent().forEach(b -> assertNotNull(b.getName()));

        // 2) create mapping, location header, response body
        BookDto toCreate = anotherBookLikeLandscapes();
        ResponseEntity<BookResponseDto> created = controller.create(toCreate);
        assertEquals(201, created.getStatusCode().value());
        URI location = created.getHeaders().getLocation();
        assertNotNull(location);
        assertNotNull(created.getBody());
        assertEquals(toCreate.getName(), created.getBody().getName());

        service.read(saved1.getId()); // ensure service works and id accessible

        // 3) read mapping and return
        ResponseEntity<BookResponseDto> readResp = controller.read(saved2.getId());
        assertEquals(200, readResp.getStatusCode().value());
        assertNotNull(readResp.getBody());
        assertEquals(saved2.getName(), readResp.getBody().getName());

        // 4) update mapping and return
        BookDto upd = conflictsAndAdaptations();
        upd.setName("Conflicts and adaptations. ");
        ResponseEntity<BookResponseDto> updated = controller.update(upd, saved2.getId());
        assertEquals(200, updated.getStatusCode().value());
        assertNotNull(updated.getBody());
        assertEquals("Conflicts and adaptations. ", updated.getBody().getName());

        // 5) delete invocation
        controller.delete(created.getBody().getId());
        // verify it is actually deleted via service call path
        assertThrows(Exception.class, () -> service.read(created.getBody().getId()));
    }
}
