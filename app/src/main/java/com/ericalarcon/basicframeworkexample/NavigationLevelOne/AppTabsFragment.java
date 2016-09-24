package com.ericalarcon.basicframeworkexample.NavigationLevelOne;

import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;

import com.ericalarcon.basicframework.Templates.BFMenu;
import com.ericalarcon.basicframework.Templates.BFMenuItem;
import com.ericalarcon.basicframework.Templates.NavigationActivity;
import com.ericalarcon.basicframework.Templates.TabsFragment;

/**
 * This Fragment shows 2 tabs and a different menu for each selected Tab
 */
public class AppTabsFragment extends TabsFragment{
    //we declare the 1st tab menu and his lazy instantiation
    private BFMenu tabMenu1;
    private BFMenu getTabMenu1() {
        //lazy instantiation. If you don't like this pattern you can put the initialization code in viewCreated
        if(tabMenu1 == null){
            tabMenu1 = new BFMenu();

            tabMenu1.addItem(new BFMenuItem("More actions in Tab1", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));
            tabMenu1.addItem(new BFMenuItem("More actions in Tab1", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));

            tabMenu1.addItem(new BFMenuItem("Action1", 0, BFMenuItem.BFMenuItemType.SHOW_AS_ACTION, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Action1 clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));

        }
        return tabMenu1;
    }

    //we declare the 2nd tab menu and his lazy instantiation
    private BFMenu tabMenu2;
    private BFMenu getTabMenu2() {
        //lazy instantiation. If you don't like this pattern you can put the initialization code in viewCreated
        if(tabMenu2 == null){
            tabMenu2 = new BFMenu();

            tabMenu2.addItem(new BFMenuItem("More actions in Tab2", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));
            tabMenu2.addItem(new BFMenuItem("More actions in Tab2", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));

            tabMenu2.addItem(new BFMenuItem("Action2", 0, BFMenuItem.BFMenuItemType.SHOW_AS_ACTION, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Action2 clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));
        }
        return tabMenu2;
    }

    @Override
    public Fragment getTabFragment(int position) {
        //return a different Fragment for each tab position
        if(position == 0){
            return new AppTabOne();
        }
        else{
            return new AppTabTwo();
        }
    }

    @Override
    public int getTabCount() {
        return 2;
    }

    @Override
    public String getTabTitle(int position) {
        return "Tab " + (position+1);
    }

    @Override
    public Integer getTabIcon(int position) {
        //if you want an icon return R. [...]
        return null;
    }

    @Override
    public Integer getTabIconTint(int position) {
        return null;
    }

    @Override
    public void didSelectTab(TabLayout.Tab tab) {
        NavigationActivity nav = (NavigationActivity)getActivity();

        //set the title (can be different for each tab if you want)
        nav.setTitle("First level");

        //change the ActionBar menu depending on the tab selected
        if (tab.getPosition() == 0)
            nav.replaceActionBarMenu(getTabMenu1());
        else
            nav.replaceActionBarMenu(getTabMenu2());

    }

    @Override
    public void viewCreated() {

    }
}
