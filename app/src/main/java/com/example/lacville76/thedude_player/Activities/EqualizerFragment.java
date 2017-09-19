package com.example.lacville76.thedude_player.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.example.lacville76.thedude_player.R;

import java.util.ArrayList;


public class EqualizerFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Intent playerIntent;



    private MediaPlayerService player;
    private OnFragmentInteractionListener mListener;
    SeekBar seekBar90Hz;
    SeekBar seekBar230Hz;
    SeekBar seekBar910Hz;
    SeekBar seekBar3600Hz;
    SeekBar seekBar14000Hz;
    short numberFrequencyBands;
    short lowerEqualizerBandLevel;
    short upperEqualizerBandLevel ;
    int bandLevelDiff;
    ArrayList<String> EqualizerPresetNames;
    ArrayAdapter<String> equalizerPresetSpinnerAdapter;
    SharedPreferences.Editor editor;
    Spinner equalizerPresetSpinner;
    public EqualizerFragment() {

    }

    public static EqualizerFragment newInstance(String param1, String param2) {
        EqualizerFragment fragment = new EqualizerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        playerIntent= new Intent(getContext(), MediaPlayerService.class);
        getActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_equalizer, container, false);
        seekBar90Hz = (SeekBar)rootView.findViewById(R.id.seekBar90Hz);
        seekBar230Hz = (SeekBar)rootView. findViewById(R.id.seekBar230Hz);
        seekBar910Hz = (SeekBar)rootView. findViewById(R.id.seekBar910Hz);
        seekBar3600Hz = (SeekBar)rootView. findViewById(R.id.seekBar3600Hz);
        seekBar14000Hz = (SeekBar)rootView. findViewById(R.id.seekBar14000Hz);
        EqualizerPresetNames = new ArrayList<>();
        equalizerPresetSpinnerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,EqualizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerPresetSpinner = (Spinner)rootView.findViewById(R.id.presetSpinner);

        seekBar90Hz.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int new_level = lowerEqualizerBandLevel + (upperEqualizerBandLevel - lowerEqualizerBandLevel) * progress / 100;
                player.getEqualizer().setBandLevel((short)0, (short)(new_level));
                editor.putInt("90HzEq",progress);
                editor.apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar230Hz.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int new_level = lowerEqualizerBandLevel + (upperEqualizerBandLevel - lowerEqualizerBandLevel) * progress / 100;
                player.getEqualizer().setBandLevel((short)1, (short)(new_level));
                editor.putInt("230HzEq",progress);
                editor.apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar910Hz.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int new_level = lowerEqualizerBandLevel + (upperEqualizerBandLevel - lowerEqualizerBandLevel) * progress / 100;
                player.getEqualizer().setBandLevel((short)2, (short)(new_level));
                editor.putInt("910HzEq",progress);
                editor.apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar3600Hz.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int new_level = lowerEqualizerBandLevel + (upperEqualizerBandLevel - lowerEqualizerBandLevel) * progress / 100;
                player.getEqualizer().setBandLevel((short)3, (short)(new_level));
                editor.putInt("3600HzEq",progress);
                editor.apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar14000Hz.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int new_level = lowerEqualizerBandLevel + (upperEqualizerBandLevel - lowerEqualizerBandLevel) * progress / 100;
                player.getEqualizer().setBandLevel((short)4, (short)(new_level));
                editor.putInt("14000HzEq",progress);
                editor.apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("selectedPreset",position);
                editor.apply();

                if(position>0)
                {
                    player.getEqualizer().usePreset((short) position);
                    seekBar90Hz.setProgress(player.getEqualizer().getBandLevel((short) 0));
                    seekBar230Hz.setProgress(player.getEqualizer().getBandLevel((short) 1));
                    seekBar910Hz.setProgress(player.getEqualizer().getBandLevel((short) 2));
                    seekBar3600Hz.setProgress(player.getEqualizer().getBandLevel((short) 3));
                    seekBar14000Hz.setProgress(player.getEqualizer().getBandLevel((short) 4));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();

            numberFrequencyBands = player.getEqualizer().getNumberOfBands();
            lowerEqualizerBandLevel = player.getEqualizer().getBandLevelRange()[0];
            upperEqualizerBandLevel = player.getEqualizer().getBandLevelRange()[1];
            bandLevelDiff = upperEqualizerBandLevel-lowerEqualizerBandLevel;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            seekBar90Hz.setProgress( preferences.getInt("90HzEq", 0));
            seekBar230Hz.setProgress( preferences.getInt("230HzEq", 0));
            seekBar910Hz.setProgress( preferences.getInt("910HzEq", 0));
            seekBar3600Hz.setProgress( preferences.getInt("3600HzEq", 0));
            seekBar14000Hz.setProgress( preferences.getInt("14000HzEq", 0));
            EqualizerPresetNames.add("User Preset");

            for (short i = 0; i < player.getEqualizer().getNumberOfPresets(); i++)
            {
                EqualizerPresetNames.add(player.getEqualizer().getPresetName(i));
            }
            equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
            equalizerPresetSpinner.setSelection(preferences.getInt("selectedPreset",0));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
