package cn.iscas.xlab.uxbot.mvp.user.register;

import cn.iscas.xlab.uxbot.mvp.BasePresenter;
import cn.iscas.xlab.uxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/13.
 */

public interface RegisterContract {

    interface Presenter extends BasePresenter{
        /**
         * 进行人脸注册[优图盒子]
         * @param userId  用户中文名的十六进制形式
         * @param strBitmap  人脸图片的base64字符串
         */
        void register(String userId, String  strBitmap);


    }

    interface View extends BaseView<Presenter>{
        void showInfo(String s);

        void showSuccess();
    }

}
