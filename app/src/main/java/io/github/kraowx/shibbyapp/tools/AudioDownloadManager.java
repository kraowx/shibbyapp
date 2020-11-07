package io.github.kraowx.shibbyapp.tools;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.odysseus.ithaka.audioinfo.AudioInfo;
import de.odysseus.ithaka.audioinfo.mp3.MP3Info;
import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class AudioDownloadManager
{
    private MainActivity mainActivity;
    private DownloadManager downloadManager;
    private Map<Long, ShibbyFile> downloads;
    private Map<Long, ImageButton> buttons;

    public AudioDownloadManager(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        downloads = new HashMap<Long, ShibbyFile>();
        buttons = new HashMap<Long, ImageButton>();
        downloadManager = (DownloadManager)mainActivity
                .getSystemService(Context.DOWNLOAD_SERVICE);
        mainActivity.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static List<ShibbyFile> getDownloadedFiles(
            Context context, List<ShibbyFile> allFiles)
    {
        List<ShibbyFile> downloadedFiles = new ArrayList<ShibbyFile>();
        for (ShibbyFile file : allFiles)
        {
            if (fileIsDownloaded(context, file))
            {
                downloadedFiles.add(file);
            }
        }
        return downloadedFiles;
    }

    public static boolean fileIsDownloaded(Context context, ShibbyFile file)
    {
        if (file.getTier() != null && file.getTier().equals(new PatreonTier(PatreonTier.USER)))
        {
            return true;
        }
        File dir = context.getExternalFilesDir("/audio");
        for (File fileX : dir.listFiles())
        {
            if (fileX.getName().equals(file.getId() + ".m4a"))
            {
                return true;
            }
        }
        return false;
    }

    public static File getFileLocation(Context context, ShibbyFile file)
    {
        return new File(context.getExternalFilesDir("/audio") +
                "/" + file.getId() + ".m4a");
    }

    public void downloadFile(ShibbyFile file, ImageButton btn)
    {
        String urlStr = file.getAudioURL();
        if (file.getTier().greaterThan(mainActivity.getPatreonSessionManager().getTier()))
        {
            resetButton(btn);
            showTextDialog("Download failed", "This file is currently only " +
                    "available to " + file.getTier() + " patrons and higher.");
            return;
        }
        else if (urlStr == null || (urlStr != null && urlStr.equals("")))
        {
            resetButton(btn);
//            Toast.makeText(mainActivity, "Download failed: file " +
//                    "not yet ready for download", Toast.LENGTH_LONG).show();
            showTextDialog("Download failed", "This file is not yet available to download.");
            return;
        }
        if (file.getTier().getTier() == PatreonTier.USER)
        {
            resetButton(btn);
            Toast.makeText(mainActivity, "Download failed: " +
                    "user files are already downloaded", Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = null;
        try
        {
            if (file.getTier().greaterThan(new PatreonTier(PatreonTier.FREE)))
            {
                uri = Uri.parse(file.getAudioURL());
            }
            else
            {
                uri = Uri.parse(file.getFreeAudioURL());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            resetButton(btn);
            Toast.makeText(mainActivity, "Download failed: " +
                    "File has a malformed source", Toast.LENGTH_LONG).show();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.addRequestHeader("Cookie", mainActivity
                .getPatreonSessionManager().getCookie());
        request.setTitle(file.getName());
        request.setDescription("Downloading file");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(mainActivity,
                "/audio", file.getId() + ".m4a");
        long id = downloadManager.enqueue(request);
        downloads.put(id, file);
        buttons.put(id, btn);
        Toast.makeText(mainActivity, "Downloading file...",
                Toast.LENGTH_LONG).show();
    }

    public boolean cancelDownload(ShibbyFile file)
    {
        for (long id : downloads.keySet())
        {
            if (file.getId().equals(downloads.get(id).getId()))
            {
                downloadManager.remove(id);
                downloads.remove(id);
                buttons.remove(id);
                return true;
            }
        }
        return false;
    }

    public boolean isDownloadingFile(ShibbyFile file)
    {
        for (long id : downloads.keySet())
        {
            if (file.getId().equals(downloads.get(id).getId()))
            {
                return true;
            }
        }
        return false;
    }

    public static void deleteFile(Context context, ShibbyFile file)
    {
        File fileLoc = getFileLocation(context, file);
        if (fileLoc.exists())
        {
            fileLoc.delete();
            Toast.makeText(context, "File deleted",
                    Toast.LENGTH_LONG).show();
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver()
    {
        public void onReceive(Context ctxt, Intent intent)
        {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            ImageButton btn = buttons.get(id);
            if (btn != null)
            {
                int status = query(id);
                if (status == DownloadManager.STATUS_SUCCESSFUL)
                {
                    btn.setColorFilter(ContextCompat.getColor(
                            mainActivity, R.color.colorAccent));
                }
                else
                {
                    resetButton(btn);
                }
            }
            downloads.remove(id);
            buttons.remove(id);
        }
    };
    
    private int query(long id)
    {
        DownloadManager.Query query = new DownloadManager.Query();
        if(query != null)
        {
            query.setFilterById(id);
        }
        else
        {
            return -1;
        }
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst())
        {
            return c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }
        return -1;
    }
    
    private void resetButton(final ImageButton button)
    {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mainActivity);
        final boolean darkModeEnabled = prefs
                .getBoolean("darkMode", false);
        button.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (darkModeEnabled)
                {
                    button.setColorFilter(ContextCompat
                            .getColor(mainActivity, R.color.grayLight));
                }
                else
                {
                    button.setColorFilter(null);
                }
            }
        });
    }
    
    private void showTextDialog(String title, String text)
    {
        Drawable darkIcon = ContextCompat.getDrawable(mainActivity,
                R.drawable.ic_warning).mutate();
        darkIcon.setColorFilter(new ColorMatrixColorFilter(new float[]
                {
                        -1, 0, 0, 0, 200,
                        0, -1, 0, 0, 200,
                        0, 0, -1, 0, 200,
                        0, 0, 0, 1, 0
                }));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
        AlertDialog.Builder builder;
        if (darkModeEnabled)
        {
            builder = new AlertDialog.Builder(mainActivity, R.style.DialogThemeDark);
            builder.setIcon(darkIcon);
        }
        else
        {
            builder = new AlertDialog.Builder(mainActivity);
            builder.setIcon(R.drawable.ic_warning);
        }
        builder.setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
