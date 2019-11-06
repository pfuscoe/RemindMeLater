package patrick.fuscoe.remindmelater.models;

/**
 * Data model for reminder categories.
 */
public class ReminderCategory implements Comparable<ReminderCategory> {

    private String categoryName;
    private String iconName;

    public ReminderCategory(String categoryName, String iconName)
    {
        this.categoryName = categoryName;
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

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
