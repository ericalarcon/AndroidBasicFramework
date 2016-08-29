package com.ericalarcon.basicframework.Templates;

/**
 * Created by erica on 07/08/2016.
 * Represents an ActionBar Menu item
 * Can have title, icon, and type (always visible, only visible in the ... popup menu, or visible if room)
 * Handles the click events of the item.
 */
public class BFMenuItem {
    public enum BFMenuItemType{
        SHOW_AS_ACTION,
        SHOW_AS_MENUITEM,
        SHOW_AS_ACTION_IF_ROOM
    }

    String title;
    Integer icon;
    BFMenuItemType menuItemType;
    BFMenuItemListener listener;

    /**
     * Creates a menu item
     * @param pTitle
     * Title of the item
     * @param pIcon
     * (optional) Icon of the item. Set 0 if none.
     * @param pMenuItemType
     * SHOW_AS_ACTION is always visible, SHOW_AS_MENUITEM is only visible in the ... popup menu, SHOW_AS_ACTION_IF_ROOM is visible if room
     * @param aListener
     * Handles click action.
     */
    public BFMenuItem(String pTitle, Integer pIcon, BFMenuItemType pMenuItemType, BFMenuItemListener aListener){
        title = pTitle;
        icon = pIcon;
        menuItemType = pMenuItemType;
        listener = aListener;
    }

    public static abstract class BFMenuItemListener{
        public abstract void onClick();
    }
}

