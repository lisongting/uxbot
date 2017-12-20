package cn.iscas.xlab.uxbot.mvp.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.uxbot.App;
import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.Constant;
import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.customview.RockerView;
import cn.iscas.xlab.uxbot.entity.Twist;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * Created by lisongting on 2017/12/6.
 */

public class FullScreenVideoActivity extends AppCompatActivity implements ControlContract.View{

    private static final String TAG = "VideoActivity";
    private SurfaceView surfaceView;
    private RelativeLayout topBar,bottomBar,infoView;
    private ImageButton btPlayState,btFullScreen,btVideoList,btBack,btRockerSwitch;
    private RockerView rockerView;
    private ListView listView;
    private ImageView infoImage;
    private TextView infoText;
    private TextView title;

    private ControlContract.Presenter presenter;
    private SimpleAdapter simpleAdapter;
    private String[] videoList = {"彩色图像","深度图像"};
    private List<Map<String,String>> listData;
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
    private SurfaceHolder holder;
    private int videoIndex = 0;

    //用于隐藏菜单
    private static final int MSG_FLAG_HIDEN_MENU = 1;
    //用于显示超时
    private static final int MSG_FLAG_LOADING = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate()");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_full_screen);

        surfaceView = (SurfaceView) findViewById(R.id.sv_video);
        topBar = (RelativeLayout) findViewById(R.id.top_bar);
        bottomBar = (RelativeLayout)findViewById(R.id.bottom_bar);
        infoView = (RelativeLayout) findViewById(R.id.info_view);
        btPlayState = (ImageButton) findViewById(R.id.ib_play_state);
        btFullScreen = (ImageButton) findViewById(R.id.ib_screen_state);
        btVideoList = (ImageButton) findViewById(R.id.ib_list);
        listView = (ListView) findViewById(R.id.list_view);
        rockerView = (RockerView) findViewById(R.id.rocker_view);
        infoImage = (ImageView) findViewById(R.id.info_image);
        infoText = (TextView) findViewById(R.id.info_text);
        title = (TextView) findViewById(R.id.tv_title);
        btBack = (ImageButton) findViewById(R.id.ib_back);
        btRockerSwitch = (ImageButton) findViewById(R.id.rocker_switch);

        int type = getIntent().getIntExtra("video_type", -1);
        isPlaying = getIntent().getBooleanExtra("isPlaying", false);
        if (type == Constant.VIDEO_TYPE_RGB) {
            rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
            videoTitle = videoList[0];
        } else if (type ==Constant.VIDEO_TYPE_DEPTH) {
            rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_DEPTH_RTMP_SUFFIX;
            videoTitle = videoList[1];
        }
        title.setText(videoTitle);

        rockerTwist = new Twist();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_FLAG_HIDEN_MENU && topBar.getVisibility() == View.VISIBLE) {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    isMenuOpened = false;
                } else if (msg.what == MSG_FLAG_LOADING && infoView.getVisibility() == View.VISIBLE) {
                    showLoadingFailed();
                }

                return true;
            }
        });
        isLoadingFailed = false;
        isMenuOpened = true;

        initView();
    }

    @Override
    public void initView() {
        listData = new ArrayList<>();
        for(int i=0;i<videoList.length;i++) {
            Map<String, String> map = new HashMap<>();
            map.put("text", videoList[i]);
            listData.add(map);
        }

        simpleAdapter = new SimpleAdapter(this, listData, R.layout.video_list_item,
                new String[]{"text"}, new int[]{R.id.item_text});
        listView.setAdapter(simpleAdapter);

        waitAnimation = AnimationUtils.loadAnimation(this, R.anim.watting_anim);
        waitAnimation.setInterpolator(new LinearInterpolator());

        Display display =  getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        holder = surfaceView.getHolder();
        float ratio = metrics.heightPixels / 480F;
        holder.setFixedSize((int) (640*ratio),metrics.heightPixels);

    }


    @Override
    protected void onResume() {
        super.onResume();
        log("onResume()");
        initOnClickListeners();
        if (isPlaying) {
            showLoading();
            play(rtmpAddress);
        } else {
            btPlayState.setBackgroundResource(R.drawable.ic_play);
        }

        if (Config.isRosServerConnected) {
            App app = (App) getApplication();
            if (presenter == null) {
                presenter = new ControlPresenter(this,this);
                presenter.setServiceProxy(app.getRosServiceProxy());
                presenter.start();
            }
            rockerView.setAvailable(true);
        } else {
            rockerView.setAvailable(false);

        }
            
    }



    private void initOnClickListeners() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultData();
                finish();
            }
        });

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
                    listView.setVisibility(View.GONE);
                    handler.removeMessages(MSG_FLAG_HIDEN_MENU);
                }
                isMenuOpened = !isMenuOpened;
            }
        });

        btRockerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rockerView.getVisibility() == View.GONE) {
                    rockerView.setVisibility(View.VISIBLE);
                    if (Config.isRosServerConnected) {
                        rockerView.setAvailable(true);
                    } else {
                        rockerView.setAvailable(false);
                    }
                    btRockerSwitch.setBackgroundResource(R.drawable.ic_rocker_off);
                } else if (rockerView.getVisibility() == View.VISIBLE) {
                    rockerView.setVisibility(View.GONE);
                    btRockerSwitch.setBackgroundResource(R.drawable.ic_rocker_on);
                }
            }
        });

        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoadingFailed) {
                    if (videoIndex == 0) {
                        rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
                    } else {
                        rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_DEPTH_RTMP_SUFFIX;
                    }
                    play(rtmpAddress);
                    showLoading();
                    isLoadingFailed = false;
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StringBuilder addr = new StringBuilder("rtmp://");

                if (position == 0) {
                    addr.append(Config.ROS_SERVER_IP).append(Constant.CAMERA_RGB_RTMP_SUFFIX);
                } else {
                    addr.append(Config.ROS_SERVER_IP).append(Constant.CAMERA_DEPTH_RTMP_SUFFIX);
                }
                videoTitle = videoList[videoIndex];
                //如果选择的播放视频源与正在播放的不相同则开始播放，如果相同，则不播放
                if (!rtmpAddress.equals(addr.toString())) {
                    rtmpAddress = addr.toString();
                    showLoading();
                    play(rtmpAddress);
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
                    if (videoIndex == 0) {
                        rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
                    } else {
                        rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_DEPTH_RTMP_SUFFIX;
                    }
                    play(rtmpAddress);
                }

            }
        });

        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultData();
                finish();
            }

        });

        btVideoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.getVisibility() == View.GONE) {
                    listView.setVisibility(View.VISIBLE);
                } else if(listView.getVisibility()== View.VISIBLE){
                    listView.setVisibility(View.GONE);
                }
            }
        });

        rockerView.setOnDirectionChangeListener(new RockerView.OnDirectionChangeListener() {
            @Override
            public void onStart() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(FullScreenVideoActivity.this, "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                } else {
                    startTwistPublisher();
                }
            }

            @Override
            public void onDirectionChange(RockerView.Direction direction) {
//                log("当前的摇杆方向：" + direction.name());
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
                iMediaPlayer.setDisplay(holder);
            }
        });

        mediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (i == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    hideLoading();
                    isPlaying = true;
                    btPlayState.setBackgroundResource(R.drawable.ic_pause);
                    if (rtmpAddress.endsWith(Constant.CAMERA_RGB_RTMP_SUFFIX)) {
                        videoIndex = 0;
                        videoTitle = videoList[0];
                    }else if (rtmpAddress.endsWith(Constant.CAMERA_DEPTH_RTMP_SUFFIX)) {
                        videoIndex = 1;
                        videoTitle = videoList[1];
                    }
                    title.setText(videoTitle);
//                    log("ijkMediaPlayer onInfo:" + i + " , " + i1);
                }
                return true;
            }
        });

        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                log("ijkMediaPlayer onError:" + i + " , " + i1);
                showLoadingFailed();

                return true;
            }
        });

    }

    private void play(String rtmpAddress) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setDisplay(null);
            mediaPlayer.release();
        }
        createMediaPlayer();

        try {
            mediaPlayer.setDataSource(rtmpAddress);
        } catch (IOException e) {
            showLoadingFailed();
            e.printStackTrace();
        }

        mediaPlayer.setDisplay(holder);
        mediaPlayer.prepareAsync();

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

    private void setResultData() {
        Intent intent = new Intent();
        if (rtmpAddress.endsWith(Constant.CAMERA_RGB_RTMP_SUFFIX)) {
            intent.putExtra("video_type",Constant.VIDEO_TYPE_RGB);
        } else if (rtmpAddress.endsWith(Constant.CAMERA_DEPTH_RTMP_SUFFIX)) {
            intent.putExtra("video_type", Constant.VIDEO_TYPE_DEPTH);
        }
        intent.putExtra("isPlaying", isPlaying);
        setResult(0, intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy()");
        if (presenter != null) {
            presenter.destroy();
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setDisplay(null);
            mediaPlayer.release();
            mediaPlayer.reset();
            mediaPlayer = null;
        }
    }

    private void log(String string) {
        Log.i(TAG, TAG + " -- " + string);
    }

    @Override
    public void showLoading() {
        infoView.setVisibility(View.VISIBLE);
        infoImage.setBackgroundResource(R.drawable.loading_large);
        infoImage.startAnimation(waitAnimation);
        infoText.setText(R.string.text_loading);
        handler.sendEmptyMessageDelayed(MSG_FLAG_LOADING, 5000);
    }

    @Override
    public void showLoadingFailed(){
        isPlaying = false;
        isLoadingFailed = true;
        infoView.setVisibility(View.VISIBLE);
        infoImage.clearAnimation();
        infoImage.setBackgroundResource(R.drawable.retry_large);
        infoText.setText(R.string.text_retry);
        handler.removeMessages(MSG_FLAG_LOADING);
        btPlayState.setBackgroundResource(R.drawable.ic_play);
        Toast.makeText(this, R.string.load_fail_check_config, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        infoImage.clearAnimation();
        infoView.setVisibility(View.GONE);
    }
}
