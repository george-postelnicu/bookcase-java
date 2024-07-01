package ro.georgepostelnicu.app.dto.keyword;

import ro.georgepostelnicu.app.dto.ListResultDto;

import java.util.List;

public class KeywordsResponseDto extends ListResultDto<KeywordResponseDto> {
    public static KeywordsResponseDto of(List<KeywordResponseDto> elements) {
        KeywordsResponseDto result = new KeywordsResponseDto();
        result.setElements(elements);
        return result;
    }

}
