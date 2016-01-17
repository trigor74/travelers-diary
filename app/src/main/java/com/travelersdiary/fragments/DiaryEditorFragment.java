package com.travelersdiary.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.travelersdiary.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryEditorFragment extends Fragment {

    @Bind(R.id.rtEditText)
    RTEditText mRtEditText;

    @Bind(R.id.rte_toolbar_container)
    ViewGroup toolbarContainer;

    @Bind(R.id.rte_toolbar)
    RTToolbar rtToolbar;

    private RTManager mRtManager;

    String message;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // read extras
        if (savedInstanceState == null) {
            Intent intent = getActivity().getIntent();
            message = getStringExtra(intent, "message");
        }

        // set theme
        getActivity().setTheme(R.style.RteTheme);

        // inflate layout
        View view = inflater.inflate(R.layout.fragment_diary_editor, container, false);
        ButterKnife.bind(this, view);

        // initialize rich text manager
        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl(getActivity()), new RTMediaFactoryImpl(getContext(), true));
        mRtManager = new RTManager(rtApi, savedInstanceState);
//
//        // register toolbar (if it exists)
        if (rtToolbar != null) {
            mRtManager.registerToolbar(toolbarContainer, rtToolbar);
        }

        // register message editor
        mRtManager.registerEditor(mRtEditText, true);
        if (message != null) {
            mRtEditText.setRichTextEditing(true, message);
        }

        mRtEditText.requestFocus();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                .showSoftInput(mRtEditText, InputMethodManager.SHOW_FORCED);
    }

    private String getStringExtra(Intent intent, String key) {
        String s = intent.getStringExtra(key);
        return s == null ? "" : s;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRtManager != null) {
            mRtManager.onDestroy(getActivity().isFinishing());
        }
    }
}
