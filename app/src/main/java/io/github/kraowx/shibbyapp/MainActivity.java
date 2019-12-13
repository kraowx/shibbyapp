package io.github.kraowx.shibbyapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.HttpRequest;
import io.github.kraowx.shibbyapp.tools.UpdateManager;
import io.github.kraowx.shibbyapp.tools.Version;

public class MainActivity extends AppCompatActivity
{
    private static final boolean IS_PRE_RELEASE = false;

    private static MainActivity mContext;
    private AppBarConfiguration mAppBarConfiguration;
    private AudioController audioController;
    private AudioDownloadManager downloadManager;
    private SearchView searchView;

    public static Context getContext()
    {
        return mContext;
    }

    public AudioController getAudioController()
    {
        return audioController;
    }

    public AudioDownloadManager getDownloadManager()
    {
        return downloadManager;
    }

    public SearchView getSearchView()
    {
        return searchView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean darkModeEnabled = prefs.getBoolean(
                "darkMode", false);
        if (darkModeEnabled)
        {
            setTheme(R.style.AppThemeDark);
        }
        else
        {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        audioController = new AudioController(this);
        downloadManager = new AudioDownloadManager(this);

        FloatingActionButton fab = findViewById(R.id.fabAudioController);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                audioController.toggleVisible();
            }
        });

        mContext = this;

        setVersionOnUI();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isStartup", true);
        editor.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_allfiles, R.id.nav_tags, R.id.nav_series,
                R.id.nav_downloads, R.id.nav_favorites, R.id.nav_playlists)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(
                this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        checkForUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                showSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void checkForUpdate()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    UpdateManager manager = new UpdateManager();
                    if (manager.updateAvailable(MainActivity.this, getVersion()))
                    {
                        manager.showUpdateMessage(MainActivity.this);
                    }
                }
                catch (HttpRequest.HttpRequestException hre)
                {
                    /*
                     * If the server cannot be reached, ignore it (don't notify the user).
                     * The user will be notified about an update the next time they
                     * connect to the network and open the application.
                     */
                }
            }
        }).start();
    }

    private void showSettingsDialog()
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settings_dialog);
        dialog.setTitle("Settings");
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean updateStartup = prefs.getBoolean("updateStartup", true);
        boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
        int autoplay = prefs.getInt("autoplay", 1);
        String server = prefs.getString("server", "shibbyserver.ddns.net:2012");
        Switch switchUpdateBackground = dialog.findViewById(R.id.switchUpdateOnStartup);
        switchUpdateBackground.setChecked(updateStartup);
        switchUpdateBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                editor.putBoolean("updateStartup", isChecked);
                editor.commit();
            }
        });
        Switch switchDarkMode = dialog.findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(darkModeEnabled);
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    setTheme(R.style.AppTheme);
                }
                else
                {
                    setTheme(R.style.AppThemeDark);
                }
                editor.putBoolean("darkMode", isChecked);
                editor.commit();
                Toast.makeText(dialog.getContext(),
                        "Relaunch the app for the change to take effect",
                        Toast.LENGTH_LONG).show();
            }
        });
        final EditText txtServer = dialog.findViewById(R.id.txtServer);
        txtServer.setText(server);
        Spinner spinnerAutoplay = dialog.findViewById(R.id.spinnerAutoplay);
        spinnerAutoplay.setSelection(autoplay);
        spinnerAutoplay.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id)
            {
                editor.putInt("autoplay", position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Button btnApplyChanges = dialog.findViewById(R.id.btnApplyChanges);
        btnApplyChanges.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editor.putString("server", txtServer.getText().toString());
                editor.commit();
            }
        });
        dialog.show();
    }

    private void setVersionOnUI()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView txtVersion = (TextView)hView.findViewById(R.id.txtVersion);
        txtVersion.setText(getVersionName());
    }

    private Version getVersion()
    {
        //System.out.println(new Version(getVersionName(), IS_PRE_RELEASE).getName());
        return new Version(getVersionName(), IS_PRE_RELEASE);
    }

    private String getVersionName()
    {
        try
        {
            PackageInfo pInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return "v" + pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException nnfe)
        {
            nnfe.printStackTrace();
        }
        return "";
    }
}
