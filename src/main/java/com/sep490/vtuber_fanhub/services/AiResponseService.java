package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;

public interface AiResponseService {
    void generateAndSendReply(User user, String userMessageContent);
    String smartChat(String userPrompt, Long userId, Long sessionId);

}
