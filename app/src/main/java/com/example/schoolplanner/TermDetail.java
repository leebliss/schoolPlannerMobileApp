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

public class TermDetail extends AppCompatActivity implements AddCourseDialog.AddCourseDialogListener {

    //for passing values to another activity
    public static final String COURSE_ID = "com.example.schoolplanner.COURSE_ID";
    public static final String COURSE_NAME = "com.example.schoolplanner.COURSE_NAME";

    EditText titleText, startDate, endDate;
    //EditText name, contact, DOB;
    //TextView displayTermsText;
    Button addNew, update, delete, save;
    //for db connectivity
    DBHelper dbHelper;
    //for displaying data in a list
    ArrayList<String> listItem;
    ArrayAdapter adapter;
    ListView userList;
    //for holding index of selected item
    int currentlySelectedItem;
    //for parsing name of selected item
    String nameOfSelectedItem;
    String courseNameOnly;
    //for holding termID and corresponding values
    int termID;
    String nameOfTermSelected; //this is the term featured in this term detail
    String termStartDate;
    String termEndDate;
    //for holding courseID to pass to courseDetail activity
    int courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_detail);

        //database connection
        dbHelper = new DBHelper(this);
        //for displaying data in a list
        listItem = new ArrayList<>();
        userList = findViewById(R.id.displayCourseNames);
        //for holding index and name of selected items
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";
        courseNameOnly = "";
        //for holding course ID
        courseID = 0;

        //get term name from intent passed over from main activity
        Intent intent = getIntent();
        termID = intent.getIntExtra(MainActivity.TERM_ID,1000);
        //use termID to get other info from same row
        Cursor cursor = dbHelper.getDataByID(termID, "TermInfo");
        if (cursor.getCount() == 0) {
            Toast.makeText(TermDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                nameOfTermSelected = cursor.getString(1);
                termStartDate = cursor.getString(2);
                termEndDate = cursor.getString(3);
            }
        }

        //set name for this activity in title bar using value passed in by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfTermSelected);
        //set start date and end date for term in textviews
        startDate = findViewById(R.id.termStartDate);
        String startHolder = "Start Date: "+termStartDate;
        startDate.setText(startHolder);
        endDate = findViewById(R.id.termEndDate);
        String endHolder = "End Date: "+termEndDate;
        endDate.setText(endHolder);

        //text input fields
        //name = findViewById(R.id.termName);
        //contact = findViewById(R.id.termStartDate);
        //DOB = findViewById(R.id.termEndDate);
        //buttons
        addNew = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        save = findViewById(R.id.btnSave);
        delete = findViewById(R.id.btnDelete);

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
                //get course name only
                String[] separated = nameOfSelectedItem.split("\n");
                courseNameOnly = separated[0];
                //pass term name to database to get term ID and assign to termID variable
                Cursor cursor = dbHelper.getDataByName(courseNameOnly, "CourseInfo");
                if (cursor.getCount() == 0) {
                    Toast.makeText(TermDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        courseID = cursor.getInt(0);
                    }
                    //toast item
                    Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
                }
            }
        });

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openCourseDetailActivity(courseNameOnly, courseID);
            }
        });

        //this is for saving a detail screen when done with changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get values to save changes
                String termName = titleText.getText().toString();
                String termStart = startDate.getText().toString();
                String termEnd = endDate.getText().toString();
                //save the changes
                Boolean checkSavedChanges = dbHelper.updateTermData(termID,termName,termStart,termEnd);
                //test for success
                if(checkSavedChanges) {
                    Toast.makeText(TermDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                    refreshList();
                }
                else{
                    Toast.makeText(TermDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parse name off of listItem
                String[] separated = nameOfSelectedItem.split("\n");
                Boolean checkDeleteData = dbHelper.deleteData(separated[0],"CourseInfo");
                if(checkDeleteData) {
                    Toast.makeText(TermDetail.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                    nameOfSelectedItem = "";
                    refreshList();
                }
                else{
                    if(nameOfSelectedItem == ""){
                        Toast.makeText(TermDetail.this, "Please make a selection.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(TermDetail.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //this causes everything to reload so the data is up to date when back arrow is used
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    //methods
    //refresh the list after making changes to data
    public void refreshList(){
        listItem.clear();
        viewData();
    }
    private void viewData(){
        Cursor cursor = dbHelper.viewData("course");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //get name and start/end dates, add to list, also get course ID for passing to assessment detail activity
            while (cursor.moveToNext()) {
                //courseID = cursor.getInt(0);
                String nameAndDates = (cursor.getString(1))+ "\n"+(cursor.getString(2))+" to "+(cursor.getString(3));
                listItem.add(nameAndDates); //index 1 is the name
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
        }
    }
    public void  openDialog(){
        AddCourseDialog addCourseDialog = new AddCourseDialog(nameOfTermSelected, termID);
        addCourseDialog.show(getSupportFragmentManager(), "Add Course Dialog");
    }
    @Override
    public void applyTexts(String courseName, String startDate, String endDate, String status, String professor, String phone, String email, int termID) {

        Boolean checkInsertData = dbHelper.insertUserData(courseName, startDate, endDate, status, professor, phone, email, termID);
        if(checkInsertData)
            Toast.makeText(TermDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(TermDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    public void openCourseDetailActivity(String name, int ID){
        Intent intent =new Intent(this, CourseDetail.class);
        intent.putExtra(COURSE_ID, ID);
        intent.putExtra(COURSE_NAME, name);
        startActivity(intent);
    }
}