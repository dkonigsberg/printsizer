package org.logicprobe.printsizer.ui.papers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

public class PaperProfileKeyProvider extends ItemKeyProvider<Long> {
    private final PaperProfileAdapter adapter;

    public PaperProfileKeyProvider(PaperProfileAdapter adapter) {
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
