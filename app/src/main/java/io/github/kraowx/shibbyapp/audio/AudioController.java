package io.github.kraowx.shibbyapp.audio;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class AudioController
{
    private AudioPlayerDialog dialog;
    private MainActivity mainActivity;

    public AudioController(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        dialog = new AudioPlayerDialog(mainActivity);
    }

    public void loadFile(ShibbyFile file)
    {
        dialog.loadFile(file);
    }

    public void setQueue(List<ShibbyFile> queue)
    {
        dialog.setQueue(queue);
    }

    public boolean isVisible()
    {
        return dialog.isShowing();
    }

    public void toggleVisible()
    {
        if (dialog.isShowing())
        {
            dialog.stopTimer();
            dialog.hide();
        }
        else
        {
            dialog.startTimer();
            dialog.show();
        }
    }

    public void setVisible(boolean visible)
    {
        if (visible && !dialog.isShowing())
        {
            dialog.startTimer();
            dialog.show();
        }
        else if (dialog.isShowing())
        {
            dialog.stopTimer();
            dialog.hide();
        }
    }
}
