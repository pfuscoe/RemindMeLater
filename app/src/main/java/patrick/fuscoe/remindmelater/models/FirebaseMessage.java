package patrick.fuscoe.remindmelater.models;

/**
 * Data model for firebase messages.
 */
public class FirebaseMessage {

    private String messageType;
    private String actionType;
    private String friendEmail;
    private String senderId;
    private String senderDisplayName;
    private String senderDeviceToken;
    private String toDoGroupId;
    private String toDoGroupTitle;


    public FirebaseMessage() {

    }

    public FirebaseMessage(String messageType, String actionType, String friendEmail,
                           String senderId, String senderDisplayName, String senderDeviceToken,
                           String toDoGroupId, String toDoGroupTitle)
    {
        this.messageType = messageType;
        this.actionType = actionType;
        this.friendEmail = friendEmail;
        this.senderId = senderId;
        this.senderDisplayName = senderDisplayName;
        this.senderDeviceToken = senderDeviceToken;
        this.toDoGroupId = toDoGroupId;
        this.toDoGroupTitle = toDoGroupTitle;
    }


    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
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

    public String getToDoGroupId() {
        return toDoGroupId;
    }

    public void setToDoGroupId(String toDoGroupId) {
        this.toDoGroupId = toDoGroupId;
    }

    public String getToDoGroupTitle() {
        return toDoGroupTitle;
    }

    public void setToDoGroupTitle(String toDoGroupTitle) {
        this.toDoGroupTitle = toDoGroupTitle;
    }
}
