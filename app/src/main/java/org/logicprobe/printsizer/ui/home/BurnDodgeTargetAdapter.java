package org.logicprobe.printsizer.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.BurnDodgeTargetItemBinding;

import java.util.List;

public class BurnDodgeTargetAdapter extends RecyclerView.Adapter<BurnDodgeTargetViewHolder> {
    private List<BurnDodgeTargetItem> burnDodgeTargetList;

    public BurnDodgeTargetAdapter() {
        setHasStableIds(true);
    }

    public void setBurnDodgeTargetList(final List<BurnDodgeTargetItem> burnDodgeTargetList) {
        if (this.burnDodgeTargetList == null) {
            this.burnDodgeTargetList = burnDodgeTargetList;
            notifyItemRangeInserted(0, burnDodgeTargetList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return BurnDodgeTargetAdapter.this.burnDodgeTargetList.size();
                }

                @Override
                public int getNewListSize() {
                    return burnDodgeTargetList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return BurnDodgeTargetAdapter.this.burnDodgeTargetList.get(oldItemPosition) ==
                            burnDodgeTargetList.get(newItemPosition);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    if (oldItemPosition != newItemPosition) {
                        // Assume the index values have changed if the item position has changed.
                        // This is necessary to avoid needing to deep-copy the entire list on
                        // every single change.
                        return false;
                    } else {
                        BurnDodgeTargetItem newItem = burnDodgeTargetList.get(newItemPosition);
                        BurnDodgeTargetItem oldItem = BurnDodgeTargetAdapter.this.burnDodgeTargetList.get(oldItemPosition);
                        return newItem.equals(oldItem);
                    }
                }
            });
            BurnDodgeTargetAdapter.this.burnDodgeTargetList = burnDodgeTargetList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public BurnDodgeTargetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BurnDodgeTargetItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.burn_dodge_target_item,
                parent, false);
        return new BurnDodgeTargetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BurnDodgeTargetViewHolder holder, int position) {
        BurnDodgeTargetItem item = burnDodgeTargetList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return burnDodgeTargetList == null ? 0 : burnDodgeTargetList.size();
    }

    @Override
    public long getItemId(int position) {
        if (position > 0 && position < burnDodgeTargetList.size()) {
            BurnDodgeTargetItem item = burnDodgeTargetList.get(position);
            if (item != null) {
                return item.getItemId();
            }
        }
        return RecyclerView.NO_ID;
    }
}
