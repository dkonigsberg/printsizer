package org.logicprobe.printsizer.ui.papers;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.PaperProfileItemBinding;
import org.logicprobe.printsizer.model.PaperProfile;

import java.util.List;

public class PaperProfileAdapter extends RecyclerView.Adapter<PaperProfileViewHolder> {
    private static final String TAG = PaperProfileAdapter.class.getSimpleName();
    private List<? extends PaperProfile> paperProfileList;
    private SelectionTracker<Long> selectionTracker;

    @Nullable
    private final PaperProfileClickCallback clickCallback;
    
    public PaperProfileAdapter(@Nullable PaperProfileClickCallback clickCallback) {
        this.clickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setPaperProfileList(final List<? extends PaperProfile> paperProfileList) {
        if (this.paperProfileList == null) {
            this.paperProfileList = paperProfileList;
            notifyItemRangeInserted(0, paperProfileList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return PaperProfileAdapter.this.paperProfileList.size();
                }

                @Override
                public int getNewListSize() {
                    return paperProfileList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return PaperProfileAdapter.this.paperProfileList.get(oldItemPosition).getId() ==
                            paperProfileList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    PaperProfile newProfile = paperProfileList.get(newItemPosition);
                    PaperProfile oldProfile = PaperProfileAdapter.this.paperProfileList.get(oldItemPosition);
                    return newProfile.getId() == oldProfile.getId()
                            && TextUtils.equals(newProfile.getName(), oldProfile.getName())
                            && TextUtils.equals(newProfile.getDescription(), oldProfile.getDescription());
                    //TODO make this comparison better, if we display more fields on the list
                }
            });
            PaperProfileAdapter.this.paperProfileList = paperProfileList;
            result.dispatchUpdatesTo(this);
        }
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }
    
    @NonNull
    @Override
    public PaperProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        PaperProfileItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.paper_profile_item,
                        parent, false);
        binding.setCallback(clickCallback);
        return new PaperProfileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperProfileViewHolder holder, int position) {
        PaperProfile paperProfile = paperProfileList.get(position);
        boolean selected;
        if (selectionTracker != null) {
            selected = selectionTracker.isSelected((long)paperProfile.getId());
        } else {
            selected = false;
        }
        holder.bind(paperProfile, selected);
    }

    @Override
    public int getItemCount() {
        return paperProfileList == null ? 0 : paperProfileList.size();
    }

    @Override
    public long getItemId(int position) {
        return paperProfileList.get(position).getId();
    }

    public int getItemPosition(long key) {
        for (int i = paperProfileList.size() - 1; i >= 0; --i) {
            if (paperProfileList.get(i).getId() == key) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
