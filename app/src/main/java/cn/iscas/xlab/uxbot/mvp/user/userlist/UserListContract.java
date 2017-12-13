package cn.iscas.xlab.uxbot.mvp.user.userlist;

import cn.iscas.xlab.uxbot.entity.UserInfo;
import cn.iscas.xlab.uxbot.mvp.BasePresenter;
import cn.iscas.xlab.uxbot.mvp.BaseView;


/**
 * Created by lisongting on 2017/7/11.
 * 采用MVP架构
 */

public interface UserListContract {

    interface View extends BaseView<Presenter> {

        void showUserInList(UserInfo info);

        void showRefreshError();

        void hideLoading();

        void showInfo(String s);
    }

    interface Presenter extends BasePresenter {

        void requestUserData();

        void deleteUser(String userId);
    }

}
