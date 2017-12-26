package cn.iscas.xlab.uxbot.entity;

/**
 * Created by lisongting on 2017/12/25.
 */

public class FaceRegisterResultFly {

    /**
     * ret : 0
     * suc : 1
     * rgn : 1
     * sst : enroll
     * ssub : ifr
     * fid : 2e7d53cb78deadc67518dd53ac2e2a3a
     */

    //返回值，0为成功，-1为失败
    private int ret;

    //是否成功,成功则为1
    private int suc;
    //
    private int rgn;
    private String sst;
    private String ssub;
    private String fid;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public int getSuc() {
        return suc;
    }

    public void setSuc(int suc) {
        this.suc = suc;
    }

    public int getRgn() {
        return rgn;
    }

    public void setRgn(int rgn) {
        this.rgn = rgn;
    }

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }

    public String getSsub() {
        return ssub;
    }

    public void setSsub(String ssub) {
        this.ssub = ssub;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }
}
