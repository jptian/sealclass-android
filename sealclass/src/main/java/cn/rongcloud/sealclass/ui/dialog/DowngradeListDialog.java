package cn.rongcloud.sealclass.ui.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.ui.adapter.DowngradeMemberListAdapter;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import cn.rongcloud.sealclass.model.RequestState;

public class DowngradeListDialog extends CommonDialog {


    private DowngradeMemberListAdapter adapter;
    private ClassViewModel classViewModel;
    private String roomId;

    @Override
    protected View onCreateContentView() {

        View view = View.inflate(getContext(), R.layout.class_dialog_member_list_downgrade, null);
        GridView memberListView = view.findViewById(R.id.class_gv_member_list);
        adapter = new DowngradeMemberListAdapter();
        memberListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ss_checked", "onViewCreated");
        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        classViewModel.getRoomId().observe(this, new Observer<String>() {

            @Override
            public void onChanged(String s) {
                roomId = s;
            }
        });

        classViewModel.getMemberList().observe(this, new Observer<List<ClassMember>>() {
            @Override
            public void onChanged(List<ClassMember> classMembers) {

                if (classMembers == null || classMembers.size() <= 0) {
                    return;
                }
                List<ClassMember> canDowngradeMemeber = new ArrayList<>();

                for (ClassMember member : classMembers) {
                    if (member.getRole() == Role.LECTURER || member.getRole() == Role.STUDENT ) {
                        canDowngradeMemeber.add(member);
                    }
                }

                adapter.setListData(canDowngradeMemeber);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected boolean onPositiveClick() {
        List<ClassMember> members = adapter.getCheckedDatas();
        downgradeMembers(roomId, members);
        return true;
    }

    //批量降级
    private void downgradeMembers(String roomId, List<ClassMember> members) {
        if (classViewModel != null) {
            classViewModel.downgrade(roomId, members).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    } else if (requestState.getState() == RequestState.State.FAILED) {

                    } else {

                    }
                }
            });
        }
    }


    public static class Builder extends CommonDialog.Builder {
        @Override
        protected CommonDialog getCurrentDialog() {
            return new DowngradeListDialog();
        }
    }
}
