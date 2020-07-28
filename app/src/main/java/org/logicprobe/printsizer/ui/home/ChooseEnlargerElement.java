package org.logicprobe.printsizer.ui.home;

import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

public class ChooseEnlargerElement {
    private final EnlargerProfileEntity enlargerProfile;
    private final int actionId;

    private ChooseEnlargerElement(EnlargerProfileEntity enlargerProfile, int actionId) {
        this.enlargerProfile = enlargerProfile;
        this.actionId = actionId;
    }

    public static ChooseEnlargerElement create(EnlargerProfileEntity enlargerProfile) {
        if (enlargerProfile == null) {
            throw new NullPointerException("enlargerProfile cannot be null");
        } else if (enlargerProfile.getId() <= 0) {
            throw new IllegalArgumentException("enlargerProfile does not contain a valid ID");
        }
        return new ChooseEnlargerElement(enlargerProfile, -1);
    }

    public static ChooseEnlargerElement createAction(int actionId) {
        if (actionId < 0) {
            throw new IllegalArgumentException("actionId must be greater than zero");
        }
        return new ChooseEnlargerElement(null, actionId);
    }

    public long getElementId() {
        if (enlargerProfile != null) {
            return enlargerProfile.getId();
        } else {
            return -1 * actionId;
        }
    }

    public EnlargerProfileEntity profile() {
        return enlargerProfile;
    }

    public int getActionId() {
        return actionId;
    }
}
