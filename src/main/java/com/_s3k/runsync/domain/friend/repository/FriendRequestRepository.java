package com._s3k.runsync.domain.friend.repository;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT fr.receiver.id FROM FriendRequest fr " +
            "WHERE fr.sender.id = :userId AND fr.receiver.id IN :targetIds AND fr.status = :status")
    List<Long> findReceiverIdsBySenderAndStatus(@Param("userId") Long userId,
                                                @Param("targetIds") List<Long> targetIds,
                                                @Param("status") FriendRequestStatus status);

    @Query("SELECT fr.sender.id FROM FriendRequest fr " +
            "WHERE fr.receiver.id = :userId AND fr.sender.id IN :targetIds AND fr.status = :status")
    List<Long> findSenderIdsByReceiverAndStatus(@Param("userId") Long userId,
                                                @Param("targetIds") List<Long> targetIds,
                                                @Param("status") FriendRequestStatus status);
}
