package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySender_IdAndReceiver_IdAndStatus(Long senderId, Long receiverId, FriendRequestStatus status);
}
