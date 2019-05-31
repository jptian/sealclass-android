package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.ui.ToastBySelfComponent;
import cn.rongcloud.sealclass.utils.ToastUtils;

public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutResId = getLayoutResId();
        if (layoutResId <= 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        View view = inflater.inflate(layoutResId, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onInitView(savedInstanceState, getActivity().getIntent());
        onInitViewModel();
    }

    @Override
    public void onClick(View v) {
        onClick(v, v.getId());
    }


    /**
     * 通过 id 查询当前布局的 View 控件。
     *
     * @param id  控件 id
     * @param <T> 控件类型
     * @return 返回对应的控件
     */
    public <T extends View> T findView(int id) {
        return findView(id, false);
    }

    /**
     * 通过 id 查询当前布局的 View 控件。并是否对此控件设置点击监听
     *
     * @param id      控件 id
     * @param isClick 是否设置点击点击监听
     * @param <T>     控件类型
     * @return 返回对应的控件
     */
    public <T extends View> T findView(int id, boolean isClick) {
        View viewById = getView().findViewById(id);
        if (isClick) {
            viewById.setOnClickListener(this);
        }
        return (T) viewById;
    }

    /**
     * 通过 id 查询当前布局的 View 控件。并是否对此控件设置点击监听
     *
     * @param view    当前的布局
     * @param id      控件 id
     * @param isClick 是否设置点击点击监听
     * @param <T>     控件类型
     * @return 返回对应的控件
     */
    public <T extends View> T findView(View view, int id, boolean isClick) {
        View viewById = view.findViewById(id);
        if (isClick) {
            viewById.setOnClickListener(this);
        }
        return (T) viewById;
    }


    public void showToast(String text) {
        FragmentActivity activity = getActivity();
        if (activity instanceof ToastBySelfComponent) {
            ToastBySelfComponent component = (ToastBySelfComponent) activity;
            component.showToast(text, 0);
        } else {
            ToastUtils.showToast(text);
        }
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }


    /**
     * 设置布局资源id
     *
     * @return 设置布局资源id
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化 view
     *
     * @param savedInstanceState
     * @param intent
     */
    protected abstract void onInitView(Bundle savedInstanceState, Intent intent);

    /**
     * 点击监听，当通过{@link #findView(int, boolean)} 方法， 当设置为true ， 设置点击监听时， 则会通过此
     * 方法进行监听回调。
     *
     * @param v  监听的 view 控件
     * @param id 控件的 ID
     */
    protected void onClick(View v, int id) {

    }


    protected void onInitViewModel() {

    }

}
