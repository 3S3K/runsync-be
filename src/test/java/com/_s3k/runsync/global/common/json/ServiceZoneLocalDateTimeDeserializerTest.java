package com._s3k.runsync.global.common.json;

import com._s3k.runsync.domain.run.dto.request.RunSessionEndReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceZoneLocalDateTimeDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("UTC(Z)로 받은 시각은 같은 순간의 한국 시간으로 바꾼다")
    void deserialize_utcToKorea() throws Exception {
        RunSessionStartReq request = read("{\"startTime\": \"2026-11-27T15:24:22.123Z\"}", RunSessionStartReq.class);

        assertThat(request.getStartTime()).isEqualTo(LocalDateTime.of(2026, 11, 28, 0, 24, 22, 123_000_000));
    }

    @Test
    @DisplayName("+09:00 이 붙은 시각은 숫자가 그대로다")
    void deserialize_koreaOffset() throws Exception {
        RunSessionEndReq request = read("{\"endTime\": \"2026-11-28T07:30:00+09:00\", \"totalDistance\": 5.2}", RunSessionEndReq.class);

        assertThat(request.getEndTime()).isEqualTo(LocalDateTime.of(2026, 11, 28, 7, 30));
    }

    @Test
    @DisplayName("시간대가 없는 시각은 한국 시간으로 보고 그대로 쓴다")
    void deserialize_withoutOffset() throws Exception {
        RunSessionStartReq request = read("{\"startTime\": \"2026-04-10T22:11:00\"}", RunSessionStartReq.class);

        assertThat(request.getStartTime()).isEqualTo(LocalDateTime.of(2026, 4, 10, 22, 11));
    }

    @Test
    @DisplayName("null 은 null 로 둔다 (필수 여부는 @NotNull 이 검증한다)")
    void deserialize_null() throws Exception {
        RunSessionStartReq request = read("{\"startTime\": null}", RunSessionStartReq.class);

        assertThat(request.getStartTime()).isNull();
    }

    @Test
    @DisplayName("시각 형식이 아니면 역직렬화에 실패한다")
    void deserialize_invalid() {
        assertThatThrownBy(() -> read("{\"startTime\": \"어제\"}", RunSessionStartReq.class))
                .isInstanceOf(JsonMappingException.class);
    }

    private <T> T read(String json, Class<T> type) throws Exception {
        return objectMapper.readValue(json, type);
    }
}
