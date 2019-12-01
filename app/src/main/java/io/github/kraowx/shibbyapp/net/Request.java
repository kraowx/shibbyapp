package io.github.kraowx.shibbyapp.net;

import org.json.JSONException;
import org.json.JSONObject;

public class Request
{
    private RequestType reqType;

    public Request(RequestType reqType)
    {
        this.reqType = reqType;
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

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("type", reqType.toString());
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return json;
    }

    public RequestType getType()
    {
        return reqType;
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
