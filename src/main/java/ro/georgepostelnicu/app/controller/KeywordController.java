package ro.georgepostelnicu.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.georgepostelnicu.app.dto.keyword.KeywordDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordResponseDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordsDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordsResponseDto;
import ro.georgepostelnicu.app.mapper.LibraryMapper;
import ro.georgepostelnicu.app.model.Keyword;
import ro.georgepostelnicu.app.service.KeywordService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static ro.georgepostelnicu.app.controller.ApiPrefix.BULK;
import static ro.georgepostelnicu.app.controller.ApiPrefix.KEYWORDS;

@RestController
@RequestMapping(KEYWORDS)
@Validated
public class KeywordController {
    private final KeywordService service;

    public KeywordController(KeywordService service) {
        this.service = service;
    }

    @PostMapping(value = BULK,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeywordsResponseDto> createBulk(@Valid @RequestBody KeywordsDto keywordsDto) {
        List<Keyword> keywords = service.createBulk(keywordsDto);
        List<KeywordResponseDto> responseDtos = keywords.stream()
                .map(LibraryMapper.INSTANCE::toKeywordResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(KeywordsResponseDto.of(responseDtos));
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeywordResponseDto> create(@Valid @RequestBody KeywordDto keywordDto) {
        Keyword keyword = service.create(keywordDto);
        URI location = fromPath(KEYWORDS).pathSegment("{id}")
                .buildAndExpand(keyword.getId()).toUri();
        KeywordResponseDto responseDto = LibraryMapper.INSTANCE.toKeywordResponseDto(keyword);

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeywordResponseDto> read(@PathVariable Long id) {
        Keyword keyword = service.read(id);
        KeywordResponseDto responseDto = LibraryMapper.INSTANCE.toKeywordResponseDto(keyword);

        return ResponseEntity.ok().body(responseDto);
    }

    @PutMapping(value = "/{id}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeywordResponseDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody KeywordDto keywordDto) {
        Keyword keyword = service.update(id, keywordDto);
        KeywordResponseDto responseDto = LibraryMapper.INSTANCE.toKeywordResponseDto(keyword);
        return ResponseEntity.ok().body(responseDto);
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
