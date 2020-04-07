package io.github.kraowx.shibbyapp.tools;

import android.content.Context;
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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.RequestType;
import io.github.kraowx.shibbyapp.net.Response;
import io.github.kraowx.shibbyapp.net.ResponseType;

public class DataManager
{
    final int MAX_UPDATE_TIME = 10*60*1000; // 10 minutes
    final int SHIBBY_CAMPAIGN_ID = 322138; // Shibby's unique Patreon campaign ID

    private MainActivity mainActivity;
    private SharedPreferences prefs;

    public DataManager(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    }
    
    private enum ResponseCode
    {
        SUCCESS, FAILED, ERROR
    }
    
    private enum PatreonResponseCode
    {
        SUCCESS, INVALID_LOGIN, NO_LOGIN,
        EMAIL_VERIFICATION_REQUIRED, TOO_MANY_REQUESTS
    }

    @Deprecated
    public boolean needsUpdate()
    {
        long lastUpdate = prefs.getLong("lastUpdate", 0);
        long currentTime = Calendar.getInstance().getTime().getTime();
        if (currentTime-lastUpdate > MAX_UPDATE_TIME)
        {
            return true;
        }
        return false;
    }
    
    public List<ShibbyFile> getPatreonFiles()
    {
        List<ShibbyFile> patreonFiles = new ArrayList<ShibbyFile>();
        JSONArray filesjson = null;
        try
        {
            filesjson = new JSONArray(prefs.getString(
                    "patreonFiles", "[]"));
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
                    patreonFiles.add(ShibbyFile.fromJSON(
                            filesjson.getJSONObject(i).toString()));
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
        }
        return patreonFiles;
    }

    public List<ShibbyFile> getUserFiles()
    {
        List<ShibbyFile> files = new ArrayList<ShibbyFile>();
        JSONArray userFiles;
        try
        {
            userFiles = new JSONArray(prefs.getString("userFiles", "[]"));
            for (int i = 0; i < userFiles.length(); i++)
            {
                JSONObject json = userFiles.getJSONObject(i);
                ShibbyFile file = ShibbyFile.fromJSON(json.toString());
                file.setType("user");
                JSONArray tagsJson = json.getJSONArray("tags");
                List<String> tags = new ArrayList<String>();
                for (int j = 0; j < tagsJson.length(); j++)
                {
                    tags.add(tagsJson.getString(j));
                }
                file.setTags(tags);
                files.add(file);
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return files;
    }

    public boolean addUserFile(ShibbyFile file)
    {
        List<ShibbyFile> userFiles = getUserFiles();
        if (!listHasFile(userFiles, file))
        {
            JSONArray arr;
            try
            {
                arr = new JSONArray(prefs.getString("userFiles", "[]"));
                JSONObject fileJson = file.toJSON();
                arr.put(fileJson);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userFiles", arr.toString());
                editor.commit();
                return true;
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return false;
    }
    
    public boolean removeUserFile(ShibbyFile file)
    {
        List<ShibbyFile> userFiles = getUserFiles();
        if (listHasFile(userFiles, file))
        {
            JSONArray arr;
            try
            {
                arr = new JSONArray(prefs.getString("userFiles", "[]"));
                for (int i = 0; i < arr.length(); i++)
                {
                    String idI = arr.getJSONObject(i).getString("id");
                    String idF = file.getId();
                    if (idI.equals(idF))
                    {
                        arr.remove(i);
                    }
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userFiles", arr.toString());
                editor.commit();
                return true;
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return false;
    }

    public List<ShibbyFile> getFiles()
    {
        List<ShibbyFile> files = new ArrayList<ShibbyFile>();
        JSONArray filesjson = null;
        try
        {
            filesjson = new JSONArray(prefs.getString("files", "[]"));
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
                    files.add(ShibbyFile.fromJSON(
                            filesjson.getJSONObject(i).toString()));
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
        }
        List<ShibbyFile> userFiles = getUserFiles();
        for (ShibbyFile file : userFiles)
        {
            files.add(file);
        }
        List<ShibbyFile> patreonFiles = getPatreonFiles();
        for (ShibbyFile file : patreonFiles)
        {
            files.add(file);
        }
        return files;
    }

    public List<ShibbyFileArray> getTags()
    {
        List<ShibbyFileArray> arr = new ArrayList<ShibbyFileArray>();
        JSONArray filesjson = null;
        try
        {
            filesjson = new JSONArray(prefs.getString("tags", ""));
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        if (filesjson != null)
        {
            List<ShibbyFile> files = getFiles();
            for (int i = 0; i < filesjson.length(); i++)
            {
                try
                {
                    JSONObject tagjson = filesjson.getJSONObject(i);
                    String tagname = tagjson.getString("name");
                    JSONArray tagfiles = tagjson.getJSONArray("files");
                    List<ShibbyFile> taglist = new ArrayList<ShibbyFile>();
                    for (int j = 0; j < tagfiles.length(); j++)
                    {
                        for (ShibbyFile file : files)
                        {
                            if (file.getId().equals(tagfiles.getString(j)))
                            {
                                taglist.add(file);
                            }
                        }
                    }
                    arr.add(new ShibbyFileArray(tagname, taglist.toArray(
                            new ShibbyFile[]{}), null));
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
        }
        return arr;
    }

    public List<ShibbyFileArray> getSeries()
    {
        return getShibbyArray("series");
    }

    private List<ShibbyFileArray> getShibbyArray(String key)
    {
        List<ShibbyFileArray> arr = new ArrayList<ShibbyFileArray>();
        JSONArray filesjson = null;
        try
        {
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

    @Deprecated
    public void loadAllData()
    {
        try
        {
            String[] server = prefs.getString("server",
                    "shibbyserver.ddns.net:2012").split(":");
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
                InetSocketAddress addr = new InetSocketAddress(hostname, port);
                Socket socket = new Socket();
                socket.connect(addr, 5000);
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
                        if (resp.getType() == ResponseType.ALL)
                        {
                            JSONObject rawjson = resp.toJSON();
                            JSONArray dataArr = rawjson.getJSONArray("data");
                            JSONArray files = dataArr.getJSONObject(0)
                                    .getJSONArray("files");
                            JSONArray tags = dataArr.getJSONObject(0)
                                    .getJSONArray("tags");
                            JSONArray series = dataArr.getJSONObject(0)
                                    .getJSONArray("series");
                            long time = Calendar.getInstance().getTime().getTime();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("files", files.toString());
                            editor.putString("tags", tags.toString());
                            editor.putString("series", series.toString());
                            editor.putLong("lastUpdate", time);
                            editor.commit();
                            showToast("Data updated");
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
        catch (SocketTimeoutException ste)
        {
            ste.printStackTrace();
            showToast("Timed out while connecting to server");
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            showToast("Server connection error");
        }
    }
    
    private String getFileUrl(String postId, String attachmentId)
    {
        return "https://patreon.com/file?h=" + postId + "&i=" + attachmentId;
    }
    
    private boolean isShibbyAudioPost(JSONObject post)
    {
        try
        {
            if (post.getJSONObject("relationships").getJSONObject("campaign")
                    .getJSONObject("data").getString("id").equals(SHIBBY_CAMPAIGN_ID + ""))
            {
                JSONArray tags = post.getJSONObject("relationships")
                        .getJSONObject("user_defined_tags").getJSONArray("data");
                if (tags.length() < 3)
                {
                    return false;
                }
                for (int i = 0; i < tags.length(); i++)
                {
                    if (tags.getJSONObject(i).getString("id")
                            .toLowerCase().equals("user_defined;live event") ||
                            tags.getJSONObject(i).getString("id")
                                    .toLowerCase().equals("user_defined;meta"))
                    {
                        return false;
                    }
                }
            }
            return post.getJSONObject("relationships").has("attachments");
            /*return true;*/
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return false;
    }
    
    private List<String> getLinks(JSONObject post)
    {
        List<String> links = new ArrayList<String>();
        try
        {
            JSONArray linkData = post.getJSONObject("relationships")
                    .getJSONObject("attachments").getJSONArray("data");
            for (int i = 0; i < linkData.length(); i++)
            {
                links.add(getFileUrl(post.getString("id"),
                        linkData.getJSONObject(i).getString("id")));
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return links;
    }
    
    private List<String> parseTags(String content)
    {
        List<String> tags = new ArrayList<String>();
        if (content != null)
        {
            String tag = "";
            boolean inStr = false;
            char c;
            for (int i = 0; i < content.length(); i++)
            {
                c = content.charAt(i);
                if (c == ']')
                {
                    tags.add(tag);
                    tag = "";
                    inStr = false;
                }
                else if (c == '[')
                {
                    inStr = true;
                }
                else if (c != '[' && inStr)
                {
                    tag += c;
                }
            }
        }
        return tags;
    }
    
    private List<ShibbyFile> generatePatreonData()
    {
        PatreonSessionManager patreonSessionManager =
                mainActivity.getPatreonSessionManager();
        JSONObject data = null, post = null, file = null;
        JSONArray posts = null;
        String name, content, date;
        List<String> links, tags;
        List<ShibbyFile> files = new ArrayList<ShibbyFile>();
        HttpRequest req = HttpRequest.get("https://api.patreon.com/campaigns/322138/posts");
        patreonSessionManager.addHeadersToRequest(req);
        try
        {
            data = new JSONObject(req.body());
            posts = data.getJSONArray("data");
            for (int i = 0; i < posts.length(); i++)
            {
                post = posts.getJSONObject(i);
                name = post.getJSONObject("attributes").getString("title");
                System.out.println(name);
                if (name != null && isShibbyAudioPost(post))
                {
                    links = getLinks(post);
                    content = post.getJSONObject("attributes").getString("content");
                    tags = parseTags(content);
                    date = post.getJSONObject("attributes").getString("created_at")
                            .split("T")[0];
                    if (links.size() == 1)
                    {
                        file = new JSONObject();
                        file.put("name", name);
                        file.put("links", links);
                        file.put("description", content);
                        file.put("tags", tags);
                        file.put("isPatreonFile", true);
                        file.put("type", "patreon");
                        file.put("date", date);
                        files.add(ShibbyFile.fromJSON(file.toString()));
                    }
                }
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return files;
    }
    
    public PatreonResponseCode requestPatreonData()
    {
        List<ShibbyFile> files = generatePatreonData();
        System.out.println(files);
        PatreonSessionManager patreonSessionManager =
                mainActivity.getPatreonSessionManager();
        final String patreonEmail = prefs.getString(
                "patreonEmail", null);
        final String patreonPassword = prefs.getString(
                "patreonPassword", null);
        if (patreonEmail != null && patreonPassword != null)
        {
            final int INVALID = 0;
            final int VALID = 1;
            final int EMAIL = 2;
            final int OVERLOAD = 3;
            switch (patreonSessionManager.verifyCredentials(
                    patreonEmail, patreonPassword))
            {
                case INVALID:
                    return PatreonResponseCode.INVALID_LOGIN;
                case VALID:
                    List<ShibbyFile> data = generatePatreonData();
                    System.out.println(data);
                    
                    return PatreonResponseCode.SUCCESS;
                case EMAIL:
                    return PatreonResponseCode.EMAIL_VERIFICATION_REQUIRED;
                case OVERLOAD:
                    return PatreonResponseCode.TOO_MANY_REQUESTS;
            }
        }
        return PatreonResponseCode.NO_LOGIN;
    }
    
    public void requestData(Request request)
    {
        String url = getURL(request);
        HttpRequest httpreq = null;
        try
        {
            httpreq = HttpRequest.get(url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (httpreq != null)
        {
            String body = httpreq.body();
            httpreq.closeOutputQuietly();
            if (body != null)
            {
                Response resp = Response.fromJSON(body);
                switch (handleResponse(resp))
                {
                    case SUCCESS:
                        showToast("Data updated");
                        return;
                    case ERROR:
                        return;
                }
            }
        }
    }
    
    private String getURL(Request request)
    {
        String url = getServer();
        url += "?version=" + mainActivity.getVersionName().substring(1);
        String type = request.getType().toString().toLowerCase();
        url += "&type=" + type;
        if (type != null)
        {
            if (type.equals("all") || type.equals("tags") ||
                    type.equals("patreon_files") ||
                    type.equals("verify_patreon_account"))
            {
                JSONObject data = request.getData();
                if (data != null)
                {
                    try
                    {
                        url += "&email=" + data.getString("email");
                        url += "&password=" + data.getString("password");
                    }
                    catch (JSONException je)
                    {
                        je.printStackTrace();
                    }
                }
            }
        }
        return url;
    }
    
    private String getServer()
    {
        String server = prefs.getString("server",
                mainActivity.getString(R.string.main_server));
        String hostname = server.substring(0, server.lastIndexOf(':'));
        String portstr = server.substring(server.lastIndexOf(':')+1, server.length());
        int port = -1;
        if (server.indexOf(':') == server.lastIndexOf(':') &&
                (server.contains("http://") || server.contains("https://")))
        {
            hostname = server;
        }
        else
        {
            try
            {
                port = Integer.parseInt(portstr);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return port == -1 ? hostname :
                prefs.getString("server",
                        mainActivity.getString(R.string.main_server));
    }
    
    private ResponseCode handleResponse(Response response)
    {
        ResponseCode code = ResponseCode.FAILED;
        try
        {
            SharedPreferences.Editor editor = prefs.edit();
            JSONObject rawjson = response.toJSON();
            if (response.getType() == ResponseType.ALL)
            {
                JSONObject data = rawjson.getJSONObject("data");
                JSONArray files = data.getJSONArray("files");
                JSONArray tags = data.getJSONArray("tags");
                JSONArray series = data.getJSONArray("series");
                JSONArray patreonFiles = null;
                if (data.has("patreonFiles"))
                {
                    patreonFiles = data.getJSONArray("patreonFiles");
                }
                editor.putString("files", files.toString());
                editor.putString("tags", tags.toString());
                editor.putString("series", series.toString());
                if (patreonFiles != null)
                {
                    editor.putString("patreonFiles", patreonFiles.toString());
                }
                code = ResponseCode.SUCCESS;
            }
            else if (response.getType() == ResponseType.FILES)
            {
                JSONArray data = rawjson.getJSONArray("data");
                editor.putString("files", data.toString());
                code = ResponseCode.SUCCESS;
            }
            else if (response.getType() == ResponseType.TAGS)
            {
                JSONArray data = rawjson.getJSONArray("data");
                editor.putString("tags", data.toString());
                code = ResponseCode.SUCCESS;
            }
            else if (response.getType() == ResponseType.SERIES)
            {
                JSONArray data = rawjson.getJSONArray("data");
                editor.putString("series", data.toString());
                code = ResponseCode.SUCCESS;
            }
            else if (response.getType() == ResponseType.PATREON_FILES)
            {
                JSONArray data = rawjson.getJSONArray("data");
                editor.putString("patreonFiles", data.toString());
                code = ResponseCode.SUCCESS;
            }
            else if (response.getType() == ResponseType.FEATURE_NOT_SUPPORTED)
            {
                showToast("Error: Current server does not support " +
                        "Patreon data (or it is disabled)");
                code = ResponseCode.ERROR;
            }
            else if (response.getType() == ResponseType.TOO_MANY_REQUESTS)
            {
                showToast("Error: Too many Patreon server requests");
                code = ResponseCode.ERROR;
            }
            else if (response.getType() == ResponseType.VERIFY_PATREON_ACCOUNT)
            {
                showToast("Error: Email verification is required");
                code = ResponseCode.ERROR;
            }
            else if (response.getType() == ResponseType.BAD_ACCOUNT)
            {
                showToast("Error: Patreon account is invalid");
                code = ResponseCode.ERROR;
            }
            else if (response.getType() == ResponseType.OUTDATED_CLIENT)
            {
                showToast("Error: You must update the app in order " +
                        "to connect to this server");
            }
            if (code == ResponseCode.SUCCESS)
            {
                long time = Calendar.getInstance().getTime().getTime();
                editor.putLong("lastUpdate", time);
                editor.commit();
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return code;
    }

    private boolean listHasFile(List<ShibbyFile> list, ShibbyFile file)
    {
        String id = file.getId();
        for (ShibbyFile fileX : list)
        {
            String idX = fileX.getId();
            if (idX != null && id != null && id.equals(idX))
            {
                return true;
            }
        }
        return false;
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
