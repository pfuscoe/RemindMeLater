package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.Arrays;

public class UserProfile {

    private String id;
    private String displayName;
    private String[] subscriptions;


    public UserProfile() {

    }

    public UserProfile(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = new String[]{};
    }

    public UserProfile(String id, String displayName, String[] subscriptions)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = subscriptions;
    }


    public void addSubscription(String groupId)
    {
        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(subscriptions));
        tempList.add(groupId);
        subscriptions = tempList.toArray(subscriptions);
    }

    /** Getters and Setters **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(String[] subscriptions) {
        this.subscriptions = subscriptions;
    }
}
