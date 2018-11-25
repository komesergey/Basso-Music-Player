package com.basso.basso.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.basso.basso.R;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.VisualizerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment {
    private static final float VISUALIZER_HEIGHT_DIP = 50f;
    private Visualizer mVisualizer;
    private LinearLayout mLinearLayout;
    private LinearLayout mFooterLayout;
    private ListView mEqualizerListView;
    private View mEqualizerFooter;
    private View mEqualizerHeader;
    private VisualizerView mVisualizerView;
    private JSONArray customEqualizerPresets;
    private SharedPreferences mPreferences;
    private float balance = 1.0f;
    private SeekBar balanceSeekBar;
    private View rootView;
    private int progressStored;
    private boolean D = false;
    private String TAG = "Equalizer fragment";
    private SharedPreferences.OnSharedPreferenceChangeListener listenere = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (D) Log.d(TAG, "Update called ");
            MusicUtils.updatePref();
        }
    };
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
        }
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        this.rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_equalizer, container, false);
        refresh();
        return rootView;
    }

    public void refresh(){
        if(rootView != null) {
            if (D) Log.d(TAG,"Equalizer fragment updated");
            if(MusicUtils.getAudioSessionId() != -1) {
                setupVisualizerFxAndUI();
                setupEqualizerFxAndUI();
            } else{
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (D) Log.d(TAG,"Called from handler");
                                refresh();
                            }
                        });
                    }
                }, 100);
            }
        }
    }

    public void updatePresets(){
        try{
            mPreferences = getActivity().getSharedPreferences("Service", 0);
            String customPresetsString = mPreferences.getString("equalizer_presets", "no_presets");
            if (D) Log.d(TAG,"Updated in equalizer fragments");
            if(!customPresetsString.equals("no_presets")){
                if (D) Log.d(TAG,"No presets in equalizer");
                customEqualizerPresets =  new JSONArray(customPresetsString);
            }else{
                customEqualizerPresets = null;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    private void equalizeSound() {
        mFooterLayout = (LinearLayout)mEqualizerFooter.findViewById(R.id.equalizer_footer_layout);
        balance = mPreferences.getFloat("balance", 1.0f);
        balanceSeekBar = (SeekBar) mFooterLayout.findViewById(R.id.balance_seekbar);
        balanceSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.audio_player_seekbar));
        balanceSeekBar.setThumb(null);
        balanceSeekBar.setMax(1000);
        balanceSeekBar.setProgress((int)(500*balance));
        balanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressStored = progress;
                balance = progressStored/500f;
                if(balance <= 1.05f && balance >= 0.95f) {
                    balance = 1.0f;
                    balanceSeekBar.setProgress((int) (500 * balance));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    MusicUtils.setBalance(balance);
            }
        });
        Button mSavePresetButton = (Button)mEqualizerFooter.findViewById(R.id.save_equalizer_preset);
        Button mDeletePresetButton = (Button)mEqualizerFooter.findViewById(R.id.delete_equalizer_preset);
        final ArrayList<String> equalizerPresetNames = new ArrayList<String>();
        final ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.presets_spinner_item, R.id.spinner_item_textview,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(R.layout.presets_spinner_item);
        final Spinner equalizerPresetSpinner = (Spinner) mFooterLayout.findViewById(R.id.spinner);

        mSavePresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Context mContext = getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final AlertDialog alertDialog;
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
                View layout  = inflater.inflate(R.layout.adding_equalizer_preset, null);
                final EditText editTextPresetName = (EditText)layout.findViewById(R.id.edit_text_preset_name);
                builder.setView(layout);
                alertDialog = builder.create();

                Button cancelButton = (Button)layout.findViewById(R.id.cancel_preset_button);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(alertDialog.isShowing())
                            alertDialog.dismiss();
                    }
                });

                Button createButton = (Button)layout.findViewById(R.id.add_preset_button);
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String presetName = editTextPresetName.getText().toString();
                            if(TextUtils.isEmpty(presetName)){
                                Toast.makeText(mContext, "Yout must enter preset name", Toast.LENGTH_SHORT).show();
                            } else if(equalizerPresetNames.contains(presetName)) {
                                Toast.makeText(mContext, "Preset with that name already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                JSONObject preset = new JSONObject();
                                preset.put("name", presetName);
                                short numberFrequencyBands = (short)MusicUtils.getNumberOfBands();

                                final short lowerEqualizerBandLevel = (short)MusicUtils.getBandLevelRange(0);
                                for (short i = 0; i < numberFrequencyBands; i++) {
                                    short equalizerBandIndex = i;
                                    SeekBar seekBar = (SeekBar) mLinearLayout.findViewById(equalizerBandIndex);
                                    preset.put("" + i, seekBar.getProgress() + lowerEqualizerBandLevel);
                                }
                                if(customEqualizerPresets != null) {
                                    customEqualizerPresets.put(customEqualizerPresets.length(), preset);
                                } else {
                                    customEqualizerPresets = new JSONArray();
                                    customEqualizerPresets.put(customEqualizerPresets.length(), preset);
                                }
                                MusicUtils.savePreset(customEqualizerPresets.toString());
                                MusicUtils.updatePref();
                                equalizerPresetNames.add(presetName);
                                equalizerPresetSpinnerAdapter.notifyDataSetChanged();
                                if(alertDialog.isShowing())
                                    alertDialog.dismiss();
                                Toast.makeText(mContext, "Preset " +  presetName + " saved" , Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });

                alertDialog.show();
            }
        });

        mDeletePresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = equalizerPresetSpinner.getSelectedItemPosition();
                if (selectedPosition < MusicUtils.getNumberOfPresets()) {
                    Toast.makeText(getActivity(), "Can not delete system preset", Toast.LENGTH_SHORT).show();
                } else {
                    MusicUtils.deletePreset(selectedPosition);
                    updatePresets();
                    MusicUtils.updatePref();
                    equalizerPresetSpinner.setSelection(selectedPosition - 1);
                    equalizerPresetNames.remove(selectedPosition);
                    equalizerPresetSpinnerAdapter.notifyDataSetChanged();
                }
            }
        });

        for (short i = 0; i < MusicUtils.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(MusicUtils.getPresetName(i));
        }
        try{
            String customPresetsString = mPreferences.getString("equalizer_presets", "no_presets");
            if(!customPresetsString.equals("no_presets")){
                customEqualizerPresets =  new JSONArray(customPresetsString);
                for(int i = 0; i < customEqualizerPresets.length(); i++){
                    JSONObject preset = customEqualizerPresets.getJSONObject(i);
                    equalizerPresetNames.add(preset.getString("name"));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        int selection = mPreferences.getInt("active_preset", 0);
        if(selection >= 0 && selection < equalizerPresetNames.size()){
            equalizerPresetSpinner.setSelection(selection);
        }
        equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MusicUtils.usePreset((short) position);
                short numberFrequencyBands = (short)MusicUtils.getNumberOfBands();
                final short lowerEqualizerBandLevel = (short)MusicUtils.getBandLevelRange(0);
                for (short i = 0; i < numberFrequencyBands; i++) {
                    short equalizerBandIndex = i;
                    SeekBar seekBar = (SeekBar) mLinearLayout.findViewById(equalizerBandIndex);
                    seekBar.setProgress(MusicUtils.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupEqualizerFxAndUI() {
        mLinearLayout = (LinearLayout) mEqualizerHeader.findViewById(R.id.linearLayoutVisual);
        short numberFrequencyBands = (short)MusicUtils.getNumberOfBands();
        final short lowerEqualizerBandLevel = (short)MusicUtils.getBandLevelRange(0);
        final short upperEqualizerBandLevel = (short)MusicUtils.getBandLevelRange(1);

        for (short i = 0; i < numberFrequencyBands; i++) {
            final short equalizerBandIndex = i;

            TextView frequencyHeaderTextview = new TextView(getActivity());
            frequencyHeaderTextview.setTextColor(Color.WHITE);
            frequencyHeaderTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            frequencyHeaderTextview.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextview.setText((MusicUtils.getCenterFreq(equalizerBandIndex) / 1000) + " Hz");
            mLinearLayout.addView(frequencyHeaderTextview);
            LinearLayout seekBarRowLayout = new LinearLayout(getActivity());
            seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView lowerEqualizerBandLevelTextview = new TextView(getActivity());
            lowerEqualizerBandLevelTextview.setTextColor(Color.WHITE);
            lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB");

            TextView upperEqualizerBandLevelTextview = new TextView(getActivity());
            upperEqualizerBandLevelTextview.setTextColor(Color.WHITE);
            upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            upperEqualizerBandLevelTextview.setText((upperEqualizerBandLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            SeekBar seekBar = new SeekBar(getActivity());
            seekBar.setId(i);
            seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.audio_player_seekbar));
            seekBar.setThumb(null);
            seekBar.setPadding(0, getResources().getDimensionPixelSize(R.dimen.grid_item_padding_left), 0, getResources().getDimensionPixelSize(R.dimen.grid_item_padding_left));
            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
            seekBar.setProgress(MusicUtils.getBandLevel(equalizerBandIndex));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MusicUtils.setBandLeve(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            seekBarRowLayout.addView(lowerEqualizerBandLevelTextview);
            seekBarRowLayout.addView(seekBar);
            seekBarRowLayout.addView(upperEqualizerBandLevelTextview);
            mLinearLayout.addView(seekBarRowLayout);
            equalizeSound();
        }
    }

    private void setupVisualizerFxAndUI() {
        try {
            if (D) Log.d(TAG,"Setup visualizer " + rootView);
            mEqualizerListView = (ListView) rootView.findViewById(R.id.equalizer_listview);
            if (mEqualizerFooter != null) mEqualizerListView.removeFooterView(mEqualizerFooter);
            if (mEqualizerHeader != null) mEqualizerListView.removeHeaderView(mEqualizerHeader);
            mEqualizerFooter = getActivity().getLayoutInflater().inflate(R.layout.equalizer_listview_footer, null);
            mEqualizerHeader = getActivity().getLayoutInflater().inflate(R.layout.equalizer_listview_header, null);
            mEqualizerListView.addHeaderView(mEqualizerHeader);
            mEqualizerListView.addFooterView(mEqualizerFooter);
            mEqualizerListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>()));
            mPreferences = getActivity().getSharedPreferences("Service", 0);
            mPreferences.registerOnSharedPreferenceChangeListener(listenere);
            mLinearLayout = (LinearLayout) mEqualizerHeader.findViewById(R.id.linearLayoutVisual);
            ToggleButton equalizerSwitch = (ToggleButton) mEqualizerHeader.findViewById(R.id.toggle_equalizer);
            equalizerSwitch.setChecked(MusicUtils.isEqualizerEnabled());
            equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                    MusicUtils.setEqualizerEnabled(isChecked);
                }
            });
            mVisualizerView = new VisualizerView(getActivity());
            mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
            mLinearLayout.addView(mVisualizerView);
            if (D) Log.d(TAG,"Session id: " + MusicUtils.getAudioSessionId());
            mVisualizer = new Visualizer(MusicUtils.getAudioSessionId());
            mVisualizer.setEnabled(false);

            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                  int samplingRate) {
                    mVisualizerView.updateVisualizer(bytes);
                }

                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                }
            }, Visualizer.getMaxCaptureRate(), true, false);
            mVisualizer.setEnabled(true);
        }catch (RuntimeException e){ e.printStackTrace();};
    }
}
