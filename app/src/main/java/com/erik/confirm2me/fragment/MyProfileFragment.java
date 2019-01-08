package com.erik.confirm2me.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.activity.LoginActivity;
import com.erik.confirm2me.activity.MainActivity;
import com.erik.confirm2me.activity.VerifyPinActivity;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by iGold on 9/28/15.
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener{

    private static final int REQUEST_VERIFY_PIN = 1;

    private EditText txtFirstname;
    private EditText txtLastname;
    private EditText txtUsername;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private EditText txtMobilePhone;
    private EditText txtPin;
    private Button logoutButton;
    private Button doneButton;
    private boolean isCreated= false;
    private boolean isEditting= false;
    private View mRootView;
    private ProgressDialog mProgressDialog;
    public static MyProfileFragment mFragment = null;
//    private SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mProgressDialog = new ProgressDialog(getActivity(), android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

        if (!isCreated || mRootView == null) {
            isCreated = true;
            mRootView = inflater.inflate(R.layout.fragment_my_profile, container, false);

            txtFirstname = (EditText) mRootView.findViewById(R.id.txtFirstname);
            txtLastname = (EditText) mRootView.findViewById(R.id.txtLastname);
            txtUsername = (EditText) mRootView.findViewById(R.id.txtUsername);
            txtEmail = (EditText) mRootView.findViewById(R.id.txtEmail);
            txtPassword = (EditText) mRootView.findViewById(R.id.txtPassword);
            txtConfirmPassword = (EditText) mRootView.findViewById(R.id.txtConfirmPassword);
            txtMobilePhone = (EditText) mRootView.findViewById(R.id.txtMobilePhone);
            txtPin = (EditText) mRootView.findViewById(R.id.txtPin);

            logoutButton = (Button) mRootView.findViewById(R.id.btnLogout);
            logoutButton.setOnClickListener(this);
            logoutButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
            doneButton = (Button) mRootView.findViewById(R.id.btnDone);
            doneButton.setOnClickListener(this);
            doneButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mFragment = this;
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        setFormEnabled(isEditting);

        ((MainActivity)getActivity()).showTabBadges();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).showTabBadges();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VERIFY_PIN && !isEditting) {
                isEditting = true;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == logoutButton) {
            if (isEditting) {
                // Cancel Editing
                isEditting = false;
                setFormEnabled(isEditting);
            }
            SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
            SharedPreferences.Editor loginEditor = loginPreference.edit();
            loginEditor.clear();
            loginEditor.commit();
            startActivity(new Intent(getContext(), LoginActivity.class));
        }
        if (v == doneButton) {
            if (isEditting) {
                // Complete Editing
                updateProfile();
            } else {
                // Try to Editting
                new BottomSheet.Builder(getActivity()).title("Choose the way to verify you").sheet(R.menu.menu_verify).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.menu_verify_pin: {
                                Global.fragment = 2;
                                getActivity().startActivityForResult(new Intent(getActivity(), VerifyPinActivity.class), REQUEST_VERIFY_PIN);
                                break;
                            }
                        }
                    }
                }).show();
            }
        }
    }

    private void setFormEnabled(boolean isEnable) {

        txtFirstname.setEnabled(isEnable);
        txtLastname.setEnabled(isEnable);
        txtUsername.setEnabled(false);
        txtEmail.setEnabled(isEnable);
        txtPassword.setEnabled(isEnable);
        txtConfirmPassword.setEnabled(isEnable);
        txtMobilePhone.setEnabled(isEnable);
        txtPin.setEnabled(isEnable);
        loadProfileInfo();
        hideAllKeyboards(getActivity());
    }

    private void loadProfileInfo() {

        // Load Profile Informations
        //ParseUser currentUser = ParseUser.getCurrentUser();
        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);

        if (loginPreference.getString("isLoggedIn", "").equals("true")) {
            txtFirstname.setText(loginPreference.getString("firstname", ""));
            txtLastname.setText(loginPreference.getString("lastname", ""));
            txtUsername.setText(loginPreference.getString("loginName", ""));
            txtEmail.setText(loginPreference.getString("email", ""));
            txtMobilePhone.setText(loginPreference.getString("phonenumber", ""));
        }

        if (isEditting) {
            doneButton.setText("Done");
            logoutButton.setText("Cancel");
        } else {
            doneButton.setText("Edit");
            logoutButton.setText("LogOut");
        }
    }

    public static void hideAllKeyboards(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void updateProfile() {

        final String firstName = txtFirstname.getText().toString();
        final String lastName = txtLastname.getText().toString();
        final String userName = txtUsername.getText().toString();
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();
        final String confirmPassword = txtConfirmPassword.getText().toString();
        final String mobilePhone = txtMobilePhone.getText().toString();
        final String pin = txtPin.getText().toString();

        if (firstName == null || firstName.length() == 0) {
            Toast.makeText(getActivity(), "Firstname is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (lastName == null || lastName.length() == 0) {
            Toast.makeText(getActivity(), "Lastname is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (userName == null || userName.length() == 0) {
            Toast.makeText(getActivity(), "Username is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (email == null || email.length() == 0) {
            Toast.makeText(getActivity(), "Email is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getActivity(), "Email is invalid format!", Toast.LENGTH_LONG).show();
            return;
        }
        if (password == null || password.length() == 0) {
            Toast.makeText(getActivity(), "Password is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (confirmPassword == null || confirmPassword.length() == 0) {
            Toast.makeText(getActivity(), "Confirm Password is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Password does not match with Confirm Password!", Toast.LENGTH_LONG).show();
            return;
        }
        if (mobilePhone == null || mobilePhone.length() == 0) {
            Toast.makeText(getActivity(), "Mobile phone# is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (pin == null || pin.length() == 0) {
            Toast.makeText(getActivity(), "PIN is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (pin.length() < 4 || pin.length() > 6) {
            Toast.makeText(getActivity(), "PIN must contains 4~6 digits only!", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);

        Global.url = Global.baseUrl + Global.updateUrl;
        Global.client = new AsyncHttpClient(true, 80, 443);
        mProgressDialog.show();
        Global.params = new RequestParams();
        Global.params.put("idx", loginPreference.getString("id", ""));
        Global.params.put("userName"   , userName);
        Global.params.put("password"   , password);
        Global.params.put("email"      , email);
        Global.params.put("firstname"  , firstName);
        Global.params.put("lastname"   , lastName);
        Global.params.put("phonenumber", mobilePhone);
        Global.params.put("PIN"        , pin);
        Global.params.setUseJsonStreamer(true);

        Global.client.put(Global.url, Global.params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                mProgressDialog.dismiss();
                try {
                    JSONObject response = new JSONObject(responseString);
                    if (response.getBoolean("Success") == true) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Account Updated!")
                                .setNeutralButton("OK", null)
                                .show();

                        isEditting = false;
                        setFormEnabled(isEditting);
                        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
                        SharedPreferences.Editor loginEditor = loginPreference.edit();
                        loginEditor.putString("loginName", userName);
                        loginEditor.putString("firstname", firstName);
                        loginEditor.putString("lastname", lastName);
                        loginEditor.putString("phonenumber", mobilePhone);
                        loginEditor.putString("email", email);
                        loginEditor.putString("isLoggedIn", "true");
                        loginEditor.putString("PIN", pin);
                        loginEditor.commit();

                        txtFirstname.setText(loginPreference.getString("firstname", ""));
                        txtLastname.setText(loginPreference.getString("lastname", ""));
                        txtUsername.setText(loginPreference.getString("loginName", ""));
                        txtEmail.setText(loginPreference.getString("email", ""));
                        txtMobilePhone.setText(loginPreference.getString("phonenumber", ""));

                    } else {
                        Toast.makeText(getActivity(), "Unable to update User. Try again.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }
}
