package cn.iscas.xlab.uxbot.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lisongting on 2017/12/12.
 * 返回已注册的用户列表
 * 请求示例：
 * GET http://IP:8000/management/userids
 */

public class UserIdList {

    /**
     * Ret : 0
     * Userids : ["e4bbbbe6b5b7e6b3a2","e6b1aae9b98f","e69fb4e995bfe59da4","e68888e69da8","e69d8ee69dbee5bbb7","e8928be5bbbae698a5"]
     */

    /**
     * 返回状态码，Ret=0，获取成功。
     */
    @SerializedName("Ret")
    private int ret;

    /**
     * 已注册人脸的Userid列表。
     */
    @SerializedName("Userids")
    private List<String> userList;

    public UserIdList() {

    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "UserIdList{" +
                "ret=" + ret +
                ", userList=" + userList +
                '}';
    }
}
