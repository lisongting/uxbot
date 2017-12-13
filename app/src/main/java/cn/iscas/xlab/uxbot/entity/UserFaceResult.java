package cn.iscas.xlab.uxbot.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lisongting on 2017/12/12.
 * 请求接口：http://IP:8000/face
    请求示例：GET http://IP:8000/face?userid=xiaoming
 */

public class UserFaceResult {

    @SerializedName("Ret")
    private int ret;

    @SerializedName("Image")
    private String strFaceImage;

    public UserFaceResult() {

    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getStrFaceImage() {
        return strFaceImage;
    }

    public void setStrFaceImage(String strFaceImage) {
        this.strFaceImage = strFaceImage;
    }

    @Override
    public String toString() {
        return "UserFaceResult{" +
                "ret=" + ret +
                ", strFaceImage='" + strFaceImage + '\'' +
                '}';
    }
}
