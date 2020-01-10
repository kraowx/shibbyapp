package io.github.kraowx.shibbyapp.tools;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Uri uri = Uri.parse(file.getLink());
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
    
//    public void patreonLogin(String email, String password)
//    {
//        System.out.println("starting request");
//        HttpRequest req = HttpRequest.post("https://api.patreon.com/login");
//        req.header("Connection", "keep-alive");
//        req.header("Upgrade-Insecure-Requests", 1);
//        req.userAgent("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
////        req.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
//        req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        req.header("Accept-Language", "en-US,en;q=0.9");
//        req.send("{\"data\":{\"email\":\"" + email + "\"," +
//                "\"password\":\"" + password + "\"}}");
//        System.out.println(req.body());
//        System.out.println(req.headers());
//        List<String> cookies = req.headers().get("Set-Cookie");
//        String cookiestr = "";
//        for (int i = 0; i < cookies.size(); i++)
//        {
//            cookiestr += cookies.get(i);
//            if (i < cookies.size()-1)
//            {
//                cookiestr += "; ";
//            }
//        }
//        System.out.println(cookiestr);
//
//
//
//        req = req.get("https://api.patreon.com/current_user");
//        req.header("Connection", "keep-alive");
//        req.header("Upgrade-Insecure-Requests", 1);
//        req.userAgent("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
////        req.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
//        req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        req.header("Accept-Language", "en-US,en;q=0.9");
//        req.header("Cookie", cookiestr);
//        System.out.println(req.body());
//
//
//        String link = "http://patreon.com/file?h=29364945&i=4334926";
//        Uri uri = Uri.parse(link);
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.addRequestHeader("Cookie", cookiestr);
//        request.setTitle("test");
//        request.setDescription("Downloading file");
//        request.setNotificationVisibility(
//                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalFilesDir(mainActivity,
//                "/", "testdownload.mp3");
//        long id = downloadManager.enqueue(request);
//        System.out.println(id);
//        mainActivity.runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                Toast.makeText(mainActivity, "Downloading file...",
//                        Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }

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
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(mainActivity);
                    boolean darkModeEnabled = prefs
                            .getBoolean("darkMode", false);
                    if (darkModeEnabled)
                    {
                        btn.setColorFilter(ContextCompat
                                .getColor(mainActivity, R.color.grayLight));
                    }
                    else
                    {
                        btn.setColorFilter(null);
                    }
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
}
