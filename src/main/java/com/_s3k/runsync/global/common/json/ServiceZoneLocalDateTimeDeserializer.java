package com._s3k.runsync.global.common.json;

import com._s3k.runsync.global.config.ClockConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * 요청으로 받은 시각을 서비스 시간대(Asia/Seoul)의 LocalDateTime 으로 바꾼다.
 * - 시간대가 붙은 값("...Z", "...+09:00")은 같은 순간의 서비스 시간대 시각으로 변환한다.
 * - 시간대가 없는 값은 이미 서비스 시간대 시각으로 보고 그대로 쓴다.
 * 기본 역직렬화는 "Z"를 버리고 숫자만 읽어, UTC 로 보낸 시각이 9시간 어긋난 채 저장된다.
 */
public class ServiceZoneLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = parser.getValueAsString();
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.trim();
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ClockConfig.SERVICE_ZONE).toLocalDateTime();
        } catch (DateTimeParseException offsetMissing) {
            // 시간대 표기가 없는 경우
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return (LocalDateTime) context.handleWeirdStringValue(
                    LocalDateTime.class, value, "ISO-8601 형식의 시각이어야 합니다");
        }
    }
}
