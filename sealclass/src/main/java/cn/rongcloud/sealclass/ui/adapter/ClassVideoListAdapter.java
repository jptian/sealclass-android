package cn.rongcloud.sealclass.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.ui.view.ClassVideoListItem;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * 视频列表的 Adapter
 */
public class ClassVideoListAdapter extends BaseAdapter {

    private List<ClassMember> menberList;
    private OnUserUpdateListener listener;
    private List<String> updateUserIds = Collections.synchronizedList(new ArrayList<String>());

    @Override
    public int getCount() {
        return menberList == null ? 0 : menberList.size();
    }

    @Override
    public Object getItem(int position) {
        return menberList == null ? null : menberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ClassVideoListItem item = new ClassVideoListItem(parent.getContext());
            item.setBackgroundResource(R.color.colorVideoViewBg);
            int width = DisplayUtils.dip2px(parent.getContext(), Math.round(84 * 1.333));
            int height = DisplayUtils.dip2px(parent.getContext(), 84);
            AbsListView.LayoutParams layoutParams = (AbsListView.LayoutParams) item.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new AbsListView.LayoutParams(width, height);
            } else {
                layoutParams.height = height;
                layoutParams.width = width;
            }
            item.setLayoutParams(layoutParams);
            convertView = item;
        }

        ClassMember classMember = menberList.get(position);
        ClassMember oldData = ((ClassVideoListItem) convertView).getData();
        ClassVideoListItem item = ((ClassVideoListItem) convertView);
//        SLog.d("ss_video_adapter", "ClassVideoListItem , position = " + position + ", count = " + item.getVideoViewCount());
        item.setData(classMember);
        if (oldData != null) {
            //如果不为空，则判读是当前的人是否还是同一个人， 如果不是， 则通知刷新
            boolean update = isUpdate(classMember.getUserId());
//            SLog.d("ss_video_adapter", "ClassVideoListItem , position = " + position + ", update = " + update + ", equal = " + (!oldData.getUserId().equals(classMember.getUserId())));

            if (classMember == null || update || !oldData.getUserId().equals(classMember.getUserId())) {
//                SLog.d("ss_video_adapter", "ClassVideoListItem , position = " + position + ", 1");
                // 通知解绑和绑定
                if (listener != null) {
                    listener.onUpdate(item, position, oldData, classMember);
                }
            }
        } else {
//            SLog.d("ss_video_adapter", "ClassVideoListItem , position = " + position + ", 2");
            // 通知订阅
            if (listener != null) {
                listener.onUpdate(item, position, oldData, classMember);
            }
        }
        return convertView;
    }

    public void setListData(List<ClassMember> menberList) {
        this.menberList = menberList;
    }

    public void setOnUserUpdateListener(OnUserUpdateListener listener) {
        this.listener = listener;
    }

//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//    }

    public void notifyDataSetChanged(List<String> userIds) {
        if (userIds == null || userIds.size() <= 0) {
            return;
        }
        if (updateUserIds == null) {
            updateUserIds = userIds;
        } else {
            for (String userId : userIds) {
                boolean contains = updateUserIds.contains(userId);
                if (!contains) {
                    updateUserIds.add(userId);
                }
            }
        }
        userIds.clear();
        notifyDataSetChanged();
    }

    private boolean isUpdate(String userId) {
        if (updateUserIds == null || updateUserIds.size() <= 0) {
            return false;
        }
        boolean contains = updateUserIds.contains(userId);
        if (contains) {
            int index = updateUserIds.indexOf(userId);
            updateUserIds.remove(index);
        }
        return contains;
    }

    public interface OnUserUpdateListener {
        void onUpdate(ClassVideoListItem view, int position, ClassMember oldMember, ClassMember newMember);
    }

}
