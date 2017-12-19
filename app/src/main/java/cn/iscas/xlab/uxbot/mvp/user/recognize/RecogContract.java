package cn.iscas.xlab.uxbot.mvp.user.recognize;

import cn.iscas.xlab.uxbot.mvp.BasePresenter;
import cn.iscas.xlab.uxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/14.
 */

public interface RecogContract {

    interface Presenter extends BasePresenter{

        void recognize(String strBitmap);

        void destroy();
    }

    interface View extends BaseView<Presenter> {

        void showRecognitionSuccess(String userName);

        void showError(String s);
    }


}
