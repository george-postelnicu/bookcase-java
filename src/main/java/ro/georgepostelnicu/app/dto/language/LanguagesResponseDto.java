package ro.georgepostelnicu.app.dto.language;

import ro.georgepostelnicu.app.dto.ListResultDto;

import java.util.List;

public class LanguagesResponseDto extends ListResultDto<LanguageResponseDto> {
    public static LanguagesResponseDto of(List<LanguageResponseDto> elements) {
        LanguagesResponseDto result = new LanguagesResponseDto();
        result.setElements(elements);
        return result;
    }
}
