package org.logicprobe.printsizer.ui.home;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.PaperProfileItemBinding;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.papers.PaperProfileClickCallback;

import java.util.List;

public class ChoosePaperDialogAdapter extends RecyclerView.Adapter<ChoosePaperDialogViewHolder> {
    private static final String TAG = ChoosePaperDialogAdapter.class.getSimpleName();
    private List<? extends PaperProfile> paperProfileList;

    @Nullable
    private PaperProfileClickCallback clickCallback;

    public ChoosePaperDialogAdapter(@Nullable PaperProfileClickCallback clickCallback) {
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
                    return ChoosePaperDialogAdapter.this.paperProfileList.size();
                }

                @Override
                public int getNewListSize() {
                    return paperProfileList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return ChoosePaperDialogAdapter.this.paperProfileList.get(oldItemPosition).getId() ==
                            paperProfileList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    PaperProfile newProfile = paperProfileList.get(newItemPosition);
                    PaperProfile oldProfile = ChoosePaperDialogAdapter.this.paperProfileList.get(oldItemPosition);
                    return newProfile.getId() == oldProfile.getId()
                            && TextUtils.equals(newProfile.getName(), oldProfile.getName())
                            && TextUtils.equals(newProfile.getDescription(), oldProfile.getDescription());
                    //TODO make this comparison better, if we display more fields on the list
                }
            });
            ChoosePaperDialogAdapter.this.paperProfileList = paperProfileList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ChoosePaperDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PaperProfileItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.paper_profile_item,
                parent, false);
        binding.setCallback(clickCallback);
        return new ChoosePaperDialogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoosePaperDialogViewHolder holder, int position) {
        PaperProfile paperProfile = paperProfileList.get(position);
        holder.bind(paperProfile);
    }

    @Override
    public int getItemCount() {
        return paperProfileList == null ? 0 : paperProfileList.size();
    }

    @Override
    public long getItemId(int position) {
        return paperProfileList.get(position).getId();
    }
}
