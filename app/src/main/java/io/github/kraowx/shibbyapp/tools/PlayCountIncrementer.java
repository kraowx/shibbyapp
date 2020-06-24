package io.github.kraowx.shibbyapp.tools;

import android.content.Context;

import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class PlayCountIncrementer
{
	private boolean incrementing;
	private Timer resetTimer;
	
	public void increment(final ShibbyFile activeFile,
						  final Context context)
	{
		if (!incrementing)
		{
			incrementing = true;
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					PlayCountManager.incrementPlayCount(
							activeFile, context);
					reset();
				}
			}).start();
		}
	}
	
	private void reset()
	{
		resetTimer = new Timer();
		resetTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				incrementing = false;
				resetTimer.cancel();
				resetTimer.purge();
			}
		}, 3000);
	}
}
