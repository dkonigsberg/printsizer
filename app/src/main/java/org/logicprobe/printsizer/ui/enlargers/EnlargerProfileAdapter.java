package org.logicprobe.printsizer.ui.enlargers;

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
import org.logicprobe.printsizer.databinding.EnlargerProfileItemBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;

import java.util.List;

public class EnlargerProfileAdapter extends RecyclerView.Adapter<EnlargerProfileViewHolder> {
    private static final String TAG = EnlargerProfileAdapter.class.getSimpleName();
    private List<? extends EnlargerProfile> enlargerProfileList;
    private SelectionTracker<Long> selectionTracker;

    @Nullable
    private final EnlargerProfileClickCallback clickCallback;

    public EnlargerProfileAdapter(@Nullable EnlargerProfileClickCallback clickCallback) {
        this.clickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setEnlargerProfileList(final List<? extends EnlargerProfile> enlargerProfileList) {
        if (this.enlargerProfileList == null) {
            this.enlargerProfileList = enlargerProfileList;
            notifyItemRangeInserted(0, enlargerProfileList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return EnlargerProfileAdapter.this.enlargerProfileList.size();
                }

                @Override
                public int getNewListSize() {
                    return enlargerProfileList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return EnlargerProfileAdapter.this.enlargerProfileList.get(oldItemPosition).getId() ==
                            enlargerProfileList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    EnlargerProfile newProfile = enlargerProfileList.get(newItemPosition);
                    EnlargerProfile oldProfile = EnlargerProfileAdapter.this.enlargerProfileList.get(oldItemPosition);
                    return newProfile.getId() == oldProfile.getId()
                            && TextUtils.equals(newProfile.getName(), oldProfile.getName())
                            && TextUtils.equals(newProfile.getDescription(), oldProfile.getDescription())
                            && newProfile.getLensFocalLength() == oldProfile.getLensFocalLength();
                }
            });
            EnlargerProfileAdapter.this.enlargerProfileList = enlargerProfileList;
            result.dispatchUpdatesTo(this);
        }
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public EnlargerProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        EnlargerProfileItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.enlarger_profile_item,
                        parent, false);
        binding.setCallback(clickCallback);
        return new EnlargerProfileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EnlargerProfileViewHolder holder, int position) {
        EnlargerProfile enlargerProfile = enlargerProfileList.get(position);
        boolean selected;
        if (selectionTracker != null) {
            selected = selectionTracker.isSelected((long)enlargerProfile.getId());
        } else {
            selected = false;
        }
        holder.bind(enlargerProfile, selected);
    }

    @Override
    public int getItemCount() {
        return enlargerProfileList == null ? 0 : enlargerProfileList.size();
    }

    @Override
    public long getItemId(int position) {
        return enlargerProfileList.get(position).getId();
    }

    public int getItemPosition(long key) {
        for (int i = enlargerProfileList.size() - 1; i >= 0; --i) {
            if (enlargerProfileList.get(i).getId() == key) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
