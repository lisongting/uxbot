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

/**
 * Created by lisongting on 2017/9/27.
 */

public class Config {

    //标记ROS服务端是否连接成功
    public static boolean isRosServerConnected = false;

    //Ros服务器IP
    public static String ROS_SERVER_IP = "192.168.0.135";

    //ROS服务端的端口
    public static final String ROS_SERVER_PORT = "9090";

    //控制Xbot进行移动的速度
    public static double speed = 0.3;

}
