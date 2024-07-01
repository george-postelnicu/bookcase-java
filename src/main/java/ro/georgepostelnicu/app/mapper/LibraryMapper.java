package ro.georgepostelnicu.app.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ro.georgepostelnicu.app.dto.author.AuthorDto;
import ro.georgepostelnicu.app.dto.author.AuthorResponseDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordDto;
import ro.georgepostelnicu.app.dto.keyword.KeywordResponseDto;
import ro.georgepostelnicu.app.dto.language.LanguageDto;
import ro.georgepostelnicu.app.dto.language.LanguageResponseDto;
import ro.georgepostelnicu.app.model.Author;
import ro.georgepostelnicu.app.model.Keyword;
import ro.georgepostelnicu.app.model.Language;
import ro.georgepostelnicu.app.util.StringUtil;

@Mapper
public interface LibraryMapper {
    LibraryMapper INSTANCE = Mappers.getMapper(LibraryMapper.class);

    @Mapping(target = "name", source = "name")
    Author toAuthor(AuthorDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateAuthorFromDto(AuthorDto dto, @MappingTarget Author entity);

    AuthorResponseDto toAuthorResponseDto(Author entity);

    @Mapping(target = "name", source = "name")
    Keyword toKeyword(KeywordDto dto);

    @AfterMapping
    default void toKeyword(@MappingTarget Keyword keyword) {
        keyword.setName(StringUtil.splitCapitalizeAndJoin(keyword.getName()));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateKeywordFromDto(KeywordDto dto, @MappingTarget Keyword entity);

    KeywordResponseDto toKeywordResponseDto(Keyword entity);

    @Mapping(target = "name", source = "name")
    Language toLanguage(LanguageDto dto);

    @AfterMapping
    default void toLanguage(@MappingTarget Language language) {
        language.setName(StringUtil.splitCapitalizeAndJoin(language.getName()));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateLanguageFromDto(LanguageDto dto, @MappingTarget Language entity);

    LanguageResponseDto toLanguageResponseDto(Language entity);

}
