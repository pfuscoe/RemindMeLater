package patrick.fuscoe.remindmelater.models;

import java.util.Arrays;

public class User {

    private String id;
    private String displayName;
    private String[] subscriptions;


    public User() {

    }

    public User(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = new String[]{};
    }

    public User(String id, String displayName, String[] subscriptions)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = subscriptions;
    }


    public addSubscription(String id)
    {
        //subscriptions =
    }

}
