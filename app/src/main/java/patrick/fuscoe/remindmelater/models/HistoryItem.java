package patrick.fuscoe.remindmelater.models;

import java.util.Date;

public class HistoryItem {

    private Date dateOfAction;
    private String actionTaken;

    /** History Action Strings **/
    public static final String DONE = "Done";
    public static final String SNOOZE = "Snooze";
    public static final String DISMISS = "Dismiss";


    public HistoryItem(Date dateOfAction, String actionTaken)
    {
        this.dateOfAction = dateOfAction;
        this.actionTaken = actionTaken;
    }


    /** Getters **/
    public Date getDateOfAction() {
        return dateOfAction;
    }

    public String getActionTaken() {
        return actionTaken;
    }


    /** Setters **/
    public void setDateOfAction(Date dateOfAction) {
        this.dateOfAction = dateOfAction;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

}
