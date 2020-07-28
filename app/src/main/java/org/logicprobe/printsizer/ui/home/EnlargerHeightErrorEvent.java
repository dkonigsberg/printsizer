package org.logicprobe.printsizer.ui.home;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.ui.ModelErrorEvent;

public enum EnlargerHeightErrorEvent implements ModelErrorEvent {
    NONE(0),
    INVALID(R.string.error_enlarger_height_invalid),
    TOO_LOW_FOR_FOCAL_LENGTH(R.string.error_enlarger_height_too_low_for_focal_length);

    private final int resourceId;

    EnlargerHeightErrorEvent(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getErrorResource() {
        return resourceId;
    }
}
