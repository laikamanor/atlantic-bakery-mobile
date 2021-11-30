package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.atlanticbakery.Adapter.CustomExpandableListAdapter;
import com.example.atlanticbakery.Helper.FragmentNavigationManager_API_SalesLogsItems;
import com.example.atlanticbakery.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API_SalesLogsItems extends AppCompatActivity {
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();
    DatabaseHelper myDb;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    DecimalFormat df = new DecimalFormat("#,###.00");

    private OkHttpClient client;
    Menu menu;
    TableLayout tableLayout;
    Button btnBack;

    String title, hidden_title;
    TextView txtReference,txtHeader;
    ProgressBar progressBar;
    Long mLastClickTime;

    CountDownTimer countDownTimer = null;
    private BadgeDrawerArrowDrawable badgeDrawable;
    notification_class notifc = new notification_class(this);
    int sapCount = 0, transferCount = 0, offlineCount = 0, totalCount = 0;
    DatabaseHelper7 myDb7 = new DatabaseHelper7(this);
    int isAgent = 0;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i_sales_logs_items);

        tableLayout = findViewById(R.id.table_main);
        btnBack = findViewById(R.id.btnBack);
        txtReference = findViewById(R.id.txtReference);
        txtHeader = findViewById(R.id.txtHeader);
        progressBar = findViewById(R.id.progressBar4);
        progressBar.setVisibility(View.GONE);

        myDb = new DatabaseHelper(this);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_SalesLogsItems.getmInstance(this);

        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");


        genData();
        addDrawersItem();
        setupDrawer();

        if(savedInstanceState == null){
            selectFirstItemDefault();
        }
        txtReference = findViewById(R.id.txtReference);
        String reference = getIntent().getStringExtra("reference");
        txtReference.setText(reference);

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


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String typeTrans = getIntent().getStringExtra("type");
        if(typeTrans.equals("Sales Transactions") || typeTrans.equals("Received Transactions") || typeTrans.equals("Transfer Transactions")){
            txtHeader.setVisibility(View.VISIBLE);
        }else{
            txtHeader.setVisibility(View.GONE);
        }

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
                    intent.putExtra("title", "System Receive Item");
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
                else if(selectedItem.equals("Change Password")){
                    changePassword();
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
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SalesLogsItems.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_SalesLogsItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_SalesLogsItems.myChangePassword myChangePassword = new API_SalesLogsItems.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_SalesLogsItems.this);
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

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(30, TimeUnit.SECONDS);
                builder.readTimeout(30, TimeUnit.SECONDS);
                builder.writeTimeout(30, TimeUnit.SECONDS);
                client = builder.build();

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

                        AlertDialog.Builder builder = new AlertDialog.Builder(API_SalesLogsItems.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_SalesLogsItems.this);
                                        pc.removeToken(API_SalesLogsItems.this);
                                        startActivity(uic.goTo(API_SalesLogsItems.this, MainActivity.class));
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


    public void loadData() {
        tableLayout.removeAllViews();
        TableRow tableColumn = new TableRow(API_SalesLogsItems.this);
        String[] columns;
        String typeTrans = getIntent().getStringExtra("type");
        if(typeTrans.equals("Received Transactions")) {
            columns = new String[]{"Item", "Qty.", "Actual Qty.", "Variance", "Uom"};
        }else if(typeTrans.equals("Transfer Transactions")){
            columns = new String[]{"Item", "Qty.", "Uom"};
        }else{
            columns = new String[]{"Item", "Qty.", "Price", "Gross", "Disc. %", "Disc. Amt.", "Total"};
        }
        for (String s : columns) {
            TextView lblColumn1 = new TextView(API_SalesLogsItems.this);
            lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
            lblColumn1.setText(s);
            lblColumn1.setPadding(10, 0, 10, 0);
            lblColumn1.setTextSize(20);
            lblColumn1.setTextColor(Color.BLACK);
            tableColumn.addView(lblColumn1);
        }
        tableLayout.addView(tableColumn);

        MyData myData = new MyData();
        myData.execute();

    }

    private class MyData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(30, TimeUnit.SECONDS);
                builder.readTimeout(30, TimeUnit.SECONDS);
                builder.writeTimeout(30, TimeUnit.SECONDS);
                client = builder.build();

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences0.getString("token", "");

                String URL = getIntent().getStringExtra("URL");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress +  URL)
                        .method("GET", null)
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
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if(s != null) {
                    progressBar.setVisibility(View.GONE);
                    String answer = s;
                    formatResponse(answer);
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    public void formatResponse(String temp){
        if(!temp.isEmpty() && temp.substring(0,1).equals("{")){
            try{
                String typeTrans = getIntent().getStringExtra("type");
                JSONObject jsonObject1 = new JSONObject(temp);
                if(jsonObject1.getBoolean("success")){
                    JSONObject jsonObject = jsonObject1.getJSONObject("data");

                    String headerFormat = "";
                    if(typeTrans.equals("Sales Transactions")) {
                        headerFormat = "Customer Code: " + jsonObject.getString("cust_code") + "\n" +
                                "Remarks: " + (jsonObject.isNull("remarks") ? "" : jsonObject.getString("remarks")) + "\n" +
                                "Trans. Type: " + jsonObject.getString("transtype") + "\n" +
                                "Gross: " + jsonObject.getDouble("gross") + "\n" +
                                "Discount Amount: " + jsonObject.getDouble("disc_amount") + "\n" +
                                "Document Total: " + jsonObject.getDouble("doctotal") + "\n" +
                                "Applied Amount: " + jsonObject.getDouble("appliedamt") + "\n" +
                                "Tender Amount: " + jsonObject.getDouble("tenderamt") + "\n" +
                                "Amount Due: " + jsonObject.getDouble("amount_due") + "\n";
                    }else if(typeTrans.equals("Received Transactions") || typeTrans.equals("Transfer Transactions")) {
                        String docStatus = jsonObject.getString("docstatus"),
                                docStatusDecode = docStatus.equals("O") ? "Open" : docStatus.equals("C") ? "Closed" : "Cancelled";
                        headerFormat = "Transaction Date: " + jsonObject.getString("transdate").replace("T", " ") + "\n" +
                                "Remarks: " + jsonObject.getString("remarks") + "\n" +
                                "Document Status: " + docStatusDecode+ "\n";
                    }
                    String finalHeaderFormat1 = headerFormat;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtHeader.setText(finalHeaderFormat1);
                        }
                    });

                    String srow;
                    if(typeTrans.equals("Received Transactions")){
                        srow = "recrow";
                    }else if(typeTrans.equals("Transfer Transactions")){
                        srow = "transrow";
                    }
                    else{
                        srow = "salesrow";
                    }
                    JSONArray jsonArrayRecRow = jsonObject.getJSONArray(srow);
                    for (int i = 0; i < jsonArrayRecRow.length(); i++) {
                        JSONObject jsonObjectRecRow = jsonArrayRecRow.getJSONObject(i);

                        final TableRow tableRow = new TableRow(getBaseContext());
                        tableRow.setBackgroundColor(Color.WHITE);
                        LinearLayout linearLayoutItem = new LinearLayout(this);
                        linearLayoutItem.setPadding(10, 10, 10, 10);
                        linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                        linearLayoutItem.setBackgroundColor(Color.WHITE);
                        linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        tableRow.addView(linearLayoutItem);

                        LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextView lblColumn1 = new TextView(this);
                        lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn1.setLayoutParams(layoutParamsItem);
//                       String v = cutWord(item);
                        lblColumn1.setText(jsonObjectRecRow.getString("item_code"));
                        lblColumn1.setTextSize(15);
                        lblColumn1.setTextColor(Color.BLACK);
                        lblColumn1.setBackgroundColor(Color.WHITE);
                        linearLayoutItem.addView(lblColumn1);

                        TextView lblColumn2 = new TextView(getBaseContext());
                        lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn2.setText(df.format(jsonObjectRecRow.getDouble("quantity")));
                        lblColumn2.setTextSize(15);
                        lblColumn2.setTextColor(Color.BLACK);
                        lblColumn2.setBackgroundColor(Color.WHITE);
                        lblColumn2.setPadding(10, 10, 10, 10);
                        tableRow.addView(lblColumn2);

                        TextView lblColumn3 = new TextView(getBaseContext());
                        lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);

                        double col3 = 0.00, col4 = 0.00,col5 = 0.00, col6 = 0.00, col7 = 0.00;
                        if(typeTrans.equals("Received Transactions")) {
                            col3 = jsonObjectRecRow.getDouble("actualrec");
                            col4 = jsonObjectRecRow.getDouble("quantity") - col3;
                        }else if(typeTrans.equals("Sales Transactions")){
                            col3 = jsonObjectRecRow.getDouble("unit_price");
                            col4 = jsonObjectRecRow.getDouble("gross");
                            col5 = jsonObjectRecRow.getDouble("disc_amount");
                            col6 = jsonObjectRecRow.getDouble("discprcnt");
                            col7 = jsonObjectRecRow.getDouble("linetotal");
                        }

                        if(typeTrans.equals("Received Transactions") || typeTrans.equals("Sales Transactions")){
                            lblColumn3.setText(df.format(col3));
                            lblColumn3.setBackgroundColor(Color.WHITE);
                            lblColumn3.setPadding(10, 10, 10, 10);
                            lblColumn3.setTextSize(15);
                            lblColumn3.setTextColor(Color.BLACK);
                            tableRow.addView(lblColumn3);

                            TextView lblColumn4 = new TextView(getBaseContext());
                            lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn4.setText(df.format(col4));
                            lblColumn4.setTextSize(15);
                            lblColumn4.setTextColor(Color.BLACK);
                            lblColumn4.setBackgroundColor(Color.WHITE);
                            lblColumn4.setPadding(10, 10, 10, 10);

                            if(typeTrans.equals("Received Transactions")) {
                                if(col4 == 0.00){
//                                    lblColumn4.setTextColor(Color.BLACK);
                                }
                                if (col4 > 0) {
                                    lblColumn4.setTextColor(Color.BLUE);
                                } else if (col4 < 0) {
                                    lblColumn4.setTextColor(Color.RED);
                                }
                            }
                            tableRow.addView(lblColumn4);

                            if(typeTrans.equals("Received Transactions")){
                                TextView lblColumn8 = new TextView(getBaseContext());
                                lblColumn8.setText(jsonObjectRecRow.getString("inv_uom"));
                                lblColumn8.setBackgroundColor(Color.WHITE);
                                lblColumn8.setPadding(10, 10, 10, 10);
                                lblColumn8.setTextSize(15);
                                lblColumn8.setTextColor(Color.BLACK);
                                tableRow.addView(lblColumn8);
                            }
                        }

                        if(typeTrans.equals("Sales Transactions")){
                            TextView lblColumn4 = new TextView(getBaseContext());
                            lblColumn4.setText(df.format(col5));
                            lblColumn4.setTextSize(15);
                            lblColumn4.setTextColor(Color.BLACK);
                            lblColumn4.setBackgroundColor(Color.WHITE);
                            lblColumn4.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn4);

                            TextView lblColumn5 = new TextView(getBaseContext());
                            lblColumn5.setText(df.format(col6));
                            lblColumn5.setTextSize(15);
                            lblColumn5.setTextColor(Color.BLACK);
                            lblColumn5.setBackgroundColor(Color.WHITE);
                            lblColumn5.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn5);

                            TextView lblColumn6 = new TextView(getBaseContext());
                            lblColumn6.setText(df.format(col7));
                            lblColumn6.setTextSize(15);
                            lblColumn6.setTextColor(Color.BLACK);
                            lblColumn6.setBackgroundColor(Color.WHITE);
                            lblColumn6.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn6);
                        }

                        if(typeTrans.equals("Transfer Transactions")){
                            lblColumn3.setText(jsonObjectRecRow.getString("inv_uom"));
                            lblColumn3.setBackgroundColor(Color.WHITE);
                            lblColumn3.setPadding(10, 10, 10, 10);
                            lblColumn3.setTextSize(15);
                            lblColumn3.setTextColor(Color.BLACK);
                            tableRow.addView(lblColumn3);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tableLayout.addView(tableRow);
                                View viewLine = new View(getBaseContext());
                                LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                                viewLine.setLayoutParams(layoutParamsLine);
                                viewLine.setBackgroundColor(Color.GRAY);
                                tableLayout.addView(viewLine);
                            }
                        });

                    }
                }else{
                    String msg = jsonObject1.getString("message");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Error: \n" + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }catch (Exception ex){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            Runnable r = () -> {
                Toast.makeText(getBaseContext(), temp, Toast.LENGTH_SHORT).show();
            };
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(API_SalesLogsItems.this);
                        pc.removeToken(API_SalesLogsItems.this);
                        startActivity(uic.goTo(API_SalesLogsItems.this, MainActivity.class));
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

}
