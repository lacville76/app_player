package com.example.lacville76.aplikacja_mgr_player;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ListFileActivity extends ListActivity {
    private String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);
        // Use the current directory as title
        filePath = "/";
        if (getIntent().hasExtra("path")) {
            filePath = getIntent().getStringExtra("path");
        }
        setTitle(filePath);


        // Read all files sorted into the values-array
        ArrayList values = new ArrayList();
        File dir = new File(filePath);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        if (filePath.endsWith(File.separator)) {
            filename = filePath + filename;
        } else {
            filename = filePath + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);
        } else if (new File(filename).isFile()){

            Intent backToMainActivityIntent = new Intent(getBaseContext(), MainActivity.class);
            backToMainActivityIntent.putExtra("TRACK_FILEPATH", filename);
            startActivity(backToMainActivityIntent);
        }
    }
}
