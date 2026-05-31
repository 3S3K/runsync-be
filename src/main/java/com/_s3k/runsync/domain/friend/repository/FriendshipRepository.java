package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.Friendship;
import com._s3k.runsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f.friend FROM Friendship f WHERE f.user.id = :userId AND f.friend.isDeleted = false")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
}
