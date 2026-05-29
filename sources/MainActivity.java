package com.group9.wastetracker;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.*;
import android.view.*;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout mainLayout;
    SharedPreferences prefs;
    int chemWeight, plasWeight, metlWeight;
    String chemStatus, plasStatus, metlStatus, historyLog, archiveLog;
    
    // Tracks who is currently logged in to isolate their data
    String currentLoggedInUser = "admin"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("WasteTrackerFinal", MODE_PRIVATE);
        showLoginScreen();
    }

    private void loadSystemData() {
        // If it's the default admin, check for legacy keys first, otherwise use scoped user keys
        if (currentLoggedInUser.equals("admin")) {
            chemWeight = prefs.getInt("chem", prefs.getInt("admin_chem", 0));
            plasWeight = prefs.getInt("plas", prefs.getInt("admin_plas", 0));
            metlWeight = prefs.getInt("metl", prefs.getInt("admin_metl", 0));
            chemStatus = prefs.getString("s_chem", prefs.getString("admin_s_chem", "None"));
            plasStatus = prefs.getString("s_plas", prefs.getString("admin_s_plas", "None"));
            metlStatus = prefs.getString("s_metl", prefs.getString("admin_s_metl", "None"));
            historyLog = prefs.getString("history", prefs.getString("admin_history", "System Initialized."));
            archiveLog = prefs.getString("archive", prefs.getString("admin_archive", "No archived sessions yet."));
        } else {
            chemWeight = prefs.getInt(currentLoggedInUser + "_chem", 0);
            plasWeight = prefs.getInt(currentLoggedInUser + "_plas", 0);
            metlWeight = prefs.getInt(currentLoggedInUser + "_metl", 0);
            chemStatus = prefs.getString(currentLoggedInUser + "_s_chem", "None");
            plasStatus = prefs.getString(currentLoggedInUser + "_s_plas", "None");
            metlStatus = prefs.getString(currentLoggedInUser + "_s_metl", "None");
            historyLog = prefs.getString(currentLoggedInUser + "_history", "System Initialized.");
            archiveLog = prefs.getString(currentLoggedInUser + "_archive", "No archived sessions yet.");
        }
    }

    private void saveSystemData() {
        SharedPreferences.Editor editor = prefs.edit();
        
        // Generate real-time timestamp for this data alteration sync
        String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault()).format(new Date());
        editor.putString(currentLoggedInUser + "_timestamp", currentTimestamp);

        // Save to both global and scoped versions if admin to maintain backward visibility
        if (currentLoggedInUser.equals("admin")) {
            editor.putInt("chem", chemWeight);
            editor.putInt("plas", plasWeight);
            editor.putInt("metl", metlWeight);
            editor.putString("s_chem", chemStatus);
            editor.putString("s_plas", plasStatus);
            editor.putString("s_metl", metlStatus);
            editor.putString("history", historyLog);
            editor.putString("archive", archiveLog);
        }
        
        editor.putInt(currentLoggedInUser + "_chem", chemWeight);
        editor.putInt(currentLoggedInUser + "_plas", plasWeight);
        editor.putInt(currentLoggedInUser + "_metl", metlWeight);
        editor.putString(currentLoggedInUser + "_s_chem", chemStatus);
        editor.putString(currentLoggedInUser + "_s_plas", plasStatus);
        editor.putString(currentLoggedInUser + "_s_metl", metlStatus);
        editor.putString(currentLoggedInUser + "_history", historyLog);
        editor.putString(currentLoggedInUser + "_archive", archiveLog);
        editor.apply();
    }

    private void showLoginScreen() {
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.WHITE);

        LinearLayout loginCard = new LinearLayout(this);
        loginCard.setOrientation(LinearLayout.VERTICAL);
        loginCard.setPadding(50, 60, 50, 60);
        loginCard.setBackgroundResource(R.drawable.transparent_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                850,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(60, 0, 60, 0);
        loginCard.setLayoutParams(cardParams);

        TextView label = new TextView(this);
        label.setText("WASTE TRACKER SYSTEM");
        label.setTextSize(24);
        label.setTextColor(Color.parseColor("#222222"));
        label.setPadding(0, 0, 0, 40);
        label.setGravity(Gravity.CENTER_HORIZONTAL);
        loginCard.addView(label);

        final EditText user = new EditText(this);
        user.setHint("Username");
        user.setHintTextColor(Color.parseColor("#222222"));
        user.setTextColor(Color.BLACK);
        loginCard.addView(user);

        final EditText pass = new EditText(this);
        pass.setHint("Password");
        pass.setHintTextColor(Color.parseColor("#222222"));
        pass.setTextColor(Color.BLACK);
        pass.setInputType(129);
        loginCard.addView(pass);

        Button loginBtn = new Button(this);
        loginBtn.setText("LOG IN");
        loginBtn.setBackgroundColor(Color.parseColor("#1976D2"));
        loginBtn.setTextColor(Color.WHITE);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUser = user.getText().toString().trim();
                String inputPass = pass.getText().toString().trim();

                if (inputUser.equals("masteradmin") && inputPass.equals("master123")) {
                    showMasterAdminScreen();
                    return;
                }

                String registeredUser = prefs.getString("reg_user_" + inputUser, inputUser.equals("admin") ? "admin" : "");
                String registeredPass = prefs.getString("reg_pass_" + inputUser, inputUser.equals("admin") ? "1234" : "");

                if(!registeredUser.isEmpty() && inputUser.equals(registeredUser) && inputPass.equals(registeredPass)) {
                    currentLoggedInUser = inputUser; 
                    loadSystemData();              
                    showDashboard();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loginCard.addView(loginBtn);

        LinearLayout linkRow = new LinearLayout(this);
        linkRow.setOrientation(LinearLayout.HORIZONTAL);
        linkRow.setGravity(Gravity.CENTER_HORIZONTAL);
        linkRow.setPadding(0, 30, 0, 0);

        TextView forgotPasswordTxt = new TextView(this);
        forgotPasswordTxt.setText("Forgot Password?");
        forgotPasswordTxt.setTextColor(Color.BLUE);
        forgotPasswordTxt.setTextSize(13);
        forgotPasswordTxt.setPaintFlags(forgotPasswordTxt.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        forgotPasswordTxt.setOnClickListener(v -> {
            String targetUser = user.getText().toString().trim();
            if(targetUser.isEmpty()) {
                Toast.makeText(MainActivity.this, "Type username first in the field above", Toast.LENGTH_LONG).show();
                return;
            }
            if (targetUser.equals("masteradmin")) {
                Toast.makeText(MainActivity.this, "Master password cannot be recovered.", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentUser = prefs.getString("reg_user_" + targetUser, targetUser.equals("admin") ? "admin" : null);
            String currentPass = prefs.getString("reg_pass_" + targetUser, targetUser.equals("admin") ? "1234" : null);
            
            if (currentUser != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Account Reminder")
                        .setMessage("Active profile match found:\n\nUser: " + currentUser + "\nPass: " + currentPass)
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "No profile exists for: " + targetUser, Toast.LENGTH_SHORT).show();
            }
        });

        TextView divider = new TextView(this);
        divider.setText("  |  ");
        divider.setTextColor(Color.GRAY);

        TextView signUpTxt = new TextView(this);
        signUpTxt.setText("Create Account");
        signUpTxt.setTextColor(Color.parseColor("#2E7D32"));
        signUpTxt.setTextSize(13);
        signUpTxt.setPaintFlags(signUpTxt.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        signUpTxt.setOnClickListener(v -> showSignUpDialog());

        linkRow.addView(forgotPasswordTxt);
        linkRow.addView(divider);
        linkRow.addView(signUpTxt);
        loginCard.addView(linkRow);

        mainLayout.addView(loginCard);
        setContentView(mainLayout);
    }

    private void showSignUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Up Registration");

        LinearLayout formContainer = new LinearLayout(this);
        formContainer.setOrientation(LinearLayout.VERTICAL);
        formContainer.setPadding(50, 30, 50, 10);

        final EditText newExtUser = new EditText(this);
        newExtUser.setHint("Set New Username");
        formContainer.addView(newExtUser);

        final EditText newExtPass = new EditText(this);
        newExtPass.setHint("Set New Password");
        newExtPass.setInputType(129);
        formContainer.addView(newExtPass);

        builder.setView(formContainer);
        builder.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String u = newExtUser.getText().toString().trim();
                String p = newExtPass.getText().toString().trim();

                if (u.equalsIgnoreCase("masteradmin")) {
                    Toast.makeText(MainActivity.this, "Username reserved by system admin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!u.isEmpty() && !p.isEmpty()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("reg_user_" + u, u);
                    editor.putString("reg_pass_" + u, p);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Account " + u + " Created!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showMasterAdminScreen() {
        mainLayout.removeAllViews();
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 40, 40, 40);
        container.setBackgroundColor(Color.parseColor("#ECEFF1")); 

        TextView title = new TextView(this);
        title.setText("MASTER DATABASE MANAGEMENT");
        title.setTextSize(20);
        title.setTextColor(Color.parseColor("#37474F"));
        title.setPadding(0, 0, 0, 30);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        container.addView(title);

        Set<String> detectedUsers = new HashSet<>();
        detectedUsers.add("admin"); 
        
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("reg_user_")) {
                detectedUsers.add(entry.getValue().toString());
            }
        }

        for (String uName : detectedUsers) {
            LinearLayout accountCard = new LinearLayout(this);
            accountCard.setOrientation(LinearLayout.VERTICAL);
            accountCard.setPadding(30, 30, 30, 30);
            accountCard.setBackgroundColor(Color.WHITE);
            
            LinearLayout.LayoutParams cardMargin = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardMargin.setMargins(0, 0, 0, 30);
            accountCard.setLayoutParams(cardMargin);

            int uChem, uPlas, uMetl;
            String uSChem, uSPlas, uSMetl, uHist, uTime;

            // Extract the user data along with their real-time timestamp saved key
            uTime = prefs.getString(uName + "_timestamp", "Never Updated");

            if (uName.equals("admin")) {
                uChem = prefs.getInt("chem", prefs.getInt("admin_chem", 0));
                uPlas = prefs.getInt("plas", prefs.getInt("admin_plas", 0));
                uMetl = prefs.getInt("metl", prefs.getInt("admin_metl", 0));
                uSChem = prefs.getString("s_chem", prefs.getString("admin_s_chem", "None"));
                uSPlas = prefs.getString("s_plas", prefs.getString("admin_s_plas", "None"));
                uSMetl = prefs.getString("s_metl", prefs.getString("admin_s_metl", "None"));
                uHist = prefs.getString("history", prefs.getString("admin_history", "No records yet."));
            } else {
                uChem = prefs.getInt(uName + "_chem", 0);
                uPlas = prefs.getInt(uName + "_plas", 0);
                uMetl = prefs.getInt(uName + "_metl", 0);
                uSChem = prefs.getString(uName + "_s_chem", "None");
                uSPlas = prefs.getString(uName + "_s_plas", "None");
                uSMetl = prefs.getString(uName + "_s_metl", "None");
                uHist = prefs.getString(uName + "_history", "No records yet.");
            }

            TextView userTitle = new TextView(this);
            userTitle.setText("👤 USER ACCOUNT: " + uName.toUpperCase());
            userTitle.setTextSize(15);
            userTitle.setTextColor(Color.parseColor("#1976D2"));
            accountCard.addView(userTitle);

            // --- REAL-TIME DATA AND TIME STAMP VIEW ELEMENT ---
            TextView dataTimestamp = new TextView(this);
            dataTimestamp.setText("📅 Last Updated: " + uTime);
            dataTimestamp.setTextSize(11);
            dataTimestamp.setTextColor(Color.parseColor("#757575"));
            dataTimestamp.setPadding(0, 0, 0, 15);
            accountCard.addView(dataTimestamp);

            TextView metricsSummary = new TextView(this);
            metricsSummary.setText("🔴 Chem: " + uChem + "kg [" + uSChem + "]\n" +
                                   "🟢 Plas: " + uPlas + "kg [" + uSPlas + "]\n" +
                                   "🟡 Metl: " + uMetl + "kg [" + uSMetl + "]");
            metricsSummary.setTextSize(13);
            metricsSummary.setTextColor(Color.BLACK);
            metricsSummary.setPadding(10, 0, 0, 15);
            accountCard.addView(metricsSummary);

            TextView historyTitle = new TextView(this);
            historyTitle.setText("📋 Action Logs:");
            historyTitle.setTextSize(11);
            historyTitle.setTextColor(Color.GRAY);
            accountCard.addView(historyTitle);

            TextView actionsLog = new TextView(this);
            actionsLog.setText(uHist);
            actionsLog.setTextSize(11);
            actionsLog.setTextColor(Color.DKGRAY);
            actionsLog.setPadding(10, 0, 0, 0);
            accountCard.addView(actionsLog);

            container.addView(accountCard);
        }

        Button backBtn = new Button(this);
        backBtn.setText("LOG OUT MANAGEMENT");
        backBtn.setBackgroundColor(Color.parseColor("#B71C1C"));
        backBtn.setTextColor(Color.WHITE);
        backBtn.setOnClickListener(v -> {
            mainLayout.removeAllViews();
            showLoginScreen();
        });
        container.addView(backBtn);

        Button wipeBtn = new Button(this);
        wipeBtn.setText("WIPE SYSTEM DATABASE");
        wipeBtn.setBackgroundColor(Color.BLACK);
        wipeBtn.setTextColor(Color.RED);
        
        LinearLayout.LayoutParams wipeBtnMargin = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wipeBtnMargin.setMargins(0, 30, 0, 0);
        wipeBtn.setLayoutParams(wipeBtnMargin);

        wipeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Factory Reset Data?")
                    .setMessage("This will completely clear all created user accounts, tracking metrics, and historical log strings. This cannot be undone.")
                    .setPositiveButton("PERMANENTLY WIPE", (dialog, which) -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.apply();
                        
                        Toast.makeText(MainActivity.this, "System Cleaned. All Data Erased.", Toast.LENGTH_LONG).show();
                        mainLayout.removeAllViews();
                        showLoginScreen();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        container.addView(wipeBtn);

        scroll.addView(container);
        mainLayout.addView(scroll);
        setContentView(mainLayout);
    }

    private void showDashboard() {
        mainLayout.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 40, 40, 40);
        container.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("LIFECYCLE CONTROL PANEL (" + currentLoggedInUser.toUpperCase() + ")");
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        container.addView(title);

        final EditText inChem = createInput("Add Chemical (kg)");
        final EditText inPlas = createInput("Add Plastic (kg)");
        final EditText inMetl = createInput("Add Metal (kg)");
        container.addView(inChem); container.addView(inPlas); container.addView(inMetl);

        Button updateBtn = new Button(this);
        updateBtn.setText("UPDATE QUANTITIES");
        updateBtn.setBackgroundColor(Color.parseColor("#1976D2"));
        updateBtn.setTextColor(Color.WHITE);
        container.addView(updateBtn);

        container.addView(createStatusRow("CHEM", "chem"));
        container.addView(createStatusRow("PLAS", "plas"));
        container.addView(createStatusRow("METL", "metl"));

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setPadding(0, 30, 0, 0);

        Button saveBtn = new Button(this);
        saveBtn.setText("SAVE DATA");
        saveBtn.setOnClickListener(v -> {
            saveSystemData();
            Toast.makeText(this, "Progress Saved for " + currentLoggedInUser, Toast.LENGTH_SHORT).show();
        });

        Button resetBtn = new Button(this);
        resetBtn.setText("NEW SESSION");
        resetBtn.setTextColor(Color.RED);
        resetBtn.setOnClickListener(v -> confirmReset());

        Button logoutBtn = new Button(this);
        logoutBtn.setText("LOG OUT");
        logoutBtn.setTextColor(Color.BLACK);
        logoutBtn.setOnClickListener(v -> {
            mainLayout.removeAllViews();
            showLoginScreen();
        });

        actionRow.addView(saveBtn);
        actionRow.addView(resetBtn);
        actionRow.addView(logoutBtn);
        container.addView(actionRow);

        final TextView display = new TextView(this);
        display.setPadding(0, 40, 0, 20);
        display.setTextSize(16);
        display.setTextColor(Color.BLACK);

        final TextView historyDisplay = new TextView(this);
        historyDisplay.setTextSize(12);
        historyDisplay.setTextColor(Color.DKGRAY);

        updateUI(display, historyDisplay);
        container.addView(display);
        container.addView(historyDisplay);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = getVal(inChem); int p = getVal(inPlas); int m = getVal(inMetl);
                if (c > 0 || p > 0 || m > 0) {
                    chemWeight += c; plasWeight += p; metlWeight += m;
                    historyLog = "Added: +" + c + "c, +" + p + "p, +" + m + "m\n" + historyLog;
                    saveSystemData(); 
                    updateUI(display, historyDisplay);
                    inChem.setText(""); inPlas.setText(""); inMetl.setText("");
                }
            }
        });

        scroll.addView(container);
        setContentView(scroll);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Archive & Reset?")
                .setMessage("Current data will move to ARCHIVE and reset to 0kg.")
                .setPositiveButton("Proceed", (dialog, which) -> {
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                            java.util.Locale.getDefault()).format(new java.util.Date());

                    archiveLog = "\n--- Archived Session (" + timestamp + ") ---\nC: " + chemWeight + "kg [" + chemStatus +
                            "]\nP: " + plasWeight + "kg [" + plasStatus +
                            "]\nM: " + metlWeight + "kg [" + metlStatus + "]\n" + archiveLog;
                    chemWeight = 0; plasWeight = 0; metlWeight = 0;
                    chemStatus = "None"; plasStatus = "None"; metlStatus = "None";
                    historyLog = "New Session Started.";
                    saveSystemData(); showDashboard();
                }).setNegativeButton("Cancel", null).show();
    }

    private LinearLayout createStatusRow(final String label, final String type) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView labelTxt = new TextView(this);
        labelTxt.setText(label + ": ");
        labelTxt.setTextSize(11);
        labelTxt.setTextColor(Color.BLACK);
        row.addView(labelTxt);
        String[] opts = {"Factory", "Recycled", "Disposed"};
        for (final String s : opts) {
            Button b = new Button(this);
            b.setText(s); b.setTextSize(8);
            b.setOnClickListener(v -> {
                if(type.equals("chem")) chemStatus = s;
                else if(type.equals("plas")) plasStatus = s;
                else metlStatus = s;
                historyLog = label + " -> " + s + "\n" + historyLog;
                saveSystemData(); showDashboard();
            });
            row.addView(b);
        }
        return row;
    }

    private int getVal(EditText et) {
        String s = et.getText().toString();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    private EditText createInput(String hint) {
        EditText et = new EditText(this);
        et.setHint(hint); et.setInputType(2);
        et.setHintTextColor(Color.GRAY);
        et.setTextColor(Color.BLACK);
        return et;
    }

    private void updateUI(TextView tv, TextView hist) {
        tv.setText("CURRENT METRICS:\n 🔴  Chem: " + chemWeight + "kg [" + chemStatus +
                "]\n 🟢  Plas: " + plasWeight + "kg [" + plasStatus +
                "]\n 🟡  Metl: " + metlWeight + "kg [" + metlStatus + "]");
        hist.setText("HISTORY & ARCHIVE:\n" + historyLog + "\n\n" + archiveLog);
    }
}
