package com.food.foodforyou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RulesActivity extends AppCompatActivity {

    private TextView openSourceLibrariesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setting the theme
        setTheme(R.style.CollapsingToolbarTheme);

        setContentView(R.layout.activity_rules);


        // this code will change the color of the status bar
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDarkCollapsing));

//        Toolbar toolbar = findViewById(R.id.toolbar_rules_activity);
//        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        openSourceLibrariesButton=(TextView)findViewById(R.id.open_source_libraries);

        openSourceLibrariesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libraries_used();
            }
        });
    }

    public void libraries_used() {
        AttributionPresenter attributionPresenter = new AttributionPresenter.Builder(RulesActivity.this)
                .addAttributions(
                        new Attribution.Builder("Lovely Dialog")
                                .addCopyrightNotice("Copyright (c) 2016 Yaroslav Shevchuk")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/yarolegovich/LovelyDialog")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("AttributionPresenter")
                                .addCopyrightNotice("Copyright (c) 2017 Francisco José Montiel Navarro")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("SwipeSelector")
                                .addCopyrightNotice("Copyright (c) 2016 Iiro Krankka")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/roughike/SwipeSelector")
                                .build()
                ).addAttributions(
                        new Attribution.Builder("Picasso")
                                .addCopyrightNotice("Copyright 2016, Arthur Teplitzki, 2013, Edmodo, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/ArthurHub/Android-Image-Cropper")
                                .build()
                )
                .build();
        attributionPresenter.showDialog("Libraries Used");
    }
}