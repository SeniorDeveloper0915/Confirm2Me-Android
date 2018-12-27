package com.erik.confirm2me.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;

/**
 * Created by alex on 15.08.15.
 */
public class BillingService {

    Context context;
    IInAppBillingService mService;

    //TODO Insert real SKU for Premium User
    public static final String SKU_UPGRADE = "confirm2me_newrequest_fee";


    public BillingService(Context context, IInAppBillingService service){
        this.context = context;
        this.mService = service;
    }

    public Boolean checkIfPurchased(){
        Boolean result = false;

        try {
            Bundle ownedItems = mService.getPurchases(3, context.getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String>  purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String>  signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);

                    if(SKU_UPGRADE.equals(sku)){
                        return true;
                    }
                    // do something with this purchase information
                    // e.g. display the updated list of products owned by user
                }

                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }
        } catch (RemoteException e) {
            Log.e(this.getClass().toString(), e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

}
