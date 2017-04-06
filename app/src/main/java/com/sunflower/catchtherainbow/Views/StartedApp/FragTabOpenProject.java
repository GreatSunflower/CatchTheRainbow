package com.sunflower.catchtherainbow.Views.StartedApp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.sunflower.catchtherainbow.Adapters.ProjectAdapter;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.ProjectActivity;
import com.sunflower.catchtherainbow.R;

import java.io.File;
import java.util.ArrayList;

public class FragTabOpenProject extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SearchView.OnQueryTextListener
{
    private ProjectStartActivity projectStartActivity;

    public FragTabOpenProject()
    {
        // Required empty public constructor
    }

    public static FragTabOpenProject newInstance(String param1, String param2)
    {
        FragTabOpenProject fragment = new FragTabOpenProject();
        return fragment;
    }

    ListView listView;
    ProjectAdapter adapter;
    ArrayList<Project> superProjects =null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    Button b_Ok;
    private SearchView searchView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.tab_frag_open_project, container, false);
        b_Ok = (Button)resView.findViewById(R.id.bOk);
        b_Ok.setOnClickListener(this);

        searchView = (SearchView)resView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        superProjects = new ArrayList<Project>();

        listView = (ListView) resView.findViewById(R.id.listView);
        // ”казать xml-файл с шаблоном пункта
        adapter = new ProjectAdapter(getContext(), R.layout.item_project, superProjects);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        adapter.notifyDataSetChanged();

        return resView;
    }

    public void onResume()
    {
        super.onResume();
        if(adapter!=null) adapter.readFiles();
    }

    Project selectProject = null;
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    {
        Project item = adapter.getProjects().get(position);
        if(selectProject == item)
        {
            Intent intent = new Intent(FragTabOpenProject.this.getActivity(), ProjectActivity.class);
            intent.putExtra("openProjectWithName", selectProject.getName());
            startActivity(intent);
            getActivity().finish();
        }
        File f = new File(item.getProjectFolderLocation());
        if(f.isDirectory())
        {
            adapter.setNewSelection(item);
            selectProject = item;
            //adapter.path = item.getProjectFolderLocation() + "/" + item.getName();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.bOk:
            {
                if(selectProject == null) Toast.makeText(this.getActivity(), R.string.error_select_a_project, Toast.LENGTH_SHORT).show();
                else
                {
                    Intent intent = new Intent(FragTabOpenProject.this.getActivity(), ProjectActivity.class);
                    intent.putExtra("openProjectWithName", selectProject.getName());
                    startActivity(intent);
                    getActivity().finish();
                    //setCurrentProject(selectProject);
                }
                break;
            }
        }
    }

    public void AddLinkProjectStartActivity(ProjectStartActivity projectStartActivity)
    {
        this.projectStartActivity = projectStartActivity;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Search(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query)
    {
        Search(query);
        return true;
    }

    // Filter Class
    public void Search(String query)
    {
        adapter.setSearch(query);
    }
}
