package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TranslatePostResponse {
    private String translatedContent;
    private String translatedTitle;
    private boolean translateLanguageSet;
    private String extraComment;
    private List<String> pollOptionsTranslation;
}
