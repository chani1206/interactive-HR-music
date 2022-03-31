package com.Nulody.NupianoV2;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import static com.Nulody.NupianoV2.support_source.FileOP.m_nBMP;
import com.Nulody.NupianoV2.support_source.MusicNote4d;

/**
 * Created by Chani on 2022/03/28.
 */
public class Fragment2 extends Fragment {
    Switch switch1;
    TextView tv_tempo;
    int m_newtempo = 60;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment2, container, false);
        switch1 = view.findViewById(R.id.switch1);
        tv_tempo = view.findViewById(R.id.TV_tempo);
        final SeekBar tempoBar  = view.findViewById(R.id.seekBar);

        switch1.setOnCheckedChangeListener(switch1Lis);

        tempoBar.setProgress(60);
        tv_tempo.setText("Tempo");
        tempoBar.setOnSeekBarChangeListener(tempoBarLis);

        return view;
    }

    public CompoundButton.OnCheckedChangeListener switch1Lis = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b){
                if (m_newtempo > 0){
                    MusicNote4d.SetBPM(m_newtempo);
                }
            }
            else{
                if (m_nBMP > 0) {
                    MusicNote4d.SetBPM(m_nBMP);
                }
            }
        }
    };

    public SeekBar.OnSeekBarChangeListener tempoBarLis = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            tv_tempo.setText("Tempo: " + i);
            m_newtempo=i;
            if (m_newtempo > 0) {
                MusicNote4d.SetBPM(m_newtempo);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    }




