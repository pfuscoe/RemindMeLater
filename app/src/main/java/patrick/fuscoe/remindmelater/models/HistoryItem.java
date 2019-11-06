package patrick.fuscoe.remindmelater.models;

import java.util.Date;

/**
 * Placeholder class for possible future feature of adding reminder history. This may not be
 * needed as it could be implemented with simply a HashMap of Strings.
 */
public class HistoryItem {

    private Date dateOfAction;
    private String actionTaken;

    public static final String DONE = "Done";
    public static final String SNOOZE = "Snooze";
    public static final String DISMISS = "Dismiss";


    public HistoryItem() {

    }

    public HistoryItem(Date dateOfAction, String actionTaken)
    {
        this.dateOfAction = dateOfAction;
        this.actionTaken = actionTaken;
    }


    public Date getDateOfAction() {
        return dateOfAction;
    }

    public void setDateOfAction(Date dateOfAction) {
        this.dateOfAction = dateOfAction;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

}
