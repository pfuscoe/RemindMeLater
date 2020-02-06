package patrick.fuscoe.remindmelater.models;

/**
 * Data model for friends.
 */

public class Friend implements Comparable<Friend> {

    private String friendDisplayName;
    private String friendUserId;
    private String friendEmail;

    public Friend(String friendDisplayName, String friendUserId)
    {
        this.friendDisplayName = friendDisplayName;
        this.friendUserId = friendUserId;
    }
    
    @Override
    public int compareTo(Friend o) {
        return this.getFriendDisplayName().compareTo(o.getFriendDisplayName());
    }

    public String getFriendDisplayName() {
        return friendDisplayName;
    }

    public void setFriendDisplayName(String friendDisplayName) {
        this.friendDisplayName = friendDisplayName;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }
}
