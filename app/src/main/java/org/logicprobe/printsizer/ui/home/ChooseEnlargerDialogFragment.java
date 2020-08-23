package org.logicprobe.printsizer.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogChooseEnlargerBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;

import java.util.List;

public class ChooseEnlargerDialogFragment extends DialogFragment {
    public static final int ACTION_SELECTED_ENLARGER = 0;
    public static final int ACTION_ADD_ENLARGER = 1;
    private String requestKey;
    private ChooseEnlargerDialogViewModel viewModel;
    private DialogChooseEnlargerBinding binding;
    private ChooseEnlargerDialogAdapter adapter;

    public ChooseEnlargerDialogFragment() {
    }

    public static ChooseEnlargerDialogFragment create(String requestKey) {
        Bundle args = new Bundle();
        args.putString("requestKey", requestKey);
        ChooseEnlargerDialogFragment dialog = new ChooseEnlargerDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_choose_enlarger, null, false);

        adapter = new ChooseEnlargerDialogAdapter(adapterClickCallback);
        binding.enlargerProfileList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChooseEnlargerDialogViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setDialogViewModel(viewModel);

        viewModel.getSelectionList().observe(requireActivity(),
                new Observer<List<ChooseEnlargerElement>>() {
                    @Override
                    public void onChanged(List<ChooseEnlargerElement> chooseEnlargerElements) {
                        if (chooseEnlargerElements != null) {
                            adapter.setSelectionList(chooseEnlargerElements);
                        }
                        binding.executePendingBindings();
                    }
                });

        View view = binding.getRoot();

        RecyclerView recyclerView = view.findViewById(R.id.enlarger_profile_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        builder.setView(view).setTitle(R.string.dialog_choose_enlarger_profile);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            requestKey = arguments.getString("requestKey");
        }

        return builder.create();
    }

    private ChooseEnlargerClickCallback adapterClickCallback = new ChooseEnlargerClickCallback() {
        @Override
        public void onClickProfile(EnlargerProfile enlargerProfile) {
            Bundle result = new Bundle();
            result.putInt("action", ACTION_SELECTED_ENLARGER);
            result.putInt("profileId", enlargerProfile.getId());
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        }

        @Override
        public void onClickAction(int actionId) {
            Bundle result = new Bundle();
            result.putInt("action", ACTION_ADD_ENLARGER);
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        }
    };
}
