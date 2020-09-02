package org.logicprobe.printsizer.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.BurnDodgeItemBinding;

import java.util.List;

public class BurnDodgeAdapter extends RecyclerView.Adapter<BurnDodgeViewHolder> {
    private static final String TAG = BurnDodgeAdapter.class.getSimpleName();
    private List<BurnDodgeItem> burnDodgeList;
    private final BurnDodgeClickCallback clickCallback;

    public BurnDodgeAdapter(BurnDodgeClickCallback clickCallback) {
        this.clickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setBurnDodgeList(final List<BurnDodgeItem> burnDodgeList) {
        if (this.burnDodgeList == null) {
            this.burnDodgeList = burnDodgeList;
            notifyItemRangeInserted(0, burnDodgeList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return BurnDodgeAdapter.this.burnDodgeList.size();
                }

                @Override
                public int getNewListSize() {
                    return burnDodgeList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return BurnDodgeAdapter.this.burnDodgeList.get(oldItemPosition) ==
                            burnDodgeList.get(newItemPosition);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    if (oldItemPosition != newItemPosition) {
                        // Assume the index values have changed if the item position has changed.
                        // This is necessary to avoid needing to deep-copy the entire list on
                        // every single change.
                        return false;
                    } else {
                        BurnDodgeItem newItem = burnDodgeList.get(newItemPosition);
                        BurnDodgeItem oldItem = BurnDodgeAdapter.this.burnDodgeList.get(oldItemPosition);
                        return newItem.equals(oldItem);
                    }
                }
            });
            BurnDodgeAdapter.this.burnDodgeList = burnDodgeList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public BurnDodgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BurnDodgeItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.burn_dodge_item,
                parent, false);
        return new BurnDodgeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BurnDodgeViewHolder holder, int position) {
        BurnDodgeItem item = burnDodgeList.get(position);
        holder.bind(item, clickCallback);
    }

    @Override
    public int getItemCount() {
        return burnDodgeList == null ? 0 : burnDodgeList.size();
    }

    @Override
    public long getItemId(int position) {
        if (position > 0 && position < burnDodgeList.size()) {
            BurnDodgeItem item = burnDodgeList.get(position);
            if (item != null) {
                return item.getItemId();
            }
        }
        return RecyclerView.NO_ID;
    }
}
