package ro.georgepostelnicu.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.georgepostelnicu.app.dto.keyword.KeywordDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordsDto;
import ro.georgepostelnicu.app.exception.EntityAlreadyExistException;
import ro.georgepostelnicu.app.exception.EntityAlreadyLinkedException;
import ro.georgepostelnicu.app.exception.EntityNotFoundException;
import ro.georgepostelnicu.app.mapper.LibraryMapper;
import ro.georgepostelnicu.app.model.Keyword;
import ro.georgepostelnicu.app.repository.KeywordRepository;
import ro.georgepostelnicu.app.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static ro.georgepostelnicu.app.model.EntityName.KEYWORD;
import static ro.georgepostelnicu.app.util.StringUtil.splitCapitalizeAndJoin;

@Service
public class KeywordService {
    private final KeywordRepository repository;

    @Autowired
    public KeywordService(KeywordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Keyword> createBulk(KeywordsDto keywordsDto) {
        return keywordsDto.getKeywords().stream()
                .map(StringUtil::splitCapitalizeAndJoin)
                .map(KeywordDto::new)
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public Keyword create(KeywordDto keywordDto) {
        Keyword keyword = LibraryMapper.INSTANCE.toKeyword(keywordDto);

        if (repository.existsByNameIgnoreCase(keywordDto.getName())) {
            throw new EntityAlreadyExistException(KEYWORD, splitCapitalizeAndJoin(keywordDto.getName()));
        }

        return repository.save(keyword);
    }

    @Transactional
    public Keyword createIfNotExisting(KeywordDto keywordDto) {
        return repository.findByNameIgnoreCase(keywordDto.getName())
                .orElseGet(() -> create(keywordDto));
    }

    @Transactional(readOnly = true, propagation = REQUIRED)
    public Keyword read(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(KEYWORD, id));
    }

    @Transactional
    public Keyword update(Long id, KeywordDto keywordDto) {
        Keyword keyword = read(id);

        if (repository.existsByNameIgnoreCaseAndIdIsNot(keywordDto.getName(), id)) {
            throw new EntityAlreadyExistException(KEYWORD, splitCapitalizeAndJoin(keywordDto.getName()));
        }

        LibraryMapper.INSTANCE.updateKeywordFromDto(keywordDto, keyword);

        return repository.save(keyword);
    }

    @Transactional
    public void delete(Long id) {
        Keyword keyword = read(id);

        if (keyword.getBooks().isEmpty()) {
            repository.delete(keyword);
        } else {
            throw new EntityAlreadyLinkedException(KEYWORD, keyword.getName());
        }
    }
}
