package com.sunflower.catchtherainbow.Views.Helpful;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.Collections;

public class ExportSongFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAMEPROJECT = "";

    private ExportSongFragment.OnFragmentExportSongListener mListener;

    // TODO: Rename and change types of parameters
    private String nameProject;

    public ExportSongFragment()
    {
        // Required empty public constructor
    }

    public static ExportSongFragment newInstance(String nameProject)
    {
        ExportSongFragment fragment = new ExportSongFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAMEPROJECT, nameProject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            nameProject = getArguments().getString(ARG_NAMEPROJECT);
        }
    }

    EditText etName, etAlbum, etYear;
    private NDSpinner ndSpinFormat;
    private ArrayAdapter<String> spinFormatAdapter;
    Button bOk, bCancel;
    private String format = "MP3";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.export_audio_fragment, container, false);
        etName = (EditText)resView.findViewById(R.id.edNameSong);
        if(!nameProject.equals("")) etName.setText(nameProject);
        etAlbum = (EditText)resView.findViewById(R.id.edNameAlbum);
        bOk = (Button)resView.findViewById(R.id.bOk);
        bCancel = (Button)resView.findViewById(R.id.bCancel);
        ndSpinFormat = (NDSpinner)resView.findViewById(R.id.spinnerFormatOfFile);
        etYear = (EditText)resView.findViewById(R.id.etYear);

        bOk.setOnClickListener(this);
        bCancel.setOnClickListener(this);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        String[] spinner_array = { "WAV", "AIFF", "OPUS", "OGG" };

        ArrayList<String> items = new ArrayList<String>();
        Collections.addAll(items, spinner_array);

        ndSpinFormat = (NDSpinner) resView.findViewById(R.id.spinnerFormatOfFile);
        spinFormatAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        spinFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ndSpinFormat.setAdapter(spinFormatAdapter);

        ndSpinFormat.setOnItemSelectedListener(this);

        return  resView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof ExportSongFragment.OnFragmentExportSongListener)
        {
            mListener = (ExportSongFragment.OnFragmentExportSongListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnEffectsHostListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.bOk:
            {
                if(!etName.getText().toString().equals(""))
                {
                    if (mListener != null) mListener.onOk(etName.getText().toString(), etAlbum.getText().toString(), etYear.getText().toString(), ndSpinFormat.getSelectedItem().toString());
                    dismiss();// Закрытие текущего фрагмента
                }
                else Toast.makeText(getActivity(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.bCancel:
            {
                // Закрытие текущего фрагмента
                dismiss();
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l)
    {
        ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorForeground));
        ((TextView) parent.getChildAt(0)).setTextSize(18);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView){}

    public interface OnFragmentExportSongListener
    {
        void onOk(String name, String album, String year, String format);
    }
}
