package io.github.kraowx.shibbyapp.net;

import org.json.JSONException;
import org.json.JSONObject;

public class Request
{
    private RequestType reqType;
    private JSONObject data;

    public Request(RequestType reqType)
    {
        this.reqType = reqType;
    }
    
    public Request(RequestType reqType, JSONObject data)
    {
        this.reqType = reqType;
        this.data = data;
    }

    public static Request fromJSON(String json)
    {
        Request req = new Request(null);
        try
        {
            JSONObject obj = new JSONObject(json);
            if (obj.has("type"))
            {
                req.reqType = formatRequestType(obj.getString("type"));
            }
            if (obj.has("data"))
            {
                req.data = obj.getJSONObject("data");
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return req;
    }

    public static Request all()
    {
        return new Request(RequestType.ALL);
    }

    public static Request files()
    {
        return new Request(RequestType.FILES);
    }

    public static Request tags()
    {
        return new Request(RequestType.TAGS);
    }

    public static Request series()
    {
        return new Request(RequestType.SERIES);
    }
    
    public static Request patreonFiles(String email, String password)
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("email", email);
            obj.put("password", password);
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return new Request(RequestType.PATREON_FILES, obj);
    }

    public JSONObject toJSON()
    {
        return data;
    }

    public RequestType getType()
    {
        return reqType;
    }
    
    public JSONObject getData()
    {
        return data;
    }

    private static RequestType formatRequestType(String requestStr)
    {
        for (RequestType type : RequestType.values())
        {
            if (type.toString().equals(requestStr))
            {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return toJSON().toString();
    }
}
