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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.uxbot.entity.PublishEvent;
import cn.iscas.xlab.uxbot.entity.RobotState;
import cn.iscas.xlab.uxbot.entity.Twist;
import cn.iscas.xlab.uxbot.ros.ROSClient;
import cn.iscas.xlab.uxbot.ros.rosbridge.ROSBridgeClient;
import de.greenrobot.event.EventBus;

/**
 * Created by lisongting on 2017/6/5.
 *
 */

public class RosConnectionService extends Service {

    public static final String TAG = "RosConnectionService";


    public Binder proxy = new ServiceBinder();
    private ROSBridgeClient rosBridgeClient;

    private boolean isConnected = false;
    private Timer rosConnectionTimer;
    private TimerTask connectionTask;
    //上一次更新机器人状态界面的时间
    private long lastUpdateStateTime;

    public class ServiceBinder extends Binder {
        public boolean isConnected(){
            return isConnected;
        }

        /**
         * 发布message控制机器人移动，消息类型为twist
         * @param twist 消息类型
         */
        public void publishCommand(Twist twist) {
            if (isConnected()) {
                JSONObject body = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject linearMsg = new JSONObject();
                JSONObject angularMsg = new JSONObject();
                try {

                    linearMsg.put("x", twist.getLinear_x());
                    linearMsg.put("y", twist.getLinear_y());
                    linearMsg.put("z", twist.getLinear_z());
                    angularMsg.put("x", twist.getAngular_x());
                    angularMsg.put("y", twist.getAngular_y());
                    angularMsg.put("z", twist.getAngular_z());
                    jsonArray.put(linearMsg);
                    jsonArray.put(angularMsg);

                    body.put("op", "publish");
                    body.put("topic", Constant.PUBLISH_TOPIC_CMD_MOVE);

                    JSONObject message = new JSONObject();
                    message.put("angular", angularMsg);
                    message.put("linear", linearMsg);
                    body.put("msg", message);

                    rosBridgeClient.send(body.toString());
                    Log.v(TAG, "publish "+Constant.PUBLISH_TOPIC_CMD_MOVE+" to Ros Server :\n" + body.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        /**
         *订阅某个topic或者取消订阅某个topic
         */
        public void manipulateTopic(String topic, boolean isSubscribe) {
            if (isConnected()) {
                //订阅
                if (isSubscribe) {
                    JSONObject subscribeMuseumPos = new JSONObject();
                    try {
                        subscribeMuseumPos.put("op", "subscribe");
                        subscribeMuseumPos.put("topic", topic);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    rosBridgeClient.send(subscribeMuseumPos.toString());
                } else {//取消订阅
                    JSONObject subscribeMuseumPos = new JSONObject();
                    try {
                        subscribeMuseumPos.put("op", "unsubscribe");
                        subscribeMuseumPos.put("topic", topic);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    rosBridgeClient.send(subscribeMuseumPos.toString());
                }
                if (topic.equals(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE)) {
                    lastUpdateStateTime = System.currentTimeMillis();
                }
            }
        }

        /**
         * 发布message控制升降台高度
         * @param percent  高度百分比[0,100]
         */
//        public void sendLiftHeightMsg(int percent) {
//            JSONObject msg = new JSONObject();
//            JSONObject body = new JSONObject();
//            try {
//                msg.put("height_percent", percent);
//                body.put("op", "publish");
//                body.put("topic", Constant.PUBLISH_TOPIC_CMD_LIFT);
//                body.put("msg", msg);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            rosBridgeClient.send(body.toString());
//            //Log.v(TAG, "publish "+Constant.PUBLISH_TOPIC_CMD_LIFT+" to Ros Server :\n" + body.toString());
//        }

        /**
         * 发布Message控制云台旋转角度和摄像头角度
         * @param cloudDegree  云台角度
         * @param cameraDegree 摄像头角度
         */
        public void sendCloudCameraMsg(int cloudDegree, int cameraDegree){
            JSONObject msg = new JSONObject();
            JSONObject body = new JSONObject();
            try {
                msg.put("cloud_degree", cloudDegree);
                msg.put("camera_degree", cameraDegree);
                body.put("op", "publish");
                body.put("topic", Constant.PUBLISH_TOPIC_CMD_CLOUD_CAMERA);
                body.put("msg", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rosBridgeClient.send(body.toString());
            //Log.v(TAG, "publish "+Constant.PUBLISH_TOPIC_CMD_CLOUD_CAMERA+" to Ros Server :\n" + body.toString());
        }

        /**
         * 发布Message控制电机电源开关
         * @param activate true 表示打开电源，false表示关闭
         */
        public void sendElectricMachineryMsg(boolean activate) {
            JSONObject msg = new JSONObject();
            JSONObject body = new JSONObject();
            try {
                if (activate) {
                    msg.put("power", true);
                } else {
                    msg.put("power", false);
                }
                body.put("op", "publish");
                body.put("topic", Constant.PUBLISH_TOPIC_CMD_MACHINERY_POWER);
                body.put("msg", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rosBridgeClient.send(body.toString());
            //Log.v(TAG, "publish "+Constant.PUBLISH_TOPIC_CMD_MACHINERY_POWER+" to Ros Server :\n" + body.toString());

        }

        public void disConnect() {
            connectionTask.cancel();
            rosBridgeClient.disconnect();
        }
    }

    //onCreate()会自动触发Ros服务器的连接
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "RosConnService--onCreate()");
        rosConnectionTimer = new Timer();
        connectionTask = new TimerTask() {
            @Override
            public void run() {
                if (!isConnected) {
                    String rosURL = "ws://" + Config.ROS_SERVER_IP + ":" + Config.ROS_SERVER_PORT;
                    //Log.v(TAG, "Connecting to ROS Server: " + rosURL);
                    rosBridgeClient = new ROSBridgeClient(rosURL);
                    boolean conneSucc = rosBridgeClient.connect(new ROSClient.ConnectionStatusListener() {
                        @Override
                        public void onConnect() {
//                            rosBridgeClient.setDebug(true);
                            Log.i(TAG, "Ros ConnectionStatusListener--onConnect");

                        }

                        @Override
                        public void onDisconnect(boolean normal, String reason, int code) {
                            //Log.v(TAG, "Ros ConnectionStatusListener--disconnect");
                            Intent broadcastIntent = new Intent(Constant.ROS_RECEIVER_INTENTFILTER);
                            Bundle data = new Bundle();
                            data.putInt("ros_conn_status", Constant.CONN_ROS_SERVER_ERROR);
                            broadcastIntent.putExtras(data);
                            sendBroadcast(broadcastIntent);
                            isConnected = false;
                        }

                        @Override
                        public void onError(Exception ex) {
                            ex.printStackTrace();
                            Log.i(TAG, "Ros ConnectionStatusListener--ROS communication error");
                        }
                    });
                    isConnected = conneSucc;
                    Intent broadcastIntent = new Intent(Constant.ROS_RECEIVER_INTENTFILTER);
                    Bundle data = new Bundle();
                    if (!isConnected) {
                        data.putInt(Constant.KEY_BROADCAST_ROS_CONN, Constant.CONN_ROS_SERVER_ERROR);
                        broadcastIntent.putExtras(data);
                        sendBroadcast(broadcastIntent);
                    } else{
                        data.putInt(Constant.KEY_BROADCAST_ROS_CONN, Constant.CONN_ROS_SERVER_SUCCESS);
                        broadcastIntent.putExtras(data);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(broadcastIntent);
                    }
                }
            }
        };

        startRosConnectionTimer();
        //注册Eventbus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, TAG + " -- onRebind()");
        super.onRebind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "RosConnService--onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "RosConnectionService onBind()");

        return proxy;
    }

    //订阅某个topic后，接收到Ros服务器返回的message，回调此方法
    public void onEvent(PublishEvent event) {
        //topic的名称
        String topicName = event.name;
        if (event.msg.length() < 500) {
            Log.v(TAG, "onEvent:" + event.msg);
        } else {
            Log.v(TAG,"got base64 map string");
        }
        String response = event.msg;
        if (topicName.equals(Constant.SUBSCRIBE_TOPIC_MAP)) {
            try {
                JSONObject object = new JSONObject();
                object.put("topicName", topicName);
                object.put("data", new JSONObject(response).get("data"));
                if (response.length()>100) {
                    EventBus.getDefault().post(object.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (topicName.equals(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE)) {
            long currentTime = System.currentTimeMillis();
            //5秒更新一次界面，如果更新太快的话，则影响实际控制指令的发出
            if (currentTime - lastUpdateStateTime > 5000) {
                try {
                    JSONObject responseObj = new JSONObject(response);
                    int powerPercent = responseObj.getInt("power_percent");
                    int heightPercent = responseObj.getInt("height_percent");
                    int cloudDegree = responseObj.getInt("cloud_degree");
                    int cameraDegree = responseObj.getInt("camera_degree");

                    EventBus.getDefault().post(new RobotState(powerPercent, heightPercent, cloudDegree, cameraDegree));
                } catch (JSONException e) {
//                    e.printStackTrace();
                }
                lastUpdateStateTime = currentTime;
            }
        }


    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "RosConnService--onDestroy()");
        EventBus.getDefault().unregister(this);
        rosConnectionTimer.cancel();
        super.onDestroy();
    }

    public void startRosConnectionTimer() {
        Log.i(TAG, "isConnected:" + isConnected);
        if (!isConnected) {
            rosConnectionTimer.schedule(connectionTask,0,3000);
        }
    }

}
