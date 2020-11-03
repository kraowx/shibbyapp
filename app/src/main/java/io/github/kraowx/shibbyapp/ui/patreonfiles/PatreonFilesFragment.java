package io.github.kraowx.shibbyapp.ui.patreonfiles;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.ui.dialog.FileFilterController;
import io.github.kraowx.shibbyapp.ui.dialog.FileInfoDialog;
import io.github.kraowx.shibbyapp.ui.dialog.PatreonLoginDialog;
import io.github.kraowx.shibbyapp.ui.dialog.PatreonRefreshInfoDialog;
import io.github.kraowx.shibbyapp.ui.playlists.AddFileToPlaylistDialog;

public class PatreonFilesFragment extends Fragment
		implements ShibbyPatreonFileAdapter.ItemClickListener,
		SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener,
		PatreonLoginDialog.LoginListener, FileFilterController.FilterListener
{
	private int[] fileTypes;
	private String[] tags;
	private int[] durations;
	private RecyclerView list;
	private SwipeRefreshLayout refreshLayout;
	private ShibbyPatreonFileAdapter listAdapter;
	private LinearLayoutManager listLayoutManager;
	private View root;
	
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		root = inflater.inflate(R.layout.fragment_allfiles, container, false);
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
		
		final FloatingActionButton fabAdd = ((MainActivity)getActivity())
				.findViewById(R.id.fabAdd);
		fabAdd.setImageResource(R.drawable.ic_add);
		fabAdd.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AddFileToPlaylistDialog dialog = new AddFileToPlaylistDialog(
						(MainActivity)getActivity(), listAdapter.getCheckedFiles()
						.toArray(new ShibbyFile[0]), false);
				dialog.setFilesAddedListener(new AddFileToPlaylistDialog.FilesAddedListener()
				{
					@Override
					public void filesAdded(boolean added)
					{
						if (added)
						{
							listAdapter.clearCheckedFiles();
							listAdapter.notifyDataSetChanged();
							fabAdd.hide();
						}
					}
				});
			}
		});
		fabAdd.hide();
		
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
				final SearchView searchView = ((MainActivity)getActivity()).getSearchView();
				if (searchView != null)
				{
					searchView.post(new Runnable()
					{
						@Override
						public void run()
						{
							searchView.setQuery("", false);
							searchView.setIconified(true);
						}
					});
					searchView.setOnQueryTextListener(PatreonFilesFragment.this);
					this.cancel();
				}
			}
		}, 0, 1000);
		
		FileFilterController fileFilterController =
				((MainActivity)getActivity()).getFileFilterController();
		fileFilterController.setButtonVisible(true);
		fileFilterController.setListener(this);
		return root;
	}
	
	@Override
	public void filtersUpdated(int[] fileTypes, int[] durations, String[] tags)
	{
		if (listAdapter != null)
		{
			this.fileTypes = fileTypes;
			this.durations = durations;
			this.tags = tags;
			listAdapter.filterDisplayItems(null, fileTypes, durations, tags);
		}
	}
	
	@Override
	public void onRefresh()
	{
		refreshData();
	}
	
	private void refreshData()
	{
		final PatreonRefreshInfoDialog refreshDialog = new PatreonRefreshInfoDialog(
				(MainActivity)getActivity());
		final DataManager dataManager = new DataManager((MainActivity)getActivity());
		List<ShibbyFile> patreonFiles = dataManager.getPatreonFiles();
		final boolean firstTime = patreonFiles.isEmpty();
		if (firstTime)
		{
			refreshDialog.show();
		}
		new Thread()
		{
			@Override
			public void run()
			{
				DataManager.PatreonResponseCode resp = dataManager
						.requestPatreonData(firstTime, refreshDialog);
				switch (resp)
				{
					case NO_LOGIN:
						showNoLoginDialog();
						break;
					case NO_DATA:
						showUnknownErrorDialog();
						break;
					case TOO_MANY_REQUESTS_10:
						showOverloadDialog(10);
						break;
					case TOO_MANY_REQUESTS_30:
						showOverloadDialog(30);
						break;
					case EMAIL_VERIFICATION_REQUIRED:
						showEmailVerificationDialog();
						break;
					case INVALID_LOGIN:
						showBadLoginDialog();
						break;
					case SUCCESS:
						showToast("Data updated");
						if (firstTime)
						{
							initializeList(root, new DataManager(
									(MainActivity)getActivity()).getPatreonFiles());
						}
						else
						{
							updateList();
						}
						break;
				}
				refreshLayout.post(new Runnable()
				{
					@Override
					public void run()
					{
						refreshLayout.setRefreshing(false);
					}
				});
				((MainActivity)getActivity()).runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						refreshDialog.dismiss();
					}
				});
			}
		}.start();
	}
	
	private void showToast(final String message)
	{
		((MainActivity)getActivity()).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText((MainActivity)getActivity(),
						message, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void showEmailVerificationDialog()
	{
		showErrorDialog("Email Verification",
				"You must confirm your email for this device/location " +
						"through the email sent by Patreon.");
	}
	
	private void showOverloadDialog(int timeout)
	{
		showErrorDialog("Too Many Requests",
				"You have made too many requests to the Patreon server. " +
						"Try again in " + timeout + " minutes.");
	}
	
	private void showUnknownErrorDialog()
	{
		showErrorDialog("Unknown Error",
				"An unknown error has occurred. This is likely due to an " +
						"API change on Patreon's side. Contact me if you are " +
						"seeing this message.");
	}
	
	private void showNoLoginDialog()
	{
		showErrorDialog("Not Logged In",
				"You must log in to your Patreon account before you " +
						"can refresh the latest Patreon files.");
	}
	
	private void showBadLoginDialog()
	{
		showErrorDialog("Invalid Login",
				"Your email and password combination was rejected by " +
						"Patreon. Note: your account must have an active " +
						"pledge to Shibby.");
	}
	
	private void showErrorDialog(final String title, final String message)
	{
		((MainActivity)getActivity()).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences((MainActivity)getActivity());
				boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
				AlertDialog.Builder builder;
				if (darkModeEnabled)
				{
					builder = new AlertDialog.Builder((MainActivity)getActivity(),
							R.style.DialogThemeDark);
				}
				else
				{
					builder = new AlertDialog.Builder((MainActivity)getActivity());
				}
				builder.setTitle(title)
						.setMessage(message)
						.setCancelable(false)
						.setPositiveButton(android.R.string.ok, null)
						.show();
			}
		});
	}
	
	@Override
	public boolean onQueryTextChange(String text)
	{
		if (listAdapter != null)
		{
			listAdapter.filterDisplayItems(text, this.fileTypes, this.durations, this.tags);
		}
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
		FileInfoDialog fileInfoDialog = new FileInfoDialog(
				(MainActivity)getActivity(), listAdapter.getItem(position),
				listAdapter.getData(), null);
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
		((MainActivity)getActivity()).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText((MainActivity)getActivity(),
						"Login successful", Toast.LENGTH_LONG).show();
				refreshData();
			}
		});
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
}
