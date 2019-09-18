package patrick.fuscoe.remindmelater.models;

public class ReminderCategory implements Comparable<ReminderCategory> {

    private String categoryName;
    //private int iconId;
    private String iconName;

    public ReminderCategory(String categoryName, String iconName)
    {
        this.categoryName = categoryName;
        //this.iconId = iconId;
        this.iconName = iconName;
    }

    @Override
    public int compareTo(ReminderCategory o) {
        return this.getCategoryName().compareTo(o.getCategoryName());
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getIconName() {
        return iconName;
    }
}
