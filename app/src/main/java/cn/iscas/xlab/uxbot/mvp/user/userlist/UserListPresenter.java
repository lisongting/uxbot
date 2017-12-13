package cn.iscas.xlab.uxbot.mvp.user.userlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;

import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.RecognitionRetrofit;
import cn.iscas.xlab.uxbot.entity.UserFaceResult;
import cn.iscas.xlab.uxbot.entity.UserIdList;
import cn.iscas.xlab.uxbot.entity.UserInfo;
import cn.iscas.xlab.uxbot.util.ImageUtils;
import cn.iscas.xlab.uxbot.util.Util;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lisongting on 2017/7/11.
 */

public class UserListPresenter implements UserListContract.Presenter {

    public static final String TAG = "UserListPresenter";
    private Context context;
    private UserListContract.View view;
    private Handler handler;
    private Retrofit retrofit;
    private RecognitionRetrofit service;

    public UserListPresenter(Context context, UserListContract.View view) {
        this.context = context;
        this.view = view;
        handler = new Handler();
        //在这里就给View绑定了presenter
        view.setPresenter(this);

    }

    @Override
    public void start() {
        StringBuilder baseUrl = new StringBuilder("http://");
        baseUrl.append(Config.ROS_SERVER_IP).append(":").append(Config.RECOGNITION_SERVER_PORT).append("/");

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.toString())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(RecognitionRetrofit.class);

    }

    @Override
    public void requestUserData() {
        //先用flatMap将每个用户的id取出来
        service.getUserIdList()
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Function<UserIdList, Observable<String>>() {
                    @Override
                    public Observable<String> apply(final UserIdList userList) throws Exception {
                        return Observable.fromIterable(userList.getUserList());
                    }
                })
                .map(new Function<String, UserInfo>() {
                    @Override
                    public UserInfo apply(final String id) throws Exception {
                        final UserInfo userInfo = new UserInfo();
                        userInfo.setName(Util.hexStringToString(id));
                        service.getUserFace(id)
                                .subscribe(new Consumer<UserFaceResult>() {
                                    @Override
                                    public void accept(UserFaceResult userFaceResult) throws Exception {
                                        WeakReference<Bitmap> bitmapWeakReference =
                                                new WeakReference<>(ImageUtils.decodeBase64ToBitmap(userFaceResult.getStrFaceImage()));
                                        userInfo.setFace(bitmapWeakReference.get());
                                    }
                                });
                        return userInfo;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(UserInfo userInfo) {
                        view.showUserInList(userInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showRefreshError();
                    }

                    @Override
                    public void onComplete() {
                        view.hideLoading();
                    }
                });
    }

    @Override
    public void deleteUser(String userId) {
        service.deleteUser(userId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 0) {
                         view.showInfo("删除成功");
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        view.showInfo("服务器连接异常");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }



    private void log(String s) {
        Log.i(TAG, s);
    }
}
