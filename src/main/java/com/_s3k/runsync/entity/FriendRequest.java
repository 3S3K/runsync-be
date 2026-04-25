package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.FriendRequestStatus;
import com._s3k.runsync.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "friend_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendRequestStatus status;

    @Builder(access = PRIVATE)
    private FriendRequest(User sender, User receiver, FriendRequestStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = (status != null) ? status : FriendRequestStatus.PENDING;
    }

}

