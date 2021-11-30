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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.example.atlanticbakery.Interface.NavigationManager;
import com.example.atlanticbakery.Helper.FragmentNavigationManager_API_SelectedItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API_SelectedItems extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    DatabaseHelper4 myDb4;
    DatabaseHelper3 myDb3;
    DatabaseHelper7 myDb7;
    DatabaseHelper8 myDb8;
    long mLastClickTime = 0;
    Button btnProceed,btnBack;
    DecimalFormat df = new DecimalFormat("#,##0.00");
    String title,hiddenTitle;
    private OkHttpClient client;
    private RequestQueue mQueue;

    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
//    private String[] items;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;


    DatabaseHelper myDb;
    TextView lblSelectedDate;

    Menu menu;

    String gBranch = "";

    CountDownTimer countDownTimer = null;
    private BadgeDrawerArrowDrawable badgeDrawable;
    notification_class notifc = new notification_class(this);
    int sapCount = 0, transferCount = 0, offlineCount = 0, totalCount = 0;
    int isAgent = 0,gI = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i__selected_items);

        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);

        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);
        myDb7 = new DatabaseHelper7(this);
        btnProceed = findViewById(R.id.btnProceed);
        btnBack = findViewById(R.id.btnBack);
        mQueue = Volley.newRequestQueue(this);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        client = builder.build();

        hiddenTitle = getIntent().getStringExtra("hiddenTitle");

        if (hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request")) {
            hmReturnBranches();
            hmReturnBranches();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView) findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_SelectedItems.getmInstance(this);

        title = getIntent().getStringExtra("title");


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

        btnProceed.setOnClickListener(view -> {
            gI++;
            btnProceed.setEnabled(false);
            String hashedID =   randomString();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (gI == 1) {
                        btnProceed.setEnabled(true);
                        final AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SelectedItems.this);
                        myDialog.setCancelable(false);
                        LinearLayout layout = new LinearLayout(getBaseContext());
                        layout.setPadding(40, 40, 40, 40);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        TextView lblFromSelectedBranch = new TextView(getBaseContext());
                        TextView lblToSelectedBranch = new TextView(getBaseContext());
                        EditText txtSAPNumber = new EditText(getBaseContext());
                        EditText txtSupplier = new EditText(getBaseContext());
                        LinearLayout.LayoutParams layoutParamsBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        if (hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request") || hiddenTitle.equals("API Target For Delivery")) {
                            String type = getIntent().getStringExtra("type");

                            if (hiddenTitle.equals("API Received Item") && type.equals("SAPPO")) {
                                TextView lblSupplier = new TextView(getBaseContext());
                                lblSupplier.setText("*Supplier: ");
                                lblSupplier.setTextColor(Color.rgb(0, 0, 0));
                                lblSupplier.setTextSize(15);
                                lblSupplier.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                lblSupplier.setLayoutParams(layoutParamsBranch);
                                layout.addView(lblSupplier);

                                txtSupplier.setTextSize(15);
                                txtSupplier.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                txtSupplier.setLayoutParams(layoutParamsBranch);
                                layout.addView(txtSupplier);
                            } else {
                                TextView lblFromBranch = new TextView(getBaseContext());
                                lblFromBranch.setText((hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Item Request")) ? "*From Warehouse:" : "*To Warehouse:");
                                lblFromBranch.setTextColor(Color.rgb(0, 0, 0));
                                lblFromBranch.setTextSize(15);
                                lblFromBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                if(!hiddenTitle.equals("API Target For Delivery")) {
                                    layout.addView(lblFromBranch);
                                }

                                LinearLayout layoutFromBranch = new LinearLayout(getBaseContext());
                                LinearLayout.LayoutParams layoutParamsFromBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParamsFromBranch.setMargins(20, 0, 0, 20);
                                layoutFromBranch.setLayoutParams(layoutParamsFromBranch);
                                layoutFromBranch.setOrientation(LinearLayout.HORIZONTAL);

                                LinearLayout.LayoutParams layoutParamsBranch2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                LinearLayout.LayoutParams layoutParamsBranch3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParamsBranch3.setMargins(10, 0, 0, 0);

                                lblFromSelectedBranch.setText("N/A");
                                lblFromSelectedBranch.setTextColor(Color.rgb(0, 0, 0));
                                lblFromSelectedBranch.setTextSize(15);
                                lblFromSelectedBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                lblFromSelectedBranch.setLayoutParams(layoutParamsBranch2);
                                layoutFromBranch.addView(lblFromSelectedBranch);

                                TextView btnFromSelectBranch = new TextView(getBaseContext());
                                btnFromSelectBranch.setText("...");
                                btnFromSelectBranch.setPadding(20, 10, 20, 10);
                                btnFromSelectBranch.setBackgroundColor(Color.BLACK);
                                btnFromSelectBranch.setTextColor(Color.WHITE);
                                btnFromSelectBranch.setTextSize(15);
                                btnFromSelectBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                btnFromSelectBranch.setLayoutParams(layoutParamsBranch3);

                                btnFromSelectBranch.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myWarehouse myWarehouse = new myWarehouse(lblFromSelectedBranch, false);
                                        myWarehouse.execute("");
                                    }
                                });
                                layoutFromBranch.addView(btnFromSelectBranch);
                                if(!hiddenTitle.equals("API Target For Delivery")){
                                    layout.addView(layoutFromBranch);
                                }

//                    TextView lblToBranch = new TextView(getBaseContext());
//                    lblToBranch.setText((hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Item Request")) ? "*To Warehouse:" : "*From Warehouse:");
//                    lblToBranch.setTextColor(Color.rgb(0,0,0));
//                    lblToBranch.setTextSize(15);
//                    lblToBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
//
//                    LinearLayout layoutToBranch = new LinearLayout(getBaseContext());
//                    LinearLayout.LayoutParams layoutParamsToBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    layoutParamsToBranch.setMargins(20,0,0,20);
//                    layoutToBranch.setLayoutParams(layoutParamsFromBranch);
//                    layoutToBranch.setOrientation(LinearLayout.HORIZONTAL);
//
//                    lblToSelectedBranch.setText("N/A");
//                    lblToSelectedBranch.setTextColor(Color.rgb(0,0,0));
//                    lblToSelectedBranch.setTextSize(15);
//                    lblToSelectedBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                    lblToSelectedBranch.setLayoutParams(layoutParamsBranch2);
//                    layoutToBranch.addView(lblToSelectedBranch);
//
//                    TextView btnToSelectBranch = new TextView(getBaseContext());
//                    btnToSelectBranch.setText("...");
//                    btnToSelectBranch.setPadding(20,10,20,10);
//                    btnToSelectBranch.setBackgroundColor(Color.BLACK);
//                    btnToSelectBranch.setTextColor(Color.WHITE);
//                    btnToSelectBranch.setTextSize(15);
//                    btnToSelectBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                    btnToSelectBranch.setLayoutParams(layoutParamsBranch3);
//
//                    btnToSelectBranch.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            showWarehouses(lblToSelectedBranch);
//                        }
//                    });
//                    layoutToBranch.addView(btnToSelectBranch);
//                    layout.addView(lblToBranch);
//                    layout.addView(layoutToBranch);

                            }

                            if (hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item")) {
                                TextView lblSAPNumber = new TextView(getBaseContext());
                                lblSAPNumber.setText("SAP #:");
                                lblSAPNumber.setTextColor(Color.rgb(0, 0, 0));
                                lblSAPNumber.setTextSize(15);
                                lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                lblSAPNumber.setLayoutParams(layoutParamsBranch);
                                layout.addView(lblSAPNumber);

                                txtSAPNumber.setTextSize(15);
                                txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                                txtSAPNumber.setLayoutParams(layoutParamsBranch);
                                layout.addView(txtSAPNumber);
                            }
                        }

                        if (hiddenTitle.equals("API Item Request")) {
                            Button btnPickDate = new Button(getBaseContext());
                            LinearLayout.LayoutParams layoutParamsBtnDate = new LinearLayout.LayoutParams(300, 100);
                            layoutParamsBtnDate.setMargins(0, 0, 0, 20);
                            btnPickDate.setText("Pick Due Date");
                            btnPickDate.setLayoutParams(layoutParamsBtnDate);
                            btnPickDate.setBackgroundResource(R.color.colorPrimary);
                            btnPickDate.setTextColor(Color.WHITE);
                            btnPickDate.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            btnBack.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                            btnPickDate.setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onClick(View view) {
                                    showDatePickerDialog();
                                }
                            });
                            layout.addView(btnPickDate);

                            lblSelectedDate = new TextView(getBaseContext());
                            LinearLayout.LayoutParams layoutParamsLblSelectedDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParamsLblSelectedDate.setMargins(0, 0, 0, 20);
                            lblSelectedDate.setLayoutParams(layoutParamsLblSelectedDate);
                            lblSelectedDate.setTextColor(Color.rgb(0, 0, 0));
                            lblSelectedDate.setText("N/A");
                            lblSelectedDate.setTextSize(15);
                            lblSelectedDate.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            layout.addView(lblSelectedDate);
                        }
                        if (hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer") || hiddenTitle.equals("API Target For Delivery")) {
                            TextView lblSAPNumber = new TextView(getBaseContext());
                            lblSAPNumber.setText("SAP #:");
                            lblSAPNumber.setTextColor(Color.rgb(0, 0, 0));
                            lblSAPNumber.setTextSize(15);
                            lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblSAPNumber.setLayoutParams(layoutParamsBranch);
                            layout.addView(lblSAPNumber);

                            txtSAPNumber.setTextSize(15);
                            txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                            txtSAPNumber.setLayoutParams(layoutParamsBranch);
                            layout.addView(txtSAPNumber);
                        }

                        TextView lblRemarks = new TextView(getBaseContext());
                        lblRemarks.setTextColor(Color.rgb(0, 0, 0));
                        lblRemarks.setText("*Remarks:");
                        lblRemarks.setTextSize(15);
                        lblRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        layout.addView(lblRemarks);

                        EditText txtRemarks = new EditText(API_SelectedItems.this);
                        txtRemarks.setTextColor(Color.rgb(0, 0, 0));
                        txtRemarks.setTextSize(15);
                        txtRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        layout.addView(txtRemarks);

                        String supplier = "";
                        Cursor cursor = myDb3.getAllData(hiddenTitle);
                        if (cursor != null) {
                            if (cursor.moveToNext()) {
                                supplier = cursor.getString(2);
                            }
                        }

                        String finalSupplier = supplier;
                        myDialog.setPositiveButton("Submit", null);
                        myDialog.setNegativeButton("Cancel", null);
                        myDialog.setView(layout);
                        AlertDialog show2 = myDialog.show();
                        Button btnSubmit = show2.getButton(DialogInterface.BUTTON_POSITIVE);
                        Button btnCancel = show2.getButton(DialogInterface.BUTTON_NEGATIVE);

                        btnSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("second hashed id: " + hashedID);
                                gI++;
                                btnProceed.setEnabled(false);
                                btnSubmit.setEnabled(false);
                                btnCancel.setEnabled(false);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (gI == 1) {
                                            btnProceed.setEnabled(true);
                                            btnSubmit.setEnabled(true);
                                            btnCancel.setEnabled(true);
                                            String type = getIntent().getStringExtra("type");
                                            if (lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Received Item") && type.equals("SAPIT")) {
                                                Toast.makeText(getBaseContext(), "Please select from Warehouse", Toast.LENGTH_SHORT).show();
                                            } else if (lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Transfer Item")) {
                                                Toast.makeText(getBaseContext(), "Please select to Warehouse", Toast.LENGTH_SHORT).show();
                                            } else if (lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Item Request")) {
                                                Toast.makeText(getBaseContext(), "Please select to Warehouse", Toast.LENGTH_SHORT).show();
                                            } else if (txtRemarks.getText().toString().isEmpty()) {
                                                Toast.makeText(getBaseContext(), "Remarks field is empty", Toast.LENGTH_SHORT).show();
                                            } else if (hiddenTitle.equals("API Item Request") && lblSelectedDate.getText().toString() == "N/A") {
                                                Toast.makeText(getBaseContext(), "Please select Due Date", Toast.LENGTH_SHORT).show();

                                            } else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                                builder.setMessage("Are you sure want to submit?")
                                                        .setCancelable(false);
                                                builder.setPositiveButton("Yes", null);
                                                builder.setNegativeButton("Cancel", null);
                                                AlertDialog show = builder.show();
                                                Button btn = show.getButton(DialogInterface.BUTTON_POSITIVE);
                                                Button btn2 = show.getButton(DialogInterface.BUTTON_NEGATIVE);
                                                btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        System.out.println("third hashed id: " + hashedID);
                                                        gI++;
                                                        btnProceed.setEnabled(false);
                                                        btn.setEnabled(false);
                                                        btn2.setEnabled(false);
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (gI == 1) {
                                                                    btn.setEnabled(true);
                                                                    btn2.setEnabled(true);
                                                                    btnProceed.setEnabled(true);
                                                                    String whseCode = "";
                                                                    if (hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request")) {
//                                                                        if (hiddenTitle.equals("API Received Item") && type.equals("SAPIT")) {
//                                                                            whseCode = findWarehouseCode(lblFromSelectedBranch.getText().toString());
//                                                                        } else if (!hiddenTitle.equals("API Received Item")) {
//                                                                            whseCode = findWarehouseCode(lblFromSelectedBranch.getText().toString());
//                                                                        }
                                                                        whseCode = findWarehouseCode(lblFromSelectedBranch.getText().toString());
                                                                    }

                                                                    if (hiddenTitle.equals("API Received Item")) {

                                                                        if(myDb4.checkItems(title)){
                                                                            AlertDialog.Builder builderErr = new AlertDialog.Builder(API_SelectedItems.this);
                                                                            builderErr.setMessage("There are items that are identical! Please remove the duplicate item")
                                                                                    .setCancelable(false)
                                                                                    .setTitle("Validation");
                                                                            builderErr.setPositiveButton("OK", null);
                                                                            AlertDialog showErr = builderErr.show();
                                                                            Button btn = showErr.getButton(DialogInterface.BUTTON_POSITIVE);
                                                                            btn.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View view) {
                                                                                    showErr.dismiss();
                                                                                    show.dismiss();
                                                                                }
                                                                            });

                                                                        }else{
                                                                            int sapNumber = (txtSAPNumber.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtSAPNumber.getText().toString()));
                                                                            btnProceed.setText("Waiting...");
                                                                            btnProceed.setEnabled(false);
//                                                                            apiSaveManualReceived(whseCode, txtRemarks.getText().toString(), sapNumber, type, txtSupplier.getText().toString(),hashedID,show,show2);
                                                                            ReceiveItem rec = new ReceiveItem(whseCode, txtRemarks.getText().toString(), sapNumber, type, txtSupplier.getText().toString(), hashedID, show, show2);
                                                                            rec.execute("");

                                                                        }
                                                                    } else if (hiddenTitle.equals("API Transfer Item")) {

                                                                        if(myDb4.checkItems(title)){
                                                                            AlertDialog.Builder builderErr = new AlertDialog.Builder(API_SelectedItems.this);
                                                                            builderErr.setMessage("There are items that are identical! Please remove the duplicate item")
                                                                                    .setCancelable(false)
                                                                                    .setTitle("Validation");
                                                                            builderErr.setPositiveButton("OK", null);
                                                                            AlertDialog showErr = builderErr.show();
                                                                            Button btn = showErr.getButton(DialogInterface.BUTTON_POSITIVE);
                                                                            btn.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View view) {
                                                                                    showErr.dismiss();
                                                                                    show.dismiss();
                                                                                }
                                                                            });
                                                                            
                                                                        }else{
                                                                            btnProceed.setText("Waiting...");
                                                                            btnProceed.setEnabled(false);
//                                                                            apiSaveTransferItem(whseCode, txtRemarks.getText().toString(),hashedID);
                                                                            TransferItem trans = new TransferItem(whseCode,txtRemarks.getText().toString(), hashedID);
                                                                            trans.execute("");
                                                                        }


                                                                    } else if (hiddenTitle.equals("API Item Request")) {
                                                                        btnProceed.setText("Waiting...");
                                                                        btnProceed.setEnabled(false);
                                                                        apiItemRequest(txtRemarks.getText().toString(), whseCode,hashedID);
                                                                    } else if (hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count Variance")) {
                                                                        btnProceed.setEnabled(false);
                                                                        apiSaveInventoryCount(txtRemarks.getText().toString(),hashedID);
                                                                    } else if (hiddenTitle.equals("API Pull Out Request Confirmation")) {
                                                                        try {
                                                                            apiSavePullOutConfirm(txtRemarks.getText().toString(),hashedID);
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    } else if (hiddenTitle.equals("API Received from Production")) {
                                                                        btnProceed.setText("Waiting...");
                                                                        btnProceed.setEnabled(false);
                                                                        apiSaveReceivedProduction(txtRemarks.getText().toString(), txtSAPNumber.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtSAPNumber.getText().toString()),hashedID,show, show2);
                                                                    }
                                                                    else if (hiddenTitle.equals("API Item Request For Transfer")) {
                                                                        btnProceed.setText("Waiting...");
                                                                        btnProceed.setEnabled(false);
                                                                        apiSaveItemRequestForTransfer(txtSAPNumber.getText().toString(), txtRemarks.getText().toString().trim(),hashedID,show);
                                                                    }
                                                                     else if (hiddenTitle.equals("API Target For Delivery")) {
                                                                        btnProceed.setText("Waiting...");
                                                                        btnProceed.setEnabled(false);
                                                                        apiSaveItemRequestForTransfer(txtSAPNumber.getText().toString(), txtRemarks.getText().toString().trim(),hashedID,show);
                                                                    }
                                                                    else {
                                                                        btnProceed.setText("Waiting...");
                                                                        btnProceed.setEnabled(false);
//                                apiSaveDataRec(finalSupplier, txtRemarks.getText().toString());
                                                                        apiSaveDataReceived api = new apiSaveDataReceived(finalSupplier, txtRemarks.getText().toString(), show,hashedID,show2);
                                                                        api.execute("");
                                                                    }
                                                                } else {
                                                                    Toast.makeText(getBaseContext(), "You pressed too many times! Please click the button Ok once only", Toast.LENGTH_SHORT).show();
                                                                }
                                                                gI = 0;
                                                            }
                                                        }, 500);
                                                    }
                                                });
                                                btn2.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        btnProceed.setEnabled(true);
                                                        show.dismiss();
                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(getBaseContext(), "You pressed too many times! Please click the button OK once only", Toast.LENGTH_SHORT).show();
                                        }
                                        gI = 0;
                                    }
                                }, 500);
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                show2.dismiss();
                                btnSubmit.setEnabled(true);
                                btnCancel.setEnabled(true);
                                btnProceed.setEnabled(true);
                            }
                        });
                    } else {
                        Toast.makeText(getBaseContext(), "You pressed too many times! Please click the button Proceed once only", Toast.LENGTH_SHORT).show();
                    }
                    gI = 0;
                }
            }, 500);
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        startTimer();
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

    private class apiSaveDataReceived extends AsyncTask<String, Void, String> {
        String gSupplier = "", gRemarks = "",gHashedID="";
        AlertDialog gAlertDialog = null,gAlertDialog2 = null;
        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);

        public apiSaveDataReceived(String supplier, String remarks, AlertDialog dialog,String hashedID,AlertDialog dialog2) {
            gSupplier = supplier;
            gRemarks = remarks;
            gAlertDialog = dialog;
            gAlertDialog2 = dialog2;
            gHashedID= hashedID;
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected String doInBackground(String... strings) {
            Cursor cursor = myDb3.getAllData(hiddenTitle);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    String sap_number = cursor.getString(1);
                    String fromBranch = cursor.getString(2);

                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                    JSONObject jsonObject = new JSONObject();
                    try {
                        // create your json here
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        JSONObject objectHeaders = new JSONObject();

                        String transType;
                        if (cursor.getInt(7) == 0 && hiddenTitle.equals("API Received from SAP")) {
                            transType = "SAPPO";
                        } else if (cursor.getInt(7) == 1 && hiddenTitle.equals("API Received from SAP")) {
                            transType = "SAPIT";
                        } else if (cursor.getInt(7) == 2 && hiddenTitle.equals("API Received from SAP")) {
                            transType = "SAPDN";
                        } else if (cursor.getInt(7) == 3 && hiddenTitle.equals("API Received from SAP")) {
                            transType = "SAPAR";
                        } else {
                            transType = "TRFR";
                        }

                        objectHeaders.put("transtype", transType);
                        if (hiddenTitle.equals("API Received from SAP")) {
                            objectHeaders.put("transfer_id", null);
                        } else {
                            objectHeaders.put("base_id", (cursor.getInt(9) <= 0 ? null : cursor.getInt(9)));
                        }
                        objectHeaders.put("sap_number", (hiddenTitle.equals("API Received from SAP") ? (sap_number.isEmpty() ? null : sap_number) : null));
                        objectHeaders.put("transdate", currentDateandTime);
                        objectHeaders.put("remarks", gRemarks);
                        objectHeaders.put((hiddenTitle.equals("API Received from SAP") ? "reference2" : "ref2"), null);
                        objectHeaders.put("supplier", (cursor.getInt(7) == 0) ? gSupplier : null);
                        objectHeaders.put("hashed_id", gHashedID);
                        jsonObject.put("header", objectHeaders);

                        JSONArray arrayDetails = new JSONArray();

                        Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                        while (cursor2.moveToNext()) {
                            if (cursor2.getInt(6) == 1) {
                                JSONObject objectDetails = new JSONObject();
                                String itemName = cursor2.getString(3);
                                Double deliveredQty = cursor2.getDouble(4);
                                Double actualQty = cursor2.getDouble(5);

                                objectDetails.put("item_code", itemName);
                                objectDetails.put("from_whse", fromBranch);
                                objectDetails.put("to_whse", cursor2.getString(8));
                                objectDetails.put("quantity", deliveredQty);
                                objectDetails.put("actualrec", actualQty);
                                objectDetails.put("uom", cursor2.getString(11));
                                arrayDetails.put(objectDetails);
                            }
                        }
                        jsonObject.put("details", arrayDetails);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btnProceed.setText("Proceed");
                        btnProceed.setEnabled(true);
                    }
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                    String IPAddress = sharedPreferences2.getString("IPAddress", "");
                    System.out.println("/api/inv/recv/new");
                    System.out.println(jsonObject);
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(IPAddress + "/api/inv/recv/new")
                            .method("POST", body)
                            .addHeader("Authorization", "Bearer " + token)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Validation");
                                    builder.setMessage("Error\n" + e.getMessage());
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        gAlertDialog.dismiss();
                                        gAlertDialog2.dismiss();
                                        btnProceed.setText("Proceed");
                                        btnProceed.setEnabled(true);
                                        finish();
                                        dialog.dismiss();
                                    });
                                    builder.show();
                                }
                            });
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(Call call, okhttp3.Response response) {
                            try {
                                formatResponse(response.body().string(), gAlertDialog,gAlertDialog2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            loadingDialog.dismissDialog();
        }
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
            navigationManager.showFragment(title);
            getSupportActionBar().setTitle(title);
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
                else if(selectedItem.equals("Inventory Count")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Inventory Count");
                    intent.putExtra("hiddenTitle", "API Inventory Count");
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

    public void apiSaveReceivedProduction(String remarks, int sapNumber,String hashedID, AlertDialog gDialog, AlertDialog gDialog2){
        Cursor cursor = myDb3.getAllSelected(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONArray arrayDetails = new JSONArray();
                    String whseCode = "";
                    int baseID = 0;
                    Cursor cursor2 = myDb3.getAllSelected(hiddenTitle);
                    int docID = 0;
                    while (cursor2.moveToNext()) {
                        JSONObject objectDetails = new JSONObject();
                        String itemName = cursor2.getString(3);
                        whseCode = cursor2.getString(2);
                        Double actualQty = cursor2.getDouble(5);
                        objectDetails.put("item_code", itemName);
                        objectDetails.put("quantity", actualQty);
                        objectDetails.put("whsecode", whseCode);
                        objectDetails.put("uom", cursor2.getString(11));
                        arrayDetails.put(objectDetails);

                        JSONObject jsonObjectResponse = new JSONObject(cursor2.getString(16));
                        if (!jsonObjectResponse.isNull("data") && jsonObjectResponse.getBoolean("success")) {
                            JSONArray jaData = jsonObjectResponse.getJSONArray("data");
                            for (int i = 0; i < jaData.length(); i++) {
                                JSONObject joData = jaData.getJSONObject(i);

                                String cItemCode = cursor2.getString(3);
                                String jItemCode = joData.has("item_code") ? joData.getString("item_code") : "";
                                if(cItemCode.equals(jItemCode)){
                                    int prodOrderRow = joData.has("id") ? !joData.isNull("id") ? joData.getInt("id") : 0 : 0;
                                    docID = joData.has("doc_id") ? !joData.isNull("doc_id") ? joData.getInt("doc_id") : 0 : 0;
                                    System.out.println("iddd " + prodOrderRow);
                                    objectDetails.put("prod_order_row_id",prodOrderRow);
                                    objectDetails.put("base_row_id",prodOrderRow);
                                }
                            }
                        }
                    }
                    cursor2.close();
                    JSONObject objectHeaders = new JSONObject();
//                    objectHeaders.put("prod_order_id", baseID);
                    objectHeaders.put("prod_order_id", docID);
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL : sapNumber));
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put("whsecode", whseCode);
                    objectHeaders.put("hashed_id", hashedID);
                    jsonObject.put("header", objectHeaders);
                    jsonObject.put("rows", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                    gDialog.dismiss();
                    gDialog2.dismiss();
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                System.out.println("HOY: " + jsonObject);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/production/rec_from_prod/new")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" + e.getMessage());
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gDialog.dismiss();
                                        gDialog2.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
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
                            gDialog.dismiss();
                            gDialog2.dismiss();
                            e.printStackTrace();
                        }
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                boolean isSuccess = jj.getBoolean("success");
                                String msg = jj.getString("message");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Message");
                                    builder.setMessage(msg);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent;
                                            intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                            intent.putExtra("title", title);
                                            intent.putExtra("hiddenTitle", hiddenTitle);
                                            startActivity(intent);
                                            finish();
                                            gDialog.dismiss();
                                            gDialog2.dismiss();
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                            finish();
                                            gDialog.dismiss();
                                            gDialog2.dismiss();
                                            dialog.dismiss();
                                        });
                                        builder.show();
                                    } else {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("Validation1");
                                        builder.setMessage(msg);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                btnProceed.setEnabled(true);
                                                btnProceed.setText("Proceed");
                                                gDialog.dismiss();
                                                gDialog2.dismiss();
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.show();
                                    }
                                }
                            } catch (Exception e) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Message");
                                builder.setMessage(e.getMessage());
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        btnProceed.setEnabled(true);
                                        btnProceed.setText("Proceed");
                                        gDialog.dismiss();
                                        gDialog2.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                });
            }
        }else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
            builder.setCancelable(false);
            builder.setTitle("Validation");
            builder.setMessage("Selected item is currently empty!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btnProceed.setEnabled(true);
                    btnProceed.setText("Proceed");
                    gDialog.dismiss();
                    gDialog2.dismiss();
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private class myWarehouse extends AsyncTask<String, Void, String> {
        String gitle = "";
        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);
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
                    System.out.println("module " + cursor.getString(4));
                    String module = cursor.getString(4);
                    if (module.equals("Warehouse") && !gIsRefresh) {
                        System.out.println("first last");
                        cResponse = cursor.getString(3);
                        return cursor.getString(3);
                    } else if (module.equals("Warehouse") && gIsRefresh) {
                        cURL = cursor.getString(1);
                        cMethod = cursor.getString(2);
                        cResponse = cursor.getString(3);
                    }
                }
                if (gIsRefresh && !cURL.trim().isEmpty() && !cMethod.trim().isEmpty()) {
                    String sURL = "/api/whse/get_all";
                    utility_class utilityc = new utility_class();

                    System.out.println("sURL " + sURL);

                    SharedPreferences sharedPreferences2 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    String bearerToken = Objects.requireNonNull(sharedPreferences2.getString("token", ""));

                    OkHttpClient client;

                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(30, TimeUnit.SECONDS);
                    builder.readTimeout(30, TimeUnit.SECONDS);
                    builder.writeTimeout(30, TimeUnit.SECONDS);
                    client = builder.build();

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    okhttp3.Request request = null;
                    request = new okhttp3.Request.Builder()
                            .url(utilityc.getIPAddress(API_SelectedItems.this) + sURL)
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
            gBranch = s;
            showWarehouses(gLbl, s);
            loadingDialog.dismissDialog();
        }
    }

    public void showWarehouses(TextView lblSelectedBranch, String vResult){
        AlertDialog _dialog = null;
        AlertDialog.Builder dialogSelectWarehouse = new AlertDialog.Builder(API_SelectedItems.this);
        dialogSelectWarehouse.setTitle("Select Warehouse");
        dialogSelectWarehouse.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout textInputLayout = new TextInputLayout(API_SelectedItems.this);
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeColor(Color.parseColor("#1687a7"));
        textInputLayout.setHint("Search Warehouse");

        AutoCompleteTextView txtSearchBranch = new AutoCompleteTextView(getBaseContext());
        txtSearchBranch.setTextSize(15);
        textInputLayout.addView(txtSearchBranch);
        layout.addView(textInputLayout);

        LinearLayout.LayoutParams layoutParamsLa = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout la = new LinearLayout(API_SelectedItems.this);
        la.setWeightSum(2f);
        la.setOrientation(LinearLayout.HORIZONTAL);
        la.setLayoutParams(layoutParamsLa);

        final List<String>[] warehouses = new List[]{returnBranches(vResult)};
        final ArrayList<String>[] myReference = new ArrayList[]{getReference(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final ArrayList<String>[] myID = new ArrayList[]{getID(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final List<String>[] listItems = new List[]{getListItems(warehouses[0])};

        MaterialButton btnSearchBranch = new MaterialButton(API_SelectedItems.this);
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

                API_SelectedItems.MyAdapter adapter = new API_SelectedItems.MyAdapter(API_SelectedItems.this, myReference[0], myID[0]);

                listView.setAdapter(adapter);
            }
        });

        AppCompatButton btnRefresh = new AppCompatButton(API_SelectedItems.this);
//        btnRefresh.setCornerRadius(20);
        LinearLayout.LayoutParams layoutParamsRefresh = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        layoutParamsRefresh.setMargins(10, 0, 0, 0);
        btnRefresh.setLayoutParams(layoutParamsRefresh);
        btnRefresh.setBackgroundColor(Color.rgb(157, 203, 242));
//        btnSearchBranch.setPadding(20,20,20,20);
        btnRefresh.setTextColor(Color.WHITE);
        btnRefresh.setTextSize(13);
        btnRefresh.setText("Refresh");

//        layout.addView(btnSearchBranch);
        la.addView(btnSearchBranch);
        la.addView(btnRefresh);
        layout.addView(la);


        LinearLayout.LayoutParams layoutParamsWarehouses = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,300);
        layoutParamsWarehouses.setMargins(10,10,10,10);
        listView.setLayoutParams(layoutParamsWarehouses);

        txtSearchBranch.setAdapter(fillItems(listItems[0]));
        API_SelectedItems.MyAdapter adapter = new API_SelectedItems.MyAdapter(API_SelectedItems.this, myReference[0], myID[0]);
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
                        final_dialog.dismiss();
                    }
                });
            }
        });
        layout.addView(listView);
    }

    public List<String> getListItems(List<String> warehouses){
        List<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                result.add(temp);
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
            View row = layoutInflater.inflate(R.layout.custom_list_view_sales_logs, parent, false);
            TextView textView1 = row.findViewById(R.id.txtReference);
            TextView textView2 = row.findViewById(R.id.txtIDs);
            TextView textView3 = row.findViewById(R.id.txtAmount);
            textView1.setText(myReference.get(position));
            textView2.setText(myIds.get(position));
            textView2.setVisibility(View.GONE);
            textView3.setVisibility(View.GONE);
            return row;
        }
    }

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SelectedItems.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_SelectedItems.myChangePassword myChangePassword = new API_SelectedItems.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_SelectedItems.this);
                                        pc.removeToken(API_SelectedItems.this);
                                        startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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



    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    pc.loggedOut(API_SelectedItems.this);
                    pc.removeToken(API_SelectedItems.this);
                    startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private class TransferItem extends AsyncTask<String, Void, String> {
        String toBranch = "", remarks = "", hashedID = "";
        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);
        JSONObject jsonObject = new JSONObject();
        public TransferItem(String sToBranch, String sRemarks, String sHashedID){
            toBranch= sToBranch;
            remarks = sRemarks;
            hashedID= sHashedID;
            loadingDialog = new LoadingDialog(API_SelectedItems.this);
        }

        @Override
        protected void onPreExecute() {


            try {
                // create your json here
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());

                JSONObject objectHeaders = new JSONObject();
//            objectHeaders.put("transtype", "TRFR");
                objectHeaders.put("sap_number", null);
                objectHeaders.put("transdate", currentDateandTime);
                objectHeaders.put("remarks", remarks);
                objectHeaders.put("reference2", null);
                objectHeaders.put("hashed_id", hashedID);
                jsonObject.put("header", objectHeaders);

                JSONArray arrayDetails = new JSONArray();

                Cursor cursor = myDb4.getAllData(title);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        JSONObject objectDetails = new JSONObject();
                        objectDetails.put("item_code", cursor.getString(1));
                        objectDetails.put("to_whse", toBranch);
//                    objectDetails.put("to_whse", null);
                        objectDetails.put("quantity", cursor.getDouble(2));
                        objectDetails.put("uom", cursor.getString(5));
                        arrayDetails.put(objectDetails);
                    }
                    jsonObject.put("details", arrayDetails);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loadingDialog.startLoadingDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.contains("IO Exception")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData("/api/inv/trfr/new","POST", jsonObject.toString(), title, hiddenTitle,currentDate);
                        if(isInserted){
                            myDb4.truncateTable();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("The data is inserted to local database");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    loadingDialog.dismissDialog();
                                    Intent intent;
                                    intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("hiddenTitle", hiddenTitle);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }else{
                            loadingDialog.dismissDialog();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("Your data is failed to insert in local database");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }else{
                String result;
                try {
                    result = s;
                    System.out.println("transfer result: " + result);
                    if(result.startsWith("{")){
                        JSONObject jj = new JSONObject(result);
                        boolean isSuccess = jj.getBoolean("success");
                        if (isSuccess) {
                            JSONObject jsonObjectData = jj.getJSONObject("data");
                            String reference = jsonObjectData.getString("reference");
                            String finalReference = reference;
                            API_SelectedItems.this.runOnUiThread(() -> {
                                myDb4.truncateTable();
                                Toast.makeText(getBaseContext(), "Transaction Completed", Toast.LENGTH_SHORT).show();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setMessage("Reference #: " + finalReference);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadingDialog.dismissDialog();
                                        Intent intent;
                                        intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                        intent.putExtra("title", title);
                                        intent.putExtra("hiddenTitle", hiddenTitle);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder.show();
                            });
                        }else {
                            API_SelectedItems.this.runOnUiThread(() -> {
                                try {

                                    System.out.println(jj.getString("message"));
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                } catch (JSONException e) {
                                    loadingDialog.dismissDialog();
                                    e.printStackTrace();
                                }
                            });
                        }
                    }else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                        btnProceed.setEnabled(true);
                        btnProceed.setText("Proceed");
                    }
                } catch (Exception e) {
                    loadingDialog.dismissDialog();
                    btnProceed.setEnabled(true);
                    btnProceed.setText("Proceed");
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
            String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
            String IPaddress = sharedPreferences2.getString("IPAddress", "");

            String sURL = IPaddress + "/api/inv/trfr/new";
            String method = "POST";
            String bodyy = jsonObject.toString();
            String fromModule = title;
            String hiddenFromModule = hiddenTitle;

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(sURL)
                    .method(method, body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;
            try {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(30, TimeUnit.SECONDS);
                builder.readTimeout(30, TimeUnit.SECONDS);
                builder.writeTimeout(30, TimeUnit.SECONDS);
                client = builder.build();
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO Exception\n" + e.getMessage();
            }
        }
    }

    public void apiSaveTransferItem(String toBranch,String remarks,String hashedID){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            JSONObject objectHeaders = new JSONObject();
//            objectHeaders.put("transtype", "TRFR");
            objectHeaders.put("sap_number", null);
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);
            objectHeaders.put("hashed_id", hashedID);
            jsonObject.put("header", objectHeaders);

            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("to_whse", toBranch);
//                    objectDetails.put("to_whse", null);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("details", arrayDetails);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String sURL = IPaddress + "/api/inv/trfr/new";
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method(method, body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        if(isInserted){
                            myDb4.truncateTable();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("The data is inserted to local database");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent;
                                    intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("hiddenTitle", hiddenTitle);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }else{
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("Your data is failed to insert in local database");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                String result;
                try {
                    result = response.body().string();
                    System.out.println("transfer result: " + result);
                    if(result.startsWith("{")){
                        JSONObject jj = new JSONObject(result);
                        boolean isSuccess = jj.getBoolean("success");
                        if (isSuccess) {
                            JSONObject jsonObjectData = jj.getJSONObject("data");
                            String reference = jsonObjectData.getString("reference");
                            String finalReference = reference;
                            API_SelectedItems.this.runOnUiThread(() -> {
                                myDb4.truncateTable();
                                Toast.makeText(getBaseContext(), "Transaction Completed", Toast.LENGTH_SHORT).show();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setMessage("Reference #: " + finalReference);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent;
                                        intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                        intent.putExtra("title", title);
                                        intent.putExtra("hiddenTitle", hiddenTitle);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder.show();
                            });
                        }else {
                            API_SelectedItems.this.runOnUiThread(() -> {
                                try {
                                    System.out.println(jj.getString("message"));
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                    btnProceed.setEnabled(true);
                                    btnProceed.setText("Proceed");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }else {
                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                        btnProceed.setEnabled(true);
                        btnProceed.setText("Proceed");
                    }
                } catch (JSONException | IOException e) {
                    btnProceed.setEnabled(true);
                    btnProceed.setText("Proceed");
                    e.printStackTrace();
                }
            }
        });
    }

    public void apiItemRequest(String remarks,String fromWarehouse,String hashedID){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());


            JSONObject objectHeaders = new JSONObject();
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("duedate", lblSelectedDate.getText().toString() + " 00:00");
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);
            objectHeaders.put("hashed_id", hashedID);
            jsonObject.put("header", objectHeaders);

            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

//            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
//            String branch = Objects.requireNonNull(sharedPreferences2.getString("whse", ""));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
//                    objectDetails.put("to_whse", null);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    objectDetails.put("from_whse", fromWarehouse);
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("rows", arrayDetails);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String sURL = IPaddress + "/api/inv/item_request/new";
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method(method, body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        if(isInserted){
                            myDb4.truncateTable();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("The data is inserted to local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                Intent intent;
                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hiddenTitle);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                            });
                        }else{
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Message");
                            builder.setMessage("Your data is failed to insert in local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                            });
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                String result;
                try {
                    result = response.body().string();

                    JSONObject jj = new JSONObject(result);
                    boolean isSuccess = jj.getBoolean("success");
                    String msg = jj.getString("message");
                    JSONObject jsonObjectData = jj.getJSONObject("data");
                    String reference = jsonObjectData.getString("reference");
                    if (isSuccess) {
                        API_SelectedItems.this.runOnUiThread(() -> {
                            myDb4.truncateTable();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle(msg);
                            builder.setMessage("Reference #: " + reference);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent;
                                    intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("hiddenTitle", hiddenTitle);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            builder.show();
                        });
                    }else {
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" +msg);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                      dialog.dismiss();
                                    }
                                });
                                builder.show();
                            } catch (Exception e) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" +e.getMessage());
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int returnDate(String value){
        int result = 0;
        SimpleDateFormat y = new SimpleDateFormat(value, Locale.getDefault());
        result = Integer.parseInt(y.format(new Date()));
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        lblSelectedDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
    }

    public void apiSavePullOutConfirm(String remarks,String hashedID) throws JSONException {
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences2.getString("IPAddress", "");

        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

        Cursor cursor = myDb3.getAllData(hiddenTitle);
        int id = 0;
        JSONArray jsonArrayRows = new JSONArray();
        while (cursor.moveToNext()) {
            JSONObject jsonObjectRows = new JSONObject();
            id = cursor.getInt(9);
            jsonObjectRows.put("item_code", cursor.getString(3));
            jsonObjectRows.put("receive_qty", cursor.getDouble(5));
            jsonArrayRows.put(jsonObjectRows);
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectHeader = new JSONObject();
        jsonObjectHeader.put("remarks", remarks);
        jsonObjectHeader.put("hashed_id", hashedID);
        jsonObject.put("header", jsonObjectHeader);
        jsonObject.put("rows", jsonArrayRows);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

//        System.out.println(IPAddress + "/api/pullout/confirm/" + id);

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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                        builder.setCancelable(false);
                        builder.setTitle("Validation");
                        builder.setMessage("Error\n" +e.getMessage());
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String s = response.body().string();
                    if (s.startsWith("{")) {
                        JSONObject jsonObject = new JSONObject(s);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String msg = jsonObject.getString("message");
                                    boolean isSuccess = jsonObject.getBoolean("success");
                                    if (isSuccess) {
                                        myDb3.truncateTable();
                                    }
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Message");
                                    builder.setMessage(isSuccess ? "Message" : "Error" + "\n" + msg);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (isSuccess) {
                                                btnBack.performClick();
                                            }
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } catch (Exception e) {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Validation");
                                    builder.setMessage("Error\n" + e.getMessage());
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Validation");
                                    builder.setMessage("Error\n" + s);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                            }
                        });
                    }
                } catch (Exception ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setTitle("Validation");
                            builder.setMessage("Error\n" + ex.getMessage());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
                }
            }
        });
    }

    private class ReceiveItem extends AsyncTask<String, Void, String> {
        String fromBranch = "",remarks = "",type2 = "",supplier = "",hashedID= "";
        int sapNumber = 0;
        AlertDialog show1 = null, show2 = null;

        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();

        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);
        public ReceiveItem(String fromBranch,String remarks,Integer sapNumber,String type2,String supplier,String hashedID,AlertDialog show1, AlertDialog show2){
            this.fromBranch = fromBranch;
            this.remarks = remarks;
            this.sapNumber = sapNumber;
            this.type2 = type2;
            this.supplier = supplier;
            this.hashedID = hashedID;
            this.show1 = show1;
            this.show2 = show2;

            loadingDialog = new LoadingDialog(API_SelectedItems.this);
        }

        @Override
        protected void onPreExecute() {
            try {
                loadingDialog.startLoadingDialog();
                // create your json here
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());

                JSONObject objectHeaders = new JSONObject();
                objectHeaders.put("transtype", "MNL");
                objectHeaders.put("transfer_id", null);
                objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL : sapNumber));
                objectHeaders.put("transdate", currentDateandTime);
                objectHeaders.put("remarks", remarks);
                objectHeaders.put("reference2", null);
                objectHeaders.put("supplier", supplier.isEmpty() ? JSONObject.NULL : supplier);
                objectHeaders.put("type2", type2);
                objectHeaders.put("hashed_id", hashedID);

                jsonObject.put("header", objectHeaders);


                JSONArray arrayDetails = new JSONArray();

                Cursor cursor = myDb4.getAllData(title);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        JSONObject objectDetails = new JSONObject();
                        objectDetails.put("item_code", cursor.getString(1));
                        objectDetails.put("from_whse", fromBranch);
//                    objectDetails.put("to_whse", null);
                        objectDetails.put("quantity", cursor.getDouble(2));
                        objectDetails.put("actualrec", cursor.getDouble(2));
                        objectDetails.put("uom", cursor.getString(5));
                        arrayDetails.put(objectDetails);
                    }
                    jsonObject.put("details", arrayDetails);
                }
            }
            catch (Exception e) {
                loadingDialog.dismissDialog();
                show1.dismiss();
                show2.dismiss();
                btnProceed.setText("Proceed");
                btnProceed.setEnabled(true);
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.contains("IO Exception")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData("/api/inv/recv/new","POST", jsonObject.toString(), title, hiddenTitle,currentDate);
                        btnProceed.setEnabled(true);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                        builder.setCancelable(false);
                        builder.setTitle("Message");
                        if(isInserted){
                            myDb4.truncateTable();
                            builder.setMessage("The data is inserted to local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                loadingDialog.dismissDialog();
                                show1.dismiss();
                                show2.dismiss();
                                Intent intent;
                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hiddenTitle);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                            });
                            builder.show();
                        }else{
                            builder.setMessage("Your data is failed to insert in local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                loadingDialog.dismissDialog();
                                show1.dismiss();
                                show2.dismiss();
                                btnProceed.setText("Proceed");
                                btnProceed.setEnabled(true);
                                dialog.dismiss();
                            });
                            builder.show();
                        }
                    }
                });
            }else{
                loadingDialog.dismissDialog();
                formatResponse(s,show1,show2);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
            String IPAddress = sharedPreferences2.getString("IPAddress", "");

            String sURL = IPAddress + "/api/inv/recv/new";
            System.out.println(sURL);
            String method = "POST";
            String bodyy = jsonObject.toString();
            System.out.println(jsonObject);
            String fromModule = title;
            String hiddenFromModule = hiddenTitle;
//        System.out.println("body: " + jsonObject);
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(sURL)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;
            try {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(30, TimeUnit.SECONDS);
                builder.readTimeout(30, TimeUnit.SECONDS);
                builder.writeTimeout(30, TimeUnit.SECONDS);
                client = builder.build();
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO Exception\n" + e.getMessage();
            }
        }
    }

    public void apiSaveManualReceived(String fromBranch,String remarks,Integer sapNumber,String type2,String supplier,String hashedID,AlertDialog show1, AlertDialog show2){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            JSONObject objectHeaders = new JSONObject();
            objectHeaders.put("transtype", "MNL");
            objectHeaders.put("transfer_id", null);
            objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL: sapNumber));
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);
            objectHeaders.put("supplier", supplier.isEmpty() ? JSONObject.NULL : supplier);
            objectHeaders.put("type2", type2);
            objectHeaders.put("hashed_id", hashedID);

            jsonObject.put("header", objectHeaders);


            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("from_whse", fromBranch);
//                    objectDetails.put("to_whse", null);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("actualrec", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("details", arrayDetails);
            }else{
                btnProceed.setText("Proceed");
                btnProceed.setEnabled(true);
                show1.dismiss();
                show2.dismiss();
            }
        } catch (JSONException e) {
            show1.dismiss();
            show2.dismiss();
            btnProceed.setText("Proceed");
            btnProceed.setEnabled(true);
            e.printStackTrace();
        }
        System.out.println(jsonObject);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences2.getString("IPAddress", "");

        String sURL = IPAddress + "/api/inv/recv/new";
        System.out.println(sURL);
        String method = "POST";
        String bodyy = jsonObject.toString();
        System.out.println(jsonObject);
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;
//        System.out.println("body: " + jsonObject);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        btnProceed.setEnabled(true);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                        builder.setCancelable(false);
                        builder.setTitle("Message");
                        if(isInserted){
                            myDb4.truncateTable();
                            builder.setMessage("The data is inserted to local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                show1.dismiss();
                                show2.dismiss();
                                Intent intent;
                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hiddenTitle);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                            });
                            builder.show();
                        }else{
                            builder.setMessage("Your data is failed to insert in local database");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                show1.dismiss();
                                show2.dismiss();
                                btnProceed.setText("Proceed");
                                btnProceed.setEnabled(true);
                                dialog.dismiss();
                            });
                            builder.show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String answer = response.body().string();
                System.out.println("answer: " + answer);
                formatResponse(answer,show1,show2);
            }
        });
    }

    public void formatResponse(String temp, AlertDialog show,AlertDialog show2){
        if(!temp.isEmpty()){
            if(temp.substring(0,1).equals("{")){
                try{
                    JSONObject jj = new JSONObject(temp);
                    boolean isSuccess = jj.getBoolean("success");
                    if (isSuccess) {
                        try {
                            JSONObject jsonObjectData = jj.getJSONObject("data");
                            String reference = jsonObjectData.getString("reference");
                            String msg = jj.getString("message");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item")){
                                        myDb3.truncateTable();
                                    }else{
                                        myDb4.truncateTable();
                                    }
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(msg);
                                    builder.setMessage("Reference #: " + reference);
                                    builder.setPositiveButton("OK", null);
                                    AlertDialog showw = builder.show();
                                    Button btn = showw.getButton(DialogInterface.BUTTON_POSITIVE);
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            btn.setEnabled(false);
                                            gI++;
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(gI == 1) {
                                                        if (show != null) {
                                                            show.dismiss();
                                                        }
                                                        if (show2 != null) {
                                                            show2.dismiss();
                                                        }
                                                        btn.setEnabled(true);
                                                        btnProceed.setText("Proceed");
                                                        btnProceed.setEnabled(true);
                                                        Intent intent;
                                                        intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                                        intent.putExtra("title", title);
                                                        intent.putExtra("hiddenTitle", hiddenTitle);
                                                        startActivity(intent);
                                                        finish();
                                                        showw.dismiss();
                                                    }else{
                                                        btn.setEnabled(true);
                                                    }
                                                    gI = 0;
                                                }
                                            }, 500);
                                        }
                                    });
                                }
                            });
                        }
                        catch (Exception ex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Validation");
                                    builder.setMessage("Error\n" + ex.getMessage());
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        btnProceed.setText("Proceed");
                                        btnProceed.setEnabled(true);
                                        if(show != null){
                                            show.dismiss();
                                        }
                                        if(show2 != null){
                                            show2.dismiss();
                                        }
                                        dialog.dismiss();
                                    });
                                    builder.show();
                                }
                            });
                        }
                    } else {
                        String msg = jj.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnProceed.setEnabled(true);
                                if(msg.equals("Token is invalid")){
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setMessage("Your session is expired. Please login again.");
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        if(show != null){
                                            show.dismiss();
                                        }
                                        if(show2 != null){
                                            show2.dismiss();
                                        }
                                        pc.loggedOut(API_SelectedItems.this);
                                        pc.removeToken(API_SelectedItems.this);
                                        startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                        finish();
                                        dialog.dismiss();
                                    });
                                    builder.show();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                            builder.setCancelable(false);
                                            builder.setTitle("Validation");
                                            builder.setMessage("Error\n" + msg);
                                            builder.setPositiveButton("OK", (dialog, which) -> {
                                                if(show != null){
                                                    show.dismiss();
                                                }
                                                if(show2 != null){
                                                    show2.dismiss();
                                                }
                                                btnProceed.setText("Proceed");
                                                btnProceed.setEnabled(true);
                                                dialog.dismiss();
                                            });

                                            builder.show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Validation");
                                    builder.setMessage("Error\n" + ex.getMessage());
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        if(show != null){
                                            show.dismiss();
                                        }
                                        if(show2 != null){
                                            show2.dismiss();
                                        }
                                        btnProceed.setText("Proceed");
                                        btnProceed.setEnabled(true);
                                        dialog.dismiss();
                                    });

                                    builder.show();
                                }
                            });
                        }
                    });
                }
            }else{
                Runnable r = () -> {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                    builder.setCancelable(false);
                    builder.setTitle("Validation");
                    builder.setMessage("Error\n" +temp);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        if(show != null){
                            show.dismiss();
                        }
                        if(show2 != null){
                            show2.dismiss();
                        }
                        btnProceed.setText("Proceed");
                        btnProceed.setEnabled(true);
                        dialog.dismiss();
                    });
                    builder.show();
                };
            }
        }else {
            if(show != null){
                show.dismiss();
            }
            if(show2 != null){
                show2.dismiss();
            }
            btnProceed.setText("Proceed");
            btnProceed.setEnabled(true);
        }
    }


    public void hmReturnBranches(){
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
                try  {
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
                            while (cursor.moveToNext()){
                                String module = cursor.getString(3);
                                if(module.contains("Warehouse")){
//                                    System.out.println(cursor.getString(4));
                                    if(hiddenTitle.equals("API Item Request")){
                                       if(cursor.getString(4).toLowerCase().contains("prod")){
                                           gBranch = cursor.getString(4);
                                       }
                                    }else{
                                        gBranch = cursor.getString(4);
                                    }
                                }else{
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

    public List<String> returnBranches(String vResult){
        List<String> result = new ArrayList<>();
        result.add("Select Warehouse");
        try{
            if(!gBranch.isEmpty()){
                if(gBranch.substring(0,1).equals("{")){
                    JSONObject jsonObjectResponse = new JSONObject(vResult);
                    if(!jsonObjectResponse.isNull("data") && jsonObjectResponse.getBoolean("success")){
                        JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
                            String branch = jsonObject.getString("whsename");
                            result.add(branch);

                        }
                    }else{
                        Toast.makeText(getBaseContext(),"Error \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(),"Error \n" + gBranch, Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getBaseContext(),"Error \n" + gBranch, Toast.LENGTH_SHORT).show();
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), "Front-end Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }


    public String findWarehouseCode(String value){
        try{
            JSONObject jsonObjectResponse = new JSONObject(gBranch);
            JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
                if(value.contains(jsonObject.getString("whsename"))){
                    System.out.println("value " + value + "/" + jsonObject.get("whsename"));
                    return jsonObject.getString("whsecode");
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loadItems() {
        System.out.println("TITLE: " + hiddenTitle);
        Cursor cursor;
        int count = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API System Transfer Item") ? myDb3.countItems(hiddenTitle) : myDb4.countItems(title));
//                int count = myDb4.countItems(title);

        if (hiddenTitle.equals("API Received from SAP")) {
            count = myDb3.countSelected(hiddenTitle);
        } else if (hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count Variance")) {
            count = myDb3.countSelected(hiddenTitle);
        } else if (hiddenTitle.equals("API Pull Out Count")) {
            count = myDb3.countSelected(hiddenTitle);
        } else if (hiddenTitle.equals("API System Transfer Item")|| hiddenTitle.equals("API Pull Out Request Confirmation")) {
            count = myDb3.countSelected(hiddenTitle);
        } else if (hiddenTitle.equals("API Received from Production")) {
            count = myDb3.countSelected(hiddenTitle);
        } else if (hiddenTitle.equals("API Item Request For Transfer")) {
            count = myDb3.countSelected(hiddenTitle);
        }
        else if (hiddenTitle.equals("API Target For Delivery")) {
            count = myDb3.countSelected(hiddenTitle);
        }
        else {
            count = myDb4.countItems(title);
        }

        LinearLayout layout = findViewById(R.id.layoutNoItems);

        if (count == 0) {
            layout.setVisibility(View.VISIBLE);
            Button btnGoto = findViewById(R.id.btnGoto);
            btnGoto.setOnClickListener(view -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                finish();
            });
            btnProceed.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.GONE);

            btnProceed.setVisibility(View.VISIBLE);

            TableRow tableColumn = new TableRow(API_SelectedItems.this);
            tableColumn.setBackgroundColor(Color.rgb(242, 245, 242));
            LinearLayout.LayoutParams layoutParamsTableColumn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
            tableColumn.setLayoutParams(layoutParamsTableColumn);
            final TableLayout tableLayout = findViewById(R.id.table_main);
            tableLayout.removeAllViews();
            String[] columns = null;

            if(hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Pull Out Request Confirmation")){
                columns =  new String[]{"Item", "Del. Qty.", "Act. Qty.", "Var.", "Action"};
            }else if( hiddenTitle.equals("API Received from Production")){
                columns = new String[]{"Item", "For Receive","Actual Qty", "Var", "Action"};
            }
            else if( hiddenTitle.equals("API Item Request For Transfer")){
                columns = new String[]{"Item", "Delivered Qty.", "Actual Qty."};
            }
            else if( hiddenTitle.equals("API Target For Delivery")){
                columns = new String[]{"Item", "Balance.", "Actual Del.","Var.","Action"};
            }
            else if(hiddenTitle.equals("API Pull Out Count")){
                columns = new String[]{"Item", "Qty."};
            }
            else if(hiddenTitle.equals("API Item Request")){
                columns = new String[]{"Item", "Qty.","Action"};
            }
            else{
                columns = new String[]{"Item", "Qty.", "Uom"};
            }

            for (String s : columns) {
                TextView lblColumn1 = new TextView(API_SelectedItems.this);
                lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn1.setText(s);
                lblColumn1.setPadding(10, 0, 10, 0);
                tableColumn.addView(lblColumn1);
            }
            tableLayout.addView(tableColumn);
            cursor = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Item Request For Transfer") || hiddenTitle.equals("API Pull Out Count Variance") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Target For Delivery") ? myDb3.getAllSelected(hiddenTitle) : myDb4.getAllData(title));

//                    cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Item Request For Transfer") || hiddenTitle.equals("API Pull Out Count Variance") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Target For Delivery")) {
                        if (cursor.getInt(6) == 1) {
                            final TableRow tableRow = new TableRow(API_SelectedItems.this);
                            tableRow.setBackgroundColor(Color.WHITE);
                            String itemName = cursor.getString((hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item")  || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer")  || hiddenTitle.equals("API Pull Out Count Variance") || hiddenTitle.equals("API Target For Delivery") ? 3 : 1));
                            String v = cutWord(itemName);
                            double quantity = 0.00;

                            if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item")  || hiddenTitle.equals("API Item Request For Transfer") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Target For Delivery")) {
                                quantity = cursor.getDouble(4);
                            } else if (hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API Pull Out Count Variance")) {
                                quantity = cursor.getDouble(5);
                            }else if(hiddenTitle.equals("API Received from Production")){
                                quantity = cursor.getDouble(17);
                            }
                            else {
                                quantity = cursor.getDouble(2);
                            }

                            final int id = cursor.getInt(0);

                            LinearLayout linearLayoutItem = new LinearLayout(this);
                            linearLayoutItem.setPadding(10, 10, 10, 10);
                            linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                            linearLayoutItem.setBackgroundColor(Color.WHITE);
                            linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            tableRow.addView(linearLayoutItem);

                            LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            TextView lblColumn1 = new TextView(this);
                            lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn1.setLayoutParams(layoutParamsItem);
//                       String v = cutWord(item);
                            lblColumn1.setText(itemName);
                            lblColumn1.setTextSize(13);
                            lblColumn1.setBackgroundColor(Color.WHITE);

                            lblColumn1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getBaseContext(), itemName, Toast.LENGTH_SHORT).show();
                                }
                            });

                            linearLayoutItem.addView(lblColumn1);

                            TextView lblColumn2 = new TextView(API_SelectedItems.this);
                            lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn2.setText(df.format(quantity));
                            lblColumn2.setTextSize(13);
                            lblColumn2.setBackgroundColor(Color.WHITE);
                            lblColumn2.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn2);



                            if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Item Request For Transfer") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Target For Delivery")) {

//                                if(hiddenTitle.equals("API Received from Production")){
//                                    TextView lblColumn44 = new TextView(API_SelectedItems.this);
//                                    lblColumn44.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                                    lblColumn44.setText(df.format(cursor.getDouble(12)));
//                                    lblColumn44.setBackgroundColor(Color.WHITE);
//                                    lblColumn44.setPadding(10, 10, 10, 10);
//                                    lblColumn44.setTextSize(13);
//                                    tableRow.addView(lblColumn44);
//                                }
                                //actual
                                TextView lblColumn4 = new TextView(API_SelectedItems.this);
                                lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                lblColumn4.setText(df.format(cursor.getDouble(5)));
                                lblColumn4.setBackgroundColor(Color.WHITE);
                                lblColumn4.setPadding(10, 10, 10, 10);
                                lblColumn4.setTextSize(13);
                                tableRow.addView(lblColumn4);

                                if(hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Target For Delivery")){
                                    TextView lblColumn5 = new TextView(API_SelectedItems.this);
                                    lblColumn5.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                    double variance = 0.00;
                                    variance = cursor.getDouble(5) - quantity;
                                    lblColumn5.setText(df.format(variance));
                                    lblColumn5.setBackgroundColor(Color.WHITE);
                                    lblColumn5.setTextSize(13);
                                    lblColumn5.setPadding(10, 10, 10, 10);
                                    tableRow.addView(lblColumn5);
                                }
                            }

                            TextView lblColumn3 = new TextView(API_SelectedItems.this);
                            lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn3.setTag(id);
                            lblColumn3.setBackgroundColor(Color.WHITE);
                            lblColumn3.setText("Remove");
                            lblColumn3.setTextSize(13);
                            lblColumn3.setPadding(10, 10, 10, 10);
                            lblColumn3.setTextColor(Color.RED);

                            lblColumn3.setOnClickListener(view -> {
                                boolean deletedItem;
                                deletedItem = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Pull Out Request Confirmation") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Target For Delivery") ? myDb3.removeData(Integer.toString(id)) : myDb3.deleteData(Integer.toString(id)));
                                if (!deletedItem) {
                                    Toast.makeText(API_SelectedItems.this, "Item not remove", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(API_SelectedItems.this, "Item removed1", Toast.LENGTH_SHORT).show();
                                    Intent intent;
                                    intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("hiddenTitle", hiddenTitle);
                                    startActivity(intent);
                                    finish();
                                }


                                if (myDb4.countItems(title).equals(0)) {
                                    tableLayout.removeAllViews();
                                    btnProceed.setVisibility(View.GONE);
                                }
                            });

                            tableRow.addView(lblColumn3);

                            tableLayout.addView(tableRow);
                        }
                        View viewLine = new View(this);
                        LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        viewLine.setLayoutParams(layoutParamsLine);
                        viewLine.setBackgroundColor(Color.GRAY);
                        tableLayout.addView(viewLine);
                    } else {
                        final TableRow tableRow = new TableRow(API_SelectedItems.this);
                        tableRow.setBackgroundColor(Color.WHITE);
                        String itemName = cursor.getString(1);
                        String v = cutWord(itemName);
                        double quantity = cursor.getDouble(2);
                        final int id = cursor.getInt(0);

                        LinearLayout linearLayoutItem = new LinearLayout(this);
                        linearLayoutItem.setPadding(10, 10, 10, 10);
                        linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                        linearLayoutItem.setBackgroundColor(Color.WHITE);
                        linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        tableRow.addView(linearLayoutItem);

                        LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextView lblColumn1 = new TextView(this);
                        lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn1.setLayoutParams(layoutParamsItem);
                        lblColumn1.setBackgroundColor(Color.WHITE);
                        lblColumn1.setText(itemName);
                        lblColumn1.setTextSize(15);
                        lblColumn1.setBackgroundColor(Color.WHITE);

                        lblColumn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getBaseContext(), itemName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        linearLayoutItem.addView(lblColumn1);

                        TextView lblColumn2 = new TextView(API_SelectedItems.this);
                        lblColumn2.setBackgroundColor(Color.WHITE);
                        lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn2.setText(df.format(quantity));
                        lblColumn2.setTextSize(15);
                        lblColumn2.setPadding(10, 10, 10, 10);
                        tableRow.addView(lblColumn2);

                        if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Received from Production")){
                            TextView lblColumn4 = new TextView(API_SelectedItems.this);
                            lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn4.setTag(id);
                            lblColumn4.setBackgroundColor(Color.WHITE);
                            lblColumn4.setText(cursor.getString(5));
                            lblColumn4.setTextSize(13);
                            lblColumn4.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn4);
                        }

                        TextView lblColumn3 = new TextView(API_SelectedItems.this);
                        lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn3.setBackgroundColor(Color.WHITE);
                        lblColumn3.setTag(id);
                        lblColumn3.setText("Remove");
                        lblColumn3.setTextSize(13);
                        lblColumn3.setPadding(10, 10, 10, 10);
                        lblColumn3.setTextColor(Color.RED);

                        lblColumn3.setOnClickListener(view -> {
                            int deletedItem;
                            deletedItem =  myDb4.deleteData(Integer.toString(id));

                            if (deletedItem < 0) {
                                Toast.makeText(API_SelectedItems.this, "Item not remove", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(API_SelectedItems.this, "Item removed2", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                intent.putExtra("title", title);
                                intent.putExtra("hiddenTitle", hiddenTitle);
                                startActivity(intent);
                                finish();
                            }

                            if (myDb4.countItems(title).equals(0)) {
                                tableLayout.removeAllViews();
                                btnProceed.setVisibility(View.GONE);
                            }
                        });

                        tableRow.addView(lblColumn3);

                        tableLayout.addView(tableRow);

                        View viewLine = new View(this);
                        LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        viewLine.setLayoutParams(layoutParamsLine);
                        viewLine.setBackgroundColor(Color.GRAY);
                        tableLayout.addView(viewLine);

                    }
                }
            }
        }
    }

    public void apiSaveItemRequestForTransfer(String sapNumber, String remarks,String hashedID, AlertDialog show) {
        Cursor cursor = myDb3.getAllSelected(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                String fromBranch = cursor.getString(2);

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    objectHeaders.put("sap_number", sapNumber.isEmpty() ? null : sapNumber);
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put("reference2", null);
                    objectHeaders.put("base_id", cursor.getInt(9));
                    objectHeaders.put("base_objtype", cursor.getInt(14));
                    objectHeaders.put("hashed_id", hashedID);
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllSelected(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("row_base_id", cursor2.getInt(13));
                            objectDetails.put("item_code", itemName);
                            objectDetails.put("to_whse", cursor2.getString(8));
                            objectDetails.put("quantity", actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("details", arrayDetails);
                } catch (JSONException e) {
                    show.dismiss();
                    e.printStackTrace();
                    btnProceed.setText("Proceed");
                    btnProceed.setEnabled(true);
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                System.out.println("body " + jsonObject);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/inv/trfr/new")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Message");
                                builder.setMessage(e.getMessage());
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    btnProceed.setText("Proceed");
                                    btnProceed.setEnabled(true);
                                    dialog.dismiss();
                                    show.dismiss();
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) {
                        String result = "";
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                            show.dismiss();
                        }
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                System.out.println("result " + finalResult);
                                if(finalResult.length() > 0) {
                                    if (finalResult.startsWith("{")) {
                                        JSONObject jj = new JSONObject(finalResult);
                                        boolean isSuccess = jj.getBoolean("success");
                                        JSONObject jsonObjectData = jj.isNull("data") ? new JSONObject() : jj.getJSONObject("data");
                                        String msg = jj.getString("message");
                                        String reference = !jsonObjectData.has("reference") ? "" : jsonObjectData.isNull("reference") ? "" : jsonObjectData.getString("reference");
                                        if (isSuccess) {
                                            myDb3.truncateTable();
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                            builder.setCancelable(false);
                                            builder.setTitle(msg);
                                            builder.setMessage("Reference #: " + reference);
                                            builder.setPositiveButton("OK", (dialog, which) -> {
                                                show.dismiss();
                                                Intent intent;
                                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                                intent.putExtra("title", title);
                                                intent.putExtra("hiddenTitle", hiddenTitle);
                                                startActivity(intent);
                                                finish();
                                                dialog.dismiss();
                                            });
                                            builder.show();
                                        } else {
                                            if (msg.equals("Token is invalid")) {
                                                btnProceed.setText("Proceed");
                                                btnProceed.setEnabled(true);
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                                builder.setCancelable(false);
                                                builder.setMessage("Your session is expired. Please login again.");
                                                builder.setPositiveButton("OK", (dialog, which) -> {
                                                    show.dismiss();
                                                    pc.loggedOut(API_SelectedItems.this);
                                                    pc.removeToken(API_SelectedItems.this);
                                                    startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                                    finish();
                                                    dialog.dismiss();
                                                });
                                                builder.show();
                                            } else {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                                builder.setCancelable(false);
                                                builder.setTitle("Validation");
                                                builder.setMessage("Error\n" + msg);
                                                builder.setPositiveButton("OK", (dialog, which) -> {
                                                    show.dismiss();
                                                    btnProceed.setText("Proceed");
                                                    btnProceed.setEnabled(true);
                                                    dialog.dismiss();
                                                });
                                                builder.show();
                                            }
                                        }
                                    }else{
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("Validation");
                                        builder.setMessage("Error\n" +finalResult);
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            show.dismiss();
                                            btnProceed.setText("Proceed");
                                            btnProceed.setEnabled(true);
                                            dialog.dismiss();
                                        });
                                        builder.show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" + e.getMessage());
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    show.dismiss();
                                    btnProceed.setText("Proceed");
                                    btnProceed.setEnabled(true);
                                    dialog.dismiss();
                                });
                                builder.show();
                            }
                        });
                    }
                });
            }
        }
    }

    public void apiSaveDataRec(String supplier, String remarks) {
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                String sap_number = cursor.getString(1);
                String fromBranch = cursor.getString(2);

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    String transType;
                    if(cursor.getInt(7) == 0 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPPO";
                    }else if(cursor.getInt(7) == 1 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPIT";
                    }else if(cursor.getInt(7) == 2 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPDN";
                    }else if(cursor.getInt(7) == 3 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPAR";
                    }
                    else {
                        transType = "TRFR";
                    }

                    objectHeaders.put("transtype", transType);
                  if(hiddenTitle.equals("API Received from SAP")){
                      objectHeaders.put("transfer_id", null);
                  }else {
                      objectHeaders.put("base_id", (cursor.getInt(9) <= 0 ? null : cursor.getInt(9)));
                  }
                    objectHeaders.put("sap_number", (hiddenTitle.equals("API Received from SAP") ? (sap_number.isEmpty() ? null : sap_number) : null));
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put((hiddenTitle.equals("API Received from SAP") ? "reference2" : "ref2"), null);
                    objectHeaders.put("supplier", (cursor.getInt(7) == 0) ? supplier : null);
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double deliveredQty = cursor2.getDouble(4);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("item_code", itemName);
                            objectDetails.put("from_whse", fromBranch);
                            objectDetails.put("to_whse", cursor2.getString(8));
                            objectDetails.put("quantity", deliveredQty);
                            objectDetails.put("actualrec", actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("details", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                    btnProceed.setText("Proceed");
                    btnProceed.setEnabled(true);
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/inv/recv/new")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" + e.getMessage());
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    btnProceed.setText("Proceed");
                                    btnProceed.setEnabled(true);
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            }
                        });
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call call, okhttp3.Response response) {
                        String result = "";
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                String msg = jj.getString("message");
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    JSONObject jsonObjectData = jj.getJSONObject("data");
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(msg);
                                    builder.setMessage("Reference #: " + jsonObjectData.getString("reference"));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent;
                                            intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                            intent.putExtra("title", title);
                                            intent.putExtra("hiddenTitle", hiddenTitle);
                                            startActivity(intent);
                                            finish();
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                            finish();
                                            dialog.dismiss();
                                        });
                                        builder.show();
                                    } else {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("Validation");
                                        builder.setMessage("Error\n" + msg);
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            btnProceed.setText("Proceed");
                                            btnProceed.setEnabled(true);
                                            finish();
                                            dialog.dismiss();
                                        });
                                        builder.show();
                                    }
                                }
                            } catch (Exception e) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" + e.getMessage());
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    btnProceed.setText("Proceed");
                                    btnProceed.setEnabled(true);
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            }
                        });
                    }
                });
            }
        }
    }

    public void apiSaveInventoryCount(String remarks,String hashedID) {
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put("hashed_id", hashedID);
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double deliveredQty = cursor2.getDouble(4);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("item_code", itemName);

                            if(hiddenTitle.equals("API Inventory Count")){
                                objectDetails.put("quantity", deliveredQty);
                            }
                            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                            String isManager = sharedPreferences2.getString("isManager", "");
                            objectDetails.put((hiddenTitle.equals("API Inventory Count") ? "actual_count" :  (hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count Variance")) && Integer.parseInt(isManager) > 0  ? "final_count" :"quantity"), actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("rows", arrayDetails);
                } catch (JSONException e) {
                    btnProceed.setText("Proceed");
                    btnProceed.setEnabled(true);
                    e.printStackTrace();
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");

                String isInvCount = (hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Inventory Count Variance") ? "inv/count" : "pulloutreq");

                System.out.println("ip: " + IPaddress + "/api/" + isInvCount + (hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count Variance") ? "/variance" : "/create"));
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/" + isInvCount + (hiddenTitle.equals("API Inventory Count Variance") || hiddenTitle.equals("API Pull Out Count Variance") ? "/variance" : "/create"))
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" +e.getMessage());
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        btnProceed.setText("Proceed");
                                        btnProceed.setEnabled(true);
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) {
                        String result = "";
                        try {
                            result = response.body().string();
                            System.out.println("eheh: " + result);
                        } catch (IOException e) {
                            btnProceed.setText("Proceed");
                            btnProceed.setEnabled(true);
                            e.printStackTrace();
                        }
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                String msg = jj.getString("message");
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("Message");
                                    builder.setMessage(msg);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent;
                                            intent = new Intent(getBaseContext(), API_SelectedItems.class);
                                            intent.putExtra("title", title);
                                            intent.putExtra("hiddenTitle", hiddenTitle);
                                            startActivity(intent);
                                            finish();
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                            finish();
                                            dialog.dismiss();
                                        });
                                        builder.show();
                                    } else {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("Validation");
                                        builder.setMessage("Error\n" + msg);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                btnProceed.setText("Proceed");
                                                btnProceed.setEnabled(true);
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.show();
                                    }
                                }
                            } catch (Exception e) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setTitle("Validation");
                                builder.setMessage("Error\n" + e.getMessage());
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        btnProceed.setText("Proceed");
                                        btnProceed.setEnabled(true);
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                });
            }
        }
    }

    public String cutWord(String value){
        String result;
        int limit = 10;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }
}