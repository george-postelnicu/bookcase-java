package ro.georgepostelnicu.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.georgepostelnicu.app.dto.language.LanguageDto;
import ro.georgepostelnicu.app.dto.language.LanguagesDto;
import ro.georgepostelnicu.app.exception.EntityAlreadyExistException;
import ro.georgepostelnicu.app.exception.EntityAlreadyLinkedException;
import ro.georgepostelnicu.app.exception.EntityNotFoundException;
import ro.georgepostelnicu.app.mapper.LibraryMapper;
import ro.georgepostelnicu.app.model.Language;
import ro.georgepostelnicu.app.repository.LanguageRepository;
import ro.georgepostelnicu.app.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static ro.georgepostelnicu.app.model.EntityName.LANGUAGE;
import static ro.georgepostelnicu.app.util.StringUtil.splitCapitalizeAndJoin;

@Service
public class LanguageService {
    private final LanguageRepository repository;

    @Autowired
    public LanguageService(LanguageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Language> createBulk(LanguagesDto request) {
        return request.getLanguages().stream()
                .map(StringUtil::splitCapitalizeAndJoin)
                .map(LanguageDto::new)
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Transactional
    public Language create(LanguageDto languageDto) {
        Language language = LibraryMapper.INSTANCE.toLanguage(languageDto);

        if (repository.existsByNameIgnoreCase(languageDto.getName())) {
            throw new EntityAlreadyExistException(LANGUAGE, splitCapitalizeAndJoin(languageDto.getName()));
        }

        return repository.save(language);
    }

    @Transactional
    public Language createIfNotExisting(LanguageDto languageDto) {
        return repository.findByNameIgnoreCase(languageDto.getName())
                .orElseGet(() -> create(languageDto));
    }

    @Transactional(readOnly = true, propagation = REQUIRED)
    public Language read(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(LANGUAGE, id));
    }

    @Transactional
    public Language update(Long id, LanguageDto languageDto) {
        Language language = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(LANGUAGE, id));

        if (repository.existsByNameIgnoreCaseAndIdIsNot(languageDto.getName(), id)) {
            throw new EntityAlreadyExistException(LANGUAGE, splitCapitalizeAndJoin(languageDto.getName()));
        }

        LibraryMapper.INSTANCE.updateLanguageFromDto(languageDto, language);

        return repository.save(language);
    }

    @Transactional
    public void delete(Long id) {
        Language language = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(LANGUAGE, id));

        if (language.getBooks().isEmpty()) {
            repository.delete(language);
        } else {
            throw new EntityAlreadyLinkedException(LANGUAGE, language.getName());
        }
    }
}
