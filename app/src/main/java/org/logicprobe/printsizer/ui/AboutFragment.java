package org.logicprobe.printsizer.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.logicprobe.printsizer.R;

public class AboutFragment extends Fragment {
    private static final String TAG = AboutFragment.class.getSimpleName();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView textViewAppVersion = root.findViewById(R.id.textViewAppVersion);

        try {
            Context context = getContext();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            if (info.versionName != null && info.versionName.length() > 0) {
                textViewAppVersion.setText(getString(R.string.label_version_prefix, info.versionName));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }

        return root;
    }
}
