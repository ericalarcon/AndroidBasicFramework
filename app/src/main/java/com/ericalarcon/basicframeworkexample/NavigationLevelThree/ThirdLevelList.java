package com.ericalarcon.basicframeworkexample.NavigationLevelThree;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ericalarcon.basicframework.RowAdapters.BasicRowAdapter;
import com.ericalarcon.basicframework.RowAdapters.UniversalRowAdapter;
import com.ericalarcon.basicframework.Templates.BFMenu;
import com.ericalarcon.basicframework.Templates.BFMenuItem;
import com.ericalarcon.basicframework.Templates.ListFragment;
import com.ericalarcon.basicframework.Templates.NavigationActivity;

/**
 * The third level of navigation of this example
 */
public class ThirdLevelList extends ListFragment{
    public String instantiatedInLevelTwo;
    private BasicRowAdapter rowAdapter;

    private BFMenu menu1;
    private BFMenu getMenu1() {
        //lazy instantiation
        if(menu1 == null){
            menu1 = new BFMenu();

            menu1.addItem(new BFMenuItem("More actions", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));
            menu1.addItem(new BFMenuItem("More actions 2", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {
                    Snackbar.make(getView(),"Menu item clicked",Snackbar.LENGTH_SHORT).show();
                }
            }));

            menu1.addSearchItem(new BFMenu.BFMenuSearchListener() {
                @Override
                public void onSearchPressed(String query) {
                    Snackbar.make(getView(),"Search pressed",Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onQueryChanged(String query) {

                }

                @Override
                public void onSearchIconClicked() {

                }

                @Override
                public void onSearchIconDismissed() {

                }
            });

        }
        return menu1;
    }

    @Override
    public String title() {
        return "Third level";
    }

    @Override
    public UniversalRowAdapter getAdapter(Context context, ListView listview) {
        return new UniversalRowAdapter(context,1,listview) {
            @Override
            public Integer numberOfSections() {
                return 1;
            }

            @Override
            public Integer numberOfRowsInSection(Integer section) {
                return 1;
            }

            @Override
            public String headerForSection(Integer section) {
                return " ";
            }

            @Override
            public String footerForSection(Integer section) {
                return null;
            }

            @Override
            public Boolean showLoadingSpinnerOnBottom() {
                return null;
            }

            @Override
            public Boolean startSelectingOnLongClick(SectionIndex index) {
                return false;
            }

            @Override
            public Boolean pullToRefreshEnabled() {
                return false;
            }

            @Override
            public ListAdapter listAdapterForItem(Integer position) {
                //in this case we will return always the same rowAdapter but
                //if you want you can show different row styles for each position
                return rowAdapter;
            }

            @Override
            public void onListRefresh() {

            }

            @Override
            public void bottomReached() {

            }

            @Override
            public void onListItemClick(SectionIndex index) {

            }

            @Override
            public void onListItemLongClick(SectionIndex index) {

            }

            @Override
            public void onItemSelected(SectionIndex index) {

            }

            @Override
            public void onItemDeselected(SectionIndex index) {

            }

            @Override
            public void onSelectionModeCancelled() {

            }
        };
    }

    @Override
    public FloatingButton floatingActionButton() {
        return null;
    }

    @Override
    public void onCreateFinished() {
        //initialize the row adapters (you can have multiple row adapters if you want)
        rowAdapter = new BasicRowAdapter(getActivity().getApplicationContext()) {
            @Override
            public String getRowText(Integer position) {
                return instantiatedInLevelTwo;
            }

            @Override
            public Integer getRowImageId(Integer position) {
                return null;
            }

            @Override
            public Boolean showRowDisclosureIcon(Integer position) {
                return false;
            }
        };

        NavigationActivity nav = (NavigationActivity)getActivity();
        nav.addActionBarMenu(getMenu1());

        adapter.notifyDataSetChanged();
    }
}
