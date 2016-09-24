package com.ericalarcon.basicframeworkexample.NavigationLevelOne;

import android.content.Context;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ericalarcon.basicframework.RowAdapters.BasicRowAdapter;
import com.ericalarcon.basicframework.RowAdapters.UniversalRowAdapter;
import com.ericalarcon.basicframework.Templates.ListFragment;
import com.ericalarcon.basicframework.Templates.NavigationActivity;
import com.ericalarcon.basicframeworkexample.NavigationLevelTwo.SecondLevelList;

/**
 * List shown as the second tab
 */
public class AppTabTwo extends ListFragment{
    BasicRowAdapter rowAdapter;

    @Override
    public String title() {
        return null;//don't need to set the title since it's set in AppTabsFragment
    }

    @Override
    public UniversalRowAdapter getAdapter(Context context, ListView listview) {
        return new UniversalRowAdapter(context,10,listview) {
            @Override
            public Integer numberOfSections() {
                return 2;
            }

            @Override
            public Integer numberOfRowsInSection(Integer section) {
                return 5;
            }

            @Override
            public String headerForSection(Integer section) {
                if(section == 0){
                    return "First section in second tab";
                }
                else{
                    return "Second section in second tab";
                }
            }

            @Override
            public String footerForSection(Integer section) {
                if(section == 0){
                    return "First sample footer in the Tab 2";
                }
                else{
                    return "Second sample footer in the Tab 2";
                }

            }

            @Override
            public Boolean showLoadingSpinnerOnBottom() {
                return false;
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
                SecondLevelList dest = new SecondLevelList();
                dest.instantiatedInLevelOne = "Hello, I come From Section:"+index.section.toString()+" Item " + index.position.toString();
                NavigationActivity nav = (NavigationActivity)getActivity();
                nav.pushFragment(dest, NavigationActivity.animationType.RIGHT_TO_LEFT);
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
                return "Item "+ adapter.getSectionedPosition(position).position;
            }

            @Override
            public Integer getRowImageId(Integer position) {
                return null;
            }

            @Override
            public Boolean showRowDisclosureIcon(Integer position) {
                return true;
            }
        };
    }
}
