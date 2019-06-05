package patrick.fuscoe.remindmelater.models;

public class Recurrance {

    private int recurValue;
    private String recurType;

    /** Recurrance Type Strings **/
    private static final String DAYS = "Days";
    private static final String WEEKS = "Weeks";
    private static final String MONTHS = "Months";
    private static final String YEARS = "Years";


    public Recurrance(int recurValue, String recurType)
    {
        this.recurValue = recurValue;
        this.recurType = recurType;
    }

}
