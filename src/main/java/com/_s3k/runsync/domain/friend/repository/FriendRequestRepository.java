package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiver_IdAndStatus(Long receiverId, FriendRequestStatus status);
    List<FriendRequest> findBySender_IdAndStatus(Long senderId, FriendRequestStatus status);
}
