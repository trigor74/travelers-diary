package com.travelersdiary.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.travelersdiary.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.test_button)
    public void opedEditor() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DiaryEditorFragment())
                .addToBackStack(null)
                .commit();
    }

}
