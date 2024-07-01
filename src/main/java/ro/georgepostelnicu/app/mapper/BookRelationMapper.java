package ro.georgepostelnicu.app.mapper;

import org.mapstruct.Mapper;
import ro.georgepostelnicu.app.dto.author.AuthorResponseDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordResponseDto;
import ro.georgepostelnicu.app.dto.language.LanguageResponseDto;
import ro.georgepostelnicu.app.model.Author;
import ro.georgepostelnicu.app.model.Keyword;
import ro.georgepostelnicu.app.model.Language;

import java.util.Set;

@Mapper
public interface BookRelationMapper {
    Set<AuthorResponseDto> mapAuthors(Set<Author> authors);

    Set<KeywordResponseDto> mapKeywords(Set<Keyword> keywords);

    Set<LanguageResponseDto> mapLanguages(Set<Language> languages);
}
