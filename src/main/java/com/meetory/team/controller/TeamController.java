package com.meetory.team.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetory.common.ApiResponse;
import com.meetory.team.dto.MyTeamResponse;
import com.meetory.team.dto.TeamApplicationResponse;
import com.meetory.team.dto.TeamApplyResponse;
import com.meetory.team.dto.TeamCreateRequest;
import com.meetory.team.dto.TeamDetailResponse;
import com.meetory.team.dto.TeamListResponse;
import com.meetory.team.dto.TeamMemberResponse;
import com.meetory.team.service.TeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 잡코리아 채용공고 스타일의 모임 매칭 API
//   GET  /api/teams                                   : 목록(패널) - 비로그인도 열람 가능
//   GET  /api/teams/{teamId}                          : 상세(팝업) - 비로그인도 열람 가능
//   POST /api/teams                                   : 모임 개설 - 로그인 필요
//   POST /api/teams/{teamId}/apply                    : 신청하기 - 로그인 필요
//   GET  /api/teams/{teamId}/members                  : 현재 팀 멤버(승인된 멤버) 목록 - 비로그인도 열람 가능
//   GET  /api/teams/{teamId}/applications              : 대기중 신청 목록 - 리더 본인만
//   POST /api/teams/{teamId}/applications/{memberId}/approve : 신청 수락 - 리더 본인만
//   POST /api/teams/{teamId}/applications/{memberId}/reject  : 신청 거절 - 리더 본인만
//
// ※ @PathVariable 은 컴파일 시 -parameters 플래그가 없으면 파라미터 이름을 못 읽어와서
//    "Name for argument of type [java.lang.Long] not specified" 예외가 발생할 수 있으므로
//    build.gradle 설정과 별개로 이름을 명시적으로 지정한다.

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

    // 내가 속한 모임 목록 (모임 관리 화면). "/{teamId}" 보다 위에 있어야 우선 매칭됩니다.
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MyTeamResponse>>> getMyTeams(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getMyTeams(currentUserId(authentication))));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeamDetail(
            @PathVariable("teamId") Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamDetail(teamId)));
    }

    @PostMapping("/{teamId}/apply")
    public ResponseEntity<ApiResponse<TeamApplyResponse>> applyToTeam(
            @PathVariable("teamId") Long teamId,
            Authentication authentication) {
        TeamApplyResponse response = teamService.applyToTeam(teamId, currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("모임 신청이 완료되었습니다", response));
    }

    // 모임 탈퇴
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(
            @PathVariable("teamId") Long teamId,
            Authentication authentication) {
        teamService.leaveTeam(teamId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success("모임에서 탈퇴했습니다", null));
    }

    // 현재 팀 멤버(승인된 멤버) 목록 - 리더/멤버만 열람 가능 (로그인 필요)
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
            @PathVariable("teamId") Long teamId,
            Authentication authentication) {
        List<TeamMemberResponse> members =
                teamService.getTeamMembers(teamId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @GetMapping("/{teamId}/applications")
    public ResponseEntity<ApiResponse<List<TeamApplicationResponse>>> getApplications(
            @PathVariable("teamId") Long teamId,
            Authentication authentication) {
        List<TeamApplicationResponse> applications =
                teamService.getApplications(teamId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping("/{teamId}/applications/{memberId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveApplication(
            @PathVariable("teamId") Long teamId,
            @PathVariable("memberId") Long memberId,
            Authentication authentication) {
        teamService.approveApplication(teamId, memberId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success("신청을 수락했습니다", null));
    }

    @PostMapping("/{teamId}/applications/{memberId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectApplication(
            @PathVariable("teamId") Long teamId,
            @PathVariable("memberId") Long memberId,
            Authentication authentication) {
        teamService.rejectApplication(teamId, memberId, currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success("신청을 거절했습니다", null));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}