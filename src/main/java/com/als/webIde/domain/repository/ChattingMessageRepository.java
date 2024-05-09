package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.ChattingMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingMessageRepository extends JpaRepository<ChattingMessage, Long> {
}
