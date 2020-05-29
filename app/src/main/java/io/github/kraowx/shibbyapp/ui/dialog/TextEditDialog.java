package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class TextEditDialog extends AlertDialog
{
	private EditText input;
	
	public TextEditDialog(Context context, String title,
						  String hintText, Type size, int theme,
						  DialogInterface.OnClickListener positiveAction,
						  DialogInterface.OnClickListener negativeAction)
	{
		super(context, theme);
		setTitle(title);
		input = new EditText(getContext());
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		if (size == Type.MULTI_LINE)
		{
			input.setSingleLine(false);
			input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		}
		if (hintText != null)
		{
			input.setText(hintText);
			input.setSelection(hintText.length());
		}
		setView(input);
		setButton(AlertDialog.BUTTON_POSITIVE, "OK", positiveAction);
		setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", negativeAction);
	}
	
	public String getText()
	{
		return input.getText().toString();
	}
	
	public enum Type
	{
		SINGLE_LINE, MULTI_LINE
	}
}
