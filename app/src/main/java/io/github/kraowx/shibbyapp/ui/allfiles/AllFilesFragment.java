package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.DataManager;

public class AllFilesFragment extends Fragment
        implements ShibbyFileAdapter.ItemClickListener, SearchView.OnQueryTextListener
{
    private RecyclerView list;
    private ShibbyFileAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.fragment_allfiles, container, false);
        final ProgressDialog progressDialog = new ProgressDialog((MainActivity)getActivity());
        final DataManager dataManager = new DataManager((MainActivity)getActivity());
        final List<ShibbyFile> files = dataManager.getFiles();
        if (files.size() > 0)
        {
            initializeList(root, files);
        }
        else
        {
            progressDialog.setTitle("Fetching first-time data...");
            progressDialog.show();
            new Timer().scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (!dataManager.needsUpdate())
                    {
                        System.out.println("INITIALIZING LIST");
                        initializeList(root, dataManager.getFiles());
                        ((MainActivity)getActivity()).runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.hide();
                                }
                            }
                        });
                        this.cancel();
                    }
                }
            }, 0, 1000);
        }

        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.hide();

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                SearchView searchView = ((MainActivity)getActivity()).getSearchView();
                if (searchView != null)
                {
                    searchView.setOnQueryTextListener(AllFilesFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
        return root;
    }

    @Override
    public boolean onQueryTextChange(String text)
    {
        listAdapter.filterDisplayItems(text);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String text)
    {
        return false;
    }

    @Override
    public void onItemClick(View view, int position)
    {
        createFileInfoDialog(listAdapter.getItem(position));
    }

    private void initializeList(View root, final List<ShibbyFile> files)
    {
        list = root.findViewById(R.id.listFiles);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
//                files.add(new ShibbyFile("file1", "link1", "description1"));
//                files.add(new ShibbyFile("file2", "link2", "description2"));
//                files.add(new ShibbyFile("file3", "link3", "description3"));
//                files.add(new ShibbyFile("file4", "link4", "description4"));
//                files.add(new ShibbyFile("file5", "link5", "description5"));
                listLayoutManager = new LinearLayoutManager(getContext());
                list.setLayoutManager(listLayoutManager);
                listAdapter = new ShibbyFileAdapter(getContext(),
                        files, ((MainActivity)getActivity()));
                list.setAdapter(listAdapter);
                listAdapter.setClickListener(AllFilesFragment.this);
            }
        });
    }

    private void createFileInfoDialog(ShibbyFile file)
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.file_info_dialog);
        dialog.setTitle("File Info");
        TextView title = dialog.findViewById(R.id.txtTitle);
        title.setText(file.getName());
        TextView description = dialog.findViewById(R.id.txtDescription);
        description.setText(file.getDescription());
        dialog.show();
    }
}