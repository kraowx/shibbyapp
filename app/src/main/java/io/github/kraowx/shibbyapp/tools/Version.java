package io.github.kraowx.shibbyapp.tools;

import org.json.JSONException;
import org.json.JSONObject;

public class Version
{
    private int semVer;
    private boolean preRelease;
    private String name;

    public Version(String name, boolean preRelease)
    {
        parseVersion(name);
        this.name = name;
        this.preRelease = preRelease;
    }

    public Version(JSONObject json)
    {
        try
        {
            parseVersion(json.getString("name"));
            name = json.getString("name");
            preRelease = json.getBoolean("prerelease");
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isPreRelease()
    {
        return this.preRelease;
    }

    private void parseVersion(String rawVersion)
    {
        if (rawVersion != null)
        {
            rawVersion = rawVersion.replace("v", "");
            String[] semVerArr = rawVersion.split("\\.");
            String semVerStr = "";
            for (int i = 0; i < semVerArr.length; i++)
            {
                semVerStr += semVerArr[i];
            }
            semVer = Integer.parseInt(semVerStr);
        }
        else
        {
            semVer = -1;
        }
    }

    public boolean greater(Version other)
    {
        return this.semVer > other.semVer;
    }
}
