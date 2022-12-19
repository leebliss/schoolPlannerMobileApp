package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import java.util.Calendar;

public class TermDetail extends AppCompatActivity implements AddCourseDialog.AddCourseDialogListener {

    //for passing values to another activity
    public static final String COURSE_ID = "com.example.schoolplanner.COURSE_ID";

    //static intent to be used for alarms so they can be cancelled, must have common intent
    static Intent courseAlarmIntent;
    //intent for alarms
    Intent intent;

    EditText titleText;
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageEndCalendar;
    //for date picker
    private int mDate, mMonth, mYear;
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

        //initialize alarmIntent, this is for course alarms
        courseAlarmIntent = new Intent(TermDetail.this, CourseReminderBroadcast.class);
        intent = courseAlarmIntent;

        //so editText layout elements do not trigger input keyboard on loading page
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
            //for testing
            //Toast.makeText(TermDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
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
        textViewStartDate = findViewById(R.id.termStartDate);
        imageStartCalendar = findViewById(R.id.termStartCalendar);
        String startHolder = termStartDate;
        textViewStartDate.setText(startHolder);
        textViewEndDate = findViewById(R.id.termEndDate);
        imageEndCalendar = findViewById(R.id.termEndCalendar);
        String endHolder = termEndDate;
        textViewEndDate.setText(endHolder);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //call local viewData method
        viewData();

        //set listeners
        //listener for start date
        imageStartCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(TermDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
        //listener for end date
        imageEndCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(TermDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
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
                    try {
                        parent.getChildAt(currentlySelectedItem).setBackgroundColor(Color.TRANSPARENT);
                    }
                    catch(Exception e){
                        //do nothing, just need this in case the last selected item has been deleted
                    }
                }
                currentlySelectedItem = i;
                //get course name only
                String[] separated = nameOfSelectedItem.split("\n");
                courseNameOnly = separated[0];
                //pass course name to database to get course ID and assign to courseID variable
                Cursor cursor = dbHelper.getDataByName(courseNameOnly, "CourseInfo");
                if (cursor.getCount() == 0) {
                    //for testing
                    //Toast.makeText(TermDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        courseID = cursor.getInt(0);
                    }
                    //for testing
                    //Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
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
                        if(courseID==0){
                            Toast.makeText(TermDetail.this, "ERROR: Please select a course.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        else {
                            openCourseDetailActivity(courseID);
                            return true;
                        }

                    case R.id.delete:
                        //parse name off of listItem
                        String[] separated = nameOfSelectedItem.split("\n");
                        if(nameOfSelectedItem == ""){
                            Toast.makeText(TermDetail.this, "ERROR: Please select a course.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            new AlertDialog.Builder(TermDetail.this)
                                    .setTitle("Delete Entry?")
                                    .setMessage("Are you sure you want to delete '"+separated[0]+"'?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //delete item
                                            Boolean checkDeleteData = dbHelper.deleteData(separated[0], "CourseInfo");
                                            if (checkDeleteData) {
                                                Toast.makeText(TermDetail.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                                                nameOfSelectedItem = "";
                                                refreshList();
                                            }
                                            else {
                                                Toast.makeText(TermDetail.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null) //does nothing
                                    .setIcon(R.drawable.ic_baseline_warning_24)
                                    .show();
                            }
                        return true;

                    case R.id.save:
                        //get values to save changes
                        String termName = titleText.getText().toString();
                        String termStart = textViewStartDate.getText().toString();
                        String termEnd = textViewEndDate.getText().toString();

                        //test for all fields being filled
                        if(termName.equals("") || termStart.equals("") || termEnd.equals("")){
                            Toast.makeText(TermDetail.this, "ERROR: All fields must be filled.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //parse start and end for comparison
                            String[] ssd = termStart.split("-");
                            String[] sed = termEnd.split("-");
                            Log.d("split values", ssd[2] + "," + sed[2]);

                            //compare start and end
                            boolean startLessThanEnd = false; //default
                            if (Integer.parseInt(ssd[2]) < (Integer.parseInt(sed[2]))) {
                                startLessThanEnd = true;
                            } else if (Integer.parseInt(ssd[2]) == (Integer.parseInt(sed[2]))
                                    && Integer.parseInt(ssd[0]) < (Integer.parseInt(sed[0]))) {
                                startLessThanEnd = true;
                            } else if (Integer.parseInt(ssd[2]) == (Integer.parseInt(sed[2]))
                                    && Integer.parseInt(ssd[0]) == (Integer.parseInt(sed[0]))
                                    && Integer.parseInt(ssd[1]) <= (Integer.parseInt(sed[1]))) {
                                startLessThanEnd = true;
                            }
                            //use startLessThanEnd to determine next
                            if (startLessThanEnd) {
                                //save the changes
                                Boolean checkSavedChanges = dbHelper.updateTermData(termID, termName, termStart, termEnd);
                                //test for success
                                if (checkSavedChanges) {
                                    Toast.makeText(TermDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                                    refreshList();
                                } else {
                                    Toast.makeText(TermDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            } else {
                                Toast.makeText(TermDetail.this, "ERROR: End is before Start.", Toast.LENGTH_SHORT).show();
                            }
                        }
                }
                return false;
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.back:
            finish();
            return(true);
        case R.id.home:
            goHome();
            return(true);
        case R.id.exit:
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    ////////////////////////////////////methods/////////////////////////////////////
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
                if(cursor.getInt(10)==termID) { //index 10 is the termID foreign key for CourseInfo DB
                    String nameAndDates = (cursor.getString(1)) + "\n" + (cursor.getString(2)) + " to " + (cursor.getString(3));
                    listItem.add(nameAndDates); //index 1 is the name
                }
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(userList);
        }
    }
    public void  openDialog(){
        AddCourseDialog addCourseDialog = new AddCourseDialog(nameOfTermSelected, termID);
        addCourseDialog.show(getSupportFragmentManager(), "Add Course Dialog");
    }
    @Override
    public void applyTexts(String courseName, String startDate, String endDate, String status, int startAlert, int endAlert, String professor, String phone, String email, int termID) {

        Boolean checkInsertData = dbHelper.insertUserData(courseName, startDate, endDate, status, startAlert, endAlert, professor, phone, email, termID);
        if(checkInsertData)
            Toast.makeText(TermDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(TermDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    public void openCourseDetailActivity(int ID){
        Intent intent =new Intent(this, CourseDetail.class);
        intent.putExtra(COURSE_ID, ID);
        startActivity(intent);
    }
    public void goHome(){
        Intent intent =new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}