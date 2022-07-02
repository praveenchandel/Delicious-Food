package com.food.foodforyou.ViewHolder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Interface.itemClickListener;
import com.food.foodforyou.R;

public class orderHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtCustomerName, txtTotalPrice,txtSoldDate;
    public RelativeLayout layout;
    public itemClickListener itemClickListner;

    public orderHistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        txtCustomerName = (TextView) itemView.findViewById(R.id.order_history_customer_name);
        txtTotalPrice = (TextView) itemView.findViewById(R.id.order_history_price);
        txtSoldDate = (TextView) itemView.findViewById(R.id.order_history_ordered_date);
        layout=(RelativeLayout)itemView.findViewById(R.id.order_history_relative_layout);
    }

    public void setItemClickListner(itemClickListener itemClickListner) {
        this.itemClickListner = itemClickListner;
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition(), false);
    }
}