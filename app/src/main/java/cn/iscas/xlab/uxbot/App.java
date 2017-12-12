/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.uxbot;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by lisongting on 2017/10/9.
 */

public class App extends Application {

    public static final String TAG = "App";

    public ServiceConnection mServiceConnection;
    private RosConnectionService.ServiceBinder mServiceProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate()");

        Config.isRosServerConnected = false;

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                log("onServiceConnected()");
                mServiceProxy = (RosConnectionService.ServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Config.isRosServerConnected = false;
                log("onServiceDisconnected()");
            }
        };

        Intent intent = new Intent(this, RosConnectionService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        LeakCanary.install(this);
    }

    public RosConnectionService.ServiceBinder getRosServiceProxy(){
        if (mServiceProxy == null) {
            Log.e("App", "mServiceProxy is null");
        }
        return mServiceProxy;
    }

    @Override
    public void onTerminate(){
        log("onTerminate()");
        unbindService(mServiceConnection);
        super.onTerminate();

    }


    private void log(String s) {
        Log.i(TAG,TAG+" -- "+ s);
    }



}
