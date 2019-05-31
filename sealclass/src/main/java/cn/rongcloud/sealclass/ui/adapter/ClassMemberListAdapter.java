package cn.rongcloud.sealclass.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.ui.view.ClassItemMemberItem;

/**
 * 成员列表的 Adapter
 */
public class ClassMemberListAdapter extends BaseAdapter {
    private List<ClassMember> menberList = new ArrayList<>();
    private UserInfo currentUser;
    private HashMap<String, Boolean> expandStatus = new HashMap<>();
    private OnItemMemberClickListenr listener;
    private ClassMember preExpandClassMember;

    private ClassItemMemberItem preExpandStatusView;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ClassItemMemberItem(parent.getContext());
        }
        ClassMember classMember = menberList.get(position);
        boolean expand = expandStatus.get(classMember.getUserId()) == null ? false : expandStatus.get(classMember.getUserId());
        final ClassItemMemberItem itemView = ((ClassItemMemberItem) convertView);
        itemView.setOnItemMemberClickListener(new ClassItemMemberItem.OnItemMemberClickListener() {
            @Override
            public void onClick(ClassItemMemberItem.OperateType type, ClassMember member) {
                if (listener != null) {
                    listener.onClick(itemView, type, member);
                }
            }

            @Override
            public void onExpandViewStatus(ClassMember member, boolean expand) {
                if (expand) {
                    if (preExpandClassMember != null) {
                        expandStatus.remove(preExpandClassMember.getUserId());
                    }
                    preExpandClassMember = member;
                    expandStatus.put(member.getUserId(), expand);
                } else {
                    expandStatus.remove(member.getUserId());
                }

                notifyDataSetChanged();

                if (listener != null) {
                    listener.onExpandViewStatus(member, position, expand);
                }
            }
        });
        itemView.setData(currentUser, classMember, expand);
        return convertView;
    }

    public void setListData(List<ClassMember> menberList) {
        if (menberList != null) {
            this.menberList.clear();
            this.menberList.addAll(menberList);
        }
    }

    public void setCurrentUser(UserInfo currentUser) {
        this.currentUser = currentUser;
    }

    public void setOnItemMemberClickListenr(OnItemMemberClickListenr listenr) {
        this.listener = listenr;
    }

    public interface OnItemMemberClickListenr {
        public void onClick(ClassItemMemberItem itemView, ClassItemMemberItem.OperateType type, ClassMember member);
        public void onExpandViewStatus(ClassMember member, int position, boolean expand);

    }
}
