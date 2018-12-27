package com.erik.confirm2me.PromisePay;

import java.util.HashMap;

/**
 * Created by Kevin Hakans on 1/14/16.
 */
public class PPCard {

    /**
     * The card number.
     */
    private final String number;

    /**
     * The name on card.
     */
    private final String fullName;

    /**
     * The expiration month as a one or two-digit number on the Gregorian calendar.
     */
    private final String expiryMonth;

    /**
     * The expiration year as a two or four-digit number on the Gregorian calendar.
     */
    private final String expiryYear;

    /**
     * The card CVV.
     */
    private final String cvv;


    /**
     * Constructor - Create instance of PPCard
     *
     * @param number - The card number.
     * @param fullName - The name on card.
     * @param expiryMonth - The expiration month as a one or two-digit number on the Gregorian calendar.
     * @param expiryYear - The expiration year as a two or four-digit number on the Gregorian calendar.
     * @param cvv - The card CVV.
     */
    public PPCard(String number, String fullName, String expiryMonth, String expiryYear, String cvv) {
        this.number = number;
        this.fullName = fullName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
    }

    /**
     * Convert parameters as HasMap to post to server
     *
     * @return
     */
    public HashMap<String, String> parameters() {
        HashMap<String, String>params = new HashMap<>();
        if (this.number != null)
            params.put("number", this.number);
        if (this.fullName != null)
            params.put("full_name", this.fullName);
        if (this.expiryMonth != null)
            params.put("expiry_month", this.expiryMonth);
        if (this.expiryYear != null)
            params.put("expiry_year", this.expiryYear);
        if (this.cvv != null)
            params.put("cvv", this.cvv);

        return params;
    }
}
