package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.SendMessageResponse;

public interface ChatMessageService {
    SendMessageResponse sendMessage(SendMessageRequest sendMessageRequest);
}
