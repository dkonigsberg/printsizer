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

    public static class Builder {
        private final Bundle args;

        public Builder() {
            args = new Bundle();
        }

        public Builder setRequestKey(String requestKey) {
            args.putString("requestKey", requestKey);
            return this;
        }

        public Builder setTitle(@StringRes int titleResourceId) {
            args.putInt("titleResourceId", titleResourceId);
            return this;
        }

        public Builder setPaperProfile(PaperProfileEntity paperProfile) {
            args.putParcelable("paperProfile", paperProfile);
            return this;
        }

        public Builder setPaperGradeId(@PaperProfile.GradeId int paperGradeId) {
            args.putInt("gradeId", paperGradeId);
            return this;
        }

        public Builder setRemoveButton(boolean showRemove) {
            args.putBoolean("showRemove", showRemove);
            return this;
        }

        public Builder setReferencePaperIsoR(int isoR) {
            args.putInt("referenceIsoR", isoR);
            return this;
        }

        public PaperSettingsDialogFragment create() {
            final PaperSettingsDialogFragment dialog = new PaperSettingsDialogFragment();
            dialog.setArguments(args);
            return dialog;
        }
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

        if (args.getBoolean("showRemove")) {
            builder.setNeutralButton(R.string.action_remove, buttonClickListener);
        }

        builder.setTitle(titleResourceId);

        // Only set arguments to the model if the model has not yet been initialized.
        // Otherwise, we might wipe out any user changes that have occurred before an
        // activity recreation.
        if (!viewModel.isInitialized()) {
            PaperProfileEntity paperProfile = args.getParcelable("paperProfile");
            int gradeId = args.getInt("gradeId", -10);
            int referenceIsoR = args.getInt("referenceIsoR");

            viewModel.setPaperProfile(paperProfile);
            viewModel.setReferenceIsoR(referenceIsoR);
            if (gradeId != -10) {
                viewModel.setPaperGrade(gradeId);
            }
        }

        getParentFragmentManager().setFragmentResultListener(SELECT_PAPER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                PaperProfileEntity paperProfile = result.getParcelable("paperProfile");
                if (paperProfile != null) {
                    Log.d(TAG, "Paper profile selected: [" + paperProfile.getId() + "] " + paperProfile.getName());
                    handlePaperProfileSelected(paperProfile);
                }
            }
        });

        viewModel.setInitialized(true);

        return builder.create();
    }

    private DialogInterface.OnClickListener buttonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            if (requestKey != null && requestKey.length() > 0) {
                Bundle result = new Bundle();
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Accept
                    LiveData<PaperProfileEntity> livePaperProfile = viewModel.getPaperProfile();
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

    private void handlePaperProfileSelected(PaperProfileEntity paperProfile) {
        viewModel.setPaperProfile(paperProfile);
        // If the new paper profile does not contain the currently selected grade,
        // then the model will attempt to select a next best choice.
    }
}
