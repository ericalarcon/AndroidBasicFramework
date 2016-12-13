package com.ericalarcon.basicframework.Templates;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.ericalarcon.basicframework.R;

import java.io.Serializable;
import java.util.ArrayList;
@SuppressWarnings("unused,deprecation")

/**
 * The NavigationActivity class implements a specialized Activity that manages
 * the navigation of hierarchical content. A NavigationActivity object manages
 * the currently displayed screens using the navigation stack, which is represented
 * by an array of Fragments. The first Fragment in the array is the root
 * Fragment. The last Fragment in the array is the Fragment currently
 * being displayed. You add and remove Fragments from the stack using the methods of this class.
 * Must be subclassed to implement abstract methods.
 * The NavigationActivity class handles:
 * -Pushing/popping fragments
 * -Animation between fragments
 * -Changing the actionBar title
 * -Changing the actionBar menus
 * -Changing the actionBar menu to "edition mode" (with different color)
 * -Back button behaviour
 */
public abstract class NavigationActivity extends AppCompatActivity implements Serializable {

    //animation types that will be used in the transition between Fragments
    public enum animationType{
        RIGHT_TO_LEFT, //recommended for sequential navigation
        BOTTOM_TO_TOP, //recommended for showing Fragment as "modal" pop-up (i.e. the last step of a navigation)
        FLIP //not efficient yet
    }

    public abstract Fragment firstFragment();
    public abstract Boolean showBackButtonInFirstFragment();
    public abstract Boolean showMasterDetailLayoutInTablets();

    private ArrayList<Integer> fragmentIds;
    //fragmentsStack lazy instantiation
    public ArrayList<Integer> getFragmentIds() {
        if(fragmentIds == null){
            fragmentIds = new ArrayList<>();
        }
        return fragmentIds;
    }

    private ArrayList<Fragment> fragmentsStack;
    //fragmentsStack lazy instantiation
    public ArrayList<Fragment> getFragmentsStack() {
        if(fragmentsStack == null){
            fragmentsStack = new ArrayList<>();
        }
        return fragmentsStack;
    }

    //Titles that will be shown in the actionBar. There's a title for every Fragment
    private ArrayList<String> titlesStack;
    //titlesStack lazy instantiation
    public ArrayList<String> getTitlesStack() {
        if(titlesStack == null){
            titlesStack = new ArrayList<>();
        }
        return titlesStack;
    }

    //Menus that will be shown in the actionBar. There's a BFMenu Object for every Fragment
    private ArrayList<BFMenu> menusStack;
    //menusStack lazy instantiation
    public ArrayList<BFMenu> getMenusStack() {
        if(menusStack == null){
            menusStack = new ArrayList<>();
        }
        return menusStack;
    }

    //special actionBar Menus when user is selecting multiple items (selection mode) (will change the color of the actionBar)
    private BFMenu editionModeMenu;

    private String currentTheme = "default";
    private String titleBeforeEditing = "";

    private Boolean isShowingDetailFragment = false;
    private String currentTitle;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //inflate the view
        if(showMasterDetailLayoutInTablets()){
            //let the system choose wether to inflate sw600dp layout or default layout
            setContentView(R.layout.activity_navigation);
        }
        else{
            //force the phone layout (no master-detail)
            setContentView(R.layout.activity_navigation_phone);
        }

        //call abstract method to know if must show back button in the action bar in the first fragment
        //set as true if the NavigationActivity is launched by another Activity, so the back button
        //will automatically return to the previous activity.
        if(showBackButtonInFirstFragment() && getSupportActionBar() != null) {
            //show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        //try to put a toolbar (for master/detail template)
        if(getSupportActionBar() != null){
            Log.w("BasicFramework WARNING", "*************************\nTOOLBAR IN TABLETS NOT SHOWN PROPERLY. CHANGE YOUR STYLE TO REMOVE THE DEFAULT ACTIONBAR. ADD:\n\n <item name=\"windowActionBar\">false</item> \n <item name=\"windowNoTitle\">true</item> \n<item name=\"android:actionMenuTextColor\">#fff</item><!--replace color-->\n" +
                    "<item name=\"android:textColorSecondary\">#fff</item><!--replace color-->\n IN YOUR style.xml FILE \n*************************\n");

            //anyway remove the default toolbar. In phones it will work OK but in Master-detail layout Toolbars will not display properly
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            ((ViewGroup)myToolbar.getParent()).removeView(myToolbar);
        }
        else{
            //set our toolbar (which has a specific width for master-detail layout and full width for phone layout)
            // as the default ActionBar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(myToolbar);
        }

        //call abstract method to get the first fragment in the stack (root fragment) must be implemented in the Activity extending NavigationActivity
        Fragment first = firstFragment();
        //check if the fragment already exists, in this case we don't need to re-add it.
        if(getFragmentManager().findFragmentById(R.id.frameMaster) == null) {
            //start FragmentTransaction to add root fragment
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frameMaster, first);
            fragmentTransaction.commitAllowingStateLoss();
            //add the root fragment to the stack
            getFragmentsStack().add(first);
            getFragmentIds().add(first.getId());
        }
        else{
            //if android:configChanges="orientation|keyboardHidden|screenSize" is not set in the androidmanifest.xml, then
            //onCreate is called every time the device is rotated. If we are here this means the androidmanifest.xml has not the setting
            //and the device has been rotated (because the fragment already exists)
            // A warning is displayed
            Log.w("BasicFramework WARNING", "*************************\nTO SUPPORT INTERFACE ROTATION UPDATE YOUR AndroidManifest.xml FILE. SET: \n android:configChanges=\"orientation|keyboardHidden|screenSize\" \n IN THE <activity> TAG \n *************************\n");
        }


    }

    /**
     * Show a new Fragment with the specified animation.
     * @param newFragment
     * New fragment to be shown
     * @param animation
     * Animation type
     */
    public void pushFragment(Fragment newFragment, animationType animation, boolean showAsDetailFragmentIfPossible){

        //if frameDetail exists and want to show it:
        if(showAsDetailFragmentIfPossible && findViewById(R.id.frameDetail) != null){
            //start FragmentTransaction to add new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.frameDetail,newFragment);
            transaction.commitAllowingStateLoss();
            if(!isShowingDetailFragment)
                getMenusStack().add(null); //add a dummy menu in the stack. Will not be shown.
            isShowingDetailFragment = true;
        }
        else{
            isShowingDetailFragment = false;

            //start FragmentTransaction to add new fragment and animate it
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            //set animation depending on the animationType parameter
            if(animation == animationType.RIGHT_TO_LEFT){
                transaction.setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_left, R.animator.enter_from_right,R.animator.exit_to_right);
            }
            else if(animation == animationType.BOTTOM_TO_TOP){
                transaction.setCustomAnimations(R.animator.enter_from_bottom, R.animator.exit_to_top, R.animator.enter_from_top,R.animator.exit_to_bottom);
                //changes the back arrow in the actionBar for a cross indicating "close"
                //because the bottom to top animation indicates modal popup rather than hierarchical navigation. (just UX things)
                //noinspection ConstantConditions
                getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_close);
            }
            else if(animation == animationType.FLIP){
                //CAUTION: NOT EFFICIENT ANIMATION
                transaction.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out, R.animator.card_flip_left_in, R.animator.card_flip_left_out);
            }

            //transaction will consist in hiding current fragment and adding new fragment
            Fragment currentFragment = getFragmentsStack().get(getFragmentsStack().size()-1);
            transaction.hide(currentFragment);
            transaction.add(R.id.frameMaster,newFragment);

            //add the fragment title to the titles stack
            getTitlesStack().add(getTitle().toString());


            //add the transaction to the Android BackStack, so the back button will animate back if pressed
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

            //add the fragment to the stack
            getFragmentsStack().add(newFragment);
            getFragmentIds().add(newFragment.getId());
            //by default set the actionBarMenu to null. menu will be changed later by calling addActionBarMenu()
            getMenusStack().add(null);

            //show the back arrow in the actionBar
            if(getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        }
    }


    public void pushFragment(Fragment newFragment, animationType animation) {
        pushFragment(newFragment,animation,false);
    }

    /**
     * Removes the current Fragment and shows the previous Fragment. If it's the last Fragment, will go back to the
     * previous Activity (if there is one). The animation performed is the same that was performed when it was added,
     * but backwards.
     * This function is called automatically when user press back button, but can be called manually as well.
     */
    public void pop(){
        //if the editionModeMenu is active, the pop behaviour will be simply disable the edition mode (multiple selection mode)
        if(editionModeMenu != null){
            editionModeMenu = null;
            invalidateOptionsMenu(); //refresh actionBar menu

            //if fragment is a ListFragment subclass, tell its UniversalRowAdapter to end selection mode
            Fragment currentFragment = getFragmentsStack().get(getFragmentsStack().size()-1);
            if(Fragment.class.isAssignableFrom(ListFragment.class)){
                ((ListFragment)currentFragment).adapter.endSelectionMode();

            }
            return;
        }

        if(showBackButtonInFirstFragment() && getFragmentsStack().size() == 1){
            //finish the activity and shows the previous activity
            finish();
        }

        //if we already are in the first fragment, do nothing
        if(getFragmentsStack().size() <= 1){
            return;
        }

        //remove fragment from stack
        Fragment currentFragment = getFragmentsStack().get(getFragmentsStack().size()-1);
        getFragmentsStack().remove(currentFragment);

        //restore the arrow back icon as home up indicator (that may have been previously replaced by a cross in pushFragment())
        if(getSupportActionBar() != null)
            getSupportActionBar().setHomeAsUpIndicator(0);

        //show back arrow only if not the first fragment, or if is the first fragment but want to show it anyway
        getSupportActionBar().setDisplayHomeAsUpEnabled((getFragmentsStack().size() > 1) || showBackButtonInFirstFragment());

        //pop from Android BackStack. This will start the animation to go back to the previous fragment
        getFragmentManager().popBackStack();

        //restore Title that is shown in the actionBar
        if(getTitlesStack().size() > 0){
            setTitle(getTitlesStack().get(getTitlesStack().size()-1));
            getTitlesStack().remove(getTitlesStack().size()-1);
        }

        //restore Menu that is shown in the actionBar
        if(getMenusStack().size()>0){
            getMenusStack().remove(getMenusStack().size()-1);
            if(isShowingDetailFragment)
                getMenusStack().remove(getMenusStack().size()-1);
            //the system will call onPrepareOptionsMenu that will refresh the menu
        }

        isShowingDetailFragment = false;
    }


    /**
     * Set a single ActionBar menu, that is independent of the selected Tab
     * @param menu
     * The menu
     */
    public void replaceActionBarMenu(BFMenu menu){
        if(getMenusStack().size() > 0){
            getMenusStack().set(getMenusStack().size()-1,menu);
        }
        else{
            addActionBarMenu(menu);
        }
    }

    /**
     * Add a Menu that will be shown in the ActionBar. The menus are stored in a stack, that must match the Fragments stack,
     * so they are recovered when a Fragment is popped back.
     * @param menu
     * Menu structure that represents a menu
     */
    public void addActionBarMenu(BFMenu menu){
        if(isShowingDetailFragment){
            //inflate the detailFragment menu
            Toolbar secondaryToolbar = (Toolbar) findViewById(R.id.secondaryToolbar);
            secondaryToolbar.getMenu().clear();
            loadActionMenu(secondaryToolbar.getMenu(),menu);
            secondaryToolbar.setTitle(getTitle());
        }
        else{
            if(getMenusStack().size()>0){
                //remove last item because last item is always a null value inserted by pushFragment
                getMenusStack().remove(getMenusStack().size()-1);
            }
            getMenusStack().add(menu);
            //now onPrepareOptionsMenu will be called automatically and change the menu
            //onPrepareOptionsMenu is only called for the main ToolBar
        }

    }

    /**
     * Set a special ActionBar menu with a different color, that indicates the special action of
     * multi selecting/editing in a listView. When set, this ActionBar will be displayed.
     * Must be set to null when finished.
     * @param menu
     * Menu structure that represents a menu, or null if you want to remove it.
     */
    public void setEditingActionbarMenu(BFMenu menu){
        editionModeMenu = menu;
        invalidateOptionsMenu();
    }

    //event that is launched when user press a menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                pop();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //event that is launched when user press the android back button
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        pop();
    }

    /**
     * Change the actionBar color to a special color (defined in R.color.EditColorPrimary)
     * to indicate action of multi selecting/editing in a listView.
     */
    private void setEditingTheme(){
        //if it's already changed, exit the function
        if(currentTheme.equals("edit")){return;}

        //get the title before entering editing mode, so it can be recovered later
        titleBeforeEditing = getTitle().toString();

        //set color
        int color = ContextCompat.getColor(getApplicationContext(), R.color.EditColorPrimary);
        int colorDark = ContextCompat.getColor(getApplicationContext(), R.color.EditColorPrimaryDark);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));

            Toolbar secondaryToolbar = (Toolbar) findViewById(R.id.secondaryToolbar);
            if(secondaryToolbar != null)
                secondaryToolbar.setBackgroundDrawable(new ColorDrawable(color));
        }
        //also change the Android Status Bar color (only if version > Lollipop)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(colorDark);
        }

        currentTheme = "edit";
    }

    /**
     * Change the actionBar style to its standard color.
     */
    private void setStandardTheme(){
        //if it's already default, exit the function
        if(currentTheme.equals("default")){return;}

        //set color
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        int colorDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        if (getSupportActionBar()!= null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));

            Toolbar secondaryToolbar = (Toolbar) findViewById(R.id.secondaryToolbar);
            if(secondaryToolbar != null)
                secondaryToolbar.setBackgroundDrawable(new ColorDrawable(color));
        }
        //also change the Android Status Bar color (only if version > Lollipop)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(colorDark);
        }
        currentTheme = "default";

        //set the previous-editing title
        setTitle(titleBeforeEditing);
    }


    /**
     * operation called by the System, when need to draw the menu items in the ActionBar
     * will "inflate" the menu with items depending on the last menu in the menusStack
     * @param menu
     * Menu that will be inflated
     * @return
     * Success boolean
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //clear all items to reset
        menu.clear();

        //in Master-Detail layout, if is showing the detailFragment, we don't want detail's title in this toolbar
        //so we set the last currentTitle (before showing the detailFragment)
        if(isShowingDetailFragment){
            setTitle(currentTitle);
        }

        //is edition mode enabled, we will show a special actionBar
        if (editionModeMenu != null){
            //set editing theme which has different colors
            setEditingTheme();

            loadActionMenu(menu,editionModeMenu);

            //if title listener is set, set the title each time the menu is rendered
            //(might be useful for example if we want to display the number of items
            //selected in a table view in the ActionBar's title)
            if (editionModeMenu.titleListener != null){
                setTitle(editionModeMenu.titleListener.getText());
            }

            return true;
        }
        if(getMenusStack().size()>0){
            //set editing theme which has standard colors
            setStandardTheme();

            //get last menu from the stack
            final BFMenu m;
            if(isShowingDetailFragment)
                m = getMenusStack().get(getMenusStack().size()-2);
            else
                m = getMenusStack().get(getMenusStack().size()-1);


            loadActionMenu(menu,m);
        }

        currentTitle = getTitle().toString();
        return true;
    }

    /**
     * Inflates the Menu 'menu' with the information provided in the data structure BFMenu m
     * @param menu
     * Menu that will be inflated
     * @param m
     * Data structure that provides the menu information (presentation type, text, icon...)
     */
    private void loadActionMenu(final Menu menu,final BFMenu m){
        //security check if m is null
        if (m == null){return;}

        //if the menu has a searchListener, it means is has a special search item
        if(m.searchListener != null){
            //inflate the special search menu from layout
            getMenuInflater().inflate(R.menu.searchview, menu);
            final MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setIconifiedByDefault(true);

            //setup listeners, that must be implemented by the BFMenu when creating it
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    m.searchListener.onSearchPressed(query);//abstract method
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    m.searchListener.onQueryChanged(newText);//abstract method
                    return false;
                }
            });
            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    m.searchListener.onSearchIconDismissed();//abstract method
                    return false;
                }
            });
            mSearchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m.searchListener.onSearchIconClicked();//abstract method
                }
            });
        }

        //iterate all the regular items of the menu
        for (final BFMenuItem item:m.getItems()) {
            //first we add the menu. We will set the style later
            menu.add(item.title);
            final MenuItem currentItem = menu.getItem(menu.size()-1);

            //set the style
            if(item.menuItemType == BFMenuItem.BFMenuItemType.SHOW_AS_ACTION){
                //always show in the actionBar. If an icon is set later it will only display the icon
                currentItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            else if(item.menuItemType == BFMenuItem.BFMenuItemType.SHOW_AS_ACTION_IF_ROOM){
                //show in the actionBar if there is room. If an icon is set later it will only display the icon when showing as action
                currentItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
            else if(item.menuItemType == BFMenuItem.BFMenuItemType.SHOW_AS_MENUITEM){
                //show always in the ... popup menu. If an icon is set later it will be ignored (only shows text)
                currentItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            }

            currentItem.setIcon(item.icon);

            //setup click listener, that must be implemented by the BFMenuItem when creating it
            currentItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    item.listener.onClick(); //abstract method
                    return false;
                }
            });

        }

    }
}