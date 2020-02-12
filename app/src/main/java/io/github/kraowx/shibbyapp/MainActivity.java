package io.github.kraowx.shibbyapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.HttpRequest;
import io.github.kraowx.shibbyapp.tools.PatreonSessionManager;
import io.github.kraowx.shibbyapp.tools.UpdateManager;
import io.github.kraowx.shibbyapp.tools.Version;
import io.github.kraowx.shibbyapp.ui.dialog.ImportAppDataDialog;
import io.github.kraowx.shibbyapp.ui.dialog.ImportFileDialog;
import io.github.kraowx.shibbyapp.ui.dialog.PatreonLoginDialog;
import io.github.kraowx.shibbyapp.ui.dialog.SettingsDialog;

public class MainActivity extends AppCompatActivity
{
    private static final boolean IS_PRE_RELEASE = false;

    private static MainActivity mContext;
    private AppBarConfiguration mAppBarConfiguration;
    private AudioController audioController;
    private AudioDownloadManager downloadManager;
    private PatreonSessionManager patreonSessionManager;
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
    
    public PatreonSessionManager getPatreonSessionManager()
    {
        return patreonSessionManager;
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
        patreonSessionManager = new PatreonSessionManager(this);
        
        final String patreonEmail = prefs.getString("patreonEmail", null);
        final String patreonPassword = prefs.getString("patreonPassword", null);
        if (patreonEmail != null && patreonPassword != null)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    patreonSessionManager.generateCookie(patreonEmail, patreonPassword);
                }
            }.start();
        }
        
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
                R.id.nav_patreonfiles, R.id.nav_downloads,
                R.id.nav_userfiles, R.id.nav_playlists)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(
                this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        checkForUpdate();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        if (requestCode == 1)
        {
            if (grantResults.length == 0 || (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED))
            {
                Toast.makeText(MainActivity.this,
                        "Import files feature has been disabled",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 2)
        {
            if (grantResults.length == 0 || (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED))
            {
                Toast.makeText(MainActivity.this,
                        "This feature requires the storage permission",
                        Toast.LENGTH_SHORT).show();
            }
        }
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
            case R.id.action_import:
                showImportFileDialog();
                return true;
            case R.id.action_export_appdata:
                exportAllData();
                return true;
            case R.id.action_import_appdata:
                showImportAppDataDialog();
                return true;
            case R.id.action_patreon_login:
                showPatreonLoginDialog();
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
        SettingsDialog dialog = new SettingsDialog(this);
    }

    private void showImportFileDialog()
    {
        ImportFileDialog importDialog = new ImportFileDialog(this);
    }
    
    private void showImportAppDataDialog()
    {
        ImportAppDataDialog importAppDataDialog = new ImportAppDataDialog(this);
    }
    
    private void showPatreonLoginDialog()
    {
        PatreonLoginDialog patreonLoginDialog = new PatreonLoginDialog(this);
        patreonLoginDialog.setLoginListener(new PatreonLoginDialog.LoginListener()
        {
            @Override
            public void onLoginVerified(String email, String password)
            {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("patreonEmail", email);
                editor.putString("patreonPassword", password);
                editor.commit();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(MainActivity.this,
                                "Login successful", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    private void exportAllData()
    {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        JSONObject data = new JSONObject();
        try
        {
            JSONArray playlistNames = new JSONArray(
                    prefs.getString("playlists", "[]"));
            data.put("playlists", playlistNames);
            JSONArray userFiles = new JSONArray(
                    prefs.getString("userFiles", "[]"));
            data.put("userFiles", userFiles);
            for (int i = 0; i < playlistNames.length(); i++)
            {
                JSONArray playlistData = new JSONArray(
                        prefs.getString("playlist" +
                                playlistNames.get(i), "[]"));
                data.put("playlist" + playlistNames.get(i),
                        playlistData);
            }
            Date time = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMYYYY");
            File dir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/ShibbyApp");
            File out = new File(dir.getAbsolutePath() +
                    "/export" + sdf.format(time) + ".json");
            BufferedWriter writer = null;
            try
            {
                out.createNewFile();
                writer = new BufferedWriter(new FileWriter(out));
                writer.write(data.toString());
                writer.close();
                Toast.makeText(this, "Exported data to \"" +
                        out.getAbsolutePath() + "\"", Toast.LENGTH_LONG).show();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
    }

    private void setVersionOnUI()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        TextView txtVersion = (TextView)hView.findViewById(R.id.txtVersion);
        txtVersion.setText(getVersionName());
    }

    private Version getVersion()
    {
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
