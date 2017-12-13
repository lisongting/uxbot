package cn.iscas.xlab.uxbot.mvp.user.userlist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.entity.UserInfo;
import cn.iscas.xlab.uxbot.util.Util;


/**
 * Created by lisongting on 2017/7/11.
 * 采用MVP架构
 */

public class UserListActivity extends AppCompatActivity implements UserListContract.View{

    public static final String TAG = "UserListActivity";
    private UserListContract.Presenter presenter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private UserListAdapter userListAdapter;
    private TextView title;
    private ImageButton btBack,btDelete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_list);

//        getSupportActionBar().setTitle("用户列表");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        presenter = new UserListPresenter(this,this);
        initView();
        initListeners();
    }


    @Override
    public void initView() {
        title = findViewById(R.id.page_title);
        btBack = findViewById(R.id.ib_back);
        btDelete = findViewById(R.id.ib_delete);


        //获取状态栏高度，显示一个占位的View(该view和actionbar颜色相同)，达到沉浸式状态栏效果
        View status_bar = findViewById(R.id.status_bar_view);
        ViewGroup.LayoutParams params = status_bar.getLayoutParams();
        params.height = Util.getStatusBarHeight(this);
        status_bar.setLayoutParams(params);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_user_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        userListAdapter = new UserListAdapter(this);
        recyclerView.setAdapter(userListAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorLightPink);


    }

    private void initListeners() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userListAdapter.setDeleteMode(!userListAdapter.getDeleteMode());
            }
        });

        userListAdapter.setOnDeleteListener(new UserListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position, final String name) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);
                builder.setTitle("是否删除用户:"+name)
                        .setMessage("人脸识别服务器将清除该用户的姓名和面部数据。")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userListAdapter.setDeleteMode(false);
                            }
                        })
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userListAdapter.setDeleteMode(false);
                                //presenter.deleteUser(Util.makeUserNameToHex(name));
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        });

        //从列表顶部向下拉动的时候触发
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.requestUserData();
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        swipeRefreshLayout.setRefreshing(true);
        presenter.requestUserData();

    }

    @Override
    public void onBackPressed() {
        if (userListAdapter.getDeleteMode()) {
            userListAdapter.setDeleteMode(false);
        } else {
            finish();
        }
    }

    @Override
    public void setPresenter(UserListContract.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void showUserInList(UserInfo info) {
        if (info != null) {
            userListAdapter.addUser(info);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showRefreshError() {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "人脸识别服务器连接超时，下拉重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showInfo(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        presenter.requestUserData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(String s) {
        Log.i(TAG,  s);
    }
}
