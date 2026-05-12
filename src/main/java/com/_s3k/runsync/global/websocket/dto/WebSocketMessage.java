package com._s3k.runsync.global.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebSocketMessage<T> {

    private String type;
    private T data;

    public static <T> WebSocketMessage<T> of(String type, T data) {
        WebSocketMessage<T> message = new WebSocketMessage<>();
        message.type = type;
        message.data = data;
        return message;
    }
}
