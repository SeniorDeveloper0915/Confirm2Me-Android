package com.erik.confirm2me;


import android.app.Application;

import com.parse.Parse;


/**
 * Created by iGold on 22.02.15.
 */
public class UIApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppData.getInstance().init(getBaseContext());

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, Global.kParseAppID, Global.kParseClientID);

        //ParseUser.enableAutomaticUser();
        //ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        //ParseACL.setDefaultACL(defaultACL, true);

    }

}
