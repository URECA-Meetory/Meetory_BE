package com.meetory.team.controller;

import java.util.List;

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
import com.meetory.team.dto.TeamApplyResponse;
import com.meetory.team.dto.TeamCreateRequest;
import com.meetory.team.dto.TeamDetailResponse;
import com.meetory.team.dto.TeamListResponse;
import com.meetory.team.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 잡코리아 채용공고 스타일의 모임 매칭 API
//   GET  /api/teams            : 목록(패널) - 비로그인도 열람 가능
//   GET  /api/teams/{teamId}   : 상세(팝업) - 비로그인도 열람 가능
//   POST /api/teams            : 모임 개설 - 로그인 필요
//   POST /api/teams/{teamId}/apply : 신청하기 - 로그인 필요
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTeam(
            @Valid @RequestBody TeamCreateRequest request,
            Authentication authentication) {
        Long teamId = teamService.createTeam(request, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("모임이 개설되었습니다", teamId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamListResponse>>> getTeamList() {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamList()));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeamDetail(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamDetail(teamId)));
    }

    @PostMapping("/{teamId}/apply")
    public ResponseEntity<ApiResponse<TeamApplyResponse>> applyToTeam(
            @PathVariable Long teamId,
            Authentication authentication) {
        TeamApplyResponse response = teamService.applyToTeam(teamId, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("모임 신청이 완료되었습니다", response));
    }

    // JwtAuthenticationFilter 에서 principal 자리에 userId(Long) 를 그대로 담아두므로
    // Authentication.getPrincipal() 을 Long 으로 캐스팅해서 사용한다.
    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
