package cn.iscas.xlab.uxbot.entity;

/**
 * Created by lisongting on 2017/12/25.
 */

public class FaceRecogResultFly {


    /**
     * ret : 0
     * face_score : 99.999
     * voice_score : 0
     * ssub : ifr
     * decision : accepted
     * fusion_score : 99.999
     * sst : verify
     */

    private int ret;

    //人脸验证的得分（验证时返回）
    private double face_score;

    //
    private int voice_score;
    private String ssub;
    private String decision;
    private double fusion_score;
    private String sst;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public double getFace_score() {
        return face_score;
    }

    public void setFace_score(double face_score) {
        this.face_score = face_score;
    }

    public int getVoice_score() {
        return voice_score;
    }

    public void setVoice_score(int voice_score) {
        this.voice_score = voice_score;
    }

    public String getSsub() {
        return ssub;
    }

    public void setSsub(String ssub) {
        this.ssub = ssub;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public double getFusion_score() {
        return fusion_score;
    }

    public void setFusion_score(double fusion_score) {
        this.fusion_score = fusion_score;
    }

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }
}
