package cn.iscas.xlab.uxbot.mvp.user.recognize;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.RecognitionRetrofit;
import cn.iscas.xlab.uxbot.entity.FaceRecogResult;
import cn.iscas.xlab.uxbot.util.Util;
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
 * Created by lisongting on 2017/12/14.
 */

public class RecogPresenter implements RecogContract.Presenter {

    private static final String TAG = "RecogPresenter";
    private RecogContract.View view ;
    private RecognitionRetrofit api;
    private Retrofit retrofit;
    private Disposable disposable;
    private Context context;

    private IdentityVerifier identityVerifier;

    public RecogPresenter(Context context,RecogContract.View view) {
        this.view = view;
        this.context = context;
        view.setPresenter(this);
        start();
    }

    @Override
    public void recognize(String strBitmap) {

        HashMap<String, String> map = new HashMap<>();
        map.put("Image", strBitmap);
        Gson gson = new Gson();
        String str = gson.toJson(map);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), str);
        api.recognizeFace(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FaceRecogResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(FaceRecogResult faceRecogResult) {
                        log("recognize Result:" + faceRecogResult);
                        if (faceRecogResult.getRet() == 0&&
                                faceRecogResult.getConfidence()>0.3) {
                            view.showRecognitionSuccess(Util.hexStringToString(faceRecogResult.getId()));
                        } else if (faceRecogResult.getRet() == 1000) {
                            view.showRecognitionSuccess("未注册用户");
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showError("请检查网络和服务器配置");
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(RecognitionRetrofit.class);

        identityVerifier = IdentityVerifier.getVerifier();
        if (identityVerifier == null) {
            identityVerifier = IdentityVerifier.createVerifier(context, new InitListener() {
                @Override
                public void onInit(int i) {
                    Log.i("test", "identityVerifier init:" + i);
                }
            });
        }
    }

    public void destroy(){
        if (disposable != null) {
            disposable.dispose();
        }
    }

    //使用科大讯飞来进行人脸识别
    @Override
    public void recognizeIFly(Bitmap b, IdentityListener listener) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();

        //设置会话场景，人脸模式
        identityVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        //设置会话类型
        identityVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        //设置验证模式，"sin"表示单一验证模式
        identityVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        String param = "scope=group,group_id=3588979238";

        identityVerifier.startWorking(listener);
        identityVerifier.writeData("ifr", param, data, 0, data.length);
        identityVerifier.stopWrite("ifr");
        b.recycle();
    }

    private void log(String s) {
        Log.i(TAG, s);
    }
}
