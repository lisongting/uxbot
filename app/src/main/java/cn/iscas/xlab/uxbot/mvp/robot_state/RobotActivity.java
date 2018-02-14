package cn.iscas.xlab.uxbot.mvp.robot_state;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.andresoviedo.app.model3D.services.SceneLoader;
import org.andresoviedo.app.model3D.view.ModelHelper;
import org.andresoviedo.app.model3D.view.ModelSurfaceView;

import java.util.Arrays;

/**
 * Created by lisongting on 2017/12/21.
 */

public class RobotActivity extends AppCompatActivity implements ModelHelper.AdapterInterface{

    private static final String TAG = "TestActivity";
    //中间类，作为适配器
    private ModelHelper model;

    private SceneLoader sceneLoader;
    private ModelSurfaceView gLView;

    private String assetDir = "models";
    private String assetFilename = "xbot_model.obj";
    private float[] backgroundColor = new float[]{0,0,0,1};


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        model = new ModelHelper(this, assetDir, assetFilename, null, backgroundColor, this);

        //创建ModelSurfaceView
        gLView = new ModelSurfaceView(model);
        setContentView(gLView);


        //创建3D场景加载器
        sceneLoader = new SceneLoader(model);
        sceneLoader.init();
        sceneLoader.toggleLighting();
        log("LightPosition:" + Arrays.toString(sceneLoader.getLightPosition()));
//        sceneLoader.toggleWireframe();


    }


    @Override
    public void requestRender() {
        if (gLView != null) {
            gLView.requestRender();
        }else {
            Log.e(TAG, "gLView is null");
        }
    }

    @Override
    public SceneLoader getSceneLoader() {
        if (sceneLoader != null) {
            return sceneLoader;
        }
        Log.e(TAG, "SceneLoader is null");
        return null;
    }

    private void log(String s) {
        Log.i("tag", s);
    }
}
