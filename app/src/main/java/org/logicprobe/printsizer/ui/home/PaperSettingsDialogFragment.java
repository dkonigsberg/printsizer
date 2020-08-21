package org.logicprobe.printsizer.ui.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import org.logicprobe.printsizer.LiveDataUtil;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogPaperSettingsBinding;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.Converter;
import org.logicprobe.printsizer.ui.papers.PaperProfileClickCallback;

public class PaperSettingsDialogFragment extends DialogFragment {
    private static final String TAG = PaperSettingsDialogFragment.class.getSimpleName();
    private static final String SELECT_PAPER_REQUEST_KEY = PaperSettingsDialogFragment.class.getSimpleName() + "_SELECT_PAPER";

    public static final int ACTION_ACCEPT = 1;
    public static final int ACTION_CANCEL = 2;
    public static final int ACTION_REMOVE = 3;

    private String requestKey;
    private PaperSettingsDialogViewModel viewModel;
    private DialogPaperSettingsBinding binding;

    public PaperSettingsDialogFragment() {
    }

    public static PaperSettingsDialogFragment create(
            String requestKey,
            @StringRes int titleResourceId,
            int paperProfileId, @PaperProfile.GradeId int gradeId,
            boolean showRemove) {
        return createImpl(requestKey, titleResourceId, paperProfileId, gradeId, showRemove);
    }

    public static PaperSettingsDialogFragment create(
            String requestKey,
            @StringRes int titleResourceId,
            int paperProfileId) {
        return createImpl(requestKey, titleResourceId, paperProfileId, Integer.MIN_VALUE, false);
    }

    private static PaperSettingsDialogFragment createImpl(
            String requestKey,
            @StringRes int titleResourceId,
            int paperProfileId, int gradeId,
            boolean showRemove) {
        Bundle args = new Bundle();
        args.putString("requestKey", requestKey);
        args.putInt("titleResourceId", titleResourceId);
        args.putInt("profileId", paperProfileId);
        args.putInt("gradeId", gradeId);
        args.putBoolean("showRemove", showRemove);
        PaperSettingsDialogFragment dialog = new PaperSettingsDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_paper_settings, null, false);
        viewModel = new ViewModelProvider(this).get(PaperSettingsDialogViewModel.class);
        binding.setDialogViewModel(viewModel);
        binding.setLifecycleOwner(getActivity());

        final View view = binding.getRoot();
        builder.setView(view);
        builder.setPositiveButton(R.string.action_accept, buttonClickListener);
        builder.setNegativeButton(R.string.action_cancel, buttonClickListener);

        final Slider sliderContrastGrade = view.findViewById(R.id.sliderContrastGrade);
        final TextView labelContrastGrade = view.findViewById(R.id.labelContrastGrade);

        sliderContrastGrade.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                viewModel.setGradeIndex((int)value);
            }
        });
        viewModel.getGradeIndex().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                sliderContrastGrade.setValue(integer);
            }
        });
        binding.setPaperProfileClickCallback(new PaperProfileClickCallback() {
            @Override
            public void onClick(PaperProfile paperProfile) {
                paperProfileClicked();
            }
        });

        float width = measureLongestGradeLabel(labelContrastGrade);
        labelContrastGrade.setMinWidth((int)width);
        labelContrastGrade.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        final Bundle args = requireArguments();
        requestKey = args.getString("requestKey");
        int titleResourceId = args.getInt("titleResourceId");
        int profileId = args.getInt("profileId");
        int gradeId = args.getInt("gradeId");

        if (args.getBoolean("showRemove")) {
            builder.setNeutralButton(R.string.action_remove, buttonClickListener);
        }

        builder.setTitle(titleResourceId);
        viewModel.setPaperProfileId(profileId);
        viewModel.setPaperGrade(gradeId);

        getParentFragmentManager().setFragmentResultListener(SELECT_PAPER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int paperProfileId = result.getInt("id", 0);
                Log.d(TAG, "Paper profile selected: " + paperProfileId);
                handlePaperProfileSelected(paperProfileId);
            }
        });

        return builder.create();
    }

    private DialogInterface.OnClickListener buttonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            if (requestKey != null && requestKey.length() > 0) {
                Bundle result = new Bundle();
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Accept
                    LiveData<PaperProfile> livePaperProfile = viewModel.getPaperProfile();
                    PaperProfile paperProfile = livePaperProfile != null ? livePaperProfile.getValue() : null;
                    if (paperProfile == null) { paperProfile = new PaperProfileEntity(); }

                    result.putInt("action", ACTION_ACCEPT);
                    result.putInt("profileId", paperProfile.getId());
                    result.putInt("gradeId", LiveDataUtil.getIntValue(viewModel.getPaperGrade()));
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    // Cancel
                    result.putInt("action", ACTION_CANCEL);
                } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                    // Remove
                    result.putInt("action", ACTION_REMOVE);
                }
                getParentFragmentManager().setFragmentResult(requestKey, result);
            }
        }
    };

    private static float measureLongestGradeLabel(TextView textView) {
        float width = 0;
        Resources res = textView.getResources();
        Paint paint = textView.getPaint();

        int[] resList = {
                PaperProfile.GRADE_00, PaperProfile.GRADE_0, PaperProfile.GRADE_1,
                PaperProfile.GRADE_2, PaperProfile.GRADE_3, PaperProfile.GRADE_4,
                PaperProfile.GRADE_5, PaperProfile.GRADE_NONE };

        for (int value : resList) {
            String text = res.getString(Converter.paperGradeToResourceId(value));
            float measure = paint.measureText(text);
            if (measure > width) {
                width = measure;
            }
        }

        return width;
    }

    private void paperProfileClicked() {
        ChoosePaperDialogFragment dialog = ChoosePaperDialogFragment.create(SELECT_PAPER_REQUEST_KEY);
        dialog.show(getParentFragmentManager(), "paper_settings_choose_paper_alert");
    }

    private void handlePaperProfileSelected(int paperProfileId) {
        viewModel.setPaperProfileId(paperProfileId);
        // If the new paper profile does not contain the currently selected grade,
        // then the model will attempt to select a next best choice.
    }
}
