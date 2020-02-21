package patrick.fuscoe.remindmelater.models;

/**
 * Data model for firebase messages.
 */
public class FirebaseMessage {

    private String messageType;
    private String friendEmail;
    private String senderId;
    private String senderDisplayName;
    private String senderDeviceToken;


    public FirebaseMessage() {

    }

    public FirebaseMessage(String messageType, String friendEmail, String senderId,
                           String senderDisplayName, String senderDeviceToken)
    {
        this.messageType = messageType;
        this.friendEmail = friendEmail;
        this.senderId = senderId;
        this.senderDisplayName = senderDisplayName;
        this.senderDeviceToken = senderDeviceToken;
    }


    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public String getSenderDeviceToken() {
        return senderDeviceToken;
    }

    public void setSenderDeviceToken(String senderDeviceToken) {
        this.senderDeviceToken = senderDeviceToken;
    }
}
