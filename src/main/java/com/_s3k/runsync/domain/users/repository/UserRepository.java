package com._s3k.runsync.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._s3k.runsync.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId);

	boolean existsByNicknameAndIdNot(String nickname, Long id);
}
