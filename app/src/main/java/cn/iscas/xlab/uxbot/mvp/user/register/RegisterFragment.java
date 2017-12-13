package cn.iscas.xlab.uxbot.mvp.user.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import cn.iscas.xlab.uxbot.R;

/**
 * Created by lisongting on 2017/12/13.
 */

public class RegisterFragment extends DialogFragment {

    private Window window;
    public RegisterFragment(){}
    private Button btCapture,btCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        btCapture = v.findViewById(R.id.id_bt_camera);
        btCancel = v.findViewById(R.id.id_bt_cancel);

        window = getDialog().getWindow();
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        window.setLayout((int) (metrics.widthPixels*0.7), (int) (metrics.heightPixels*0.5));

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                startActivity(new Intent(getContext(), CameraActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}
