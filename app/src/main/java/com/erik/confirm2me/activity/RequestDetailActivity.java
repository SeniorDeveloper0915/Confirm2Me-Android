package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cocosw.bottomsheet.BottomSheet;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class RequestDetailActivity extends Activity implements View.OnClickListener {

    private static String TAG = "RequestDetailActivity";

    private static final int REQUEST_VERIFY_PIN_FOR_ACCEPT = 1;
    private static final int REQUEST_VERIFY_PIN_FOR_DECLINE = 2;
    private static final int REQUEST_CREATE_VIDEO = 3;

//    public static ParseObject mDetailRequest;
    public static JSONObject mDetailRequest;
    public static boolean isFromRequester;

    private TextView txtCategory;
    private TextView txtRequestDate;
    private TextView txtMessage;
    private ImageView arrowRequester;
    private TextView txtRequesterName;
    private ImageView arrowProvider;
    private TextView txtProviderName;
    private ImageView thumbnailView;
    private ImageButton btnPlayVideo;
    private Button declineButton;
    private Button acceptButton;
    private ViewGroup videoView;
    private ViewGroup buttonView;
    private ImageButton backButton;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        txtCategory = (TextView)findViewById(R.id.txtCategory);
        txtRequestDate = (TextView)findViewById(R.id.txtRequestDate);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        arrowRequester = (ImageView)findViewById(R.id.arrowRequest);
        txtRequesterName = (TextView)findViewById(R.id.txtRequesterName);
        arrowProvider = (ImageView)findViewById(R.id.arrowProvider);
        txtProviderName = (TextView)findViewById(R.id.txtProviderName);
        thumbnailView = (ImageView)findViewById(R.id.thumbnailView);
        videoView = (ViewGroup)findViewById(R.id.videoView);
        buttonView = (ViewGroup)findViewById(R.id.buttonView);

        backButton = (ImageButton)findViewById(R.id.btnBack);
        backButton.setOnClickListener(this);
        backButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        btnPlayVideo = (ImageButton)findViewById(R.id.btnPlayVideo);
        btnPlayVideo.setOnClickListener(this);
        btnPlayVideo.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        declineButton = (Button)findViewById(R.id.btnDecline);
        declineButton.setOnClickListener(this);
        declineButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        acceptButton = (Button)findViewById(R.id.btnAccept);
        acceptButton.setOnClickListener(this);
        acceptButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("LoadFing...");
        mProgressDialog.setCancelable(false);

        loadRequestDetails();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VERIFY_PIN_FOR_ACCEPT) { // ACCEPTED
                if (isFromRequester) { // As Requester
                    Global.url = Global.baseUrl + Global.updateStatusUrl;
                    Global.client = new AsyncHttpClient(true, 80, 443);
                    Global.params = new RequestParams();
                    try {
                        Global.params.put("status", Global.kSenderStatusCompleted);
                        Global.params.put("idx", mDetailRequest.get("id").toString());
                        Global.params.setUseJsonStreamer(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                                    Global.url = Global.baseUrl + Global.providerUrl;
                                    Global.client = new AsyncHttpClient(true, 80, 443);
                                    Global.params = new RequestParams();
                                    try {
                                        Global.params.put("name", mDetailRequest.get("provider_pNumber").toString());
                                        Global.params.setUseJsonStreamer(true);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                            mProgressDialog.dismiss();
//                                            Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                            JSONObject response = null;
                                            try {
                                                response = new JSONObject(responseString);
                                                if (response.getBoolean("Success") == true) {
                                                    JSONObject user = null;
                                                    user = response.getJSONArray("User").getJSONObject(0);
                                                    sendPushnotification("Accepted", user);
                                                    new AlertDialog.Builder(RequestDetailActivity.this)
                                                            .setMessage("Updated requester_status!")
                                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else { // As Provider
                    try {
                        if (mDetailRequest.get("video").toString().equals("") == false) {
                            Global.url = Global.baseUrl + Global.providerUrl;
                            Global.client = new AsyncHttpClient(true, 80, 443);
                            Global.params = new RequestParams();
                            try {
                                Global.params.put("name", mDetailRequest.get("Requester").toString());
                                Global.params.setUseJsonStreamer(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                                            JSONObject user = null;
                                            user = response.getJSONArray("User").getJSONObject(0);
                                            sendPushnotification("Accepted", user);
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == REQUEST_VERIFY_PIN_FOR_DECLINE) { // DECLINED
                if (isFromRequester) { // As Requester
                    Global.url = Global.baseUrl + Global.senderStatusUrl;
                    Global.client = new AsyncHttpClient(true, 80, 443);
                    Global.params = new RequestParams();
                    try {
                        Global.params.put("status", Global.kSenderStatusDeclined);
                        Global.params.put("idx", mDetailRequest.get("id").toString());
                        Global.params.setUseJsonStreamer(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Global.client.put(Global.url, Global.params, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                            Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            JSONObject response = null;
                            try {
                                response = new JSONObject(responseString);
                                if (response.getBoolean("Success") == true) {
                                    Global.url = Global.baseUrl + Global.providerUrl;
                                    Global.client = new AsyncHttpClient(true, 80, 443);
                                    Global.params = new RequestParams();
                                    try {
                                        Global.params.put("name", mDetailRequest.get("provider_pNumber").toString());
                                        Global.params.setUseJsonStreamer(true);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                            Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                            JSONObject response = null;
                                            try {
                                                response = new JSONObject(responseString);
                                                if (response.getBoolean("Success") == true) {
                                                    JSONObject user = null;
                                                    user = response.getJSONArray("User").getJSONObject(0);
                                                    sendPushnotification("Declined", user);
                                                    new AlertDialog.Builder(RequestDetailActivity.this)
                                                            .setMessage("Updated requester_status!")
                                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else { // As Provider
                    try {
                        if (mDetailRequest.get("video").toString().equals("") == true) {
//                            mProgressDialog.show();
                            Global.client = new AsyncHttpClient(true, 80, 443);
                            Global.url = Global.baseUrl + Global.receiverStatusUrl;
                            Global.params = new RequestParams();
                            try {
                                Global.params.put("status", Global.kProviderStatusDeclined);
                                Global.params.put("idx", mDetailRequest.get("id").toString());
                                Global.params.setUseJsonStreamer(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Global.client.put(Global.url, Global.params, new TextHttpResponseHandler() {
                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    mProgressDialog.dismiss();
//                                    Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                    JSONObject response = null;
                                    try {
                                        response = new JSONObject(responseString);
                                        if (response.getBoolean("Success") == true) {
                                            Global.client = new AsyncHttpClient(true, 80, 443);
                                            Global.url = Global.baseUrl + Global.providerUrl;
                                            Global.params = new RequestParams();
                                            try {
                                                Global.params.put("name", mDetailRequest.get("Requester").toString());
                                                Global.params.setUseJsonStreamer(true);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                                    Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                                    JSONObject response = null;
                                                    try {
                                                        response = new JSONObject(responseString);
                                                        if (response.getBoolean("Success") == true) {
                                                            JSONObject user = null;
                                                            user = response.getJSONArray("User").getJSONObject(0);
                                                            sendPushnotification("Declined", user);
                                                            new AlertDialog.Builder(RequestDetailActivity.this)
                                                                    .setMessage("Updated requester_status!")
                                                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            finish();
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
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == REQUEST_CREATE_VIDEO) { // Created New Video for the request
                finish();
            }
        }
    }

    /**
     *
     * @param action the user action (Accepted or Declined)
     * @param toUser the user to be received
     * @return void.
     */
    private void sendPushnotification(String action, JSONObject toUser) {
        String pushMsg = null;
        try {
            pushMsg = String.format("%s %s has %s", toUser.getString("firstname"), toUser.getString("lastname"), action);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        Global.client.post(getApplicationContext(), "https://fcm.googleapis.com/fcm/send", jsonEntity, "application/json",
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

    @Override
    public void onClick(View v) {

        if (v == backButton) {
            finish();
        }
        else if (v == btnPlayVideo) {

        }
        else if (v == declineButton) {
            // Decline Request
            new BottomSheet.Builder(this).title("Choose the way to verify you").sheet(R.menu.menu_verify).listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case R.id.menu_verify_pin: {
                            startActivityForResult(new Intent(RequestDetailActivity.this, VerifyPinActivity.class), REQUEST_VERIFY_PIN_FOR_DECLINE);
                            break;
                        }
                    }
                }
            }).show();
        }
        else if (v == acceptButton) {
            // Accept Request
            if (isFromRequester) { // As Requester
                // TODO:: alert You should look at video first!

                new BottomSheet.Builder(this).title("Choose the way to verify you").sheet(R.menu.menu_verify).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.menu_verify_pin: {
                                startActivityForResult(new Intent(RequestDetailActivity.this, VerifyPinActivity.class), REQUEST_VERIFY_PIN_FOR_ACCEPT);
                                break;
                            }
                        }
                    }
                }).show();
            }
            else { // As Provider
                // TODO:: go to message read page
                MessageReadActivity.mDetailRequest = mDetailRequest;
                startActivityForResult(new Intent(RequestDetailActivity.this, MessageReadActivity.class), REQUEST_CREATE_VIDEO);
            }
        }
    }

    private void loadRequestDetails() {

        if (mDetailRequest != null) {
            // Category
//            SimpleDateFormat formater = new SimpleDateFormat("MMM/dd/yyyy");

            try {
                txtCategory.setText(mDetailRequest.get("affidavit_category").toString());
                txtRequestDate.setText(mDetailRequest.get("updated_at").toString());
                txtMessage.setText(mDetailRequest.get("affidavit_description").toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Global.url = Global.baseUrl + Global.requesterUrl;
            Global.client = new AsyncHttpClient(true, 80, 443);
            Global.params = new RequestParams();
            try {
                Global.params.put("requester", mDetailRequest.get("Requester").toString());
                Global.params.put("provider", mDetailRequest.get("provider_pNumber").toString());
                Global.params.setUseJsonStreamer(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Global.client.get(Global.url, Global.params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Toast.makeText(RequestDetailActivity.this, "Fail", Toast.LENGTH_LONG).show();
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

                            txtRequesterName.setText(String.format("%s %s", requester.getString("firstname"), requester.getString("lastname")));
                            txtProviderName.setText(String.format("%s %s", provider.getString("firstname"), provider.getString("lastname")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            String requesterStatus = null;
            try {
                requesterStatus = mDetailRequest.get("sender_status").toString();
                if (requesterStatus.equals("Submitted")) {
                    arrowRequester.setImageResource(R.drawable.arrow_requester_pending);
                } else if (requesterStatus.equals("Completed")) {
                    arrowRequester.setImageResource(R.drawable.arrow_requester_accept);
                } else if (requesterStatus.equals("Declined")) {
                    arrowRequester.setImageResource(R.drawable.arrow_requester_decline);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String providerStatus = null;
            try {
                providerStatus = mDetailRequest.get("receiver_status").toString();
                if (providerStatus.equals("Pending")) {
                    arrowProvider.setImageResource(R.drawable.arrow_provider_pending);
                } else if (providerStatus.equals("Accepted")) {
                    arrowProvider.setImageResource(R.drawable.arrow_provider_accept);
                } else if (providerStatus.equals("Declined")) {
                    arrowProvider.setImageResource(R.drawable.arrow_provider_decline);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (mDetailRequest.getString("video").equals("") == false ) {
                    videoView.setVisibility(View.VISIBLE);
                    final VideoView video = (VideoView) findViewById(R.id.videoPlayingView);
                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);

                    Uri uri = Uri.parse("http://18.235.201.14:8080/api/v1/download?filename=" + mDetailRequest.getString("video"));
                    video.setMediaController(mediaController);
                    video.setVideoURI(uri);
                    video.start();
                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            video.start();
                            video.pause();
                        }
                    });

                } else {
                    videoView.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // Buttons
        if (isFromRequester) {
            // As Requester

            acceptButton.setText("ACCEPT");
            try {
                if (mDetailRequest.get("sender_status").toString().equals(Global.kSenderStatusSubmitted) &&
                        mDetailRequest.get("receiver_status").toString().equals(Global.kProviderStatusAccepted)) {
                    buttonView.setVisibility(View.VISIBLE);
                } else {
                    buttonView.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            // As Provider
            acceptButton.setText("CREATE");
            try {
                if (mDetailRequest.getString("receiver_status").equals(Global.kProviderStatusPending) == true) {
                    buttonView.setVisibility(View.VISIBLE);
                } else {
                    buttonView.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param path
     *            the path to the Video
     * @return a thumbnail of the video or null if retrieving the thumbnail failed.
     */
    public static Bitmap getVidioThumbnail(String path) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            bitmap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MICRO_KIND);
            if (bitmap != null) {
                return bitmap;
            }
        }
        // MediaMetadataRetriever is available on API Level 8 but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();
            final Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, path);
            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
                bitmap = (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                final byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
                if (bitmap == null) {
                    bitmap = (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
                }
            }
        } catch (Exception e) {
            bitmap = null;
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (final Exception ignored) {
            }
        }
        return bitmap;
    }
}
