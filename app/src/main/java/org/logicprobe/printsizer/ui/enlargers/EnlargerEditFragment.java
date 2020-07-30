package org.logicprobe.printsizer.ui.enlargers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.model.PrintMath;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EnlargerEditFragment extends Fragment {
    private static final String TAG = EnlargerEditFragment.class.getSimpleName();
    private int profileId = 0;
    private EditText editName;
    private EditText editDescription;
    private EditText editLensFocalLength;
    private EditText editHeightOffset;
    private EditText editSmallerHeight;
    private EditText editSmallerTime;
    private EditText editLargerHeight;
    private EditText editLargerTime;

    private TextInputLayout editNameLayout;
    private TextInputLayout editDescriptionLayout;
    private TextInputLayout editLensFocalLengthLayout;
    private TextInputLayout editHeightOffsetLayout;
    private TextInputLayout editSmallerHeightLayout;
    private TextInputLayout editSmallerTimeLayout;
    private TextInputLayout editLargerHeightLayout;
    private TextInputLayout editLargerTimeLayout;

    private boolean height_as_cm;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_enlarger_edit, container, false);

        editName = root.findViewById(R.id.editName);
        editDescription = root.findViewById(R.id.editDescription);
        editLensFocalLength = root.findViewById(R.id.editLensFocalLength);
        editHeightOffset = root.findViewById(R.id.editHeightOffset);
        editSmallerHeight = root.findViewById(R.id.editSmallerHeight);
        editSmallerTime = root.findViewById(R.id.editSmallerTime);
        editLargerHeight = root.findViewById(R.id.editLargerHeight);
        editLargerTime = root.findViewById(R.id.editLargerTime);

        editNameLayout = root.findViewById(R.id.editNameLayout);
        editDescriptionLayout = root.findViewById(R.id.editDescriptionLayout);
        editLensFocalLengthLayout = root.findViewById(R.id.editLensFocalLengthLayout);
        editHeightOffsetLayout = root.findViewById(R.id.editHeightOffsetLayout);
        editSmallerHeightLayout = root.findViewById(R.id.editSmallerHeightLayout);
        editSmallerTimeLayout = root.findViewById(R.id.editSmallerTimeLayout);
        editLargerHeightLayout = root.findViewById(R.id.editLargerHeightLayout);
        editLargerTimeLayout = root.findViewById(R.id.editLargerTimeLayout);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditAccept();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefValue = sharedPreferences.getString("enlarger_height_units", null);
        if (prefValue == null || prefValue.length() == 0 || prefValue.equals("millimeters")) {
            height_as_cm = false;

            editHeightOffset.setInputType(InputType.TYPE_CLASS_NUMBER);
            editHeightOffsetLayout.setSuffixText(getString(R.string.unit_suffix_mm));

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editSmallerHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editLargerHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));

        } else {
            height_as_cm = true;

            editHeightOffset.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editHeightOffsetLayout.setSuffixText(getString(R.string.unit_suffix_cm));

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editSmallerHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editLargerHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle arguments = getArguments();

        if (arguments != null) {
            int profileId = arguments.getInt("id");
            if (profileId > 0) {
                App app = (App)requireActivity().getApplication();
                LiveData<EnlargerProfileEntity> liveEntity = app.getRepository().loadEnlargerProfile(profileId);

                liveEntity.observe(getViewLifecycleOwner(), new Observer<EnlargerProfileEntity>() {
                    @Override
                    public void onChanged(EnlargerProfileEntity enlargerProfileEntity) {
                        enableTextHintAnimations(false);
                        populateFromEnlargerProfile(enlargerProfileEntity);
                        enableTextHintAnimations(true);
                    }
                });
            }
        }

        final TextWatcher validator = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateInputFields(editable);
            }
        };

        editName.addTextChangedListener(validator);
        editDescription.addTextChangedListener(validator);
        editLensFocalLength.addTextChangedListener(validator);
        editHeightOffset.addTextChangedListener(validator);
        editSmallerHeight.addTextChangedListener(validator);
        editSmallerTime.addTextChangedListener(validator);
        editLargerHeight.addTextChangedListener(validator);
        editLargerTime.addTextChangedListener(validator);
    }

    private void enableTextHintAnimations(boolean enabled) {
        editNameLayout.setHintAnimationEnabled(enabled);
        editDescriptionLayout.setHintAnimationEnabled(enabled);
        editLensFocalLengthLayout.setHintAnimationEnabled(enabled);
        editHeightOffsetLayout.setHintAnimationEnabled(enabled);
        editSmallerHeightLayout.setHintAnimationEnabled(enabled);
        editSmallerTimeLayout.setHintAnimationEnabled(enabled);
        editLargerHeightLayout.setHintAnimationEnabled(enabled);
        editLargerTimeLayout.setHintAnimationEnabled(enabled);
    }

    private void handleEditAccept() {
        Util.hideKeyboardFrom(getContext(), getView());
        if (validateInputFields(null)) {
            EnlargerProfileEntity enlargerProfile = buildEnlargerProfile();

            App app = (App)requireActivity().getApplication();
            app.getRepository().insert(enlargerProfile);

            Navigation.findNavController(getView()).popBackStack();
        }
    }

    private boolean validateInputFields(Editable editable) {
        boolean result = true;

        // Validate non-numeric fields that need values, which is just the name
        if (editable == null || editable == editName.getText()) {
            if (!hasText(editName)) {
                editNameLayout.setError(getString(R.string.error_enlarger_missing_name));
                result = false;
            }
        }

        // Parse all the numeric values here, whether or not we're directly validating their
        // fields, because we may still use them for cross-field validation.
        double focalLength = parseDoubleFromText(editLensFocalLength);
        double heightOffset = parseHeightDoubleFromText(editHeightOffset);
        double smallerHeight = parseHeightDoubleFromText(editSmallerHeight);
        double smallerTime = parseDoubleFromText(editSmallerTime);
        double largerHeight = parseHeightDoubleFromText(editLargerHeight);
        double largerTime = parseDoubleFromText(editLargerTime);

        // Validate the lens focal length
        if (editable == null || editable == editLensFocalLength.getText()) {
            if (!hasText(editLensFocalLength)) {
                editLensFocalLengthLayout.setError(getString(R.string.error_enlarger_missing_lens_focal_length));
                result = false;
            } else if (Double.isNaN(focalLength) || focalLength <= 0 || focalLength > 10000) {
                editLensFocalLengthLayout.setError(getString(R.string.error_enlarger_lens_focal_length_invalid));
                result = false;
            }
        }

        // Validate the optional height offset
        if (editable == null || editable == editHeightOffset.getText()) {
            if (hasText(editHeightOffset) && (Double.isNaN(heightOffset) || heightOffset < -10000 || heightOffset > 10000)) {
                editHeightOffsetLayout.setError(getString(R.string.error_enlarger_height_offset_invalid));
                result = false;
            }
        }
        // Assume unparsable height offset is zero for the rest of the validation code
        if (Double.isNaN(heightOffset)) {
            heightOffset = 0.0d;
        }

        // Validate the smaller test height
        if (editable == null || editable == editSmallerHeight.getText()) {
            if (!hasText(editSmallerHeight)) {
                editSmallerHeightLayout.setError(getString(R.string.error_enlarger_height_needed));
                result = false;
            } else if (Double.isNaN(smallerHeight) || (smallerHeight + heightOffset) <= 0 || (smallerHeight + heightOffset) > 10000) {
                editSmallerHeightLayout.setError(getString(R.string.error_enlarger_height_invalid));
                result = false;
            }
        }

        // Validate the smaller test time
        if (editable == null || editable == editSmallerTime.getText()) {
            if (!hasText(editSmallerTime)) {
                editSmallerTimeLayout.setError(getString(R.string.error_exposure_time_needed));
                result = false;
            } else if (Double.isNaN(smallerTime) || smallerTime <= 0 || smallerTime > 7200) {
                editSmallerTimeLayout.setError(getString(R.string.error_exposure_time_invalid));
                result = false;
            }
        }

        // Validate the larger test height
        if (editable == null || editable == editLargerHeight.getText()) {
            if (editLargerHeight.getText().toString().length() == 0) {
                editLargerHeightLayout.setError(getString(R.string.error_enlarger_height_needed));
                result = false;
            } else if (Double.isNaN(largerHeight) || (largerHeight + heightOffset) <= 0 || (largerHeight + heightOffset) > 10000) {
                editLargerHeightLayout.setError(getString(R.string.error_enlarger_height_invalid));
                result = false;
            }
        }

        // Validate the larger test time
        if (editable == null || editable == editLargerTime.getText()) {
            if (!hasText(editLargerTime)) {
                editLargerTimeLayout.setError(getString(R.string.error_exposure_time_needed));
                result = false;
            } else if (Double.isNaN(largerTime) || largerTime <= 0 || largerTime > 7200) {
                editLargerTimeLayout.setError(getString(R.string.error_exposure_time_invalid));
                result = false;
            }
        }

        // If all the basic validation checks passed, then do some of the more complex cross-field
        // validation checks. Keeping these specific to the whole-screen validation case for
        // the sake of simplicity.
        if (result && editable == null) {
            if (largerTime <= smallerTime) {
                editLargerTimeLayout.setError(getString(R.string.error_exposure_time_invalid));
                result = false;
            }

            if (largerHeight <= smallerHeight) {
                editLargerHeightLayout.setError(getString(R.string.error_enlarger_height_invalid));
                result = false;
            } else {
                if (smallerHeight + heightOffset < PrintMath.computeMinimumHeight(focalLength)) {
                    editSmallerHeightLayout.setError(getString(R.string.error_enlarger_height_too_low_for_focal_length));
                    result = false;
                }
                if (largerHeight + heightOffset < PrintMath.computeMinimumHeight(focalLength)) {
                    editLargerHeightLayout.setError(getString(R.string.error_enlarger_height_too_low_for_focal_length));
                    result = false;
                }
            }
        }

        return result;
    }

    private static boolean hasText(EditText editText) {
        return editText != null
                && editText.getText() != null
                && editText.getText().length() > 0
                && editText.getText().toString().trim().length() > 0;
    }

    private double parseHeightDoubleFromText(EditText editText) {
        double value;
        if (editText == null || editText.getText() == null) {
            value = Double.NaN;
        } else {
            try {
                value = Double.parseDouble(editText.getText().toString());
                if (height_as_cm) {
                    value = value * 10.0d;
                }
            } catch (NumberFormatException e) {
                value = Double.NaN;
            }
        }
        return value;
    }

    private double parseDoubleFromText(EditText editText) {
        double value;
        if (editText == null || editText.getText() == null) {
            value = Double.NaN;
        } else {
            try {
                value = Double.parseDouble(editText.getText().toString());
            } catch (NumberFormatException e) {
                value = Double.NaN;
            }
        }
        return value;
    }

    private void populateFromEnlargerProfile(EnlargerProfileEntity enlargerProfile) {
        NavDestination dest = Navigation.findNavController(getView()).getCurrentDestination();
        if (dest.getId() == R.id.nav_enlarger_add) {
            Log.d(TAG, "Enlarger " + enlargerProfile.getId() + " copied for adding");
            profileId = 0;
            editName.setText(getString(R.string.enlarger_profile_copy_name_template, enlargerProfile.getName()));
        } else if (dest.getId() == R.id.nav_enlarger_edit) {
            Log.d(TAG, "Enlarger " + enlargerProfile.getId() + " opened for editing");
            profileId = enlargerProfile.getId();
            editName.setText(enlargerProfile.getName());
        } else {
            Log.e(TAG, "Unknown navigation destination: " + dest);
            return;
        }

        editDescription.setText(enlargerProfile.getDescription());
        
        if (enlargerProfile.getLensFocalLength() > 0) {
            editLensFocalLength.setText(convertDoubleToText(enlargerProfile.getLensFocalLength()));
        }
        if (isDoubleNonZero(enlargerProfile.getHeightMeasurementOffset())) {
            editHeightOffset.setText(convertHeightDoubleToText(enlargerProfile.getHeightMeasurementOffset()));
        }
        
        editSmallerHeight.setText(convertHeightDoubleToText(enlargerProfile.getSmallerTestDistance()));
        editSmallerTime.setText(convertDoubleToText(enlargerProfile.getSmallerTestTime()));

        editLargerHeight.setText(convertHeightDoubleToText(enlargerProfile.getLargerTestDistance()));
        editLargerTime.setText(convertDoubleToText(enlargerProfile.getLargerTestTime()));
    }

    private EnlargerProfileEntity buildEnlargerProfile() {
        EnlargerProfileEntity enlargerProfile = new EnlargerProfileEntity();

        enlargerProfile.setId(profileId);
        enlargerProfile.setName(safeGetEditTextString(editName));
        enlargerProfile.setDescription(safeGetEditTextString(editDescription));

        enlargerProfile.setLensFocalLength(safeGetEditTextDouble(editLensFocalLength, 0, 10000));
        enlargerProfile.setHeightMeasurementOffset(safeGetEditTextHeightDouble(editHeightOffset, -10000, 10000));

        enlargerProfile.setSmallerTestDistance(safeGetEditTextHeightDouble(editSmallerHeight, 0, 10000));
        enlargerProfile.setSmallerTestTime(safeGetEditTextDouble(editSmallerTime, 0, 7200));

        enlargerProfile.setLargerTestDistance(safeGetEditTextHeightDouble(editLargerHeight, 0, 10000));
        enlargerProfile.setLargerTestTime(safeGetEditTextDouble(editLargerTime, 0, 7200));

        return enlargerProfile;
    }

    private static String safeGetEditTextString(EditText editText) {
        if (editText.getText() != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }

    private double safeGetEditTextHeightDouble(EditText editText, double min, double max) {
        double value = 0.0d;
        if (editText.getText() != null) {
            try {
                value = Double.parseDouble(editText.getText().toString());
                if (height_as_cm) {
                    value = value * 10.0d;
                }
            } catch (NumberFormatException e) {
                value = Double.NaN;
            }
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = 0.0d;
        } else if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    private double safeGetEditTextDouble(EditText editText, double min, double max) {
        double value = 0.0d;
        if (editText.getText() != null) {
            try {
                value = Double.parseDouble(editText.getText().toString());
            } catch (NumberFormatException e) {
                value = Double.NaN;
            }
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = 0.0d;
        } else if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    private String convertHeightDoubleToText(double value) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setGroupingUsed(false);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
        }
        if (height_as_cm) {
            value = value / 10.0d;
        }
        return f.format(value);
    }

    private String convertDoubleToText(double value) {
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setGroupingUsed(false);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
        }
        return f.format(value);
    }

    private boolean isDoubleNonZero(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && (value < 0.0d || value > 0.0d);
    }
}
