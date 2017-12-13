package cn.iscas.xlab.uxbot;


import cn.iscas.xlab.uxbot.entity.FaceRecogResult;
import cn.iscas.xlab.uxbot.entity.UserFaceResult;
import cn.iscas.xlab.uxbot.entity.UserIdList;
import cn.iscas.xlab.uxbot.entity.UserRegisterResult;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit接口类
 * Created by lisongting on 2017/12/12.
 */

public interface RecognitionRetrofit {

    /**
     * 获取所有已注册的用户列表
     * @return 用户列表
     */
    @GET("management/userids")
    Observable<UserIdList> getUserIdList();

    /**
     * 获取某个指定用户的人脸图像
     * @param userId   用户姓名的十六机制字符串
     * @return  返回码+人脸图像
     */
    @GET("face")
    Observable<UserFaceResult> getUserFace(@Query("userid")String userId);

    /**
     * 删除某个用户
     * @param userId  用户姓名的十六机制字符串
     * @return 删除结果，0表示删除成功
     */
    @GET("management/logout")
    Observable<Integer> deleteUser(@Query("userid") String userId);

    /**
     * 进行人脸注册
     * @param requestBody   以键值对形式，放入用户id 和 人脸图片的base64字符串
     * @return  注册结果
     */
    @POST("management/register?method=force")
    Observable<UserRegisterResult> registerFace(@Body RequestBody requestBody);

    /**
     * 进行人脸识别
     * @param requestBody  以键值对形式：放入人脸图片的base64字符串
     * @return 识别结果
     */
    @POST("recognition")
    Observable<FaceRecogResult> recognizeFace(@Body RequestBody requestBody);




}
