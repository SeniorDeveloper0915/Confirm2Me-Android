package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.parse.ParseUser;


public class VerifyPinActivity extends Activity implements View.OnClickListener {

    private static String TAG = "VerifyPinActivity";
    private EditText txtPin;

    private Button cancelButton;
    private Button confirmButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pin);

        txtPin = (EditText)findViewById(R.id.txtPIN);

        cancelButton = (Button)findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);
        cancelButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        confirmButton = (Button)findViewById(R.id.btnConfirm);
        confirmButton.setOnClickListener(this);
        confirmButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

        // show keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if (v == cancelButton) {
            hideKeyboard();
            finish();
        }
        else if (v == confirmButton) {
            // Progress Verify PIN
            final String pin = txtPin.getText().toString();

            if (pin == null || pin.length() == 0) {
                Toast.makeText(VerifyPinActivity.this, "PIN is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (pin.length() < 4 || pin.length() > 6) {
                Toast.makeText(VerifyPinActivity.this, "PIN must contains 4~6 digits only!", Toast.LENGTH_LONG).show();
                return;
            }

//            ParseUser currentUser = ParseUser.getCurrentUser();
            SharedPreferences loginPreference = getSharedPreferences("login", 0);
            if (loginPreference.getString("PIN", "").equals(pin)) {
                hideKeyboard();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                Toast.makeText(VerifyPinActivity.this, "PIN does not match!", Toast.LENGTH_LONG).show();
            }
        }

    }
    private void hideKeyboard() {
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPin.getWindowToken(), 0);
    }
}
