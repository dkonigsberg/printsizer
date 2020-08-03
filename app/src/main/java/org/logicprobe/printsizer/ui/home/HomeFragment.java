package org.logicprobe.printsizer.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogChooseEnlargerBinding;
import org.logicprobe.printsizer.databinding.FragmentHomeBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;
import org.logicprobe.printsizer.ui.enlargers.EnlargerProfileClickCallback;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ADD_ENLARGER_REQUEST_KEY = HomeFragment.class.getSimpleName() + "_ADD_ENLARGER";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    private EditText editSmallerHeight;
    private EditText editSmallerTime;
    private EditText editLargerHeight;

    private TextInputLayout editSmallerHeightLayout;
    private TextInputLayout editLargerHeightLayout;

    private boolean height_as_cm;
    private boolean ignoreHeightChange;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(ADD_ENLARGER_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int enlargerProfileId = result.getInt("id", 0);
                Log.d(TAG, "Enlarger profile inserted: " + enlargerProfileId);
                handleEnlargerProfileInserted(enlargerProfileId);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        final View root = binding.getRoot();

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setHomeViewModel(homeViewModel);

        binding.setEnlargerProfileClickCallback(new EnlargerProfileClickCallback() {
            @Override
            public void onClick(EnlargerProfile enlargerProfile) {
                enlargerViewClicked();
            }
        });

        editSmallerHeight = root.findViewById(R.id.editSmallerHeight);
        editSmallerTime = root.findViewById(R.id.editSmallerTime);
        editLargerHeight = root.findViewById(R.id.editLargerHeight);
        editSmallerHeightLayout = root.findViewById(R.id.editSmallerHeightLayout);
        editLargerHeightLayout = root.findViewById(R.id.editLargerHeightLayout);

        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        editSmallerHeight.setText(modelHeightValueToString(homeViewModel.getSmallerPrintHeight()));
        editSmallerTime.setText(modelTimeValueToString(homeViewModel.getSmallerPrintExposureTime()));
        editLargerHeight.setText(modelHeightValueToString(homeViewModel.getLargerPrintHeight()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        updateHeightUnits(sharedPreferences);

        editSmallerHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!ignoreHeightChange) {
                    homeViewModel.setSmallerPrintHeight(charSequenceToModelHeightDouble(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editSmallerTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                homeViewModel.setSmallerPrintExposureTime(charSequenceToModelTimeDouble(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        editLargerHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!ignoreHeightChange) {
                    homeViewModel.setLargerPrintHeight(charSequenceToModelHeightDouble(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        homeViewModel.getSmallerPrintHeightError().observe(getViewLifecycleOwner(), new Observer<EnlargerHeightErrorEvent>() {
            @Override
            public void onChanged(EnlargerHeightErrorEvent enlargerHeightErrorEvent) {
                if (enlargerHeightErrorEvent != EnlargerHeightErrorEvent.NONE) {
                    editSmallerHeightLayout.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editSmallerHeightLayout.setError(null);
                }
            }
        });

        homeViewModel.getLargerPrintHeightError().observe(getViewLifecycleOwner(), new Observer<EnlargerHeightErrorEvent>() {
            @Override
            public void onChanged(EnlargerHeightErrorEvent enlargerHeightErrorEvent) {
                if (enlargerHeightErrorEvent != EnlargerHeightErrorEvent.NONE) {
                    editLargerHeightLayout.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editLargerHeightLayout.setError(null);
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
        if (key != null && key.equals("enlarger_height_units")) {
            updateHeightUnits(sharedPreferences);
        }
    }

    private void updateHeightUnits(SharedPreferences sharedPreferences) {
        String prefValue = sharedPreferences.getString("enlarger_height_units", null);
        if (prefValue == null || prefValue.length() == 0 || prefValue.equals("millimeters")) {
            height_as_cm = false;

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editSmallerHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            editLargerHeightLayout.setSuffixText(getString(R.string.unit_suffix_mm));
        } else {
            height_as_cm = true;

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editSmallerHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editLargerHeightLayout.setSuffixText(getString(R.string.unit_suffix_cm));
        }

        ignoreHeightChange = true;
        editSmallerHeight.setText(modelHeightValueToString(homeViewModel.getSmallerPrintHeight()));
        editLargerHeight.setText(modelHeightValueToString(homeViewModel.getLargerPrintHeight()));
        ignoreHeightChange = false;
    }

    private void enlargerViewClicked() {
        ChooseEnlargerDialogFragment dialog = new ChooseEnlargerDialogFragment(new ChooseEnlargerClickCallback() {
            @Override
            public void onClickProfile(EnlargerProfile enlargerProfile) {
                homeViewModel.setEnlargerProfile(enlargerProfile);
            }

            @Override
            public void onClickAction(int actionId) {
                Bundle bundle = new Bundle();
                bundle.putString("requestKey", ADD_ENLARGER_REQUEST_KEY);
                Navigation.findNavController(requireView()).navigate(R.id.action_add_enlarger, bundle);
            }
        });
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

    public static class ChooseEnlargerDialogFragment extends DialogFragment {
        private ChooseEnlargerDialogViewModel viewModel;
        private DialogChooseEnlargerBinding binding;
        private ChooseEnlargerDialogAdapter adapter;
        private ChooseEnlargerClickCallback clickCallback;

        public ChooseEnlargerDialogFragment(ChooseEnlargerClickCallback clickCallback) {
            this.clickCallback = clickCallback;
        }

        private ChooseEnlargerClickCallback adapterClickCallback = new ChooseEnlargerClickCallback() {
            @Override
            public void onClickProfile(EnlargerProfile enlargerProfile) {
                if (clickCallback != null) {
                    clickCallback.onClickProfile(enlargerProfile);
                }
                dismiss();
            }

            @Override
            public void onClickAction(int actionId) {
                if (clickCallback != null) {
                    clickCallback.onClickAction(actionId);
                }
                dismiss();
            }
        };

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            binding = DataBindingUtil.inflate(inflater, R.layout.dialog_choose_enlarger, null, false);

            adapter = new ChooseEnlargerDialogAdapter(adapterClickCallback);
            binding.enlargerProfileList.setAdapter(adapter);

            viewModel = new ViewModelProvider(this).get(ChooseEnlargerDialogViewModel.class);
            binding.setLifecycleOwner(getActivity());
            binding.setDialogViewModel(viewModel);

            viewModel.getSelectionList().observe(requireActivity(),
                    new Observer<List<ChooseEnlargerElement>>() {
                        @Override
                        public void onChanged(List<ChooseEnlargerElement> chooseEnlargerElements) {
                            if (chooseEnlargerElements != null) {
                                adapter.setSelectionList(chooseEnlargerElements);
                            }
                            binding.executePendingBindings();
                        }
                    });

            View view = binding.getRoot();

            RecyclerView recyclerView = view.findViewById(R.id.enlarger_profile_list);
            recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

            builder.setView(view).setTitle(R.string.dialog_choose_enlarger_profile);
            return builder.create();
        }
    }
}