package cn.rongcloud.sealclass.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.rtc.VideoResolution;

public class SimpleCheckTextAdapter extends BaseAdapter {
    private List<VideoResolution> dataList;

    public SimpleCheckTextAdapter(List<VideoResolution> list) {
        dataList = list;
    }

    public SimpleCheckTextAdapter() {
    }

    @Override
    public int getCount() {
        return dataList != null ? dataList.size() : 0;
    }

    @Override
    public VideoResolution getItem(int position) {
        return dataList != null ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.common_item_checktextview, parent,false);
        }

        CheckedTextView checkedTextView = (CheckedTextView) convertView;
        VideoResolution item = getItem(position);
        checkedTextView.setText(item.getResolution());

        return convertView;
    }

    public void setDataList(List<VideoResolution> list) {
        this.dataList = list;
    }
}
