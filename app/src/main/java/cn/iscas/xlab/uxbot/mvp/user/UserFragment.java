package cn.iscas.xlab.uxbot.mvp.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.iscas.xlab.uxbot.R;

/**
 * Created by lisongting on 2017/12/11.
 */

public class UserFragment extends Fragment {

    public UserFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.container_layout, container, false);
        return v;
    }
}
