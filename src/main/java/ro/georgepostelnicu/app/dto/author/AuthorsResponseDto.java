package ro.georgepostelnicu.app.dto.author;

import ro.georgepostelnicu.app.dto.ListResultDto;

import java.util.List;

public class AuthorsResponseDto extends ListResultDto<AuthorResponseDto> {
    public static AuthorsResponseDto of(List<AuthorResponseDto> elements) {
        AuthorsResponseDto result = new AuthorsResponseDto();
        result.setElements(elements);
        return result;
    }

}
