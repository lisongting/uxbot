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
package cn.iscas.xlab.uxbot;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.iscas.xlab.uxbot.mvp.control.ControlFragment;
import cn.iscas.xlab.uxbot.mvp.control.ControlPresenter;
import cn.iscas.xlab.uxbot.mvp.robot_state.RobotStateFragment;
import cn.iscas.xlab.uxbot.mvp.robot_state.RobotStatePresenter;
import cn.iscas.xlab.uxbot.util.Util;


/**
 * Created by lisongting on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY_NAV_ITEM = "current_navigation_item";

    private BottomNavigationView bottomNavigationView;

    private RobotStateFragment robotStateFragment;
    private ControlFragment controlFragment;
    private ControlPresenter controlPresenter;
    private RobotStatePresenter robotStatePresenter;

    private long lastExitTime;
    private FragmentManager fragmentManager;
    private int selectedNavItem = 0;
    private TextView pageTitle;
    private ImageButton settingButton;
    private RosConnectionReceiver receiver;

    @TargetApi(23)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate()");
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        pageTitle = (TextView) findViewById(R.id.page_title);
        settingButton = (ImageButton) findViewById(R.id.setting_button);

        //获取状态栏高度，显示一个占位的View(该view和actionbar颜色相同)，达到沉浸式状态栏效果
        View status_bar = findViewById(R.id.status_bar_view);
        ViewGroup.LayoutParams params = status_bar.getLayoutParams();
        params.height = Util.getStatusBarHeight(this);
        status_bar.setLayoutParams(params);

        initListeners();
        initConfiguration();

        if (savedInstanceState == null) {
            log("savedInstanceState is null");
            fragmentManager = getSupportFragmentManager();
            robotStateFragment = new RobotStateFragment();
            controlFragment = new ControlFragment();

            fragmentManager.beginTransaction()
                .add(R.id.container, robotStateFragment, robotStateFragment.getClass().getSimpleName())
                .add(R.id.container, controlFragment, controlFragment.getClass().getSimpleName())
                .commit();
            bottomNavigationView.setSelectedItemId(R.id.robot_state);
        } else {
            log("restore savedInstanceState ");
            fragmentManager = getSupportFragmentManager();
            robotStateFragment = (RobotStateFragment) fragmentManager.findFragmentByTag(RobotStateFragment.class.getSimpleName());
            controlFragment = (ControlFragment) fragmentManager.findFragmentByTag(ControlFragment.class.getSimpleName());
            selectedNavItem = savedInstanceState.getInt(KEY_NAV_ITEM);
            switch (selectedNavItem) {
                case 0:
                    bottomNavigationView.setSelectedItemId(R.id.robot_state);
                    break;
                case 1:
                    bottomNavigationView.setSelectedItemId(R.id.control);
                    break;
                default:
                    break;
            }
        }
        //将Presenter与View关联起来
        controlPresenter = new ControlPresenter(this, controlFragment);
        robotStatePresenter = new RobotStatePresenter(this, robotStateFragment);

        //更改底部tab按钮选中的颜色
        ColorStateList list = new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {android.R.attr.state_enabled}},
                new int[]{getResources().getColor(R.color.colorPrimary, null), Color.GRAY});
        bottomNavigationView.setItemIconTintList(list);
        bottomNavigationView.setItemTextColor(list);


        initBroadcastReceiver();
    }

    private void initConfiguration() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Config.ROS_SERVER_IP = sp.getString(getResources().getString(R.string.pref_key_ros_server_ip), "192.168.8.101");
        Config.speed = sp.getInt(getResources().getString(R.string.pref_key_speed), 30) / 100.0;
        log("初始设置：" + Config.ROS_SERVER_IP + " ," + Config.speed);
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");
    }

    @Override
    protected void onResume() {
        log("onResume()");
        super.onResume();
    }


    private void initListeners() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                log("onNavigationItemSelected():" + item.getItemId());
                switch (item.getItemId()) {
                    case R.id.robot_state:
                        pageTitle.setText("Xbot状态");
                        fragmentManager.beginTransaction()
                                .hide(controlFragment)
                                .show(robotStateFragment)
                                .commit();
                        selectedNavItem = 0;
                        break;
                    case R.id.control:
                        pageTitle.setText("控制界面");
                        fragmentManager.beginTransaction()
                                .hide(robotStateFragment)
                                .show(controlFragment)
                                .commit();
                        selectedNavItem = 1;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                App app = (App) getApplication();
                robotStatePresenter.setServiceProxy(app.getRosServiceProxy());
                robotStatePresenter.subscribeRobotState();
                controlPresenter.setServiceProxy(app.getRosServiceProxy());
                robotStateFragment.notifyRosConnectionStateChange(true);
                controlFragment.notifyRosConnectionStateChange(true);
            }

            @Override
            public void onFailure() {
                robotStateFragment.notifyRosConnectionStateChange(false);
                controlFragment.notifyRosConnectionStateChange(false);

            }
        });

        IntentFilter filter = new IntentFilter(Constant.ROS_RECEIVER_INTENTFILTER);
        registerReceiver(receiver,filter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        if (robotStateFragment.isAdded()) {
            fragmentManager.putFragment(outState, robotStateFragment.getClass().getSimpleName(), robotStateFragment);
        }
        if (controlFragment.isAdded()) {
            fragmentManager.putFragment(outState, controlFragment.getClass().getSimpleName(), controlFragment);
        }
//        if (userFragment.isAdded()) {
//            fragmentManager.putFragment(outState, userFragment.getClass().getSimpleName(), userFragment);
//        }
        outState.putInt(KEY_NAV_ITEM, selectedNavItem);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - lastExitTime < 2000) {
                finish();
            } else {
                Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                lastExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        log("onDestroy()");
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void log(String s) {
        Log.i(TAG, TAG + " -- " + s);
    }

}
