package com.example.note_gabru_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    public static ArrayList<String> categoryName;
    public static int catPosition;
    Button addCategory;
    DataBaseHelper dataBaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Categories");
        listView = findViewById(R.id.category_list_view);
        addCategory = findViewById(R.id.btn_add_category);
        categoryName = new ArrayList<>();
        dataBaseHelper = new DataBaseHelper(this);
        final SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE);

        try {
            categoryName = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("cname",ObjectSerializer.serialize(new ArrayList<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,categoryName);
        listView.setAdapter(adapter);
