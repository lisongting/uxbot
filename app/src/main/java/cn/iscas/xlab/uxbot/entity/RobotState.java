package cn.iscas.xlab.uxbot.entity;

/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotState {

    /**
     * 电量百分比,取值[0,100]
     */
    private int powerPercent ;

    /**
     * 升降台高度百分比，取值[0,100]
     */
    private int heightPercent ;

    /**
     * 云台角度,取值[-90,90]
     */
    private int cloudDegree ;

    /**
     * 摄像头角度，取值[-45,45]
     */
    private int cameraDegree ;

    public RobotState(){}

    public RobotState(int powerPercent, int heightPercent, int cloudDegree, int cameraDegree) {
        this.powerPercent = powerPercent;
        this.heightPercent = heightPercent;
        this.cloudDegree = cloudDegree;
        this.cameraDegree = cameraDegree;
    }

    public int getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(int powerPercent) {
        this.powerPercent = powerPercent;
    }

    public int getHeightPercent() {
        return heightPercent;
    }

    public void setHeightPercent(int heightPercent) {
        this.heightPercent = heightPercent;
    }

    public int getCloudDegree() {
        return cloudDegree;
    }

    public void setCloudDegree(int cloudDegree) {
        this.cloudDegree = cloudDegree;
    }

    public int getCameraDegree() {
        return cameraDegree;
    }

    public void setCameraDegree(int cameraDegree) {
        this.cameraDegree = cameraDegree;
    }

    @Override
    public String toString() {
        return "RobotState{" +
                "powerPercent=" + powerPercent +
                ", heightPercent=" + heightPercent +
                ", cloudDegree=" + cloudDegree +
                ", cameraDegree=" + cameraDegree +
                '}';
    }


}
