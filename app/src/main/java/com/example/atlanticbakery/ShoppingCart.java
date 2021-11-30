package com.example.atlanticbakery;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.atlanticbakery.Adapter.CustomExpandableListAdapter;
import com.example.atlanticbakery.Helper.FragmentNavigationManager_ShoppingCart;
import com.example.atlanticbakery.Interface.NavigationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShoppingCart extends AppCompatActivity {
    Connection con;
    DatabaseHelper myDb;
    DatabaseHelper8 myDb8;
    DatabaseHelper7 myDb7;
    String discountID = "";
    String discountName = "";
    long mLastClickTime = 0;
    private RequestQueue mQueue;

    ui_class uic = new ui_class();
    prefs_class pc = new prefs_class();
    navigation_class navc = new navigation_class();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    int userID = 0;

    DecimalFormat df = new DecimalFormat("#,###.00");
    DecimalFormat dfInt = new DecimalFormat("#,###.00");
    String salesType = "";

    private OkHttpClient client;

    Menu menu;
    String title,hiddenTitle;
    String gCustomer,gDiscountResponse = "";

    CountDownTimer countDownTimer = null;
    private BadgeDrawerArrowDrawable badgeDrawable;
    notification_class notifc = new notification_class(this);
    int sapCount = 0, transferCount = 0, offlineCount = 0, totalCount = 0;
    int isAgent = 0;
    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);
        myDb7 = new DatabaseHelper7(this);

        OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
        OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
        client = OkHttpBuilder.build();
        mQueue = Volley.newRequestQueue(this);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        title = getIntent().getStringExtra("title");
        hiddenTitle = getIntent().getStringExtra("hiddenTitle");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView) findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_ShoppingCart.getmInstance(this);
        genData();
        addDrawersItem();
        setupDrawer();

        if (savedInstanceState == null) {
            selectFirstItemDefault();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String fullName = Objects.requireNonNull(sharedPreferences.getString("fullname", ""));
        View listReaderView = getLayoutInflater().inflate(R.layout.nav_header, null,false);
        TextView txtName = listReaderView.findViewById(R.id.txtName);
        txtName.setText("Name: " +fullName +  "\nVersion: " + BuildConfig.VERSION_NAME);
        expandableListView.addHeaderView(listReaderView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        String branch = Objects.requireNonNull(sharedPreferences.getString("branch", ""));
        String whse = Objects.requireNonNull(sharedPreferences.getString("whse", ""));
        LayoutInflater inflator = LayoutInflater.from(this);
        View v = inflator.inflate(R.layout.custom_action_bar, null);
        ((TextView)v.findViewById(R.id.title)).setText(title);
        ((TextView)v.findViewById(R.id.title2)).setText(branch + " - " + whse);
        this.getSupportActionBar().setCustomView(v);

        loadData();
        startTimer();
    }

    public void startTimer(){
        countDownTimer = new CountDownTimer(5000,5000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                new Thread(new Runnable() {
                    public void run() {
//                        sapCount = isAgent > 0 ? 0 : notifc.notif("/api/sapb1/getit");
//                        transferCount = notifc.notif("/api/inv/trfr/forrec?mode=For Sale Items");
                        offlineCount = myDb7.getCount();
                        totalCount = sapCount + transferCount + offlineCount;
                        badgeDrawable.setText(String.valueOf(totalCount));
                        countDownTimer.start();
                    }
                }).start();
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void setupDrawer(){
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.open,R.string.close){
        };
        badgeDrawable = new BadgeDrawerArrowDrawable(getSupportActionBar().getThemedContext());
        mDrawerToggle.setDrawerArrowDrawable(badgeDrawable);
//        sapCount = isAgent > 0 ? 0 : notifc.notif("/api/sapb1/getit");
//        transferCount = notifc.notif("/api/inv/trfr/forrec?mode=For Sale Items");
        offlineCount = myDb7.getCount();
        totalCount = sapCount + transferCount + offlineCount;
        badgeDrawable.setText(String.valueOf(totalCount));
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

    }

    public void selectFirstItemDefault(){
        if(navigationManager != null){
            String firstItem = listTitle.get(0);
            navigationManager.showFragment(firstItem);
            getSupportActionBar().setTitle(firstItem);
        }
    }

    public void addDrawersItem(){
        adapter = new CustomExpandableListAdapter(this, listTitle, listChild,totalCount,sapCount,transferCount,offlineCount);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedItem = ((List)listChild.get(listTitle.get(groupPosition)))
                        .get(childPosition).toString();
                getSupportActionBar().setTitle(selectedItem);
                Intent intent;
                if(selectedItem.equals("Receive from SAP")){
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Receive from SAP");
                    intent.putExtra("hiddenTitle", "API Received from SAP");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("System Receive Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "System Transfer Item");
                    intent.putExtra("hiddenTitle", "API System Transfer Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Manual Receive Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Manual Receive Item");
                    intent.putExtra("hiddenTitle", "API Received Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("System Transfer Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "System Transfer Item");
                    intent.putExtra("hiddenTitle", "API Transfer Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Sales")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Sales");
                    intent.putExtra("hiddenTitle", "API Menu Items");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Goods Issue")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Goods Issue");
                    intent.putExtra("hiddenTitle", "API Issue For Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Receive Goods Issue")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Receive Goods Issue");
                    intent.putExtra("hiddenTitle", "API Confirm Issue For Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Finish Goods Receive")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Finish Goods Receive");
                    intent.putExtra("hiddenTitle", "API Received from Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Pending Item Transfer Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pending Item Transfer Request");
                    intent.putExtra("hiddenTitle", "API Target For Delivery");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Item Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request");
                    intent.putExtra("hiddenTitle", "API Item Request");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Item Request For Transfer")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request For Transfer");
                    intent.putExtra("hiddenTitle", "API Item Request For Transfer");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Production Order List")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Production Order List");
                    intent.putExtra("hiddenTitle", "API Production Order List");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Change Password")){
                    changePassword();
                }
                else if(selectedItem.equals("Logout")){
                    onBtnLogout();
                }
                else if(selectedItem.equals("Logs")){
                    intent = new Intent(getBaseContext(), API_SalesLogs.class);
                    intent.putExtra("title", "Inventory Logs");
                    intent.putExtra("hiddenTitle", "API Inventory Logs");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Cut Off")){
                    intent = new Intent(getBaseContext(), CutOff.class);
                    intent.putExtra("title", "Cut Off");
                    intent.putExtra("hiddenTitle", "API Cut Off");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Offline Pending Transactions")){
                    intent = new Intent(getBaseContext(), OfflineList.class);
                    intent.putExtra("title", "Offline Pending Transactions");
                    intent.putExtra("hiddenTitle", "API Offline List");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Inventory Count")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Inventory Count");
                    intent.putExtra("hiddenTitle", "API Inventory Count");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Inventory Count Variance")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Inventory Count Variance");
                    intent.putExtra("hiddenTitle", "API Inventory Count Variance");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Pull out Request Variance")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request Variance");
                    intent.putExtra("hiddenTitle", "API Pull Out Count Variance");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Receive Pullout From Ending Bal")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Receive Pullout From Ending Bal");
                    intent.putExtra("hiddenTitle", "API Pull Out Request Confirmation");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Pull out Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request");
                    intent.putExtra("hiddenTitle", "API Pull Out Count");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Pull out Request Variance")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request Variance");
                    intent.putExtra("hiddenTitle", "API Pull Out Count Variance");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Final Count & Pull out Confirmation")){
                    intent = new Intent(getBaseContext(), API_InventoryConfirmation.class);
                    intent.putExtra("title", "Final Count & Pull out Confirmation");
                    intent.putExtra("hiddenTitle", "API Inventory Count Confirmation");
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }

    public void genData(){
        List<String>title = navc.getTitles(getString(R.string.app_name));
        listChild = new TreeMap<>();
        int iterate = 5;
        int titleIndex = 0;
        while (iterate >= 0){
            listChild.put(title.get(titleIndex),navc.getItem(title.get(titleIndex)));
            titleIndex += 1;
            iterate -= 1;
        }
        listTitle = new ArrayList<>(listChild.keySet());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu item) {
        getMenuInflater().inflate(R.menu.main_menu,item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item))
            adapter = new CustomExpandableListAdapter(this, listTitle, listChild,totalCount,sapCount, transferCount,offlineCount);
        expandableListView.setAdapter(adapter);
        return true;
    }


    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(ShoppingCart.this);
        myDialog.setCancelable(false);
        myDialog.setMessage("*Enter Your New Password");
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 0, 40, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,0,20);
        EditText txtPassword = new EditText(getBaseContext());
        txtPassword.setTextSize(15);
        txtPassword.setGravity(View.TEXT_ALIGNMENT_CENTER);
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());
        txtPassword.setLayoutParams(layoutParams);
        layout.addView(txtPassword);

        CheckBox checkPassword = new CheckBox(getBaseContext());
        checkPassword.setText("Show Password");
        checkPassword.setTextSize(15);
        checkPassword.setGravity(View.TEXT_ALIGNMENT_CENTER);
        checkPassword.setLayoutParams(layoutParams);

        checkPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    txtPassword.setTransformationMethod(null);
                }else{
                    txtPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });

        layout.addView(checkPassword);

        myDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(txtPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(getBaseContext(), "Password field is required", Toast.LENGTH_SHORT).show();
                }else{

                    AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ShoppingCart.myChangePassword myChangePassword = new ShoppingCart.myChangePassword(txtPassword.getText().toString().trim());
                                    myChangePassword.execute();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            }
        });

        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        myDialog.setView(layout);
        myDialog.show();
    }

    private class myChangePassword extends AsyncTask<String, Void, String> {
        String password = "";
        LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        public myChangePassword(String sPassword) {
            password = sPassword;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("password", password);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
                OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
                OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
                OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
                client = OkHttpBuilder.build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/user/change_pass")
                        .method("PUT", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if(s != null) {
                    JSONObject jsonObjectResponse = new JSONObject(s);
                    loadingDialog.dismissDialog();
                    Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();

                    if(jsonObjectResponse.getBoolean("success")){

                        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(ShoppingCart.this);
                                        pc.removeToken(ShoppingCart.this);
                                        startActivity(uic.goTo(ShoppingCart.this, MainActivity.class));
                                        finish();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }



    public void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(ShoppingCart.this);
                        startActivity(uic.goTo(ShoppingCart.this, MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @SuppressLint({"SetTextI18n", "ResourceType", "RtlHardcoded"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loadData() {
        final LinearLayout layout = findViewById(R.id.parentLayout);
        layout.removeAllViews();

        final LinearLayout.LayoutParams layoutParamsBtnback = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParamsBtnback.setMargins(20,20,0,10);
        MaterialButton btnBack = new MaterialButton(ShoppingCart.this);
        btnBack.setLayoutParams(layoutParamsBtnback);
        btnBack.setText("BACK");
        btnBack.setCornerRadius(10);
        btnBack.setTextSize(13);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        layout.addView(btnBack);


        final LinearLayout.LayoutParams layoutParamsNoItems = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParamsNoItems.setMargins(50, 20, 50, 0);
        final LinearLayout.LayoutParams layoutParamsLblError = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParamsLblError.setMargins(30,0,20,20);
        int totalItems = myDb.countItems();
        if (totalItems <= 0) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParamsNoItems);
            imageView.setImageResource(R.drawable.ic_sad_face);
            layout.addView(imageView);
            TextView lblNoItemFound = new TextView(this);
            lblNoItemFound.setLayoutParams(layoutParamsNoItems);
            lblNoItemFound.setText("Your Shopping Cart is currently empty. Tap the button below to shop for items.");
            lblNoItemFound.setBackgroundColor(Color.WHITE);
            lblNoItemFound.setTextSize(15);
            lblNoItemFound.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(lblNoItemFound);

            MaterialButton btnGotoShoppingCart = new MaterialButton(ShoppingCart.this);
            btnGotoShoppingCart.setText("Go Menu Items");
            btnGotoShoppingCart.setLayoutParams(layoutParamsNoItems);
            btnGotoShoppingCart.setCornerRadius(10);
            btnGotoShoppingCart.setTextColor(Color.WHITE);

            btnGotoShoppingCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Handler handler = new Handler();
                    LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
                    loadingDialog.startLoadingDialog();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                try {
                                    wait(10);
                                } catch (InterruptedException ex) {

                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    loadingDialog.dismissDialog();
                                }
                            });
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            });
            layout.addView(btnGotoShoppingCart);
        } else {
            LinearLayout.LayoutParams layoutParamsHorizontal = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(ShoppingCart.this);
            horizontalScrollView.setLayoutParams(layoutParamsHorizontal);

            LinearLayout.LayoutParams layoutParamsPay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParamsBtnRemoveItem = new LinearLayout.LayoutParams(70, 70);
            LinearLayout.LayoutParams layoutParamsItems = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParamsBtnPay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParamsView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            LinearLayout.LayoutParams layoutParamsQuantity = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams layoutParamsTableLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams layoutParamsDiscount = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lpDiscountType = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsBtnRemoveItem.gravity = Gravity.RIGHT;

            layoutParamsQuantity.setMargins(20, 0, 20, 20);
            lpDiscountType.setMargins(20, 20, 20, 20);
            layoutParamsDiscount.setMargins(20, 0, 20, 20);
            layoutParamsTableLayout.setMargins(0,20,0,20);
            layoutParamsBtnPay.setMargins(30,0,30,0);
            layout.setOrientation(LinearLayout.VERTICAL);


            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);

            layout.addView(horizontalScrollView);
            final TableLayout tableLayout = new TableLayout(this);
            tableLayout.setStretchAllColumns(true);
            tableLayout.setLayoutParams(layoutParamsTableLayout);
            tableLayout.setBackgroundColor(Color.rgb(244,244,244));

            String[] columns = {"Item","Price,","Qty.","Disc.", "Total","Action"};

            TableRow tableColumn = new TableRow(this);
            for (String s : columns) {
                TextView lblColumn1 = new TextView(this);
                lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn1.setText(s);
                lblColumn1.setPadding(10, 0, 10, 0);
                lblColumn1.setTextSize(13);
                tableColumn.addView(lblColumn1);
            }
            tableLayout.addView(tableColumn);
            horizontalScrollView.addView(tableLayout);


            final Cursor result = myDb.getAllData();
               if(!result.equals(null)){
                   while (result.moveToNext()) {
                       final TableRow tableRow = new TableRow(this);
                       tableRow.setBackgroundColor(Color.WHITE);
                       final String item = result.getString(1);
                       double price = result.getDouble(3);
                       double quantity = result.getDouble(2);
                       double discount = result.getDouble(4);
                       double discountAmount = (price * quantity) * (discount / 100);
                       double total = (price * quantity) - discountAmount;
                       int id = result.getInt(0);

                       LinearLayout linearLayoutItem = new LinearLayout(this);
                       linearLayoutItem.setPadding(10, 10, 10, 10);
                       linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                       linearLayoutItem.setBackgroundColor(Color.WHITE);
                       linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       tableRow.addView(linearLayoutItem);

                       TextView lblColumn2 = new TextView(this);
                       lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn2.setLayoutParams(layoutParamsItem);
//                       String v = cutWord(item);
                       lblColumn2.setText(item);
                       lblColumn2.setTextSize(15);
                       lblColumn2.setBackgroundColor(Color.WHITE);

                       lblColumn2.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) { 
                               Toast.makeText( ShoppingCart.this, item, Toast.LENGTH_SHORT).show();
                           }
                       });

                       linearLayoutItem.addView(lblColumn2);

                       TextView lblColumn3 = new TextView(this);
                       lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn3.setText(df.format(price));
                       lblColumn3.setPadding(10, 10, 10, 10);
                       lblColumn3.setBackgroundColor(Color.WHITE);
                       lblColumn3.setTextSize(13);
                       tableRow.addView(lblColumn3);

                       TextView lblColumn4 = new TextView(this);
                       lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn4.setText(dfInt.format(quantity));
                       lblColumn4.setPadding(10, 10, 10, 10);
                       lblColumn4.setBackgroundColor(Color.WHITE);
                       lblColumn4.setTextSize(13);
                       tableRow.addView(lblColumn4);

                       TextView lblColumn5 = new TextView(this);
                       lblColumn5.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn5.setText(df.format(discount) + "%");
                       lblColumn5.setPadding(10, 10, 10, 10);
                       lblColumn5.setBackgroundColor(Color.WHITE);
                       lblColumn5.setTextSize(13);
                       tableRow.addView(lblColumn5);

                       TextView lblColumn6 = new TextView(this);
                       lblColumn6.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn6.setText(df.format(total));
                       lblColumn6.setPadding(10, 10, 10, 10);
                       lblColumn6.setBackgroundColor(Color.WHITE);
                       lblColumn6.setTextSize(13);
                       tableRow.addView(lblColumn6);

                       final TextView lblColumn7 = new TextView(this);
                       lblColumn7.setGravity(View.TEXT_ALIGNMENT_CENTER);
                       lblColumn7.setText("Remove");
                       lblColumn7.setPadding(10, 10, 10, 10);
                       lblColumn7.setTextColor(Color.RED);
                       lblColumn7.setTextSize(13);
                       lblColumn7.setBackgroundColor(Color.WHITE);
                       lblColumn7.setTag(id);

                       lblColumn7.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                   return;
                               }
                               mLastClickTime = SystemClock.elapsedRealtime();
                               AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                               builder.setMessage("Are you sure want to remove " + item + "?")
                                       .setTitle("Confirmation")
                                       .setCancelable(false)
                                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               deleteData(lblColumn7.getTag().toString());
                                               computeTotal();
                                               loadData();
                                               toastMsg("Item Removed", 0);
                                           }
                                       })
                                       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               dialog.cancel();
                                           }
                                       });
                               AlertDialog alertDialog = builder.create();
                               alertDialog.show();
                           }
                       });
                       tableRow.addView(lblColumn7);
                       tableLayout.addView(tableRow);
                       View viewLine = new View(this);
                       LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                       viewLine.setLayoutParams(layoutParamsLine);
                       viewLine.setBackgroundColor(Color.GRAY);
                       tableLayout.addView(viewLine);
                   }
               }

            LinearLayout layoutPay = new LinearLayout(this);
            layoutPay.setBackgroundColor(Color.WHITE);
            layoutPay.setLayoutParams(layoutParamsPay);

            LinearLayout.LayoutParams layoutParamsTextInputLayoutDiscountType = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParamsTextInputLayoutDiscountType.setMargins(25,5,10,20);
            TextInputLayout textInputLayoutDiscountType = new TextInputLayout(ShoppingCart.this);
            textInputLayoutDiscountType.setLayoutParams(layoutParamsTextInputLayoutDiscountType);
            textInputLayoutDiscountType.setHint("Discount Type");
            textInputLayoutDiscountType.setHelperText("e.g Wholesale, PWD, etc...");
            textInputLayoutDiscountType.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
            textInputLayoutDiscountType.setBackgroundColor(Color.WHITE);
            textInputLayoutDiscountType.setBoxCornerRadii(10,10,10,10);
            textInputLayoutDiscountType.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

            List<String> discounts = new ArrayList<>();
            AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(textInputLayoutDiscountType.getContext());
            autoCompleteTextView.setTag("DiscountType");
            LinearLayout.LayoutParams layoutParamsAutoCompleteTextView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            autoCompleteTextView.setLayoutParams(layoutParamsAutoCompleteTextView);
            autoCompleteTextView.setBackgroundColor(Color.WHITE);
            autoCompleteTextView.setInputType(InputType.TYPE_NULL);
            textInputLayoutDiscountType.addView(autoCompleteTextView);
            layoutPay.addView(textInputLayoutDiscountType);



            SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
            String token = sharedPreferences0.getString("token", "");

            SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
            String IPAddress = sharedPreferences3.getString("IPAddress", "");

            OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
            OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
            OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
            OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
            client = OkHttpBuilder.build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(IPAddress + "/api/disc_type/get_all")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .method("GET", null)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    e.printStackTrace();
//                                Toast.makeText(getBaseContext(), "Error Connection \n" + e.getMessage() + "\n" + "The data is from Resources", Toast.LENGTH_LONG).show();
                                    Cursor cursor = myDb8.getAllData();
                                    discounts.add("Select Discount");
                                    while (cursor.moveToNext()){
                                        String module = cursor.getString(4);
                                        if(module.contains("Discount Type")){
                                            try{
                                                JSONObject jsonObject1 = new JSONObject(cursor.getString(3));
                                                gDiscountResponse = jsonObject1.toString();
                                                JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                                for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                    JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                    discounts.add(jsonObject.getString("description"));
                                                }
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ShoppingCart.this, android.R.layout.simple_spinner_item, discounts);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    autoCompleteTextView.setAdapter(adapter);
                                    autoCompleteTextView.setSelection(0);
                                }
                            });
                        }
                    });
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    ShoppingCart.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (this) {
                                        try {
                                            wait(10);
                                        } catch (InterruptedException ignored) {

                                        }
                                        handler.post(() -> {
                                            try {
                                                assert response.body() != null;
                                                String result = response.body().string();
                                                System.out.println(result);
                                                JSONObject jsonObject1 = new JSONObject(result);
                                                gDiscountResponse = jsonObject1.toString();
                                                if (response.isSuccessful()) {
                                                    if (jsonObject1.getBoolean("success")) {
                                                        discounts.add("Select Discount");
                                                        JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                                        for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                            JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                            discounts.add(jsonObject.getString("description"));
                                                            System.out.println(jsonObject.getString("description"));
                                                        }
                                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ShoppingCart.this, android.R.layout.simple_spinner_item, discounts);
                                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                        autoCompleteTextView.setAdapter(adapter);
                                                        autoCompleteTextView.setSelection(0);
                                                    }else {
                                                        String msg = jsonObject1.getString("message");
                                                        if (msg.equals("Token is invalid")) {
                                                            final AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                                                            builder.setCancelable(false);
                                                            builder.setMessage("Your session is expired. Please login again.");
                                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    pc.loggedOut(ShoppingCart.this);
                                                                    pc.removeToken(ShoppingCart.this);
                                                                    startActivity(uic.goTo(ShoppingCart.this, MainActivity.class));
                                                                    finish();
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                            builder.show();
                                                        } else {

                                                            Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                } else {
                                                    System.out.println(jsonObject1.getString("message"));
                                                }
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        });
                                    }
                                }
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                        }
                    });
                }
            });

            final MaterialButton btnPay = new MaterialButton(ShoppingCart.this);
            btnPay.setTag("Pay");
            btnPay.setLayoutParams(layoutParamsBtnPay);
            btnPay.setTextSize(15);
            btnPay.setText("PAY");
            btnPay.setCornerRadius(10);

            TextView lblReminder = new TextView(ShoppingCart.this);
            lblReminder.setLayoutParams(layoutParamsBtnPay);
            lblReminder.setTextColor(Color.parseColor("#fc1c1c"));
            lblReminder.setTextSize(15);
            lblReminder.setTypeface(null, Typeface.BOLD);
            lblReminder.setText("Reminder: Always confirm Customer's order before pay!");


            btnPay.setOnClickListener(new View.OnClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    layout.clearFocus();

                    String hashedID =   randomString();

                    if (myDb.countItems() <= 0) {
                        showMessage("Validation", "No Item found");
                    } else if (myDb.checkIfHaveDiscount() && autoCompleteTextView.getText().toString().trim().isEmpty()) {
                        showMessage("Validation", "Please select Discount Type");
                    }
                    else if(myDb.checkIfHaveDiscount() && autoCompleteTextView.getText().toString().trim().equals("Select Discount")){
                        showMessage("Validation", "Please select Discount Type");
                    }
                    else if (!myDb.checkIfHaveDiscount() && autoCompleteTextView.getText().toString().isEmpty()) {
                        confirmationDialog(hashedID);
                    } else if (!myDb.checkIfHaveDiscount() && autoCompleteTextView.getText().toString().equals("Select Discount")) {
                        confirmationDialog(hashedID);
//                    } else if (myDb.checkIfHaveDiscount() && !autoCompleteTextView.getText().toString().isEmpty()) {
//                        confirmationDialog();
                    } else if (myDb.checkIfHaveDiscount() && !autoCompleteTextView.getText().toString().equals("Select Discount")) {
                        confirmationDialog(hashedID);
                    } else {
                        showMessage("Validation", "You can't select discount without discounting the item");
                    }
                }
            });


            final TextView lblTotalItems= new TextView(this);
            lblTotalItems.setText("Total Items: 0");

            lblTotalItems.setLayoutParams(layoutParamsLblError);
            lblTotalItems.setTextColor(Color.RED);
            lblTotalItems.setTag("lblTotalItems");
            lblTotalItems.setTextSize(20);


            final TextView lblSubTotal = new TextView(this);
            lblSubTotal.setText("0.00");

            lblSubTotal.setLayoutParams(layoutParamsLblError);
            lblSubTotal.setTextColor(Color.rgb(52, 168, 83));
            lblSubTotal.setTag("lblSubTotal");
            lblSubTotal.setTextSize(20);

//            layoutPay.addView(cmbDiscountType);
            layoutPay.addView(lblTotalItems);
            layoutPay.addView(lblSubTotal);
            layoutPay.addView(lblReminder);
            layoutPay.addView(btnPay);
            layoutPay.setOrientation(LinearLayout.VERTICAL);
            layoutPay.setPadding(20, 20, 20, 20);
            layout.addView(layoutPay);
            computeTotal();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String cutWord(String value){
        String result;
        int limit = 15;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }

    private class myWarehouse extends AsyncTask<String, Void, String> {
        String gitle = "";
        LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
        TextView gLbl;
        boolean gIsRefresh = false;

        public myWarehouse(TextView lbl, boolean isRefresh) {
            gLbl = lbl;
            gIsRefresh= isRefresh;
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
//                SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
//                String currentBranch = Objects.requireNonNull(sharedPreferences.getString("branch", ""));
//                String currentPlant = Objects.requireNonNull(sharedPreferences.getString("plant", ""));
//                String sParams = isGlobal ? "" : !isFromWhse && hiddenTitle.equals("API Received Item") ? "?branch=" + currentBranch : isFromWhse && hiddenTitle.equals("API Transfer Item") ? "?branch=" + currentBranch : !hiddenTitle.equals("API Received Item") ? "?plant=" + currentPlant : "";

                Cursor cursor = myDb8.getAllData();
                String cURL = "", cMethod = "", cResponse = "";
                while (cursor.moveToNext()) {
                    String module = cursor.getString(4);
                    if (module.equals("Customer") && !gIsRefresh) {
                        System.out.println("first last");
                        cResponse = cursor.getString(3);
                        return cursor.getString(3);
                    } else if (module.equals("Customer") && gIsRefresh) {
                        cURL = cursor.getString(1);
                        cMethod = cursor.getString(2);
                        cResponse = cursor.getString(3);
                    }
                }
                if (gIsRefresh && !cURL.trim().isEmpty() && !cMethod.trim().isEmpty()) {
                    String sURL = "/api/customer/get_all?transtype=sales";
                    utility_class utilityc = new utility_class();

                    System.out.println("sURL " + sURL);

                    SharedPreferences sharedPreferences2 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    String bearerToken = Objects.requireNonNull(sharedPreferences2.getString("token", ""));

                    OkHttpClient client;

                    OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
                    OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
                    OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
                    OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
                    client = OkHttpBuilder.build();

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    okhttp3.Request request = null;
                    request = new okhttp3.Request.Builder()
                            .url(utilityc.getIPAddress(ShoppingCart.this) + sURL)
                            .method("GET", null)
                            .addHeader("Authorization", "Bearer " + bearerToken)
                            .build();
                    Response response;
                    response = client.newCall(request).execute();
                    return response.body().string();
                } else {
                    return cResponse;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Cursor cursor = myDb8.getAllData();
                while (cursor.moveToNext()) {
                    String module = cursor.getString(4);
                    if (module.trim().toLowerCase().contains(gitle.trim().toLowerCase())) {
                        ex.printStackTrace();
                        return cursor.getString(3);
                    }
                }
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            gCustomer = s;
            showWarehouses(gLbl, s);
            loadingDialog.dismissDialog();
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void confirmationDialog(String hashedID){
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(ShoppingCart.this);
        LinearLayout layout = new LinearLayout(ShoppingCart.this);
        layout.setPadding(40,40,40,40);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView lblARTitle = new TextView(ShoppingCart.this);
        lblARTitle.setTextSize(20);
        lblARTitle.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblARTitle);

        TextView lblCustomer = new TextView(ShoppingCart.this);
        lblCustomer.setText("N/A");
        lblCustomer.setTextSize(15);
        lblCustomer.setGravity(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout layoutBranch = new LinearLayout(getBaseContext());

        layoutBranch.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParamsBranch2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblCustomer.setLayoutParams(layoutParamsBranch2);

        LinearLayout.LayoutParams layoutParamsBranch3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsBranch3.setMargins(10,0,0,0);


        TextView btnSelectBranch = new TextView(getBaseContext());
        btnSelectBranch.setText("...");
        btnSelectBranch.setPadding(20,10,20,10);
        btnSelectBranch.setBackgroundColor(Color.BLACK);
        btnSelectBranch.setTextColor(Color.WHITE);
        btnSelectBranch.setTextSize(15);
        btnSelectBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
        btnSelectBranch.setLayoutParams(layoutParamsBranch3);

        btnSelectBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myWarehouse myWarehouse = new myWarehouse(lblCustomer, false);
                myWarehouse.execute("");
            }

        });
        layoutBranch.addView(lblCustomer);
        layoutBranch.addView(btnSelectBranch);
        double discountType = 0.00;

        final double getSubTotal = myDb.getSubTotal() - (myDb.getSubTotal() * (discountType / 100));
        TextView lblSubtotal = new TextView(ShoppingCart.this);
        lblSubtotal.setText("Amount Payable: " + df.format(getSubTotal));
        lblSubtotal.setTextSize(15);
        lblSubtotal.setGravity(View.TEXT_ALIGNMENT_CENTER);


        TextView lblTendered = new TextView(getBaseContext());
        lblTendered.setText("Tender Amt:");
        lblTendered.setTextSize(15);
        lblTendered.setGravity(View.TEXT_ALIGNMENT_CENTER);

        final EditText txttendered = new EditText(ShoppingCart.this);
        final TextView lblChange = new TextView(ShoppingCart.this);
        txttendered.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        txttendered.setText("0.0");
        final double[] change = {0.00};


        TextView lblDelFee = new TextView(getBaseContext());
        lblDelFee.setText("Other Fee:");
        lblDelFee.setTextSize(15);
        lblDelFee.setGravity(View.TEXT_ALIGNMENT_CENTER);

        EditText txtDelFee = new EditText(getBaseContext());
        txtDelFee.setText("0");
        txtDelFee.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        txtDelFee.setTextColor(Color.rgb(0,0,0));
        txtDelFee.setTextSize(15);
        txtDelFee.setGravity(View.TEXT_ALIGNMENT_CENTER);

        txttendered.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = txttendered.getText().toString();

                double delfee = 0.00;
                try{
                    delfee = Double.parseDouble(txtDelFee.getText().toString());
                }catch(Exception exception)
                {
                    delfee= 0.00;
                }

                if(value.matches("")){
                    change[0] = 0 - (getSubTotal + delfee);
                }else{
                    change[0] = Double.parseDouble(txttendered.getText().toString()) - (getSubTotal + delfee);
                }
                lblChange.setText("Change: " + df.format(change[0]));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        txtDelFee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = txtDelFee.getText().toString();
                double delfee = 0.00;
                try{
                    delfee = Double.parseDouble(txtDelFee.getText().toString());
                }catch(Exception exception)
                {
                    delfee= 0.00;
                }
                if(value.matches("")){
                    change[0] = 0 - (getSubTotal);
                }else{
                    change[0] = Double.parseDouble(txttendered.getText().toString()) - (getSubTotal + delfee);
                }
                lblChange.setText("Change: " + df.format(change[0]));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtDelFee.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean s) {
                if(!s){
                    String value = txtDelFee.getText().toString();
                    if(value.matches("")){
                        txtDelFee.setText("0");
                    }
                }
            }
        });

        txttendered.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    String value = txttendered.getText().toString();
                    if(value.matches("")){
                        txttendered.setText("0");
                    }
                }
            }
        });

        double v = 0 - getSubTotal + Double.parseDouble(txtDelFee.getText().toString());
        lblChange.setText("Change: " + df.format(v));
        lblChange.setTextSize(15);
        lblChange.setGravity(View.TEXT_ALIGNMENT_CENTER);

         Spinner cmbTenderType = new Spinner(this);



         List<String> tenderTypes = new ArrayList<>();

        SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = sharedPreferences0.getString("token", "");

        SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences3.getString("IPAddress", "");

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(IPAddress + "/api/sales/type/get_all")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .method("GET", null)
                .build();
        OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
        OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
        client = OkHttpBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                e.printStackTrace();
//                                Toast.makeText(getBaseContext(), "Error Connection \n" + e.getMessage() + "\n" + "The data is from Resources", Toast.LENGTH_LONG).show();
                                Cursor cursor = myDb8.getAllData();
                                while (cursor.moveToNext()){
                                    String module = cursor.getString(4);
                                    if(module.contains("Sales Type")){
                                        try{
                                            JSONObject jsonObject1 = new JSONObject(cursor.getString(3));
                                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                tenderTypes.add(jsonObject.getString("code"));
                                            }
                                        }catch (Exception ex){
                                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ShoppingCart.this, android.R.layout.simple_spinner_item, tenderTypes);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cmbTenderType.setAdapter(adapter);
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                ShoppingCart.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    try {
                                        wait(10);
                                    } catch (InterruptedException ignored) {

                                    }
                                    handler.post(() -> {
                                        try {
                                            assert response.body() != null;
                                            String result = response.body().string();
//                                                                                    System.out.println(result);
                                            JSONObject jsonObject1 = new JSONObject(result);
                                            if (response.isSuccessful()) {
                                                if (jsonObject1.getBoolean("success")) {
                                                    JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                                    for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                        JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                        tenderTypes.add(jsonObject.getString("code"));
                                                    }
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ShoppingCart.this, android.R.layout.simple_spinner_item, tenderTypes);
                                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    cmbTenderType.setAdapter(adapter);
                                                }else {
                                                    String msg = jsonObject1.getString("message");
                                                    if (msg.equals("Token is invalid")) {
                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                                                        builder.setCancelable(false);
                                                        builder.setMessage("Your session is expired. Please login again.");
                                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                pc.loggedOut(ShoppingCart.this);
                                                                pc.removeToken(ShoppingCart.this);
                                                                startActivity(uic.goTo(ShoppingCart.this, MainActivity.class));
                                                                finish();
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        builder.show();
                                                    } else {
                                                        Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            } else {
                                                System.out.println(jsonObject1.getString("message"));
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    });
                                }
                            }
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                });
            }
        });

        cmbTenderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(0 == cmbTenderType.getSelectedItem().toString().indexOf("CASH")){
                    txttendered.setText("");
                    txttendered.setEnabled(true);
                    lblCustomer.setText("");
                    txttendered.requestFocus();
                    btnSelectBranch.setEnabled(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txttendered, InputMethodManager.SHOW_IMPLICIT);
                }else if(0 == cmbTenderType.getSelectedItem().toString().indexOf("Agent AR Sales")){
                    txttendered.setText("");
                    txttendered.setEnabled(false);
                    lblCustomer.setText("");
                    btnSelectBranch.setEnabled(false);
                }
                else{
                    txttendered.setText("");
                    txttendered.setEnabled(false);
                    lblCustomer.setText("");
                    lblCustomer.requestFocus();
                    btnSelectBranch.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextView lblRemarks = new TextView(getBaseContext());
        lblRemarks.setText("Remarks:");
        lblRemarks.setTextSize(15);
        lblRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);


        EditText txtRemarks = new EditText(getBaseContext());
        txtRemarks.setTextColor(Color.rgb(0,0,0));
        txtRemarks.setTextSize(15);
        txtRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);


        TextView lblTenderType = new TextView(getBaseContext());
        lblTenderType.setText("Tender Type:");
        lblTenderType.setTextSize(15);
        lblTenderType.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblTenderType);
        layout.addView(cmbTenderType);
//        layout.addView(lblCustomer);

        TextView lblCustomerr = new TextView(getBaseContext());
        lblCustomerr.setText("Customer:");
        lblCustomerr.setTextSize(15);
        lblCustomerr.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblCustomerr);

        layout.addView(layoutBranch);
        layout.addView(lblSubtotal);


        LinearLayout linearLayoutDelFee = new LinearLayout(getBaseContext());
        LinearLayout.LayoutParams layoutParamsDelFee = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutDelFee.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutDelFee.setLayoutParams(layoutParamsDelFee);

        LinearLayout.LayoutParams layoutParamsTxtDelFee = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutDelFee.addView(lblDelFee);
        txtDelFee.setLayoutParams(layoutParamsTxtDelFee);
        linearLayoutDelFee.addView(txtDelFee);

        layout.addView(linearLayoutDelFee);

        LinearLayout linearLayoutTendered = new LinearLayout(getBaseContext());
        LinearLayout.LayoutParams layoutParamsTendered = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutTendered.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutTendered.setLayoutParams(layoutParamsTendered);

        txttendered.setLayoutParams(layoutParamsTxtDelFee);

        linearLayoutTendered.addView(lblTendered);
        linearLayoutTendered.addView(txttendered);

        layout.addView(linearLayoutTendered);
        layout.addView(lblChange);

        LinearLayout linearLayoutRemarks = new LinearLayout(getBaseContext());
        LinearLayout.LayoutParams layoutParamsRemarks= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutRemarks.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutRemarks.setLayoutParams(layoutParamsRemarks);

        txtRemarks.setLayoutParams(layoutParamsTxtDelFee);

        linearLayoutRemarks.addView(lblRemarks);
        linearLayoutRemarks.addView(txtRemarks);
        layout.addView(linearLayoutRemarks);

        myDialog.setView(layout);
        myDialog.setCancelable(false);

        myDialog.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Handler handler = new Handler();
                LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
                loadingDialog.startLoadingDialog();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(10);
                            } catch (InterruptedException ex) {

                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    final String customerName = lblCustomer.getText().toString().trim();
                                    String tendertype = cmbTenderType.getAdapter() == null || cmbTenderType.getSelectedItem().toString().trim().isEmpty() ? "" : cmbTenderType.getSelectedItem().toString() == null ? "" : cmbTenderType.getSelectedItem().toString();
//                                   String tendertype = cmbTenderType.getSelectedItem().toString();
                                    if (cmbTenderType.getAdapter() == null) {
//                                        runOnUiThread(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//
//                                            }
//                                        });
                                        toastMsg("Please select Service Type", 0);
                                    } else if (customerName.isEmpty() && cmbTenderType.getSelectedItem().toString() == "AR Sales") {
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//
//                                            }
//                                        });
                                        toastMsg("Name field is required", 0);
                                    }
                                    else {
                                        AlertDialog.Builder dialogConfirmation = new AlertDialog.Builder(ShoppingCart.this);
                                        dialogConfirmation.setTitle("Confirm Order?");
                                        final String finalTendertype = tendertype;
                                        dialogConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                                    return;
                                                }
                                                mLastClickTime = SystemClock.elapsedRealtime();
                                                String disctype;
                                                String tendered = txttendered.getText().toString();
                                                double tenderamt;
//                                          DISCOUNT TYPEa
//                                                View root = getWindow().getDecorView().getRootView();
//                                                Spinner currentDiscountType = root.findViewWithTag("cmbDiscountType");
//                                                if (currentDiscountType.getSelectedItemPosition() == 0) {
//                                                    disctype = "N/A";
//                                                } else {
//                                                    disctype = currentDiscountType.getSelectedItem().toString();
//                                                }
//                                          TENDER AMOUNT
                                                if (tendered.isEmpty()) {
                                                    tenderamt = 0.00;
                                                } else if (Double.parseDouble(tendered) < 0) {
                                                    tenderamt = 0.00;
                                                } else {
                                                    tenderamt = Double.parseDouble(tendered);
                                                }
                                                double delfee = 0.00;
                                                try{
                                                    delfee = Double.parseDouble(txtDelFee.getText().toString());
                                                }catch (Exception ex){
                                                    delfee = 0.00;
                                                }
//                                          CUSTOMER
                                                try {
                                                    APISaveTransaction(finalTendertype, tenderamt, lblCustomer.getText().toString(),txtRemarks.getText().toString(),hashedID,delfee);
//                                                    insertTransaction(finalTendertype, disctype, tenderamt, customerName);
                                                    discountID = "";
                                                    discountName = "";
                                                } catch (Exception e) {
                                                    showMessage("Error", e.getMessage());
                                                }
                                            }
                                        });

                                        dialogConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                        dialogConfirmation.show();
                                    }
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                loadingDialog.dismissDialog();
                            }
                        });
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myDialog.show();
    }

    public void showWarehouses(TextView lblSelectedBranch,String vResult){
        AlertDialog _dialog= new AlertDialog.Builder(ShoppingCart.this).create();
        AlertDialog.Builder dialogSelectWarehouse = new AlertDialog.Builder(ShoppingCart.this);
        dialogSelectWarehouse.setTitle("Select Customer");
        dialogSelectWarehouse.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(20, 20, 20, 20);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout textInputLayout = new TextInputLayout(ShoppingCart.this);
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeColor(Color.parseColor("#1687a7"));
        textInputLayout.setHint("Search Warehouse");

        AutoCompleteTextView txtSearchBranch = new AutoCompleteTextView(getBaseContext());
        txtSearchBranch.setTextSize(15);
        textInputLayout.addView(txtSearchBranch);
        layout.addView(textInputLayout);

        LinearLayout.LayoutParams layoutParamsLa = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout la = new LinearLayout(ShoppingCart.this);
        la.setWeightSum(2f);
        la.setOrientation(LinearLayout.HORIZONTAL);
        la.setLayoutParams(layoutParamsLa);

        final List<String>[] warehouses = new List[]{returnCustomers(vResult)};
        final ArrayList<String>[] myReference = new ArrayList[]{getReference(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final ArrayList<String>[] myID = new ArrayList[]{getID(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final List<String>[] listItems = new List[]{getListItems(warehouses[0])};

        MaterialButton btnSearchBranch = new MaterialButton(ShoppingCart.this);
        btnSearchBranch.setCornerRadius(20);
        LinearLayout.LayoutParams layoutParamsBtn = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnSearchBranch.setLayoutParams(layoutParamsBtn);
        btnSearchBranch.setBackgroundResource(R.color.colorPrimary);
//        btnSearchBranch.setPadding(20,20,20,20);
        btnSearchBranch.setTextColor(Color.WHITE);
        btnSearchBranch.setTextSize(13);
        btnSearchBranch.setText("Search");
        ListView listView = new ListView(getBaseContext());
        btnSearchBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myReference[0] = getReference(warehouses[0], txtSearchBranch.getText().toString().trim());
                myID[0] = getID(warehouses[0], txtSearchBranch.getText().toString().trim());
                listItems[0] = getListItems(warehouses[0]);

                ShoppingCart.MyAdapter adapter = new ShoppingCart.MyAdapter(ShoppingCart.this, myReference[0], myID[0]);

                listView.setAdapter(adapter);
            }
        });

        AppCompatButton btnRefresh = new AppCompatButton(ShoppingCart.this);
//        btnRefresh.setCornerRadius(20);
        LinearLayout.LayoutParams layoutParamsRefresh = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        layoutParamsRefresh.setMargins(10, 0, 0, 0);
        btnRefresh.setLayoutParams(layoutParamsRefresh);
        btnRefresh.setBackgroundColor(Color.rgb(157, 203, 242));
//        btnSearchBranch.setPadding(20,20,20,20);
        btnRefresh.setTextColor(Color.WHITE);
        btnRefresh.setTextSize(13);
        btnRefresh.setText("Refresh");

        la.addView(btnSearchBranch);
        la.addView(btnRefresh);
        layout.addView(la);


        LinearLayout.LayoutParams layoutParamsWarehouses = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,300);
        layoutParamsWarehouses.setMargins(10,10,10,10);
        listView.setLayoutParams(layoutParamsWarehouses);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtSearchBranch.setAdapter(fillItems(listItems[0]));
            }
        });

        ShoppingCart.MyAdapter adapter = new ShoppingCart.MyAdapter(ShoppingCart.this, myReference[0], myID[0]);
        dialogSelectWarehouse.setView(layout);

        dialogSelectWarehouse.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final AlertDialog[] as = {_dialog};
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                as[0] = dialogSelectWarehouse.show();
                listView.setAdapter(adapter);
            }
        });

        AlertDialog final_dialog = as[0];
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = view.findViewById(R.id.txtIDs);
                        TextView textView1 = view.findViewById(R.id.txtReference);
                        textView.setVisibility(View.GONE);
                        lblSelectedBranch.setText(textView1.getText().toString());
                        lblSelectedBranch.setTag(textView1.getText().toString());
                        if(final_dialog !=null){
                            final_dialog.dismiss();
                        }
                    }
                });
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String fullName = Objects.requireNonNull(sharedPreferences.getString("fullname", ""));

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullName.equals("Offline Mode")) {
                    Toast.makeText(getBaseContext(), "You can't refresh warehouse because you are in offline mode!", Toast.LENGTH_SHORT).show();
                } else {
                    final_dialog.dismiss();
                    myWarehouse myWarehouse = new myWarehouse(lblSelectedBranch, true);
                    myWarehouse.execute("");
                }
            }
        });

        layout.addView(listView);
    }

    public List<String> getListItems(List<String> warehouses){
        List<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                result.add(temp);
//                if (!txtSearchBranch.getText().toString().trim().isEmpty()) {
//                    if (txtSearchBranch.getText().toString().trim().contains(temp)) {
//                        myReference.add(temp);
//                        myID.add("0");
//                    }
//                }else{
//                    myReference.add(temp);
//                    myID.add("0");
//                }
            }
        }
        return result;
    }

    public ArrayList<String> getReference(List<String> warehouses,String value){
        ArrayList<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                if (!value.isEmpty()) {
                    if (value.trim().toLowerCase().equals(temp.toLowerCase())) {
                        result.add(temp);
                    }
                }else{
                    result.add(temp);
//                    myID.add("0");
                }
            }
        }
        return result;
    }

    public ArrayList<String> getID(List<String> warehouses,String value){
        ArrayList<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                if (!value.isEmpty()) {
                    if (value.trim().contains(temp)) {
                        result.add("0");
//                        myID.add("0");
                    }
                }else{
                    result.add("0");
//                    myID.add("0");
                }
            }
        }
        return result;
    }




    public ArrayAdapter<String> fillItems(List<String> items){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context rContext;
        ArrayList<String> myReference;
        ArrayList<String> myIds;

        MyAdapter(Context c, ArrayList<String> reference, ArrayList<String> id) {
            super(c, R.layout.custom_list_view_sales_logs, R.id.txtReference, reference);
            this.rContext = c;
            this.myReference = reference;
            this.myIds = id;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.custom_list_view_sales_logs, parent, false);
            TextView textView1 = row.findViewById(R.id.txtReference);
            TextView textView2 = row.findViewById(R.id.txtIDs);

            textView1.setText(myReference.get(position));
            textView2.setText(myIds.get(position));
            textView2.setVisibility(View.GONE);

            return row;
        }
    }

    void doGetRequest(String url) throws IOException, InterruptedException {
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
        OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
        client = OkHttpBuilder.build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        countDownLatch.countDown();
                        Cursor cursor = myDb8.getAllData();
                        while (cursor.moveToNext()){
                            String module = cursor.getString(3);
                            if(module.contains("Customer")){
                                gCustomer = cursor.getString(4);
                            }else{
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "This data is from offline mode!", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if(response.body().string() ==null){
                            String res = response.body().string();
                            gCustomer = res;
                        }else {
                            Cursor cursor = myDb8.getAllData();
                            while (cursor.moveToNext()){
                                String module = cursor.getString(3);
                                if(module.contains("Customer")){
                                    gCustomer = cursor.getString(4);
                                }else{
                                }
                            }
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getBaseContext(), "This data is from offline mode!", Toast.LENGTH_SHORT).show();
//                                }
//                            });
                        }
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();
    }

    void doPostRequest(String sURL,String body,String fromModule,String hiddenFromModule) throws IOException, InterruptedException {
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody bodyy = RequestBody.create(JSON, body);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url( sURL)
                .method("POST", bodyy)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        OkHttpClient.Builder OkHttpBuilder = new OkHttpClient.Builder();
        OkHttpBuilder.connectTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.readTimeout(30, TimeUnit.SECONDS);
        OkHttpBuilder.writeTimeout(30, TimeUnit.SECONDS);
        client = OkHttpBuilder.build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        countDownLatch.countDown();
                        ShoppingCart.this.runOnUiThread(new Runnable() {
                            public void run() {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String currentDate = sdf.format(new Date());
                                JSONObject joData = new JSONObject();
                                try {
                                    joData = new JSONObject(body);
                                    JSONObject joHeader = joData.getJSONObject("header");
                                    joHeader.put("is_offline", true);
                                    joData.remove("header");
                                    joData.put("header", joHeader);
                                } catch (JSONException jsonException) {
                                    jsonException.printStackTrace();
                                }
                                System.out.println(joData);
                                boolean isInserted = false;
                                isInserted = myDb7.insertData(sURL, "POST", body, fromModule, hiddenFromModule, currentDate);
                                if (isInserted) {
                                    myDb.truncateTable();
                                    loadData();
                                    Toast.makeText(getBaseContext(), "The data is inserted to local database", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getBaseContext(), "Your data is failed to insert in local database", Toast.LENGTH_SHORT).show();
                                    View root1 = getWindow().getDecorView().getRootView();
                                    MaterialButton btnPay = root1.findViewWithTag("Pay");
                                    btnPay.setEnabled(false);
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String finalResult = response.body().string();
                        if(finalResult != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        JSONObject jj = new JSONObject(finalResult);
                                        boolean isSuccess = jj.getBoolean("success");
                                        if (isSuccess) {
                                            myDb.truncateTable();
                                            loadData();
                                            JSONObject jsonObjectData = jj.getJSONObject("data");
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                                            builder.setCancelable(false);
                                            builder.setTitle("Transaction Completed!");
                                            builder.setMessage("Order #: " + jsonObjectData.getString("reference"));
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            builder.show();

                                        } else {
                                            String msg = jj.getString("message");
                                            if (msg.equals("Token is invalid")) {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                                                builder.setCancelable(false);
                                                builder.setMessage("Your session is expired. Please login again.");
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        pc.loggedOut(ShoppingCart.this);
                                                        pc.removeToken(ShoppingCart.this);
                                                        startActivity(uic.goTo(ShoppingCart.this, MainActivity.class));
                                                        finish();
                                                        dialog.dismiss();
                                                    }
                                                });
                                                builder.show();
                                            } else {
                                                View root1 = getWindow().getDecorView().getRootView();
                                                MaterialButton btnPay = root1.findViewWithTag("Pay");
                                                btnPay.setEnabled(true);

                                                final AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
                                                builder.setTitle("Validation");
                                                builder.setCancelable(false);
                                                builder.setMessage(msg);
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                builder.show();

                                            }
                                        }
                                    }catch (Exception ex){
                                        Toast.makeText(getBaseContext(), "Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();
    }

//
//    public void hmReturnCustomers(){
//        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
//        String IPAddress = sharedPreferences2.getString("IPAddress", "");
//        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
//        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
//        String URL = IPAddress + "/api/customer/get_all?transtype=sales";
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(URL)
//                .method("GET", null)
//                .addHeader("Authorization", "Bearer " + token)
//                .addHeader("Content-Type", "application/json")
//                .build();
//        Thread thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try  {
//                    client = new OkHttpClient();
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    e.printStackTrace();
////                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//
//
//                        }
//
//                        @Override
//                        public void onResponse(Call call, okhttp3.Response response) {
//                            try {
////                                System.out.println(response.body().string());
//                                String sResult = response.body().string();
//                                gCustomer = sResult;
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(getBaseContext(), "loadCustomers()\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        thread.start();
//    }

    public List<String> returnCustomers(String vResult) {
        List<String> result = new ArrayList<>();
        System.out.println(gCustomer);
        try {
            if (vResult != null) {
                if (vResult.substring(0, 1).equals("{")) {
                    JSONObject jsonObjectResponse = new JSONObject(vResult);
                    JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
                        String branch = jsonObject.getString("code");
                        result.add(branch);
                    }
                }
            }
        } catch (Exception ex) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ex.printStackTrace();
                    Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        return result;
    }


    public ArrayAdapter<String> fillName(List<String> names){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, names);
    }

    @SuppressLint({"WrongConstant", "ShowToast"})
    public void deleteData(String id){
        Integer deletedItem = myDb.deleteData(id);
        if(deletedItem < 0){
            Toast.makeText(this,"Item not remove", 0).show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void computeTotal(){
        View root = getWindow().getDecorView().getRootView();
        TextView lblsubtotal = root.findViewWithTag("lblSubTotal");
        TextView lblTotalQuantity =root.findViewWithTag("lblTotalItems");
//        Spinner cmbDiscount =root.findViewWithTag("cmbDiscountType");
        double discountType = 0.00;
//        if(cmbDiscount.getSelectedItemPosition() != 0){
//            String spinnerText = (String) cmbDiscount.getSelectedItem();
////            discountType = sc.getDiscountPercent(ShoppingCart.this, spinnerText);
//        }

        double getSubTotal = myDb.getSubTotal() - (myDb.getSubTotal() * (discountType / 100));
        if(getSubTotal > 0){
            lblsubtotal.setText(Html.fromHtml("<font color='#606665'>" + "Amount Payable: " + "</font>₱"+  df.format(getSubTotal)));
        }else{
            lblsubtotal.setText(Html.fromHtml("<font color='#606665'>" + "Amount Payable: " + "</font>") + "₱0.00");
        }
        lblTotalQuantity.setText(Html.fromHtml("<font color='#606665'>" + "Total Item(s): " + "</font>"+ dfInt.format(myDb.geTotalItems())));
    }

    public void closeKeyboard(View v){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            v.clearFocus();
        }
    }

    public void toastMsg(String value, Integer duration){
        Toast.makeText(this, value, duration).show();
    }

    public void showMessage(String title, String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public void insertTransaction(final String tendertype, final String disctype, final Double tenderamt, final String customer) {
//        try {
//            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
//            int userid = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("userid", "")));
//            Integer ordernum = sc.getOrderNumber(ShoppingCart.this);
//            Double less = sc.getDiscountPercent(ShoppingCart.this, disctype);
//            double change;
//            con = cc.connectionClass(ShoppingCart.this);
//            if (con == null) {
//                Toast.makeText(ShoppingCart.this, "Check Your Internet Access", Toast.LENGTH_SHORT).show();
//            } else {
//                double getSubTotal = myDb.getSubTotal() - (myDb.getSubTotal() * (less / 100));
//                if (tenderamt > getSubTotal) {
//                    change = tenderamt - getSubTotal;
//                } else {
//                    change = 0.00;
//                }
//                double discamt = myDb.getSubTotal() * (less / 100);
//                double subtotalBefore = myDb.getSubTotalBefore();
//                Cursor result2 = myDb.getAllData();
//                if(result2.moveToNext()){
//                    inventory_type = result2.getString(7);
//                }
//
//                String query = "INSERT INTO tbltransaction2 (ornum, ordernum, transdate, cashier," +
//                        " tendertype, servicetype, delcharge, subtotal, disctype, less, vatsales, vat," +
//                        " amtdue, tenderamt, change, refund, comment, remarks, customer, tinnum, tablenum," +
//                        " pax, createdby, datecreated, datemodified, status, status2, area, gctotal, typez," +
//                        " discamt,inventory_type) VALUES ('000'," + ordernum + ",(SELECT FORMAT (GETDATE(), 'MM/dd/yyyy')),(SELECT username FROM tblusers WHERE" +
//                        " systemid=" + userid + "),'" + tendertype + "','Take Out'," +
//                        "0," + subtotalBefore + ",'" + disctype + "'," + less + ",0,0," + getSubTotal + "," + tenderamt + "," + change + "," +
//                        "0,'','','" + customer + "',0,0,0,(SELECT username FROM tblusers WHERE systemid=" + userid + ")," +
//                        "(SELECT GETDATE()),(SELECT GETDATE()),1,'Unpaid','Sales',0,(SELECT postype FROM tblusers WHERE systemid=" +
//                        "" + userid + ")," + discamt + ",'" + inventory_type + "');";
//                Statement stmt = con.createStatement();
//                stmt.executeUpdate(query);
//
//                if (!disctype.equals("N/A")) {
//                    Statement stmtSenior = con.createStatement();
//                    String querySenior = "INSERT INTO tblsenior (transnum,idno,name,disctype,datedisc,status)" +
//                            "VALUES ('" + ordernum + "','" + discountID + "','" + discountName + "','" + disctype + "'," +
//                            "(SELECT GETDATE()),3)";
//                    stmtSenior.executeUpdate(querySenior);
//                }
//                Cursor result = myDb.getAllData();
//                while (result.moveToNext()) {
//                    String itemname = result.getString(1);
//                    double quantity = Double.parseDouble(result.getString(2));
//                    double totalprice = Double.parseDouble(result.getString(5));
//                    double discount = Double.parseDouble(result.getString(4));
//                    double discountPercent = discount / 100;
//                    int free = Integer.parseInt(result.getString(6));
//                    String query2 = "Insert into tblorder2 (ordernum, category, itemname, qty, price, totalprice, dscnt, free," +
//                            "request, status, discprice, disctrans,area,gc,less,deliver,datecreated,pricebefore,discamt)" +
//                            "VALUES(" + ordernum + ",(SELECT category FROM tblitems WHERE itemname='" + itemname + "'),'" + itemname + "'" +
//                            "," + quantity + ",(SELECT price FROM tblitems WHERE itemname='" + itemname + "')," + totalprice + "," + discount + "," + free + "" +
//                            ",'',1,(SELECT SUM(price - ((" + discount + "/ 100) *price)) FROM tblitems WHERE itemname='" + itemname + "'),0,'Sales',0,0,0,(SELECT GETDATE())," +
//                            "(SELECT SUM(price * " + quantity + ") FROM tblitems WHERE itemname='" + itemname + "')," +
//                            "(SELECT " + discountPercent + " * (price * " + quantity + ") FROM tblitems WHERE itemname='" + itemname + "'));";
//                    Statement stmt2 = con.createStatement();
//                    stmt2.executeUpdate(query2);
//                }
//                con.close();
//                myDb.truncateTable();
//                showMessage("Transaction Completed", "Order #: " + ordernum + "\n" + "Change: " + change);
//                loadData();
//
//            }
//        } catch (SQLException ex) {
//            showMessage("Error", ex.getMessage());
//        }
//    }

    public String randomSpecialChart(){
        int n = 1;
        StringBuilder sb = new StringBuilder(n);
        String AlphaNumericString = "!@#$%^&*())_+-=,./<>?";
        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    public String randomString(){
        int n = 20;
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnñopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

//    private class apiSaveSales extends AsyncTask<String, Void, String> {
//        String gTransType = "", gCustomerName = "", gRemarks = "",gHashedID="";
//        AlertDialog gAlertDialog = null,gAlertDialog2 = null;
//        double gTenderAmount =0.00,gDelFee=0.00;
//        LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
//
//        public apiSaveSales(String transtype, Double tenderAmount, String customerName,String remarks,double delFee, AlertDialog dialog,String hashedID,AlertDialog dialog2) {
//            gTransType = transtype;
//            gCustomerName = customerName;
//            gRemarks = remarks;
//            gTenderAmount = tenderAmount;
//            gDelFee = delFee;
//            gAlertDialog = dialog;
//            gAlertDialog2 = dialog2;
//            gHashedID= hashedID;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            loadingDialog.startLoadingDialog();
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            Cursor cursor = myDb.getAllData();
//            if (cursor != null) {
//                if (cursor.moveToNext()) {
//
//                    
//
//                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());
//
//                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
//                    String IPAddress = sharedPreferences2.getString("IPAddress", "");
//                    System.out.println("/api/inv/recv/new");
//                    System.out.println(jsonObject);
//                    okhttp3.Request request = new okhttp3.Request.Builder()
//                            .url(IPAddress + "/api/inv/recv/new")
//                            .method("POST", body)
//                            .addHeader("Authorization", "Bearer " + token)
//                            .addHeader("Content-Type", "application/json")
//                            .build();
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
//                                    builder.setCancelable(false);
//                                    builder.setTitle("Validation");
//                                    builder.setMessage("Error\n" + e.getMessage());
//                                    builder.setPositiveButton("OK", (dialog, which) -> {
//                                        gAlertDialog.dismiss();
//                                        gAlertDialog2.dismiss();
//                                        finish();
//                                        dialog.dismiss();
//                                    });
//                                    builder.show();
//                                }
//                            });
//                        }
//
//                        @SuppressLint("SetTextI18n")
//                        @Override
//                        public void onResponse(Call call, okhttp3.Response response) {
//                            try {
//                                formatResponse(response.body().string(), gAlertDialog,gAlertDialog2);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String s) {
//            loadingDialog.dismissDialog();
//        }
//    }

    public void APISaveTransaction(String transtype, Double tenderAmount, String customerName,String remarks,String hashedID,double delFee){
        View root1 = getWindow().getDecorView().getRootView();
        MaterialButton btnPay = root1.findViewWithTag("Pay");
        btnPay.setEnabled(false);
        LoadingDialog loadingDialog = new LoadingDialog(ShoppingCart.this);
        loadingDialog.startLoadingDialog();
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
            String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
            JSONObject jsonObject = new JSONObject();

            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            JSONObject jsonObjectHeader = new JSONObject();

            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
            String userName = Objects.requireNonNull(sharedPreferences2.getString("username", ""));

            String finalHashedID =currentDateandTime.getBytes() + randomSpecialChart() + userName.getBytes() + randomSpecialChart() + hashedID;
            jsonObjectHeader.put("transdate", currentDateandTime);
            jsonObjectHeader.put("hashed_id", finalHashedID);
            if (!transtype.equals("CASH")) {
                jsonObjectHeader.put("cust_code", customerName);
                jsonObjectHeader.put("cust_name", customerName);
            }
            View root = getWindow().getDecorView().getRootView();
            AutoCompleteTextView cmbDiscType = root.findViewWithTag("DiscountType");
            JSONObject jsonObject1 = new JSONObject(gDiscountResponse);
            JSONArray jsonArray = jsonObject1.getJSONArray("data");
            String selectedDiscountCode = "", sDiscType = cmbDiscType.getText().toString().trim();
            for (int ii = 0; ii < jsonArray.length(); ii++) {
                JSONObject jsonObjectData = jsonArray.getJSONObject(ii);
                if (sDiscType.equals(jsonObjectData.getString("description"))) {
                    selectedDiscountCode = jsonObjectData.getString("code");
                }
            }

            jsonObjectHeader.put("remarks", remarks.trim().isEmpty() ? JSONObject.NULL : remarks);
            jsonObjectHeader.put("transtype", transtype);
            jsonObjectHeader.put("reference2", null);
            jsonObjectHeader.put("delfee", delFee);
            jsonObjectHeader.put("discprcnt", 0);
            jsonObjectHeader.put("gc_amount", null);
            jsonObjectHeader.put("tenderamt", tenderAmount);
            jsonObjectHeader.put("sap_number", null);
            jsonObjectHeader.put("disctype", selectedDiscountCode.trim().isEmpty() ? JSONObject.NULL : selectedDiscountCode);
            jsonObject.put("header", jsonObjectHeader);
            Cursor cursor = myDb.getAllData();
            if (cursor != null) {
                JSONArray arrayRows = new JSONArray();
                while (cursor.moveToNext()) {
                    JSONObject jsonObjectRow = new JSONObject();
                    jsonObjectRow.put("item_code", cursor.getString(1));
                    jsonObjectRow.put("quantity", cursor.getDouble(2));
                    jsonObjectRow.put("uom", cursor.getString(8));
                    jsonObjectRow.put("unit_price", cursor.getDouble(3));
                    jsonObjectRow.put("discprcnt", cursor.getDouble(4));
                    boolean free = (cursor.getInt(6) > 0);
                    jsonObjectRow.put("free", free);
                    arrayRows.put(jsonObjectRow);
                }
                jsonObject.put("rows", arrayRows);
            }
            System.out.println("this is a body: " + jsonObject);
            SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
            String IPaddress = sharedPreferences3.getString("IPAddress", "");

            String sURL = IPaddress + "/api/sales/new";
            String method = "POST";
            String bodyy = jsonObject.toString();
            String fromModule = title;
            String hiddenFromModule = hiddenTitle;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        doPostRequest(sURL, bodyy, fromModule, hiddenFromModule);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            loadingDialog.dismissDialog();
        } catch (Exception ex){
            btnPay.setEnabled(true);
            ex.printStackTrace();
            Toast.makeText(ShoppingCart.this, "APISaveTransaction() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
        }
    }

}