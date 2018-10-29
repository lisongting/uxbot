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
package cn.iscas.xlab.uxbot.mvp.robot_state;

import android.content.Context;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;

import cn.iscas.xlab.uxbot.Constant;
import cn.iscas.xlab.uxbot.RosConnectionService;
import cn.iscas.xlab.uxbot.entity.PitchPlatformDegree;
import cn.iscas.xlab.uxbot.entity.PowerPercent;
import cn.iscas.xlab.uxbot.entity.YawPlatformDegree;
import de.greenrobot.event.EventBus;


/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStatePresenter implements RobotStateContract.Presenter {

    private static final String TAG = "RobotStatePresenter";
    private RosConnectionService.ServiceBinder serviceProxy;

    RobotStateContract.View view;
    private Context context;

    public RobotStatePresenter(Context context,RobotStateContract.View v){
        view = v;
        this.context = context;
        view.setPresenter(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }

    @Override
    public void subscribeRobotState() {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
//        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE, true);
        serviceProxy.advertise(Constant.PUBLISH_TOPIC_CMD_MACHINERY_POWER,"std_msgs/Bool");
        serviceProxy.advertise(Constant.PUBLISH_TOPIC_PITCH_PLATFORM,"std_msgs/Int8");
        serviceProxy.advertise(Constant.PUBLISH_TOPIC_YAW_PLATFORM,"std_msgs/Int8");
        serviceProxy.advertise(Constant.SUBSCRIBE_TOPIC_YAW_PLATFORM_STATE,"std_msgs/Int8");
        serviceProxy.advertise(Constant.SUBSCRIBE_TOPIC_PITCH_PLATFORM_STATE,"std_msgs/Int8");
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_BATTERY_PERCENT, true);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_YAW_PLATFORM_STATE, true);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_PITCH_PLATFORM_STATE, true);
    }

    @Override
    public void unSubscribeRobotState() {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
//        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE,false);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_BATTERY_PERCENT, false);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_YAW_PLATFORM_STATE, false);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_PITCH_PLATFORM_STATE, false);
    }

//    @Override
//    public void publishCloudCameraMsg(int cloudDegree, int cameraDegree) {
//        if (serviceProxy == null) {
//            Log.e(TAG,"serviceProxy is null");
//            return;
//        }
//        serviceProxy.sendCloudCameraMsg(cloudDegree, cameraDegree);
//    }

    @Override
    public void publishElectricMachineryMsg(boolean isDisable) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendElectricMachineryMsg(isDisable);
    }

    @Override
    public void publishYawPlatFormMsg(int degree) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendYawPlatformDegree(degree);
    }

    @Override
    public void publishPitchPlatFormMsg(int degree) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendPitchPlatformDegree(degree);
    }


    public void onEvent(PowerPercent powerPercent) {
        view.updateBattery(powerPercent.getValue());
    }

    public void onEvent(PitchPlatformDegree degree) {
        view.updatePitchPlatForm(degree.getValue());
    }

    public void onEvent(YawPlatformDegree degree) {
        view.updateYawPlatForm(degree.getValue());
    }


    public void reset() {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
//        serviceProxy.sendCloudCameraMsg(0, 0);
        serviceProxy.sendPitchPlatformDegree(0);
        serviceProxy.sendYawPlatformDegree(0);
    }

    @Override
    public void destroy(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void start() {

    }
}
