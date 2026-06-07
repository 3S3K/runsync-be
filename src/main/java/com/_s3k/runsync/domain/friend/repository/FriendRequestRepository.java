package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySender_IdAndReceiver_IdAndStatus(Long senderId, Long receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    Optional<FriendRequest> findByIdAndReceiver_Id(Long id, Long receiverId);

    @EntityGraph(attributePaths = {"sender"})
    List<FriendRequest> findByReceiver_IdAndStatusOrderByCreatedAtDesc(Long receiverId, FriendRequestStatus status);

    @EntityGraph(attributePaths = {"receiver"})
    List<FriendRequest> findBySender_IdAndStatusOrderByCreatedAtDesc(Long senderId, FriendRequestStatus status);
}
