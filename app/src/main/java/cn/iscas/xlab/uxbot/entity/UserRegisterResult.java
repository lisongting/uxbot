package cn.iscas.xlab.uxbot.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lisongting on 2017/12/12.
 * 返回用户注册的结果
 *
 * 请求接口：http://IP:8000/management/register
 * 请求示例：
 * [POST] http://IP:8000/management/register?method=normal，
 * normal:普通注册，人脸ID存在，返回结果RET=9，注册失败；
 * [POST] http://IP:8000/management/register?method=force，
 * force：强制注册，人脸ID存在强制覆盖。
 *
 * POST请求参数：
 * Userid	String	人员ID(中文名的十六进制形式)
 * Image	String	base64 编码的二进制图片数据(图片大小要小于1280x720)
 */

public class UserRegisterResult {

    /**
     * 带人脸框与人脸五点的图片（base64 编码的二进制数据）。
     用户可根据人脸框和五点的位置决定是否需要重新注册
     */
    @SerializedName("Image")
    private String strImage;

    /**
     * 返回状态码，Ret=0，注册成功。
     */
    @SerializedName("Ret")
    private int ret ;




}
