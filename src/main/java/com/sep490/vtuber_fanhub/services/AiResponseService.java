package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.models.User;

public interface AiResponseService {
    void generateAndSendReply(User user, String userMessageContent);
    AIMessageResponse smartChat(String userPrompt, Long userId, Long sessionId);

}
