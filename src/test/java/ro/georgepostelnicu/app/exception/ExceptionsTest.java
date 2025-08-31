package ro.georgepostelnicu.app.exception;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ro.georgepostelnicu.app.exception.EntityAlreadyExistException.ENTITY_ALREADY_HAS_A;
import static ro.georgepostelnicu.app.exception.EntityAlreadyExistException.ENTITY_ALREADY_HAS_COLLECTION;
import static ro.georgepostelnicu.app.exception.EntityAlreadyLinkedException.ENTITY_ALREADY_HAS_A_LINK;
import static ro.georgepostelnicu.app.exception.EntityNotFoundException.CANNOT_FIND_ENTITY_ID;
import static ro.georgepostelnicu.app.exception.EntityValidationException.ENTITY_VALIDATION_FAILURE;

class ExceptionsTest {

    @Test
    void entityAlreadyExistException_formatsMessage_forSingleDuplicate() {
        EntityAlreadyExistException ex = new EntityAlreadyExistException("keyword", "Art");
        assertEquals(String.format(ENTITY_ALREADY_HAS_A, "keyword", "Art"), ex.getMessage());
    }

    @Test
    void entityAlreadyExistException_formatsMessage_forCollectionDuplicates() {
        Set<String> dups = Set.of("Art", "Architecture");
        EntityAlreadyExistException ex = new EntityAlreadyExistException("keyword", dups);
        assertEquals(String.format(ENTITY_ALREADY_HAS_COLLECTION, "keyword", dups), ex.getMessage());
    }

    @Test
    void entityNotFoundException_formatsMessage_forId() {
        EntityNotFoundException ex = new EntityNotFoundException("keyword", 0L);
        assertEquals(String.format(CANNOT_FIND_ENTITY_ID, "keyword", 0L), ex.getMessage());
    }

    @Test
    void entityAlreadyLinkedException_formatsMessage() {
        EntityAlreadyLinkedException ex = new EntityAlreadyLinkedException("author", "books");
        assertEquals(String.format(ENTITY_ALREADY_HAS_A_LINK, "author", "books"), ex.getMessage());
    }

    @Test
    void entityValidationException_formatsMessage() {
        EntityValidationException ex = new EntityValidationException("book", "Invalid ISBN");
        assertEquals(String.format(ENTITY_VALIDATION_FAILURE, "book", "Invalid ISBN"), ex.getMessage());
    }
}
