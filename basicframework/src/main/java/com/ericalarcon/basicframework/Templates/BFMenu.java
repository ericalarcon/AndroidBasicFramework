package com.ericalarcon.basicframework.Templates;

import java.util.ArrayList;

/**
 * Created by erica on 07/08/2016.
 * Class that represents an ActionBar menu.
 * -Can have a search menu item
 * -Handles all the necessary for performing a search
 * -Stores all the BFMenuItems in an array
 * -This class will not handle onClick item events. Each BFMenuItem will handle it itself.
 * */
public class BFMenu {
    /**
     * Default constructor
     */
    public BFMenu(){}

    /**
     * constructor with titleListener, in case we want to change the ActionBarTitle dynamically
     * @param aTitleListener
     * This listener is called by NavigationActivity every time it wants to know the title
     */
    public BFMenu(BFMenuTitleListener aTitleListener){titleListener = aTitleListener;}

    private ArrayList<BFMenuItem> items;
    //items lazy instantiation
    public ArrayList<BFMenuItem> getItems() {
        if (items == null){
            items = new ArrayList<>();
        }
        return items;
    }

    //handles all search events
    BFMenuSearchListener searchListener;

    //handles title changes
    BFMenuTitleListener titleListener;

    /**
     * Add item to the menu
     * @param item
     * Item to be added
     */
    public void addItem(BFMenuItem item){
        getItems().add(item);
    }

    /**
     * Add a search item to the menu
     * @param aListener
     * Listener that will handle all the events
     */
    public void addSearchItem(BFMenuSearchListener aListener){
        searchListener = aListener;
    }

    public static abstract class BFMenuSearchListener{
        public abstract void onSearchPressed(String query);
        public abstract void onQueryChanged(String query);
        public abstract void onSearchIconClicked();
        public abstract void onSearchIconDismissed();
    }

    public static abstract class BFMenuTitleListener{
        public abstract String getText();
    }


}
