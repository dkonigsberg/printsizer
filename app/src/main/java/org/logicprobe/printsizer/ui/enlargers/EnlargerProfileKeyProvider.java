package org.logicprobe.printsizer.ui.enlargers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

public class EnlargerProfileKeyProvider extends ItemKeyProvider<Long> {
    private final EnlargerProfileAdapter adapter;

    public EnlargerProfileKeyProvider(EnlargerProfileAdapter adapter) {
        super(ItemKeyProvider.SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public int getPosition(@NonNull Long key) {
        return adapter.getItemPosition(key);
    }
}
