package com.erik.confirm2me.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.erik.confirm2me.AppData;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customtab.BaseContainerFragment;
import com.erik.confirm2me.customtab.Tab1Container;
import com.erik.confirm2me.customtab.Tab2Container;
import com.erik.confirm2me.customtab.Tab3Container;
import com.erik.confirm2me.customtab.Tab4Container;
import com.erik.confirm2me.fragment.GivenFragment;
import com.erik.confirm2me.fragment.MyProfileFragment;
import com.erik.confirm2me.fragment.NewFragment;
import com.erik.confirm2me.fragment.PendingFragment;
import com.erik.confirm2me.helper.BadgeView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends FragmentActivity {

    private static final String TAB_1_TAG = "tab_1";
    private static final String TAB_2_TAG = "tab_2";
    private static final String TAB_3_TAG = "tab_3";
    private static final String TAB_4_TAG = "tab_4";

    private FragmentTabHost mTabHost;
    private BadgeView mBadgeRequeser;
    private BadgeView mBadgeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        // tab
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.setBackgroundResource(R.drawable.tabbar_bg);
        mTabHost.getTabWidget().setDividerDrawable(null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_1_TAG), "As Requester", R.drawable.tabbar_icon_requester), Tab1Container.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_2_TAG), "As Provider", R.drawable.tabbar_icon_provider), Tab2Container.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_3_TAG), "New",  R.drawable.tabbar_icon_new), Tab3Container.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_4_TAG), "Profile", R.drawable.tabbar_icon_profile), Tab4Container.class, null);
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
                    setTabSelected(mTabHost.getTabWidget().getChildAt(i), i, null, false);
                }
                setTabSelected(mTabHost.getCurrentTabView(), -1, tabId, true);

            }
        });

        setTabSelected(mTabHost.getCurrentTabView(), -1, mTabHost.getCurrentTabTag(), true);
        showTabBadges();
        SharedPreferences loginPreference = getSharedPreferences("login", 0);
        if (loginPreference.getString("id", "") != null){
            Global.url = Global.baseUrl + Global.changeToken;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            Global.params.put("Token", FirebaseInstanceId.getInstance().getToken());
            Global.params.put("idx", loginPreference.getString("id", ""));
            Global.params.setUseJsonStreamer(true);
            Global.client.put( Global.url, Global.params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                }
            });
        }
    }

    public void showTabBadges() {
        AppData.getInstance().setApplicationBadgeNumber(0);
        TabWidget tabWidget = mTabHost.getTabWidget();
        if (mBadgeRequeser == null)
            mBadgeRequeser = new BadgeView(this, tabWidget, 0);
        mBadgeRequeser.setBadgeMargin(30, 10);
        if (mBadgeProvider == null)
            mBadgeProvider = new BadgeView(this, tabWidget, 1);
        mBadgeProvider.setBadgeMargin(30, 10);

        // Badge Number of Requester
        SharedPreferences loginPreference = getSharedPreferences("login", 0);
        Global.url = Global.baseUrl + Global.countRequestsUrl1;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("requester", loginPreference.getString("loginName", ""));
        Global.params.setUseJsonStreamer(true);
        Global.client.get(Global.url, Global.params, new TextHttpResponseHandler()  {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                mBadgeRequeser.setText("" + response.getInt("Count"));
                                if (response.getInt("Count") > 0){
                                    mBadgeRequeser.show();
                                } else {
                                    mBadgeRequeser.hide();
                                }
                                // Set application Badge number
                                AppData.getInstance().setApplicationBadgeNumber(AppData.getInstance().getApplicationBadgeNumber() + response.getInt("Count"));
                                ShortcutBadger.with(getApplicationContext()).count(AppData.getInstance().getApplicationBadgeNumber());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Badge Number of Provider

        Global.url = Global.baseUrl + Global.countRequestsUrl2;
        Global.params = new RequestParams();
        Global.params.put("provider", loginPreference.getString("loginName", ""));
        Global.params.setUseJsonStreamer(true);
        Global.client.get( Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseString);

                            if (response.getBoolean("Success") == true) {
                                mBadgeProvider.setText("" + response.getInt("Count"));
                                    if (response.getInt("Count") > 0){
                                        mBadgeProvider.show();
                                } else {
                                    mBadgeProvider.hide();
                                }
                                // Set application Badge number
                                AppData.getInstance().setApplicationBadgeNumber(AppData.getInstance().getApplicationBadgeNumber() + response.getInt("Count"));
                                ShortcutBadger.with(getApplicationContext()).count(AppData.getInstance().getApplicationBadgeNumber());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (MyProfileFragment.mFragment != null) {
            MyProfileFragment.mFragment.onActivityResult(requestCode, resultCode, data);
        }
        if (PendingFragment.mFragment != null) {
            PendingFragment.mFragment.onActivityResult(requestCode, resultCode, data);
        }
        if (GivenFragment.mFragment != null) {
            GivenFragment.mFragment.onActivityResult(requestCode, resultCode, data);
        }
        if (NewFragment.mFragment != null) {
            NewFragment.mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;
        String currentTabTag = mTabHost.getCurrentTabTag();

        if (currentTabTag.equals(TAB_1_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_1_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_2_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_2_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_3_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_3_TAG)).popFragment();
        }
        else if (currentTabTag.equals(TAB_4_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_4_TAG)).popFragment();
        }

        if (!isPopFragment) {
            finish();
        }
    }

    private TabHost.TabSpec setIndicator(Context ctx, TabHost.TabSpec spec, String string,int intIconResID) {

        View v = LayoutInflater.from(ctx).inflate(R.layout.tab_item, null);
        ImageView iv = (ImageView)v.findViewById(R.id.icon_tabicon);
        iv.setImageResource(intIconResID);
        TextView tv = (TextView)v.findViewById(R.id.txt_tabtxt);
        tv.setText(string);
        return spec.setIndicator(v);
    }

    private void setTabSelected(View tabView, int tabIndex, String tabId, boolean selected) {
        ImageView iv = (ImageView) tabView.findViewById(R.id.icon_tabicon);
        iv.setImageResource(tabId != null ? getTabIconResId(tabId, selected) : getTabIconResId(tabIndex, selected));
        ImageView cV = (ImageView) tabView.findViewById(R.id.tab_selector);
        cV.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    private int getTabIconResId(int tabIndex, boolean selected) {
        switch (tabIndex) {
            case 0:
                return selected ? R.drawable.tabbar_icon_requester_selected : R.drawable.tabbar_icon_requester;
            case 1:
                return selected ? R.drawable.tabbar_icon_provider_selected : R.drawable.tabbar_icon_provider;
            case 2:
                return selected ? R.drawable.tabbar_icon_new_selected : R.drawable.tabbar_icon_new;
            case 3:
                return selected ? R.drawable.tabbar_icon_profile_selected : R.drawable.tabbar_icon_profile;
            default:
                return 0;
        }
    }

    private int getTabIconResId(String tabId, boolean selected) {
        if (tabId.equals(TAB_1_TAG))
            return selected ? R.drawable.tabbar_icon_requester_selected : R.drawable.tabbar_icon_requester;
        else if (tabId.equals(TAB_2_TAG))
            return selected ? R.drawable.tabbar_icon_provider_selected : R.drawable.tabbar_icon_provider;
        else if (tabId.equals(TAB_3_TAG))
            return selected ? R.drawable.tabbar_icon_new_selected : R.drawable.tabbar_icon_new;
        else if (tabId.equals(TAB_4_TAG))
            return selected ? R.drawable.tabbar_icon_profile_selected : R.drawable.tabbar_icon_profile;
        else
            return 0;
    }

}
