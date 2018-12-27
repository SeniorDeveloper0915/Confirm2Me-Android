package com.erik.confirm2me.PromisePay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.erik.confirm2me.R;
import com.erik.confirm2me.activity.MainActivity;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONObject;


public class PromisePayTestActivity extends Activity implements View.OnClickListener {

    private static String TAG = "PromisePayTestActivity";
    private EditText txtFullName;
    private EditText txtCardNumber;
    private EditText txtCardExpMonth;
    private EditText txtCardExpYear;
    private EditText txtCardCVC;

    private Button createCardAccountButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promise_pay_test);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        txtFullName = (EditText)findViewById(R.id.txtFullName);
        txtCardNumber = (EditText)findViewById(R.id.txtCardNumber);
        txtCardExpMonth = (EditText)findViewById(R.id.txtExpMonth);
        txtCardExpYear = (EditText)findViewById(R.id.txtExpYear);
        txtCardCVC = (EditText)findViewById(R.id.txtCVC);

        txtFullName.setText("Bobby Buyer");
        txtCardNumber.setText("4111111111111111");
        txtCardExpMonth.setText("12");
        txtCardExpYear.setText("2020");
        txtCardCVC.setText("123");

        createCardAccountButton = (Button)findViewById(R.id.btnCreateCardAccount);
        createCardAccountButton.setOnClickListener(this);
        createCardAccountButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {

        if (v == createCardAccountButton) {

            // Progress Create Card Account

            final String fullname = txtFullName.getText().toString();
            final String cardNumber = txtCardNumber.getText().toString();
            final String cardExpMonth = txtCardExpMonth.getText().toString();
            final String cardExpYear = txtCardExpYear.getText().toString();
            final String cardCVV = txtCardCVC.getText().toString();


            mProgressDialog.show();
            PPCard card = new PPCard(cardNumber, fullname, cardExpMonth, cardExpYear, cardCVV);
            PromisePay promisePay = PromisePay.getInstance();
            promisePay.initialize("prelive", "cbd748a608eda8635e1f325d914080b4");
            promisePay.createCardAccount("da59a92f130dfc51719d13947330f4a2", card, new PromisePay.OnPromiseRequestListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    mProgressDialog.dismiss();
                }

                @Override
                public void onError(Exception e) {
                    mProgressDialog.dismiss();
                }
            });
        }
    }
}
