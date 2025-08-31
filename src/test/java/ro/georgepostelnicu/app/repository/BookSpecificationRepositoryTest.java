package ro.georgepostelnicu.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.georgepostelnicu.app.AbstractIntegrationTest;
import ro.georgepostelnicu.app.model.Book;
import ro.georgepostelnicu.app.model.BookSearchCriteria;
import ro.georgepostelnicu.app.service.BookService;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ro.georgepostelnicu.app.DataCommon.*;

class BookSpecificationRepositoryTest extends AbstractIntegrationTest {

    private static final Pageable SIZE = Pageable.ofSize(20);

    private final BookService bookService;
    private final BookSpecificationRepository repository;

    @Autowired
    BookSpecificationRepositoryTest(BookService bookService, BookSpecificationRepository repository) {
        this.bookService = bookService;
        this.repository = repository;
    }

    @BeforeEach
    void seed() {
        bookService.create(landscapesOfIdentity());
        bookService.create(conflictsAndAdaptations());
        bookService.create(oneHundredStepsThrough20thCenturyEstonianArchitecture());
        bookService.create(oneHundredFiftyHouses());
    }

    @Test
    void search_filtersAcrossMultipleFields_likeServiceTest_smoke() {
        // name wildcard
        BookSearchCriteria nameWildcard = new BookSearchCriteria("*AND*", null, null, null, null,
                null, null, null,
                null, null, null,
                null, null, null);
        assertFound(nameWildcard, estonianArtBookNames());

        // publisher exact
        BookSearchCriteria publisher = new BookSearchCriteria(null, null, null, null, null,
                null, null, null,
                ART_MUSEUM_OF_ESTONIA, null, null,
                null, null, null);
        assertFound(publisher, estonianArtBookNames());

        // min/max years
        BookSearchCriteria minMaxYear = new BookSearchCriteria(null, null, null, null, null,
                null, null, null,
                null, null, CONFLICTS_PUBLISH_YEAR,
                CONFLICTS_PUBLISH_YEAR, null, null);
        assertFound(minMaxYear, Set.of(CONFLICTS_AND_ADAPTATIONS));

        // authors AND keywords combined: pick an author present in multiple books (KAJA)
        // and keywords common to Estonian art books; expect those books returned
        BookSearchCriteria authorsKeywords = new BookSearchCriteria(null, null, null, null, null,
                Set.of(LINDA, KADI, BART, KAJA), estonianArtKeywords(), null,
                null, null, null,
                null, null, null);
        assertFound(authorsKeywords, Set.of(LANDSCAPES_OF_IDENTITY));

        // pages max
        BookSearchCriteria maxPages = new BookSearchCriteria(null, null, null, null, null,
                null, null, null,
                null, null, null,
                null, null, ESTONIAN_ART_BOOKS_PAGE_NR);
        assertFound(maxPages, estonianArtBookNames());

        // barcode wildcard
        BookSearchCriteria barcodeWildcard = new BookSearchCriteria(null, null, null, null, "9789949*",
                null, null, null,
                null, null, null,
                null, null, null);
        assertFound(barcodeWildcard, estonianBookNames());
    }

    private void assertFound(BookSearchCriteria criteria, Set<String> expectedNames) {
        Page<Book> page = repository.search(criteria, SIZE);
        assertEquals(expectedNames.size(), page.getTotalElements());
        assertEquals(expectedNames, page.getContent().stream().map(Book::getName).collect(Collectors.toSet()));
    }
}
