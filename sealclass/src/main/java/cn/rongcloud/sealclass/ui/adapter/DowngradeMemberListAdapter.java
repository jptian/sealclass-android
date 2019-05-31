package cn.rongcloud.sealclass.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;

public class DowngradeMemberListAdapter extends BaseAdapter {

    private List<ClassMember> menberList;
    private List<ClassMember> checkedMember;

    public DowngradeMemberListAdapter() {
        checkedMember = new ArrayList<>();
    }

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
            convertView = View.inflate(parent.getContext(), R.layout.class_dialog_item_downgrade_member, null);
        }
        final ClassMember member = menberList.get(position);
        ((CheckBox) convertView).setOnCheckedChangeListener(null);
        ((CheckBox) convertView).setChecked(checkedMember.contains(member));
        ((CheckBox) convertView).setText(member.getUserName());
        ((CheckBox) convertView).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!checkedMember.contains(member)) {
                        checkedMember.add(member);
                    }
                } else {
                    if (checkedMember.contains(member)) {
                        checkedMember.remove(member);
                    }
                }
            }
        });

        return convertView;
    }

    public void setListData(List<ClassMember> menberList) {
        this.menberList = menberList;
    }

    public List<ClassMember> getCheckedDatas() {
        return checkedMember;
    }
}
