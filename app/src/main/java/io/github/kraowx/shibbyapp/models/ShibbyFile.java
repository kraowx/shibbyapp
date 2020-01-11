package io.github.kraowx.shibbyapp.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShibbyFile
{
    private boolean isPatreonFile;
    private String name, shortName, id,
            link, description, type;
    private List<String> tags;
    private Map<String, String> extraData;

    public ShibbyFile(String name, String link,
                      String description, String type)
    {
        init(name, null, null, link,
                description, type, null);
    }

    public ShibbyFile(String name, String shortName,
                      String id, String link,
                      String description, String type,
                      Map<String, String> extraData)
    {
        init(name, shortName, id, link,
                description, type, extraData);
    }

    private void init(String name, String shortName,
                      String id, String link,
                      String description, String type,
                      Map<String, String> extraData)
    {
        this.name = name;
        if (shortName != null)
        {
            this.shortName = shortName;
        }
        else
        {
            this.shortName = getShortName(name);
        }
        if (id == null && name != null)
        {
            createIdFromName();
        }
        this.link = link;
        this.description = description;
        this.tags = getTagsFromName();
        this.type = type;
        this.extraData = extraData != null ? extraData :
                new HashMap<String, String>();
    }

    public static ShibbyFile fromJSON(String jsonStr)
    {
        ShibbyFile file = new ShibbyFile(null, null,
                null, null);
        try
        {
            JSONObject json = new JSONObject(jsonStr);
            file.name = json.getString("name");
            if (!json.has("shortName"))
            {
                file.shortName = file.getShortName(file.name);
            }
            else
            {
                file.shortName = json.getString("shortName");
            }
            if (!json.has("id") && file.name != null)
            {
                file.createIdFromName();
            }
            else
            {
                file.id = json.getString("id");
            }
            if (json.has("tags"))
            {
                JSONArray tags = json.getJSONArray("tags");
                file.tags = new ArrayList<String>();
                for (int i = 0; i < tags.length(); i++)
                {
                    file.tags.add(tags.getString(i));
                }
            }
            else
            {
                file.tags = file.getTagsFromName();
            }
            if (json.has("link"))
            {
                file.link = json.getString("link");
            }
            else if (json.has("links"))
            {
                file.link = json.getString("links")
                        .replace("[\"", "")
                        .replace("\"]", "")
                        .replace("\\/\\/", "//")
                        .replace("\\/", "/");
            }
            file.description = json.getString("description");
            if (!json.has("type"))
            {
                file.type = "";
            }
            else
            {
                file.type = json.getString("type");
            }
            if (!json.has("isPatreonFile"))
            {
                file.isPatreonFile = false;
            }
            else
            {
                file.isPatreonFile = json.getBoolean("isPatreonFile");
            }
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
            json.put("shortName", shortName);
            if (id == null)
            {
                createIdFromName();
            }
            json.put("id", id);
            JSONArray tagsJson = new JSONArray();
            for (String tag : tags)
            {
                tagsJson.put(tag);
            }
            json.put("tags", tagsJson);
            json.put("link", link);
            json.put("description", description);
            json.put("type", type);
            json.put("isPatreonFile", isPatreonFile);
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

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
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

    public boolean matchesTag(String search)
    {
        for (String tag : tags)
        {
            if (tag.toLowerCase().contains(search))
            {
                return true;
            }
        }
        return false;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public boolean isPatreonFile()
    {
        return isPatreonFile;
    }
    
    public void setIsPatreonFile(boolean isPatreonFile)
    {
        this.isPatreonFile = isPatreonFile;
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

    private String getShortName(String name)
    {
        String tags = "";
        if (name != null)
        {
            char[] chars = name.toCharArray();
            boolean in = true;
            char c;
            for (int i = 0; i < chars.length; i++)
            {
                c = chars[i];
                if (c == '[')
                {
                    if (in && i != 0)
                    {
                        tags = "";
                        break;
                    }
                    in = true;
                }
                else if (c == ']' || c == ')')
                {
                    in = false;
                }
                else if (!in && c != ' ')
                {
                    name = name.substring(i, name.length() - 1);
                    break;
                }
                tags += c;
            }
            if (!tags.endsWith(" "))
            {
                tags += " ";
            }
            if (!tags.contains("[") && !tags.contains("]"))
            {
                tags = "";
            }
            // Remove right tags
            int rightIndex = name.indexOf('[');
            if (rightIndex != -1)
            {
                name = name.substring(0, rightIndex);
            }
        }
        return tags + name;
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
                String hex = Integer.toHexString(0xFF & hash[i]);
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
        return shortName;
    }
}
