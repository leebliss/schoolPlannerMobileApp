package com.example.schoolplanner;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AssessmentDetail extends AppCompatActivity {

    TextView titleText;
    EditText name, contact, DOB;
    //TextView displayTermsText;
    Button addNew, update, delete;
    //for db connectivity
    DBHelper dbHelper;
    //for displaying data in a list
    ArrayList<String> listItem;
    ArrayAdapter adapter;
    ListView userList;
    //variable for holding index of selected item
    int currentlySelectedItem;
    //variable for holding name of selected item
    String nameOfSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set name for this activity in title bar
        titleText = findViewById(R.id.textTitle);
        titleText.setText("Assessment Details");
        //text input fields
        name = findViewById(R.id.termName);
        contact = findViewById(R.id.termStartDate);
        DOB = findViewById(R.id.termEndDate);
        //text views
        //displayTermsText = findViewById(R.id.displayTerms);
        //buttons
        addNew = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        delete = findViewById(R.id.btnDelete);
        //database connection
        dbHelper = new DBHelper(this);
        //for displaying data in a list
        listItem = new ArrayList<>();
        userList = findViewById(R.id.displayTermNames);
        //variables for holding index and name of selected items
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";

        //call local viewData method
        viewData();

        //set listeners
        //for clicking on list items
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                nameOfSelectedItem = userList.getItemAtPosition(i).toString();
                //highlight selected item
                parent.getChildAt(i).setBackgroundColor(Color.YELLOW);
                //reset previously selected item to transparent
                if (currentlySelectedItem != -1 && currentlySelectedItem != i){
                    parent.getChildAt(currentlySelectedItem).setBackgroundColor(Color.TRANSPARENT);
                }
                currentlySelectedItem = i;
                //toast item
                Toast.makeText(AssessmentDetail.this,""+nameOfSelectedItem, Toast.LENGTH_SHORT).show();
            }
        });
        /*
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
         */

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get rid of this method?
                //openAssessmentDetailActivity();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //parse name off of listItem
                String[] separated = nameOfSelectedItem.split("\n");
                Boolean checkDeleteData = dbHelper.deleteData(separated[0],"TermInfo");
                if(checkDeleteData) {
                    Toast.makeText(AssessmentDetail.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                    nameOfSelectedItem = "";
                    refreshList();
                }
                else{
                    if(nameOfSelectedItem == ""){
                        Toast.makeText(AssessmentDetail.this, "Please make a selection.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(AssessmentDetail.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //methods
    //refresh the list after making changes to data
    public void refreshList(){
        listItem.clear();
        viewData();
    }
    private void viewData(){
        Cursor cursor = dbHelper.viewData("what to put here?");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            while (cursor.moveToNext()) {
                listItem.add(cursor.getString(0)); //index 0 is name
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
        }
    }
    public void  openDialog(){
        AddAssessmentDialog addAssessmentDialog = new AddAssessmentDialog("holder", 0);
        addAssessmentDialog.show(getSupportFragmentManager(), "Add Assessment Dialog");
    }
    /*
    @Override
    public void applyTexts(String termName, String TermContact, String TermDOB) {

        Boolean checkInsertData = dbHelper.insertUserData(termName, TermContact, TermDOB);
        if(checkInsertData)
            Toast.makeText(AssessmentDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(AssessmentDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    */
}