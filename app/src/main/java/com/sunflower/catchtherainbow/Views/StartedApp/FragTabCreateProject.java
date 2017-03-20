package com.sunflower.catchtherainbow.Views.StartedApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.ProjectActivity;
import com.sunflower.catchtherainbow.R;

import java.io.File;

public class FragTabCreateProject extends Fragment implements View.OnClickListener
{
    private ProjectStartActivity projectStartActivity;

    public FragTabCreateProject()
    {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragTabCreateProject newInstance(String param1, String param2)
    {
        FragTabCreateProject fragment = new FragTabCreateProject();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    TextView tv_nameProject;
    Button b_Ok;
    TextView tvNavigation;
    ImageView iv_strelochka;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.frag_tab_create_project, container, false);
        tvNavigation = (TextView)resView.findViewById(R.id.tv_navigation_swipe_open);
        iv_strelochka = (ImageView)resView.findViewById(R.id.iv_strelochka);

        tv_nameProject = (TextView)resView.findViewById(R.id.edNameProject);
        b_Ok = (Button)resView.findViewById(R.id.buttonOk);
        b_Ok.setOnClickListener(this);
        return resView;
    }

    public void onResume()
    {
        super.onResume();
        //To check the availability of projects
        try
        {
            if(new File(Helper.getPathOfProject()).listFiles().length == 0)
            {
                if(tvNavigation!=null) tvNavigation.setVisibility(View.GONE);
                if(iv_strelochka!=null) iv_strelochka.setVisibility(View.GONE);
            }
            else
            {
                if(tvNavigation!=null) tvNavigation.setVisibility(View.VISIBLE);
                if(iv_strelochka!=null) iv_strelochka.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception ex) {}
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonOk:
            {
                File file = new File(Helper.getPathOfProject() + "//" + tv_nameProject.getText().toString());
                if(tv_nameProject.getText().toString().equals("")) Toast.makeText(this.getActivity(), R.string.error_enter_name_project, Toast.LENGTH_SHORT).show();
                else if(file.exists())
                {
                    //A project with this name already exists, open it?
                    AlertDialog diaBox = AskOption(getContext());
                    diaBox.show();
                }
                else
                {
                    Intent intent = new Intent(FragTabCreateProject.this.getActivity(), ProjectActivity.class);
                    intent.putExtra("nameProject", tv_nameProject.getText().toString());
                    startActivity(intent);
                    getActivity().finish();
                }
               break;
            }
        }
    }

    public void openProject()
    {
        Intent intent = new Intent(FragTabCreateProject.this.getActivity(), ProjectActivity.class);
        intent.putExtra("openProjectWithName", tv_nameProject.getText().toString());
        startActivity(intent);
        getActivity().finish();
    }

    public AlertDialog AskOption(Context context)
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(context)
                //set message, title, and icon
                .setTitle(R.string.warning)
                .setMessage(R.string.ask_open)
                //.setIcon(R.drawable.delete)

                .setPositiveButton(R.string.conformation, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        openProject();
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    public void AddLinkProjectStartActivity(ProjectStartActivity projectStartActivity)
    {
        this.projectStartActivity = projectStartActivity;
    }
}
