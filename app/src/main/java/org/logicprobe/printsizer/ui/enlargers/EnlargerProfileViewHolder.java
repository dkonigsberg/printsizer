package org.logicprobe.printsizer.ui.enlargers;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.databinding.EnlargerProfileItemBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;

public class EnlargerProfileViewHolder extends RecyclerView.ViewHolder {
    private final EnlargerProfileItemBinding binding;

    public EnlargerProfileViewHolder(EnlargerProfileItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(EnlargerProfile enlargerProfile, boolean selected) {
        binding.setEnlargerProfile(enlargerProfile);
        itemView.setActivated(selected);
        binding.executePendingBindings();
    }

    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new EnlargerProfileItemDetails(getAdapterPosition(), binding.getEnlargerProfile().getId());
    }
}
