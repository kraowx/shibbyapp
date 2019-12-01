package io.github.kraowx.shibbyapp.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class PlaylistManager
{
    public static List<String> getPlaylists(Context context)
    {
        List<String> playlists = new ArrayList<String>();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String playlistsData = prefs.getString("playlists", "none");
        if (!playlistsData.equals("none"))
        {
            try
            {
                JSONArray arr = new JSONArray(playlistsData);
                for (int i = 0; i < arr.length(); i++)
                {
                    playlists.add(arr.getString(i));
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return playlists;
    }

    public static boolean addPlaylist(Context context, String playlistName)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        List<String> playlists = getPlaylists(context);
        if (!playlists.contains(playlistName))
        {
            JSONArray arr = new JSONArray();
            for (String playlist : playlists)
            {
                arr.put(playlist);
            }
            arr.put(playlistName);
            editor.putString("playlists", arr.toString());
            editor.putString("playlist" + playlistName, "[]");
            editor.commit();
            return true;
        }
        return false;
    }

    public static boolean removePlaylist(Context context, String playlistName)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        List<String> playlists = getPlaylists(context);
        if (playlists.contains(playlistName))
        {
            JSONArray arr = new JSONArray();
            for (String playlist : playlists)
            {
                if (!playlist.equals(playlistName))
                {
                    arr.put(playlist);
                }
            }
            editor.putString("playlists", arr.toString());
            editor.remove("playlist" + playlistName);
            editor.commit();
            return true;
        }
        return false;
    }

    public static List<ShibbyFile> getFilesFromPlaylist(Context context, String playlistName)
    {
        List<ShibbyFile> files = new ArrayList<ShibbyFile>();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String playlistData = prefs.getString("playlist" + playlistName, "none");
        if (!playlistData.equals("none"))
        {
            try
            {
                JSONArray arr = new JSONArray(playlistData);
                for (int i = 0; i < arr.length(); i++)
                {
                    ShibbyFile file = ShibbyFile.fromJSON(arr.getJSONObject(i).toString());
                    files.add(file);
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return files;
    }

    public static int getPlaylistFileCount(Context context, String playlistName)
    {
        ArrayList<ShibbyFile> files =
                (ArrayList<ShibbyFile>)getFilesFromPlaylist(context, playlistName);
        return files.size();
    }

    public static boolean addFileToPlaylist(Context context, ShibbyFile file, String playlistName)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        List<ShibbyFile> files = getFilesFromPlaylist(context, playlistName);
        if (!listHasFile(files, file))
        {
            String playlistData = prefs.getString("playlist" + playlistName, "none");
            if (playlistData.equals("none"))
            {
                playlistData = "[]";
            }
            try
            {
                JSONArray arr = new JSONArray(playlistData);
                JSONObject fileJson = file.toJSON();
                arr.put(fileJson);
                editor.putString("playlist" + playlistName, arr.toString());
                editor.commit();
                return true;
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return false;
    }

    public static boolean removeFileFromPlaylist(Context context, ShibbyFile file, String playlistName)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        List<ShibbyFile> files = getFilesFromPlaylist(context, playlistName);
        if (listHasFile(files, file))
        {
            String playlistData = prefs.getString("playlist" + playlistName, "none");
            if (playlistData.equals("none"))
            {
                playlistData = "[]";
            }
            try
            {
                JSONArray arr = new JSONArray(playlistData);
                JSONObject fileJson = file.toJSON();
                for (int i = 0; i < arr.length(); i++)
                {
                    String idI = arr.getJSONObject(i).getString("id");
                    String idF = fileJson.getString("id");
                    if (idI.equals(idF))
                    {
                        arr.remove(i);
                    }
                }
                editor.putString("playlist" + playlistName, arr.toString());
                editor.commit();
                return true;
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return false;
    }

    private static boolean listHasFile(List<ShibbyFile> list, ShibbyFile file)
    {
        String id = file.getId();
        for (ShibbyFile fileX : list)
        {
            String idX = fileX.getId();
            if (idX != null && id != null && id.equals(idX))
            {
                return true;
            }
        }
        return false;
    }
}
