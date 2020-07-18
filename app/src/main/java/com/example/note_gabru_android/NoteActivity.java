package com.example.note_gabru_android;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {
    GridView gridView;
    FloatingActionButton floatingActionButton;
    DataBaseHelper dataBaseHelper;
    String audioPath;
    int ccid;
    SearchView searchView;
    List<CategoryModel> filterList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notes");
     gridView = findViewById(R.id.gridView);

        floatingActionButton = findViewById(R.id.floatingActionButton);

        dataBaseHelper = new DataBaseHelper(this);
        searchView = findViewById(R.id.searchView);

        filterList = new ArrayList<>();

        loadNotes();

        final IconAdapter iconAdapter = new IconAdapter(this, CategoryModel.listNotes);
        gridView.setAdapter(iconAdapter);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesActivity.this,DescriptionActivity.class);
                startActivity(intent);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty()) {

                    if(!newText.isEmpty()){
                        filterList.clear();
                        for(int i = 0;i < CategoryModel.listNotes.size();i++){
                            CategoryModel categoryModelnote =  CategoryModel.listNotes.get(i);
                            if(categoryModelnote.title.contains(newText)){
                               filterList.add(categoryModelnote);
                            }
                        }

                        IconAdapter iconAdapter1 = new IconAdapter(NotesActivity.this,filterList);
                        gridView.setAdapter(iconAdapter1);
                    }

                    if(newText.isEmpty()){
                        IconAdapter iconAdapter1 = new IconAdapter(NotesActivity.this,CategoryModel.listNotes);
                        gridView.setAdapter(iconAdapter1);
                    }



                }
                return false;
            }
        });

