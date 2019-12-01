package io.github.kraowx.shibbyapp.tools;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class AudioDownloadManager
{
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

    public static void downloadFile(MainActivity mainActivity, ShibbyFile file)
    {
        DownloadManager downloadManager =
                (DownloadManager)mainActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file.getLink());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(file.getName());
        request.setDescription("Downloading file");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(mainActivity,
                "/audio", file.getId() + ".m4a");
        downloadManager.enqueue(request);
        Toast.makeText(mainActivity, "Downloading file...",
                Toast.LENGTH_LONG).show();
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
}
