package com.erik.confirm2me.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.cocosw.bottomsheet.BottomSheet;
import com.erik.confirm2me.AppData;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.activity.LoginActivity;
import com.erik.confirm2me.activity.MainActivity;
import com.erik.confirm2me.activity.RequestDetailActivity;
import com.erik.confirm2me.activity.VerifyPinActivity;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Erik on 9/28/15.
 */
public class PendingFragment extends Fragment implements View.OnClickListener{

    private static final int LOAD_REQUEST_CODE = 100;
    private static final int REQUEST_VERIFY_PIN = 101;
    private View mRootView;
    private ImageButton mRefreshButton;
    private SwipeMenuListView mRequestListView;
    private RequestAdapter mAdapter;
    private JSONArray mRequests;
    private JSONObject delRequest;
    public static PendingFragment mFragment = null;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mProgressDialog = new ProgressDialog(getActivity(), android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        mRootView = inflater.inflate(R.layout.fragment_pending, container, false);
        mRefreshButton = (ImageButton)mRootView.findViewById(R.id.btnRefresh);
        mRequestListView = (SwipeMenuListView)mRootView.findViewById(R.id.requestListView);
        setButtonStyle(mRefreshButton);

        // create a SwipeMenuCreator to add items.
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth((int) AppData.dipToPixels(getActivity(), 80));
                // set a icon
                //deleteItem.setIcon(R.drawable.ic_delete);
                // set item title
                deleteItem.setTitle("Delete");
                // set item title fontsize
                deleteItem.setTitleSize(14);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        loadRequestData();
        // set creator
        mRequestListView.setMenuCreator(creator);
        mRequestListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        // listener item click event
        mRequestListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // delete
                        //can delete if provider was declined only!
                        try {
                            delRequest = mRequests.getJSONObject(position);
                            if (delRequest.get("receiver_status").toString().equals(Global.kSenderStatusDeclined)) {
                                // Try to Delete
                                new BottomSheet.Builder(getActivity()).title("Choose the way to verify you").sheet(R.menu.menu_verify).listener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case R.id.menu_verify_pin: {
                                                getActivity().startActivityForResult(new Intent(getActivity(), VerifyPinActivity.class), REQUEST_VERIFY_PIN);
                                                break;
                                            }
                                        }
                                    }
                                }).show();
                            } else {
                                Toast.makeText(getActivity(), "The delete can be done if Requester has declined only!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        mRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject requestObject = mAdapter.getItem(position);
                try {
                    if (requestObject.getInt("sender_mail_unread") == 0) {
                        mProgressDialog.show();
                        Global.url = Global.baseUrl + Global.updateSenderMail;
                        Global.client = new AsyncHttpClient(true, 80, 443);
                        Global.params = new RequestParams();
                        Global.params.put("idx", requestObject.get("id").toString());
                        Global.params.setUseJsonStreamer(true);
                        Global.client.put(Global.url, Global.params, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                mProgressDialog.dismiss();
                                try {
                                    JSONObject response = new JSONObject(responseString);
                                    if (response.getBoolean("Success") == true) {
                                        RequestDetailActivity.mDetailRequest = requestObject;
                                        RequestDetailActivity.isFromRequester = true;
                                        getActivity().startActivity(new Intent(getActivity(), RequestDetailActivity.class));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        RequestDetailActivity.mDetailRequest = requestObject;
                        RequestDetailActivity.isFromRequester = true;
                        getActivity().startActivity(new Intent(getActivity(), RequestDetailActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mAdapter = new RequestAdapter();
        mRequestListView.setAdapter(mAdapter);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mFragment = this;
        m_handler.sendEmptyMessage(LOAD_REQUEST_CODE);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_handler.sendEmptyMessage(LOAD_REQUEST_CODE);
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
            if (requestCode == REQUEST_VERIFY_PIN) {
                // PIN verified to delete the object
                mProgressDialog.show();
                Global.url = Global.baseUrl + Global.deleteRequestUrl;
                Global.client = new AsyncHttpClient(true, 80, 443);
                Global.params = new RequestParams();
                try {
                    Global.params.put("idx", delRequest.get("id").toString());
                    Global.params.setUseJsonStreamer(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Global.client.delete(Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.dismiss();
                        try {
                            JSONObject response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                loadRequestData();
                                Log.d("PendingFragment", "Deleted the request.");
                            } else {
                                Toast.makeText(getContext(), response.get("Message").toString(), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mRefreshButton) {
            m_handler.sendEmptyMessage(LOAD_REQUEST_CODE);
        }
    }

    public Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case LOAD_REQUEST_CODE:
                    loadRequestData();
                    break;
            }
        }
    };

    public void loadRequestData() {
        mProgressDialog.show();
        final SharedPreferences loginPreference = getContext().getSharedPreferences("login", 0);
        Global.url = Global.baseUrl + Global.byRequester;
        Global.client = new AsyncHttpClient(true, 80, 443);
        Global.params = new RequestParams();
        Global.params.put("Requester", loginPreference.getString("loginName", ""));
        Global.params.setUseJsonStreamer(true);
        Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.dismiss();
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseString);
                            if (response.getBoolean("Success") == true) {
                                mRequests = new JSONArray();
                                if (response.getInt("Code") == 200) {
                                    mRequests = response.getJSONArray("Requests");
                                    mAdapter.notifyDataSetChanged();
                                } else if (response.getInt("Code") == 401) {
                                    mRequests = null;
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    // Functions
    private void setButtonStyle(View v) {
        v.setOnClickListener(this);
        v.setOnTouchListener(Confirm2MeButtonListener.getInstance());
    }

    // Adapter
    public class RequestAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        ViewHolder holder = null;
        String requesterStatus = null;
        String providerStatus = null;
        JSONObject request = null;

        public RequestAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return mRequests != null ? mRequests.length() : 0;
        }

        @Override
//        public ParseObject getItem(int position) {
//
//            try {
//                return mRequests.get(position);
//            } catch (Exception e) {e.printStackTrace();}
//            return null;
//        }
        public JSONObject getItem(int position) {

            try {
                return mRequests.getJSONObject(position);
            } catch (Exception e) {e.printStackTrace();}
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item_pending, null);
                holder.requestDot = (ImageView)convertView.findViewById(R.id.dotRequest);
                holder.requesterName = (TextView)convertView.findViewById(R.id.txtRequesterName);
                holder.requestArrow = (ImageView)convertView.findViewById(R.id.arrowRequest);
                holder.providerName = (TextView)convertView.findViewById(R.id.txtProviderName);
                holder.providerArrow = (ImageView)convertView.findViewById(R.id.arrowProvider);
                holder.affidavit = (TextView)convertView.findViewById(R.id.txtAffidavit);
                holder.requestDate = (TextView)convertView.findViewById(R.id.txtRequestDate);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            try {
                request = mRequests.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (request.get("sender_mail_unread").toString().equals("1") == true)
                    holder.requestDot.setVisibility(View.VISIBLE);
                else
                    holder.requestDot.setVisibility(View.GONE);
                holder.affidavit.setText(request.get("affidavit_category").toString());
                holder.requestDate.setText(request.get("updated_at").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Requester
            Global.url = Global.baseUrl + Global.requesterUrl;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            try {
                Global.params.put("requester", request.get("Requester").toString());
                Global.params.put("provider", request.get("provider_pNumber").toString());
                Global.params.setUseJsonStreamer(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                            Toast.makeText(getContext(), "Fail", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            JSONObject response = null;
                            try {
                                response = new JSONObject(responseString);
                                if (response.getBoolean("Success") == true) {
                                    JSONObject requester = new JSONObject();
                                    JSONObject provider = new JSONObject();

                                    requester = response.getJSONArray("Requester").getJSONObject(0);
                                    provider = response.getJSONArray("Provider").getJSONObject(0);
                                    Global.requesterFirstName = requester.getString("firstname");
                                    Global.requesterLastName = requester.getString("lastname");
                                    Global.providerFirstName = provider.getString("firstname");
                                    Global.providerLastName = provider.getString("lastname");

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            holder.requesterName.setText(String.format("%s %s", Global.requesterFirstName, Global.requesterLastName));
            holder.providerName.setText(String.format("%s %s", Global.providerFirstName, Global.providerLastName));
            try {
                requesterStatus = request.get("sender_status").toString();
                if (requesterStatus.equals("Submitted")) {
                    holder.requestArrow.setImageResource(R.drawable.arrow_requester_pending);
                } else if (requesterStatus.equals("Completed")) {
                    holder.requestArrow.setImageResource(R.drawable.arrow_requester_accept);
                } else if (requesterStatus.equals("Declined")) {
                    holder.requestArrow.setImageResource(R.drawable.arrow_requester_decline);
                }

                providerStatus = request.get("receiver_status").toString();
                if (providerStatus.equals("Pending")) {
                    holder.providerArrow.setImageResource(R.drawable.arrow_provider_pending);
                } else if (providerStatus.equals("Accepted")) {
                    holder.providerArrow.setImageResource(R.drawable.arrow_provider_accept);
                } else if (providerStatus.equals("Declined")) {
                    holder.providerArrow.setImageResource(R.drawable.arrow_provider_decline);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    public static class ViewHolder {
        public ImageView requestDot;
        public TextView requesterName;
        public ImageView requestArrow;
        public TextView providerName;
        public ImageView providerArrow;
        public TextView affidavit;
        public TextView requestDate;
    }
}
