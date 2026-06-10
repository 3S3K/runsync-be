package com._s3k.runsync.domain.users.service;

import com._s3k.runsync.domain.friend.repository.FriendRequestRepository;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.MonthlyStatsProjection;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.dto.request.UserUpdateReq;
import com._s3k.runsync.domain.users.dto.response.UserSearchScrollRes;
import com._s3k.runsync.domain.users.dto.response.UserSummaryRes;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.Role;
import com._s3k.runsync.entity.enums.UserRelation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RunRecordRepository runRecordRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-10T00:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("닉네임 검색 성공 - 친구/보낸요청/받은요청/없음 관계가 정확히 매핑된다")
    void searchUsersByNickname_relationMapping() {
        // given
        Long me = 1L;
        User friend = user(10L, "hw_friend");
        User sent = user(11L, "hw_sent");
        User received = user(12L, "hw_received");
        User none = user(13L, "hw_none");

        given(userRepository.searchByNickname(eq(me), eq("hw"), any(), any(Pageable.class)))
                .willReturn(List.of(friend, sent, received, none));
        given(friendshipRepository.findFriendIdsByUserIdAndFriendIdIn(eq(me), anyList()))
                .willReturn(List.of(10L));
        given(friendRequestRepository.findReceiverIdsBySenderAndStatus(eq(me), anyList(), eq(FriendRequestStatus.PENDING)))
                .willReturn(List.of(11L));
        given(friendRequestRepository.findSenderIdsByReceiverAndStatus(eq(me), anyList(), eq(FriendRequestStatus.PENDING)))
                .willReturn(List.of(12L));

        // when
        UserSearchScrollRes result = userService.searchUsersByNickname(me, "hw", null, 10);

        // then
        assertThat(result.getUsers()).hasSize(4);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getUsers().get(0).getRelation()).isEqualTo(UserRelation.FRIEND);
        assertThat(result.getUsers().get(1).getRelation()).isEqualTo(UserRelation.REQUEST_SENT);
        assertThat(result.getUsers().get(2).getRelation()).isEqualTo(UserRelation.REQUEST_RECEIVED);
        assertThat(result.getUsers().get(3).getRelation()).isEqualTo(UserRelation.NONE);
    }

    @Test
    @DisplayName("닉네임 검색 결과 없음 - 빈 목록 반환, 관계 조회를 호출하지 않는다")
    void searchUsersByNickname_empty() {
        // given
        given(userRepository.searchByNickname(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of());

        // when
        UserSearchScrollRes result = userService.searchUsersByNickname(1L, "nobody", null, 10);

        // then
        assertThat(result.getUsers()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        verify(friendshipRepository, never()).findFriendIdsByUserIdAndFriendIdIn(any(), anyList());
    }

    @Test
    @DisplayName("닉네임 검색 다음 페이지 존재 - size+1건 조회 시 hasNext=true, nextCursor=현재 페이지 마지막 id")
    void searchUsersByNickname_hasNext() {
        // given size=2 인데 3건(size+1) 반환
        Long me = 1L;
        given(userRepository.searchByNickname(eq(me), eq("hw"), any(), any(Pageable.class)))
                .willReturn(List.of(user(1L, "hw1"), user(2L, "hw2"), user(3L, "hw3")));
        given(friendshipRepository.findFriendIdsByUserIdAndFriendIdIn(eq(me), anyList())).willReturn(List.of());
        given(friendRequestRepository.findReceiverIdsBySenderAndStatus(eq(me), anyList(), any())).willReturn(List.of());
        given(friendRequestRepository.findSenderIdsByReceiverAndStatus(eq(me), anyList(), any())).willReturn(List.of());

        // when
        UserSearchScrollRes result = userService.searchUsersByNickname(me, "hw", null, 2);

        // then
        assertThat(result.getUsers()).hasSize(2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.getUsers().get(0).getId()).isEqualTo(1L);
        assertThat(result.getUsers().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("TMP_USER가 성별·생년월일을 채워 프로필이 완성되면 USER로 승격된다")
    void updateMyInfo_promotesWhenProfileComplete() {
        // given - 카카오 가입으로 닉네임은 이미 있고 성별/생년월일만 비어있는 TMP_USER
        Long userId = 1L;
        User tmpUser = User.createTmpUser(Provider.KAKAO, "kakao1", "현우", "img1");
        ReflectionTestUtils.setField(tmpUser, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(tmpUser));

        UserUpdateReq request = new UserUpdateReq();
        ReflectionTestUtils.setField(request, "gender", Gender.MALE);
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(2000, 1, 1));

        // when
        userService.updateMyInfo(userId, request);

        // then
        assertThat(tmpUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("프로필이 미완성(생년월일 누락)이면 TMP_USER로 유지된다")
    void updateMyInfo_staysTmpWhenIncomplete() {
        // given
        Long userId = 1L;
        User tmpUser = User.createTmpUser(Provider.KAKAO, "kakao1", "현우", "img1");
        ReflectionTestUtils.setField(tmpUser, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(tmpUser));

        UserUpdateReq request = new UserUpdateReq();
        ReflectionTestUtils.setField(request, "gender", Gender.MALE);

        // when
        userService.updateMyInfo(userId, request);

        // then
        assertThat(tmpUser.getRole()).isEqualTo(Role.TMP_USER);
    }

    @Test
    @DisplayName("월간 통계 - 평균 페이스(분/km)와 총 시간·목표가 계산되어 반환된다")
    void getUserSummary_computesStats() {
        // given
        Long userId = 1L;
        User user = user(userId, "현우");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        MonthlyStatsProjection stats = mock(MonthlyStatsProjection.class);
        given(stats.getTotalDistance()).willReturn(BigDecimal.valueOf(5.0));
        given(stats.getTotalRunCount()).willReturn(2L);
        given(stats.getTotalDurationSeconds()).willReturn(1500L); // 25분
        given(runRecordRepository.findMonthlyStats(eq(userId), any(), any())).willReturn(stats);

        // when
        UserSummaryRes result = userService.getUserSummary(userId);

        // then
        UserSummaryRes.MonthlyStats monthly = result.getMonthlyStats();
        assertThat(monthly.getTotalDistance()).isEqualTo(5.0);
        assertThat(monthly.getTotalRunCount()).isEqualTo(2);
        assertThat(monthly.getTotalDurationSeconds()).isEqualTo(1500);
        assertThat(monthly.getAveragePace()).isEqualTo(5.0); // 25분 / 5km
        assertThat(monthly.getMonthlyGoalKm()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("월간 통계 - 이번 달 기록이 없으면 평균 페이스는 null이다")
    void getUserSummary_noRecordsAveragePaceNull() {
        // given
        Long userId = 1L;
        User user = user(userId, "현우");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        MonthlyStatsProjection stats = mock(MonthlyStatsProjection.class);
        given(stats.getTotalDistance()).willReturn(BigDecimal.ZERO);
        given(stats.getTotalRunCount()).willReturn(0L);
        given(stats.getTotalDurationSeconds()).willReturn(0L);
        given(runRecordRepository.findMonthlyStats(eq(userId), any(), any())).willReturn(stats);

        // when
        UserSummaryRes result = userService.getUserSummary(userId);

        // then
        assertThat(result.getMonthlyStats().getAveragePace()).isNull();
        assertThat(result.getMonthlyStats().getMonthlyGoalKm()).isEqualTo(50.0);
    }

    private User user(Long id, String nickname) {
        User user = User.createTmpUser(Provider.KAKAO, "kakao" + id, nickname, "img" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
