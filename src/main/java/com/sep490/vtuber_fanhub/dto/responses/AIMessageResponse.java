package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AIMessageResponse {
    private String message;
    private String thought;
}
