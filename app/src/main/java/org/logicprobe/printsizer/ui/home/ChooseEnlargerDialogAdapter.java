package org.logicprobe.printsizer.ui.home;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogChooseEnlargerProfileItemBinding;

import java.util.List;

public class ChooseEnlargerDialogAdapter extends RecyclerView.Adapter<ChooseEnlargerDialogViewHolder> {
    private static final String TAG = ChooseEnlargerDialogAdapter.class.getSimpleName();
    private ChooseEnlargerClickCallback clickCallback;
    private List<ChooseEnlargerElement> selectionList;
    private static final int ITEM_UNKNOWN = -1;
    private static final int ITEM_PROFILE = 0;
    private static final int ITEM_ACTION_ADD = 1;

    public ChooseEnlargerDialogAdapter(ChooseEnlargerClickCallback clickCallback) {
        this.clickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setSelectionList(final List<ChooseEnlargerElement> selectionList) {
        if (this.selectionList == null) {
            this.selectionList = selectionList;
            notifyItemRangeInserted(0, selectionList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return ChooseEnlargerDialogAdapter.this.selectionList.size();
                }

                @Override
                public int getNewListSize() {
                    return selectionList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return ChooseEnlargerDialogAdapter.this.selectionList.get(oldItemPosition).getElementId() ==
                            selectionList.get(newItemPosition).getElementId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    ChooseEnlargerElement newElement = selectionList.get(newItemPosition);
                    ChooseEnlargerElement oldElement = ChooseEnlargerDialogAdapter.this.selectionList.get(oldItemPosition);
                    return newElement.getElementId() == oldElement.getElementId()
                            && newElement.profile() != null && oldElement.profile() != null
                            && TextUtils.equals(newElement.profile().getName(), oldElement.profile().getName())
                            && TextUtils.equals(newElement.profile().getDescription(), oldElement.profile().getDescription())
                            && newElement.profile().getLensFocalLength() == oldElement.profile().getLensFocalLength();
                }
            });
            ChooseEnlargerDialogAdapter.this.selectionList = selectionList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ChooseEnlargerDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_PROFILE) {
            DialogChooseEnlargerProfileItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.dialog_choose_enlarger_profile_item,
                    parent, false);
            binding.setCallback(clickCallback);
            return new ChooseEnlargerDialogViewHolder(binding);
        } else if (viewType == ITEM_ACTION_ADD) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.dialog_choose_enlarger_add_item, parent, false);
            return new ChooseEnlargerDialogViewHolder(view);
        } else {
            return new ChooseEnlargerDialogViewHolder(new View(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseEnlargerDialogViewHolder holder, int position) {
        ChooseEnlargerElement element = selectionList.get(position);
        if (element.profile() != null) {
            holder.bind(element.profile());
        } else if (element.getActionId() == 1) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickCallback.onClickAction(1);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return selectionList == null ? 0 : selectionList.size();
    }

    @Override
    public long getItemId(int position) {
        return selectionList.get(position).getElementId();
    }

    @Override
    public int getItemViewType(int position) {
        ChooseEnlargerElement element = selectionList.get(position);
        if (element.getElementId() > 0 && element.profile() != null) {
            return ITEM_PROFILE;
        } else if (element.getActionId() == 1) {
            return ITEM_ACTION_ADD;
        } else {
            return ITEM_UNKNOWN;
        }
    }
}
