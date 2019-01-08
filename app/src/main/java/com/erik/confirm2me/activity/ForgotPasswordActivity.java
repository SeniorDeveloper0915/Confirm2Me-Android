package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.internal.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class ForgotPasswordActivity extends Activity implements View.OnClickListener {

    private static String TAG = "ForgotPasswordActivity";
    private EditText txtEmail;
    private Button cancelButton;
    private Button resetButton;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmail = (EditText)findViewById(R.id.txtEmail);

        cancelButton = (Button)findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);
        cancelButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        resetButton = (Button)findViewById(R.id.btnReset);
        resetButton.setOnClickListener(this);
        resetButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {

        if (v == cancelButton) {
            finish();
        }
        else if (v == resetButton) {
            // Progress Forgot Password
            final String email = txtEmail.getText().toString();

            if (email == null || email.length() == 0) {
                Toast.makeText(ForgotPasswordActivity.this, "Email is missing!", Toast.LENGTH_LONG).show();
                return;
            }

            // Hide Keyboards
            InputMethodManager imm = (InputMethodManager) getSystemService(ForgotPasswordActivity.this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtEmail.getWindowToken(), 0);

            mProgressDialog.show();
            Global.url = Global.baseUrl + Global.emailUrl;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            Global.params.put("email", email);
            Global.params.setUseJsonStreamer(true);

            Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                    Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    JSONObject response = null;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("Success") == true) {
                            mProgressDialog.dismiss();
                            JSONObject user = null;
                            user = response.getJSONArray("User").getJSONObject(0);
                            ResetPasswordActivity.mResetUser = user;
                            startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class));
                        } else {
                            new AlertDialog.Builder(ForgotPasswordActivity.this)
                                    .setMessage("There is not User!")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
