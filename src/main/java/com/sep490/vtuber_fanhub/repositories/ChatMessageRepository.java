package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20BySession_Id(Long id);
}