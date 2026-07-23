package com.meetory.user;


import com.meetory.auth.entity.TokenBlacklist;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.user.dto.AccountDeleteRequest;
import com.meetory.user.dto.PasswordUpdateRequest;
import com.meetory.user.dto.ProfileResponse;
import com.meetory.user.dto.ProfileUpdateRequest;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {


	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenBlacklistRepository tokenBlacklistRepository;


	
	// ====== 내 프로필 조회 ======
    public ProfileResponse getProfile(Long userId) {
    	    User user = findUserById(userId);
    	    return ProfileResponse.from(user);
    }
    
    // ====== 닉네임 변경 ======
    @Transactional
    public ProfileResponse updateNickname(Long userId, ProfileUpdateRequest request) {
    	    User user = findUserById(userId);
    	    user.updateNickname(request.nickname());
    	    return ProfileResponse.from(user);
    }
    
    // ====== 비밀번호 변경 ======
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
    	    User user = findUserById(userId);
    	    
    	    //현재 비밀번호 확인
    	    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
    	    	    throw new CustomException(ErrorCode.INVALID_PASSWORD);
    	    }
    	    
    	    //기존과 동일한 비밀번호로 변경 시도하는거 방지
    	    if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
    	    	    throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
    	    }
    	    
    	    user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }
    
    // 계정 삭제
    @Transactional
    public void deleteAccount(Long userId, AccountDeleteRequest request, String token) {
    	    User user = findUserById(userId);
    	    
    	    // 본인 확인을 위해 비밀번호 재검증
    	    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
    	       	throw new CustomException(ErrorCode.INVALID_PASSWORD);
    	    }
    	    
    	    // 현재 사용 중인 토큰도 즉시 무효화 (블랙리스트 등록)
    	    if (token != null && jwtTokenProvider.validateToken(token)
    	    		   && !tokenBlacklistRepository.existsByToken(token)) {
    	       	
    	    	    LocalDateTime expiredAt = jwtTokenProvider.getExpiration(token);
    	    	    tokenBlacklistRepository.save(
    	    	    		TokenBlacklist.builder()
    	    	    		        .token(token)
    	    	    		        .expiredAt(expiredAt)
    	    	    		        .build()
    	    	    );
    	    }
    	    
    	    // User 삭제 시 연관 team/member/message 데이터는
    	    // 서비스 레이어 또는 DB 제약에 따라 별도 처리 필요
    	    userRepository.delete(user);
    }
    
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
        		     .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
    
}
