package io.github.kraowx.shibbyapp.ui.patreonfiles;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.ui.dialog.PatreonLoginDialog;

public class PatreonFilesFragment extends Fragment
		implements ShibbyPatreonFileAdapter.ItemClickListener,
		SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener,
		PatreonLoginDialog.LoginListener
{
	private RecyclerView list;
	private SwipeRefreshLayout refreshLayout;
	private ShibbyPatreonFileAdapter listAdapter;
	private LinearLayoutManager listLayoutManager;
	
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		final View root = inflater.inflate(R.layout.fragment_allfiles, container, false);
		final ProgressDialog progressDialog = new ProgressDialog((MainActivity)getActivity());
		final DataManager dataManager = new DataManager((MainActivity)getActivity());
		
		refreshLayout = (SwipeRefreshLayout)root.findViewById(R.id.refreshLayout);
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setColorSchemeResources(R.color.colorPrimary,
				android.R.color.holo_green_dark,
				android.R.color.holo_orange_dark,
				android.R.color.holo_blue_dark);
		
		new Thread()
		{
			@Override
			public void run()
			{
				initializeList(root, dataManager.getPatreonFiles());
			}
		}.start();
		
		FloatingActionButton fabAddPlaylist =
				((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
		fabAddPlaylist.hide();
		
		if (!((MainActivity)getActivity()).getPatreonSessionManager().isAuthenticated())
		{
			PatreonLoginDialog loginDialog =
					new PatreonLoginDialog((MainActivity)getActivity());
			loginDialog.setLoginListener(this);
		}
		
		new Timer().scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				SearchView searchView = ((MainActivity)getActivity()).getSearchView();
				if (searchView != null)
				{
					searchView.setOnQueryTextListener(PatreonFilesFragment.this);
					this.cancel();
				}
			}
		}, 0, 1000);
		return root;
	}
	
	@Override
	public void onRefresh()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences((MainActivity)getActivity());
				String email = prefs.getString("patreonEmail", null);
				String password = prefs.getString("patreonPassword", null);
				if (email != null && password != null)
				{
					new DataManager((MainActivity) getActivity())
							.requestData(Request.patreonFiles(email, password));
					updateList();
					refreshLayout.post(new Runnable()
					{
						@Override
						public void run()
						{
							refreshLayout.setRefreshing(false);
						}
					});
				}
				else
				{
					Toast.makeText((MainActivity)getActivity(),
							"You are not logged in!",
							Toast.LENGTH_LONG).show();
				}
			}
		}.start();
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
	
	@Override
	public void onLoginVerified(String email, String password)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences((MainActivity)getActivity());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("patreonEmail", email);
		editor.putString("patreonPassword", password);
		editor.commit();
		updateList();
	}
	
	private void initializeList(View root, final List<ShibbyFile> patreonFiles)
	{
		list = root.findViewById(R.id.listFiles);
		list.post(new Runnable()
		{
			@Override
			public void run()
			{
				listLayoutManager = new LinearLayoutManager(getContext());
				list.setLayoutManager(listLayoutManager);
				listAdapter = new ShibbyPatreonFileAdapter(getContext(),
						patreonFiles, ((MainActivity)getActivity()));
				list.setAdapter(listAdapter);
				listAdapter.setClickListener(PatreonFilesFragment.this);
			}
		});
	}
	
	private void updateList()
	{
		DataManager dataManager = new DataManager((MainActivity)getActivity());
		listAdapter.setData(dataManager.getPatreonFiles());
		list.post(new Runnable()
		{
			@Override
			public void run()
			{
				listAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private void createFileInfoDialog(ShibbyFile file)
	{
		final Dialog dialog = new Dialog(getContext());
		dialog.setContentView(R.layout.file_info_dialog);
		dialog.setTitle("File Info");
		TextView title = dialog.findViewById(R.id.txtTitle);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences((MainActivity)getActivity());
		boolean displayLongNames = prefs.getBoolean(
				"displayLongNames", false);
		title.setText(displayLongNames ?
				file.getName() : file.getShortName());
		TextView tags = dialog.findViewById(R.id.txtTags);
		tags.setText(getTagsString(file.getTags()));
		TextView description = dialog.findViewById(R.id.txtDescription);
		description.setText(file.getDescription());
		dialog.show();
	}
	
	private String getTagsString(List<String> tags)
	{
		String tagsStr = "";
		for (int i = 0; i < tags.size(); i++)
		{
			tagsStr += tags.get(i);
			if (i < tags.size()-1)
			{
				tagsStr += "  |  ";
			}
		}
		return tagsStr + "\n";
	}
}
