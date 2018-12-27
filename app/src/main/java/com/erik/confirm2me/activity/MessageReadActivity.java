package com.erik.confirm2me.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.erik.confirm2me.Global;
import com.erik.confirm2me.R;
import com.erik.confirm2me.customcontrol.Confirm2MeButtonListener;
import com.erik.confirm2me.helper.CameraPreview;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;


public class MessageReadActivity extends Activity implements View.OnClickListener {

    protected Camera.CameraInfo cameraInfo;
    private static String TAG = "MessageReadActivity";

    private static final int REQUEST_VERIFY_PIN = 1;
    public static JSONObject mDetailRequest;
    private ViewGroup cameraPreView;
    private ViewGroup videoPreview;
    private ImageView thumbnailView;
    private ImageButton btnPlayVideo;;
    private TextView txtMessage;
    private Button recordButton;

    private ImageButton backButton;
    private Button sendButton;
    private ProgressDialog mProgressDialog;
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;

    private boolean isReaded = false;
    private Timer mTimer;
    private int mReadingIndex;
    public static final String CAMERA_RECORD_PREF = "camera_record_pref";

    public static final String ALLOW_KEY = "ALLOWED";
    public static final int MY_PERMISSION_REQUEST = 100;
    public static final int MAX_LENGTH = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_read);

        cameraPreView = (ViewGroup)findViewById(R.id.cameraPreview);
        videoPreview = (ViewGroup)findViewById(R.id.videoPreview);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        thumbnailView = (ImageView)findViewById(R.id.thumbnailView);
        btnPlayVideo = (ImageButton)findViewById(R.id.btnPlayVideo);
        recordButton = (Button)findViewById(R.id.btnRecord);

        backButton = (ImageButton)findViewById(R.id.btnBack);
        backButton.setOnClickListener(this);
        backButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        sendButton = (Button)findViewById(R.id.btnSend);
        sendButton.setOnClickListener(this);
        sendButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        btnPlayVideo.setOnClickListener(this);
        btnPlayVideo.setOnTouchListener(Confirm2MeButtonListener.getInstance());
        recordButton.setOnClickListener(this);
        recordButton.setOnTouchListener(Confirm2MeButtonListener.getInstance());

        mProgressDialog = new ProgressDialog(this, android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        setmCameraPreviewSize();
        cameraPreView.addView(mPreview);
        loadRequestDetails();
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(this)) {
            new AlertDialog.Builder(MessageReadActivity.this)
                    .setMessage("Sorry, your phone does not have a camera!")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
        if (mCamera == null) {
            mCamera = getCameraInstance();
            mPreview.refreshCamera(mCamera);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();
    }

    public String randomName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VERIFY_PIN) {
                mProgressDialog.show();

                // Upload the video file to Parse
                SharedPreferences loginPreference = getSharedPreferences("login", 0);
                Uri fileUri = Uri.fromFile(Global.getOutputMediaFile());
                File sourceFile = new File(fileUri.getPath());
                Global.client = new AsyncHttpClient();
                Global.params = new RequestParams();
                try {
                    Global.params.put("filetoupload", sourceFile);
                } catch(FileNotFoundException e) {}

                Global.client.post(Global.uploadUrl,  Global.params,  new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String fileName, url;
                            Global.url = Global.baseUrl + Global.videoUrl;
                            Global.client = new AsyncHttpClient(true, 80, 443);
                            Global.params = new RequestParams();
                            fileName = response.getString("filename");
                            url = Global.BASE_URL + "./" + fileName + ".mp4";
                            try {
                                Global.params.put("status", Global.kProviderStatusAccepted);
                                Global.params.put("video", url);
                                Global.params.put("idx", mDetailRequest.get("id").toString());
                                Global.params.setUseJsonStreamer(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Global.client.put(Global.url, Global.params, new TextHttpResponseHandler() {
                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    mProgressDialog.dismiss();
//                                    Toast.makeText(MessageReadActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                    mProgressDialog.dismiss();
                                    JSONObject response = null;
                                    try {
                                        response = new JSONObject(responseString);
                                        if (response.getBoolean("Success") == true) {
                                            new AlertDialog.Builder(MessageReadActivity.this)
                                                    .setMessage("Submitted video!")
                                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent returnIntent = new Intent();
                                                            setResult(Activity.RESULT_OK,returnIntent);
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFinish() {
                        mProgressDialog.dismiss();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v == backButton) {
            finish();
        }
        else if (v == sendButton) {
            if (!isReaded) {
                new AlertDialog.Builder(MessageReadActivity.this)
                        .setMessage("Take your video first by clicking 'Start' button!")
                        .setNeutralButton("OK", null)
                        .show();
                return;
            }
            new BottomSheet.Builder(this).title("Choose the way to verify you").sheet(R.menu.menu_verify).listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case R.id.menu_verify_pin: {
                            startActivityForResult(new Intent(MessageReadActivity.this, VerifyPinActivity.class), REQUEST_VERIFY_PIN);
                            break;
                        }
                    }
                }
            }).show();

        }
        else if (v == btnPlayVideo) {
            // Play Video
            try {
                Intent in = new Intent(Intent.ACTION_VIEW);
                in.setDataAndType(Uri.fromFile(Global.getOutputMediaFile()), "video/*");
                startActivity(in);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        else if (v == recordButton) {
            startRecording();
        }
    }

    private void loadRequestDetails() {
        if (mDetailRequest != null) {
            // Message
            try {
                txtMessage.setText(mDetailRequest.get("affidavit_description").toString());
                if (mDetailRequest.get("video").toString().equals("") ==  true) {
                    videoPreview.setVisibility(View.GONE);
                } else {
                    videoPreview.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*** Update TextView Task ***/
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String msgStr = txtMessage.getText().toString();
            SpannableStringBuilder sb = new SpannableStringBuilder(msgStr);
            final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 255));
            sb.setSpan(fcs, 0, mReadingIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            txtMessage.setText(sb);

            mReadingIndex++;
            if (mReadingIndex > msgStr.length()) {
                isReaded = true;
                stopRecording();
            }

        }
    };

    /***  Start / Stop Recording with Timmer ************/
    private void startRecording() {
        if (mReadingIndex == 0) {

            isReaded = false;

            // init the timer
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    (MessageReadActivity.this).runOnUiThread(mUpdateTimeTask);
                }
            }, 1000, 1000);
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                recordButton.setEnabled(false);
                recordButton.setAlpha(0.5f);
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }

            //hide video playing if video is playing
            showVideoPlayer(false);
        }
    }

    private void stopRecording() {
        // stop timer
        mReadingIndex = 0;
        mTimer.cancel();
        // stop camera recording and release camera
        try {
            mMediaRecorder.stop();  // stop the recording
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        recordButton.setEnabled(true);
        recordButton.setAlpha(1);
        recordButton.setText("Redo");

        // show video playing just recorded
        showVideoPlayer(true);
    }

    private void showVideoPlayer(boolean isShow) {
        if (isShow) {
            cameraPreView.setVisibility(View.INVISIBLE);
            videoPreview.setVisibility(View.VISIBLE);
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(Global.getOutputMediaFile().getPath(), Thumbnails.FULL_SCREEN_KIND);
            thumbnailView.setImageBitmap(thumbnail);
        } else {
            cameraPreView.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.INVISIBLE);
        }
    }



    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(findFrontFacingCamera()); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void setmCameraPreviewSize() {
        // Get the set dimensions

        // Get the width of the screen
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = cameraPreView.getLayoutParams();
        lp.width = (int) (screenProportion * lp.height);
        // Commit the layout parameters
        cameraPreView.setLayoutParams(lp);
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

//         Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(Global.getOutputMediaFile().toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        mMediaRecorder.setOrientationHint(270);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
}
