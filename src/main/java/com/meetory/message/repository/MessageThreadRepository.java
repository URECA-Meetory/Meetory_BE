package com.meetory.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meetory.message.entity.MessageThread;

public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {
}
