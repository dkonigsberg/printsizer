package org.logicprobe.printsizer.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.databinding.DialogChooseEnlargerBinding;
import org.logicprobe.printsizer.databinding.FragmentHomeBinding;
import org.logicprobe.printsizer.model.EnlargerProfile;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private EditText editSmallerHeight;
    private EditText editSmallerTime;
    private EditText editLargerHeight;
    private TextView enlargerName;
    private TextView textViewSmallerHeightUnits;
    private TextView textViewLargerHeightUnits;
    private boolean height_as_cm;
    private boolean ignoreHeightChange;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        final View root = binding.getRoot();

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setHomeViewModel(homeViewModel);

        View enlargerView = root.findViewById(R.id.layoutEnlarger);
        enlargerName = enlargerView.findViewById(R.id.name);
        enlargerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enlargerViewClicked();
            }
        });

        editSmallerHeight = root.findViewById(R.id.editSmallerHeight);
        editSmallerTime = root.findViewById(R.id.editSmallerTime);
        editLargerHeight = root.findViewById(R.id.editLargerHeight);
        textViewSmallerHeightUnits = root.findViewById(R.id.textViewSmallerHeightUnits);
        textViewLargerHeightUnits = root.findViewById(R.id.textViewLargerHeightUnits);

        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        editSmallerHeight.setText(modelHeightValueToString(homeViewModel.getSmallerPrintHeight()));
        editSmallerTime.setText(modelTimeValueToString(homeViewModel.getSmallerPrintExposureTime()));
        editLargerHeight.setText(modelHeightValueToString(homeViewModel.getLargerPrintHeight()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
                    editSmallerHeight.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editSmallerHeight.setError(null);
                }
            }
        });

        homeViewModel.getLargerPrintHeightError().observe(getViewLifecycleOwner(), new Observer<EnlargerHeightErrorEvent>() {
            @Override
            public void onChanged(EnlargerHeightErrorEvent enlargerHeightErrorEvent) {
                if (enlargerHeightErrorEvent != EnlargerHeightErrorEvent.NONE) {
                    editLargerHeight.setError(getString(enlargerHeightErrorEvent.getErrorResource()));
                } else {
                    editLargerHeight.setError(null);
                }
            }
        });

        homeViewModel.getEnlargerProfileValid().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean valid) {
                if (valid) {
                    enlargerName.setError(null);
                } else {
                    enlargerName.setError(getString(R.string.error_enlarger_profile_invalid));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key != null || key.equals("enlarger_height_units")) {
            updateHeightUnits(sharedPreferences);
        }
    }

    private void updateHeightUnits(SharedPreferences sharedPreferences) {
        String prefValue = sharedPreferences.getString("enlarger_height_units", null);
        if (prefValue == null || prefValue.length() == 0 || prefValue.equals("millimeters")) {
            height_as_cm = false;

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            textViewSmallerHeightUnits.setText(R.string.unit_suffix_mm);

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
            textViewLargerHeightUnits.setText(R.string.unit_suffix_mm);
        } else {
            height_as_cm = true;

            editSmallerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            textViewSmallerHeightUnits.setText(R.string.unit_suffix_cm);

            editLargerHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            textViewLargerHeightUnits.setText(R.string.unit_suffix_cm);
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
                Navigation.findNavController(getView()).navigate(R.id.action_add_enlarger);
            }
        });
        dialog.show(getParentFragmentManager(), "choose_enlarger_alert");
    }

    private String modelHeightValueToString(LiveData<Double> liveValue) {
        if (liveValue == null || liveValue.getValue() == null) {
            return "";
        }

        double value = liveValue.getValue().doubleValue();
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

        double value = liveValue.getValue().doubleValue();
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
        if (charSequence == null || charSequence.toString() == null) {
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
        if (charSequence == null || charSequence.toString() == null) {
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

            viewModel.getSelectionList().observe(getActivity(),
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
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            builder.setView(view).setTitle(R.string.dialog_choose_enlarger_profile);
            return builder.create();
        }
    }
}