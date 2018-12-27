package com.erik.confirm2me.PromisePay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class PPRequest {
	private URL url;
	private HttpURLConnection con;
	private OutputStream os;


	/**
	 * Can be instantiated with java.net.URL object, which checked for MalformedURLException by caller, so no need to declare here
	 *
	 * @param url
	 */
	public PPRequest(URL url){
		this.url=url;
	}

	/**
	 * Can be instantiated with String representation of url, force caller to check for MalformedURLException which can be thrown by URL's constructor
	 *
	 * @param url
	 * @throws MalformedURLException
	 */
	public PPRequest(String url)throws MalformedURLException{
		this.url=new URL(url);
	}

	/**
	 * used by all sending methods: send(), sendAndReadString(), sendAndReadJson() - to close open resources
	 *
	 * @throws IOException
	 */
	private void done()throws IOException{
		con.disconnect();
		os.close();
	}

	/**
	 * Sending connection and opening an output stream to server by pre-defined instance variable url
	 * 
	 * @param isPost boolean - indicates whether this request should be sent in POST method
	 * @throws IOException - should be checked by caller
	 * */
	private void prepareAll(boolean isPost)throws IOException{
		con = (HttpURLConnection) url.openConnection();
		if(isPost)con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
        os = con.getOutputStream();
	}

	/**
	 * prepare request in GET method
	 *
	 * @return
	 * @throws IOException
	 */
	public PPRequest prepare() throws IOException{
		prepareAll(false);
		return this;
	}

	/**
	 * prepare request in POST method
	 *
	 * @return
	 * @throws IOException
	 */
	public PPRequest preparePost() throws IOException{
		prepareAll(true);
		return this;
	}

	/**
	 * Writes query to open stream to server
	 * 
	 * @param query String params in format of key1=v1&key2=v2 to open stream to server
	 * @return PPRequest this instance -> for chaining method @see line 22
	 * @throws IOException - should be checked by caller
	 * */
	public PPRequest withData(String query) throws IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.close();
		return this;
	}

	/**
	 * Builds query on format of key1=v1&key2=v2 from given hashMap structure
	 * for map: {name=Bubu, age=29} -> builds "name=Bubu&age=29"
	 * for map: {Iam=Groot} -> builds "Iam=Groot"
	 * 
	 * @param params HashMap consists of key-> value pairs to build query from
	 * @return PPRequest this instance -> for chaining method @see line 22
	 * @throws IOException - should be checked by caller
	 * */
	public PPRequest withData(HashMap<String,String> params) throws IOException{
		String result="",and = "";
        for(Map.Entry<String, String> entry : params.entrySet()){
            result+=and+entry.getKey()+"="+entry.getValue();//concats: key=value (for first param) OR &key=value(second and more)
            if(and.isEmpty())and="&";//& between params, except first so added after first concatenation
        }
        withData(result);
		return this;
	}

	/**
	 * When caller only need to send, and don't need String response from server
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean send() throws IOException{
		boolean status = con.getResponseCode()==HttpURLConnection.HTTP_OK;//Http OK == 200
		done();
		return status; //boolean return to indicate whether it successfully sent
	}

	/**
	 * Sending request to the server and pass to caller String as it received in response from server
	 * 
	 * @return String printed from server's response
	 * @throws IOException - should be checked by caller
	 * */
	public String sendAndReadString() throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line,response="";
        while ((line=br.readLine()) != null)response+=line;
        done();
		return response;
	}

	/**
	 * JSONObject representation of String response from server
	 *
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public JSONObject sendAndReadJSON() throws JSONException, IOException{
		return new JSONObject(sendAndReadString());
	}
}