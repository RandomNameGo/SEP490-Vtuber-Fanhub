package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslatePostResponse {
    private String translatedText;
    private boolean translate_language_set;
    private String extraComment;
}
