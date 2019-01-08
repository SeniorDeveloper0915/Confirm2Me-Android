package com.erik.confirm2me.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.erik.confirm2me.AppData;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.activity.LoginActivity;
import com.erik.confirm2me.activity.MainActivity;
import com.erik.confirm2me.activity.MessageReadActivity;
import com.erik.confirm2me.activity.SignupActivity;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.erik.confirm2me.helper.BillingService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.erik.confirm2me.activity.MessageReadActivity.MY_PERMISSION_REQUEST;

/**
 * Created by iGold on 9/28/15.
 */
public class NewFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CHOOSE_LIBRARY = 2;
    private static final int REQUEST_CHOOSE_LOCATION = 3;

    private Button mSemdButton;
    private ImageButton mCategoryButton;
    private EditText mMobileNumber;
    private TextView mCategoryTitle;
    private EditText mCategoryDescription;
    private Spinner mCaregorySpinner;
    private CategoryAdapter mAdapter;
    private static JSONArray categoryList = null;
    public static NewFragment mFragment = null;
    private View mRootView;
    private ProgressDialog mProgressDialog;
    String TEXT_OWN_CATEGORY = "Enter Your Own";
    IInAppBillingService mService;
    ServiceConnection mServiceConn;

    private static final int PERMISSION_REQUEST_CODE = 1;
    public static final String SMS_PREF = "sms_pref";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mProgressDialog = new ProgressDialog(getActivity(), android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

        mRootView = inflater.inflate(R.layout.fragment_new, container, false);
        mSemdButton = (Button) mRootView.findViewById(R.id.btnSend);
        mCategoryButton = (ImageButton) mRootView.findViewById(R.id.btnCategory);
        mMobileNumber = (EditText) mRootView.findViewById(R.id.txtMobilePhone);
        mCategoryTitle = (TextView) mRootView.findViewById(R.id.txtCategoryTitle);
        mCategoryDescription = (EditText) mRootView.findViewById(R.id.txtCategoryDescription);
        mCaregorySpinner = (Spinner)mRootView.findViewById(R.id.caregorySpinner);

        mAdapter = new CategoryAdapter();
        mCaregorySpinner.setAdapter(mAdapter);
        mCaregorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chooseCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setButtonStyle(mSemdButton);
        setButtonStyle(mCategoryButton);
        initBilling();
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mFragment = this;
        loadCategories();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        loadCategories();
        super.onResume();
        ((MainActivity)getActivity()).showTabBadges();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).showTabBadges();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null)
        {
            getActivity().unbindService(mServiceConn);
        }
        Crouton.cancelAllCroutons();
    }

    @Override
    public void onClick(View v) {
        if (v == mSemdButton) {
//            buyProduct();
            onSend();
        } else if (v == mCategoryButton) {
            mCaregorySpinner.performClick();
        }
    }

    private void onSend()
    {
        final String mobilePhone = mMobileNumber.getText().toString();
        final String categoryTitle = mCategoryTitle.getText().toString();
        final String categoryDescription = mCategoryDescription.getText().toString();
        if (mobilePhone == null || mobilePhone.length() == 0) {
            Toast.makeText(getActivity(), "Mobile phone# is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (categoryTitle == null || categoryTitle.length() == 0) {
            Toast.makeText(getActivity(), "Category is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        if (categoryDescription == null || categoryDescription.length() == 0) {
            Toast.makeText(getActivity(), "Request message is missing!", Toast.LENGTH_LONG).show();
            return;
        }
        mProgressDialog.show();
        Global.url = Global.baseUrl + Global.findByPhone;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("phonenumber", mobilePhone);
        Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.dismiss();
                        JSONObject response = null;
                        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
                        try {
                            response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                JSONObject user = response.getJSONArray("User").getJSONObject(0);
                                if (user.getString("phonenumber").equals(loginPreference.getString("phonenumber", "")) == true) {
                                    Toast.makeText(getActivity(), "You cannot send Affidavit Request for yourself!", Toast.LENGTH_SHORT).show();
                                } else {
                                    saveNewRequest(user);
                                }
                            } else {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("This phonenumber is not registered in this app")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SmsManager smsManager = SmsManager.getDefault();
                                                smsManager.sendTextMessage(mobilePhone, null, categoryDescription, null, null);
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

    private void saveNewRequest(final JSONObject provider) {

        if (mCategoryTitle.getText().toString().equals(TEXT_OWN_CATEGORY))
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            final EditText edittext = new EditText(getActivity());
            alert.setMessage("Would you like to save this new category?");
            alert.setTitle(null);
            alert.setView(edittext);
            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    final String newCategory = edittext.getText().toString();
                    // Save New Category
                    final SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
                    Global.url = Global.baseUrl + Global.addCategoryUrl;
                    Global.client = new AsyncHttpClient(true, 80, 443);
                    Global.params = new RequestParams();
                    Global.params.put("no", 1000);
                    Global.params.put("category", newCategory);
                    Global.params.put("description", mCategoryDescription.getText().toString());
                    Global.params.put("owner", loginPreference.getString("loginName", ""));
                    Global.params.setUseJsonStreamer(true);
                    mProgressDialog.show();
                    Global.client.post(Global.url, Global.params, new TextHttpResponseHandler() {
                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                    Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                    mProgressDialog.hide();
                                    JSONObject response = null;
                                    try {
                                        response = new JSONObject(responseString);
                                        if (response.getBoolean("Success") == true) {
                                            loadCategories();
                                            saveNewRequest(provider, newCategory);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            });
            alert.show();
        }
        else
        {
            saveNewRequest(provider, mCategoryTitle.getText().toString());
        }
    }

    private void saveNewRequest(final JSONObject user, String affidavit)
    {
        mProgressDialog.show();
        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
        Global.url = Global.baseUrl + Global.newRequestUrl;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("Requester"                  , loginPreference.getString("loginName", ""));
        Global.params.put("affidavit_category"         , affidavit);
        Global.params.put("affidavit_description"      , mCategoryDescription.getText().toString());
        Global.params.put("sender_status"              , Global.kSenderStatusSubmitted);
        Global.params.put("receiver_status"            , Global.kProviderStatusPending);
        Global.params.put("sender_mail_unread"         , false);
        Global.params.put("receiver_mail_unread"       , false);

        if (user != null) {
            try {
                Global.params.put("provider_pNumber"       , user.get("userName").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Global.params.put("provider_pNumber"       , mMobileNumber.getText().toString());
        }
        Global.params.setUseJsonStreamer(true);
        Global.client.post(Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.hide();
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                mMobileNumber.setText(null);
                                mCategoryTitle.setText(null);
                                mCategoryDescription.setText(null);
                                Toast.makeText(getActivity(), "Successfully Submitted!", Toast.LENGTH_SHORT).show();
                                sendPushnotification(user);
                                startActivity(new Intent(getContext(), GivenFragment.class));
                            } else {
                                Toast.makeText(getActivity(), response.getString("Message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void sendPushnotification(JSONObject toUser) {
        String pushMsg = null;
        SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
        pushMsg = String.format("%s %s has sent you a new Affidavit Request!", loginPreference.getString("firstname", ""), loginPreference.getString("lastname", ""));

        Global.client = new AsyncHttpClient();
        Global.client.addHeader("Content-Type", "application/json");
        Global.client.addHeader("Authorization", Global.key);
        JSONObject jsonParams = new JSONObject();
        StringEntity jsonEntity = null;

        try {
            jsonParams.put("to", toUser.getString("FCM_Token"));
            JSONObject notification = new JSONObject();
            notification.put("title", "Confirm2Me");
            notification.put("body", pushMsg);
            jsonParams.put("notification", notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonEntity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Global.client.post(getContext(), "https://fcm.googleapis.com/fcm/send", jsonEntity, "application/json",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                });
    }

    // Load Categories
    private void loadCategories() {
        mProgressDialog.show();
        final SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
        Global.url = Global.baseUrl + Global.sortedCategoryUrl;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("owner", loginPreference.getString("loginName", ""));
        Global.params.setUseJsonStreamer(true);
        Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(getContext(), "GetCategories failed", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.dismiss();
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                if (response.getInt("Code") == 200){
                                    categoryList = response.getJSONArray("Categories");
                                    JSONObject ownCategory = new JSONObject();
                                    ownCategory.put("category", TEXT_OWN_CATEGORY);
                                    ownCategory.put("description", "");
                                    ownCategory.put("owner", loginPreference.getString("loginName", ""));
                                    categoryList.put(ownCategory);
                                    mAdapter.notifyDataSetChanged();
                                    chooseCategory(0);
                                } else if (response.getInt("Code") == 401) {
                                    categoryList = new JSONArray();
                                    JSONObject ownCategory = new JSONObject();
                                    ownCategory.put("category", TEXT_OWN_CATEGORY);
                                    ownCategory.put("description", "");
                                    ownCategory.put("owner", loginPreference.getString("loginName", ""));
                                    categoryList.put(ownCategory);
                                    mAdapter.notifyDataSetChanged();
                                    chooseCategory(0);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void chooseCategory(int index) {
        if (categoryList == null || categoryList.length() == 0)
            return;
        JSONObject category = null;
        try {
            category = categoryList.getJSONObject(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            mCategoryTitle.setText(category.get("category").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            mCategoryDescription.setText(category.get("description").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (category.get("category").toString().equals(TEXT_OWN_CATEGORY))
            {
                mCategoryDescription.setEnabled(true);
            } else
            {
                mCategoryDescription.setEnabled(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setButtonStyle(View v) {
        v.setOnClickListener(this);
        v.setOnTouchListener(Confirm2MeButtonListener.getInstance());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == 0) { // OK
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Crouton.makeText(getActivity(), R.string.purchase_success, Style.CONFIRM).show();

                    // TODO::
                    onSend();
                } catch (JSONException e) {
                    Crouton.makeText(getActivity(), R.string.purchase_error, Style.ALERT).show();
                    e.printStackTrace();
                } catch (Exception e) {
                    Crouton.makeText(getActivity(), R.string.purchase_error, Style.ALERT).show();
                    e.printStackTrace();
                }
            }

            if(responseCode == 7){ // Aleady Owned
            }
        }
    }
    //=====InApp Billing===========================================================================
    private void initBilling(){
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void buyProduct(){
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getActivity().getPackageName(), BillingService.SKU_UPGRADE, "inapp", "");
            int response = buyIntentBundle.getInt("RESPONSE_CODE");
            if (response == 0) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }else{
                Log.e("InAppBilling", "RESPONSE_CODE="+response);
                Crouton.makeText(getActivity(), R.string.purchase_error, Style.ALERT).show();
            }
        } catch (RemoteException e) {
            Log.e(getClass().toString(), e.getMessage());
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            Log.e(getClass().toString(), e.getMessage());
            e.printStackTrace();
        }
    }

    // Adapter
    public class CategoryAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public CategoryAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return categoryList != null ? categoryList.length() : 0;
        }

        @Override
        public JSONObject getItem(int position) {

            try {
                return categoryList.getJSONObject(0);
            } catch (Exception e) {e.printStackTrace();}
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item_category, null);
                holder.categoryTitle = (TextView)convertView.findViewById(R.id.categoryName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            if (categoryList != null) {
                JSONObject category = null;
                try {
                    category = categoryList.getJSONObject(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    holder.categoryTitle.setText(category.get("category").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return convertView;
        }
    }

    public static class ViewHolder {
        public TextView categoryTitle;
    }
}
