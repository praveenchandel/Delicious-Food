package com.food.foodforyou;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Adapter.RestaurantItemsAdapter;
import com.food.foodforyou.Adapter.SimilarProductAdapter;
import com.food.foodforyou.Model.isHasToShowCategory;
import com.food.foodforyou.SQLDatabase.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

public class SearchInsideRestaurant extends AppCompatActivity {

    private ListView searchListView;
    private RestaurantItemsAdapter listVi;
    private String searchItem="";
    private FirebaseAuth mAuth;

    private ImageView back_arrow_icon;
    private ImageView cart_icon;

    private LinearLayout searchLayout;
    private LinearLayout nothingMatchSearchLayout;

    private ListView myList;
    private TextView deleteHistoryButton;
    private SearchView searchView;

    // creating a DatabaseHelper class object
    private DatabaseHelper myDb;

    private ArrayList<String> searchHistory=new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private ImageView nothingFoundImageView;
    private TextView nothingFoundTextView;

    private boolean isSearched=false;

    private ArrayList<isHasToShowCategory> searchMatchedProducts=new ArrayList<isHasToShowCategory>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_inside_restaurant);

        initializingViews();

        // initializing database
        myDb=new DatabaseHelper(this);

        viewAllData();

        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,searchHistory);
        myList.setAdapter(adapter);

        back_arrow_icon=(ImageView)findViewById(R.id.search_inside_toolbar_back_arrow_search);
        cart_icon=(ImageView)findViewById(R.id.search_inside_toolbar_cart_icon_search);

        back_arrow_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cart_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),CartViewActivity.class);
                startActivity(intent);
            }
        });

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){

            Intent intent = new Intent(SearchInsideRestaurant.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // show all the products
        Toast.makeText(SearchInsideRestaurant.this, RestaurantItemsActivity.allProductsOfRestaurant.size() + " Items found", Toast.LENGTH_SHORT).show();
        listVi = new RestaurantItemsAdapter(SearchInsideRestaurant.this, RestaurantItemsActivity.allProductsOfRestaurant);
        searchListView.setAdapter(listVi);

        // working on search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // hiding list view
                searchLayout.setVisibility(View.GONE);
                searchListView.setVisibility(View.VISIBLE);

                searchItem=query;
                searchingAlgorithm();

                // if query is already present in database then we will not save it else we will
                boolean flag=true;
                for(int i=0;i<searchHistory.size();i++){
                    if(searchHistory.get(i).equals(query))
                        flag=false;
                }

                if(flag){
                    // saving the query in the database
                    myDb.insertData(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // this will run every time when any char of query changes
                adapter.getFilter().filter(newText);
                int count=adapter.getCount();

                // if nothing matches in recent search
                if(count==0){
                    nothingMatchSearchLayout.setVisibility(View.VISIBLE);
                    myList.setVisibility(View.GONE);
                }else{
                    nothingMatchSearchLayout.setVisibility(View.GONE);
                    myList.setVisibility(View.VISIBLE);
                }

                deleteHistoryButton.setVisibility(View.VISIBLE);
                searchListView.setVisibility(View.GONE);
                nothingFoundTextView.setVisibility(View.GONE);
                nothingFoundImageView.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);
                return false;
            }
        });

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                searchLayout.setVisibility(View.GONE);
                searchListView.setVisibility(View.VISIBLE);
                searchItem=parent.getItemAtPosition(position).toString();
                searchingAlgorithm();
            }
        });

        /*
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(searchEdit.getText().toString())){
                        searchItem=searchEdit.getText().toString().trim();

                        // hiding the keyboard and searching
                            hideKeyboard(v);
                            searchingAlgorithm();
                    }
                    return true;
                }
                return false;
            }
        });

        */

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(SearchInsideRestaurant.this, ProductDetails.class);
                if (isSearched){
                    intent.putExtra("Pid", searchMatchedProducts.get(position).product.getProductId());
                }else {
                    intent.putExtra("Pid", RestaurantItemsActivity.allProductsOfRestaurant.get(position).product.getProductId());
                }
                startActivity(intent);
            }
        });
    }

    // accessing all the data present in the database
    public void viewAllData(){

        Cursor res=myDb.getAllData();

        // moving cursor to the first row
        res.moveToFirst();

        // accessing all the rows one by one
        for(int i=0;i<res.getCount();i++){

            searchHistory.add(res.getString(0));
            // moving cursor to next row
            res.moveToNext();
        }

        // reversing the arraylist
        for(int i=0;i<searchHistory.size()/2;i++){
            String s=searchHistory.get(i);
            searchHistory.set(i,searchHistory.get(searchHistory.size()-i-1));
            searchHistory.set(searchHistory.size()-i-1,s);
        }
    }

    // function to hide the keyboard
    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }

    // searching algorithm
    private void searchingAlgorithm(){

        if(searchItem.equals("")){
            // show all the items
            isSearched=false;
            searchListView.setVisibility(View.VISIBLE);
            nothingFoundTextView.setVisibility(View.GONE);
            nothingFoundImageView.setVisibility(View.GONE);

            listVi = new RestaurantItemsAdapter(SearchInsideRestaurant.this, RestaurantItemsActivity.allProductsOfRestaurant);
            searchListView.setAdapter(listVi);
        }else{
            searchMatchedProducts.clear();
            // perform searching
            for(int k=0;k<RestaurantItemsActivity.allProductsOfRestaurant.size();k++){

                String s1=searchItem.toLowerCase();
                int M = s1.length();

                String s2=RestaurantItemsActivity.allProductsOfRestaurant.get(k).product.getProductName().toLowerCase();
                int N = s2.length();

                /* A loop to slide pat[] one by one */
                for (int i = 0; i <= N - M; i++) {
                    int j;

            /* For current index i, check for
            pattern match */
                    for (j = 0; j < M; j++)
                        if (s2.charAt(i + j)
                                != s1.charAt(j))
                            break;

                    // if result matches
                    if (j == M){
                        i=N;
                        searchMatchedProducts.add(RestaurantItemsActivity.allProductsOfRestaurant.get(k));
                    }
                }
            }
            // searching is done
            if (searchMatchedProducts.size()==0){
                // nothing found will be shown
                searchListView.setVisibility(View.GONE);
                isSearched=true;
                nothingFoundTextView.setVisibility(View.VISIBLE);
                nothingFoundImageView.setVisibility(View.VISIBLE);
            }else{
                // show results
                isSearched=true;
                searchListView.setVisibility(View.VISIBLE);
                nothingFoundTextView.setVisibility(View.GONE);
                nothingFoundImageView.setVisibility(View.GONE);

                decideThatHasToShowCategory();
            }
        }
    }

    private void decideThatHasToShowCategory(){
        String cat="NO";

        for (int i=0;i<searchMatchedProducts.size();i++){

            if (cat.equals(searchMatchedProducts.get(i).product.getCategory())){
                searchMatchedProducts.get(i).setHasTo(View.GONE);
            }else {
                searchMatchedProducts.get(i).setHasTo(View.VISIBLE);
            }
            cat=searchMatchedProducts.get(i).product.getCategory();
        }

        // show all matched products
        Toast.makeText(SearchInsideRestaurant.this, searchMatchedProducts.size() + " Items matches..", Toast.LENGTH_SHORT).show();
        listVi = new RestaurantItemsAdapter(SearchInsideRestaurant.this, searchMatchedProducts);
        searchListView.setAdapter(listVi);

    }

    private void initializingViews(){

        searchView=(SearchView) findViewById(R.id.search_inside_search_edit_text);

        searchListView=(ListView)findViewById(R.id.search_inside_activity_list_view);

        nothingFoundImageView=(ImageView)findViewById(R.id.search_inside_activity_nothing_found_image);
        nothingFoundTextView=(TextView)findViewById(R.id.search_inside_activity_nothing_found_text);

        myList=(ListView)findViewById(R.id.search_inside_activity_my_list);
        deleteHistoryButton=(TextView)findViewById(R.id.search_inside_activity_delete_Button_text);

        searchLayout=(LinearLayout)findViewById(R.id.search_inside_activity_searching_layout);
        nothingMatchSearchLayout=(LinearLayout)findViewById(R.id.search_inside_activity_nothing_match_in_search);
    }
}