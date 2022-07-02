package com.food.foodforyou.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.food.foodforyou.ProductDetails;
import com.food.foodforyou.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExampleBottomSheetDialog extends BottomSheetDialogFragment {
 //   private BottomSheetListener mListener;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.offers_bottom_sheet_layout, container, false);

        TextView noOfferText,offer1PercentageOff,offer1MinimumPurchase,offer2PercentageOff,offer2MinimumPurchase;
        CardView offer1Linear,offer2Linear;

        noOfferText=(TextView)v.findViewById(R.id.no_offer_available_text);

        offer1PercentageOff=(TextView)v.findViewById(R.id.offer1_product_percent_off);
        offer1MinimumPurchase=(TextView)v.findViewById(R.id.offer1_product_minimum_purchase);

        offer2PercentageOff=(TextView)v.findViewById(R.id.offer2_product_percent_off);
        offer2MinimumPurchase=(TextView)v.findViewById(R.id.offer2_product_minimum_purchase);

        offer1Linear=(CardView) v.findViewById(R.id.offer_one_card_layout);
        offer2Linear=(CardView) v.findViewById(R.id.offer_two_card_layout);

        if (ProductDetails.offer1.getStatus().equals("YES")){
            offer1Linear.setVisibility(View.VISIBLE);
            offer1PercentageOff.setText("Buy " + ProductDetails.offer1.getMin_quantity() + " or More");
            offer1MinimumPurchase.setText("& Get " + ProductDetails.offer1.getPercentage_off() + "% Off upto ₹" + ProductDetails.offer1.getMax_off());
        }

        if (ProductDetails.offer2.getStatus().equals("YES")){
            offer2Linear.setVisibility(View.VISIBLE);
            offer2PercentageOff.setText("Buy " + ProductDetails.offer2.getMin_quantity() + " or More");
            offer2MinimumPurchase.setText("& Get " + ProductDetails.offer2.getPercentage_off() + "% Off upto ₹" + ProductDetails.offer2.getMax_off());
        }

        if (ProductDetails.offer1.getStatus().equals("NO") && ProductDetails.offer2.getStatus().equals("NO")){
            noOfferText.setText("No offers Available");
        }else {
            noOfferText.setText("Available Offers");
        }

        return v;
    }

    // setting the AppBottomSheetDialogTheme style to the bottom sheet
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }
}