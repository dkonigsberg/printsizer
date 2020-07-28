package org.logicprobe.printsizer.ui.home;

import org.logicprobe.printsizer.model.EnlargerProfile;

public interface ChooseEnlargerClickCallback {
    void onClickProfile(EnlargerProfile enlargerProfile);
    void onClickAction(int actionId);
}
