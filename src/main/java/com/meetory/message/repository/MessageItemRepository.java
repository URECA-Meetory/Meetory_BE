package com.meetory.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meetory.message.entity.MessageItem;

public interface MessageItemRepository extends JpaRepository<MessageItem, Long> {
}
