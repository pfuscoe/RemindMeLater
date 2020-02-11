package patrick.fuscoe.remindmelater.models;

/**
 * Data model for friends.
 */

public class Friend implements Comparable<Friend> {

    private String friendNickname;
    private String friendId;
    private String friendEmail;

    public Friend(String friendId, String friendNickname)
    {
        this.friendId = friendId;
        this.friendNickname = friendNickname;
    }
    
    @Override
    public int compareTo(Friend o) {
        return this.getFriendNickname().compareTo(o.getFriendNickname());
    }

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
