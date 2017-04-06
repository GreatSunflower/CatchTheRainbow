package com.sunflower.catchtherainbow.Adapters;

/**
 * Created by Alexandr on 17.03.2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.StartedApp.ProjectStartActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends ArrayAdapter<Project> implements View.OnClickListener
{
    int resource;
    public String path = "";
    Context context;
    protected List<Project> projects = null;
    public ProjectAdapter(Context context, int resource, List<Project> items)
    {
        super(context, resource, items);
        this.context = context;
        this.resource=resource;
        this.projects = items;
        path =  Environment.getExternalStorageDirectory().toString() + "/Catch The Rainbow";
        readFiles();
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public void add(Project p)
    {
        // Добавить проект в начало списка
        projects.add(projects.size(), p);
        // Перерисовать список
        notifyDataSetChanged();
    }

    private File root;
    private String search = "";
    public void setSearch(String search)
    {
        this.search = search;
        readFiles();
    }
    private ArrayList<File> fileList = new ArrayList<File>();
    public void readFiles()
    {
        try
        {
            String secStore = "";
            projects.clear();
            fileList.clear();
            notifyDataSetChanged();
            if(path.equals(""))
            {
                secStore = System.getenv("SECONDARY_STORAGE");
                path = secStore;
            }
            else secStore = path;
            root = new File(secStore);
            getfile(root);

            for (int i = 0; i < fileList.size(); i++)
            {
                if (fileList.get(i).isDirectory())
                {
                    if(fileList.get(i).getName().toLowerCase().contains(search.toLowerCase()))
                    {
                        try
                        {
                            add(Project.openProject(fileList.get(i).getName(), null));
                        }
                        catch (Exception exception){}
                    }
                } // is dir
            }
            notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Project selectedProject = null;
    public void setNewSelection(Project selectedProject)
    {
        this.selectedProject = selectedProject;
        notifyDataSetChanged();
    }

    private String getFileExtension(File file)
    {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public ArrayList<File> getfile(File dir)
    {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0)
        {
            for (int i = 0; i < listFile.length; i++)
            {
                if (listFile[i].isDirectory())
                {
                    if(hasProjectFile(listFile[i].getAbsolutePath()))
                    {
                        fileList.add(listFile[i]);
                    }
                }
            }
        }
        return fileList;
    }

    private boolean hasProjectFile(String dirPath)
    {
        File dir = new File(dirPath);

        if(!dir.exists() || !dir.isDirectory()) return false;

        for (File f: dir.listFiles())
        {
            try
            {
                Project.openProject(f.getName().replaceFirst("[.][^.]+$", ""), null);
                return true;
            }
            catch (Exception ex){}
        }
        return false;
    }

    // Отрендерить пункт
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        Project project = getItem(position);
        if(convertView==null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView=inflator.inflate(R.layout.item_project,parent,false);
        }

        View itemView = convertView.findViewById(R.id.folders_layout);
        itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundListView));

        if(selectedProject == project)
            itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedListItem));
        else
            itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundListView));

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tvTitleFolder);
        tvTitle.setText(project.getName());

        TextView tvPath = (TextView)convertView.findViewById(R.id.tvPathFolder);
        tvPath.setText(project.getProjectFolderLocation());

        Button bRemoveProject = (Button)convertView.findViewById(R.id.bRemoveProject);
        bRemoveProject.setOnClickListener(this);
        bRemoveProject.setTag(project.getProjectFolderLocation());

        /*ImageView tvImage = (ImageView)convertView.findViewById(R.id.imageViewFolder);
        tvImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder));*/

        return convertView;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bRemoveProject:
            {
                String pathProject = view.getTag().toString();
                AlertDialog diaBox = AskOption(new File(pathProject), context);
                diaBox.show();
                break;
            }
        }
    }

    public void deleteProjectByName(String strFileName)
    {
        for(Project project : projects)
        {
            if(project.getName().equals(strFileName))
            {
                projects.remove(project);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void openProjectStartActivity()
    {
        Intent intent = new Intent(context, ProjectStartActivity.class);
        context.startActivity(intent);
    }

    public AlertDialog AskOption(final File dir, Context context)
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(context)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(R.string.ask_delete)
                //.setIcon(R.drawable.delete)

                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        Helper.deleteDirectoryRecursive(dir);
                        deleteProjectByName(dir.getName());
                        if(projects.size() == 0) openProjectStartActivity();
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

    /*class MyThread extends Thread
    {
        String pathProject="";
        public MyThread(String pathProject)
        {
            super("Thread");
            this.pathProject = pathProject;
            start();
        }

        @Override
        public void run()
        {
            AlertDialog diaBox = Helper.AskOption(new File(pathProject), context);
            diaBox.show();
        }
    }*/
}