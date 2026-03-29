package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;

import java.util.List;

public interface ChatMessageService {
    MessageResponse sendMessage(SendMessageRequest sendMessageRequest, String username);
    List<MessageResponse> getAllMessages(String username);
}
