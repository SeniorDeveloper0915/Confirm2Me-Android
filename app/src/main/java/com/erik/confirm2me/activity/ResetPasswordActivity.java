package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ResetPasswordActivity extends Activity implements View.OnClickListener {

    private static String TAG = "RestPasswordActivity";
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button resetButton;

    private ProgressDialog mProgressDialog;
    public static JSONObject mResetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtConfirmPassword = (EditText)findViewById(R.id.txtConfirmPassword);
        resetButton = (Button)findViewById(R.id.btnResetPassword);
        resetButton.setOnClickListener(this);
        resetButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
//        Global.requestQueue = Volley.newRequestQueue(getApplicationContext());
        try {
            txtEmail.setText(mResetUser.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

         if (v == resetButton) {

            // Progress SignUp

            final String email = txtEmail.getText().toString();
            final String password = txtPassword.getText().toString();
            final String confirmPassword = txtConfirmPassword.getText().toString();

            if (password == null || password.length() == 0) {
                Toast.makeText(ResetPasswordActivity.this, "Password is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (confirmPassword == null || confirmPassword.length() == 0) {
                Toast.makeText(ResetPasswordActivity.this, "Confirm Password is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Password does not match with Confirm Password!", Toast.LENGTH_LONG).show();
                return;
            }

            mProgressDialog.show();

            Global.url = Global.baseUrl + Global.resetPassword;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            Global.params.put("password"   , password);
             try {
                 Global.params.put("idx"        , mResetUser.getString("id"));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             String token = FirebaseInstanceId.getInstance().getToken();
            Global.params.put("token", token);
            Toast.makeText(ResetPasswordActivity.this, token, Toast.LENGTH_LONG).show();
            Global.params.setUseJsonStreamer(true);

             Global.client.put( Global.url, Global.params, new TextHttpResponseHandler() {
                 @Override
                 public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                     mProgressDialog.dismiss();
//                            Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                 }

                 @Override
                 public void onSuccess(int statusCode, Header[] headers, String responseString) {
                     JSONObject response = null;
                     try {
                         response = new JSONObject(responseString);
                         if (response.getBoolean("Success") == true) {
                             new AlertDialog.Builder(ResetPasswordActivity.this)
                                     .setMessage("Password Reset!")
                                     .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
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
