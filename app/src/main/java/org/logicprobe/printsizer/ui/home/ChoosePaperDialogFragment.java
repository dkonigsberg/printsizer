package org.logicprobe.printsizer.ui.home;

import android.app.AlertDialog;
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

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogChoosePaperBinding;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.papers.PaperProfileClickCallback;

import java.util.List;

public class ChoosePaperDialogFragment extends DialogFragment {
    private static final String TAG = ChoosePaperDialogFragment.class.getSimpleName();
    private String requestKey;
    private ChoosePaperDialogViewModel viewModel;
    private DialogChoosePaperBinding binding;
    private ChoosePaperDialogAdapter adapter;

    public ChoosePaperDialogFragment() {
    }

    public static ChoosePaperDialogFragment create(String requestKey) {
        Bundle args = new Bundle();
        args.putString("requestKey", requestKey);
        ChoosePaperDialogFragment dialog = new ChoosePaperDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_choose_paper, null, false);

        adapter = new ChoosePaperDialogAdapter(adapterClickCallback);
        binding.paperProfileList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChoosePaperDialogViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setDialogViewModel(viewModel);

        viewModel.getPaperProfiles().observe(requireActivity(),
                new Observer<List<PaperProfileEntity>>() {
                    @Override
                    public void onChanged(List<PaperProfileEntity> paperProfileEntities) {
                        if (paperProfileEntities != null) {
                            adapter.setPaperProfileList(paperProfileEntities);
                        }
                        binding.executePendingBindings();
                    }
                });

        View view = binding.getRoot();

        RecyclerView recyclerView = view.findViewById(R.id.paper_profile_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        builder.setView(view).setTitle(R.string.dialog_choose_paper_profile);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            requestKey = arguments.getString("requestKey");
        }

        return builder.create();
    }

    private PaperProfileClickCallback adapterClickCallback = new PaperProfileClickCallback() {
        @Override
        public void onClick(PaperProfile paperProfile) {
            Bundle result = new Bundle();
            if (paperProfile instanceof PaperProfileEntity) {
                result.putParcelable("paperProfile", (PaperProfileEntity)paperProfile);
            }
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        }
    };
}
