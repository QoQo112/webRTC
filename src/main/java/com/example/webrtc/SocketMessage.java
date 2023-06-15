package com.example.webrtc;

import lombok.Data;

@Data
public class SocketMessage {
    private int type;
    private String sender;
    private String receiver;
    private String message;
}
