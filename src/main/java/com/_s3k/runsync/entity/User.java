package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity{

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "birth_date", nullable = false)
    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String nickname, String providerId, Provider provider, String profileImage, Role role, LocalDateTime birthDate, Gender gender) {
        this.nickname = nickname;
        this.providerId = providerId;
        this.provider = provider;
        this.profileImage = profileImage;
        this.role = role;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isDeleted = false;
    }

}


