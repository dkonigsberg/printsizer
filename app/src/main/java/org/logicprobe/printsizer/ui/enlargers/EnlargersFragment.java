package org.logicprobe.printsizer.ui.enlargers;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.FragmentEnlargersBinding;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.model.EnlargerProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnlargersFragment extends Fragment {
    private static final String TAG = EnlargersFragment.class.getSimpleName();
    private static final String DELETE_PROFILES_KEY = EnlargersFragment.class.getSimpleName() + "_DELETE_PROFILES";
    private FragmentEnlargersBinding binding;
    private EnlargersViewModel enlargersViewModel;
    private EnlargerProfileAdapter enlargerProfileAdapter;
    private SelectionTracker<Long> selectionTracker;
    private ActionMode actionMode = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enlargers, container, false);
        final View root = binding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.enlarger_profile_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        enlargerProfileAdapter = new EnlargerProfileAdapter(clickCallback);
        binding.enlargerProfileList.setAdapter(enlargerProfileAdapter);
        binding.setIsLoaded(false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(root).navigate(R.id.action_add_enlarger);
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

    private final EnlargerProfileClickCallback clickCallback = new EnlargerProfileClickCallback() {
        @Override
        public void onClick(EnlargerProfile enlargerProfile) {
            Bundle bundle = new Bundle();
            bundle.putInt("id", enlargerProfile.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_edit_enlarger, bundle);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        enlargersViewModel = new ViewModelProvider(requireActivity()).get(EnlargersViewModel.class);

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setEnlargersViewModel(enlargersViewModel);

        enlargersViewModel.getEnlargerProfiles().observe(getViewLifecycleOwner(),
                new Observer<List<EnlargerProfileEntity>>() {
                    @Override
                    public void onChanged(List<EnlargerProfileEntity> enlargerProfileEntities) {
                        if (enlargerProfileEntities != null) {
                            binding.setIsLoaded(true);
                            enlargerProfileAdapter.setEnlargerProfileList(enlargerProfileEntities);
                        } else {
                            binding.setIsLoaded(false);
                        }
                        binding.executePendingBindings();
                    }
                });

        selectionTracker = new SelectionTracker.Builder<>(
                "profile_selection",
                binding.enlargerProfileList,
                new EnlargerProfileKeyProvider(enlargerProfileAdapter),
                new EnlargerProfileDetailsLookup(binding.enlargerProfileList),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();
        enlargerProfileAdapter.setSelectionTracker(selectionTracker);

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
                    copySelectedEnlargerProfile(actionMode);
                    return true;
                case R.id.action_delete:
                    deleteSelectedEnlargerProfiles(actionMode);
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

    private void copySelectedEnlargerProfile(ActionMode actionMode) {
        Log.d(TAG, "Copy enlarger profile: " + selectionTracker.getSelection());
        Bundle bundle = new Bundle();
        bundle.putInt("id", getFirstSelectedId());
        Navigation.findNavController(requireView()).navigate(R.id.action_add_enlarger, bundle);
        actionMode.finish();
    }

    private void deleteSelectedEnlargerProfiles(final ActionMode actionMode) {
        Log.d(TAG, "Delete enlarger profile: " + selectionTracker.getSelection());
        final List<Integer> idList = getSelectedIdList();
        if (idList.size() == 0) {
            return;
        }
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.create(DELETE_PROFILES_KEY, idList);
        dialog.show(getParentFragmentManager(), "delete_enlargers_alert");
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
        enlargerProfileAdapter = null;
        enlargersViewModel = null;
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
                            app.getRepository().deleteEnlargerProfilesById(idList);

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