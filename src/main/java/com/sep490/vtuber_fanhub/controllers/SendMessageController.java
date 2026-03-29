package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.SendMessageResponse;
import com.sep490.vtuber_fanhub.services.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("vhub/api/v1/message")
@RequiredArgsConstructor
public class SendMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid SendMessageRequest sendMessageRequest){

        return ResponseEntity.ok().body(APIResponse.<SendMessageResponse>builder()
                .success(true)
                .message("Message sent successfully")
                .data(chatMessageService.sendMessage(sendMessageRequest))
                .build()
        );
    }
}
