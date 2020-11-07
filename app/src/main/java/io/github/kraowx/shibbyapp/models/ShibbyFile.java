package io.github.kraowx.shibbyapp.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.kraowx.shibbyapp.tools.PatreonTier;

public class ShibbyFile
{
    public static final String DEFAULT_TIER = "free";
    
    private final String SHIBBYDEX_ROOT_URL = "https://shibbydex.com/";
    private final String SHIBBYDEX_FILE_URL = SHIBBYDEX_ROOT_URL + "file/";
    
    private String name;
    private String id;
    private int version;
    private PatreonTier tier;
    private long duration;
    private ShibbyBasicInfo basicInfo;
    private ShibbyAudioInfo audioInfo;
    private List<String> tags;
    private ShibbyHypnosisInfo hypnosisInfo;
    private List<String> triggers;
    private String description;
    
    public ShibbyFile(String name, String id,
                      String description, PatreonTier tier, long duration)
    {
        this.name = name;
        this.id = id;
        this.description = description;
        this.tier = tier;
        this.duration = duration;
    }

    public static ShibbyFile fromJSON(String jsonStr)
    {
        ShibbyFile file = new ShibbyFile(null, null,
                null, null, 0);
        try
        {
            JSONObject json = new JSONObject(jsonStr);
            file.name = json.has("name") ? json.getString("name") : null;
            file.id = json.has("id") ? json.getString("id") : null;
            file.version = json.has("version") ? json.getInt("version") : 0;
            file.tier = PatreonTier.fromString(json.has("tier") ? json.getString("tier") : DEFAULT_TIER);
            file.duration = json.has("duration") ? json.getLong("duration") : 0;
            file.basicInfo = json.has("basic_info") ?
                    ShibbyBasicInfo.fromJSON(json.getJSONObject("basic_info")) :
                    new ShibbyBasicInfo();
            file.audioInfo = json.has("audio_info") ?
                    ShibbyAudioInfo.fromJSON(json.getJSONObject("audio_info")) :
                    new ShibbyAudioInfo();
            file.tags = new ArrayList<String>();
            if (json.has("tags"))
            {
                for (int i = 0; i < json.getJSONArray("tags").length(); i++)
                {
                    file.tags.add((String) json.getJSONArray("tags").get(i));
                }
            }
            file.hypnosisInfo = json.has("hypnosis_info") ?
                    ShibbyHypnosisInfo.fromJSON(json.getJSONObject("hypnosis_info")) :
                    new ShibbyHypnosisInfo();
            file.triggers = new ArrayList<String>();
            if (json.has("triggers"))
            {
                for (int i = 0; i < json.getJSONArray("triggers").length(); i++)
                {
                    file.triggers.add((String) json.getJSONArray("triggers").get(i));
                }
            }
            file.description = json.getString("description");
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
            json.put("id", id);
            json.put("version", version);
            json.put("tier", tier);
            json.put("duration", duration);
            if (basicInfo != null) {
                json.put("basic_info", basicInfo.toJSON());
            }
            if (audioInfo != null) {
                json.put("audio_info", audioInfo.toJSON());
            }
            JSONArray tagsJson = new JSONArray();
            if (tags != null)
            {
                for (String tag : tags)
                {
                    tagsJson.put(tag);
                }
            }
            json.put("tags", tagsJson);
            if (hypnosisInfo != null)
            {
                json.put("hypnosis_info", hypnosisInfo.toJSON());
            }
            JSONArray triggersJson = new JSONArray();
            if (triggers != null)
            {
                for (String trigger : triggers)
                {
                    triggersJson.put(trigger);
                }
            }
            json.put("triggers", triggersJson);
            json.put("description", description);
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
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public int getVersion()
    {
        return version;
    }
    
    public void setVersion(int version)
    {
        this.version = version;
    }
    
    public PatreonTier getTier()
    {
        return tier;
    }
    
    public void setTier(PatreonTier tier)
    {
        this.tier = tier;
    }
    
    public long getDuration()
    {
        return duration;
    }
    
    public void setDuration(long duration)
    {
        this.duration = duration;
    }
    
    public String getFileUrl()
    {
        return SHIBBYDEX_FILE_URL + id + "?spoilers=1";
    }
    
    public ShibbyBasicInfo getBasicInfo() {
        return basicInfo;
    }
    
    public String getAuthor()
    {
        return basicInfo.getAuthor();
    }
    
    public String getArtist()
    {
        return basicInfo.getArtist();
    }
    
    public String getReleaseDate()
    {
        return basicInfo.getRelease();
    }
    
    public String getAudienceType()
    {
        return basicInfo.getAudience();
    }
    
    public String getTone()
    {
        return basicInfo.getTone();
    }
    
    public String getSetting()
    {
        return basicInfo.getSetting();
    }
    
    public String getConsentType()
    {
        return basicInfo.getConsent();
    }
    
    public String getDSType()
    {
        return basicInfo.getDS();
    }
    
    public String getOrgasm()
    {
        return basicInfo.getOrgasm();
    }
    
    public String getInstructions()
    {
        return basicInfo.getInstructions();
    }
    
    public String getIntendedEffect()
    {
        return basicInfo.getIntendedEffect();
    }
    
    public ShibbyAudioInfo getAudioInfo()
    {
        return audioInfo;
    }
    
    public String getAudioFileType()
    {
        return audioInfo.getFileType();
    }
    
    public String getAudioType()
    {
        return audioInfo.getAudioType();
    }
    
    public String getAudioURL()
    {
        return audioInfo.getAudioURL();
    }
    
    public String getFreeAudioURL()
    {
        return audioInfo.getFreeAudioURL();
    }
    
    public String getAudioEffects()
    {
        return audioInfo.getEffects();
    }
    
    public String getAudioBackground()
    {
        return audioInfo.getBackground();
    }
    
    public List<String> getTags()
    {
        return tags;
    }
    
    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }
    
    public boolean hasTag(String tag)
    {
        for (String sTag : tags)
        {
            if (sTag.toLowerCase().equals(tag.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }
    
    public ShibbyHypnosisInfo getHypnosisInfo()
    {
        return hypnosisInfo;
    }
    
    public String getHypnosisStyle()
    {
        return hypnosisInfo.getStyle();
    }
    
    public String getHypnosisLevel()
    {
        return hypnosisInfo.getLevel();
    }
    
    public String getHypnosisInduction()
    {
        return hypnosisInfo.getInduction();
    }
    
    public String getHypnosisDeepener()
    {
        return hypnosisInfo.getDeepener();
    }
    
    public String getHypnosisBody()
    {
        return hypnosisInfo.getBody();
    }
    
    public boolean hasWakener()
    {
        return hypnosisInfo.hasWakener();
    }
    
    public boolean hasAftercare()
    {
        return hypnosisInfo.hasAftercare();
    }
    
    public List<String> getTriggers()
    {
        return triggers;
    }
    
    public void setTriggers(List<String> triggers)
    {
        this.triggers = triggers;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @Override
    public String toString()
    {
        return name;
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
}
