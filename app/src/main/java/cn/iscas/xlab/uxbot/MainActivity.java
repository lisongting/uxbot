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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by lisongting on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY_NAV_ITEM = "current_navigation_item";

    private BottomNavigationView bottomNavigationView;

    private MapFragment mapFragment;
    private CameraFragment cameraFragment;
    private RobotStateFragment robotStateFragment;
    private long lastExitTime;
    private FragmentManager fragmentManager;
    private int selectedNavItem = 0;
    private TextView pageTitle;
    private ImageButton settingButton;

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
        params.height = getStatusBarHeight();
        status_bar.setLayoutParams(params);

        initListeners();
        initConfiguration();

        if (savedInstanceState == null) {
            log("savedInstanceState is null");
            fragmentManager = getSupportFragmentManager();
            mapFragment = new MapFragment();
            cameraFragment = new CameraFragment();
            robotStateFragment = new RobotStateFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, robotStateFragment, robotStateFragment.getClass().getSimpleName())
                    .add(R.id.container, mapFragment, mapFragment.getClass().getSimpleName())
                    .add(R.id.container, cameraFragment, cameraFragment.getClass().getSimpleName())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.robot_state);
        } else {
            log("restore savedInstanceState ");
            fragmentManager = getSupportFragmentManager();
            robotStateFragment = (RobotStateFragment) fragmentManager.getFragment(savedInstanceState, robotStateFragment.getClass().getSimpleName());
            mapFragment = (MapFragment) fragmentManager.getFragment(savedInstanceState, mapFragment.getClass().getSimpleName());
            cameraFragment = (CameraFragment) fragmentManager.getFragment(savedInstanceState, cameraFragment.getClass().getSimpleName());
            selectedNavItem = savedInstanceState.getInt(KEY_NAV_ITEM);
            switch (selectedNavItem) {
                case 0:
                    bottomNavigationView.setSelectedItemId(R.id.robot_state);
                    break;
                case 1:
                    bottomNavigationView.setSelectedItemId(R.id.camera);
                    break;
                case 2:
                    bottomNavigationView.setSelectedItemId(R.id.map);
                    break;
                default:
                    break;
            }
        }
        ColorStateList list = new ColorStateList(new int[][]{
                {android.R.attr.state_checked},
                {android.R.attr.state_enabled},
        }, new int[]{
                getResources().getColor(R.color.colorPrimary, null),
                Color.GRAY
        });
        bottomNavigationView.setItemIconTintList(list);
        bottomNavigationView.setItemTextColor(list);
    }

    private void initConfiguration() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Config.ROS_SERVER_IP = sp.getString(getResources().getString(R.string.pref_key_ros_server_ip), "192.168.0.135");
        Config.speed = sp.getInt(getResources().getString(R.string.pref_key_speed),30) / 100.0;
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
                log("onNavigationItemSelected():"+item.getItemId());
                switch (item.getItemId()) {
                    case R.id.robot_state:
                        pageTitle.setText("Xbot状态");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(cameraFragment)
                                .hide(mapFragment)
                                .show(robotStateFragment)
                                .commit();
                        selectedNavItem = 0;
//                        bottomNavigationView.setForeground();
//                        bottomNavigationView.setItemIconTintList();
                        break;
                    case R.id.camera:
                        pageTitle.setText("摄像头");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(mapFragment)
                                .hide(robotStateFragment)
                                .show(cameraFragment)
                                .commit();
                        selectedNavItem = 1;
                        break;
                    case R.id.map:
                        pageTitle.setText("2D地图");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(cameraFragment)
                                .hide(robotStateFragment)
                                .show(mapFragment)
                                .commit();
                        selectedNavItem = 2;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        if (robotStateFragment.isAdded()) {
            fragmentManager.putFragment(outState, robotStateFragment.getClass().getSimpleName(), robotStateFragment);
        }
        if (mapFragment.isAdded()) {
            fragmentManager.putFragment(outState, mapFragment.getClass().getSimpleName(), mapFragment);
        }
        if (cameraFragment.isAdded()) {
            fragmentManager.putFragment(outState, cameraFragment.getClass().getSimpleName(), cameraFragment);
        }
        outState.putInt(KEY_NAV_ITEM, selectedNavItem);

    }

    private int getStatusBarHeight(){
        int height = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        log("onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_setting,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - lastExitTime < 2000) {
                finish();
            }else{
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
    }

    private void log(String s) {
        Log.i(TAG, TAG + " -- " + s);
    }

}
