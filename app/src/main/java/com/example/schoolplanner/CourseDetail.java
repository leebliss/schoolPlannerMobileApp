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

public class CourseDetail extends AppCompatActivity implements AddAssessmentDialog.AddAssessmentDialogListener {

    //TextView titleText;
    EditText titleText, startDate, endDate, status, professor, professorPhone, professorEmail;
    //TextView displayTermsText;
    Button addNew, update, save, delete;
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
    String assessmentNameOnly;
    //for holding courseID sent over in the intent and corresponding values
    int courseID;
    String nameOfCourseSelected; //this is the course featured in this term detail
    String courseStartDate;
    String courseEndDate;
    String courseStatus;
    String courseProfessor;
    String courseProfessorPhone;
    String courseProfessorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        //database connection
        dbHelper = new DBHelper(this);
        //for displaying data in a list
        listItem = new ArrayList<>();
        userList = findViewById(R.id.displayAssessmentNames);
        //variables for holding index and name of selected items
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";
        assessmentNameOnly = "";

        //get course ID from the intent passed over from previous activity
        Intent intent = getIntent();
        //nameOfCourseSelected = intent.getStringExtra(TermDetail.COURSE_NAME);
        courseID = intent.getIntExtra(TermDetail.COURSE_ID, 1000);

        //use courseID to get corresponding data
        Cursor cursor = dbHelper.getDataByID(courseID, "CourseInfo");
        if (cursor.getCount() == 0) {
            Toast.makeText(CourseDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                nameOfCourseSelected = cursor.getString(1); //this is the course featured in this term detail
                courseStartDate = cursor.getString(2);
                courseEndDate = cursor.getString(3);
                courseStatus = cursor.getString(4);
                courseProfessor = cursor.getString(5);
                courseProfessorPhone = cursor.getString(6);
                courseProfessorEmail = cursor.getString(7);
            }
            //toast item
            //Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
        }

        //set name for this activity in title bar using value passed over by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfCourseSelected);

        //set values in edit texts
        startDate = findViewById(R.id.courseStartDate);
        String startHolder = "Start Date: "+courseStartDate;
        startDate.setText(startHolder);

        endDate = findViewById(R.id.courseEndDate);
        String endHolder = "End Date: "+courseEndDate;
        endDate.setText(endHolder);

        status = findViewById(R.id.courseStatus);
        String statusHolder = "Status: "+courseStatus;
        status.setText(statusHolder);

        professor = findViewById(R.id.courseProfessor);
        String professorHolder = "Professor: "+courseProfessor;
        professor.setText(professorHolder);

        professorPhone = findViewById(R.id.courseProfessorPhone);
        String professorPhoneHolder = "Professor Phone: "+courseProfessorPhone;
        professorPhone.setText(professorPhoneHolder);

        professorEmail = findViewById(R.id.courseProfessorEmail);
        String professorEmailHolder = "Professor Email: "+courseProfessorEmail;
        professorEmail.setText(professorEmailHolder);

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
                assessmentNameOnly = separated[0];
                //toast item
                Toast.makeText(CourseDetail.this,""+assessmentNameOnly, Toast.LENGTH_SHORT).show();
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

                openAssessmentDetailActivity();
            }
        });

        //this is for saving a detail screen when done with changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get values to save changes
                String courseName = titleText.getText().toString();
                String courseStart = startDate.getText().toString();
                String courseEnd = endDate.getText().toString();
                String courseStatus = status.getText().toString();
                String courseProfessor = professor.getText().toString();
                String courseProfessorPhone = professorPhone.getText().toString();
                String courseProfessorEmail = professorEmail.getText().toString();

                //save the changes
                Boolean checkSavedChanges = dbHelper.updateCourseData(courseID,courseName,courseStart,courseEnd,courseStatus,courseProfessor,courseProfessorPhone,courseProfessorEmail);
                //test for success
                if(checkSavedChanges) {
                    Toast.makeText(CourseDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                    refreshList();
                }
                else{
                    Toast.makeText(CourseDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parse name off of listItem
                String[] separated = nameOfSelectedItem.split("\n");
                Boolean checkDeleteData = dbHelper.deleteData(separated[0],"AssessmentInfo");
                if(checkDeleteData) {
                    Toast.makeText(CourseDetail.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                    nameOfSelectedItem = "";
                    refreshList();
                }
                else{
                    if(nameOfSelectedItem == ""){
                        Toast.makeText(CourseDetail.this, "Please make a selection.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CourseDetail.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
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
        Cursor cursor = dbHelper.viewData("assessment");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //display name and start/end dates
            while (cursor.moveToNext()) {
                String nameAndDates = (cursor.getString(1))+ "\n"+(cursor.getString(2))+" to "+(cursor.getString(3));
                listItem.add(nameAndDates); //index 0 is name
                //listItem.add(cursor.getString(0)); //index 0 is name
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
        }
    }
    public void openDialog(){
        AddAssessmentDialog addAssessmentDialog = new AddAssessmentDialog(nameOfCourseSelected, courseID);
        addAssessmentDialog.show(getSupportFragmentManager(), "Add Assessment Dialog");
    }
    @Override
    public void applyTexts(String assessmentName, String assessmentStartDate,String assessmentEndDate,String assessmentType,String assessmentStartAlert,String assessmentEndAlert,int courseID) {

        Boolean checkInsertData = dbHelper.insertUserData(assessmentName,assessmentStartDate,assessmentEndDate,assessmentType, assessmentStartAlert,assessmentEndAlert,courseID);
        if(checkInsertData)
            Toast.makeText(CourseDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(CourseDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    public void openAssessmentDetailActivity(){
        Intent intent =new Intent(this, AssessmentDetail.class);
        startActivity(intent);
    }
}