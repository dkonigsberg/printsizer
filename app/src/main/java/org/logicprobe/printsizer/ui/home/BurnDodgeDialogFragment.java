package org.logicprobe.printsizer.ui.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.math3.fraction.Fraction;
import org.logicprobe.printsizer.LiveDataUtil;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.databinding.DialogBurnDodgeBinding;
import org.logicprobe.printsizer.model.ExposureAdjustment;
import org.logicprobe.printsizer.ui.Converter;

import java.util.Locale;

public class BurnDodgeDialogFragment extends DialogFragment {
    private static final String TAG = BurnDodgeDialogFragment.class.getSimpleName();

    public static final int ACTION_ACCEPT = 1;
    public static final int ACTION_CANCEL = 2;
    public static final int ACTION_REMOVE = 3;

    private TextInputLayout editAreaNameLayout;
    private TextInputEditText editAreaName;

    private TextInputEditText editSecondsAdj;
    private TextInputEditText editPercentAdj;

    private MaterialButtonToggleGroup adjTypeToggleGroup;

    private String requestKey;
    private BurnDodgeDialogViewModel viewModel;
    private DialogBurnDodgeBinding binding;

    public BurnDodgeDialogFragment() {
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

        public Builder setBurnDodgeItem(BurnDodgeItem burnDodgeItem) {
            args.putParcelable("burnDodgeItem", burnDodgeItem);
            return this;
        }

        public Builder setDefaultName(String defaultName) {
            args.putString("defaultName", defaultName);
            return this;
        }

        public Builder setAdjustmentUnit(@ExposureAdjustment.AdjustmentUnit int adjustmentUnit) {
            args.putInt("adjustmentUnit", adjustmentUnit);
            return this;
        }

        public Builder setBaseExposureTime(double baseExposureTime) {
            args.putDouble("baseExposureTime", baseExposureTime);
            return this;
        }

        public Builder setRemoveButton(boolean showRemove) {
            args.putBoolean("showRemove", showRemove);
            return this;
        }

        public BurnDodgeDialogFragment create() {
            final BurnDodgeDialogFragment dialog = new BurnDodgeDialogFragment();
            dialog.setArguments(args);
            return dialog;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_burn_dodge, null, false);
        viewModel = new ViewModelProvider(this).get(BurnDodgeDialogViewModel.class);
        binding.setLifecycleOwner(getActivity());
        binding.setDialogViewModel(viewModel);

        final View view = binding.getRoot();
        builder.setView(view);
        builder.setPositiveButton(R.string.action_accept, buttonClickListener);
        builder.setNegativeButton(R.string.action_cancel, buttonClickListener);

        final Bundle arguments = getArguments();
        boolean showRemove = false;
        if (arguments != null) {
            requestKey = arguments.getString("requestKey");
            showRemove = arguments.getBoolean("showRemove");
        }

        if (showRemove) {
            builder.setNeutralButton(R.string.action_remove, buttonClickListener);
        }

        if (!viewModel.isInitialized()) {
            BurnDodgeItem burnDodgeItem = null;
            String defaultName = null;
            int adjustmentUnit = ExposureAdjustment.UNIT_NONE;
            double baseExposureTime = Double.NaN;
            if (arguments != null) {
                burnDodgeItem = arguments.getParcelable("burnDodgeItem");
                defaultName = arguments.getString("defaultName");
                adjustmentUnit = arguments.getInt("adjustmentUnit");
                baseExposureTime = arguments.getDouble("baseExposureTime");
            }

            if (burnDodgeItem != null) {
                viewModel.setBurnDodgeItem(burnDodgeItem);
            }

            if (defaultName != null && defaultName.length() > 0) {
                viewModel.setDefaultName(defaultName);
            }

            if (adjustmentUnit != ExposureAdjustment.UNIT_NONE) {
                viewModel.setAdjustmentMode(adjustmentUnit);
            }

            if (Util.isValidPositive(baseExposureTime)) {
                viewModel.setBaseExposureTime(baseExposureTime);
            }

            viewModel.setInitialized(true);
        }

        builder.setBackgroundInsetTop(0);
        builder.setBackgroundInsetBottom(0);
        final AlertDialog dialog = builder.create();

        editAreaNameLayout = view.findViewById(R.id.editAreaNameLayout);
        editAreaName = view.findViewById(R.id.editAreaName);
        editAreaName.setText(viewModel.getName().getValue());
        editAreaName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                updateAreaNameField(focused);
            }
        });
        editAreaName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    viewModel.setName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        updateAreaNameField(editAreaNameLayout.isFocused());

        int adjMode = LiveDataUtil.getIntValue(viewModel.getAdjustmentMode());

        editSecondsAdj = view.findViewById(R.id.editSecondsAdj);
        if (adjMode == ExposureAdjustment.UNIT_SECONDS) {
            double secondsValue = LiveDataUtil.getDoubleValue(viewModel.getSecondsValue());
            editSecondsAdj.setText(Util.isValidNonZero(secondsValue) ? Converter.secondsValueToString(secondsValue) : "");
        }
        editSecondsAdj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double value = Double.NaN;
                if (s != null && s.length() > 0) {
                    try {
                        value = Double.parseDouble(s.toString());
                    } catch (NumberFormatException ignored) { }
                }
                if (!Util.isValidNonZero(value)) { value = 0.0d; }
                viewModel.setSecondsValue(value);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        editPercentAdj = view.findViewById(R.id.editPercentAdj);
        if (adjMode == ExposureAdjustment.UNIT_PERCENT) {
            int percentValue = LiveDataUtil.getIntValue(viewModel.getPercentValue());
            editPercentAdj.setText(percentValue != 0 ? String.format(Locale.getDefault(), "%d", percentValue) : "");
        }
        editPercentAdj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                if (s != null && s.length() > 0) {
                    try {
                        value = Integer.parseInt(s.toString());
                    } catch (NumberFormatException ignored) { }
                }
                viewModel.setPercentValue(value);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        Button buttonIncCoarse = view.findViewById(R.id.buttonIncCoarse);
        buttonIncCoarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fraction adjustment = getStopsAdjustmentAmount(false, true);
                viewModel.adjustStopsValue(adjustment);
            }
        });

        Button buttonIncFine = view.findViewById(R.id.buttonIncFine);
        buttonIncFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fraction adjustment = getStopsAdjustmentAmount(true, true);
                viewModel.adjustStopsValue(adjustment);
            }
        });

        Button buttonDecCoarse = view.findViewById(R.id.buttonDecCoarse);
        buttonDecCoarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fraction adjustment = getStopsAdjustmentAmount(false, false);
                viewModel.adjustStopsValue(adjustment);
            }
        });

        Button buttonDecFine = view.findViewById(R.id.buttonDecFine);
        buttonDecFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fraction adjustment = getStopsAdjustmentAmount(true, false);
                viewModel.adjustStopsValue(adjustment);
            }
        });

        adjTypeToggleGroup = view.findViewById(R.id.adjTypeToggleGroup);

        MaterialButton buttonSeconds = adjTypeToggleGroup.findViewById(R.id.buttonSeconds);
        MaterialButton buttonPercent = adjTypeToggleGroup.findViewById(R.id.buttonPercent);
        MaterialButton buttonStops = adjTypeToggleGroup.findViewById(R.id.buttonStops);
        switch (LiveDataUtil.getIntValue(viewModel.getAdjustmentMode())) {
            case ExposureAdjustment.UNIT_SECONDS:
                buttonSeconds.setChecked(true);
                buttonPercent.setChecked(false);
                buttonStops.setChecked(false);
                break;
            case ExposureAdjustment.UNIT_PERCENT:
                buttonSeconds.setChecked(false);
                buttonPercent.setChecked(true);
                buttonStops.setChecked(false);
                break;
            case ExposureAdjustment.UNIT_STOPS:
            default:
                buttonSeconds.setChecked(false);
                buttonPercent.setChecked(false);
                buttonStops.setChecked(true);
                break;
        }

        adjTypeToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.buttonSeconds) {
                    viewModel.setAdjustmentMode(ExposureAdjustment.UNIT_SECONDS);
                    double secondsValue = LiveDataUtil.getDoubleValue(viewModel.getSecondsValue());
                    editSecondsAdj.setText(Util.isValidNonZero(secondsValue) ? Converter.secondsValueToString(secondsValue) : "");
                } else if (checkedId == R.id.buttonPercent) {
                    viewModel.setAdjustmentMode(ExposureAdjustment.UNIT_PERCENT);
                    int percentValue = LiveDataUtil.getIntValue(viewModel.getPercentValue());
                    editPercentAdj.setText(percentValue != 0 ? String.format(Locale.getDefault(), "%d", percentValue) : "");
                } else if (checkedId == R.id.buttonStops) {
                    viewModel.setAdjustmentMode(ExposureAdjustment.UNIT_STOPS);
                }
            }
        });

        viewModel.hasValue().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasValue) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(hasValue);
            }
        });

        return dialog;
    }

    private void updateAreaNameField(boolean focused) {
        Editable text = editAreaName.getText();
        boolean hasText = text != null && text.toString().length() > 0;

        if (focused) {
            editAreaNameLayout.setPlaceholderText(getDefaultAreaName());
            editAreaNameLayout.setHint(getString(R.string.label_burndodge_area_name));
        } else {
            editAreaNameLayout.setPlaceholderText(null);
            editAreaNameLayout.setHint(hasText ? getString(R.string.label_burndodge_area_name) : getDefaultAreaName());
        }
    }

    private String getDefaultAreaName() {
        String defaultName = viewModel.getDefaultName().getValue();
        if (defaultName != null) {
            return defaultName;
        } else {
            return "";
        }
    }

    private DialogInterface.OnClickListener buttonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            if (requestKey != null && requestKey.length() > 0) {
                Bundle result = new Bundle();
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    BurnDodgeItem item = viewModel.buildBurnDodgeItem();
                    result.putInt("action", ACTION_ACCEPT);
                    result.putParcelable("burnDodgeItem", item);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    result.putInt("action", ACTION_CANCEL);
                } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                    BurnDodgeItem item = viewModel.buildBurnDodgeItem();
                    result.putInt("action", ACTION_REMOVE);
                    result.putParcelable("burnDodgeItem", item);
                }
                getParentFragmentManager().setFragmentResult(requestKey, result);
            }
        }
    };

    private Fraction getStopsAdjustmentAmount(boolean fine, boolean increment) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String prefKey = fine ? "burndodge_stops_fine" : "burndodge_stops_coarse";
        String prefValue = sharedPreferences.getString(prefKey, null);
        Fraction amount = Util.preferenceValueToFraction(prefValue);

        if (increment) {
            return amount;
        } else {
            return amount.negate();
        }
    }
}
