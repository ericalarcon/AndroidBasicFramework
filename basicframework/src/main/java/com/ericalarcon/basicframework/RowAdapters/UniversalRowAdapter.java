package com.ericalarcon.basicframework.RowAdapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ericalarcon.basicframework.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by erica on 15/07/2016.
 * custom adapter for rows that handles the following:
 * -Listing rows (captain obvious to the rescue)
 * -Dividing rows in 'sections'. A section is a set of rows with a header and a footer
 * -Multi select mode
 * -Show loading spinner at the bottom if necessary
 * -Provides pull-to-refresh mechanism
 * -Delegates the presentation of the row to other adapters (BasicRowAdapter, DetailRowAdapter, etc), so it can display different types of rows
 * WARNING: other adapter's layout must be a RELATIVE LAYOUT (see getView operation for more information)
 */
public abstract class UniversalRowAdapter extends ArrayAdapter<String> {
    private final Context context;
    private Integer realListSize; //keeps the real size of the list. Real size includes headers and footers as a row.
    private SwipeRefreshLayout swipeContainer; //swipe to refresh container
    private ListView parentListView; //list view which contains the rows we are controlling
    private SectionIndex selectedIndex; //class that separates the rows in section/index. Each section has header and footer and is visibly separated in the user interface.
    private Boolean justLongClicked = false; //bool to control if the user has just long clicked a row (in order to avoid an automatic click right after)
    private Integer mustAnimateRowId = -1; //variable to store the row id that must be animated. This is for the "select/check" animation
    private Integer fastClicksNumber = 0; //variable to control if the user has just clicked a row (in order to avoid an accidental double click)
    private Integer lastItemSelected = -1;
    private HashMap<Integer,Boolean> itemIsSelected  = new HashMap<>(); //rows that are selected when in multiple selection mode
    @NonNull
    private Boolean selectionModeIsActive(){
        return itemIsSelected.size() > 0;
    }

    //abstract methods, which will usually be implemented in the Fragment containing the listView
    //this methods will be the way which the adapter will obtain the information to populate the rows
    public abstract Integer numberOfSections();
    public abstract Integer numberOfRowsInSection(Integer section);
    public abstract String headerForSection(Integer section);
    public abstract String footerForSection(Integer section);
    public abstract Boolean showLoadingSpinnerOnBottom();
    public abstract Boolean startSelectingOnLongClick(final SectionIndex index);
    public abstract Boolean pullToRefreshEnabled();
    public abstract ListAdapter listAdapterForItem(Integer position);
    public abstract void onListRefresh();
    public abstract void bottomReached();
    public abstract void onListItemClick(final SectionIndex index);
    public abstract void onListItemLongClick(final SectionIndex index);
    public abstract void onItemSelected(final SectionIndex index);
    public abstract void onItemDeselected(final SectionIndex index);
    public abstract void onSelectionModeCancelled();

    public UniversalRowAdapter(Context context, Integer size, ListView listview) {
        //empty super initialization. We will add the rows later with the "addAll" method
        super(context,-1);
        this.context = context;
        this.parentListView = listview;

        if (size > 0){
            //List size is 'size', plus the headers/footers (numberOfSections*2) and a row reserved for showing a loading spinner at the bottom
            realListSize = 1+size+numberOfSections()*2;
        }else{
            realListSize = 1;
        }

        //Add empty array of elements, because rows will be populated via abstract implementations after. This is just to tell the system
        //to allocate space for X rows.
        addAll(new ArrayList<>(Arrays.asList(new String[realListSize])));

        //disable default divider, our divider will be custom for each row (see xml layouts)
        listview.setDivider(null);

        //pull to refresh logic
        try{

            swipeContainer = (SwipeRefreshLayout) parentListView.getParent();
            swipeContainer.setEnabled(false);

            if(pullToRefreshEnabled()){
                swipeContainer.setEnabled(true);
                swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        onListRefresh(); //call abstract method that must be implemented in the Fragment containing the listView
                    }
                });
                // Configure the colors of the arrow
                swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);
            }

        }
        catch (Exception e) {
            Log.w("DEBUG", "No SwipeRefreshLayout");
        }

        //item click listener. Will handle normal clicks, and clicks when we are on selection mode.
        parentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedIndex = getSectionedPosition(getPositionIgnoringHeaders(i));

                if(selectionModeIsActive()){
                    if(itemIsSelected.get(i) == null){
                        //item is not selected. Select it
                        itemIsSelected.put(i,true); //put it in the selected data structure
                        mustAnimateRowId = i; //must animate this row (used in the getView function)

                        //call abstract method that must be implemented in the Fragment containing the listView
                        SectionIndex index = getSectionedPosition(getPositionIgnoringHeaders(i));
                        onItemSelected(index);

                        //refresh the actionbar (may be necessary to refresh the new actionBar items and title)
                        ((AppCompatActivity)getContext()).invalidateOptionsMenu();
                    }
                    else{
                        if(!justLongClicked) {
                            //item is not selected. Deselect it
                            itemIsSelected.remove(i); //remove from the selected data structure
                            mustAnimateRowId = i; //must animate this row (used in the getView function)

                            //call abstract method that must be implemented in the Fragment containing the listView
                            SectionIndex index = getSectionedPosition(getPositionIgnoringHeaders(i));
                            onItemDeselected(index);

                            //refresh the actionbar (may be necessary to refresh the new actionBar items and title)
                            ((AppCompatActivity)getContext()).invalidateOptionsMenu();//refresca la actionbar
                        }
                        else justLongClicked = false;

                    }

                    //force row refreshing
                    getView(i, view, parentListView);
                }
                else{
                    if(i != lastItemSelected && lastItemSelected != -1){
                        fastClicksNumber = 0;
                    }
                    fastClicksNumber++; //control if the user has just clicked
                    final Handler handler = new Handler();

                    //abstract method call delayed, to give the user time to realize he has clicked the row
                    //(personal opinion, if this behavior is not wanted simply remove the handler and call  onListItemClick(selectedIndex); directly
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            if(fastClicksNumber == 1){
                                //call abstract method that must be implemented in the Fragment containing the listView
                                onListItemClick(selectedIndex);
                            }
                        }
                    }, 100);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fastClicksNumber = 0;//control reset (control if the user has just clicked)
                        }
                    }, 400);



                    lastItemSelected = i;
                }
            }
        });


        parentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                SectionIndex index = getSectionedPosition(getPositionIgnoringHeaders(i));

                //call abstract method -to know if we must start multiselect on long click- that must be implemented in the Fragment containing the listView
                if(startSelectingOnLongClick(index)){
                    if(itemIsSelected.get(i) == null){
                        itemIsSelected.put(i,true);//put it in the selected data structure
                        mustAnimateRowId = i;//must animate this row (used in the getView function)
                        getView(i, view, parentListView); //refresh the row
                        justLongClicked = true;

                        //call abstract method
                        onItemSelected(index);

                        //refresh the actionbar (may be necessary to refresh the new actionBar items and title)
                        ((AppCompatActivity)getContext()).invalidateOptionsMenu();
                    }
                }

                //call abstract method that must be implemented in the Fragment containing the listView
                onListItemLongClick(index);
                return false;
            }
        });
    }

    /**
     * FUNCTION CALLED BY THE SYSTEM
     * Gets the view for the row in the position 'position' of the tableview.
     * Contains the logic to:
     * -show/hide the checks when in selection mode
     * -show/hide the loading row at the bottom
     * -show footers and headers
     * -delegates getview of the row itself to other adapters (BasicRowAdapter, DetailRowAdapter, etc.)
     * @param position
     * Position of the item in the table view
     * @param convertView
     * Reusable view (parameter given by the UniversalRowAdapter's getView)
     * @param parent
     * Parent view(parameter given by the UniversalRowAdapter's getView)
     * @return
     * Row view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Integer numberOfSections = numberOfSections();
        Integer numberOfRowsInPreviousSections = 0;
        Integer numberOfRows = 0;
        Integer currentSection = 0;

        //show/hide check icon if we are in selection mode
        try {
            if(convertView != null && convertView.getClass().isAssignableFrom(RelativeLayout.class)){
                final RelativeLayout rl = (RelativeLayout)convertView; //must be a relative layout or else it will fail! TO-DO: check if is relative or linear layout...

                if(itemIsSelected.get(position) != null){ //item is selected!

                    //if the check icon has been added previously, we can fetch it...
                    int id=100+1; //mysterious way to make findViewById accept 101 parameter
                    View v = rl.findViewById(id);

                    if(v == null) {
                        //create an ImageView with the CHECK image, and assign it the id 101 (randomly chosen, high enough to not interfere with other id's)
                        ImageView check = new ImageView(getContext());
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                                (AppBarLayout.LayoutParams.WRAP_CONTENT, AppBarLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_VERTICAL);
                        params.setMargins(25,0,0,0);
                        check.setLayoutParams(params);
                        check.setId(id);
                        check.setImageResource(R.mipmap.check_edit_mode);

                        //add it to the relative layout representing the row
                        rl.addView(check);

                        //also change the background color to indicate selection
                        rl.setBackgroundColor( ContextCompat.getColor(parentListView.getContext(), R.color.EditColorPrimaryLight));

                        //only animate the row that has just been clicked! Else it will simple be marked as View.VISIBLE
                        if(mustAnimateRowId == position){
                            //fade in animation for the check animation
                            check.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.fadein));
                            mustAnimateRowId = -1;
                        }

                        //make sure check is visible
                        rl.findViewById(id).setVisibility(View.VISIBLE);
                    }
                    else{
                        //check already exists. We will just show it
                        //only animate the row that has just been clicked! Else it will simple be marked as View.VISIBLE
                        if(mustAnimateRowId == position){
                            v.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.fadein));
                            mustAnimateRowId = -1;
                        }
                        //also change the background color to indicate selection
                        rl.setBackgroundColor( ContextCompat.getColor(parentListView.getContext(), R.color.EditColorPrimaryLight));
                        //make sure check is visible
                        v.setVisibility(View.VISIBLE);
                    }

                }
                else{ //item is NOT selected!
                    //if the check icon has been added previously, we can fetch it...
                    int id=100+1;//mysterious way to make findViewById accept 101 parameter
                    View v = rl.findViewById(id);
                    if(v != null) {
                        rl.setBackgroundColor(0x00000000); //restore background to its original state (transparent)

                        //only animate the row that has just been clicked! Else it will simple be marked as View.GONE
                        if(mustAnimateRowId == position){
                            v.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.fadeout));
                            mustAnimateRowId = -1;
                        }
                        //make sure check is NOT visible
                        v.setVisibility(View.GONE);
                    }


                }
            }

        }catch (Exception e){
            Log.w("DEBUG", e.getMessage());
        }


        //loading spinner
        if(realListSize > 0 && position == realListSize-1){ //check if it's the last row
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_loadingrowlayout, parent, false);

            //call abstract method to know if loading must be shown

            if(showLoadingSpinnerOnBottom() != null && showLoadingSpinnerOnBottom()) {
                rowView.setVisibility(View.VISIBLE); //show loading spinner!

                //if it's the first row, add additional padding. Just UX things...
                View padding = rowView.findViewById(R.id.pbHeaderProgressAdditionalPadding);
                if (position == 0) padding.setVisibility(View.VISIBLE);
                else padding.setVisibility(View.GONE);
            }
            else {
                rowView.setVisibility(View.GONE); //hide loading spinner
            }

            //make the row not clickable
            rowView.setEnabled(false);
            rowView.setOnClickListener(null);


            if(position > 0){
                //call abstract method that must be implemented in the Fragment containing the listView
                bottomReached();
            }

            //return loading spinner row view!
            return rowView;
        }

        //Here we need to calculate if it's a normal row, or a header/footer row.
        //Will be header if it's the first row in the section. We will iterate every
        //section and obtain how many items has each section to know it
        for (int section = 0; section < numberOfSections; section++) {
            numberOfRowsInPreviousSections = numberOfRows;
             numberOfRows += numberOfRowsInSection(section)+2;
            if (position < numberOfRows) {
                currentSection = section;
                break;
            }
        }
        boolean isFirsRowInSection = position == numberOfRowsInPreviousSections || position == 0;
        //if isFirsRowInSection -> is a header!
        if (isFirsRowInSection){
            //get header view with the inflater
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View headerView = inflater.inflate(R.layout.row_rowheader, parent, false);

            //if it's the first row, add additional padding. Just UX things...
            View headerPadding = headerView.findViewById(R.id.rowHeaderAdditionalPadding);
            if(position == 0) headerPadding.setVisibility(View.VISIBLE);
            else  headerPadding.setVisibility(View.GONE);


            //call abstract method to obtain text for the header
            String headerText = headerForSection(currentSection);
            TextView text = (TextView)headerView.findViewById(R.id.rowHeader);
            text.setText(headerText);

            //if there's no header, hide it to not show empty space
            if ("".equals(headerText) || headerText == null){
                text.setVisibility(View.GONE);
            }

            //make the header not clickable
            headerView.setEnabled(false);
            headerView.setOnClickListener(null);

            //return the header
            return headerView;
        }

        boolean isLastRowInSection = position == numberOfRows-1;
        //if isLastRowInSection -> is a footer!
        if (isLastRowInSection){
            //get footer view with the inflater
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View footerView = inflater.inflate(R.layout.row_rowfooter, parent, false);

            //call abstract method to obtain text for the footer
            String footerText = footerForSection(currentSection);
            TextView text = (TextView)footerView.findViewById(R.id.rowFooter);
            text.setText(footerText);

            //if there's no footer, hide it to not show empty space
            if ("".equals(footerText) || footerText == null){
                text.setVisibility(View.GONE);
            }

            //make the header not clickable
            footerView.setEnabled(false);
            footerView.setOnClickListener(null);

            //return the footer
            return footerView;
        }

        //FINALLY if it's a normal row (no footer, no header, no loading spinner)
        //we obtain the necessary adapter trough the abstract operation and return the getView() result of that adapter


        //get the adapter
        Integer currentNumberOfHeaders = currentSection + 1;
        Integer currentNumberOfFooters = currentSection;
        ListAdapter adapter = listAdapterForItem(position - currentNumberOfHeaders - currentNumberOfFooters); //abstract method, must be implemented in the Fragment containing the listView

        //call getView of the obtained adapter (can be BasicRowAdapter, DetailRowAdapter, etc. or even your own)
        View returnValue =  adapter.getView(position - currentNumberOfHeaders-currentNumberOfFooters ,convertView,parent);

        //additional check to add/remove background color indicating selection, or else it can be glitchy because of the recycled views
        if(itemIsSelected.get(position) == null){
            returnValue.setBackgroundColor(0x00000000);
        }
        else{
            returnValue.setBackgroundColor( ContextCompat.getColor(parentListView.getContext(), R.color.EditColorPrimaryLight));
        }

        //finally return the shiny, magnificent row
        return returnValue;
    }


    /**
     * Returns an object containing the section of the row and the index of the row in that section (each section starts at index zero)
     * @param position
     * Item linear position.
     * @return
     * object containing the section of the row and the index of the row in that section (each section starts at index zero)
     */
    public SectionIndex getSectionedPosition(Integer position){
        Integer numberOfSections = numberOfSections();
        Integer numberOfRowsInPreviousSections = 0;
        Integer numberOfRows = 0;
        Integer currentSection = 0;

        for (int section = 0; section < numberOfSections; section++) {
            numberOfRowsInPreviousSections = numberOfRows;
            numberOfRows += numberOfRowsInSection(section);
            if (position < numberOfRows) {
                currentSection = section;
                break;
            }
        }

        return new SectionIndex(currentSection,position-numberOfRowsInPreviousSections);
    }

    /**
     * Returns linear position of the item, without counting the headers and footers (that internally count as a row)
     * @param position
     * Item real internal position (counting headers and footers)
     * @return
     * Linear position of the item, without counting the headers and footers (that internally count as a row)
     */
    public Integer getPositionIgnoringHeaders(Integer position){
        Integer numberOfSections = numberOfSections();
        Integer numberOfRows = 0;
        Integer currentSection = 0;

        for (int section = 0; section < numberOfSections; section++) {
            numberOfRows += numberOfRowsInSection(section)+2;
            if (position < numberOfRows) {
                currentSection = section;
                break;
            }
        }
        Integer currentNumberOfheaders = currentSection + 1;
        Integer currentNumberOfFooters = currentSection;
        return position-currentNumberOfheaders-currentNumberOfFooters;

    }

    /**
     * Stops the refreshing animation
     */
    public void endRefreshing(){
        swipeContainer.setRefreshing(false);
    }

    /**
     * Clear all selected items
     */
    public void endSelectionMode(){
        itemIsSelected.clear();
        onSelectionModeCancelled(); //abstract method
        resetAdapter();
    }

    /**
     * Refresh the list in case that some rows has been added
     */
    public void resetAdapter() {
        clear();
        int totalSize = 0;
        for (int i = 0; i < numberOfSections(); i++) {
            totalSize += numberOfRowsInSection(i);
        }
        totalSize = totalSize+1+(numberOfSections()*2);

        realListSize = totalSize;
        addAll(new ArrayList<>(Arrays.asList(new String[totalSize])));
        notifyDataSetChanged();
    }

    /**
     * Simple struct that contains section and position variables
     * Designed to divide a list into several sections
     */
    public class SectionIndex{
        public Integer section;
        public Integer position;

        public SectionIndex(Integer pSection, Integer pPosition){
            section = pSection;
            position = pPosition;
        }
    }
    /*public void addItems(Integer numberOfItems){

        if (realListSize == 0){
            numberOfItems = numberOfItems+1+(numberOfSections()*2);
            realListSize += numberOfItems;
        }
        else{
            realListSize += numberOfItems;
        }

        addAll(new ArrayList<String>(Arrays.asList(new String[numberOfItems])));
        notifyDataSetChanged();
    }*/


}



