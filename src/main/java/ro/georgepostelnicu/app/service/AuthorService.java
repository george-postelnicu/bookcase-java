package ro.georgepostelnicu.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.georgepostelnicu.app.dto.author.AuthorDto;
import ro.georgepostelnicu.app.dto.author.AuthorsDto;
import ro.georgepostelnicu.app.exception.EntityAlreadyExistException;
import ro.georgepostelnicu.app.exception.EntityAlreadyLinkedException;
import ro.georgepostelnicu.app.exception.EntityNotFoundException;
import ro.georgepostelnicu.app.mapper.LibraryMapper;
import ro.georgepostelnicu.app.model.Author;
import ro.georgepostelnicu.app.repository.AuthorRepository;
import ro.georgepostelnicu.app.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

import static ro.georgepostelnicu.app.model.EntityName.AUTHOR;
import static ro.georgepostelnicu.app.util.StringUtil.splitCapitalizeAndJoin;

@Service
public class AuthorService {
    private final AuthorRepository repository;

    @Autowired
    public AuthorService(AuthorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Author> createBulk(AuthorsDto request) {
        return request.getAuthors().stream()
                .map(StringUtil::splitCapitalizeAndJoin)
                .map(AuthorDto::new)
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public Author create(AuthorDto authorDto) {
        Author author = LibraryMapper.INSTANCE.toAuthor(authorDto);

        if (repository.existsByNameIgnoreCase(authorDto.getName())) {
            throw new EntityAlreadyExistException(AUTHOR, splitCapitalizeAndJoin(authorDto.getName()));
        }

        return repository.save(author);
    }

    @Transactional
    public Author createIfNotExisting(AuthorDto authorDto) {
        return repository.findByNameIgnoreCase(authorDto.getName())
                .orElseGet(() -> create(authorDto));
    }

    @Transactional
    public Author read(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR, id));
    }

    @Transactional
    public Author update(Long id, AuthorDto authorDto) {
        Author author = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR, id));

        if (repository.existsByNameIgnoreCaseAndIdIsNot(authorDto.getName(), id)) {
            throw new EntityAlreadyExistException(AUTHOR, splitCapitalizeAndJoin(authorDto.getName()));
        }

        LibraryMapper.INSTANCE.updateAuthorFromDto(authorDto, author);

        return repository.save(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR, id));

        if (author.getBooks().isEmpty()) {
            repository.delete(author);
        } else {
            throw new EntityAlreadyLinkedException(AUTHOR, author.getName());
        }
    }
}
