package org.logicprobe.printsizer.ui.home;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.databinding.DialogChooseEnlargerProfileItemBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;

public class ChooseEnlargerDialogViewHolder extends RecyclerView.ViewHolder {
    private final DialogChooseEnlargerProfileItemBinding binding;

    public ChooseEnlargerDialogViewHolder(DialogChooseEnlargerProfileItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public ChooseEnlargerDialogViewHolder(View itemView) {
        super(itemView);
        this.binding = null;
    }

    public void bind(EnlargerProfile enlargerProfile) {
        if (binding != null) {
            binding.setEnlargerProfile(enlargerProfile);
            binding.executePendingBindings();
        }
    }
}
