package org.logicprobe.printsizer.ui.papers;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class PaperProfileItemDetails extends ItemDetailsLookup.ItemDetails<Long> {
    private int position;
    private int profileId;

    public PaperProfileItemDetails(int position, int profileId) {
        this.position = position;
        this.profileId = profileId;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return (long)profileId;
    }

    @Override
    public boolean inSelectionHotspot(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean inDragRegion(@NonNull MotionEvent e) {
        return true;
    }
}
