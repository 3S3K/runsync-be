package com._s3k.runsync.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._s3k.runsync.entity.Token;
import com._s3k.runsync.entity.User;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Optional<Token> findByUser(User user);
	Optional<Token> findByRefreshToken(String refreshToken);
	void deleteByUser(User user);
}
