package cn.iscas.xlab.uxbot.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lisongting on 2017/12/12.
 * 用户人脸识别的返回结果
 *
 * 请求接口：
 * [POST]  http://IP:8000/recognition
 * POST请求参数：
 * Image	String	  base64 编码的二进制图片数据。
 */

public class FaceRecogResult {

    /**
     * 系统对结果判断的置信度，用户可根据confidence大小确定是否接受识别结果。
     */
    @SerializedName("Confidence")
    private float confidence;

    /**
     * 识别出的人员ID。中文名的十六进制形式
     */
    @SerializedName("Id")
    private String id;

    /**
     * 返回状态码，Ret=0，正常。
     */
    @SerializedName("Ret")
    private int ret;

    public FaceRecogResult(){}

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    @Override
    public String toString() {
        return "FaceRecogResult{" +
                "confidence=" + confidence +
                ", id='" + id + '\'' +
                ", ret=" + ret +
                '}';
    }
}
