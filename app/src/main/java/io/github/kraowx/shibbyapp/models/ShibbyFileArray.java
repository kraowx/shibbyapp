package io.github.kraowx.shibbyapp.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShibbyFileArray
{
    private String name;
    private List<ShibbyFile> files;

    public ShibbyFileArray(String name)
    {
        init(name, null);
    }

    public ShibbyFileArray(String name, ShibbyFile[] files)
    {
        init(name, files);
    }

    private void init(String name, ShibbyFile[] files)
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
    }

    public static ShibbyFileArray fromJSON(String jsonStr)
    {
        ShibbyFileArray arr = new ShibbyFileArray(null, null);
        try
        {
            JSONObject json = new JSONObject(jsonStr);
            arr.name = json.getString("name");
            JSONArray filesArr = json.getJSONArray("files");
            arr.files = new ArrayList<ShibbyFile>();
            for (int i = 0; i < filesArr.length(); i++)
            {
                arr.files.add(ShibbyFile.fromJSON(filesArr.getJSONObject(i).toString()));
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
            json.put("fileCount", files.size());
            JSONArray arr = new JSONArray();
            for (ShibbyFile file : files)
            {
                arr.put(file.toJSON());
            }
            json.put("files", arr);
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
