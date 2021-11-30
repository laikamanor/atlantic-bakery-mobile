package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.atlanticbakery.Adapter.CustomExpandableListAdapter;
import com.example.atlanticbakery.Helper.FragmentNavigationManager_APIReceived;
import com.example.atlanticbakery.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

public class APIReceived extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private RequestQueue mQueue;
    ProgressBar progressBar;
    Button btnDone, btnSearch,btnDate;
    TextView lblInformation, lblSelectedType, lblType, lblMode, lblHaveQuantity,lblToWhse,lblDate;
    AutoCompleteTextView txtSearch;
    Spinner spinner, spinnerType, spinnerMode, spinnerHaveQuantity,spinnerToWhse;
    CheckBox chckDate;
    Spinner spinnerItemGroup;
    TextView lblItemGroup;

    String title, hidden_title;

    DatabaseHelper4 myDb4;
    DatabaseHelper3 myDb3;
    DatabaseHelper myDb;
    DatabaseHelper8 myDb8;
    DatabaseHelper7 myDb7;
    DatabaseHelper9 myDb9;

    DecimalFormat df = new DecimalFormat("#,##0.00");

    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    //    private String[] items;

    private ExpandableListView expandableListView;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;


    long mLastClickTime = 0;
    private OkHttpClient client;
    JSONObject globalJsonObject;
    Button btnBack, btnRefresh;
    String appName = "", whseCode = "", gBranch;

    CountDownTimer countDownTimer = null;
    private BadgeDrawerArrowDrawable badgeDrawable;
    notification_class notifc = new notification_class(this);
    int sapCount = 0, transferCount = 0, offlineCount = 0, totalCount = 0;
    int isAgent = 0;

    ExpandableListAdapter adapter;

    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i_received);
        mQueue = Volley.newRequestQueue(this);
        progressBar = findViewById(R.id.progWait);
        btnDone = findViewById(R.id.btnDone);
        btnSearch = findViewById(R.id.btnSearch);
        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);
        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);
        myDb7 = new DatabaseHelper7(this);
        myDb9 = new DatabaseHelper9(this);
        lblInformation = findViewById(R.id.lblInformation);
        lblMode = findViewById(R.id.lblMode);
        lblToWhse = findViewById(R.id.lblToWhse);
        lblDate = findViewById(R.id.lblDate);
        btnDate = findViewById(R.id.btnDate);
        spinnerMode = findViewById(R.id.spinnerMode);
        spinnerToWhse = findViewById(R.id.spinnerToWhse);
        lblSelectedType = findViewById(R.id.lblSelectedType);
        lblType = findViewById(R.id.lblType);
        lblHaveQuantity = findViewById(R.id.lblHaveQuantity);
        txtSearch = findViewById(R.id.txtSearch);
        btnBack = findViewById(R.id.btnBack);
        spinner = findViewById(R.id.spinner);
        spinnerType = findViewById(R.id.spinnerType);
        btnRefresh = findViewById(R.id.btnRefresh);
        lblItemGroup = findViewById(R.id.lblItemGroup);
        spinnerItemGroup = findViewById(R.id.spinnerItemGroup);
        spinnerHaveQuantity = findViewById(R.id.spinnerHaveQuantity);
        chckDate = findViewById(R.id.chckDate);
        appName = getString(R.string.app_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        GridLayout gridLayout = findViewById(R.id.grid);
        LinearLayout layout = findViewById(R.id.layout);
        LinearLayout.LayoutParams layoutParams;
        if (width <= 720) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
        } else {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1000);
        }
        layout.setLayoutParams(layoutParams);
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(3);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        client = builder.build();

        globalJsonObject = new JSONObject();


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableListView = (ExpandableListView) findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_APIReceived.getmInstance(this);

        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");

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

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                globalJsonObject = new JSONObject();
                mLastClickTime = SystemClock.elapsedRealtime();
                getItems(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if(hidden_title.equals("API Target For Delivery")){
            spinnerToWhse = findViewById(R.id.spinnerToWhse);
            myToWhse my = new myToWhse();
            my.execute("");

            spinnerToWhse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    globalJsonObject = new JSONObject();
                    mLastClickTime = SystemClock.elapsedRealtime();
                    getItems(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        else{
            spinnerToWhse.setVisibility(View.GONE);
            lblToWhse.setVisibility(View.GONE);
        }

        if(hidden_title.equals("API Target For Delivery") || hidden_title.equals("API Received from Production")){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());

            String dateTitle = hidden_title.equals("API Target For Delivery") ? "Del. Date: " : "Prod. Date: ";

            lblDate.setText(dateTitle + currentDateTime);

            chckDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    btnDate.setEnabled(b);
                    globalJsonObject = new JSONObject();
                    loadData();
                }
            });

            btnDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        showDate();
                    } catch (ParseException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }else{
            lblDate.setVisibility(View.GONE);
            btnDate.setVisibility(View.GONE);
            chckDate.setVisibility(View.GONE);
        }

        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")) {
            myItemGroups my = new myItemGroups();
            my.execute("");

            spinnerItemGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadData();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                List<String> items = Arrays.asList("Item w/ balance", "Item w/o balance");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerHaveQuantity.setAdapter(adapter);

                spinnerHaveQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        TextView lblHaveQuantityReminder = findViewById(R.id.lblHaveQuantityReminder);
                        lblHaveQuantityReminder.setVisibility(spinnerHaveQuantity.getSelectedItemPosition() > 0 && hidden_title.equals("API Menu Items") ? View.VISIBLE : View.GONE);
                        mLastClickTime = SystemClock.elapsedRealtime();
                        getItems(0);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

        }

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalJsonObject = new JSONObject();
                loadData();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDb3.truncateTable();
                Intent intent;
                intent = new Intent(getBaseContext(), APIReceived.class);
                intent.putExtra("title", title);
                intent.putExtra("hiddenTitle", hidden_title);
                startActivity(intent);
                finish();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loadData();
//                globalJsonObject = new JSONObject();
                loadData();
            }
        });
        btnDone.setVisibility(hidden_title.equals("API Production Order List") ? View.GONE : View.VISIBLE);
        startTimer();
    }
    public void showDate() throws ParseException {
        Calendar cCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        String dateTitle = hidden_title.equals("API Target For Delivery") ? "Del. Date: " : "Prod. Date: ";
//        Toast.makeText(getBaseContext(), sdf.parse(lblDate.getText().toString().replace(dateTitle,"")).toString(), Toast.LENGTH_SHORT).show();
        cCalendar.setTime(sdf.parse(lblDate.getText().toString().replace(dateTitle,"")));// all done

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this, cCalendar.get(Calendar.YEAR),
                cCalendar.get(Calendar.MONTH), cCalendar.get(Calendar.DAY_OF_MONTH));
//        if(!isFromDate){
//            Calendar iCalendar = Calendar.getInstance();
//            iCalendar.setTime(sdf.parse(lblDate.getText().toString()));// all done
//            iCalendar.add(Calendar.DAY_OF_MONTH, 45);
//            datePickerDialog.getDatePicker().setMaxDate(iCalendar.getTimeInMillis());
//        }
        datePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateTitle = hidden_title.equals("API Target For Delivery") ? "Del. Date: " : "Prod. Date: ";
        lblDate.setText(dateTitle + year + "-" + (month + 1) + "-" + dayOfMonth);
        globalJsonObject = new JSONObject();
        loadData();
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(5000, 5000) {
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

    public void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
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

    public void selectFirstItemDefault() {
        if (navigationManager != null) {
            String firstItem = listTitle.get(0);
            navigationManager.showFragment(firstItem);
            getSupportActionBar().setTitle(firstItem);
        }
    }

    public void addDrawersItem() {
        adapter = new CustomExpandableListAdapter(this, listTitle, listChild, totalCount, sapCount, transferCount, offlineCount);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedItem = ((List) listChild.get(listTitle.get(groupPosition)))
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
                else if (selectedItem.equals("Sales")) {
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
                else if (selectedItem.equals("Item Request For Transfer")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request For Transfer");
                    intent.putExtra("hiddenTitle", "API Item Request For Transfer");
                    startActivity(intent);
                    finish();
                } else if (selectedItem.equals("Production Order List")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Production Order List");
                    intent.putExtra("hiddenTitle", "API Production Order List");
                    startActivity(intent);
                    finish();
                }
                else if (selectedItem.equals("Change Password")) {
                    changePassword();
                } else if (selectedItem.equals("Logout")) {
                    onBtnLogout();
                } else if (selectedItem.equals("Logs")) {
                    intent = new Intent(getBaseContext(), API_SalesLogs.class);
                    intent.putExtra("title", "Inventory Logs");
                    intent.putExtra("hiddenTitle", "API Inventory Logs");
                    startActivity(intent);
                    finish();
                } else if (selectedItem.equals("Cut Off")) {
                    intent = new Intent(getBaseContext(), CutOff.class);
                    intent.putExtra("title", "Cut Off");
                    intent.putExtra("hiddenTitle", "API Cut Off");
                    startActivity(intent);
                    finish();
                }
                else if (selectedItem.equals("Offline Pending Transactions")) {
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

    public void genData() {
        List<String> title = navc.getTitles(getString(R.string.app_name));
        listChild = new TreeMap<>();
        int iterate = 5;
        int titleIndex = 0;
        while (iterate >= 0) {
            listChild.put(title.get(titleIndex), navc.getItem(title.get(titleIndex)));
            titleIndex += 1;
            iterate -= 1;
        }
        listTitle = new ArrayList<>(listChild.keySet());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu item) {
        getMenuInflater().inflate(R.menu.main_menu, item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            adapter = new CustomExpandableListAdapter(this, listTitle, listChild, totalCount, sapCount, transferCount, offlineCount);
        expandableListView.setAdapter(adapter);
        return true;
    }


    @SuppressLint("SetTextI18n")
    public void changePassword() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
        myDialog.setCancelable(false);
        myDialog.setMessage("*Enter Your New Password");
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 0, 40, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 20);
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
                if (b) {
                    txtPassword.setTransformationMethod(null);
                } else {
                    txtPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });

        layout.addView(checkPassword);

        myDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (txtPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getBaseContext(), "Password field is required", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    APIReceived.myChangePassword myChangePassword = new APIReceived.myChangePassword(txtPassword.getText().toString().trim());
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
        String password;
        LoadingDialog loadingDialog = new LoadingDialog(APIReceived.this);

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
                if (s != null) {
                    JSONObject jsonObjectResponse = new JSONObject(s);
                    loadingDialog.dismissDialog();
                    Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();

                    if (jsonObjectResponse.getBoolean("success")) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(APIReceived.this);
                                        pc.removeToken(APIReceived.this);
                                        startActivity(uic.goTo(APIReceived.this, MainActivity.class));
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

    public void showWarehouses(TextView lblSelectedBranch) {
        AlertDialog _dialog = null;
        AlertDialog.Builder dialogSelectWarehouse = new AlertDialog.Builder(APIReceived.this);
        dialogSelectWarehouse.setTitle("Select Warehouse");
        dialogSelectWarehouse.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);

        AutoCompleteTextView txtSearchBranch = new AutoCompleteTextView(getBaseContext());
        txtSearchBranch.setTextSize(13);
        layout.addView(txtSearchBranch);
        final List<String>[] warehouses = new List[]{returnBranches()};
        final ArrayList<String>[] myReference = new ArrayList[]{getReference(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final ArrayList<String>[] myID = new ArrayList[]{getID(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final List<String>[] listItems = new List[]{getListItems(warehouses[0])};

        TextView btnSearchBranch = new TextView(getBaseContext());
        btnSearchBranch.setBackgroundColor(Color.parseColor("#0b8a0f"));
        btnSearchBranch.setPadding(20, 20, 20, 20);
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

                APIReceived.MyAdapter adapter = new APIReceived.MyAdapter(APIReceived.this, myReference[0], myID[0]);

                listView.setAdapter(adapter);
            }
        });

        layout.addView(btnSearchBranch);


        LinearLayout.LayoutParams layoutParamsWarehouses = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 300);
        layoutParamsWarehouses.setMargins(10, 10, 10, 10);
        listView.setLayoutParams(layoutParamsWarehouses);

        txtSearchBranch.setAdapter(fillItems(listItems[0]));
        APIReceived.MyAdapter adapter = new APIReceived.MyAdapter(APIReceived.this, myReference[0], myID[0]);
        dialogSelectWarehouse.setView(layout);

        dialogSelectWarehouse.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        _dialog = dialogSelectWarehouse.show();
        listView.setAdapter(adapter);

        AlertDialog final_dialog = _dialog;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = view.findViewById(R.id.txtIDs);
                        TextView textView1 = view.findViewById(R.id.txtReference);
                        lblSelectedBranch.setText(textView1.getText().toString());
                        lblSelectedBranch.setTag(textView1.getText().toString());
                        final_dialog.dismiss();
                    }
                });
            }
        });
        layout.addView(listView);
    }

    public List<String> getListItems(List<String> warehouses) {
        List<String> result = new ArrayList<String>();
        for (String temp : warehouses) {
            if (!temp.contains("Select Warehouse")) {
                result.add(temp);
            }
        }
        return result;
    }

    public ArrayList<String> getReference(List<String> warehouses, String value) {
        ArrayList<String> result = new ArrayList<String>();
        for (String temp : warehouses) {
            if (!temp.contains("Select Warehouse")) {
                if (!value.isEmpty()) {
                    if (value.trim().toLowerCase().equals(temp.toLowerCase())) {
                        result.add(temp);
                    }
                } else {
                    result.add(temp);
//                    myID.add("0");
                }
            }
        }
        return result;
    }

    public ArrayList<String> getID(List<String> warehouses, String value) {
        ArrayList<String> result = new ArrayList<String>();
        for (String temp : warehouses) {
            if (!temp.contains("Select Warehouse")) {
                if (!value.isEmpty()) {
                    if (value.trim().contains(temp)) {
                        result.add("0");
//                        myID.add("0");
                    }
                } else {
                    result.add("0");
//                    myID.add("0");
                }
            }
        }
        return result;
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
            View row = layoutInflater.inflate(R.layout.custom_list_view_sales_logs, parent, false);
            TextView textView1 = row.findViewById(R.id.txtReference);
            TextView textView2 = row.findViewById(R.id.txtIDs);

            textView1.setText(myReference.get(position));
            textView2.setText(myIds.get(position));
            textView2.setVisibility(View.INVISIBLE);

            return row;
        }
    }


    public void hmReturnBranches() {
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        String URL = IPaddress + "/api/whse/get_all";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(URL)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(30, TimeUnit.SECONDS);
                    builder.readTimeout(30, TimeUnit.SECONDS);
                    builder.writeTimeout(30, TimeUnit.SECONDS);
                    client = builder.build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    e.printStackTrace();
//                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Cursor cursor = myDb8.getAllData();
                            while (cursor.moveToNext()) {
                                String module = cursor.getString(3);
                                if (module.contains("Warehouse")) {
//                                    System.out.println(cursor.getString(4));
                                    if (hidden_title.equals("API Item Request")) {
                                        if (cursor.getString(4).toLowerCase().contains("prod")) {
                                            gBranch = cursor.getString(4);
                                        }
                                    } else {
                                        gBranch = cursor.getString(4);
                                    }
                                } else {
                                    System.out.println("ELSE: " + cursor.getString(4));
                                }
                            }
                        }

                        @Override
                        public void onResponse(Call call, okhttp3.Response response) {
                            try {
//                                System.out.println(response.body().string());
                                String sResult = response.body().string();
                                gBranch = sResult;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public List<String> returnBranches() {
        List<String> result = new ArrayList<>();
        result.add("Select Warehouse");
        try {
            if (!gBranch.isEmpty()) {
                if (gBranch.substring(0, 1).equals("{")) {
                    JSONObject jsonObjectResponse = new JSONObject(gBranch);
                    if (!jsonObjectResponse.isNull("data") && jsonObjectResponse.getBoolean("success")) {
                        JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
                            String branch = jsonObject.getString("whsename");
                            result.add(branch);

                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Error \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Error \n" + gBranch, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "Error \n" + gBranch, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), "Front-end Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public void loadData() {
        if (hidden_title.equals("API Received from SAP")) {
            if (myDb3.countItems(hidden_title) > 0) {

                Cursor cursor = myDb3.getAllData(hidden_title);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String fromBranch = (hidden_title.equals("API System Transfer Item") ? cursor.getString(8) : cursor.getString(2));
                        if (fromBranch.isEmpty()) {
                            lblInformation.setVisibility(View.GONE);
                        } else {
                            lblInformation.setVisibility(View.VISIBLE);
                        }
                    }
                }
                cursor.close();
                btnBack.setVisibility(View.VISIBLE);
                lblInformation.setVisibility(View.VISIBLE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                loadSelectedSAPNumberItems();
            } else {
                List<String> items;
                if (appName.equals("Atlantic Bakery")) {
                    items = Arrays.asList("IT", "PO");
                } else {
                    items = Arrays.asList("IT", "PO", "DN", "AR");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                lblInformation.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
            }

        } else if (hidden_title.equals("API System Transfer Item") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Confirm Issue For Production") || hidden_title.equals("API Item Request For Transfer")) {
            if (myDb3.countItems(hidden_title) <= 0) {
                if (hidden_title.equals("API System Transfer Item")) {
                    lblMode.setVisibility(View.VISIBLE);
                    spinnerMode.setVisibility(View.VISIBLE);
                    List<String> items = Arrays.asList("For Production Items", "For Sale Items");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMode.setAdapter(adapter);
                    spinnerMode.setSelection(0);
                    spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            globalJsonObject = new JSONObject();
                            mLastClickTime = SystemClock.elapsedRealtime();
                            getItems(0);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                lblInformation.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
            } else {
                lblInformation.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.VISIBLE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                loadSelectedSAPNumberItems();
            }
        } else if (hidden_title.equals("API Received Item")) {
            if (myDb3.countItems(hidden_title) <= 0) {
                spinnerItemGroup.setVisibility(View.VISIBLE);
                lblItemGroup.setVisibility(View.VISIBLE);

                lblInformation.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                lblType.setVisibility(View.VISIBLE);
                spinnerType.setVisibility(View.VISIBLE);
                List<String> items = Arrays.asList("Select Type", "SAPIT", "SAPPO","SAPGR");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerType.setAdapter(adapter);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                getItems(0);
            }
        } else if (hidden_title.equals("API Transfer Item")) {
            spinnerItemGroup.setVisibility(View.VISIBLE);
            lblItemGroup.setVisibility(View.VISIBLE);

            lblInformation.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            lblSelectedType.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            lblType.setVisibility(View.GONE);
            spinnerType.setVisibility(View.GONE);

            lblHaveQuantity.setVisibility(View.VISIBLE);
            spinnerHaveQuantity.setVisibility(View.VISIBLE);
            getItems(0);
        } else if (hidden_title.equals("API Item Request")) {
            if (myDb3.countItems(hidden_title) <= 0) {
                spinnerItemGroup.setVisibility(View.VISIBLE);
                lblItemGroup.setVisibility(View.VISIBLE);

                lblInformation.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
            }
        } else if (hidden_title.equals("API Menu Items")) {
            spinnerItemGroup.setVisibility(View.VISIBLE);
            lblItemGroup.setVisibility(View.VISIBLE);
            lblInformation.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            lblSelectedType.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            lblType.setVisibility(View.GONE);
            spinnerType.setVisibility(View.GONE);
            getItems(0);

            lblHaveQuantity.setVisibility(View.VISIBLE);
            spinnerHaveQuantity.setVisibility(View.VISIBLE);
        } else if (hidden_title.equals("API Inventory Count") || hidden_title.equals("API Pull Out Count") || hidden_title.equals("API Inventory Count Variance") || hidden_title.equals("API Pull Out Count Variance") || hidden_title.equals("API Pull Out Request Confirmation")) {
            if (myDb3.countItems(hidden_title) > 0 && hidden_title.equals("API Pull Out Request Confirmation")) {
                lblInformation.setVisibility(View.VISIBLE);
                lblSelectedType.setVisibility(View.GONE);
                btnBack.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
               loadSelectedSAPNumberItems();
            }else {
                lblInformation.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
                btnDone.setVisibility(hidden_title.equals("API Pull Out Request Confirmation") ? View.GONE : View.VISIBLE);
            }
        }
        else if (hidden_title.equals("API Production Order List")) {
            if (myDb3.countItems(hidden_title) > 0) {
                btnBack.setVisibility(View.VISIBLE);
                lblInformation.setVisibility(View.VISIBLE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                loadSelectedSAPNumberItems();
            } else {
                lblInformation.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
            }
        }
        else if (hidden_title.equals("API Received from Production") || hidden_title.equals("API Target For Delivery")) {
            if (myDb3.countItems(hidden_title) > 0) {
                btnBack.setVisibility(View.VISIBLE);
                lblInformation.setVisibility(View.VISIBLE);
                lblSelectedType.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                lblToWhse.setVisibility(View.GONE);
                spinnerToWhse.setVisibility(View.GONE);
                chckDate.setVisibility(View.GONE);
                lblDate.setVisibility(View.GONE);
                btnDate.setVisibility(View.GONE);
                loadSelectedSAPNumberItems();
            } else {
                lblInformation.setVisibility(View.GONE);
                lblSelectedType.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                lblType.setVisibility(View.GONE);
                spinnerType.setVisibility(View.GONE);
                getItems(0);
            }
        }
    }

    private class myToTest extends AsyncTask<String, Void, String> {
        int id = 0;
        String reference = "",delDate = "";
        LoadingDialog loadingDialog = new LoadingDialog(APIReceived.this);
        public myToTest(int id, String reference, String delDate){
            this.id = id;
            this.reference = reference;
            this.delDate = delDate;
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
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

                SharedPreferences sharedPreferences1 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences1.getString("token", "");


                String sURL = hidden_title.equals("API Target For Delivery") ? "/api/forecast/get_for_delivery/details/" : "/api/production/order/details/";
                String sParams = this.id + (hidden_title.equals("API Target For Delivery") ? "?mode=for_delivery" : "?mode=receive");
//                System.out.println("IP Address: " + IPAddress);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + sURL + sParams)
                        .addHeader("Authorization", "Bearer " + token)
                        .method("GET", null)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {
                loadingDialog.dismissDialog();
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (s != null) {
                    if (s.substring(0, 1).equals("{")) {
                        JSONObject jsonObject1 = new JSONObject(s);
                        String msg = jsonObject1.getString("message");
                        if (jsonObject1.getBoolean("success")) {
                            List<String> tenderTypes = new ArrayList<>();
                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
//                            tenderTypes.add("All");
                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(ii);

                                //qty
                                String qtyKey = hidden_title.equals("API Target For Delivery") ? "balance" : "variance";
                                String qtyKey_ = qtyKey.replace("_"," ");
                                String qtyName = hidden_title.equals("API Received from Production") ? "For Receive: " : (StringFormatter.capitalizeWord(qtyKey_) + ": ");
                                //whse
                                String whseKey = hidden_title.equals("API Target For Delivery") ? "to_whse" : "whsecode";
                                String whseKey_ = whseKey.replace("_"," ");
                                String whseName = StringFormatter.capitalizeWord(whseKey_) + ": ";

                                //date
                                String dateKey = hidden_title.equals("API Target For Delivery") ? "delivery_date" : "production_date";
                                String dateKey_ = dateKey.replace("_"," ");
                                String dateName = StringFormatter.capitalizeWord(dateKey_) + ": ";

                                tenderTypes.add("Item Code: " + jsonObject.getString("item_code") + "\n" + qtyName + df.format(jsonObject.getDouble(qtyKey)) + "\n" + whseName + jsonObject.getString(whseKey) + "\n" + dateName + delDate);
                            }
//                            System.out.println(tenderTypes);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                    builder.setTitle(reference)
                                            .setCancelable(false)
//                                            .setPositiveButton("Close", (dialog, which) -> {
//                                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//                                                    return;
//                                                }
//                                                mLastClickTime = SystemClock.elapsedRealtime();
//                                                pc.loggedOut(APIReceived.this);
//                                                pc.removeToken(APIReceived.this);
//                                                startActivity(uic.goTo(APIReceived.this, MainActivity.class));
//                                                finish();
//                                            })

                                            .setNegativeButton("Close", (dialog, which) -> dialog.cancel());



                                    View rowList = getLayoutInflater().inflate(R.layout.row, null);
                                    ListView listView = rowList.findViewById(R.id.listView);
                                    listView.setPadding(10,10,10,10);
                                    ArrayAdapter adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_list_item_1, tenderTypes);
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();


                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.setView(rowList);
                                    alertDialog.show();

                                    loadingDialog.dismissDialog();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getBaseContext(), "Validation \n" + s, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        globalJsonObject = new JSONObject();
                        loadingDialog.dismissDialog();
                        Toast.makeText(getBaseContext(), "Validation \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class myToWhse extends AsyncTask<String, Void, String> {
        LoadingDialog loadingDialog = new LoadingDialog(APIReceived.this);
        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
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

                SharedPreferences sharedPreferences1 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences1.getString("token", "");

//                System.out.println("IP Address: " + IPAddress);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/whse/get_all?is_main=1")
                        .addHeader("Authorization", "Bearer " + token)
                        .method("GET", null)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (s != null) {
                    if (s.substring(0, 1).equals("{")) {
                        JSONObject jsonObject1 = new JSONObject(s);
                        String msg = jsonObject1.getString("message");
                        if (jsonObject1.getBoolean("success")) {
                            List<String> tenderTypes = new ArrayList<>();
                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                            tenderTypes.add("All");
                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
                              tenderTypes.add(jsonObject.getString("whsecode"));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, tenderTypes);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerToWhse.setAdapter(adapter);
                            loadingDialog.dismissDialog();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getBaseContext(), "Validation \n" + s, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        globalJsonObject = new JSONObject();
                        loadingDialog.dismissDialog();
                        Toast.makeText(getBaseContext(), "Validation \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class myItemGroups extends AsyncTask<String, Void, String> {
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

                SharedPreferences sharedPreferences1 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences1.getString("token", "");

//                System.out.println("IP Address: " + IPAddress);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/item/item_grp/getall")
                        .addHeader("Authorization", "Bearer " + token)
                        .method("GET", null)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {

                Cursor cursor = myDb8.getAllData();
                while (cursor.moveToNext()){
                    String module = cursor.getString(4);
                    if(module.contains("Item Group")){
                        return cursor.getString(3);
                    }
                }
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (s != null) {
                    if (s.substring(0, 1).equals("{")) {
                        JSONObject jsonObject1 = new JSONObject(s);
                        String msg = jsonObject1.getString("message");
                        if (jsonObject1.getBoolean("success")) {
                            List<String> tenderTypes = new ArrayList<>();
                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                            tenderTypes.add("All");
                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                            String isSales = Objects.requireNonNull(sharedPreferences.getString("isSales", ""));
                            String isProduction = Objects.requireNonNull(sharedPreferences.getString("isProduction", ""));
                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
//                                System.out.println("issales: " + isSales + "\n" + "isProduction: " + isProduction);
                                if (hidden_title.equals("API Received Item") && isProduction.equals("1")) {
                                    tenderTypes.add(jsonObject.getString("code"));
                                } else if (hidden_title.equals("API Item Request") && isProduction.equals("1")) {
                                    tenderTypes.add(jsonObject.getString("code"));
                                } else {
                                    if (!jsonObject.getString("code").toLowerCase().contains("Raw materials".toLowerCase()) && !jsonObject.getString("code").toLowerCase().contains("Premix".toLowerCase()) && !jsonObject.getString("code").toLowerCase().contains("Intermediate Goods".toLowerCase()) && !jsonObject.getString("code").toLowerCase().contains("Pack&Oth".toLowerCase())) {
                                        tenderTypes.add(jsonObject.getString("code"));
                                    }
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, tenderTypes);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerItemGroup.setAdapter(adapter);
                            globalJsonObject = new JSONObject();
                            loadData();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    globalJsonObject = new JSONObject();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        globalJsonObject = new JSONObject();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Validation \n" + s, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Cursor cursor = myDb8.getAllData();
                            while (cursor.moveToNext()) {
                                String module = cursor.getString(3);
//                                System.out.println("Moduleee: " + module);
                                if (module.contains("Item Group")) {
                                    try {
                                        JSONObject jsonObject1 = new JSONObject(cursor.getString(4));
                                        String msg = jsonObject1.getString("message");
                                        if (jsonObject1.getBoolean("success")) {
                                            List<String> tenderTypes = new ArrayList<>();
                                            tenderTypes.add("All");
                                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                            String isSales = Objects.requireNonNull(sharedPreferences.getString("isSales", ""));
                                            String isProduction = Objects.requireNonNull(sharedPreferences.getString("isProduction", ""));
                                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
//                                                System.out.println("issales: " + isSales + "\n" + "isProduction: " + isProduction);
                                                if (isSales.equals("1") && !isProduction.equals("1")) {
                                                    if (!jsonObject.getString("code").toLowerCase().equals("Raw materials".toLowerCase())) {
                                                        tenderTypes.add(jsonObject.getString("code"));
                                                    }
                                                } else if (!isSales.equals("1") && !isProduction.equals("1")) {
                                                    if (!jsonObject.getString("code").toLowerCase().equals("Raw materials".toLowerCase())) {
                                                        tenderTypes.add(jsonObject.getString("code"));
                                                    }
                                                } else {
                                                    tenderTypes.add(jsonObject.getString("code"));
                                                }
                                            }
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, tenderTypes);
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spinnerItemGroup.setAdapter(adapter);
                                            globalJsonObject = new JSONObject();
                                            loadData();
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    globalJsonObject = new JSONObject();
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (JSONException ex) {
                                        globalJsonObject = new JSONObject();
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        globalJsonObject = new JSONObject();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Validation \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //        Toast.makeText(getBaseContext(), "RECEIVED", Toast.LENGTH_SHORT).show();
        int totalCart = myDb.countItems();
//        MenuItem nav_ShoppingCart = menu.findItem(R.id.nav_shoppingCart);
//        nav_ShoppingCart.setTitle("Shopping Cart (" + totalCart + ")");

        if (hidden_title.equals("API Received from SAP")) {
            lblSelectedType.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
        } else {
            lblSelectedType.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        }

//        if(hidden_title.equals("API Transfer Item")){
//        }else if(hidden_title.equals("API Menu Items")){
//        }else if(hidden_title.equals("API Received Item")){
//        }
//        else if(hidden_title.equals("API Item Request")){
//        }
//        else {
//            globalJsonObject = new JSONObject();
//            loadData();
//        }
//        if(API_ItemInfo.isSubmit){
//            loadData();
//        }
        btnRefresh.performClick();
    }


    @Override
    public void onBackPressed() {
//        if(backPressedTime + 2000 > System.currentTimeMillis()){
//            super.onBackPressed();
//            return;
//        }
//        else{
//            Toast.makeText(getBaseContext(), "Press back again to close " + title, Toast.LENGTH_SHORT).show();
//        }
//        backPressedTime = System.currentTimeMillis();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBtnLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    pc.loggedOut(APIReceived.this);
                    pc.removeToken(APIReceived.this);
                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public ArrayAdapter<String> fillItems(List<String> items) {
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
    }

    @SuppressLint("SetTextI18n")
    public void uiItems2(int id, String itemName, String sapNumber, double quantity, String fromBranch, boolean isSelected, double receivedQuantity, String uom, int itemID, boolean isClosed, int iterate, double variance) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int boxWidth = 0, boxHeight = 0;
        if(width <= 720){
            boxHeight = 230;
            boxWidth = 190;
        }else{
            boxHeight = 320;
            boxWidth = 280;
        }

        int cardViewMarginLeft = 0;
        if (iterate % 3 == 0) {
            cardViewMarginLeft = 5;
        }

        GridLayout gridLayout = findViewById(R.id.grid);
        CardView cardView = new CardView(APIReceived.this);
        LinearLayout.LayoutParams layoutParamsCv = new LinearLayout.LayoutParams(boxWidth, boxHeight);
        layoutParamsCv.setMargins(cardViewMarginLeft, 5, 5, 5);
        cardView.setLayoutParams(layoutParamsCv);
        cardView.setRadius(12);
        cardView.setCardElevation(5);


//        System.out.println("item: " + itemName + "\n quantity: " + quantity + "\n delivered: " + receivedQuantity);

        cardView.setVisibility(View.VISIBLE);
        gridLayout.addView(cardView);
        final LinearLayout linearLayout = new LinearLayout(APIReceived.this);
        LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 5f);
        linearLayout.setLayoutParams(layoutParamsLinear);
        linearLayout.setTag(id);

        linearLayout.setOnClickListener(view -> {
            if (hidden_title.equals("API Production Order List")) {
                if (isSelected || isClosed) {
                    Toast.makeText(getBaseContext(), "'" + itemName + "' is already closed!", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
                    myDialog.setCancelable(false);
                    myDialog.setTitle(itemName);
//            System.out.println("ID: " + finalDocEntry);
                    myDialog.setMessage("Are you sure you want to close?");
                    myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            JSONObject jsonObject = new JSONObject();
                            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                            SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                            String IPaddress = sharedPreferences2.getString("IPAddress", "");
                            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                            String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                            okhttp3.Request request = new okhttp3.Request.Builder()
                                    .url(IPaddress + "/api/production/order/details/close/" + itemID)
                                    .method("PUT", body)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Content-Type", "application/json")
                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, okhttp3.Response response) {
                                    String result = "";
                                    try {
                                        result = response.body().string();
//                            System.out.println(result);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String finalResult = result;
                                    APIReceived.this.runOnUiThread(() -> {
                                        try {
                                            JSONObject jj = new JSONObject(finalResult);
                                            boolean isSuccess = jj.getBoolean("success");
                                            if (isSuccess) {
                                                Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                                boolean isInserted = myDb3.updateSelected(Integer.toString(id), 1, quantity);
                                                if (isInserted) {
                                                    Intent intent;
                                                    intent = new Intent(getBaseContext(), APIReceived.class);
                                                    intent.putExtra("title", title);
                                                    intent.putExtra("hiddenTitle", hidden_title);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(getBaseContext(), "Failed to Close", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                String msg = jj.getString("message");
                                                if (msg.equals("Token is invalid")) {
                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                                    builder.setCancelable(false);
                                                    builder.setMessage("Your session is expired. Please login again.");
                                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                                        pc.loggedOut(APIReceived.this);
                                                        pc.removeToken(APIReceived.this);
                                                        startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                                        finish();
                                                        dialog.dismiss();
                                                    });
                                                    builder.show();
                                                } else {
                                                    Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    myDialog.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
                    myDialog.show();
                }
            } else {
                if (isSelected) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent;
                    intent = new Intent(getBaseContext(), API_ItemInfo.class);
                    intent.putExtra("title", title);
                    intent.putExtra("hiddenTitle", hidden_title);
                    intent.putExtra("item", itemName);
                    intent.putExtra("sapNumber", sapNumber);
                    intent.putExtra("quantity", Double.toString(quantity));
                    intent.putExtra("fromBranch", fromBranch);
                    intent.putExtra("variance", variance);
                    intent.putExtra("deliveredQuantity", quantity);
//                System.out.println("receivedQuantity: " + receivedQuantity);
                    intent.putExtra("receivedQuantity", receivedQuantity);
                    intent.putExtra("id", id);
                    startActivity(intent);

//                                loadSelectedSAPNumberItems();
                }
            }
        });
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.setVisibility(View.VISIBLE);


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 0, 20, 0);
        LinearLayout.LayoutParams layoutParamsItemLeft = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsItemLeft.setMargins(20, (hidden_title.equals("API Item Request For Transfer") || hidden_title.equals("API Received from Production") ? -100 : -50), 0, 10);

        TextView txtItemName = new TextView(APIReceived.this);
        String cutWord = cutWord(itemName, 22);
        txtItemName.setText(cutWord);
        txtItemName.setLayoutParams(layoutParams);
        txtItemName.setTextSize(15);
        txtItemName.setVisibility(View.VISIBLE);

        TextView txtItemLeft = new TextView(APIReceived.this);
        txtItemLeft.setLayoutParams(layoutParamsItemLeft);
        txtItemLeft.setTextSize(13);
        txtItemLeft.setVisibility(View.VISIBLE);
        txtItemLeft.setText(df.format(hidden_title.equals("API Received from Production") ? variance : quantity) + " " + uom);
        txtItemLeft.setTextColor(Color.parseColor("#34A853"));
        if (isSelected || isClosed) {
            linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
            txtItemName.setTextColor(Color.rgb(250, 250, 250));
            txtItemLeft.setTextColor(Color.rgb(250, 250, 250));
        }
//            else if(hidden_title.equals("API Received from Production") && receivedQuantity > 0) {
//                linearLayout.setBackgroundColor(Color.rgb(250, 208, 17));
//                txtItemName.setTextColor(Color.BLACK);
//                txtItemLeft.setTextColor(Color.BLACK);
//            }
        else {
            linearLayout.setBackgroundColor(Color.rgb(250, 250, 250));
            txtItemName.setTextColor(Color.rgb(28, 28, 28));
            txtItemLeft.setTextColor(Color.parseColor("#34A853"));
        }

        if(hidden_title.equals("API Received from Production")){
            if(quantity < 0){
                txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
            }else{
                txtItemLeft.setTextColor(Color.parseColor("#34A853"));
            }
            if (isSelected || isClosed) {
                txtItemLeft.setTextColor(Color.rgb(250, 250, 250));
            }
        }

        cardView.addView(linearLayout);
        linearLayout.addView(txtItemName);
        linearLayout.addView(txtItemLeft);
    }

    @SuppressLint("SetTextI18n")
    public void loadSelectedSAPNumberItems() {
        Handler handler = new Handler();
        progressBar.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            GridLayout gridLayout = findViewById(R.id.grid);
            gridLayout.removeAllViews();
            Cursor cursor = myDb3.getAllData(hidden_title);
            if (cursor != null) {
                List<String> listItems = new ArrayList<String>();
                int iterate = 1;
                while (cursor.moveToNext()) {
                    final int id = cursor.getInt(0);
                    final String sapNumber = cursor.getString(1);
                    final String fromBranch = (hidden_title.equals("API System Transfer Item") ? cursor.getString(8) : cursor.getString(2));
                    final String itemName = cursor.getString(3);
                    final double quantity = cursor.getDouble(4);
                    final double variance = cursor.getDouble(17);
                    final boolean isSelected = (cursor.getInt(6) > 0);
                    String toBranch = cursor.getString(8);

//                    String sData = cursor.getString(16);
//                    String delDate = "";
//                    if(sData.startsWith("{")){
//                        try{
//                            JSONObject joResponse = new JSONObject(sData);
//                            JSONArray jsonArray = joResponse.getJSONArray("data");
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                System.out.println(jsonObject);
//                                delDate = jsonObject.getString("delivery_date");
//                            }
//                        }catch (Exception ex){
//                            ex.printStackTrace();
//                        }
//                    }

                    lblInformation.setText("Ref #: " + sapNumber + "\nBranch: " +fromBranch + (hidden_title.equals("API Target For Delivery") ? "\nTo Whse: " + toBranch : ""));
                    listItems.add(itemName);
                    String uom = cursor.getString(11);
                    int received_quantity = cursor.getInt(12);
                    int itemID = cursor.getInt(13);
                    boolean isClosed = cursor.getInt(14) > 0;
                    if (!txtSearch.getText().toString().trim().isEmpty()) {
                        if (txtSearch.getText().toString().trim().toLowerCase().contains(itemName.toLowerCase())) {
                            uiItems2(id, itemName, sapNumber, quantity, fromBranch, isSelected, received_quantity, uom, itemID, false, iterate,variance);
                            iterate += 1;
                        }
                    } else {
                        uiItems2(id, itemName, sapNumber, quantity, fromBranch, isSelected, received_quantity, uom, itemID, false, iterate,variance);
                        iterate += 1;
                    }
                }
                txtSearch.setAdapter(fillItems(listItems));
            }
            cursor.close();
            progressBar.setVisibility(View.GONE);
        }, 500);
        btnDone.setOnClickListener(view -> navigateDone());
    }

    public void insertSAPItems(Integer docEntry, String supplier) {
        String appendURL;
        if (spinner.getSelectedItemPosition() == 0) {
            appendURL = "/api/sapb1/itdetails/" + docEntry;
        } else if (spinner.getSelectedItemPosition() == 1) {
            appendURL = "/api/sapb1/podetails/" + docEntry;
        } else if (spinner.getSelectedItemPosition() == 2) {
            appendURL = "/api/sapb1/dndetails/" + docEntry;
        } else {
            appendURL = "/api/sapb1/ardetails/" + docEntry;
        }

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
                            int countError = 0;
                            String selectedSapNumber = "N/A";
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String sap_number,
                                        fromBranch,
                                        itemName,
                                        toBranch;
                                Double quantity;
                                int isSAPIT_int;
                                int baseID;
                                selectedSapNumber = jsonObject.getString("docnum");
                                sap_number = jsonObject.getString("docnum");

                                if (spinner.getSelectedItemPosition() == 0) {
                                    fromBranch = jsonObject.getString("fromwhscod");
                                } else {
                                    fromBranch = supplier;
                                }

                                itemName = jsonObject.getString("itemcode");
                                toBranch = jsonObject.getString("whscode");
                                quantity = jsonObject.getDouble("quantity");

                                int ss = 0;
                                if (spinner.getSelectedItemPosition() == 0) {
                                    ss = 1;
                                } else if (spinner.getSelectedItemPosition() == 1) {
                                    ss = 0;
                                } else if (spinner.getSelectedItemPosition() == 2) {
                                    ss = 2;
                                } else {
                                    ss = 3;
                                }
                                String uom = jsonObject.getString("unitmsr");
//                                System.out.println("UOM: " + uom);
//                                isSAPIT_int = (supplier.equals("")  ? 3 : ss);
                                isSAPIT_int = ss;
                                baseID = 0;
                                boolean isSuccess = myDb3.insertData(sap_number, fromBranch, itemName, quantity, 0.00, isSAPIT_int, toBranch, baseID, hidden_title, 0, uom, 0, 0, 0, 0,response.toString(),0.00);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
//                                Intent intent = new Intent(getBaseContext(), ItemReceivable.class);
//                                intent.putExtra("title", title);
//                                intent.putExtra("hiddenTitle", hidden_title);
//                                startActivity(intent);
//                                finish();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + docEntry + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }, 500);
    }

    public void insertSystemTransfer(Integer id, String referenceNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String appendURL = (hidden_title.equals("API Pull Out Request Confirmation") ? "/api/pullout/details/" : "/api/inv/trfr/forrec/getdetails/") + id;

        System.out.println(IPaddress + appendURL);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(IPaddress + appendURL)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String s = response.body().string();
                    if(s.substring(0,1).equals("{")){
                        JSONObject joResponse = new JSONObject(s);
                        if (joResponse.getBoolean("success")) {
                            int countError = 0;

                            String selectedSapNumber = referenceNumber;
                            if(hidden_title.equals("API Pull Out Request Confirmation")){
                                JSONObject joData = joResponse.getJSONObject("data");
                                JSONArray jsonArray;
                                jsonArray = joData.getJSONArray("row");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String fromBranch,
                                            itemName,
                                            toBranch,
                                            uom;
                                    Double quantity;

                                    fromBranch = jsonObject.getString(hidden_title.equals("API Pull Out Request Confirmation") ? "whsecode" : "from_whse");
                                    itemName = jsonObject.getString("item_code");
                                    quantity = jsonObject.getDouble("quantity");
                                    toBranch = jsonObject.getString("to_whse");
                                    uom = jsonObject.getString("uom");
                                    double int_quantity = jsonObject.getDouble("quantity");
                                    boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id, hidden_title, 0, uom, 0, 0, 0, 0,response.toString(),0.00);
                                    if (!isSuccess) {
                                        countError += 1;
                                    }
                                }
                            }else {
                                JSONArray jsonArray;
                                jsonArray = joResponse.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String fromBranch,
                                            itemName,
                                            toBranch,
                                            uom;
                                    Double quantity;

                                    fromBranch = jsonObject.getString("from_whse");
                                    itemName = jsonObject.getString("item_code");
                                    quantity = jsonObject.getDouble("quantity");
                                    toBranch = jsonObject.getString("to_whse");
                                    uom = jsonObject.getString("uom");
                                    double int_quantity = jsonObject.getDouble("quantity");
                                    boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id, hidden_title, 0, uom, 0, 0, 0, 0,response.toString(),0.00);
                                    if (!isSuccess) {
                                        countError += 1;
                                    }
                                }
                            }

                            if (countError <= 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                            intent.putExtra("title", title);
                                            intent.putExtra("hiddenTitle", hidden_title);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            }
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getBaseContext(), joResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ex.printStackTrace();
                            Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void getItems(int docEntry) {
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String currentBranch = Objects.requireNonNull(sharedPreferences2.getString("branch", ""));
        String currentWhse = Objects.requireNonNull(sharedPreferences2.getString("whse", ""));
        progressBar.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = sdf.format(new Date());
                String appendURL = "";
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                if (docEntry > 0) {
                    appendURL = "/api/sapb1/itdetails/" + docEntry;
                } else if (hidden_title.equals("API Received Item")) {
                    appendURL = "/api/item/getall?is_active=1";
                } else if (hidden_title.equals("API Item Request")) {
                    appendURL = "/api/item/getall?is_active=1";
                } else if (hidden_title.equals("API Item Request For Transfer")) {
                    appendURL = "/api/inv/item_request/get_all";
                } else if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                    appendURL = "/api/inv/whseinv/getall";
                } else if (hidden_title.equals("API System Transfer Item")) {
                    appendURL = "/api/inv/trfr/forrec?mode=" +  spinnerMode.getSelectedItem().toString();
                } else if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 0) {
//                        globalJsonObject = new JSONObject();
                    appendURL = "/api/sapb1/getit";
                } else if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 1) {
//                        globalJsonObject = new JSONObject();
                    appendURL = "/api/sapb1/getpo";
                } else if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 2) {
//                        globalJsonObject = new JSONObject();
                    appendURL = "/api/sapb1/getdn";
                } else if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 3) {
//                        globalJsonObject = new JSONObject();
                    appendURL = "/api/sapb1/getar";
                } else if (hidden_title.equals("API Inventory Count") || hidden_title.equals("API Inventory Count Variance")) {
                    SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                    String isManager = sharedPreferences2.getString("isManager", "");
                    appendURL = Integer.parseInt(isManager) > 0  && hidden_title.equals("API Inventory Count Variance") ?  "/api/inv/count/variance?date=" + currentDate  : "/api/inv/count/create?date=" + currentDate;
                } else if (hidden_title.equals("API Pull Out Count")  || hidden_title.equals("API Pull Out Count Variance")) {
                    SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                    String isManager = sharedPreferences2.getString("isManager", "");
                    appendURL = Integer.parseInt(isManager) > 0  && hidden_title.equals("API Pull Out Count Variance") ?  "/api/pulloutreq/variance?date=" + currentDate  : "/api/pulloutreq/create?date=" + currentDate;
                }
                else if (hidden_title.equals("API Issue For Production")) {
                    appendURL = "/api/production/order/get_all?mode=issue";
                }
                else if (hidden_title.equals("API Pull Out Request Confirmation")) {
                    appendURL = "/api/pullout/get_for_confirm";
                }
                else if (hidden_title.equals("API Production Order List")) {
                    appendURL = "/api/production/order/get_all?docstatus=O&branch=" + currentBranch +"&whsecode=&from_date=&to_date=" + currentDate;
                }
                else if (hidden_title.equals("API Received from Production")) {
                    String sProdDateValue = lblDate.getText().toString().replace("Prod. Date: ","");
                    String sProdDateValueFinal = "";
                    try {
                        @SuppressLint("SimpleDateFormat") Date date1 =new SimpleDateFormat("yyyy-MM-dd").parse(sProdDateValue);
                        sProdDateValueFinal = dateFormat.format(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    boolean isChck = chckDate.isChecked();
                    String prodDate = "&production_date=" + (isChck ? sProdDateValueFinal : "");
                    String sMode = "?mode=receive";
                    String sParams = sMode + prodDate;
                    appendURL = "/api/production/order/get_all" + sParams;
                }
                else if (hidden_title.equals("API Target For Delivery")) {
                    String sDelDateValue = lblDate.getText().toString().replace("Del. Date: ","");
                    String sDelDateValueFinal = "";
                    try {
                        @SuppressLint("SimpleDateFormat") Date date1 =new SimpleDateFormat("yyyy-MM-dd").parse(sDelDateValue);
                        sDelDateValueFinal = dateFormat.format(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    boolean isChck = chckDate.isChecked();

                    String sDelDate = "&delivery_date=" + (isChck ? sDelDateValueFinal : "");
                    String sToWhse = "?to_whse=";
                    if(spinnerToWhse != null){
                        sToWhse += spinnerToWhse.getSelectedItem() == null || spinnerToWhse.getSelectedItemPosition() == 0 || spinnerToWhse.getSelectedItem().toString() == "All" ? "" :  spinnerToWhse.getSelectedItem().toString();
                    }

//                    String delDate = "&delivery_date=" + lblDate.getText().toString().replace("Delivery Date: ","");

                    String sParams = sToWhse + sDelDate;
                    appendURL = "/api/forecast/get_for_delivery" + sParams;
                    System.out.println("app " +appendURL);
//                    String finalAppendURL = appendURL;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getBaseContext(), finalAppendURL,Toast.LENGTH_LONG).show();
//                        }
//                    });
                }
                else if(hidden_title.equals("API Confirm Issue For Production")){
                    SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                    String branch = sharedPreferences2.getString("branch", "");
                    appendURL = "/api/production/issue_for_prod/get_all?mode=confirm&docstatus=&branch=" + branch;
                }
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");
                String URL = IPAddress + appendURL;
                System.out.println(URL);
                if (globalJsonObject.toString().equals("{}")) {
                    try {
                        doGetRequest(URL);
                    } catch (IOException | InterruptedException e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GridLayout gridLayout = findViewById(R.id.grid);
                            gridLayout.removeAllViews();
                        }
                    });

                    MyAppendData myAppendData = new MyAppendData(globalJsonObject.toString(),false);
                    myAppendData.execute("");
                }
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                navigateDone();
            }
        });
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
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
//                        e.printStackTrace();
                        countDownLatch.countDown();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GridLayout gridLayout = findViewById(R.id.grid);
                                gridLayout.removeAllViews();
                            }
                        });
                        if (hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Menu Items")) {

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Error Connection \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                            loadOffline("Stock");
                        } else if (hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")) {
                            loadOffline("Item");
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
                        String res = response.body().string();
                        countDownLatch.countDown();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        GridLayout gridLayout = findViewById(R.id.grid);
                                        gridLayout.removeAllViews();
                                    }
                                });
                                MyAppendData myAppendData = new MyAppendData(res, false);
                                myAppendData.execute("");
                            }
                        });
                    }
                });
        countDownLatch.await();
    }

    public void loadOffline(String fromModule){
        Cursor cursor = myDb8.getAllData();
        while (cursor.moveToNext()){
            String module = cursor.getString(4);
            if(module.contains(fromModule)){
                try {
                    if(!module.equals("Item Group")) {
                        globalJsonObject = new JSONObject(cursor.getString(3));
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                if(!module.equals("Item Group")) {
                    MyAppendData myAppendData = new MyAppendData(cursor.getString(3),false);
                    myAppendData.execute("");
                }
            }else{
                System.out.println(module + "/" + cursor.getString(3));
            }
        }
    }

    private class MyAppendData extends AsyncTask<String, Void, String> {
        String sResult = "";
        boolean sItemGroup = false;
        public MyAppendData(String result,boolean isItemGroup){
            sResult = result;
            sItemGroup = isItemGroup;
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(String... strings) {
            return sResult;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                String isSales = Objects.requireNonNull(sharedPreferences.getString("isSales", ""));
                String isProduction = Objects.requireNonNull(sharedPreferences.getString("isProduction", ""));
                JSONObject jsonObjectResponse = new JSONObject();
                List<String> listItems = new ArrayList<String>();
//                if (!globalJsonObject.toString().equals("{}") && !API_ItemInfo.isSubmit) {
//                    jsonObjectResponse = globalJsonObject;
//                } else {
////                jsonObjectReponse = new JSONObject(sResult);
//                    globalJsonObject = new JSONObject(s);
//                    jsonObjectResponse = new JSONObject(s);
//                }

                if(s.substring(0,1).equals("{")){
                    globalJsonObject = new JSONObject(s);
                    jsonObjectResponse = new JSONObject(s);
                    if(!jsonObjectResponse.isNull("success")){
                        if (jsonObjectResponse.getBoolean("success")) {
                            JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
                            JSONObject finalJsonObjectResponse = jsonObjectResponse;
                            System.out.println("hello po " + finalJsonObjectResponse);
                            runOnUiThread(new Runnable() {
                                @SuppressLint({"ResourceType", "SetTextI18n"})
                                @Override
                                public void run() {
                                    try {
                                        GridLayout gridLayout = findViewById(R.id.grid);
                                        gridLayout.removeAllViews();

                                        int iterate = 1;
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                            String item;
                                            String uom = "",uomGroup="";
                                            double price = 0.00;
                                            double stockQuantity = 0.00;
                                            int docEntry1 = 0;
                                            int store_quantity = 0, auditor_quantity = 0, variance_quantity = 0,manager_quantity = 0;
                                            boolean isIssued = (hidden_title.equals("API Production Order List") && (!jsonObject1.isNull("issued")));
                                            String prodStatus = (hidden_title.equals("API Production Order List") ? jsonObject1.isNull("status") ? "" : jsonObject1.getString("status") : "");
                                            switch (hidden_title) {
                                                case "API Item Request":
                                                    item = jsonObject1.getString("item_name");
                                                    break;
                                                case "API Menu Items":
                                                case "API Transfer Item":
                                                case "API Inventory Count":
                                                case "API Inventory Count Variance":
                                                case "API Pull Out Count Variance":
                                                case "API Pull Out Count":
                                                case "API Received Item":
                                                    SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                                    String isManager = sharedPreferences2.getString("isManager", "");
                                                    item = jsonObject1.has("item_code") ? jsonObject1.getString("item_code") : hidden_title.equals("API Pull Out Request Confirmation") ? jsonObject1.getString("reference") : jsonObject1.getString("code");
//                                    JSONObject jsonObjectItem = jsonObject1.getJSONObject("item");
                                                    if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                                                        price = jsonObject1.getDouble("price");
                                                        uom = jsonObject1.getString("uom");
                                                    }
                                                    if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Inventory Count")) {

                                                        stockQuantity = jsonObject1.isNull("quantity") ? 0.00 : jsonObject1.getDouble("quantity");
                                                        uom = jsonObject1.getString("uom");
                                                    } else if (hidden_title.equals("API Pull Out Count") && Integer.parseInt(isManager) <= 0) {
                                                        stockQuantity = jsonObject1.getDouble("quantity");
                                                        uom = jsonObject1.getString("uom");
                                                    }

                                                    if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Inventory Count Variance")) {
                                                        store_quantity = jsonObject1.getInt("sales_count");
                                                        auditor_quantity = jsonObject1.getInt("auditor_count");
                                                        manager_quantity = jsonObject1.getInt("manager_count");
//                                                        variance_quantity = jsonObject1.getInt("variance");
                                                        uom = jsonObject1.getString("uom");
                                                    }

                                                    if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count Variance")) {
                                                        store_quantity = jsonObject1.getInt("sales_count");
                                                        auditor_quantity = jsonObject1.getInt("auditor_count");
                                                        manager_quantity = jsonObject1.getInt("manager_count");
//                                                        variance_quantity = jsonObject1.getInt("variance");
//                                                uom = jsonObject1.getString("uom");
                                                        uom = jsonObject1.getString("uom");
                                                    }

                                                    break;
                                                case "API System Transfer Item":
                                                case "API Issue For Production":
                                                case "API Confirm Issue For Production":
                                                case "API Item Request For Transfer":
                                                case "API Production Order List":
                                                case "API Pull Out Request Confirmation":
                                                case "API Received from Production":
                                                case "API Target For Delivery":
                                                    item = jsonObject1.getString("reference");
                                                    docEntry1 = jsonObject1.getInt("id");
                                                    break;
                                                default:
                                                    item = jsonObject1.getString("docnum");
                                                    docEntry1 = jsonObject1.getInt("docentry");
                                                    break;
                                            }

                                            String supplier = "";
                                            String toWhse = jsonObject1.has("to_whse") ? !jsonObject1.isNull("to_whse") ? jsonObject1.getString("to_whse") : "" : "";

                                            String dateKey = hidden_title.equals("API Target For Delivery") ? "delivery_date" : "production_date";

                                            String delDate = jsonObject1.has(dateKey) ? !jsonObject1.isNull(dateKey) ? jsonObject1.getString(dateKey) : "" : "";
                                            if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 1) {
                                                supplier = jsonObject1.getString("cardcode");
                                            }
                                            if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")){
                                                stockQuantity -= myDb7.getDecreaseQuantity(item);
                                                stockQuantity += myDb7.getIncreaseQuantity(item);
                                                uom = jsonObject1.has("uom") ? jsonObject1.getString("uom") : "";
                                                if(hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Received from Production")){
                                                    uomGroup = jsonObject1.has("uom_group") ? jsonObject1.getString("uom_group") : "0";
                                                }
                                            }
                                            if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")){
                                                if(isSales.equals("1") && !isProduction.equals("1")){
                                                    if(!jsonObject1.getString("item_group").toLowerCase().equals("Raw materials".toLowerCase())){
                                                        if(!spinnerItemGroup.getSelectedItem().toString().equals("All")){
                                                            if(spinnerItemGroup.getSelectedItem().toString().equals(jsonObject1.getString("item_group"))){
                                                                if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                                    if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                        if(stockQuantity > 0){
                                                                            listItems.add(item);
                                                                            loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                            iterate += 1;
                                                                        }
                                                                    }else {
                                                                        if(stockQuantity <= 0){
                                                                            listItems.add(item);
                                                                            loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                            iterate += 1;
                                                                        }
                                                                    }
                                                                }else {
                                                                    listItems.add(item);
                                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                    iterate += 1;
                                                                }
                                                            }
                                                        }else{
                                                            if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                                if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                    if(stockQuantity > 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }else {
                                                                    if(stockQuantity <= 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }
                                                            }else{
                                                                listItems.add(item);
                                                                loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                iterate += 1;
                                                            }
                                                        }
                                                    }
                                                }else if(!isSales.equals("1") && !isProduction.equals("1")){
                                                    if(!jsonObject1.getString("item_group").toLowerCase().equals("Raw materials".toLowerCase())){
                                                        if(!spinnerItemGroup.getSelectedItem().toString().contains("All")){
                                                            if(spinnerItemGroup.getSelectedItem().toString().equals(jsonObject1.getString("item_group"))){
                                                                if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                                    if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                        if(stockQuantity > 0){
                                                                            listItems.add(item);
                                                                            loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        }
                                                                    }else {
                                                                        if(stockQuantity <= 0){
                                                                            listItems.add(item);
                                                                            loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                            iterate += 1;
                                                                        }
                                                                    }
                                                                }else {
                                                                    listItems.add(item);
                                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                    iterate += 1;
                                                                }
                                                            }
                                                        }else{
                                                            if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                                if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                    if(stockQuantity > 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }else{
                                                                    if(stockQuantity <= 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }
                                                            }else{
                                                                listItems.add(item);
                                                                loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                iterate += 1;
                                                            }
                                                        }
                                                    }
                                                }
                                                else{
                                                    if(!spinnerItemGroup.getSelectedItem().toString().equals("All")){
                                                        if(spinnerItemGroup.getSelectedItem().toString().equals(jsonObject1.getString("item_group"))){
                                                            if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                                if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                    if(stockQuantity > 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }else {
                                                                    if(stockQuantity <= 0){
                                                                        listItems.add(item);
                                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                        iterate += 1;
                                                                    }
                                                                }
                                                            }else{
                                                                listItems.add(item);
                                                                loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                iterate += 1;
                                                            }
                                                        }
                                                    }else{
                                                        if(hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")){
                                                            if(spinnerHaveQuantity.getSelectedItem().toString() == "Item w/ balance"){
                                                                if(stockQuantity > 0){
                                                                    listItems.add(item);
                                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                    iterate += 1;
                                                                }
                                                            }else {
                                                                if(stockQuantity <= 0){
                                                                    listItems.add(item);
                                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                                    iterate += 1;
                                                                }
                                                            }
                                                        }else{
                                                            listItems.add(item);
                                                            loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                            iterate += 1;
                                                        }
                                                    }
                                                }
                                            }else{
                                                listItems.add(item);
                                                loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom,isIssued,prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
                                                iterate += 1;
                                            }
                                        }
                                    } catch (Exception ex) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setVisibility(View.GONE);
                                                ex.printStackTrace();
                                                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            String msg = jsonObjectResponse.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                        return;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtSearch.setAdapter(fillItems(listItems));
                            }
                        });
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(),  s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (JSONException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        System.out.println("ssss" + s);
                        Toast.makeText(getBaseContext(), "Front-end Error: \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    public void loadUIItems(String item, double price, double stockQuantity, int docEntry1, String supplier, int store_quantity, int auditor_quantity, int variance_quantity, String uom, boolean isIssued, String prodStatus,String uomGroup, int iterate,double manager_quantity,String toWhse,String delDate){
        if (!txtSearch.getText().toString().trim().isEmpty()) {
            if (txtSearch.getText().toString().trim().contains(item)) {
                uiItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity,uom,isIssued, prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
            }
        }else{



            uiItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity,uom,isIssued, prodStatus,uomGroup,iterate,manager_quantity,toWhse,delDate);
        }
    }

    @SuppressLint("SetTextI18n")
    public void uiItems(String item, Double price, Double stockQuantity, int docEntry1, String supplier, int store_quantity, int auditor_quantity, int variance_quantity,String uom,boolean isIssued, String prodStatus,String uomGroup,int iterate, double manager_quantity,String toWhse,String delDate) {
        GridLayout gridLayout = findViewById(R.id.grid);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int boxWidth = 0,boxHeight = 0;
        if(width <= 720){
            boxHeight = 230;
            boxWidth = 190;
        }else{
            boxHeight = 320;
            boxWidth = 280;
        }

        int cardViewMarginLeft = 0;
        if (iterate % 3 == 0) {
            cardViewMarginLeft = 5;
        }
        CardView cardView = new CardView(APIReceived.this);
        LinearLayout.LayoutParams layoutParamsCv = new LinearLayout.LayoutParams(boxWidth, boxHeight);
        layoutParamsCv.setMargins(cardViewMarginLeft, 5, 5, 5);
        cardView.setLayoutParams(layoutParamsCv);
        cardView.setRadius(12);
        cardView.setCardElevation(5);

        cardView.setVisibility(View.VISIBLE);
        gridLayout.addView(cardView);
        final LinearLayout linearLayout = new LinearLayout(getBaseContext());
        linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
        LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 5f);
        linearLayout.setLayoutParams(layoutParamsLinear);
        linearLayout.setTag("Linear" + item);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.setVisibility(View.VISIBLE);

        String finalItem = item;
        int finalDocEntry = docEntry1;
        double finalPrice = price;
        double finalStockQuantity = stockQuantity;
        String finalSupplier = supplier;

        linearLayout.setOnClickListener(view -> {
            if (hidden_title.equals("API Menu Items")) {
                if (myDb.checkItem(item)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }

            } else if (hidden_title.equals("API Received Item")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            } else if (hidden_title.equals("API Transfer Item")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
//                anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
            } else if (hidden_title.equals("API Item Request")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else if (hidden_title.equals("API Inventory Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else if (hidden_title.equals("API Inventory Count Variance")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else if (hidden_title.equals("API Pull Out Count Variance")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else if (hidden_title.equals("API Received from Production")) {
                if (myDb4.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else if (hidden_title.equals("API Pull Out Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
                }
            }
            else {
                anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,manager_quantity);
            }
        });

        if(hidden_title.equals("API Target For Delivery") || hidden_title.equals("API Received from Production")){
            linearLayout.setLongClickable(true);
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    myToTest my = new myToTest(docEntry1,item,delDate);
                    my.execute("");
                    return true;
                }
            });
        }


        cardView.addView(linearLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 0, 20, 0);
        LinearLayout.LayoutParams layoutParamsItemLeft = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsItemLeft.setMargins(20, -80, 0, 10);

        TextView txtItemName = new TextView(getBaseContext());
        txtItemName.setTag(item);
        txtItemName.setText(cutWord(item, 35));
        txtItemName.setTextColor(Color.rgb(0, 0, 0));
        txtItemName.setLayoutParams(layoutParams);
        txtItemName.setTextSize(15);
        txtItemName.setVisibility(View.VISIBLE);
        linearLayout.addView(txtItemName);

        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Inventory Count Variance") || hidden_title.equals("API Pull Out Count") || hidden_title.equals("API Received from Production") || hidden_title.equals("API Pull Out Count Variance") || hidden_title.equals("API Target For Delivery")) {
            TextView txtItemLeft = new TextView(getBaseContext());

            if(hidden_title.equals("API Target For Delivery")){
                layoutParamsItemLeft.setMargins(20, -120, 0, 10);
            }

            txtItemLeft.setLayoutParams(layoutParamsItemLeft);
            txtItemLeft.setTextColor(Color.rgb(0, 0, 0));
            txtItemLeft.setTextSize(13);
            txtItemLeft.setVisibility(View.VISIBLE);
            if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                txtItemLeft.setText(df.format(stockQuantity) + " available");
                if (stockQuantity <= 0) {
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                } else if (stockQuantity <= 10) {
                    txtItemLeft.setTextColor(Color.rgb(247, 154, 22));
                } else if (stockQuantity > 11) {
                    txtItemLeft.setTextColor(Color.rgb(30, 203, 6));
                }
            }
            if(hidden_title.equals("API Target For Delivery")){
                String nToWhse = "<font color='#04691d'>To Whse: " + toWhse + "</font><br>";
                String nDelDate = "<font color='#2573fa'>Del. Date: " + delDate + "</font>";
                txtItemLeft.setText(Html.fromHtml(nToWhse+ nDelDate));
                txtItemLeft.setTextSize(11);
//              txtItemLeft.setTextColor(Color.BLUE);
            }
            if(hidden_title.equals("API Received from Production")){
                String nToWhse = "<font color='#04691d'>To Whse: " + toWhse + "</font>";
                String nDelDate = "<font color='#2573fa'>Prod. Date: " + delDate + "</font>";
                txtItemLeft.setText(Html.fromHtml(nDelDate));
                txtItemLeft.setTextSize(11);
//                txtItemLeft.setTextColor(Color.BLUE);
            }
//            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
//            String isManager = sharedPreferences2.getString("isManager", "");
//            if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Inventory Count Variance")) {
//                txtItemLeft.setText(df.format(variance_quantity) + " variance");
//                if (variance_quantity < 0) {
//                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
//                } else {
//                    txtItemLeft.setTextColor(Color.rgb(6, 188, 212));
//                }
//            }
//            if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count")) {
//                txtItemLeft.setText(df.format(variance_quantity) + " variance");
//                txtItemLeft.setTextColor(Color.WHITE);
//            }

//            if (stockQuantity <= 0 && hidden_title.equals("API Inventory Count Variance")) {
//                linearLayout.setBackgroundColor(Color.rgb(94, 94, 94));
//                txtItemName.setTextColor(Color.rgb(255, 255, 255));
//                txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
//            }
//            if (stockQuantity <= 0 && hidden_title.equals("API Pull Out Count") && Integer.parseInt(isManager) <= 0) {
//                linearLayout.setBackgroundColor(Color.rgb(94, 94, 94));
//                txtItemName.setTextColor(Color.rgb(255, 255, 255));
//                txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
//            }

            if (hidden_title.equals("API Received Item")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Received from Production")) {
                if (myDb4.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Transfer Item")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
//            else if (hidden_title.equals("API Received from Production")) {
//                if (myDb4.checkItem(item, hidden_title  )) {
//                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
//                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
//                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
//                }
//            }
            else if (hidden_title.equals("API Item Request")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                }
            } else if (hidden_title.equals("API Menu Items")) {
                if (myDb.checkItem(item)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            } else if (hidden_title.equals("API Inventory Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Inventory Count Variance")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Pull Out Count Variance")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Pull Out Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            linearLayout.addView(txtItemLeft);
        }

        if(hidden_title.equals("API Production Order List")){
            System.out.println("is issued: " + isIssued + "\n status: " + prodStatus);
            LinearLayout.LayoutParams layoutParamsItemLeft2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsItemLeft2.setMargins(20,  -100, 0, 10);
            TextView txtItemLeft2 = new TextView(APIReceived.this);
            txtItemLeft2.setLayoutParams(layoutParamsItemLeft2);
            txtItemLeft2.setTextSize(13);
            txtItemLeft2.setVisibility(View.VISIBLE);
            txtItemLeft2.setText(isIssued ? "✓ \n" + prodStatus : prodStatus);
            txtItemLeft2.setTextColor(Color.parseColor("#34A853"));
            linearLayout.addView(txtItemLeft2);
        }
    }

    public void anotherFunction(String finalItem, double finalPrice, Integer finalDocEntry, String finalSupplier,double quantity,int store_quantity, int auditor_quantity, int variance_quantity,String uom, String uomGroup, double manager_quantity){
        if (hidden_title.equals("API Received Item") || hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Inventory Count Variance") || hidden_title.equals("API Pull Out Count")|| hidden_title.equals("API Pull Out Count Variance")) {
            Intent intent;
            intent = new Intent(getBaseContext(), API_ItemInfo.class);
            intent.putExtra("title", title);
            intent.putExtra("hiddenTitle", hidden_title);
            intent.putExtra("item", finalItem);
            intent.putExtra("quantity", quantity);
            intent.putExtra("uom", uom);
            intent.putExtra("uomGroup", uomGroup);
            if (hidden_title.equals("API Menu Items")) {
                intent.putExtra("price", finalPrice);
            }

            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
            String isManager = sharedPreferences2.getString("isManager", "");
            if(Integer.parseInt(isManager) > 0 && hidden_title.equals("API Inventory Count Variance")){
                intent.putExtra("store_quantity", store_quantity);
                intent.putExtra("auditor_quantity", auditor_quantity);
                intent.putExtra("manager_quantity",manager_quantity);
                intent.putExtra("variance_quantity",variance_quantity);
            }
            if(Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count Variance")){
                intent.putExtra("store_quantity", store_quantity);
                intent.putExtra("auditor_quantity", auditor_quantity);
                intent.putExtra("manager_quantity", manager_quantity);
                intent.putExtra("variance_quantity",variance_quantity);
            }
            startActivity(intent);
        }else if(hidden_title.equals("API Production Order List")) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
            myDialog.setCancelable(true);
            myDialog.setTitle(finalItem);
//            System.out.println("ID: " + finalDocEntry);
            myDialog.setPositiveButton("View Item", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    insertProductionOrderItems(finalDocEntry, finalItem);
                }
            });
            myDialog.setNegativeButton("Close Transaction", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                    builder.setMessage("Are you sure want to close?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    JSONObject jsonObject = new JSONObject();
                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                                    String IPaddress = sharedPreferences2.getString("IPAddress", "");
                                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                                    String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                                    okhttp3.Request request = new okhttp3.Request.Builder()
                                            .url(IPaddress + "/api/production/order/close/" + finalDocEntry)
                                            .method("PUT", body)
                                            .addHeader("Authorization", "Bearer " + token)
                                            .addHeader("Content-Type", "application/json")
                                            .build();
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(Call call, okhttp3.Response response) {
                                            String result = "";
                                            try {
                                                result = response.body().string();
//                            System.out.println(result);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            String finalResult = result;
                                            APIReceived.this.runOnUiThread(() -> {
                                                try {
                                                    JSONObject jj = new JSONObject(finalResult);
                                                    boolean isSuccess = jj.getBoolean("success");
                                                    if (isSuccess) {
                                                        Toast.makeText(getBaseContext(),  jj.getString("message"), Toast.LENGTH_SHORT).show();
                                                        Intent intent;
                                                        intent = new Intent(getBaseContext(), APIReceived.class);
                                                        intent.putExtra("title", title);
                                                        intent.putExtra("hiddenTitle", hidden_title);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        String msg = jj.getString("message");
                                                        if (msg.equals("Token is invalid")) {
                                                            final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                                            builder.setCancelable(false);
                                                            builder.setMessage("Your session is expired. Please login again.");
                                                            builder.setPositiveButton("OK", (dialog, which) -> {
                                                                pc.loggedOut(APIReceived.this);
                                                                pc.removeToken(APIReceived.this);
                                                                startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                                                finish();
                                                                dialog.dismiss();
                                                            });
                                                            builder.show();
                                                        } else {
                                                            Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        }
                                    });
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
            });
            myDialog.show();
        }
        else if (hidden_title.equals("API Received from SAP") || hidden_title.equals("API System Transfer Item") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Confirm Issue For Production") || hidden_title.equals("API Item Request For Transfer") || hidden_title.equals("API Pull Out Request Confirmation") || hidden_title.equals("API Received from Production") || hidden_title.equals("API Target For Delivery")) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
            myDialog.setCancelable(false);
            myDialog.setTitle("Confirmation");
//            System.out.println("ID: " + finalDocEntry);
            myDialog.setMessage("Are you sure you want to select '" + finalItem + "'?");
            myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if(hidden_title.equals("API Received from SAP")){
                        insertSAPItems(finalDocEntry, finalSupplier);
                    }else if(hidden_title.equals("API Issue For Production") || hidden_title.equals("API Confirm Issue For Production")){
                        insertIssueProduction(finalDocEntry, finalItem);
                    }
                    else if(hidden_title.equals("API Received from Production")){
//                        System.out.println("hidden: " + hidden_title);
                        insertReceivedProduction(finalDocEntry, finalItem);
                    }
                    else if(hidden_title.equals("API Item Request For Transfer")){
//                        System.out.println("hidden: " + hidden_title);
                        insertReceivedItemRequest(finalDocEntry, finalItem);
                    }
                    else if(hidden_title.equals("API Target For Delivery")){
//                        System.out.println("hidden: " + hidden_title);
                        insertTargetForDelivery(finalDocEntry, finalItem);
                    }
                    else{
                        insertSystemTransfer(finalDocEntry, finalItem);
                    }
                }
            });
            myDialog.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
            myDialog.show();
        }
    }

    public void insertProductionOrderItems(int id, String referenceNumber){
        String appendURL= "/api/production/order/details/" + id + "?mode=receive";
        System.out.println("/api/production/order/details/"+ id  + "?mode=receive");
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity,variance=0.00;

                                fromBranch = jsonObject.getString("whsecode");
                                itemName = jsonObject.getString("item_code");
                                quantity = jsonObject.getDouble("planned_qty");
                                variance = jsonObject.getDouble("variance");
                                toBranch = jsonObject.getString("whsecode");



                                uom = jsonObject.getString("uom");
                                int itemID = jsonObject.getInt("id");
                                int objtype = jsonObject.has("objtype") ? !jsonObject.isNull("objtype") ? jsonObject.getInt("objtype") : 0 : 0;
                                double int_quantity = jsonObject.getDouble("planned_qty");
                                int isClosed_int = jsonObject.isNull("close") ? 0 : jsonObject.getBoolean("close") ? 1 : 0;
                                double int_received_quantity = jsonObject.isNull("received_qty") ? 0 : jsonObject.getDouble("received_qty");
                                System.out.println("planned " + quantity + "/receive " +int_received_quantity + "/variance " + variance);
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,itemID,objtype,0,response.toString(),variance);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertReceivedProduction(int id, String referenceNumber){
        String appendURL= "/api/production/order/details/" + id + "?mode=receive";
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        System.out.println(response);
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity;

                                fromBranch = jsonObject.getString("whsecode");
                                itemName = jsonObject.getString("item_code");
                                toBranch = jsonObject.getString("whsecode");
                                uom = jsonObject.getString("uom");

                                double int_quantity = jsonObject.getDouble("planned_qty");
                                double int_received_quantity = jsonObject.isNull("received_qty") ? 0 : jsonObject.getDouble("received_qty");
                                double variance = jsonObject.isNull("variance") ? 0 : jsonObject.getDouble("variance");
                                System.out.println("planned " + int_quantity + "/receive " +int_received_quantity + "/variance " + variance);
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, int_quantity, 0.00, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,0,0,0,response.toString(),variance);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertTargetForDelivery(int id, String referenceNumber){
        String appendURL= "/api/forecast/get_for_delivery/details/" + id + "?mode=for_delivery";
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        System.out.println(response);
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;

                                fromBranch = jsonObject.getString("from_whse");
                                itemName = jsonObject.getString("item_code");
                                toBranch = jsonObject.getString("to_whse");
                                uom = jsonObject.getString("uom");

                                double finalQty = jsonObject.has("balance") ? jsonObject.isNull("balance") ? 0 : jsonObject.getDouble("balance"): 0;
//                                double quantity = jsonObject.getDouble("quantity");

                                int idd = jsonObject.getInt("id");

                                int objType = jsonObject.getInt("objtype");

                                System.out.println("objtype " + objType);

                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, finalQty, 0.00, 0, toBranch, id,hidden_title,0,uom,0,idd,objType,0,response.toString(),0.00);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertReceivedItemRequest(int id, String referenceNumber){
        String appendURL= "/api/inv/item_request/details/" + id;
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject jsonObjectData;
                            jsonObjectData = response.getJSONObject("data");
                            System.out.println("ditooooooooooooo\n" + jsonObjectData);
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            JSONArray jsonArray = jsonObjectData.getJSONArray("request_rows");
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity;
                                int item_id = jsonObject.isNull("id") ? 0 : jsonObject.getInt("id");
                                int obj_type = jsonObject.isNull("objtype") ? 0 : jsonObject.getInt("objtype");
                                fromBranch = jsonObject.getString("from_whse");
                                itemName = jsonObject.getString("item_code");
                                quantity = jsonObject.getDouble("quantity");
                                toBranch = jsonObject.getString("to_whse");
                                uom = jsonObject.getString("uom");
                                double int_quantity = jsonObject.getDouble("quantity");
                                int int_received_quantity = jsonObject.isNull("deliverqty") ? 0 : jsonObject.getInt("deliverqty");
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,item_id,obj_type,0,response.toString(),0.00);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), APIReceived.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertIssueProduction(int id, String reference){
        String appendURL= (hidden_title.equals("API Issue For Production") ? "/api/production/item_to_issue/get_all/" : "/api/production/issue_for_prod/details/") + id + (hidden_title.equals("API Issue For Production") ? "" : "?mode=confirm");
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPAddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String currentDate = sdf.format(new Date());
                            boolean isSuccess = myDb9.insertData(appendURL,"GET", "Issue For Production", response.toString(),currentDate);
                            if(isSuccess){
                                Toast.makeText(getBaseContext(), reference + " added" , Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), API_IssueProductionItems.class);
                                intent.putExtra("title", title);
                                intent.putExtra("id", id);
                                intent.putExtra("reference", reference);
                                intent.putExtra("hiddenTitle", hidden_title);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public String cutWord(String value, int limit){
        String result;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }

    public void navigateDone() {
        if (hidden_title.equals("API Received Item") && spinnerType.getSelectedItem().toString() == "Select Type") {
            Toast.makeText(getBaseContext(), "Please select Type", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent;
        intent = new Intent(getBaseContext(), (hidden_title.equals("API Menu Items") ? ShoppingCart.class : API_SelectedItems.class));
        intent.putExtra("title", title);
        if(hidden_title.equals("API Received Item") && spinnerType.getSelectedItem().toString() != "Select Type" && !spinnerType.getSelectedItem().toString().isEmpty()){
            intent.putExtra("type", spinnerType.getSelectedItem().toString());
        }
        intent.putExtra("hiddenTitle", hidden_title);
        startActivity(intent);
    }

    public void confirmPullOut(int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
        builder.setMessage("Are you sure want to confirm?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                        String IPAddress = sharedPreferences2.getString("IPAddress", "");

                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

                        JSONObject jsonObject = new JSONObject();
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url(IPAddress + "/api/pullout/confirm/" + id)
                                .method("PUT", body)
                                .addHeader("Authorization", "Bearer " + token)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        client.newCall(request).enqueue(new Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try{
                                    String s = response.body().string();
                                    if(s.substring(0,1).equals("{")){
                                        JSONObject jsonObject = new JSONObject(s);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Toast.makeText(getBaseContext(),jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                                    if(jsonObject.getBoolean("success")){
                                                        btnBack.performClick();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getBaseContext(),s, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }catch (Exception ex){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
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