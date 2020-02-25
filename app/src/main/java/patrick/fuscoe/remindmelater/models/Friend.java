package patrick.fuscoe.remindmelater.models;

/**
 * Data model for friends.
 */

public class Friend implements Comparable<Friend> {

    private String friendDisplayName;
    private String friendId;
    //private String friendEmail;

    public Friend(String friendId, String friendDisplayName)
    {
        this.friendId = friendId;
        this.friendDisplayName = friendDisplayName;
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

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
