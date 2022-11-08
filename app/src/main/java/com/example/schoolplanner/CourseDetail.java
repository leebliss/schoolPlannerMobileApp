package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;

public class CourseDetail extends AppCompatActivity implements AddAssessmentDialog.AddAssessmentDialogListener {

    //for passing values to another activity
    public static final String ASSESSMENT_ID = "com.example.schoolplanner.ASSESSMENT_ID";
    //TextView titleText;
    EditText titleText, professor, professorPhone, professorEmail;
    private TextView textViewStartDate, textViewEndDate;
    //for course status
    private RadioButton inProgressRadio, completedRadio, droppedRadio,planToTakeRadio;
    //for date picker
    private int mDate, mMonth, mYear;
    //TextView displayTermsText;
    // Button addNew, update, save, delete;
    //for bottom navigation menu
    private BottomNavigationView bottomNavigationView;
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
    //for holding courseID sent over in the intent
    int courseID;
    //for holding assessmentID to send over to assessment detail in the intent
    int assessmentID;
    //ref to mainlayout
    private ScrollView mainScrollView;

    String nameOfCourseSelected; //this is the course featured in this  detail
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

        //so editText layout elements do not trigger input keyboard on loading page
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        textViewStartDate = findViewById(R.id.courseStartDate);
        //String startHolder = "Start Date: "+courseStartDate;
        textViewStartDate.setText(courseStartDate);

        textViewEndDate = findViewById(R.id.courseEndDate);
        //String endHolder = "End Date: "+courseEndDate;
        textViewEndDate.setText(courseEndDate);

        //set radio buttons based on assessment type value from DB
        inProgressRadio = (RadioButton) findViewById(R.id.inProgressRadioButton);
        if(courseStatus.equals("in progress")){inProgressRadio.setChecked(true);}
        else{inProgressRadio.setChecked(false);}
        completedRadio = (RadioButton) findViewById(R.id.completedRadioButton);
        if(courseStatus.equals("completed")){completedRadio.setChecked(true);}
        else{completedRadio.setChecked(false);}
        droppedRadio = (RadioButton) findViewById(R.id.droppedRadioButton);
        if(courseStatus.equals("dropped")){droppedRadio.setChecked(true);}
        else{droppedRadio.setChecked(false);}
        planToTakeRadio = (RadioButton) findViewById(R.id.planToTakeRadioButton);
        if(courseStatus.equals("plan to take")){planToTakeRadio.setChecked(true);}
        else{planToTakeRadio.setChecked(false);}

        professor = findViewById(R.id.courseProfessor);
        //String professorHolder = "Professor: "+courseProfessor;
        professor.setText(courseProfessor);

        professorPhone = findViewById(R.id.courseProfessorPhone);
        //String professorPhoneHolder = "Professor Phone: "+courseProfessorPhone;
        professorPhone.setText(courseProfessorPhone);

        professorEmail = findViewById(R.id.courseProfessorEmail);
        //String professorEmailHolder = "Professor Email: "+courseProfessorEmail;
        professorEmail.setText(courseProfessorEmail);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //for entire screen to scroll
        mainScrollView = findViewById(R.id.mainScrollView);
        //mainScrollView.(new ScrollingMovementMethod());

        //buttons
        /*
        addNew = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        save = findViewById(R.id.btnSave);
        delete = findViewById(R.id.btnDelete);
         */

        //call local viewData method
        viewData();

        //set listeners
        //listener for start date
        textViewStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CourseDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(CourseDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewEndDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
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
                //get assessment name only
                String[] separated = nameOfSelectedItem.split("\n");
                assessmentNameOnly = separated[0];
                //pass assessment name to database to get assessment ID and assign to assessmentID variable
                Cursor cursor = dbHelper.getDataByName(assessmentNameOnly, "AssessmentInfo");
                if (cursor.getCount() == 0) {
                    Toast.makeText(CourseDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        assessmentID = cursor.getInt(0);
                    }
                    //toast item
                    Toast.makeText(CourseDetail.this, "" + assessmentNameOnly, Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                switch (item.getItemId()){
                    case R.id.addNew:
                        openDialog();
                        return true;
                    case R.id.open:
                        openAssessmentDetailActivity(assessmentID);
                        return true;

                    case R.id.delete:
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
                        return true;

                    case R.id.save:
                        //get values to save changes
                        String courseName = titleText.getText().toString();
                        String courseStart = textViewStartDate.getText().toString();
                        String courseEnd = textViewEndDate.getText().toString();
                        //change boolean radiobutton values to strings for storage
                        //for the inProgress radio
                        Boolean inProgressRadioState = inProgressRadio.isChecked();
                        String status = ""; //default
                        if(inProgressRadioState) {status = "in progress";}
                        //for the completed radio
                        Boolean completedRadioState = completedRadio.isChecked();
                        if(completedRadioState) {status = "completed";}
                        //for the dropped radio
                        Boolean droppedRadioState = droppedRadio.isChecked();
                        if(droppedRadioState) {status = "dropped";}
                        //for the planToTake radio
                        Boolean planToTakeRadioState = planToTakeRadio.isChecked();
                        if(planToTakeRadioState) {status = "plan to take";}
                        String courseProfessor = professor.getText().toString();
                        String courseProfessorPhone = professorPhone.getText().toString();
                        String courseProfessorEmail = professorEmail.getText().toString();

                        //save the changes
                        Boolean checkSavedChanges = dbHelper.updateCourseData(courseID,courseName,courseStart,courseEnd,status,courseProfessor,courseProfessorPhone,courseProfessorEmail);
                        //test for success
                        if(checkSavedChanges) {
                            Toast.makeText(CourseDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                        else{
                            Toast.makeText(CourseDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                        }
                        return true;

                    case R.id.home:
                        goHome();
                        return true;
                }
                return false;
            }
        });
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

                openAssessmentDetailActivity(assessmentID);
            }
        });

        //this is for saving a detail screen when done with changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get values to save changes
                String courseName = titleText.getText().toString();
                String courseStart = textViewStartDate.getText().toString();
                String courseEnd = textViewEndDate.getText().toString();
                //change boolean radiobutton values to strings for storage
                //for the inProgress radio
                Boolean inProgressRadioState = inProgressRadio.isChecked();
                String status = ""; //default
                if(inProgressRadioState) {status = "in progress";}
                //for the completed radio
                Boolean completedRadioState = completedRadio.isChecked();
                if(completedRadioState) {status = "completed";}
                //for the dropped radio
                Boolean droppedRadioState = droppedRadio.isChecked();
                if(droppedRadioState) {status = "dropped";}
                //for the planToTake radio
                Boolean planToTakeRadioState = planToTakeRadio.isChecked();
                if(planToTakeRadioState) {status = "plan to take";}
                String courseProfessor = professor.getText().toString();
                String courseProfessorPhone = professorPhone.getText().toString();
                String courseProfessorEmail = professorEmail.getText().toString();

                //save the changes
                Boolean checkSavedChanges = dbHelper.updateCourseData(courseID,courseName,courseStart,courseEnd,status,courseProfessor,courseProfessorPhone,courseProfessorEmail);
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
        */
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
        Cursor cursor = dbHelper.viewData("assessment");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //display name and start/end dates
            while (cursor.moveToNext()) {
                if(cursor.getInt(7)==courseID) { //index 7 is the courseID foreign key for AssessmentInfo DB
                    String nameAndDates = (cursor.getString(1)) + "\n" + (cursor.getString(2)) + " to " + (cursor.getString(3));
                    listItem.add(nameAndDates);
                    //listItem.add(cursor.getString(0)); //index 0 is name
                }
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(userList);
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
    public void openAssessmentDetailActivity(int ID){
        Intent intent =new Intent(this, AssessmentDetail.class);
        intent.putExtra(ASSESSMENT_ID, ID);
        startActivity(intent);
    }
    public void goHome(){
        Intent intent =new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}