package com._s3k.runsync.domain.users.service;

public class UserService {

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));
        return UserInfoResponse.from(user);
    }

    /**
     * 내 정보 수정 (닉네임 중복 체크 포함)
     */
    @Transactional
    public UserUpdateResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));

        // 닉네임 변경 요청이 있고, 기존과 다를 때만 중복 검사
        if (request.getNickname() != null
                && !request.getNickname().isBlank()
                && !request.getNickname().equals(user.getNickname())) {

            boolean isDuplicated = userRepository
                    .existsByNicknameAndIdNot(request.getNickname(), userId);

            if (isDuplicated) {
                throw new GlobalException(UserErrorCode.USER_002);
            }
        }

        // JPA 더티 체킹으로 자동 업데이트
        user.updateInfo(request.getNickname(), request.getProfileImage());
        return UserUpdateResponse.from(user);
    }


}
