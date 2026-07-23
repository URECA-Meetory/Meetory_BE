package com.meetory.message.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetory.common.ApiResponse;
import com.meetory.message.dto.InboxResponse;
import com.meetory.message.dto.InquiryRequest;
import com.meetory.message.dto.ReplyRequest;
import com.meetory.message.dto.ThreadDetailResponse;
import com.meetory.message.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 모임장 문의(쪽지) API
//   POST /api/messages/teams/{teamId}/inquiry  : 팀 매칭 화면 "문의하기" -> 최초 쪽지 전송 (로그인 필요)
//   POST /api/messages/threads/{threadId}/reply : 채팅형 팝업에서 답장 전송 (로그인 필요, 참여자만)
//   GET  /api/messages/threads                  : 마이페이지 - 내 쪽지함(안읽음/읽음) (로그인 필요)
//   GET  /api/messages/threads/{threadId}        : 쪽지 클릭 -> 채팅형 팝업 데이터 (로그인 필요, 참여자만, 읽음 처리 포함)
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/teams/{teamId}/inquiry")
    public ResponseEntity<ApiResponse<Long>> sendInquiry(
            @PathVariable("teamId") Long teamId,
            @Valid @RequestBody InquiryRequest request,
            Authentication authentication) {
        Long threadId = messageService.sendInquiry(teamId, currentUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("쪽지를 보냈습니다", threadId));
    }

    @PostMapping("/threads/{threadId}/reply")
    public ResponseEntity<ApiResponse<Long>> reply(
            @PathVariable("threadId") Long threadId,
            @Valid @RequestBody ReplyRequest request,
            Authentication authentication) {
        Long messageId = messageService.reply(threadId, currentUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답장을 보냈습니다", messageId));
    }

    @GetMapping("/threads")
    public ResponseEntity<ApiResponse<InboxResponse>> getInbox(Authentication authentication) {
        InboxResponse inbox = messageService.getInbox(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(inbox));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ApiResponse<ThreadDetailResponse>> getThreadDetail(
            @PathVariable("threadId") Long threadId,
            Authentication authentication) {
        ThreadDetailResponse detail = messageService.getThreadDetail(threadId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
