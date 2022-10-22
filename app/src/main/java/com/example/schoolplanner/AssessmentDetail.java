package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class AssessmentDetail extends AppCompatActivity {

    EditText titleText, type;
    private Switch sAlert, eAlert;
    private TextView textViewStartDate, textViewEndDate;
    //for date picker
    private int mDate, mMonth, mYear;
    //TextView displayTermsText;
    Button save;
    //for db connectivity
    DBHelper dbHelper;

    ArrayAdapter adapter;
    //for holding index of selected item
    int currentlySelectedItem;
    //for parsing name of selected item
    String nameOfSelectedItem;
    String assessmentNameOnly;
    //for holding assessmentID sent over in the intent
    //int courseID;
    int assessmentID;
    String nameOfAssessmentSelected; //this is the assessment featured in this detail
    String assessmentStartDate;
    String assessmentEndDate;
    String assessmentType;
    String startAlert;
    String endAlert;

    //String courseProfessorPhone;
    //String courseProfessorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        //database connection
        dbHelper = new DBHelper(this);
        //for displaying data in a list
        //listItem = new ArrayList<>();
        //userList = findViewById(R.id.displayAssessmentNames);
        //variables for holding index and name of selected items
        //currentlySelectedItem = -1;
        //nameOfSelectedItem = "";
        assessmentNameOnly = "";

        //get course ID from the intent passed over from previous activity
        Intent intent = getIntent();
        //nameOfCourseSelected = intent.getStringExtra(TermDetail.COURSE_NAME);
        assessmentID = intent.getIntExtra(CourseDetail.ASSESSMENT_ID, 1000);

        //use assessmentID to get corresponding data
        Cursor cursor = dbHelper.getDataByID(assessmentID, "AssessmentInfo");
        if (cursor.getCount() == 0) {
            Toast.makeText(AssessmentDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                nameOfAssessmentSelected = cursor.getString(1); //this is the course featured in this term detail
                assessmentStartDate = cursor.getString(2);
                assessmentEndDate = cursor.getString(3);
                assessmentType = cursor.getString(4);
                startAlert = cursor.getString(5);
                endAlert = cursor.getString(6);
                //courseProfessorPhone = cursor.getString(6);
                //courseProfessorEmail = cursor.getString(7);
            }
            //toast item
            //Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
        }

        //set name for this activity in title bar using value passed over by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfAssessmentSelected);

        //set values in edit texts
        textViewStartDate = findViewById(R.id.assessmentStartDate);
        //String startHolder = "Start Date: "+courseStartDate;
        textViewStartDate.setText(assessmentStartDate);

        textViewEndDate = findViewById(R.id.assessmentEndDate);
        //String endHolder = "End Date: "+courseEndDate;
        textViewEndDate.setText(assessmentEndDate);

        type = findViewById(R.id.assessmentType);
        //String statusHolder = "Status: "+courseStatus;
        type.setText(assessmentType);
        //set value of start alert switch
        sAlert = (Switch) findViewById(R.id.setStartAlert);
        if(startAlert.equals("true")){sAlert.setChecked(true);}
        else{sAlert.setChecked(false);}
        //set value of end alert switch
        eAlert = (Switch) findViewById(R.id.setEndAlert);
        if(endAlert.equals("true")){eAlert.setChecked(true);}
        else{eAlert.setChecked(false);}

        /*
        sAlert = findViewById(R.id.setStartAlert);
        //String professorHolder = "Professor: "+courseProfessor;
        sAlert.setText(startAlert);

        eAlert = findViewById(R.id.setEndAlert);
        //String professorHolder = "Professor: "+courseProfessor;
        eAlert.setText(endAlert);
         */

        /*
        professorPhone = findViewById(R.id.courseProfessorPhone);
        //String professorPhoneHolder = "Professor Phone: "+courseProfessorPhone;
        professorPhone.setText(courseProfessorPhone);

        professorEmail = findViewById(R.id.courseProfessorEmail);
        //String professorEmailHolder = "Professor Email: "+courseProfessorEmail;
        professorEmail.setText(courseProfessorEmail);
        */

        //buttons
        //addNew = findViewById(R.id.btnInsert);
        //update = findViewById(R.id.btnUpdate);
        save = findViewById(R.id.btnSave);
        //delete = findViewById(R.id.btnDelete);

        //call local viewData method
        //viewData();

        //set listeners
        //listener for start date
        textViewStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AssessmentDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
        //listener for end date
        textViewEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AssessmentDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewEndDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
        /*
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
                Toast.makeText(AssessmentDetail.this,""+assessmentNameOnly, Toast.LENGTH_SHORT).show();
            }
        });

         */
    /*
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               // openAssessmentDetailActivity();
            }
        });

    */

        //this is for saving a detail screen when done with changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get values to save changes
                String assessmentName = titleText.getText().toString();
                String assessmentStart = textViewStartDate.getText().toString();
                String assessmentEnd = textViewEndDate.getText().toString();
                String assessmentType = type.getText().toString();
                //change boolean switch values to strings for storage
                //for a start alert
                Boolean switchState = sAlert.isChecked();
                String startAlert = "false"; //default
                if(switchState) {startAlert = "true";}
                //for an end alert
                switchState = eAlert.isChecked();
                String endAlert = "false"; //default
                if(switchState) {endAlert = "true";}
                //String startAlert = sAlert.getText().toString();
                //String endAlert = eAlert.getText().toString();
                //String courseProfessorPhone = professorPhone.getText().toString();
                //String courseProfessorEmail = professorEmail.getText().toString();

                //save the changes
                Boolean checkSavedChanges = dbHelper.updateAssessmentData(assessmentID,assessmentName,assessmentStart,assessmentEnd,assessmentType,startAlert,endAlert);
                //test for success
                if(checkSavedChanges) {
                    Toast.makeText(AssessmentDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                    //refreshList();
                }
                else{
                    Toast.makeText(AssessmentDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    /*
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parse name off of listItem
                String[] separated = nameOfSelectedItem.split("\n");
                Boolean checkDeleteData = dbHelper.deleteData(separated[0],"AssessmentInfo");
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

     */
    }
    /*
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
                if(cursor.getInt(7)==assessmentID) { //index 7 is the courseID foreign key for AssessmentInfo DB
                    String nameAndDates = (cursor.getString(1)) + "\n" + (cursor.getString(2)) + " to " + (cursor.getString(3));
                    listItem.add(nameAndDates);
                    //listItem.add(cursor.getString(0)); //index 0 is name
                }
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
        }
    }
    public void openDialog(){
        AddAssessmentDialog addAssessmentDialog = new AddAssessmentDialog(nameOfAssessmentSelected, assessmentID);
        addAssessmentDialog.show(getSupportFragmentManager(), "Add Assessment Dialog");
    }

     */
    /*
    @Override
    public void applyTexts(String assessmentName, String assessmentStartDate,String assessmentEndDate,String assessmentType,String assessmentStartAlert,String assessmentEndAlert,int courseID) {

        Boolean checkInsertData = dbHelper.insertUserData(assessmentName,assessmentStartDate,assessmentEndDate,assessmentType, assessmentStartAlert,assessmentEndAlert,courseID);
        if(checkInsertData)
            Toast.makeText(AssessmentDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(AssessmentDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }

     */
    /*
    public void openAssessmentDetailActivity(){
        Intent intent =new Intent(this, AssessmentDetail.class);
        startActivity(intent);
    }
     */
}