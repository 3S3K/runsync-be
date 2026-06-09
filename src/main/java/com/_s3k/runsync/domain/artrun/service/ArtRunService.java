package com._s3k.runsync.domain.artrun.service;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.ArtRunStatusUpdateReq;
import com._s3k.runsync.domain.artrun.dto.request.CoordinateReq;
import com._s3k.runsync.domain.artrun.dto.request.MeetingPlaceReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunCreateRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunDetailRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunResultParticipantRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunResultRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunScrollRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunStatusRes;
import com._s3k.runsync.domain.artrun.dto.response.ParticipantRes;
import com._s3k.runsync.domain.artrun.event.ArtRunParticipantLeftEvent;
import com._s3k.runsync.domain.artrun.event.ArtRunSessionClosedEvent;
import com._s3k.runsync.domain.artrun.exception.ArtRunErrorCode;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
import com._s3k.runsync.domain.artrun.repository.ParticipantCountProjection;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import com._s3k.runsync.global.common.ScrollPaginationCollection;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtRunService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final UserRepository userRepository;
    private final ArtRunSessionRepository artRunSessionRepository;
    private final ArtRunParticipantRepository artRunParticipantRepository;
    private final RunRecordRepository runRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ArtRunCreateRes createArtRun(Long userId, ArtRunCreateReq request) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        MeetingPlaceReq place = request.getMeetingPlace();
        ArtRunSession session = ArtRunSession.of(
                host,
                request.getTitle(),
                request.getCapacity(),
                request.getMeetingTime(),
                place.getName(),
                toPoint(place.getLatitude(), place.getLongitude()),
                toLineString(request.getCoordinates())
        );
        artRunSessionRepository.save(session);
        artRunParticipantRepository.save(ArtRunParticipant.of(session, host));

        return ArtRunCreateRes.of(session);
    }

    @Transactional(readOnly = true)
    public ArtRunScrollRes getAllArtRuns(ArtRunStatus status, Long cursor, int size) {
        List<ArtRunSession> sessions = cursor == null
                ? artRunSessionRepository.findByStatusOrderByIdDesc(status, PageRequest.of(0, size + 1))
                : artRunSessionRepository.findByStatusAndIdLessThanOrderByIdDesc(status, cursor, PageRequest.of(0, size + 1));

        Map<Long, Long> countMap = countParticipantsBySession(sessions);
        List<ArtRunRes> sessionResList = sessions.stream()
                .map(session -> ArtRunRes.of(session, countMap.getOrDefault(session.getId(), 0L).intValue()))
                .toList();

        return ArtRunScrollRes.of(ScrollPaginationCollection.of(sessionResList, size));
    }

    @Transactional(readOnly = true)
    public ArtRunDetailRes getArtRunById(Long sessionId) {
        ArtRunSession session = artRunSessionRepository.findByIdWithHost(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));

        List<ParticipantRes> participants = artRunParticipantRepository.findByArtRunSessionIdWithUser(sessionId).stream()
                .map(ParticipantRes::of)
                .toList();

        return ArtRunDetailRes.of(session, participants.size(), participants);
    }

    @Transactional(readOnly = true)
    public ArtRunResultRes getArtRunResultBySessionId(Long userId, Long sessionId) {
        ArtRunSession session = artRunSessionRepository.findByIdWithHost(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));

        if (!session.isHost(userId)
                && !artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(sessionId, userId)) {
            throw new GlobalException(ArtRunErrorCode.RESULT_ACCESS_DENIED);
        }

        Map<Long, RunRecord> recordByUserId = runRecordRepository.findByArtRunSessionIdWithPaths(sessionId).stream()
                .collect(Collectors.toMap(RunRecord::getUserId, Function.identity(), (a, b) -> a));

        List<ArtRunResultParticipantRes> participants = artRunParticipantRepository.findByArtRunSessionIdWithUser(sessionId).stream()
                .map(p -> ArtRunResultParticipantRes.of(p, recordByUserId.get(p.getUser().getId())))
                .toList();

        return ArtRunResultRes.of(session, participants);
    }

    @Transactional
    public void joinArtRun(Long userId, Long sessionId) {
        ArtRunSession session = artRunSessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));

        if (!session.isRecruiting()) {
            throw new GlobalException(ArtRunErrorCode.NOT_RECRUITING);
        }
        if (artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(sessionId, userId)) {
            throw new GlobalException(ArtRunErrorCode.ALREADY_JOINED);
        }
        if (session.isFull(artRunParticipantRepository.countByArtRunSession_Id(sessionId))) {
            throw new GlobalException(ArtRunErrorCode.SESSION_FULL);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));
        artRunParticipantRepository.save(ArtRunParticipant.of(session, user));
    }

    @Transactional
    public void leaveArtRun(Long userId, Long sessionId) {
        ArtRunSession session = artRunSessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));

        if (!session.isRecruiting()) {
            throw new GlobalException(ArtRunErrorCode.NOT_RECRUITING);
        }
        if (session.isHost(userId)) {
            throw new GlobalException(ArtRunErrorCode.HOST_CANNOT_LEAVE);
        }

        ArtRunParticipant participant = artRunParticipantRepository.findByArtRunSession_IdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.NOT_PARTICIPANT));
        artRunParticipantRepository.delete(participant);

        eventPublisher.publishEvent(new ArtRunParticipantLeftEvent(sessionId, userId));
    }

    @Transactional
    public ArtRunStatusRes updateArtRunStatus(Long userId, Long sessionId, ArtRunStatusUpdateReq request) {
        ArtRunSession session = artRunSessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));
        session.validateHost(userId);

        switch (request.getStatus()) {
            case IN_PROGRESS -> session.start();
            case COMPLETED -> session.complete();
            default -> throw new GlobalException(ArtRunErrorCode.INVALID_STATUS_TRANSITION);
        }

        return ArtRunStatusRes.of(session);
    }

    @Transactional
    public void deleteArtRun(Long userId, Long sessionId) {
        ArtRunSession session = artRunSessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new GlobalException(ArtRunErrorCode.SESSION_NOT_FOUND));
        session.validateHost(userId);

        if (!session.isRecruiting()) {
            throw new GlobalException(ArtRunErrorCode.NOT_RECRUITING);
        }

        artRunParticipantRepository.deleteByArtRunSession_Id(sessionId);
        artRunSessionRepository.delete(session);

        eventPublisher.publishEvent(new ArtRunSessionClosedEvent(sessionId));
    }

    private Map<Long, Long> countParticipantsBySession(List<ArtRunSession> sessions) {
        if (sessions.isEmpty()) {
            return Map.of();
        }
        List<Long> sessionIds = sessions.stream().map(ArtRunSession::getId).toList();
        return artRunParticipantRepository.countBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(ParticipantCountProjection::getSessionId, ParticipantCountProjection::getParticipantCount));
    }

    private Point toPoint(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private LineString toLineString(List<CoordinateReq> coordinates) {
        Coordinate[] coords = coordinates.stream()
                .map(c -> new Coordinate(c.getLongitude(), c.getLatitude()))
                .toArray(Coordinate[]::new);
        return GEOMETRY_FACTORY.createLineString(coords);
    }
}
