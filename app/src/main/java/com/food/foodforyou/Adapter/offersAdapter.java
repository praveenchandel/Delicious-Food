package com.food.foodforyou.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.food.foodforyou.Model.offers;
import com.food.foodforyou.R;

import java.util.ArrayList;

public class offersAdapter extends ArrayAdapter<offers> {

    public offersAdapter(Activity context, ArrayList<offers> worlds){
        super(context,0,worlds);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.offers_list, parent, false);

        final offers currentOffer = getItem(position);

        TextView productName=(TextView)listItemView.findViewById(R.id.offers_product_name);
        TextView percentageOff=(TextView)listItemView.findViewById(R.id.offers_product_percent_off);
        TextView minimumPurchase=(TextView)listItemView.findViewById(R.id.offers_product_minimum_purchase);
        //TextView maximumOff=(TextView)listItemView.findViewById(R.id.offers_product_maximum_off);

        productName.setText(currentOffer.getProductName());
        percentageOff.setText("Buy min. " + currentOffer.getMin_quantity() + " or More");
        minimumPurchase.setText("& Get " + currentOffer.getPercentage_off() + "% Off upto ₹" + currentOffer.getMax_off());

        return listItemView;
    }
}

