package com.sunflower.catchtherainbow.Views;

/**
 * Created by Alexandr on 05.02.2017.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;

/**
 * Created by filip on 8/21/2015.
 */
public class FragTabAudioFiles extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.frag_tab_audio_files,container,false);
    }
}
