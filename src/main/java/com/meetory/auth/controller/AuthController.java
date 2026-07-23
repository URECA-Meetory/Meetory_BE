package com.meetory.auth.controller;

import com.meetory.auth.dto.EmailCheckResponse;
import com.meetory.auth.dto.LoginRequest;
import com.meetory.auth.dto.LoginResponse;
import com.meetory.auth.dto.SignupRequest;
import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.service.AuthService;
import com.meetory.common.ApiResponse;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(new EmailCheckResponse(available)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = resolveToken(request);

        if (token == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다", null));
    }
    
    private String resolveToken(HttpServletRequest request) {
    	   String bearer = request.getHeader("Authorization");
    	   if (bearer != null && bearer.startsWith("Bearer ")) {
    		   return bearer.substring(7);
    	   }
    	    return null;
    }
}