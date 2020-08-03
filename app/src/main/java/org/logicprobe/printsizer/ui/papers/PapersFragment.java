package org.logicprobe.printsizer.ui.papers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.logicprobe.printsizer.R;

public class PapersFragment extends Fragment {

    private PapersViewModel papersViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        papersViewModel = new ViewModelProvider(this).get(PapersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_papers, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        papersViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}