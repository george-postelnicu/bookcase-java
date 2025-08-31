package ro.georgepostelnicu.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.georgepostelnicu.app.AbstractIntegrationTest;
import ro.georgepostelnicu.app.dto.book.BookDto;
import ro.georgepostelnicu.app.exception.EntityAlreadyExistException;
import ro.georgepostelnicu.app.model.Book;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static ro.georgepostelnicu.app.DataCommon.*;

class BookServiceAdditionalCoverageTest extends AbstractIntegrationTest {

    private final BookService service;

    @Autowired
    BookServiceAdditionalCoverageTest(BookService service) {
        this.service = service;
    }

    @Test
    void update_throwsAlreadyExist_onDuplicateName_whenChangedToExisting() {
        Book b1 = service.create(landscapesOfIdentity());
        Book b2 = service.create(conflictsAndAdaptations());

        BookDto updated = conflictsAndAdaptations();
        updated.setName(b1.getName()); // set duplicate name

        assertThrows(EntityAlreadyExistException.class, () -> service.update(b2.getId(), updated));
    }

    @Test
    void create_allows_whenFullTitleContainsName_and_whenFullTitleIsNull() {
        // when fullTitle contains name
        BookDto contains = new BookDto();
        contains.setName("Hello World");
        contains.setFullTitle("[Hello World] A Test Book");
        contains.setIsbn("9780596520687"); // valid isbn from positive cases
        contains.setBarcode("9780596520687"); // barcode can be same numeric; uniqueness per test
        // authors/keywords/languages null -> early returns in save helpers
        Book created1 = service.create(contains);
        assertNotNull(created1.getId());

        // when fullTitle is null
        BookDto noTitle = new BookDto();
        noTitle.setName("Another Book");
        noTitle.setFullTitle(null); // allowed by validation (no check)
        noTitle.setIsbn("0-8044-2957-X"); // valid ISBN-10
        noTitle.setBarcode("9789401462044-ALT"); // unique barcode string
        Book created2 = service.create(noTitle);
        assertNotNull(created2.getId());
    }

    @Test
    void create_allows_null_and_empty_collections_withoutErrors() {
        // Null collections
        BookDto nulls = new BookDto();
        nulls.setName("Null Collections");
        nulls.setFullTitle("Null Collections - Title includes Null Collections");
        nulls.setIsbn("ISBN 978-0-596-52068-7");
        nulls.setBarcode("9780596520687-1");
        Book savedNulls = service.create(nulls);
        assertNotNull(savedNulls.getId());

        // Empty collections
        BookDto empties = new BookDto();
        empties.setName("Empty Collections");
        empties.setFullTitle("Title for Empty Collections");
        empties.setIsbn("ISBN-10 0-596-52068-9");
        empties.setBarcode("9780596520687-2");
        empties.setAuthors(Collections.emptySet());
        empties.setKeywords(Collections.emptySet());
        empties.setLanguages(Collections.emptySet());
        Book savedEmpties = service.create(empties);
        assertNotNull(savedEmpties.getId());
    }
}
