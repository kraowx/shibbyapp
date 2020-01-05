package io.github.kraowx.shibbyapp.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response
{
    private ResponseType respType;
    private JSONArray data;

    public Response(ResponseType respType, JSONArray data)
    {
        this.respType = respType;
        this.data = data;
    }

    public static Response fromJSON(String json)
    {
        Response resp = new Response(null, null);
        try
        {
            JSONObject obj = new JSONObject(json);
            if (obj.has("type"))
            {
                resp.respType = formatResponseType(obj.getString("type"));
            }
            if (obj.has("data"))
            {
                resp.data = obj.getJSONArray("data");
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return resp;
    }

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("type", respType.toString());
            json.put("data", data);
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return json;
    }

    public ResponseType getType()
    {
        return respType;
    }
    
    public JSONArray getData()
    {
        return data;
    }

    private static ResponseType formatResponseType(String responseStr)
    {
        for (ResponseType type : ResponseType.values())
        {
            if (type.toString().equals(responseStr))
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
