package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.DataManager;

public class ImportFileDialog extends Dialog
{
    private File selectedFile;

    public ImportFileDialog(MainActivity mainActivity)
    {
        super(mainActivity);
        init(mainActivity);
    }

    private void init(final MainActivity mainActivity)
    {
        File dir = Environment.getExternalStorageDirectory();
        File[] dirs = dir.listFiles();
        if (dirs == null)
        {
            Toast.makeText(mainActivity, "Enable the \"Storage\" " +
                    "permission to use this feature", Toast.LENGTH_LONG).show();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.import_file_dialog);
        setTitle("Import File");
        final TextView txtFile = findViewById(R.id.txtFile);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSelectFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FileChooserDialog fileChooser = new FileChooserDialog(mainActivity);
                fileChooser.setFileListener(new FileChooserDialog.FileSelectedListener()
                {
                    @Override
                    public void fileSelected(File file)
                    {
                        selectedFile = file;
                        txtFile.setText(file.getName());
                    }
                });
                fileChooser.showDialog();
            }
        });
        final EditText txtName = findViewById(R.id.txtName);
        final EditText txtDescription = findViewById(R.id.txtDescription);
        final EditText txtTags = findViewById(R.id.txtTags);
        Button btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = txtName.getText().toString();
                String desc = txtDescription.getText().toString();
                String tags = txtTags.getText().toString();
                if (selectedFile != null)
                {
                    List<String> tagsList = new ArrayList<String>();
                    String[] tagsArr = tags.split(",");
                    for (String tag : tagsArr)
                    {
                        tagsList.add(tag.trim());
                    }
                    ShibbyFile file = new ShibbyFile(name,
                            selectedFile.getAbsolutePath(), desc, "user");
                    file.setTags(tagsList);
                    DataManager dataManager = new DataManager(mainActivity);
                    if (dataManager.addUserFile(file))
                    {
                        Toast.makeText(mainActivity, "File imported",
                                Toast.LENGTH_LONG).show();
                        ImportFileDialog.this.dismiss();
                    }
                    else
                    {
                        Toast.makeText(mainActivity, "error: file already exists",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(mainActivity, "You must first select a file",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        show();
    }
}
