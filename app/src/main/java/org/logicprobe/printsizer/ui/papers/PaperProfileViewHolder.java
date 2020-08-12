package org.logicprobe.printsizer.ui.papers;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.databinding.PaperProfileItemBinding;
import org.logicprobe.printsizer.model.PaperProfile;

public class PaperProfileViewHolder extends RecyclerView.ViewHolder {
    private final PaperProfileItemBinding binding;

    public PaperProfileViewHolder(PaperProfileItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(PaperProfile paperProfile, boolean selected) {
        binding.setPaperProfile(paperProfile);
        itemView.setActivated(selected);
        binding.executePendingBindings();
    }

    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new PaperProfileItemDetails(getAdapterPosition(), binding.getPaperProfile().getId());
    }
}
