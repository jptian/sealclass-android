package cn.rongcloud.sealclass.ui.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.core.EglRenderer;
import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.UserDisplayResource;
import cn.rongcloud.sealclass.model.WhiteBoard;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * 课堂资源列表适配
 */
public class ClassResourceListAdapter extends BaseAdapter {

    private List<UserDisplayResource> userList;
    private List<WhiteBoard> whiteBoardList;
    private OnLoadVideoFrameListener listener;

    @Override
    public int getCount() {
        return (userList != null ? userList.size() : 0) + (whiteBoardList != null ? whiteBoardList.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        int videoListSize = userList != null ? userList.size() : 0;
        int whiteBoardListSize = whiteBoardList != null ? whiteBoardList.size() : 0;

        // 优先显示视频资源，再显示白板内容
        if (position < videoListSize) {
            return userList.get(position);
        } else if (position < videoListSize + whiteBoardListSize) {
            return whiteBoardList.get(position - videoListSize);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item_resource_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.resourceNameTv = convertView.findViewById(R.id.class_resource_item_tv_name);
            viewHolder.resourcePreviewIv = convertView.findViewById(R.id.class_resource_item_iv_preview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Object item = getItem(position);

        // 设置白板名称
        if (item instanceof WhiteBoard) {
            WhiteBoard whiteBoard = (WhiteBoard) item;
            viewHolder.resourceNameTv.setText(whiteBoard.getName());
            viewHolder.resourceNameTv.setBackgroundColor(parent.getContext().getResources().getColor(R.color.white));
            viewHolder.resourceNameTv.setTextColor(parent.getContext().getResources().getColor(R.color.text_black));

        } else if (item instanceof UserDisplayResource) {
            UserDisplayResource userDisplayResource = (UserDisplayResource) item;
            viewHolder.resourcePreviewIv.setImageDrawable(null);
            Bitmap screenShotBitmap = userDisplayResource.getScreenShotBitmap();
            if (screenShotBitmap != null && !screenShotBitmap.isRecycled()) {
                viewHolder.resourceNameTv.setVisibility(View.GONE);
                viewHolder.resourcePreviewIv.setImageBitmap(userDisplayResource.getScreenShotBitmap());
                viewHolder.resourcePreviewIv.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                viewHolder.resourceNameTv.setVisibility(View.VISIBLE);
                String name = userDisplayResource.getClassMember().getUserName();
                viewHolder.resourceNameTv.setText(name);
                viewHolder.resourceNameTv.setTextColor(parent.getContext().getResources().getColor(R.color.white));
            }
        }

        return convertView;
    }

    /**
     * 设置白板列表
     *
     * @param list
     */
    public void setWhiteBoardList(List<WhiteBoard> list) {
        whiteBoardList = list;
        notifyDataSetChanged();
    }

    /**
     * 设置用户列表
     *
     * @param list
     */
    public void setUserDisplayResource(List<UserDisplayResource> list) {
        userList = list;
        notifyDataSetChanged();
    }

    public List<UserDisplayResource> getUserDisplayResource() {
        return userList;
    }

    private class ViewHolder {
        TextView resourceNameTv;
        ImageView resourcePreviewIv;
    }

    public void setOnLoadVideoFrameListener(OnLoadVideoFrameListener listener) {
        this.listener = listener;
    }

    public interface OnLoadVideoFrameListener {
        void onLoadFrame(ImageView view, String userId);
    }
}
