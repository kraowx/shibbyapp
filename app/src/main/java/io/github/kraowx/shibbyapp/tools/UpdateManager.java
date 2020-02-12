package io.github.kraowx.shibbyapp.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class UpdateManager
{
    private Version latest;
    
    public boolean updateAvailable(Context context, Version current)
    {
        JSONArray json = null;
        latest = new Version(null, false);
        try
        {
            String host = context.getString(R.string.version_host) + "/releases";
            json = new JSONArray(HttpRequest.get(host).body());
            if (json.length() == 0)
            {
                return false;
            }
            latest = new Version(json.getJSONObject(0));
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return latest.greater(current) && !latest.isPreRelease();
    }

    public void showUpdateMessage(final MainActivity mainActivity)
    {
        mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final Spanned updateMessage = Html.fromHtml(
                        "<h4>An update is available! " +
                        "Would you like to download it now?</h4>" +
                        latest.getUpdateMessage());
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mainActivity);
                boolean darkModeEnabled = prefs.getBoolean(
                        "darkMode", false);
                if (darkModeEnabled)
                {
                    Drawable icon = ContextCompat.getDrawable(mainActivity,
                            R.drawable.ic_update).mutate();
                    icon.setColorFilter(new ColorMatrixColorFilter(new float[]
                    {
                            -1, 0, 0, 0, 200,
                            0, -1, 0, 0, 200,
                            0, 0, -1, 0, 200,
                            0, 0, 0, 1, 0
                    }));
                    new AlertDialog.Builder(mainActivity, R.style.DialogThemeDark)
                            .setTitle("Update")
                            .setMessage(updateMessage)
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    update(mainActivity);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(icon)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Update")
                            .setMessage(updateMessage)
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            update(mainActivity);
                                        }
                                    })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(R.drawable.ic_update)
                            .show();
                }
            }
        });
    }

    private void update(Context context)
    {
        String repo = context.getString(R.string.github_repo);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo));
        context.startActivity(browserIntent);
    }
}
