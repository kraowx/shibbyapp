package io.github.kraowx.shibbyapp.tools;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.Response;
import io.github.kraowx.shibbyapp.net.ResponseType;

public class DataManager
{
    final int MAX_UPDATE_TIME = 10*60*1000; // 10 minutes

    private MainActivity mainActivity;
    private SharedPreferences prefs;

    public DataManager(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    }

    public boolean needsUpdate()
    {
        long lastUpdate = prefs.getLong("lastUpdate", 0);
        long currentTime = Calendar.getInstance().getTime().getTime();
        System.out.println(currentTime-lastUpdate);
        if (currentTime-lastUpdate > MAX_UPDATE_TIME)
        {
            return true;
        }
        return false;
    }

    public List<ShibbyFile> getFiles()
    {
        List<ShibbyFile> files = new ArrayList<ShibbyFile>();
        JSONArray filesjson = null;
        try
        {
            System.out.println(prefs.getString("files", "*******NO VALUE********"));
            filesjson = new JSONArray(prefs.getString("files", ""));
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        if (filesjson != null)
        {
            for (int i = 0; i < filesjson.length(); i++)
            {
                try
                {
                    files.add(ShibbyFile.fromJSON(filesjson.getJSONObject(i).toString()));
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
        }
        return files;
    }

    public List<ShibbyFileArray> getTags()
    {
        return getShibbyArray("tags");
    }

    public List<ShibbyFileArray> getSeries()
    {
        return getShibbyArray("series");
    }

    public List<ShibbyFileArray> getShibbyArray(String key)
    {
        List<ShibbyFileArray> arr = new ArrayList<ShibbyFileArray>();
        JSONArray filesjson = null;
        try
        {
            System.out.println(prefs.getString(key, "*******NO VALUE********"));
            filesjson = new JSONArray(prefs.getString(key, ""));
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        if (filesjson != null)
        {
            for (int i = 0; i < filesjson.length(); i++)
            {
                try
                {
                    arr.add(ShibbyFileArray.fromJSON(filesjson.getJSONObject(i).toString()));
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
        }
        return arr;
    }

    public void loadAllData()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    String[] server = prefs.getString("server",
                            "shibbyserver.ddns.net:1967").split(":");
                    String hostname = server[0];
                    int port = -1;
                    try
                    {
                        port = Integer.parseInt(server[1]);
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                    }
                    if (!hostname.isEmpty() && port != -1)
                    {
                        Socket socket = new Socket(hostname, port);
                        PrintWriter writer =
                                new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(socket.getInputStream()));
                        writer.println(Request.all());
                        Response resp;
                        String data;
                        while ((data = reader.readLine()) != null)
                        {
                            try
                            {
                                resp = Response.fromJSON(data);
                                System.out.println(resp.getType());
                                if (resp.getType() == ResponseType.ALL)
                                {
                                    JSONObject rawjson = resp.toJSON();
                                    JSONArray dataArr = rawjson.getJSONArray("data");
                                    JSONArray files = dataArr.getJSONObject(0).getJSONArray("files");
                                    JSONArray tags = dataArr.getJSONObject(0).getJSONArray("tags");
                                    JSONArray series = dataArr.getJSONObject(0).getJSONArray("series");
                                    long time = Calendar.getInstance().getTime().getTime();
                                    SharedPreferences.Editor editor = prefs.edit();
                                    System.out.println(files.toString());
                                    editor.putString("files", files.toString());
                                    editor.putString("tags", tags.toString());
                                    editor.putString("series", series.toString());
                                    editor.putLong("lastUpdate", time);
                                    editor.commit();
                                    break;
                                }
                            }
                            catch (JSONException je)
                            {
                                je.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        showToast("Server is invalid");
                    }
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                    showToast("Server error");
                }
            }
        }.start();
    }

    private void showToast(final String message)
    {
        mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(mainActivity, message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
