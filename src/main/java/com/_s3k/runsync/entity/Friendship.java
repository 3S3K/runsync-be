package com._s3k.runsync.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendship")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Builder(access = AccessLevel.PRIVATE)
    private Friendship(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }

    public static Friendship of(User user, User friend) {
        return Friendship.builder()
                .user(user)
                .friend(friend)
                .build();
    }
}
