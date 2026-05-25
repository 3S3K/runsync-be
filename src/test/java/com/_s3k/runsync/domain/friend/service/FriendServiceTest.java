package com._s3k.runsync.domain.friend.service;

import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.ActivityStatus;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private RunningSessionRepository runningSessionRepository;

    @Mock
    private RunRecordRepository runRecordRepository;

    @Test
    @DisplayName("친구가 없으면 빈 리스트 반환")
    void getFriendsByUserId_noFriends() {
        // given
        given(friendshipRepository.findFriendsByUserId(1L)).willReturn(List.of());

        // when
        List<FriendListRes> result = friendService.getFriendsByUserId(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ACTIVE 세션이 있는 친구는 RUNNING 상태, lastActiveAt은 null")
    void getFriendsByUserId_running() {
        // given
        User friend = mock(User.class);
        given(friend.getId()).willReturn(2L);
        given(friendshipRepository.findFriendsByUserId(1L)).willReturn(List.of(friend));

        RunningSession activeSession = mock(RunningSession.class);
        given(activeSession.getUserId()).willReturn(2L);
        given(runningSessionRepository.findByUserIdInAndStatus(List.of(2L), RunningSessionStatus.ACTIVE))
                .willReturn(List.of(activeSession));
        given(runRecordRepository.findLatestByUserIds(List.of(2L))).willReturn(List.of());

        // when
        List<FriendListRes> result = friendService.getFriendsByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityStatus()).isEqualTo(ActivityStatus.RUNNING.name());
        assertThat(result.get(0).getLastActiveAt()).isNull();
    }

    @Test
    @DisplayName("ACTIVE 세션이 없고 러닝 기록이 있으면 OFFLINE 상태, lastActiveAt은 마지막 러닝 종료 시각")
    void getFriendsByUserId_offlineWithRecord() {
        // given
        User friend = mock(User.class);
        given(friend.getId()).willReturn(2L);
        given(friendshipRepository.findFriendsByUserId(1L)).willReturn(List.of(friend));

        given(runningSessionRepository.findByUserIdInAndStatus(List.of(2L), RunningSessionStatus.ACTIVE))
                .willReturn(List.of());

        RunRecord lastRecord = mock(RunRecord.class);
        LocalDateTime lastActiveAt = LocalDateTime.of(2026, 5, 25, 10, 30);
        given(lastRecord.getUserId()).willReturn(2L);
        given(lastRecord.getLastActiveAt()).willReturn(lastActiveAt);
        given(runRecordRepository.findLatestByUserIds(List.of(2L))).willReturn(List.of(lastRecord));

        // when
        List<FriendListRes> result = friendService.getFriendsByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityStatus()).isEqualTo(ActivityStatus.OFFLINE.name());
        assertThat(result.get(0).getLastActiveAt()).isEqualTo(lastActiveAt);
    }

    @Test
    @DisplayName("ACTIVE 세션도 없고 러닝 기록도 없으면 OFFLINE 상태, lastActiveAt은 null")
    void getFriendsByUserId_offlineNoRecord() {
        // given
        User friend = mock(User.class);
        given(friend.getId()).willReturn(2L);
        given(friendshipRepository.findFriendsByUserId(1L)).willReturn(List.of(friend));

        given(runningSessionRepository.findByUserIdInAndStatus(List.of(2L), RunningSessionStatus.ACTIVE))
                .willReturn(List.of());
        given(runRecordRepository.findLatestByUserIds(List.of(2L))).willReturn(List.of());

        // when
        List<FriendListRes> result = friendService.getFriendsByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityStatus()).isEqualTo(ActivityStatus.OFFLINE.name());
        assertThat(result.get(0).getLastActiveAt()).isNull();
    }

    @Test
    @DisplayName("정렬 - RUNNING 먼저, OFFLINE은 lastActiveAt 최신순, 기록 없는 OFFLINE은 맨 뒤")
    void getFriendsByUserId_sortOrder() {
        // given
        User running = mock(User.class);
        given(running.getId()).willReturn(1L);
        User offlineRecent = mock(User.class);
        given(offlineRecent.getId()).willReturn(2L);
        User offlineOld = mock(User.class);
        given(offlineOld.getId()).willReturn(3L);
        User offlineNoRecord = mock(User.class);
        given(offlineNoRecord.getId()).willReturn(4L);

        given(friendshipRepository.findFriendsByUserId(1L))
                .willReturn(List.of(offlineNoRecord, offlineOld, offlineRecent, running)); // 의도적으로 섞인 순서

        RunningSession activeSession = mock(RunningSession.class);
        given(activeSession.getUserId()).willReturn(1L);
        given(runningSessionRepository.findByUserIdInAndStatus(anyList(), eq(RunningSessionStatus.ACTIVE)))
                .willReturn(List.of(activeSession));

        RunRecord recentRecord = mock(RunRecord.class);
        given(recentRecord.getUserId()).willReturn(2L);
        given(recentRecord.getLastActiveAt()).willReturn(LocalDateTime.of(2026, 5, 25, 10, 30));

        RunRecord oldRecord = mock(RunRecord.class);
        given(oldRecord.getUserId()).willReturn(3L);
        given(oldRecord.getLastActiveAt()).willReturn(LocalDateTime.of(2026, 5, 20, 8, 0));

        given(runRecordRepository.findLatestByUserIds(anyList()))
                .willReturn(List.of(recentRecord, oldRecord));

        // when
        List<FriendListRes> result = friendService.getFriendsByUserId(1L);

        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getFriendUserId()).isEqualTo(1L); // RUNNING
        assertThat(result.get(1).getFriendUserId()).isEqualTo(2L); // OFFLINE, 최신 기록
        assertThat(result.get(2).getFriendUserId()).isEqualTo(3L); // OFFLINE, 오래된 기록
        assertThat(result.get(3).getFriendUserId()).isEqualTo(4L); // OFFLINE, 기록 없음
    }
}
