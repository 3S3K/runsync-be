package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    @Override
    @EntityGraph(attributePaths = {"sender", "receiver"})
    Optional<FriendRequest> findById(Long id);

    boolean existsBySender_IdAndReceiver_IdAndStatus(Long senderId, Long receiverId, FriendRequestStatus status);

    @EntityGraph(attributePaths = {"sender"})
    List<FriendRequest> findByReceiver_IdAndStatusOrderByCreatedAtDesc(Long receiverId, FriendRequestStatus status);

    @EntityGraph(attributePaths = {"receiver"})
    List<FriendRequest> findBySender_IdAndStatusOrderByCreatedAtDesc(Long senderId, FriendRequestStatus status);
}
