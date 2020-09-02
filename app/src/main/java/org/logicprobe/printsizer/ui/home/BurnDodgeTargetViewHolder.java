package org.logicprobe.printsizer.ui.home;

import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.databinding.BurnDodgeTargetItemBinding;

public class BurnDodgeTargetViewHolder extends RecyclerView.ViewHolder {
    private BurnDodgeTargetItemBinding binding;

    public BurnDodgeTargetViewHolder(BurnDodgeTargetItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(final BurnDodgeTargetItem item) {
        binding.setBurnDodge(item);
        binding.executePendingBindings();
    }
}
