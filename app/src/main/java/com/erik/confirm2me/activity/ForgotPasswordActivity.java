package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;


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
//
//            ParseQuery<ParseUser> query = ParseUser.getQuery();
//            query.whereEqualTo("email", email);
//            query.findInBackground(new FindCallback<ParseUser>() {
//                public void done(List<ParseUser> objects, ParseException e) {
//                    if (e == null) {
//                        // The query was successful.
//                        if (objects.size() > 0) {
//                            // user is exists
//                            ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
//
//                                @Override
//                                public void done(ParseException e) {
//                                    mProgressDialog.dismiss();
//                                    if (e == null) {
//                                        new AlertDialog.Builder(ForgotPasswordActivity.this)
//                                                .setMessage("We will send the reset password link to your email address soon!")
//                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        finish();
//                                                    }
//                                                })
//                                                .show();
//                                    } else {
//                                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            });
//
//                        } else {
//                            mProgressDialog.dismiss();
//                            Toast.makeText(ForgotPasswordActivity.this, "This email is not exists", Toast.LENGTH_LONG).show();
//                        }
//                    } else {
//                        mProgressDialog.dismiss();
//                        Toast.makeText(ForgotPasswordActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
        }
    }
}
