package org.logicprobe.printsizer.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.logicprobe.printsizer.BuildConfig;
import org.logicprobe.printsizer.R;

public class AboutFragment extends Fragment {
    private static final String TAG = AboutFragment.class.getSimpleName();
    private static final String GITHUB_URL = "https://github.com/dkonigsberg/printsizer";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView textViewAppVersion = root.findViewById(R.id.textViewAppVersion);
        TextView textViewAppDescribe = root.findViewById(R.id.textViewAppDescribe);
        ImageView imageViewGithub = root.findViewById(R.id.imageViewGithub);

        textViewAppVersion.setText(getString(R.string.label_version_prefix, getVersionName()));

        if (!isTaggedVersion()) {
            textViewAppDescribe.setText(BuildConfig.VERSION_DESCRIBE);
            textViewAppDescribe.setVisibility(View.VISIBLE);
        } else {
            textViewAppDescribe.setVisibility(View.GONE);
        }

        imageViewGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        return root;
    }

    private String getVersionName() {
        String versionName = null;
        try {
            Context context = requireContext();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            if (info.versionName != null && info.versionName.length() > 0) {
                versionName = info.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        if (versionName == null) {
            versionName = "vX.Y";
        }
        return versionName;
    }

    private boolean isTaggedVersion() {
        String describe = BuildConfig.VERSION_DESCRIBE;
        String parts[] = describe.split("-");
        if (parts != null && parts.length > 2 && "0".equals(parts[1])) {
            return true;
        } else {
            return false;
        }
    }
}
