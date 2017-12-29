package cn.iscas.xlab.uxbot.entity;

import java.util.List;

/**
 * Created by lisongting on 2017/12/25.
 */

public class FaceRecogResultFly {


    /**
     * ret : 0
     * group_id : 3588979238
     * group_name : group
     * ifv_result : {"candidates":[{"model_id":"6deae78b4ce205436f3a614a4d736301","decision":"accepted","score":88.896492,"user":"e69d8ee69dbee5bbb7"}]}
     * sst : identify
     * ssub : ifr
     * topc : 1
     */

    private int ret;
    private String group_id;
    private String group_name;
    private IfvResultBean ifv_result;
    private String sst;
    private String ssub;
    private int topc;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public IfvResultBean getIfv_result() {
        return ifv_result;
    }

    public void setIfv_result(IfvResultBean ifv_result) {
        this.ifv_result = ifv_result;
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

    public int getTopc() {
        return topc;
    }

    public void setTopc(int topc) {
        this.topc = topc;
    }

    public static class IfvResultBean {
        private List<CandidatesBean> candidates;

        public List<CandidatesBean> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<CandidatesBean> candidates) {
            this.candidates = candidates;
        }

        public static class CandidatesBean {
            /**
             * model_id : 6deae78b4ce205436f3a614a4d736301
             * decision : accepted
             * score : 88.896492
             * user : e69d8ee69dbee5bbb7
             */

            private String model_id;
            private String decision;
            private double score;
            private String user;

            public String getModel_id() {
                return model_id;
            }

            public void setModel_id(String model_id) {
                this.model_id = model_id;
            }

            public String getDecision() {
                return decision;
            }

            public void setDecision(String decision) {
                this.decision = decision;
            }

            public double getScore() {
                return score;
            }

            public void setScore(double score) {
                this.score = score;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }
        }
    }
}
