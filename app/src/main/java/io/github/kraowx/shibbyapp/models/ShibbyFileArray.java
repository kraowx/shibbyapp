package io.github.kraowx.shibbyapp.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShibbyFileArray
{
    private String name, description;
    private List<ShibbyFile> files;
    private Map<String, String> extraData;

    public ShibbyFileArray(String name)
    {
        init(name, null, null);
    }

    public ShibbyFileArray(String name, ShibbyFile[] files,
                           Map<String, String> extraData)
    {
        init(name, files, extraData);
    }

    private void init(String name, ShibbyFile[] files,
                      Map<String, String> extraData)
    {
        this.name = name;
        this.files = new ArrayList<ShibbyFile>();
        if (files != null)
        {
            for (ShibbyFile file : files)
            {
                this.files.add(file);
            }
        }
        this.extraData = extraData != null ? extraData :
                new HashMap<String, String>();
    }

    public static ShibbyFileArray fromJSON(String jsonStr)
    {
        ShibbyFileArray arr = new ShibbyFileArray(null,
                null, null);
        try
        {
            JSONObject json = new JSONObject(jsonStr);
            arr.name = json.getString("name");
            if (json.has("description"))
            {
                arr.description = json.getString("description");
            }
            JSONArray filesArr = json.getJSONArray("files");
            arr.files = new ArrayList<ShibbyFile>();
            for (int i = 0; i < filesArr.length(); i++)
            {
                arr.files.add(ShibbyFile.fromJSON(
                        filesArr.getJSONObject(i).toString()));
            }
            arr.extraData = new HashMap<String, String>();
            JSONObject extras = new JSONObject();
            Iterator<String> it = extras.keys();
            while (it.hasNext())
            {
                String key = it.next();
                arr.extraData.put(key, extras.getString(key));
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return arr;
    }

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("name", name);
            if (description != null)
            {
                json.put("description", description);
            }
            json.put("fileCount", files.size());
            JSONArray arr = new JSONArray();
            for (ShibbyFile file : files)
            {
                arr.put(file.toJSON());
            }
            json.put("files", arr);
            JSONObject extras = new JSONObject();
            for (String key : extraData.keySet())
            {
                extras.put(key, extraData.get(key));
            }
            json.put("extras", extras);
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return json;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getFileCount()
    {
        return files.size();
    }

    public List<ShibbyFile> getFiles()
    {
        return files;
    }

    public void setFiles(List<ShibbyFile> files)
    {
        this.files = files;
    }

    public boolean addFile(ShibbyFile file)
    {
        if (!files.contains(file))
        {
            files.add(file);
            return true;
        }
        return false;
    }

    public boolean removeFile(ShibbyFile file)
    {
        if (files.contains(file))
        {
            files.remove(file);
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
