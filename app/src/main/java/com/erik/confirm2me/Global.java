package com.erik.confirm2me;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

public class Global {

	public static final String FILE_AUTHORITY = "com.martin.spotifyer.fileProvider";
	public static final String kSpotifyClientID = "4cb523da656e47ad9db247d2608a59ef";
	public static final String kSpotifyRedirectURI = "myspotifyerapp123456789://";
	public static final int kSpotifyRequestCode = 1337;

	public static final String kParseAppID = "LPxssnn06ztKZE787DC7wyD69g0OdlRYsP14yh54";
	public static final String kParseClientID = "mUmI43YS65gQmATpqAdwpX3I4SfKlpyMaH0xVOnb";

	public static final String kUserRoleAdmin = "Admin";
	public static final String kUserRoleJoiner = "Joiner";
	public static final String kUserRoleUndefined = "Undefined";

	public static final String kSenderStatusSubmitted = "Submitted";
	public static final String kSenderStatusCompleted = "Completed";
	public static final String kSenderStatusDeclined = "Declined";

	public static final String kProviderStatusPending = "Pending";
	public static final String kProviderStatusAccepted = "Accepted";
	public static final String kProviderStatusDeclined = "Declined";


	public static String BASE_URL   	= "";
	public static String baseUrl				= "http://18.235.201.14:8080/api/v1";
	public static String url 					= null;
	public static String registraionUrl 		= "/registration";
	public static String loginUrl       		= "/login";
	public static String countRequestsUrl1  	= "/CountbySenderMail";
	public static String countRequestsUrl2  	= "/CountbyReceiverMail";
	public static String videoUrl 				= "/addvideo";
	public static String updateStatusUrl 		= "/updatesenderstatus";
	public static String changeToken			= "/changetoken";

	public static String providerUrl 			= "/userbyprovider";
	public static String requesterUrl 			= "/userbyrequester";
	public static String emailUrl 				= "/userbyemail";

	public static String senderStatusUrl 		= "/updatesenderstatus";
	public static String receiverStatusUrl 		= "/updatereceiverstatus";
	public static String updateReceiverMail 	= "/updatereceivermail";
	public static String resetPassword 			= "/resetpassword";
	public static String updateSenderMail 		= "/updatesendermail";
	public static String deleteRequestUrl 		= "/deleterequest";

	public static String byRequester 			= "/requestsbyrequester";
	public static String byProvider				= "/requestsbyprovider";
	public static String updateUrl 				= "/updateuser";
	public static String findByPhone 			= "/findbyphone?";
	public static String addCategoryUrl 		= "/addcategory";
	public static String newRequestUrl 			= "/newrequest";
	public static String sortedCategoryUrl 		= "/getsortedcategories";
	public static String uploadUrl 				= "http://18.235.201.14:8080/api/v1/upload";
	public static String requesterFirstName 	= "";
	public static String requesterLastName 		= "";
	public static String providerFirstName		= "";
	public static String providerLastName 		= "";
	public static String key 					= "key=AIzaSyB3vwzJRn99TTq7xlY5oCK_cxIv6UdRW9c";
	public static int 	 fragment 				= 0;
	public static AsyncHttpClient 	client		= null;
	public static RequestParams 	params		= null;
	public static JsonArrayRequest arrReq = null;
	public static RequestQueue	   requestQueue;

	public static String getURLEncoded(String url) {
		try {
			url = URLDecoder.decode(url, "UTF-8");

			url = url.replaceAll("\\+", "%20");
			url = url.replace(" ", "%20");

		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return url;
	}


	/** Create a File for saving a video */
	public static File getOutputMediaFile(){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Confirm2Me");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("Confirm2Me", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "message_reading.mp4");

		return mediaFile;
	}


	// bitmap
	public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {
		final int THUMBNAIL_SIZE = 350;

		InputStream input = context.getContentResolver().openInputStream(uri);

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither = true;// optional
		onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		input.close();
		if ((onlyBoundsOptions.outWidth == -1)
				|| (onlyBoundsOptions.outHeight == -1))
			return null;

		int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
				: onlyBoundsOptions.outWidth;

		double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE)
				: 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true;// optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		input = context.getContentResolver().openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		input.close();
		return bitmap;
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0)
			return 1;
		else
			return k;
	}

	public static Bitmap scaleImage(Context context, Uri photoUri) throws IOException {

		final int MAX_IMAGE_DIMENSION = 350;


		InputStream is = context.getContentResolver().openInputStream(photoUri);
		BitmapFactory.Options dbo = new BitmapFactory.Options();
		dbo.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, dbo);
		is.close();

		int rotatedWidth, rotatedHeight;
		int orientation = getOrientation(context, photoUri);

		if (orientation == 90 || orientation == 270) {
			rotatedWidth = dbo.outHeight;
			rotatedHeight = dbo.outWidth;
		} else {
			rotatedWidth = dbo.outWidth;
			rotatedHeight = dbo.outHeight;
		}

		Bitmap srcBitmap;
		is = context.getContentResolver().openInputStream(photoUri);
		if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
			float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
			float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
			float maxRatio = Math.max(widthRatio, heightRatio);

			// Create the bitmap from file
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (int) maxRatio;
			srcBitmap = BitmapFactory.decodeStream(is, null, options);
		} else {
			srcBitmap = BitmapFactory.decodeStream(is);
		}
		is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
		if (orientation > 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);

			srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
					srcBitmap.getHeight(), matrix, true);
		}

		String type = context.getContentResolver().getType(photoUri);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (type.equals("image/png")) {
			srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		} else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
			srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		}
		byte[] bMapArray = baos.toByteArray();
		baos.close();
		return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
	}

	public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		if (cursor.getCount() != 1) {
			return -1;
		}

		cursor.moveToFirst();
		return cursor.getInt(0);
	}

}
