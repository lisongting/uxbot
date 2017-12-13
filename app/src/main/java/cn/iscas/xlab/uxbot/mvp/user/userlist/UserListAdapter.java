package cn.iscas.xlab.uxbot.mvp.user.userlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.entity.UserInfo;

/**
 * Created by lisongting on 2017/12/12.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserItemViewHolder> {
    private static final String TAG = "UserListAdapter";
    private Context context;
    private LayoutInflater layoutInflater;
    private List<UserInfo> userInfoList;
    private boolean isDeleteButtonOn = false;
    private OnDeleteListener deleteListener;

    interface OnDeleteListener{
        void onDelete(int position,String name);
    }
    
    public UserListAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        userInfoList = new ArrayList<>();
    }

    public UserListAdapter(Context context,List<UserInfo> list) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.userInfoList = list;
    }

    @Override
    public UserItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layoutInflater.inflate(R.layout.item_user, parent, false);
        UserItemViewHolder viewHolder = new UserItemViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserItemViewHolder holder, int position) {
        holder.userName.setText(userInfoList.get(position).getName());
        holder.userImg.setImageBitmap(userInfoList.get(position).getFace());
        if (isDeleteButtonOn) {
            holder.deleteImg.setVisibility(View.VISIBLE);
        } else {
            holder.deleteImg.setVisibility(View.GONE);
        } 
    }

    @Override
    public int getItemCount() {
        if (userInfoList == null) {
            return 0;
        }
        return userInfoList.size();
    }

    public void addUser(UserInfo user) {
        for (UserInfo info : userInfoList) {
            if (user.getName().equals(info.getName())) {
                return;
            }
        }
        userInfoList.add(user);
        notifyDataSetChanged();
    }

    public boolean getDeleteMode() {
        return isDeleteButtonOn;
    }

    public void setDeleteMode(boolean isDeleteButtonOn) {
        if (this.isDeleteButtonOn != isDeleteButtonOn) {
            this.isDeleteButtonOn = isDeleteButtonOn;
        }
        notifyDataSetChanged();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    class UserItemViewHolder extends RecyclerView.ViewHolder{

        private ImageView userImg;
        private TextView userName;
        private ImageView deleteImg;
        
        public UserItemViewHolder(View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.tv_user_head);
            userName = itemView.findViewById(R.id.tv_user_name);
            deleteImg = itemView.findViewById(R.id.iv_delete_user);

            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deleteListener != null) {
                        int pos  = getLayoutPosition();
                        deleteListener.onDelete(pos,userInfoList.get(pos).getName());
                    }
                }
            });
        }
    }
}
