package ro.georgepostelnicu.app.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.georgepostelnicu.app.model.Keyword;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    boolean existsByNameIgnoreCase(@NotBlank String name);

    boolean existsByNameIgnoreCaseAndIdIsNot(@NotBlank String name, @NotNull Long id);

    Optional<Keyword> findByNameIgnoreCase(@NotBlank String name);
}
