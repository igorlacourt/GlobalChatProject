package app.lacourt.globalchatproject.model;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String message;

    public Message(String messageId, String senderId,  String message) {
        this.messageId = messageId;
        this.senderId = senderId;

        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
