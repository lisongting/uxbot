package cn.iscas.xlab.uxbot.mvp.user.register;

import com.google.gson.Gson;

import java.util.HashMap;

import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.RecognitionRetrofit;
import cn.iscas.xlab.uxbot.entity.UserRegisterResult;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lisongting on 2017/12/13.
 */

public class RegisterPresenter implements RegisterContract.Presenter {

    private Retrofit retrofit;
    private RecognitionRetrofit api;
    private RegisterContract.View view;

    public RegisterPresenter(RegisterContract.View v){
        this.view = v;
        start();
    }

    @Override
    public void register(String userId, String strBitmap) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Userid", userId);
        map.put("Image", strBitmap);

        Gson gson = new Gson();
        String strBody = gson.toJson(map);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), strBody);

        api.registerFace(requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserRegisterResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(UserRegisterResult userRegisterResult) {
                        int ret = userRegisterResult.getRet();
                        if (ret == 0) {
                            view.showSuccess();
                        } else if (ret == 11) {
                            view.showInfo("注册失败:图片尺寸过大");
                        } else if (ret == 9) {
                            view.showInfo("注册失败:未检测到人脸");
                        }else {
                            view.showInfo("注册失败，错误码：" + ret);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showInfo("注册失败，请检查网络和服务器配置");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
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

        api = retrofit.create(RecognitionRetrofit.class);
    }
}
