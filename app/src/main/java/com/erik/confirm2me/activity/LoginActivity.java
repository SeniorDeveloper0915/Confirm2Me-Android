package com.erik.confirm2me.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;

import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.Throwable;
import java.lang.Object;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends Activity implements View.OnClickListener {

    private static String TAG = "LoginActivity";
    private EditText txtUsername;
    private EditText txtPassword;
    private Button loginButton;
    private Button siginupButton;
    private Button forgotPasswordButton;
    private ProgressDialog mProgressDialog;
    public static final String mypreference = "login";
    public static final String CAMERA_RECORD_PREF = "camera_record_pref";
    public static final String ALLOW_KEY = "ALLOWED";
    public static final int MY_PERMISSION_REQUEST = 100;
    public static final int MAX_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        loginButton = (Button) findViewById(R.id.btnLogin);
        siginupButton = (Button) findViewById(R.id.btnSignup);
        forgotPasswordButton = (Button) findViewById(R.id.btnForgotPassword);

        loginButton.setOnClickListener(this);
        loginButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        siginupButton.setOnClickListener(this);
        siginupButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        forgotPasswordButton.setOnClickListener(this);
        forgotPasswordButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

        SharedPreferences loginPreference = getSharedPreferences(mypreference, 0);
        if (loginPreference.contains("loginName")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        // Go Button
        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    goLogin();
                    return true;
                }
                return false;
            }
        });


        // hide keyboard for outside of the view
        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    showAlert();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST);
                }
            }
        }
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_RECORD_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_RECORD_PREF, Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the camera && record audio && external storage");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(LoginActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MY_PERMISSION_REQUEST);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the camera & audio & external storage");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(LoginActivity.this);
                    }
                });
        alertDialog.show();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    @Override
    public void onClick(View v) {
        if (v == loginButton) {
            // Progress Login
            goLogin();
        } else if (v == siginupButton) {
            // Go to SignUp
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        } else if (v == forgotPasswordButton) {
            // Go to Forgot Password
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        }
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            saveToPreferences(LoginActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }
        }
    }

    private void goLogin() {
        final String userName = txtUsername.getText().toString();
        final String password = txtPassword.getText().toString();

        if (userName == null || userName.length() == 0) {
            Toast.makeText(LoginActivity.this, "Username is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (password == null || password.length() == 0) {
            Toast.makeText(LoginActivity.this, "Password is missing!", Toast.LENGTH_LONG).show();
            return;
        }

        mProgressDialog.show();
        Global.url = Global.baseUrl + Global.loginUrl;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("userName", userName);
        Global.params.put("password", password);
        Global.params.setUseJsonStreamer(true);
        Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                mProgressDialog.hide();
                JSONObject response = null;
                try {
                    response = new JSONObject(responseString);
                    int responseCode = response.getInt("Code");

                    if (responseCode == 401) {
                        Toast.makeText(LoginActivity.this, "Password does not match", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 402) {
                        Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 403) {
                        Toast.makeText(LoginActivity.this, "Error Ocurred", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 200) {
                        JSONArray user = response.getJSONArray("User");

                        SharedPreferences loginPreference = getSharedPreferences(mypreference, 0);
                        SharedPreferences.Editor loginEditor = loginPreference.edit();
                        loginEditor.putString("loginName", user.getJSONObject(0).getString("userName"));
                        loginEditor.putString("firstname", user.getJSONObject(0).getString("firstname"));
                        loginEditor.putString("lastname", user.getJSONObject(0).getString("lastname"));
                        loginEditor.putString("phonenumber", user.getJSONObject(0).getString("phonenumber"));
                        loginEditor.putString("email", user.getJSONObject(0).getString("email"));
                        loginEditor.putString("isLoggedIn", "true");
                        loginEditor.putString("id", user.getJSONObject(0).get("id").toString());
                        loginEditor.putString("PIN", user.getJSONObject(0).getString("PIN"));

                        loginEditor.commit();
                        Global.url = Global.baseUrl + Global.changeToken;
                        Global.client = new AsyncHttpClient(true, 80, 443);
                        Global.params = new RequestParams();
                        Global.params.put("Token", FirebaseInstanceId.getInstance().getToken());
                        Global.params.put("idx", user.getJSONObject(0).getString("id"));
                        Global.params.setUseJsonStreamer(true);


                        Global.client.put( Global.url, Global.params, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            }
                        });

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
