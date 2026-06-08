package com._s3k.runsync.domain.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com._s3k.runsync.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	@Query("SELECT u FROM User u WHERE u.id <> :userId AND u.isDeleted = false " +
			"AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%')) " +
			"AND (:cursor IS NULL OR u.id > :cursor) ORDER BY u.id ASC")
	List<User> searchByNickname(@Param("userId") Long userId, @Param("nickname") String nickname,
								@Param("cursor") Long cursor, Pageable pageable);
}
