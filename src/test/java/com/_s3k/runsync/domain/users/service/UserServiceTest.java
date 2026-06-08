package com._s3k.runsync.domain.users.service;

import com._s3k.runsync.domain.friend.repository.FriendRequestRepository;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.dto.response.UserSearchScrollRes;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.UserRelation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

    @Mock
    private Clock clock;

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

    private User user(Long id, String nickname) {
        User user = User.createTmpUser(Provider.KAKAO, "kakao" + id, nickname, "img" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
