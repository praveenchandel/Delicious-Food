package com.food.foodforyou.BottomSheet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.food.foodforyou.ProductDetails;
import com.food.foodforyou.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CustomizedBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;
    private int isFullProduct=0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottum_sheet_add_new_customization, container, false);

        TextView dishName1,dishPrice1,dishTotalPrice1,dishName2,dishPrice2,dishTotalPrice2;
        final TextView dishName;
        RelativeLayout relativeLayout1,relativeLayout2;
        ImageView recycleImage1,recycleImage2;
        Button runCurrentQuery;

        dishName=(TextView)v.findViewById(R.id.bottom_sheet_customize_full_dish_name_title);

        dishName1=(TextView)v.findViewById(R.id.bottom_sheet_customize_full_dish_name);
        dishPrice1=(TextView)v.findViewById(R.id.bottom_sheet_customize_full_dish_price_full);
        dishTotalPrice1=(TextView)v.findViewById(R.id.bottom_sheet_customize_full_dish_price_total);

        dishName2=(TextView)v.findViewById(R.id.bottom_sheet_customize_dish_name);
        dishPrice2=(TextView)v.findViewById(R.id.bottom_sheet_customize_dish_price_full);
        dishTotalPrice2=(TextView)v.findViewById(R.id.bottom_sheet_customize_dish_price_total);

        relativeLayout1=(RelativeLayout)v.findViewById(R.id.bottom_sheet_customize_relative_layout_1);
        relativeLayout2=(RelativeLayout)v.findViewById(R.id.bottom_sheet_customize_relative_layout_2);

        recycleImage1=(ImageView)v.findViewById(R.id.bottom_sheet_customize_food_delete_icon1);
        recycleImage2=(ImageView)v.findViewById(R.id.bottom_sheet_customize_food_delete_icon2);

        runCurrentQuery=(Button)v.findViewById(R.id.bottom_sheet_customize_make_changes);

        dishName.setText(ProductDetails.dishName);

        if (!ProductDetails.full_quantity.equals("")) {
            if (Integer.parseInt(ProductDetails.full_quantity) > 0) {
                isFullProduct++;
                relativeLayout1.setVisibility(View.VISIBLE);
                dishName1.setText(ProductDetails.dishName);
                dishPrice1.setText("₹ " + ProductDetails.fullPlatePrice);
                dishTotalPrice1.setText("x" + ProductDetails.full_quantity + "\n₹ " + ProductDetails.fullPlatePrice * Integer.parseInt(ProductDetails.full_quantity));
            }
        }

        if (!ProductDetails.half_quantity.equals("")) {
            if (Integer.parseInt(ProductDetails.half_quantity) > 0) {
                isFullProduct++;
                relativeLayout2.setVisibility(View.VISIBLE);
                dishName2.setText(ProductDetails.dishName);
                dishPrice2.setText("₹ " + ProductDetails.halfPlatePrice + " Half");
                dishTotalPrice2.setText("x" + ProductDetails.half_quantity + "\n₹ " + ProductDetails.halfPlatePrice * Integer.parseInt(ProductDetails.half_quantity));
            }
        }

        runCurrentQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked("Run");
                dismiss();
            }
        });

        recycleImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullProduct==2){
                    mListener.onButtonClicked("Delete_Full");
                }else {
                    mListener.onButtonClicked("Remove_Product");
                }
                dismiss();
            }
        });

        recycleImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullProduct==2){
                    mListener.onButtonClicked("Delete_Half");
                }else {
                    mListener.onButtonClicked("Remove_Product");
                }
                dismiss();
            }
        });

        return v;
    }

    // setting the AppBottomSheetDialogTheme style to the bottom sheet
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }
}
