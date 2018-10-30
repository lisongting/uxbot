package cn.iscas.xlab.uxbot;

/**
 * Created by lisongting on 2017/10/9.
 */

public class Constant {

    //广播Intent中用于存放ros连接状态的key
    public static final String KEY_BROADCAST_ROS_CONN = "ros_conn_status";

    //订阅:base64地图数据的topic
    public static final String SUBSCRIBE_TOPIC_MAP = "/base64_img/map_img";

    //订阅:Xbot状态的topic
    public static final String SUBSCRIBE_TOPIC_ROBOT_STATE = "/mobile_base/xbot/state";

    //订阅：电池电量
    public static final String SUBSCRIBE_TOPIC_BATTERY_PERCENT = "/mobile_base/sensors/battery";

    //订阅：水平云台的角度的topic
    public static final String SUBSCRIBE_TOPIC_YAW_PLATFORM_STATE = "/mobile_base/sensors/yaw_platform_degree";

    public static final String SUBSCRIBE_TOPIC_PITCH_PLATFORM_STATE = "/mobile_base/sensors/pitch_platform_degree";


    //发布：用于控制Xbot移动的topic
    public static final String PUBLISH_TOPIC_CMD_MOVE = "/cmd_vel_mux/input/teleop";

    //发布：用于控制摄像头角度和云台角度的topic
//    public static final String PUBLISH_TOPIC_CMD_CLOUD_CAMERA = "/mobile_base/commands/cloud_camera";

    //发布：用于控制水平云台的角度的topic
    public static final String PUBLISH_TOPIC_YAW_PLATFORM = "/mobile_base/commands/yaw_platform";

    //发布：控制垂直云台的
    public static final String PUBLISH_TOPIC_PITCH_PLATFORM = "/mobile_base/commands/pitch_platform";

    //发布：用于控制电机电源的topic true为关闭，false为开启
    public static final String PUBLISH_TOPIC_CMD_MACHINERY_POWER = "/mobile_base/commands/motor_disable";

    //用来表示Ros服务器的连接状态:连接成功
    public static final int CONN_ROS_SERVER_SUCCESS = 0x11;

    //连接失败
    public static final int CONN_ROS_SERVER_ERROR = 0x12;

    //Intentfilter的action值，用于区别广播
    public static final String ROS_RECEIVER_INTENTFILTER = "xbotplayer.rosconnection.receiver";

    //rgb图像的URL后缀
    public static final String CAMERA_RGB_RTMP_SUFFIX = ":8080/stream_viewer?topic=/camera/color/image_raw";

    public static final String CAMERA_RGB_STREAM_PREFIX = "http://";


}
