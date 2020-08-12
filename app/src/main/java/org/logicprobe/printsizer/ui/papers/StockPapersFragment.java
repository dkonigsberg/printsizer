package org.logicprobe.printsizer.ui.papers;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.FragmentStockPapersBinding;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockPapersFragment extends Fragment {
    private static final String TAG = StockPapersFragment.class.getSimpleName();
    private FragmentStockPapersBinding binding;
    private StockPapersViewModel papersViewModel;
    private PaperProfileAdapter paperProfileAdapter;
    private SelectionTracker<Long> selectionTracker;
    private Map<Integer, PaperProfileEntity> stockProfileMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stock_papers, container, false);
        final View root = binding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.paper_profile_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        paperProfileAdapter = new PaperProfileAdapter(clickCallback);
        binding.paperProfileList.setAdapter(paperProfileAdapter);
        binding.setIsLoaded(false);
        stockProfileMap = new HashMap<>();

        return root;
    }

    private final PaperProfileClickCallback clickCallback = new PaperProfileClickCallback() {
        @Override
        public void onClick(PaperProfile paperProfile) {
            List<Integer> idList = new ArrayList<>(1);
            idList.add(paperProfile.getId());
            addPaperProfiles(idList);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        papersViewModel = new ViewModelProvider(requireActivity()).get(StockPapersViewModel.class);

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setPapersViewModel(papersViewModel);

        papersViewModel.getStockPaperProfiles().observe(getViewLifecycleOwner(),
                new Observer<List<PaperProfileEntity>>() {
            @Override
            public void onChanged(List<PaperProfileEntity> paperProfileEntities) {
                if (paperProfileEntities != null) {
                    binding.setIsLoaded(true);
                    paperProfileAdapter.setPaperProfileList(paperProfileEntities);
                    for (PaperProfileEntity entity : paperProfileEntities) {
                        stockProfileMap.put(entity.getId(), entity);
                    }
                } else {
                    binding.setIsLoaded(false);
                }
                binding.executePendingBindings();
            }
        });

        selectionTracker = new SelectionTracker.Builder<>(
                "stock_paper_profile_selection",
                binding.paperProfileList,
                new PaperProfileKeyProvider(paperProfileAdapter),
                new PaperProfileDetailsLookup(binding.paperProfileList),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();
        paperProfileAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            private ActionMode actionMode = null;
            @Override
            public void onSelectionChanged() {
                if (selectionTracker.hasSelection()) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback);
                    }
                    int count = selectionTracker.getSelection().size();
                    Resources res = getResources();
                    actionMode.setTitle(res.getQuantityString(R.plurals.title_list_selection, count, count));
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
            inflater.inflate(R.menu.menu_stock_profile_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_add_stock_paper) {
                addSelectedPaperProfiles(actionMode);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            selectionTracker.clearSelection();
        }
    };

    private void addSelectedPaperProfiles(ActionMode actionMode) {
        Log.d(TAG, "Add paper profile: " + selectionTracker.getSelection());
        List<Integer> idList = getSelectedIdList();
        if (idList.size() == 0) {
            return;
        }
        addPaperProfiles(idList);
        actionMode.finish();
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

    private void addPaperProfiles(List<Integer> idList) {
        // Transform the list of IDs into a list of detached entity objects,
        // and clear their profile ID values to avoid DB insertion conflicts.
        List<PaperProfileEntity> entityList = new ArrayList<>(idList.size());
        for (Integer id : idList) {
            PaperProfileEntity entity = stockProfileMap.get(id);
            if (entity != null) {
                PaperProfileEntity entityCopy = new PaperProfileEntity(entity);
                entityCopy.setId(0);
                entityList.add(entityCopy);
            }
        }
        if (idList.size() == 0) {
            Log.e(TAG, "No profiles selected for insertion");
            return;
        }

        App app = (App) requireActivity().getApplication();
        LiveData<List<Integer>> liveProfileIds = app.getRepository().insertAll(entityList);
        liveProfileIds.observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> integers) {
                Log.d(TAG, "Saved added stock profiles");
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        paperProfileAdapter = null;
        papersViewModel = null;
        selectionTracker = null;
        super.onDestroyView();
    }
}
