package com.google.cloud.android.speech;

/**
 * Created by 강연우 on 2017-07-03.
 */

public class ChatMessage {
    public boolean left;
    public String message;

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }
}
