package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, length = 24)
    private String nickname;

    @Column(name = "provider_id", nullable = false)
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
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String nickname, String providerId, Provider provider, String profileImage,
                 Role role, LocalDate birthDate, Gender gender) {
        this.nickname = nickname;
        this.providerId = providerId;
        this.provider = provider;
        this.profileImage = profileImage;
        this.role = role;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isDeleted = false;
    }

    public static User of(String nickname, String providerId, Provider provider,
                          String profileImage, Role role, LocalDate birthDate, Gender gender) {
        return User.builder()
                .nickname(nickname)
                .providerId(providerId)
                .provider(provider)
                .profileImage(profileImage)
                .role(role)
                .birthDate(birthDate)
                .gender(gender)
                .build();
    }
}
