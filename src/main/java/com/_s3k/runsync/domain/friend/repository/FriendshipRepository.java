package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.Friendship;
import com._s3k.runsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f.friend FROM Friendship f WHERE f.user.id = :userId AND f.friend.isDeleted = false")
    List<User> findFriendsByUserId(@Param("userId") Long userId);

    boolean existsByUser_IdAndFriend_Id(Long userId, Long friendId);

    @Query("SELECT f.friend.id FROM Friendship f WHERE f.user.id = :userId AND f.friend.id IN :targetIds")
    List<Long> findFriendIdsByUserIdAndFriendIdIn(@Param("userId") Long userId, @Param("targetIds") List<Long> targetIds);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE (f.user.id = :userId AND f.friend.id = :friendId) OR (f.user.id = :friendId AND f.friend.id = :userId)")
    void deleteFriendshipBidirectional(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
