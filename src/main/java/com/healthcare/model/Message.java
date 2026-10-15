package com.healthcare.model;

import com.healthcare.util.Fmt;
import java.time.LocalDateTime;

public class Message {
    private int id, senderId, receiverId;
    private String body, senderName, receiverName;
    private LocalDateTime sentAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public String getSentAtText() { return Fmt.dateTime(sentAt); }
}
