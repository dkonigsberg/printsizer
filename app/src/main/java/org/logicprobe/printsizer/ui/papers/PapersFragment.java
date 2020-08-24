package org.logicprobe.printsizer.ui.papers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.FragmentPapersBinding;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PapersFragment extends Fragment {
    private static final String TAG = PapersFragment.class.getSimpleName();
    private static final String DELETE_PROFILES_KEY = PapersFragment.class.getSimpleName() + "_DELETE_PROFILES";
    private FragmentPapersBinding binding;
    private PapersViewModel papersViewModel;
    private PaperProfileAdapter paperProfileAdapter;
    private SelectionTracker<Long> selectionTracker;
    private ActionMode actionMode = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_papers, container, false);
        final View root = binding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.paper_profile_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        paperProfileAdapter = new PaperProfileAdapter(clickCallback);
        binding.paperProfileList.setAdapter(paperProfileAdapter);
        binding.setIsLoaded(false);

        SpeedDialView speedDial = root.findViewById(R.id.speedDial);

        speedDial.addActionItem(new SpeedDialActionItem.Builder(
                R.id.action_add_user_paper, R.drawable.ic_add_paper_profile)
                .setLabel(R.string.action_add_paper_profile)
                .setFabBackgroundColor(getResources().getColor(R.color.onPrimaryColor))
                .setFabImageTintColor(getResources().getColor(R.color.primaryColor))
                .setLabelClickable(true)
                .create());

        speedDial.addActionItem(new SpeedDialActionItem.Builder(
                R.id.action_add_stock_paper, R.drawable.ic_add_stock_profile)
                .setLabel(R.string.action_add_stock_profile)
                .setFabBackgroundColor(getResources().getColor(R.color.onPrimaryColor))
                .setFabImageTintColor(getResources().getColor(R.color.primaryColor))
                .setLabelClickable(true)
                .create());

        speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.action_add_user_paper:
                        Log.d(TAG, "Add user-provided paper profile");
                        Navigation.findNavController(root).navigate(R.id.action_add_paper);
                        break;
                    case R.id.action_add_stock_paper:
                        Log.d(TAG, "Add stock paper profile");
                        Navigation.findNavController(root).navigate(R.id.action_add_stock_paper);
                        break;
                    default:
                        Log.d(TAG, "Unknown action id: " + actionItem.getId());
                        break;
                }
                return false;
            }
        });

        getParentFragmentManager().setFragmentResultListener(DELETE_PROFILES_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int which = result.getInt("which");
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                }
            }
        });

        return root;
    }

    private final PaperProfileClickCallback clickCallback = new PaperProfileClickCallback() {
        @Override
        public void onClick(PaperProfile paperProfile) {
            Bundle bundle = new Bundle();
            bundle.putInt("id", paperProfile.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_edit_paper, bundle);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        papersViewModel = new ViewModelProvider(requireActivity()).get(PapersViewModel.class);

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setPapersViewModel(papersViewModel);

        papersViewModel.getPaperProfiles().observe(getViewLifecycleOwner(),
                new Observer<List<PaperProfileEntity>>() {
                    @Override
                    public void onChanged(List<PaperProfileEntity> paperProfileEntities) {
                        if (paperProfileEntities != null) {
                            binding.setIsLoaded(true);
                            paperProfileAdapter.setPaperProfileList(paperProfileEntities);
                        } else {
                            binding.setIsLoaded(false);
                        }
                        binding.executePendingBindings();
                    }
                });

        selectionTracker = new SelectionTracker.Builder<>(
                "paper_profile_selection",
                binding.paperProfileList,
                new PaperProfileKeyProvider(paperProfileAdapter),
                new PaperProfileDetailsLookup(binding.paperProfileList),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();
        paperProfileAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                if (selectionTracker.hasSelection()) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback);
                    }
                    int count = selectionTracker.getSelection().size();
                    Resources res = getResources();
                    actionMode.setTitle(res.getQuantityString(R.plurals.title_list_selection, count, count));
                    actionMode.getMenu().findItem(R.id.action_copy).setVisible(count == 1);
                } else {
                    if (actionMode != null) {
                        actionMode.finish();
                        actionMode = null;
                    }
                }
            }
        });
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_profile_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_copy:
                    copySelectedPaperProfile(actionMode);
                    return true;
                case R.id.action_delete:
                    deleteSelectedPaperProfiles(actionMode);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            selectionTracker.clearSelection();
        }
    };

    private void copySelectedPaperProfile(ActionMode actionMode) {
        Log.d(TAG, "Copy paper profile: " + selectionTracker.getSelection());
        Bundle bundle = new Bundle();
        bundle.putInt("id", getFirstSelectedId());
        Navigation.findNavController(requireView()).navigate(R.id.action_add_paper, bundle);
        actionMode.finish();
    }

    private void deleteSelectedPaperProfiles(ActionMode actionMode) {
        Log.d(TAG, "Delete paper profile: " + selectionTracker.getSelection());
        List<Integer> idList = getSelectedIdList();
        if (idList.size() == 0) {
            return;
        }
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.create(DELETE_PROFILES_KEY, idList);
        dialog.show(getParentFragmentManager(), "delete_papers_alert");
    }

    private int getFirstSelectedId() {
        int selectedId;
        if (selectionTracker != null && selectionTracker.hasSelection()) {
            selectedId = (int)selectionTracker.getSelection().iterator().next().longValue();
        } else {
            selectedId = -1;
        }
        return selectedId;
    }

    private List<Integer> getSelectedIdList() {
        List<Integer> selectedIds;
        if (selectionTracker != null && selectionTracker.hasSelection()) {
            selectedIds = new ArrayList<>(selectionTracker.getSelection().size());
            for (Long element : selectionTracker.getSelection()) {
                selectedIds.add(element.intValue());
            }
        } else {
            selectedIds = new ArrayList<>(0);
        }
        return selectedIds;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        paperProfileAdapter = null;
        papersViewModel = null;
        selectionTracker = null;
        super.onDestroyView();
    }

    public static class ConfirmDeleteDialogFragment extends DialogFragment {
        public ConfirmDeleteDialogFragment() {
        }

        public static ConfirmDeleteDialogFragment create(String requestKey, List<Integer> idList) {
            int[] idArray = new int[idList.size()];
            for (int i = 0; i < idList.size(); i++) {
                idArray[i] = idList.get(i);
            }

            ConfirmDeleteDialogFragment dialog = new ConfirmDeleteDialogFragment();
            Bundle args = new Bundle();
            args.putString("requestKey", requestKey);
            args.putIntArray("idList", idArray);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Bundle args = requireArguments();
            final String requestKey = args.getString("requestKey");
            final int[] idList = args.getIntArray("idList");

            if (requestKey == null || idList == null) {
                throw new IllegalStateException("Dialog created without required arguments");
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
            Resources res = getResources();
            builder.setTitle(res.getQuantityString(R.plurals.title_delete_profiles_dialog, idList.length, idList.length))
                    .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            Log.d(TAG, "Delete confirmed for: " + Arrays.toString(idList));
                            App app = (App)requireActivity().getApplication();
                            app.getRepository().deletePaperProfilesById(idList);

                            Bundle result = new Bundle();
                            result.putInt("which", which);
                            getParentFragmentManager().setFragmentResult(requestKey, result);
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            Log.d(TAG, "Delete canceled for: " + Arrays.toString(idList));

                            Bundle result = new Bundle();
                            result.putInt("which", which);
                            getParentFragmentManager().setFragmentResult(requestKey, result);
                        }
                    });

            return builder.create();
        }
    }
}