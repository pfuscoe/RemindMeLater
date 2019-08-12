package patrick.fuscoe.remindmelater.models;

public class ReminderCategory implements Comparable<ReminderCategory> {

    private String categoryName;
    private int iconId;

    public ReminderCategory(String categoryName, int iconId)
    {
        this.categoryName = categoryName;
        this.iconId = iconId;
    }

    @Override
    public int compareTo(ReminderCategory o) {
        return this.getCategoryName().compareTo(o.getCategoryName());
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getIconId() {
        return iconId;
    }
}
