package cn.iscas.xlab.uxbot.mvp.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.mvp.user.register.RegisterFragment;
import cn.iscas.xlab.uxbot.mvp.user.userlist.UserListActivity;

/**
 * Created by lisongting on 2017/12/11.
 */

public class UserFragment extends Fragment {

    private CardView btUserList,btRegister,btRecognize;
    private RegisterFragment registerFragment;

    public UserFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);
//        View v = inflater.inflate(R.layout.fragment_user, null);

        btUserList = v.findViewById(R.id.bt_user_list);
        btRecognize = v.findViewById(R.id.bt_recognize);
        btRegister = v.findViewById(R.id.bt_register);

        initListeners();
        return v;
    }


    private void initListeners() {
        btUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserListActivity.class));
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFragment = new RegisterFragment();
                getFragmentManager().beginTransaction()
                        .add(registerFragment, "registerFragment")
                        .commit();
            }
        });
    }
}
