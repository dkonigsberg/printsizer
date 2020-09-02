package org.logicprobe.printsizer.ui.home;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.BurnDodgeItemBinding;

public class BurnDodgeViewHolder extends RecyclerView.ViewHolder {
    private final BurnDodgeItemBinding binding;

    public BurnDodgeViewHolder(BurnDodgeItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(final BurnDodgeItem item, final BurnDodgeClickCallback clickCallback) {
        binding.setBurnDodge(item);
        View root = binding.getRoot();

        TextInputLayout valueLayout = root.findViewById(R.id.editBurnDodgeValueLayout);
        valueLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCallback != null) {
                    clickCallback.onEditItem(item);
                }
            }
        });

        Button removeButton = root.findViewById(R.id.buttonRemoveBurnDodge);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCallback != null) {
                    clickCallback.onRemoveItem(item);
                }
            }
        });

        binding.executePendingBindings();
    }
}
