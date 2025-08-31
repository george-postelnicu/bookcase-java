package ro.georgepostelnicu.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.georgepostelnicu.app.AbstractIntegrationTest;
import ro.georgepostelnicu.app.dto.book.BookDto;
import ro.georgepostelnicu.app.exception.EntityAlreadyExistException;
import ro.georgepostelnicu.app.exception.EntityValidationException;
import ro.georgepostelnicu.app.model.Book;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ro.georgepostelnicu.app.DataCommon.*;
import static ro.georgepostelnicu.app.exception.EntityAlreadyExistException.ENTITY_ALREADY_HAS_COLLECTION;
import static ro.georgepostelnicu.app.exception.EntityValidationException.ENTITY_VALIDATION_FAILURE;
import static ro.georgepostelnicu.app.model.EntityName.BOOK;

class BookServiceBranchCoverageTest extends AbstractIntegrationTest {

    private final BookService service;

    @Autowired
    BookServiceBranchCoverageTest(BookService service) {
        this.service = service;
    }

    @BeforeEach
    void seed() {
        // No seed by default; each test seeds deterministically
    }

    @Test
    void create_throwsValidation_whenNameNotInFullTitle() {
        BookDto dto = new BookDto();
        dto.setName("Bar");
        dto.setFullTitle("Foo"); // does not contain name -> triggers validation
        dto.setIsbn("ISBN 978-0-596-52068-7"); // valid
        dto.setBarcode("9780596520687");
        // leave authors/keywords/languages empty

        EntityValidationException ex = assertThrows(EntityValidationException.class, () -> service.create(dto));
        assertEquals(String.format(ENTITY_VALIDATION_FAILURE, BOOK, BookService.NAME_IS_NOT_INCLUDED_IN_FULL_TITLE), ex.getMessage());
    }

    @Test
    void create_throwsAlreadyExist_onDuplicateIsbn() {
        // First book
        BookDto first = landscapesOfIdentity();
        Book saved = service.create(first);
        assertNotNull(saved.getId());

        // Second book with different name but same ISBN
        BookDto dupIsbn = landscapesOfIdentity();
        String name = "Landscapes Copy";
        dupIsbn.setName(name);

        EntityAlreadyExistException ex = assertThrows(EntityAlreadyExistException.class, () -> service.create(dupIsbn));
        assertEquals(String.format(ENTITY_ALREADY_HAS_COLLECTION, BOOK, Set.of(name, dupIsbn.getIsbn())), ex.getMessage());
    }

    @Test
    void create_throwsAlreadyExist_onDuplicateBarcode() {
        // First book
        BookDto first = conflictsAndAdaptations();
        Book saved = service.create(first);
        assertNotNull(saved.getId());

        // Second book with different name and isbn but the same barcode
        BookDto dupBarcode = conflictsAndAdaptations();
        String name = "Conflicts Copy";
        dupBarcode.setIsbn("9780596520687");
        dupBarcode.setName(name);

        EntityAlreadyExistException ex = assertThrows(EntityAlreadyExistException.class, () -> service.create(dupBarcode));
        assertEquals(String.format(ENTITY_ALREADY_HAS_COLLECTION, BOOK, Set.of(name, dupBarcode.getBarcode())), ex.getMessage());
    }

    @Test
    void update_throwsAlreadyExist_onDuplicateIsbn_whenChangedToExisting() {
        // Create two books
        Book b1 = service.create(landscapesOfIdentity());
        Book b2 = service.create(oneHundredStepsThrough20thCenturyEstonianArchitecture());

        // Attempt to update b1's ISBN to b2's ISBN
        BookDto updated = landscapesOfIdentity();
        updated.setIsbn(b2.getIsbn()); // ISBN of b2
        updated.setName(b1.getName()); // keep the same name to avoid name duplicate branch

        EntityAlreadyExistException ex = assertThrows(EntityAlreadyExistException.class, () -> service.update(b1.getId(), updated));
        assertEquals(String.format(ENTITY_ALREADY_HAS_COLLECTION, BOOK, Set.of(b1.getName(), b2.getIsbn())), ex.getMessage());
    }

    @Test
    void update_throwsAlreadyExist_onDuplicateBarcode_whenChangedToExisting() {
        // Create two books
        Book b1 = service.create(landscapesOfIdentity());
        Book b2 = service.create(conflictsAndAdaptations());

        // Attempt to update b1's barcode to b2's barcode
        BookDto updated = landscapesOfIdentity();
        updated.setBarcode(b2.getBarcode());
        updated.setName(b1.getName()); // keep the same name

        EntityAlreadyExistException ex = assertThrows(EntityAlreadyExistException.class, () -> service.update(b1.getId(), updated));
        assertEquals(String.format(ENTITY_ALREADY_HAS_COLLECTION, BOOK, Set.of(b1.getName(), b2.getBarcode())), ex.getMessage());
    }
}
