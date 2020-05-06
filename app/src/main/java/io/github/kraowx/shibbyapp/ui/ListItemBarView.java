package io.github.kraowx.shibbyapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/*
 * A small "bar" widget intended to be used to indicate the
 * type of a shibbyfile on its listview item.
 */
public class ListItemBarView extends View
{
	private int width = 0, height = 0;
	private final RectF BAR_BOUNDS = new RectF(0, 10, 15, 100);
	
	private String type;
	private Paint paint;
	
	public ListItemBarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initUI();
	}
	
	private void initUI()
	{
		paint = new Paint();
		type = "";
	}
	
	public void setType(String type)
	{
		this.type = type;
		updateDimensions();
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(width, height);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		paint.setColor(getTypeColor());
		if (paint.getColor() != 0)
		{
			paint.setStrokeWidth(3);
			canvas.drawRoundRect(BAR_BOUNDS, 50, 50, paint);
		}
	}
	
	private int getTypeColor()
	{
		if (type.equals("soundgasm"))
		{
			return Color.GREEN;
		}
		else if (type.equals("patreon"))
		{
			return Color.RED;
		}
		else if (type.equals("user"))
		{
			return Color.BLUE;
		}
		return 0;
	}
	
	private void updateDimensions()
	{
		if (getTypeColor() == 0)
		{
			width = 0;
			height = 0;
		}
		else
		{
			width = 50;
			height = 100;
		}
		setMeasuredDimension(width, height);
	}
}
