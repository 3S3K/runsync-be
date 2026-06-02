package com._s3k.runsync.domain.artrun.service;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.CoordinateReq;
import com._s3k.runsync.domain.artrun.dto.request.MeetingPlaceReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunCreateRes;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtRunService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final UserRepository userRepository;
    private final ArtRunSessionRepository artRunSessionRepository;
    private final ArtRunParticipantRepository artRunParticipantRepository;

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
