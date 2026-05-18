package com._s3k.runsync.domain.users.service;

// Spring 관련 import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lombok
import lombok.RequiredArgsConstructor;

// 프로젝트 내부 클래스들 (🌟 이 import들이 GlobalException, UserErrorCode 에러를 해결합니다!)
import com._s3k.runsync.entity.User;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.dto.request.UserUpdateRequest;
import com._s3k.runsync.domain.users.dto.response.UserInfoResponse;
import com._s3k.runsync.domain.users.dto.response.UserUpdateResponse;
import com._s3k.runsync.global.exception.GlobalException;

@Service
@RequiredArgsConstructor

public class UserService {

    // 🌟 이 필드 선언이 userRepository 에러를 해결합니다!
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));

        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserUpdateResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));

        // 사용자 정보 업데이트 로직
        // user.update(request); // 실제 업데이트 메서드는 User 엔티티에 구현되어 있을 것입니다

        // 닉네임 중복 체크 (실제 메서드명은 UserRepository 구현에 따라 다를 수 있습니다)
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new GlobalException(UserErrorCode.USER_002);
        }

        return UserUpdateResponse.from(user);
    }
}