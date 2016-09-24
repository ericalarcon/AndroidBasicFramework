package com.ericalarcon.basicframework.Templates;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.ericalarcon.basicframework.R;
import com.ericalarcon.basicframework.RowAdapters.UniversalRowAdapter;
@SuppressWarnings("unused")
/**
 * Created by erica on 22/07/2016.
 * Provides a template for a Fragment with a ListView with UniversalRowAdapter
 */
public abstract class ListFragment extends Fragment {
    /**
     * Default constructor, mandatory
     */
    public ListFragment(){}

    private ListView listView = null;
    //listView lazy instantiation
    public ListView getListView() {
        if (listView == null){
            if(getView() != null)
                listView = (ListView) getView().findViewById(R.id.list);
        }
        return listView;
    }
    public UniversalRowAdapter adapter;

    //abstract functions that must be implemented by ListFragment child
    public abstract String title(); //must return the title that will be shown in the ActionBar
    public abstract UniversalRowAdapter getAdapter(Context context, ListView listview);
    public abstract FloatingButton floatingActionButton();
    public abstract void onCreateFinished();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); //in case we want to add some menus to ActionBar
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        adapter = getAdapter(getActivity(),getListView()); //abstract operation

        //set the adapter
        getListView().setAdapter(adapter);

        //set the title of the ActionBar
        if(title() != null)
            getActivity().setTitle(title());

        //reset the actionBar to refresh its contents
        getActivity().invalidateOptionsMenu();


        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab);

        if(floatingActionButton()== null){
            fab.hide();
        }
        else{
            fab.setImageResource(floatingActionButton().buttonImage());

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    floatingActionButton().onClick();
                }
            });
        }


        onCreateFinished();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(title() != null)
            getActivity().setTitle(title());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listview_withswiperefresh, container, false);
    }




    public abstract class FloatingButton{
        public abstract void onClick();
        public abstract Integer buttonImage();
    }
}
