package org.logicprobe.printsizer.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;

import org.logicprobe.printsizer.LiveDataUtil;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.FragmentHomeBinding;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.EnlargerProfile;
import org.logicprobe.printsizer.model.ExposureAdjustment;
import org.logicprobe.printsizer.model.PaperGrade;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.enlargers.EnlargerProfileClickCallback;
import org.logicprobe.printsizer.ui.papers.PaperProfileClickCallback;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String SELECT_PAPER_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_SELECT_PAPER";
    private static final String INITIAL_PAPER_SETTINGS_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_INITIAL_PAPER_SETTINGS";
    private static final String BASE_PAPER_SETTINGS_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_BASE_PAPER_SETTINGS";
    private static final String TARGET_PAPER_SETTINGS_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_TARGET_PAPER_SETTINGS";
    private static final String CHOOSE_ENLARGER_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_CHOOSE_ENLARGER";
    private static final String ADD_ENLARGER_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_ADD_ENLARGER";
    private static final String ADD_BURN_DODGE_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_ADD_BURN_DODGE";
    private static final String EDIT_BURN_DODGE_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_EDIT_BURN_DODGE";

    private static final double[] FULL_STOPS = { -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0 };
    private static final double[] HALF_STOPS = {
            -3.0, -2.5, -2.0, -1.5, -1.0, -0.5,
            0.0,
            0.5, 1.0, 1.5, 2.0, 2.5, 3.0 };
    private static final double[] THIRD_STOPS = {
            -3.0, -2.667, -2.333, -2.0, -1.667, -1.333, -1.0, -0.667, 0.333,
            0.0,
            0.333, 0.667, 1.0, 1.333, 1.667, 2.0, 2.333, 2.667, 3.0 };
    private HashMap<String, Double> exposureOffsetLabelToValue;
    private HashMap<Double, String> exposureOffsetValueToLabel;

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    private BurnDodgeAdapter baseBurnDodgeAdapter;
    private BurnDodgeTargetAdapter targetBurnDodgeAdapter;

    private EditText editBaseHeight;
    private EditText editBaseTime;
    private EditText editTargetHeight;

    private TextInputLayout editBaseHeightLayout;
    private TextInputLayout editTargetHeightLayout;

    private AutoCompleteTextView editTargetExposureAdjustment;

    private Button buttonAddPaperProfile;
    private Button buttonAddBurnDodge;

    private boolean height_as_cm;
    private boolean ignoreHeightChange;
    private boolean ignoreExposureOffsetChange;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getParentFragmentManager();

        fragmentManager.setFragmentResultListener(SELECT_PAPER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                PaperProfileEntity paperProfile = result.getParcelable("paperProfile");
                if (paperProfile != null) {
                    Log.d(TAG, "Paper profile selected: [" + paperProfile.getId() + "] " + paperProfile.getName());
                    handlePaperProfileSelected(paperProfile);
                }
            }
        });

        fragmentManager.setFragmentResultListener(INITIAL_PAPER_SETTINGS_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int action = result.getInt("action");
                if (action == PaperSettingsDialogFragment.ACTION_ACCEPT) {
                    Log.d(TAG, "Initial paper settings accepted");
                    int profileId = result.getInt("profileId");
                    int gradeId = result.getInt("gradeId");
                    handleInitialPaperProfileChanged(profileId, gradeId);
                } else if (action == PaperSettingsDialogFragment.ACTION_CANCEL) {
                    Log.d(TAG, "Initial paper settings cancelled");
                }
            }
        });

        fragmentManager.setFragmentResultListener(BASE_PAPER_SETTINGS_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int action = result.getInt("action");
                if (action == PaperSettingsDialogFragment.ACTION_ACCEPT) {
                    Log.d(TAG, "Small paper settings changed");
                    int profileId = result.getInt("profileId");
                    int gradeId = result.getInt("gradeId");
                    handleSmallPaperSettingChanged(profileId, gradeId);
                } else if (action == PaperSettingsDialogFragment.ACTION_CANCEL) {
                    Log.d(TAG, "Small paper settings cancelled");
                } if (action == PaperSettingsDialogFragment.ACTION_REMOVE) {
                    Log.d(TAG, "Remove paper profiles");
                    handleRemovePaperProfiles();
                }
            }
        });

        fragmentManager.setFragmentResultListener(TARGET_PAPER_SETTINGS_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int action = result.getInt("action");
                if (action == PaperSettingsDialogFragment.ACTION_ACCEPT) {
                    Log.d(TAG, "Large paper settings changed");
                    int profileId = result.getInt("profileId");
                    int gradeId = result.getInt("gradeId");
                    handleLargePaperSettingChanged(profileId, gradeId);
                } else if (action == PaperSettingsDialogFragment.ACTION_CANCEL) {
                    Log.d(TAG, "Large paper settings cancelled");
                } if (action == PaperSettingsDialogFragment.ACTION_REMOVE) {
                    Log.d(TAG, "Remove paper profiles");
                    handleRemovePaperProfiles();
                }
            }
        });

        fragmentManager.setFragmentResultListener(CHOOSE_ENLARGER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int action = result.getInt("action");
                if (action == ChooseEnlargerDialogFragment.ACTION_SELECTED_ENLARGER) {
                    int profileId = result.getInt("profileId");
                    Log.d(TAG, "Enlarger profile selected: " + profileId);
                    homeViewModel.setEnlargerProfile(profileId);
                } else if (action == ChooseEnlargerDialogFragment.ACTION_ADD_ENLARGER) {
                    Bundle bundle = new Bundle();
                    bundle.putString("requestKey", ADD_ENLARGER_REQUEST_KEY);
                    Navigation.findNavController(requireView()).navigate(R.id.action_add_enlarger, bundle);
                }
            }
        });

        fragmentManager.setFragmentResultListener(ADD_ENLARGER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int enlargerProfileId = result.getInt("id", 0);
                Log.d(TAG, "Enlarger profile inserted: " + enlargerProfileId);
                handleEnlargerProfileInserted(enlargerProfileId);
            }
        });

        fragmentManager.setFragmentResultListener(ADD_BURN_DODGE_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                BurnDodgeItem burnDodgeItem = result.getParcelable("burnDodgeItem");
                if (burnDodgeItem != null) {
                    Log.d(TAG, "Burn/dodge item added");
                    homeViewModel.addBaseBurnDodgeItem(burnDodgeItem);
                }
            }
        });

        fragmentManager.setFragmentResultListener(EDIT_BURN_DODGE_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int action = result.getInt("action");
                BurnDodgeItem item = result.getParcelable("burnDodgeItem");
                if (action == BurnDodgeDialogFragment.ACTION_ACCEPT) {
                    if (item != null) {
                        if (homeViewModel.updateBaseBurnDodgeItem(item)) {
                            Log.d(TAG, "Burn/dodge item updated: " + item.getIndex());
                        } else {
                            Log.d(TAG, "Burn/dodge item could not be updated");
                        }
                    }
                } else if (action == BurnDodgeDialogFragment.ACTION_REMOVE) {
                    if (item != null) {
                        Log.d(TAG, "Remove burn/dodge item: " + item.getIndex() + " [id=" + item.getItemId() + "]");
                        homeViewModel.removeBaseBurnDodgeItem(item);
                    }
                }
            }
        });

        exposureOffsetLabelToValue = new HashMap<>();
        exposureOffsetValueToLabel = new HashMap<>();
        populateExposureOffsetMaps(R.array.exposure_offset_full_stops, FULL_STOPS);
        populateExposureOffsetMaps(R.array.exposure_offset_half_stops, HALF_STOPS);
        populateExposureOffsetMaps(R.array.exposure_offset_third_stops, THIRD_STOPS);

    }

    private void populateExposureOffsetMaps(int labelResourceId, double[] valueArray) {
        String[] stringArray = getResources().getStringArray(labelResourceId);
        if (stringArray.length != valueArray.length) {
            throw new IllegalArgumentException("Bad exposure offset resource");
        }
        for (int i = 0; i < stringArray.length; i++) {
            exposureOffsetLabelToValue.put(stringArray[i], valueArray[i]);
            exposureOffsetValueToLabel.put(valueArray[i], stringArray[i]);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        final View root = binding.getRoot();

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setHomeViewModel(homeViewModel);

        final BurnDodgeClickCallback burnDodgeBaseClickCallback = new BurnDodgeClickCallback() {
            @Override
            public void onEditItem(BurnDodgeItem item) {
                Log.d(TAG, "Edit burn/dodge item: " + item.getIndex() + " [id=" + item.getItemId() + "]");

                double baseExposureTime = LiveDataUtil.getDoubleValue(homeViewModel.getBasePrintExposureTime());
                BurnDodgeDialogFragment dialog = new BurnDodgeDialogFragment.Builder()
                        .setRequestKey(EDIT_BURN_DODGE_REQUEST_KEY)
                        .setBurnDodgeItem(item)
                        .setBaseExposureTime(baseExposureTime)
                        .setRemoveButton(true)
                        .create();
                dialog.show(getParentFragmentManager(), "burn_dodge_alert");
            }

            @Override
            public void onRemoveItem(BurnDodgeItem item) {
                Log.d(TAG, "Remove burn/dodge item: " + item.getIndex() + " [id=" + item.getItemId() + "]");
                homeViewModel.removeBaseBurnDodgeItem(item);
            }
        };

        baseBurnDodgeAdapter = new BurnDodgeAdapter(burnDodgeBaseClickCallback);
        binding.baseBurnDodgeList.setAdapter(baseBurnDodgeAdapter);

        targetBurnDodgeAdapter = new BurnDodgeTargetAdapter();
        binding.targetBurnDodgeList.setAdapter(targetBurnDodgeAdapter);

        homeViewModel.getBaseBurnDodgeList().observe(requireActivity(),
                new Observer<List<BurnDodgeItem>>() {
                    @Override
                    public void onChanged(List<BurnDodgeItem> burnDodgeItems) {
                        if (burnDodgeItems != null) {
                            baseBurnDodgeAdapter.setBurnDodgeList(burnDodgeItems);
                        }
                    }
                });

        homeViewModel.getTargetBurnDodgeList().observe(requireActivity(),
                new Observer<List<BurnDodgeTargetItem>>() {
                    @Override
                    public void onChanged(List<BurnDodgeTargetItem> burnDodgeTargetItems) {
                        if (burnDodgeTargetItems != null) {
                            targetBurnDodgeAdapter.setBurnDodgeTargetList(burnDodgeTargetItems);
                        }
                    }
                });

        binding.setBasePaperProfileClickCallback(new PaperProfileClickCallback() {
            @Override
            public void onClick(PaperProfile paperProfile) {
                if (paperProfile instanceof PaperProfileEntity) {
                    basePaperProfileClicked((PaperProfileEntity)paperProfile);
                }
            }
        });

        binding.setTargetPaperProfileClickCallback(new PaperProfileClickCallback() {
            @Override
            public void onClick(PaperProfile paperProfile) {
                if (paperProfile instanceof PaperProfileEntity) {
                    targetPaperProfileClicked((PaperProfileEntity)paperProfile);
                }
            }
        });

        binding.setEnlargerProfileClickCallback(new EnlargerProfileClickCallback() {
            @Override
            public void onClick(EnlargerProfile enlargerProfile) {
                enlargerViewClicked();
            }
        });

        editBaseHeight = root.findViewById(R.id.editBaseHeight);
        editBaseTime = root.findViewById(R.id.editBaseTime);
        editTargetHeight = root.findViewById(R.id.editTargetHeight);
        editBaseHeightLayout = root.findViewById(R.id.editBaseHeightLayout);
        editTargetHeightLayout = root.findViewById(R.id.editTargetHeightLayout);
        editTargetExposureAdjustment = root.findViewById(R.id.editTargetExposureAdjustment);

        buttonAddPaperProfile = root.findViewById(R.id.buttonAddPaperProfile);
        buttonAddBurnDodge = root.findViewById(R.id.buttonAddBurnDodge);

        buttonAddPaperProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChoosePaperDialogFragment dialog = ChoosePaperDialogFragment.create(SELECT_PAPER_REQUEST_KEY);
                dialog.show(getParentFragmentManager(), "choose_paper_alert");
            }
        });

        buttonAddBurnDodge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Add burn/dodge area");

                int adjustmentUnit;
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String prefValue = sharedPreferences.getString("burndodge_type", null);
                if ("time".equals(prefValue)) {
                    adjustmentUnit = ExposureAdjustment.UNIT_SECONDS;
                } else if ("percent".equals(prefValue)) {
                    adjustmentUnit = ExposureAdjustment.UNIT_PERCENT;
                } else {
                    adjustmentUnit = ExposureAdjustment.UNIT_STOPS;
                }

                List<BurnDodgeItem> itemList = homeViewModel.getBaseBurnDodgeList().getValue();
                int nextIndex = itemList != null ? itemList.size() : 0;
                String defaultName = BurnDodgeItem.getDefaultName(getResources(), nextIndex);

                double baseExposureTime = LiveDataUtil.getDoubleValue(homeViewModel.getBasePrintExposureTime());

                BurnDodgeDialogFragment dialog = new BurnDodgeDialogFragment.Builder()
                        .setRequestKey(ADD_BURN_DODGE_REQUEST_KEY)
                        .setDefaultName(defaultName)
                        .setAdjustmentUnit(adjustmentUnit)
                        .setBaseExposureTime(baseExposureTime)
                        .create();
                dialog.show(getParentFragmentManager(), "burn_dodge_alert");
            }
        });

        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        editBaseHeight.setText(modelHeightValueToString(homeViewModel.getBasePrintHeight()));
        editBaseTime.setText(modelTimeValueToString(homeViewModel.getBasePrintExposureTime()));
        editTargetHeight.setText(modelHeightValueToString(homeViewModel.getTargetPrintHeight()));
        editTargetExposureAdjustment.setText(modelExposureOffsetValueToString(homeViewModel.getTargetPrintExposureOffset()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        updateHeightUnits(sharedPreferences);
        updateExposureIncrements(sharedPreferences);

        editBaseHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!ignoreHeightChange) {
                    homeViewModel.setBasePrintHeight(charSequenceToModelHeightDouble(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editBaseTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                homeViewModel.setBasePrintExposureTime(charSequenceToModelTimeDouble(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editTargetHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!ignoreHeightChange) {
                    homeViewModel.setTargetPrintHeight(charSequenceToModelHeightDouble(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editTargetExposureAdjustment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!ignoreExposureOffsetChange) {
                    homeViewModel.setTargetPrintExposureOffset(charSequenceToModelExposureOffset(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        homeViewModel.getBasePrintHeightError().observe(getViewLifecycleOwner(), new Observer<EnlargerHeightErrorEvent>() {
            @Override
            public void onChanged(EnlargerHeightErrorEvent enlargerHeightErrorEvent) {
                if (enlargerHeightErrorEvent != EnlargerHeightErrorEvent.NONE) {
                    editBaseHeightLayout.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editBaseHeightLayout.setError(null);
                }
            }
        });

        homeViewModel.getTargetPrintHeightError().observe(getViewLifecycleOwner(), new Observer<EnlargerHeightErrorEvent>() {
            @Override
            public void onChanged(EnlargerHeightErrorEvent enlargerHeightErrorEvent) {
                if (enlargerHeightErrorEvent != EnlargerHeightErrorEvent.NONE) {
                    editTargetHeightLayout.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editTargetHeightLayout.setError(null);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("enlarger_height_units".equals(key)) {
            updateHeightUnits(sharedPreferences);
        } else if ("exposure_increments".equals(key)) {
            updateExposureIncrements(sharedPreferences);
        }
    }

    private void handlePaperProfileSelected(PaperProfileEntity paperProfile) {
        PaperSettingsDialogFragment paperDialog = new PaperSettingsDialogFragment.Builder()
                .setRequestKey(INITIAL_PAPER_SETTINGS_REQUEST_KEY)
                .setTitle(R.string.dialog_choose_paper_profile)
                .setPaperProfile(paperProfile)
                .create();
        paperDialog.show(getParentFragmentManager(), "initial_paper_settings_alert");
    }

    private void handleInitialPaperProfileChanged(int profileId, int gradeId) {
        homeViewModel.setBasePaperProfileId(profileId);
        homeViewModel.setBasePaperGradeId(gradeId);
        homeViewModel.setTargetPaperProfileId(profileId);
        homeViewModel.setTargetPaperGradeId(gradeId);
        homeViewModel.setPaperProfilesAdded(true);
    }

    private void handleRemovePaperProfiles() {
        homeViewModel.setPaperProfilesAdded(false);
    }

    private void basePaperProfileClicked(PaperProfileEntity paperProfile) {
        if (paperProfile == null) { return; }

        PaperSettingsDialogFragment paperDialog = new PaperSettingsDialogFragment.Builder()
                .setRequestKey(BASE_PAPER_SETTINGS_REQUEST_KEY)
                .setTitle(R.string.dialog_base_print_paper_profile)
                .setPaperProfile(paperProfile)
                .setPaperGradeId(LiveDataUtil.getIntValue(homeViewModel.getBasePaperGradeId()))
                .setRemoveButton(true)
                .create();
        paperDialog.show(getParentFragmentManager(), "base_paper_settings_alert");
    }

    private void handleSmallPaperSettingChanged(int profileId, int gradeId) {
        homeViewModel.setBasePaperProfileId(profileId);
        homeViewModel.setBasePaperGradeId(gradeId);
    }

    private void targetPaperProfileClicked(PaperProfileEntity paperProfile) {
        if (paperProfile == null) { return; }

        // Get the "reference" ISO(R) value from the base print paper profile.
        // This will be used to recommend a starting contrast grade if a different
        // paper is selected for the target print.
        int baseIsoR = 0;
        PaperProfile baseProfile = homeViewModel.getBasePaperProfile().getValue();
        if (baseProfile != null) {
            int baseGradeId = LiveDataUtil.getIntValue(homeViewModel.getBasePaperGradeId());
            PaperGrade baseGrade = baseProfile.getGrade(baseGradeId);
            if (baseGrade != null && baseGrade.getIsoP() > 0 && baseGrade.getIsoR() > 0) {
                baseIsoR = baseGrade.getIsoR();
            }
        }

        PaperSettingsDialogFragment paperDialog = new PaperSettingsDialogFragment.Builder()
                .setRequestKey(TARGET_PAPER_SETTINGS_REQUEST_KEY)
                .setTitle(R.string.dialog_target_print_paper_profile)
                .setPaperProfile(paperProfile)
                .setPaperGradeId(LiveDataUtil.getIntValue(homeViewModel.getTargetPaperGradeId()))
                .setReferencePaperIsoR(baseIsoR)
                .setRemoveButton(false)
                .create();
        paperDialog.show(getParentFragmentManager(), "target_paper_settings_alert");
    }

    private void handleLargePaperSettingChanged(int profileId, int gradeId) {
        homeViewModel.setTargetPaperProfileId(profileId);
        homeViewModel.setTargetPaperGradeId(gradeId);
    }

    private void updateHeightUnits(SharedPreferences sharedPreferences) {
        String prefValue = sharedPreferences.getString("enlarger_height_units", null);
        if (prefValue == null || prefValue.length() == 0 || prefValue.equals("millimeters")) {
            height_as_cm = false;

            editBaseHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editBaseHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));

            editTargetHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editTargetHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));
        } else {
            height_as_cm = true;

            editBaseHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editBaseHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));

            editTargetHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editTargetHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));
        }

        ignoreHeightChange = true;
        editBaseHeight.setText(modelHeightValueToString(homeViewModel.getBasePrintHeight()));
        editTargetHeight.setText(modelHeightValueToString(homeViewModel.getTargetPrintHeight()));
        ignoreHeightChange = false;
    }

    private void updateExposureIncrements(SharedPreferences sharedPreferences) {
        String prefValue = sharedPreferences.getString("exposure_increments", null);

        String[] stringArray;
        double[] valueArray;

        if ("half_stops".equals(prefValue)) {
            stringArray = getResources().getStringArray(R.array.exposure_offset_half_stops);
            valueArray = HALF_STOPS;
        } else if ("third_stops".equals(prefValue)) {
            stringArray = getResources().getStringArray(R.array.exposure_offset_third_stops);
            valueArray = THIRD_STOPS;
        } else { // "full_stops"
            stringArray = getResources().getStringArray(R.array.exposure_offset_full_stops);
            valueArray = FULL_STOPS;
        }

        if (stringArray.length != valueArray.length) {
            throw new IllegalArgumentException("Stop increment labels and values do not match");
        }

        ignoreExposureOffsetChange = true;
        ArrayAdapter<CharSequence> exposureAdapter = new ArrayAdapter<CharSequence>(
                requireContext(),
                R.layout.exposure_offset_popup_item,
                stringArray);
        editTargetExposureAdjustment.setAdapter(exposureAdapter);
        ignoreExposureOffsetChange = false;
    }

    private void enlargerViewClicked() {
        ChooseEnlargerDialogFragment dialog = ChooseEnlargerDialogFragment.create(CHOOSE_ENLARGER_REQUEST_KEY);
        dialog.show(getParentFragmentManager(), "choose_enlarger_alert");
    }

    private void handleEnlargerProfileInserted(int enlargerProfileId) {
        // An enlarger profile was just added via user action originating from the picker
        // dialog. The user likely expects it to now become the selected profile.
        Log.d(TAG, "Setting added enlarger profile " + enlargerProfileId + " as selected");
        homeViewModel.setEnlargerProfile(enlargerProfileId);
    }

    private String modelHeightValueToString(LiveData<Double> liveValue) {
        if (liveValue == null || liveValue.getValue() == null) {
            return "";
        }

        double value = liveValue.getValue();
        if (Double.isNaN(value) || Double.isInfinite(value) || value == 0.0d) {
            return "";
        }

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

    private String modelTimeValueToString(LiveData<Double> liveValue) {
        if (liveValue == null || liveValue.getValue() == null) {
            return "";
        }

        double value = liveValue.getValue();
        if (Double.isNaN(value) || Double.isInfinite(value) || value == 0.0d) {
            return "";
        }

        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setGroupingUsed(false);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
        }
        return f.format(value);
    }

    private String modelExposureOffsetValueToString(LiveData<Double> liveValue) {
        double exposureOffset;
        if (liveValue == null || liveValue.getValue() == null) {
            exposureOffset = 0.0d;
        } else {
            exposureOffset = liveValue.getValue();
        }

        String label = exposureOffsetValueToLabel.get(exposureOffset);
        if (label == null || label.length() == 0) {
            label = "0";
        }

        return label;
    }

    private double charSequenceToModelHeightDouble(CharSequence charSequence) {
        double result;
        if (charSequence == null) {
            result = Double.NaN;
        } else {
            try {
                result = Double.parseDouble(charSequence.toString());
                if (height_as_cm) {
                    result = result * 10.0d;
                }
            } catch (NumberFormatException e) {
                result = Double.NaN;
            }
        }
        return result;
    }

    private double charSequenceToModelTimeDouble(CharSequence charSequence) {
        double result;
        if (charSequence == null) {
            result = Double.NaN;
        } else {
            try {
                result = Double.parseDouble(charSequence.toString());
            } catch (NumberFormatException e) {
                result = Double.NaN;
            }
        }
        return result;
    }


    private double charSequenceToModelExposureOffset(CharSequence charSequence) {
        if (charSequence == null || charSequence.toString().length() == 0) {
            return 0.0d;
        }

        String label = charSequence.toString();
        Double value = exposureOffsetLabelToValue.get(label);

        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0d;
        } else {
            return value;
        }
    }
}