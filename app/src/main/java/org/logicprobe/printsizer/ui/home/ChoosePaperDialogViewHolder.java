package org.logicprobe.printsizer.ui.home;

import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.databinding.PaperProfileItemBinding;
import org.logicprobe.printsizer.model.PaperProfile;

public class ChoosePaperDialogViewHolder extends RecyclerView.ViewHolder {
    private final PaperProfileItemBinding binding;

    public ChoosePaperDialogViewHolder(PaperProfileItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(PaperProfile paperProfile) {
        binding.setPaperProfile(paperProfile);
        binding.executePendingBindings();
    }
}
