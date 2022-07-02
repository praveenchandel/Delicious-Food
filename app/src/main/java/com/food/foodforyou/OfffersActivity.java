package com.food.foodforyou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Adapter.offersAdapter;
import com.food.foodforyou.Model.offers;

public class OfffersActivity extends AppCompatActivity {

    private ImageView nothingFoundImage;
    private TextView nothingFoundText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offfers);

        Toolbar toolbar = findViewById(R.id.toolbar_all_available_offers);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        nothingFoundImage=(ImageView)findViewById(R.id.offers_activity_nothing_found_image);
        nothingFoundText=(TextView)findViewById(R.id.offers_activity_nothing_found_text);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        offersAdapter listVi = new offersAdapter(this, CartViewActivity.allOffersInCartList);
        ListView listView = (ListView) findViewById(R.id.offers_listView);
        listView.setAdapter(listVi);

        if (CartViewActivity.allOffersInCartList.size()==0){
            nothingFoundImage.setVisibility(View.VISIBLE);
            nothingFoundText.setVisibility(View.VISIBLE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                offers offer=CartViewActivity.allOffersInCartList.get(position);

                // check offer is applicable or not
                // if yes then calculate discount
                if (Integer.parseInt(offer.getProduct_quantity())+1>Integer.parseInt(offer.getMin_quantity())){

                    int less=Integer.parseInt(offer.getProduct_price())*Integer.parseInt(offer.getPercentage_off());

                    // if offer is not on cart or on restaurant
                    if(!offer.getProduct_price().equals(offer.getProduct_quantity())){
                        less=less*Integer.parseInt(offer.getProduct_quantity());
                    }

                    less=less/100;
                    if (less>Integer.parseInt(offer.getMax_off())){
                        less=Integer.parseInt(offer.getMax_off());
                    }
                    CartViewActivity.lastAmount.setTotalDiscount(less);
                    CartViewActivity.appliedOfferName=offer.getProductName();
                    Toast.makeText(OfffersActivity.this, "Offer Applied Successfully..", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(OfffersActivity.this, "minimum purchase is " + offer.getMin_quantity() + " for applying this offer", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}