package com.meetory.auth.repository;

import com.meetory.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

// 블랙리스트 CRUD용 Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    // 로그인 필터에서 "이 토큰이 이미 로그아웃 처리됐는지" 확인할 때 사용
    boolean existsByToken(String token);
}