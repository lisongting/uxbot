/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.uxbot.mvp.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.Constant;
import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.customview.RockerView;
import cn.iscas.xlab.uxbot.entity.Twist;
import cn.iscas.xlab.uxbot.util.Util;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by lisongting on 2017/10/20.
 */

public class ControlFragment extends Fragment implements ControlContract.View {

    public static final String TAG = "ControlFragment";
    private SurfaceView surfaceView;
    private RelativeLayout topBar,bottomBar,infoView;
    private ImageButton btPlayState,btFullScreen;
    private RockerView rockerView;
    private ImageView infoImage;
    private TextView infoText;
    private TextView title;
    private RelativeLayout relativeLayout;

    private ControlContract.Presenter presenter;
    private String[] videoList = {"彩色图像","深度图像"};
    private boolean isMenuOpened ;
    private boolean isLoadingFailed;
    private boolean isPlaying;

    private Animation waitAnimation;
    private float speed ;
    private Timer timer;
    private volatile Twist rockerTwist;
    private IjkMediaPlayer mediaPlayer;
    private String rtmpAddress;
    private Handler handler;
    private String videoTitle ;
    //视频的比例
    private final float scale = 640f / 480f;

    //用于隐藏菜单
    private static final int MSG_FLAG_HIDEN_MENU = 1;
    //用于显示超时
    private static final int MSG_FLAG_LOADING = 2;

    public ControlFragment() {
        rockerTwist = new Twist();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_FLAG_HIDEN_MENU && topBar.getVisibility() == View.VISIBLE) {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    isMenuOpened = false;
                } else if (msg.what == MSG_FLAG_LOADING && infoView.getVisibility() == View.VISIBLE) {
                    showLoadingFailed();
                }

                return true;
            }
        });
        isLoadingFailed = false;
        isMenuOpened = true;
        isPlaying = false;
        videoTitle = videoList[0];

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control_landscape, null);
        surfaceView = (SurfaceView) v.findViewById(R.id.sv_video);
        topBar = (RelativeLayout) v.findViewById(R.id.top_bar);
        bottomBar = (RelativeLayout) v.findViewById(R.id.bottom_bar);
        infoView = (RelativeLayout) v.findViewById(R.id.info_view);
        btPlayState = (ImageButton) v.findViewById(R.id.ib_play_state);
        btFullScreen = (ImageButton) v.findViewById(R.id.ib_screen_state);
        rockerView = (RockerView) v.findViewById(R.id.rocker_view);
        infoImage = (ImageView)v.findViewById(R.id.info_image);
        infoText = (TextView) v.findViewById(R.id.info_text);
        title = (TextView) v.findViewById(R.id.tv_title);
        relativeLayout = (RelativeLayout) v.findViewById(R.id.view_parent);

        initView();
        return v;

    }

    @Override
    public void initView() {
        waitAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.watting_anim);
        waitAnimation.setInterpolator(new LinearInterpolator());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();

        DisplayMetrics metrics = Util.getScreenInfo(getContext());
        log(metrics.toString());
        int small = metrics.widthPixels > metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;
        params.width = (int) (small*0.6 * scale);
        params.height = (int) (small*0.6);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.setLayoutParams(params);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) topBar.getLayoutParams();
        params2.width = (int) (small*0.6 * scale);
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        topBar.setLayoutParams(params2);

        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) bottomBar.getLayoutParams();
        params3.width = (int) (small * 0.6 * scale);
        params3.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bottomBar.setLayoutParams(params3);

        log("size:" + params.width + "x" + params.height);
        log("size:" + params2.width + "x" + params2.height);
        log("size:" + params3.width + "x" + params3.height);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        log("onResume()");
        super.onResume();
        if (Config.isRosServerConnected) {
            rockerView.setAvailable(true);
        } else {
            rockerView.setAvailable(false);
        }
        initOnClickListeners();
    }

    private void initOnClickListeners() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuOpened) {
                    topBar.setVisibility(View.VISIBLE);
                    bottomBar.setVisibility(View.VISIBLE);
                    //5秒内无操作则隐藏菜单
                    handler.sendEmptyMessageDelayed(MSG_FLAG_HIDEN_MENU, 5000);
                } else {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    handler.removeMessages(MSG_FLAG_HIDEN_MENU);
                }
                isMenuOpened = !isMenuOpened;
            }
        });

        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoadingFailed) {
                    rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
                    play(rtmpAddress);
                    showLoading();
                    isLoadingFailed = false;
                }
            }
        });


        btPlayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    //暂停播放，则把按钮设置为play
                    btPlayState.setBackgroundResource(R.drawable.ic_play);
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                        mediaPlayer.release();
                    }
                    isPlaying = false;
                } else {
                    //开始/恢复播放
                    showLoading();
                    rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
                    play(rtmpAddress);
                }

            }
        });

        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),FullScreenVideoActivity.class);
                intent.putExtra("isPlaying", isPlaying);

                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.setDisplay(null);
                    mediaPlayer.release();

                }
                startActivityForResult(intent, 1);
            }

        });


        rockerView.setOnDirectionChangeListener(new RockerView.OnDirectionChangeListener() {
            @Override
            public void onStart() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                } else {
                    startTwistPublisher();
                }
            }

            @Override
            public void onDirectionChange(RockerView.Direction direction) {
                if (!Config.isRosServerConnected) {
                    return;
                }
                speed = (float) Config.speed;
                switch (direction) {
                    case DIRECTION_UP:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, 0F);
                        break;
                    case DIRECTION_DOWN:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, 0F);
                        break;
                    case DIRECTION_LEFT:
                        rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    case DIRECTION_UP_LEFT:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    case DIRECTION_RIGHT:
                        rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_UP_RIGHT:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_DOWN_LEFT:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_DOWN_RIGHT:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFinish() {
                if (!Config.isRosServerConnected) {
                    return;
                }
                rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, 0F);
                presenter.publishCommand(rockerTwist);
                cancelTimerTask();
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            isPlaying = data.getBooleanExtra("isPlaying", false);
            title.setText(videoList[0]);
        }
        if (isPlaying) {
            showLoading();
            rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
            play(rtmpAddress);
        } else {
            btPlayState.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void play(String rtmpAddress) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setDisplay(null);
            mediaPlayer.release();
            mediaPlayer = null;

        }
        createMediaPlayer();
        try {
            mediaPlayer.setDataSource(rtmpAddress);
        } catch (IOException e) {
            showLoadingFailed();
            e.printStackTrace();
        }
        mediaPlayer.setDisplay(surfaceView.getHolder());

        mediaPlayer.prepareAsync();
    }

    private void createMediaPlayer() {
        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 16);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 50000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"packet-buffering",0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);

        mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                iMediaPlayer.setDisplay(surfaceView.getHolder());
            }
        });

        mediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (i == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    hideLoading();
                    isPlaying = true;
                    btPlayState.setBackgroundResource(R.drawable.ic_pause);
                    title.setText(videoTitle);
//                    log("ijkMediaPlayer onInfo:" + i + " , " + i1);
                }
                return true;
            }
        });
        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
//                log("ijkMediaPlayer onError:" + i + " , " + i1);
                showLoadingFailed();

                return true;
            }
        });

    }

    //当Ros服务器连接状态变化时，从外部通知该Fragment
    public void notifyRosConnectionStateChange(boolean isConnected) {
        if (isConnected) {
            rockerView.setAvailable(true);
        } else {
            rockerView.setAvailable(false);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (Config.isRosServerConnected) {
                rockerView.setAvailable(true);
            } else {
                rockerView.setAvailable(false);
            }

        }
    }

    public synchronized void startTwistPublisher() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                presenter.publishCommand(rockerTwist);
            }
        },0,200);
    }

    public synchronized void cancelTimerTask() {
        timer.cancel();
        log("Stopped Control..TimerTask is canceled ");
    }

    @Override
    public void setPresenter(ControlContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.destroy();
        }
    }

    private void log(String string) {
        Log.i(TAG, TAG + " -- " + string);
    }

    @Override
    public void showLoading() {
        infoView.setVisibility(View.VISIBLE);
        infoImage.setBackgroundResource(R.drawable.loading);
        infoImage.startAnimation(waitAnimation);
        infoText.setText(R.string.text_loading);
        handler.sendEmptyMessageDelayed(MSG_FLAG_LOADING, 8000);
    }

    @Override
    public void showLoadingFailed(){
        isPlaying = false;
        isLoadingFailed = true;
        infoView.setVisibility(View.VISIBLE);
        infoImage.clearAnimation();
        infoImage.setBackgroundResource(R.drawable.retry);
        infoText.setText(R.string.text_retry);
        handler.removeMessages(MSG_FLAG_LOADING);
        btPlayState.setBackgroundResource(R.drawable.ic_play);
        Toast.makeText(getContext(), R.string.load_fail_check_config, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        infoImage.clearAnimation();
        infoView.setVisibility(View.GONE);
    }

}
