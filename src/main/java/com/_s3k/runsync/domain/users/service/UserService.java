package com._s3k.runsync.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.dto.request.UserUpdateReq;
import com._s3k.runsync.domain.users.dto.response.UserInfoRes;
import com._s3k.runsync.domain.users.dto.response.UserUpdateRes;
import com._s3k.runsync.global.exception.GlobalException;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoRes getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        return UserInfoRes.of(user);
    }

    @Transactional
    public UserUpdateRes updateMyInfo(Long userId, UserUpdateReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null &&
                userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
            throw new GlobalException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Gender gender = request.getGender() != null ? Gender.valueOf(request.getGender()) : null;
        user.updateInfo(request.getNickname(), request.getProfileImage(), gender, request.getBirthDate());

        return UserUpdateRes.of(user);
    }
}