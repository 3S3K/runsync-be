package com._s3k.runsync.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._s3k.runsync.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId);

	/**
	 * 닉네임 중복 검사 (본인 제외)
	 */
	boolean existsByNicknameAndIdNot(String nickname, Long id);

    boolean existsByNickname(String nickname);
}
