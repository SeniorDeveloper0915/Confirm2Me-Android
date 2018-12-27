package com.erik.confirm2me.PromisePay;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;


/**
 * Created by Kevin Hakans on 1/14/16.
 */
public class PromisePay {

    /**
     * The instance.
     */
    private static PromisePay instance;

    /**
     * The environment.
     */
    private String environment;

    /**
     * The publicKey.
     */
    private String publicKey;

    /**
     * The apiUrl.
     */
    private String apiUrl;


    /**
     * Get the PromisePay Instace
     *
     * @return
     */
    public static PromisePay getInstance() {
        if (instance == null) {
            instance = new PromisePay();
        }
        return instance;
    }

    /**
     * Configuration method
     *
     * @param environment - Environment of package ("prelive" or "prod")
     * @param publicKey Public key provided by PromisePay.
     */
    public void initialize(String  environment, String publicKey) {
        this.environment = environment;
        this.publicKey = publicKey;
        if (environment.equals("dev"))
            this.apiUrl = "http://api.localhost.local:3000/";
        else if (environment.equals("prelive"))
            this.apiUrl = "https://test.api.promisepay.com/";
        else if (environment.equals("prod"))
            this.apiUrl = "https://secure.api.promisepay.com/";
    }

    /**
     * Create Card Account method
     *
     * @param cardToken - A one-time use token that lasts for 20 minutes. Generate using https://promisepay-api.readme.io/docs/generate-a-card-token. It is assigned to a user within the marketplace to authorize the creation of a card account. It is NOT the token for a credit account that is generated, that is the 'Card Account ID'.
     * @param card - Full card information (eg. { full_name: "Bobby Buyer", number: "4111111111111111", expiry_month: "02", expiry_year: "2018", cvv: "123" })
     * @param onPromiseRequestListener - Custom function to call back once card creation is a success or failure
     */
    public void createCardAccount(String cardToken, PPCard card, OnPromiseRequestListener onPromiseRequestListener) {
        String url = String.format("%scard_accounts?card_token=%s", this.apiUrl, cardToken);
        httpRequest(url, card.parameters(), onPromiseRequestListener);
    }

    /**
     * Listener Interface
     */
    public interface OnPromiseRequestListener {
        public void onSuccess(JSONObject response);
        public void onError(Exception e);
    }

    /**
     * Post request to PromisePay API Server
     *
     * @param url - The url of request.
     * @param parameters -  The parameters of request.
     * @param onPromiseRequestListener - The listener of response.
     */
    public void httpRequest(String url, HashMap<String, String> parameters, OnPromiseRequestListener onPromiseRequestListener) {
        try {
            PPRequest request = new PPRequest(url);
            try {
                onPromiseRequestListener.onSuccess(request.preparePost().withData(parameters).sendAndReadJSON());
            } catch (JSONException e) {
                onPromiseRequestListener.onError(e);
                e.printStackTrace();
            } catch (IOException e) {
                onPromiseRequestListener.onError(e);
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            onPromiseRequestListener.onError(e);
            e.printStackTrace();
        }
    }
}
