package cn.iscas.xlab.uxbot.mvp.user.recognize;

import android.graphics.Bitmap;

import com.iflytek.cloud.IdentityListener;

import cn.iscas.xlab.uxbot.mvp.BasePresenter;
import cn.iscas.xlab.uxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/14.
 */

public interface RecogContract {

    interface Presenter extends BasePresenter{

        void recognize(String strBitmap);

        void destroy();

        void recognizeIFly(Bitmap b, IdentityListener listener);
    }

    interface View extends BaseView<Presenter> {

        void showRecognitionSuccess(String userName);

        void showError(String s);
    }


}
