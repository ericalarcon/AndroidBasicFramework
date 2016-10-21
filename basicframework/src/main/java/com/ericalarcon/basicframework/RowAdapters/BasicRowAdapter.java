package com.ericalarcon.basicframework.RowAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericalarcon.basicframework.R;

import java.util.Arrays;

@SuppressWarnings({"unused" , "WeakerAccess"})

/**
 * Created by erica on 15/07/2016.
 * custom adapter for rows with a single line of text and an optional image on the left
 */
public abstract class BasicRowAdapter extends ArrayAdapter<String> {
    private final Context context;

    //abstract methods, which will usually be implemented in the Fragment containing the listView
    //this methods will be the way to which the adapter will obtain the information to populate the rows
    public abstract String getRowText(Integer position);
    public abstract Integer getRowImageId (Integer position);
    public abstract Boolean showRowDisclosureIcon (Integer position);

    /*A ViewHolder implementation allows to avoid the (expensive) findViewById() method in an adapter.
    A ViewHolder class is typically a static inner class in your adapter which holds references to the relevant views. in your layout. This reference is assigned to the row view as a tag via the setTag() method.
    If we receive a convertView object, we can get the instance of the ViewHolder via the getTag() method and assign the new attributes to the views via the ViewHolder reference.
    While this sounds complex this is approximately 15 % faster then using the findViewById() method.*/
    private static class ViewHolder {
        TextView text;
        ImageView image;
        ImageView disclosureIcon;
        View rowSeparator;
    }


    protected BasicRowAdapter(Context context) {
        //pass an empty string to the super constructor because we don't need to know the list of items
        //this is managed by UniversalRowAdapter class
        super(context,-1, Arrays.asList(new String[0]));
        this.context = context;
    }


    /**
     * Gets the view for the row in the position 'position' of the tableview.
     * This function is designed to be used only in UniversalRowAdapter Class
     * @param position
     * Position of the item in the tableview
     * @param convertView
     * Reusable view (parameter given by the UniversalRowAdapter's getView)
     * @param parent
     * Parent view(parameter given by the UniversalRowAdapter's getView)
     * @return
     * Row view
     */
    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        //get the text with this abstract method, which will usually be implemented in the Fragment containing the listView
        String rowText = getRowText(position);
        if(rowText == null) rowText = "";

        //get the image with this abstract method, which will usually be implemented in the Fragment containing the listView
        Integer rowImageId = getRowImageId(position);
        if(rowImageId == null) rowImageId = 0;

        //get the disclosure icon (arrow > showing navigation) with this abstract method, which will usually be implemented in the Fragment containing the listView
        Boolean showDisclosure = showRowDisclosureIcon(position);
        if(showDisclosure == null) showDisclosure = false;

        //convertView is a reusable view (used for performance issues)
        View rowView = convertView;

        //check if this row is of the same class as the convertView class reusable view.
        //because in UniversalRowAdapter we can use different adapters, the reusable view may be unusable
        Boolean isCurrentClass = true;
        if (rowView != null && rowView.getTag() != null)
            isCurrentClass = rowView.getTag().getClass().equals(ViewHolder.class);

        //if the reusable view is null or is of different class, we must inflate it (re-create it)
        if (rowView == null || !isCurrentClass || rowView.getTag() == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.row_basicrowlayout, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.rowText);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.rowImage);
            viewHolder.disclosureIcon = (ImageView) rowView.findViewById(R.id.rowDisclosureIndicator);
            viewHolder.rowSeparator = rowView.findViewById(R.id.rowSeparator);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        //populate the row items with the obtained information
        holder.text.setText(rowText);
        holder.image.setImageResource(rowImageId);

        boolean imageVisibility = rowImageId != 0;
        if (imageVisibility) holder.image.setVisibility(View.VISIBLE);
        else holder.image.setVisibility(View.GONE);

        if(showDisclosure) holder.disclosureIcon.setVisibility(View.VISIBLE);
        else holder.disclosureIcon.setVisibility(View.GONE);

        //return our shining, beautiful row
        return rowView;
    }


}

