package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;

import patrick.fuscoe.remindmelater.R;

/**
 * Holds the complete list of category icons that can be used in customizing to do group icons
 * and reminder categories.
 *
 * Also stores which icon is currently checked.
 */
public class CategoryIconSet {

    public static final String TAG = "patrick.fuscoe.remindmelater.CategoryIconSet";

    private ArrayList<Integer> categoryIconList;
    private ArrayList<Boolean> categoryIconListIsChecked;
    private int selectedIconPos;


    public CategoryIconSet()
    {
        categoryIconList = new ArrayList<>();

        selectedIconPos = -1;

        populateList();
        buildCheckboxList();
    }

    private void populateList()
    {
        categoryIconList.add(R.drawable.category_account_card_details);
        categoryIconList.add(R.drawable.category_account_tie);
        categoryIconList.add(R.drawable.category_airplane);
        categoryIconList.add(R.drawable.category_ambulance);
        categoryIconList.add(R.drawable.category_archive);
        categoryIconList.add(R.drawable.category_atom);
        categoryIconList.add(R.drawable.category_axe);
        categoryIconList.add(R.drawable.category_baby_buggy);
        categoryIconList.add(R.drawable.category_balloon);
        categoryIconList.add(R.drawable.category_ballot);
        categoryIconList.add(R.drawable.category_bank);
        categoryIconList.add(R.drawable.category_basketball);
        categoryIconList.add(R.drawable.category_bed_empty);
        categoryIconList.add(R.drawable.category_bike);
        categoryIconList.add(R.drawable.category_book);
        categoryIconList.add(R.drawable.category_briefcase);
        categoryIconList.add(R.drawable.category_cake_variant);
        categoryIconList.add(R.drawable.category_calculator_variant);
        categoryIconList.add(R.drawable.category_camera);
        categoryIconList.add(R.drawable.category_car_hatchback);
        categoryIconList.add(R.drawable.category_cards_heart);
        categoryIconList.add(R.drawable.category_cart);
        categoryIconList.add(R.drawable.category_cash_usd);
        categoryIconList.add(R.drawable.category_cat);
        categoryIconList.add(R.drawable.category_chart_line);
        categoryIconList.add(R.drawable.category_chat);
        categoryIconList.add(R.drawable.category_clipboard_pulse);
        categoryIconList.add(R.drawable.category_clipboard_text);
        categoryIconList.add(R.drawable.category_clock);
        categoryIconList.add(R.drawable.category_coffee);
        categoryIconList.add(R.drawable.category_cookie);
        categoryIconList.add(R.drawable.category_creation);
        categoryIconList.add(R.drawable.category_delete_sweep);
        categoryIconList.add(R.drawable.category_dice_5);
        categoryIconList.add(R.drawable.category_dog_side);
        categoryIconList.add(R.drawable.category_dump_truck);
        categoryIconList.add(R.drawable.category_email);
        categoryIconList.add(R.drawable.category_emoticon_cool);
        categoryIconList.add(R.drawable.category_equal);
        categoryIconList.add(R.drawable.category_equalizer);
        categoryIconList.add(R.drawable.category_feather);
        categoryIconList.add(R.drawable.category_flag);
        categoryIconList.add(R.drawable.category_flash);
        categoryIconList.add(R.drawable.category_flask_empty);
        categoryIconList.add(R.drawable.category_flower);
        categoryIconList.add(R.drawable.category_food);
        categoryIconList.add(R.drawable.category_food_apple);
        categoryIconList.add(R.drawable.category_football);
        categoryIconList.add(R.drawable.category_format_superscript);
        categoryIconList.add(R.drawable.category_gamepad_variant);
        categoryIconList.add(R.drawable.category_gauge);
        categoryIconList.add(R.drawable.category_gavel);
        categoryIconList.add(R.drawable.category_glass_cocktail);
        categoryIconList.add(R.drawable.category_halloween);
        categoryIconList.add(R.drawable.category_hand_saw);
        categoryIconList.add(R.drawable.category_hanger);
        categoryIconList.add(R.drawable.category_headphones);
        categoryIconList.add(R.drawable.category_hiking);
        categoryIconList.add(R.drawable.category_home);
        categoryIconList.add(R.drawable.category_ice_cream);
        categoryIconList.add(R.drawable.category_image_area);
        categoryIconList.add(R.drawable.category_image_multiple);
        categoryIconList.add(R.drawable.category_key);
        categoryIconList.add(R.drawable.category_keyboard);
        categoryIconList.add(R.drawable.category_lightbulb);
        categoryIconList.add(R.drawable.category_mailbox);
        categoryIconList.add(R.drawable.category_microphone);
        categoryIconList.add(R.drawable.category_mushroom);
        //categoryIconList.add(R.drawable.category_note);
        categoryIconList.add(R.drawable.category_pen);
        categoryIconList.add(R.drawable.category_phone_classic);
        categoryIconList.add(R.drawable.category_phone_in_talk);
        categoryIconList.add(R.drawable.category_pine_tree);
        categoryIconList.add(R.drawable.category_podcast);
        categoryIconList.add(R.drawable.category_pot_mix);
        categoryIconList.add(R.drawable.category_power_socket_us);
        categoryIconList.add(R.drawable.category_radio);
        categoryIconList.add(R.drawable.category_radioactive);
        categoryIconList.add(R.drawable.category_recycle);
        categoryIconList.add(R.drawable.category_rice);
        categoryIconList.add(R.drawable.category_ring);
        categoryIconList.add(R.drawable.category_rocket);
        categoryIconList.add(R.drawable.category_run_fast);
        categoryIconList.add(R.drawable.category_sailing);
        categoryIconList.add(R.drawable.category_school);
        categoryIconList.add(R.drawable.category_scissors_cutting);
        categoryIconList.add(R.drawable.category_script_text);
        categoryIconList.add(R.drawable.category_server);
        categoryIconList.add(R.drawable.category_share_variant);
        categoryIconList.add(R.drawable.category_shield);
        categoryIconList.add(R.drawable.category_shoe_heel);
        categoryIconList.add(R.drawable.category_shoe_print);
        categoryIconList.add(R.drawable.category_shopping);
        categoryIconList.add(R.drawable.category_shower_head);
        categoryIconList.add(R.drawable.category_sign_text);
        categoryIconList.add(R.drawable.category_sitemap);
        categoryIconList.add(R.drawable.category_skate);
        categoryIconList.add(R.drawable.category_skull_crossbones);
        categoryIconList.add(R.drawable.category_sleep);
        categoryIconList.add(R.drawable.category_soccer);
        categoryIconList.add(R.drawable.category_speaker);
        categoryIconList.add(R.drawable.category_spray_bottle);
        categoryIconList.add(R.drawable.category_sprout);
        categoryIconList.add(R.drawable.category_star);
        categoryIconList.add(R.drawable.category_store);
        categoryIconList.add(R.drawable.category_subway);
        categoryIconList.add(R.drawable.category_sword_cross);
        categoryIconList.add(R.drawable.category_tablet_android);
        categoryIconList.add(R.drawable.category_tag);
        categoryIconList.add(R.drawable.category_television);
        categoryIconList.add(R.drawable.category_tennis_ball);
        categoryIconList.add(R.drawable.category_thumb_up);
        categoryIconList.add(R.drawable.category_tournament);
        categoryIconList.add(R.drawable.category_treasure_chest);
        categoryIconList.add(R.drawable.category_triforce);
        categoryIconList.add(R.drawable.category_trophy_variant);
        categoryIconList.add(R.drawable.category_truck);
        categoryIconList.add(R.drawable.category_umbrella);
        categoryIconList.add(R.drawable.category_walk);
        categoryIconList.add(R.drawable.category_watch);
        categoryIconList.add(R.drawable.category_water_pump);
        categoryIconList.add(R.drawable.category_web);
        categoryIconList.add(R.drawable.category_wheelchair_accessibility);
        categoryIconList.add(R.drawable.category_white_balance_incandescent);
        categoryIconList.add(R.drawable.category_white_balance_sunny);
        categoryIconList.add(R.drawable.category_worker);
        categoryIconList.add(R.drawable.category_wrench);
    }

    private void buildCheckboxList()
    {
        categoryIconListIsChecked = new ArrayList<>();

        for (int i = 0; i < categoryIconList.size(); i++)
        {
            categoryIconListIsChecked.add(false);
        }
    }

    public ArrayList<Integer> getCategoryIconList() {
        return categoryIconList;
    }

    public ArrayList<Boolean> getCategoryIconListIsChecked() {
        return categoryIconListIsChecked;
    }

    public int getSelectedIconPos() {
        return selectedIconPos;
    }

    public void markSelected(Integer iconId)
    {
        for (int i = 0; i < categoryIconList.size(); i++)
        {
            if (iconId.equals(categoryIconList.get(i)))
            {
                categoryIconListIsChecked.set(i, true);
                selectedIconPos = i;
            }
        }
    }
}
