package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SignupActivity extends Activity implements View.OnClickListener {

    private static String TAG = "SignupActivity";
    private EditText txtFirstname;
    private EditText txtLastname;
    private EditText txtUsername;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private EditText txtMobilePhone;
    private EditText txtPin;
    private Button siginupButton;
    private Button cancelButton;
    private Button doneButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtFirstname = (EditText)findViewById(R.id.txtFirstname);
        txtLastname = (EditText)findViewById(R.id.txtLastname);
        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtConfirmPassword = (EditText)findViewById(R.id.txtConfirmPassword);
        txtMobilePhone = (EditText)findViewById(R.id.txtMobilePhone);
        txtPin = (EditText)findViewById(R.id.txtPin);

        siginupButton = (Button)findViewById(R.id.btnSignup);
        siginupButton.setOnClickListener(this);
        siginupButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        cancelButton = (Button)findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(this);
        cancelButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        doneButton = (Button)findViewById(R.id.btnDone);
        doneButton.setOnClickListener(this);
        doneButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        Global.requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        if (v == cancelButton) {
            finish();
        }
        else if (v == doneButton) {
            // Hide Keyboards
            InputMethodManager imm = (InputMethodManager) getSystemService(SignupActivity.this.INPUT_METHOD_SERVICE);
            if(txtFirstname.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtFirstname.getWindowToken(), 0);
            else if(txtLastname.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtLastname.getWindowToken(), 0);
            else if(txtUsername.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtUsername.getWindowToken(), 0);
            else if(txtEmail.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtEmail.getWindowToken(), 0);
            else if(txtPassword.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
            else if(txtConfirmPassword.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtConfirmPassword.getWindowToken(), 0);
            else if(txtMobilePhone.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtMobilePhone.getWindowToken(), 0);
            else if(txtPin.getWindowToken() != null)
                imm.hideSoftInputFromWindow(txtPin.getWindowToken(), 0);
        }
        else if (v == siginupButton) {

            // Progress SignUp

            final String firstName = txtFirstname.getText().toString();
            final String lastName = txtLastname.getText().toString();
            final String userName = txtUsername.getText().toString();
            final String email = txtEmail.getText().toString();
            final String password = txtPassword.getText().toString();
            final String confirmPassword = txtConfirmPassword.getText().toString();
            final String mobilePhone = txtMobilePhone.getText().toString();
            final String pin = txtPin.getText().toString();

            if (firstName == null || firstName.length() == 0) {
                Toast.makeText(SignupActivity.this, "Firstname is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (lastName == null || lastName.length() == 0) {
                Toast.makeText(SignupActivity.this, "Lastname is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (userName == null || userName.length() == 0) {
                Toast.makeText(SignupActivity.this, "Username is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (email == null || email.length() == 0) {
                Toast.makeText(SignupActivity.this, "Email is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignupActivity.this, "Email is invalid format!", Toast.LENGTH_LONG).show();
                return;
            }
            if (password == null || password.length() == 0) {
                Toast.makeText(SignupActivity.this, "Password is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (confirmPassword == null || confirmPassword.length() == 0) {
                Toast.makeText(SignupActivity.this, "Confirm Password is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Password does not match with Confirm Password!", Toast.LENGTH_LONG).show();
                return;
            }
            if (mobilePhone == null || mobilePhone.length() == 0) {
                Toast.makeText(SignupActivity.this, "Mobile phone# is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (pin == null || pin.length() == 0) {
                Toast.makeText(SignupActivity.this, "PIN is missing!", Toast.LENGTH_LONG).show();
                return;
            }
            if (pin.length() < 4 || pin.length() > 6) {
                Toast.makeText(SignupActivity.this, "PIN must contains 4~6 digits only!", Toast.LENGTH_LONG).show();
                return;
            }

            mProgressDialog.show();

            Global.url = Global.baseUrl + Global.registraionUrl;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            Global.params.put("userName"   , userName);
            Global.params.put("password"   , password);
            Global.params.put("email"      , email);
            Global.params.put("firstname"  , firstName);
            Global.params.put("lastname"   , lastName);
            Global.params.put("phonenumber", mobilePhone);
            Global.params.put("PIN"        , pin);
            String token = FirebaseInstanceId.getInstance().getToken();
            Global.params.put("Token", token);
            Toast.makeText(SignupActivity.this, token, Toast.LENGTH_LONG).show();
            Global.params.setUseJsonStreamer(true);

            Global.client.post(Global.url, Global.params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                    // Handle resulting parsed JSON response here
                    mProgressDialog.hide();
                    int responseCode = 0;
                    try {
                        responseCode = response.getInt("Code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (responseCode == 400) {
                        Toast.makeText(SignupActivity.this, "Username is conflicted", Toast.LENGTH_LONG).show();
                    } else  if (responseCode == 401) {
                        Toast.makeText(SignupActivity.this, "Email is conflicted", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 403) {
                        Toast.makeText(SignupActivity.this, "Error ocurred", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 200) {
                        new AlertDialog.Builder(SignupActivity.this)
                                .setMessage("Account Verified!")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }
            });
        }
    }
}
