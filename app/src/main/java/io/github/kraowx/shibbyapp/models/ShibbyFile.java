package io.github.kraowx.shibbyapp.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class ShibbyFile
{
    private String name, id, link, description;
    private List<String> tags;
    private Map<String, String> extraData;

    public ShibbyFile(String name, String link, String description)
    {
        init(name, null, link, description, null);
    }

    public ShibbyFile(String name, String id, String link,
                      String description, Map<String, String> extraData)
    {
        init(name, id, link, description, extraData);
    }

    private void init(String name, String id, String link,
                      String description, Map<String, String> extraData)
    {
        this.name = name;
        if (id == null && name != null)
        {
            createIdFromName();
        }
        this.link = link;
        this.description = description;
        tags = getTagsFromName();
        this.extraData = extraData != null ? extraData :
                new HashMap<String, String>();
    }

    public static ShibbyFile fromJSON(String jsonStr)
    {
        ShibbyFile file = new ShibbyFile(null, null, null);
        try
        {
            JSONObject json = new JSONObject(jsonStr);
            file.name = json.getString("name");
            if (!json.has("id") && file.name != null)
            {
                file.createIdFromName();
            }
            file.link = json.getString("link");
            file.description = json.getString("description");
            file.extraData = new HashMap<String, String>();
            JSONObject extras = json.getJSONObject("extras");
            Iterator<String> it = extras.keys();
            while (it.hasNext())
            {
                String key = it.next();
                file.extraData.put(key, extras.getString(key));
            }
        }
        catch (JSONException je)
        {
            //je.printStackTrace();
        }
        return file;
    }

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("name", name);
            if (id == null)
            {
                createIdFromName();
            }
            json.put("id", id);
            json.put("link", link);
            json.put("description", description);
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

    public String getId()
    {
        if (id == null)
        {
            createIdFromName();
        }
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public Map<String, String> getExtras()
    {
        return extraData;
    }

    public void setExtras(Map<String, String> extras)
    {
        extraData = extras;
    }

    private List<String> getTagsFromName()
    {
        List<String> tags = new ArrayList<String>();
        if (name != null)
        {
            StringBuilder tag = new StringBuilder();
            boolean in = false;
            for (int i = 0; i < name.length(); i++)
            {
                char c = name.charAt(i);
                if (c == ']')
                {
                    tags.add(tag.toString());
                    tag.setLength(0);
                    in = false;
                }
                else if (c == '[')
                {
                    in = true;
                }
                else if (c != '[' && in)
                {
                    tag.append(c);
                }
            }
        }
        return tags;
    }

    private void createIdFromName()
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++)
            {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            id = hexString.toString();
        }
        catch (NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
            id = "";
        }
    }

    @Override
    public String toString()
    {
        return name;
    }
}
