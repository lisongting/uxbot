package cn.iscas.xlab.uxbot.mvp.user;

import cn.iscas.xlab.uxbot.mvp.BasePresenter;
import cn.iscas.xlab.uxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/11.
 */

public interface UserContract {

    interface Presenter extends BasePresenter{

    }

    interface View extends BaseView<Presenter>{

    }

}
