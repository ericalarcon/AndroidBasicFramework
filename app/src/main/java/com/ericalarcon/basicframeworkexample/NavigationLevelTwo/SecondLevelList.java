package com.ericalarcon.basicframeworkexample.NavigationLevelTwo;

import android.content.Context;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ericalarcon.basicframework.RowAdapters.DetailRowAdapter;
import com.ericalarcon.basicframework.RowAdapters.UniversalRowAdapter;
import com.ericalarcon.basicframework.Templates.BFMenu;
import com.ericalarcon.basicframework.Templates.BFMenuItem;
import com.ericalarcon.basicframework.Templates.ListFragment;
import com.ericalarcon.basicframework.Templates.NavigationActivity;
import com.ericalarcon.basicframeworkexample.NavigationLevelThree.ThirdLevelList;

import java.util.ArrayList;

/**
 * The second level of navigation of this example
 */
public class SecondLevelList extends ListFragment{
    //variable that we will instanciate before pushing the fragment
    //this is how you can pass information between fragments, but you can use
    //intents if you want to
    public String instantiatedInLevelOne;

    private DetailRowAdapter dRowAdapter;

    //array to store the selected items (on long press)
    private ArrayList<Integer> selectedIndexes;
    private ArrayList<Integer> getSelectedIndexes() {
        if(selectedIndexes == null){
            selectedIndexes = new ArrayList<>();
        }
        return selectedIndexes;
    }

    //the menu that will be shown when selection items (on long press) and its lazy instantiation
    private BFMenu editMenu;
    private BFMenu getEditMenu() {
        //lazy instantiation. If you don't like this pattern you can put the initialization code in viewCreated
        if(editMenu == null){
            editMenu = new BFMenu(new BFMenu.BFMenuTitleListener() {
                @Override
                public String getText() {
                    return String.format("%d selected",getSelectedIndexes().size());
                }
            });

            editMenu.addItem(new BFMenuItem("Action 1", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {

                }
            }));
            editMenu.addItem(new BFMenuItem("Action 2", 0, BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM, new BFMenuItem.BFMenuItemListener() {
                @Override
                public void onClick() {

                }
            }));

        }
        return editMenu;
    }

    private Boolean isEditing = false;

    @Override
    public String title() {
        return "Second level";
    }

    @Override
    public UniversalRowAdapter getAdapter(Context context, ListView listview) {
        return new UniversalRowAdapter(context,100,listview) {
            @Override
            public Integer numberOfSections() {
                return 1;
            }

            @Override
            public Integer numberOfRowsInSection(Integer section) {
                return 100;
            }

            @Override
            public String headerForSection(Integer section) {
                return instantiatedInLevelOne;
            }

            @Override
            public String footerForSection(Integer section) {
                return null;
            }

            @Override
            public Boolean showLoadingSpinnerOnBottom() {
                return false;
            }

            @Override
            public Boolean startSelectingOnLongClick(SectionIndex index) {
                return true;
            }

            @Override
            public Boolean pullToRefreshEnabled() {
                return true;
            }

            @Override
            public ListAdapter listAdapterForItem(Integer position) {
                //in this case we will return always the same rowAdapter but
                //if you want you can show different row styles for each position
                return dRowAdapter;
            }

            @Override
            public void onListRefresh() {
                adapter.endRefreshing();
            }

            @Override
            public void bottomReached() {

            }

            @Override
            public void onListItemClick(SectionIndex index) {
                ThirdLevelList dest = new ThirdLevelList();
                dest.instantiatedInLevelTwo = "Hello, I come From Item " + index.position.toString();
                NavigationActivity nav = (NavigationActivity)getActivity();
                nav.pushFragment(dest, NavigationActivity.animationType.RIGHT_TO_LEFT,true);
            }

            @Override
            public void onListItemLongClick(SectionIndex index) {

            }

            @Override
            public void onItemSelected(SectionIndex index) {
                //add to selectedIndexes array
                getSelectedIndexes().add(index.position);

                //show editing action bar if necessary
                isEditing = getSelectedIndexes().size() > 0;
                if(isEditing)((NavigationActivity)getActivity()).setEditingActionbarMenu(getEditMenu());
            }

            @Override
            public void onItemDeselected(SectionIndex index) {
                //remove from selectedIndexes array
                getSelectedIndexes().remove(index.position);

                //hide editing action bar if necessary
                isEditing = getSelectedIndexes().size() > 0;
                if(!isEditing)((NavigationActivity)getActivity()).setEditingActionbarMenu(null);
            }

            @Override
            public void onSelectionModeCancelled() {

            }
        };
    }

    @Override
    public void onCreateFinished() {
        //initialize the row adapters (you can have multiple row adapters if you want)
        dRowAdapter = new DetailRowAdapter(getActivity().getApplicationContext()) {
            @Override
            public String getRowText(Integer position) {
                return "Item "+position.toString();
            }

            @Override
            public String getRowDetailText(Integer position) {
                return "Detail text for "+position.toString();
            }

            @Override
            public Integer getRowImageId(Integer position) {
                return com.ericalarcon.basicframework.R.mipmap.dummy_file_icon;
            }

            @Override
            public Boolean showRowDisclosureIcon(Integer position) {
                return true;
            }
        };
    }
}
