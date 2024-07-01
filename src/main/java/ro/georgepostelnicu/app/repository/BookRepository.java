package ro.georgepostelnicu.app.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.georgepostelnicu.app.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    boolean existsByNameIgnoreCase(@NotBlank String name);

    boolean existsByIsbnIgnoreCase(@NotBlank String isbn);

    boolean existsByBarcodeIgnoreCase(@NotBlank String barcode);
}
