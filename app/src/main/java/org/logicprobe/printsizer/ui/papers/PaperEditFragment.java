package org.logicprobe.printsizer.ui.papers;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.db.entity.PaperGradeEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

public class PaperEditFragment extends Fragment {
    private static final String TAG = PaperEditFragment.class.getSimpleName();
    private String requestKey;
    private int profileId = 0;

    private ViewGroup mainContentLayout;

    private EditText editName;
    private EditText editDescription;
    private EditText[] editGradeISOP;
    private EditText[] editGradeISOR;

    private TextInputLayout editNameLayout;
    private TextInputLayout editDescriptionLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_paper_edit, container, false);

        mainContentLayout = root.findViewById(R.id.mainContentLayout);

        editName = root.findViewById(R.id.editName);
        editDescription = root.findViewById(R.id.editDescription);

        editNameLayout = root.findViewById(R.id.editNameLayout);
        editDescriptionLayout = root.findViewById(R.id.editDescriptionLayout);

        editGradeISOP = new EditText[8];
        editGradeISOP[0] = root.findViewById(R.id.edit_isop_grade00);
        editGradeISOP[1] = root.findViewById(R.id.edit_isop_grade0);
        editGradeISOP[2] = root.findViewById(R.id.edit_isop_grade1);
        editGradeISOP[3] = root.findViewById(R.id.edit_isop_grade2);
        editGradeISOP[4] = root.findViewById(R.id.edit_isop_grade3);
        editGradeISOP[5] = root.findViewById(R.id.edit_isop_grade4);
        editGradeISOP[6] = root.findViewById(R.id.edit_isop_grade5);
        editGradeISOP[7] = root.findViewById(R.id.edit_isop_none);

        editGradeISOR = new EditText[8];
        editGradeISOR[0] = root.findViewById(R.id.edit_isor_grade00);
        editGradeISOR[1] = root.findViewById(R.id.edit_isor_grade0);
        editGradeISOR[2] = root.findViewById(R.id.edit_isor_grade1);
        editGradeISOR[3] = root.findViewById(R.id.edit_isor_grade2);
        editGradeISOR[4] = root.findViewById(R.id.edit_isor_grade3);
        editGradeISOR[5] = root.findViewById(R.id.edit_isor_grade4);
        editGradeISOR[6] = root.findViewById(R.id.edit_isor_grade5);
        editGradeISOR[7] = root.findViewById(R.id.edit_isor_none);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditAccept();
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle arguments = getArguments();

        if (arguments != null) {
            int profileId = arguments.getInt("id");
            if (profileId > 0) {
                App app = (App) requireActivity().getApplication();
                LiveData<PaperProfileEntity> liveEntity = app.getRepository().loadPaperProfile(profileId);

                liveEntity.observe(getViewLifecycleOwner(), new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        enableTextHintAnimations(false);
                        populateFromPaperProfile(paperProfileEntity);
                        enableTextHintAnimations(true);
                    }
                });
            }
            requestKey = arguments.getString("requestKey");
        }

        //TODO add change listeners
    }

    private void enableTextHintAnimations(boolean enabled) {
        editNameLayout.setHintAnimationEnabled(enabled);
        editDescriptionLayout.setHintAnimationEnabled(enabled);
    }

    private void clearInputErrors(Editable editable) {
        if (editable == null || editable == editName.getText()) {
            editNameLayout.setError(null);
        }
        if (editable == null || editable == editDescription.getText()) {
            editDescriptionLayout.setError(null);
        }
    }

    private void handleEditAccept() {
        Util.hideKeyboardFrom(requireContext(), requireView());
        if (validateInputFields(null)) {
            PaperProfileEntity paperProfile = buildPaperProfile();

            App app = (App) requireActivity().getApplication();
            LiveData<Integer> liveProfileId = app.getRepository().insert(paperProfile);

            liveProfileId.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer profileId) {
                    Log.d(TAG, "Saved added paper profile: " + profileId);
                    if (requestKey != null && requestKey.length() > 0) {
                        Bundle result = new Bundle();
                        result.putInt("id", profileId);
                        getParentFragmentManager().setFragmentResult(requestKey, result);
                    }
                    Navigation.findNavController(requireView()).popBackStack();
                }
            });
        }
    }

    private boolean validateInputFields(Editable editable) {
        boolean result = true;

        clearInputErrors(editable);

        // Validate non-numeric fields that need values, which is just the name
        if (editable == null || editable == editName.getText()) {
            if (!Util.hasText(editName)) {
                editNameLayout.setError(getString(R.string.error_paper_missing_name));
                result = false;
            }
        }

        // Validation checks that only run on accept
        if (result && editable == null) {
            boolean hasIsoP = false;
            for (int i = 0; i < editGradeISOP.length; i++) {
                int isoP = safeGetEditTextIso(editGradeISOP[i]);
                int isoR = safeGetEditTextIso(editGradeISOR[i]);
                if (isoR > 0 && isoP == 0) {
                    editGradeISOP[i].requestFocus();
                    result = false;
                    break;
                } else if (isoP > 0) {
                    hasIsoP = true;
                }
            }
            if (!hasIsoP) {
                // Default to requesting Grade 2, because its a common default
                // and not as far down the screen as Unfiltered.
                editGradeISOP[3].requestFocus();
                result = false;
            }
        }

        return result;
    }

    private void populateFromPaperProfile(PaperProfileEntity paperProfile) {
        NavDestination dest = Navigation.findNavController(requireView()).getCurrentDestination();
        if (dest == null) {
            Log.e(TAG, "Null navigation destination");
            return;
        }

        if (dest.getId() == R.id.nav_paper_add) {
            Log.d(TAG, "Paper " + paperProfile.getId() + " copied for adding");
            profileId = 0;
            editName.setText(getString(R.string.paper_profile_copy_name_template, paperProfile.getName()));
        } else if (dest.getId() == R.id.nav_paper_edit) {
            Log.d(TAG, "Paper " + paperProfile.getId() + " opened for editing");
            profileId = paperProfile.getId();
            editName.setText(paperProfile.getName());
        } else {
            Log.e(TAG, "Unknown navigation destination: " + dest);
            return;
        }

        editDescription.setText(paperProfile.getDescription());

        populateFromPaperGrade(paperProfile.getGrade00(), editGradeISOP[0], editGradeISOR[0]);
        populateFromPaperGrade(paperProfile.getGrade0(), editGradeISOP[1], editGradeISOR[1]);
        populateFromPaperGrade(paperProfile.getGrade1(), editGradeISOP[2], editGradeISOR[2]);
        populateFromPaperGrade(paperProfile.getGrade2(), editGradeISOP[3], editGradeISOR[3]);
        populateFromPaperGrade(paperProfile.getGrade3(), editGradeISOP[4], editGradeISOR[4]);
        populateFromPaperGrade(paperProfile.getGrade4(), editGradeISOP[5], editGradeISOR[5]);
        populateFromPaperGrade(paperProfile.getGrade5(), editGradeISOP[6], editGradeISOR[6]);
        populateFromPaperGrade(paperProfile.getGradeNone(), editGradeISOP[7], editGradeISOR[7]);
    }

    @SuppressLint("SetTextI18n")
    private void populateFromPaperGrade(PaperGradeEntity gradeEntity, EditText editTextIsoP, EditText editTextIsoR) {
        if (gradeEntity != null) {
            if (gradeEntity.getIsoP() > 0) {
                editTextIsoP.setText(Integer.toString(gradeEntity.getIsoP()));
            }
            if (gradeEntity.getIsoR() > 0) {
                editTextIsoR.setText(Integer.toString(gradeEntity.getIsoR()));
            }
        }
    }

    private PaperProfileEntity buildPaperProfile() {
        PaperProfileEntity paperProfile = new PaperProfileEntity();

        paperProfile.setId(profileId);
        paperProfile.setName(Util.safeGetEditTextString(editName));
        paperProfile.setDescription(Util.safeGetEditTextString(editDescription));

        paperProfile.setGrade00(buildPaperGrade(editGradeISOP[0], editGradeISOR[0]));
        paperProfile.setGrade0(buildPaperGrade(editGradeISOP[1], editGradeISOR[1]));
        paperProfile.setGrade1(buildPaperGrade(editGradeISOP[2], editGradeISOR[2]));
        paperProfile.setGrade2(buildPaperGrade(editGradeISOP[3], editGradeISOR[3]));
        paperProfile.setGrade3(buildPaperGrade(editGradeISOP[4], editGradeISOR[4]));
        paperProfile.setGrade4(buildPaperGrade(editGradeISOP[5], editGradeISOR[5]));
        paperProfile.setGrade5(buildPaperGrade(editGradeISOP[6], editGradeISOR[6]));
        paperProfile.setGradeNone(buildPaperGrade(editGradeISOP[7], editGradeISOR[7]));

        return paperProfile;
    }

    private PaperGradeEntity buildPaperGrade(EditText editTextIsoP, EditText editTextIsoR) {
        return new PaperGradeEntity(safeGetEditTextIso(editTextIsoP), safeGetEditTextIso(editTextIsoR));
    }

    private int safeGetEditTextIso(EditText editText) {
        int value = 0;
        if (editText.getText() != null) {
            try {
                value = Integer.parseInt(editText.getText().toString());
            } catch (NumberFormatException ignored) { }
        }
        if (value < 0) { value = 0; }
        return value;
    }
}
