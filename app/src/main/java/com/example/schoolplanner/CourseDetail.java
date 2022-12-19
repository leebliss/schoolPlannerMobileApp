package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class CourseDetail extends AppCompatActivity implements AddAssessmentDialog.AddAssessmentDialogListener,AddNoteDialog.AddNoteDialogListener  {

    //for passing values to another activity
    public static final String ASSESSMENT_ID = "com.example.schoolplanner.ASSESSMENT_ID";
    public static final String COURSE_NOTIFICATION_INFO = "com.example.schoolplanner.COURSE_DIALOG_NOTIFICATION_INFO";

    String notificationInfo;
    //static intent to be used for alarms so they can be cancelled, must have common intent
    static Intent assessmentAlarmIntent;
    //intent for alarms
    Intent intent;
    //context for this activity
    Context context;

    EditText titleText, professor, professorPhone, professorEmail;
    ImageView addNoteImage;
    //start and end dates
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageEndCalendar;
    //for holding dates and times for reminders
    private int startDate, startMonth,startYear, startHour, startMinute;
    private int endDate, endMonth,endYear, endHour, endMinute;
    //for course status
    private RadioButton inProgressRadio, completedRadio, droppedRadio,planToTakeRadio;
    //for choosing alerts on or off
    private Switch sAlert, eAlert;
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
    int startAlert, endAlert;
    String courseProfessor;
    String courseProfessorPhone;
    String courseProfessorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        //initialize alarmIntent, this is for assessment alarms
        assessmentAlarmIntent = new Intent(CourseDetail.this, AssessmentReminderBroadcast.class);
        //set the local intent to the term detail static intent, this way they can be deleted from term detail
        intent = TermDetail.courseAlarmIntent;
        //so editText layout elements do not trigger input keyboard on loading page
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //database connection
        dbHelper = new DBHelper(this);
        //set context
        context = getApplicationContext();
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
            //Toast.makeText(CourseDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                nameOfCourseSelected = cursor.getString(1); //this is the course featured in this term detail
                courseStartDate = cursor.getString(2);
                courseEndDate = cursor.getString(3);
                courseStatus = cursor.getString(4);
                startAlert = cursor.getInt(5);
                endAlert = cursor.getInt(6);
                courseProfessor = cursor.getString(7);
                courseProfessorPhone = cursor.getString(8);
                courseProfessorEmail = cursor.getString(9);
            }
            //for testing
            //Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
        }

        //set name for this activity in title bar using value passed over by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfCourseSelected);
        //image button for opening note dialog
        addNoteImage = findViewById(R.id.courseNoteImage);
        //set values in edit texts
        textViewStartDate = findViewById(R.id.courseStartDate);
        imageStartCalendar = findViewById(R.id.courseStartCalendar);
        textViewStartDate.setText(courseStartDate);
        textViewEndDate = findViewById(R.id.courseEndDate);
        imageEndCalendar = findViewById(R.id.courseEndCalendar);
        textViewEndDate.setText(courseEndDate);
        //set radio buttons from DB
        inProgressRadio = (RadioButton)findViewById(R.id.inProgressRadioButton);
        if(courseStatus.equals("in progress")){inProgressRadio.setChecked(true);}
        else{inProgressRadio.setChecked(false);}
        completedRadio = (RadioButton)findViewById(R.id.completedRadioButton);
        if(courseStatus.equals("completed")){completedRadio.setChecked(true);}
        else{completedRadio.setChecked(false);}
        droppedRadio = (RadioButton)findViewById(R.id.droppedRadioButton);
        if(courseStatus.equals("dropped")){droppedRadio.setChecked(true);}
        else{droppedRadio.setChecked(false);}
        planToTakeRadio = (RadioButton)findViewById(R.id.planToTakeRadioButton);
        if(courseStatus.equals("plan to take")){planToTakeRadio.setChecked(true);}
        else{planToTakeRadio.setChecked(false);}
        //set value of start alert switch
        sAlert = (Switch)findViewById(R.id.courseStartAlert);
        if(startAlert == 0){sAlert.setChecked(false);}
        else{sAlert.setChecked(true);} //it's holding the request ID for the reminder intent
        //set value of end alert switch
        eAlert = (Switch) findViewById(R.id.courseEndAlert);
        if(endAlert == 0){eAlert.setChecked(false);}
        else{eAlert.setChecked(true);} //it's holding the request ID for the reminder intent
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

        notificationInfo = "Default Alert";
        //create notification channel for assessment notifications
        createNotificationChannel();

        //preset start times to 8AM, end times to 8PM, no need for user to set these times for courses
        startHour = 8;
        startMinute = 0;
        endHour = 20;
        endMinute = 0;

        //set startDate, startMonth,startYear, startHour, and startMinute right here just in case alert switch is changed without picking new dates
        String courseStart = textViewStartDate.getText().toString();
        String courseEnd = textViewEndDate.getText().toString();
        //parse start and end strings to integers for calendar
        String[] ssd = courseStart.split("[-: ]");
        String[] sed = courseEnd.split("[-: ]");
        startDate = Integer.parseInt(ssd[1]); //day
        startMonth = (Integer.parseInt(ssd[0]))-1; //month --minus one because months are 0-11
        startYear = Integer.parseInt(ssd[2]); //year
        //set endDate, endMonth,endYear, endHour, and endMinute right here just in case alert switch is changed without picking new dates/times
        endDate = Integer.parseInt(sed[1]); //day
        endMonth = (Integer.parseInt(sed[0]))-1; //month --minus one because months are 0-11
        endYear = Integer.parseInt(sed[2]); //year

        //call local viewData method
        viewData();

        //set listeners
        addNoteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open new dialog box for client to enter/edit note in
                openAddNoteDialog();
            }
        });

        //listener for start date
        imageStartCalendar.setOnClickListener(new View.OnClickListener() {
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
                        //set values for reminder
                        startDate = dayOfMonth;
                        startMonth = month;
                        startYear = year;
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(CourseDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewEndDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                        //set values for reminder
                        endDate = dayOfMonth;
                        endMonth = month;
                        endYear = year;
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
                    //for testing
                    //Toast.makeText(CourseDetail.this, "" + assessmentNameOnly, Toast.LENGTH_SHORT).show();
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
                        if(assessmentID==0){
                            Toast.makeText(CourseDetail.this, "ERROR: Please select an assessment.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        else {
                            openAssessmentDetailActivity(assessmentID);
                            return true;
                        }

                    case R.id.delete:
                        //parse name off of listItem
                        String[] separated = nameOfSelectedItem.split("\n");
                        if(nameOfSelectedItem == ""){
                            Toast.makeText(CourseDetail.this, "ERROR: Please select an assessment.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            new AlertDialog.Builder(CourseDetail.this)
                                    .setTitle("Delete Entry?")
                                    .setMessage("Are you sure you want to delete '"+separated[0]+"'?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //delete item
                                            Boolean checkDeleteData = dbHelper.deleteData(separated[0], "AssessmentInfo");
                                            if (checkDeleteData) {
                                                Toast.makeText(CourseDetail.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                                                nameOfSelectedItem = "";
                                                refreshList();

                                            }
                                            else {
                                                Toast.makeText(CourseDetail.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null) //does nothing
                                    .setIcon(R.drawable.ic_baseline_warning_24)
                                    .show();
                        }
                        return true;

                    case R.id.save:

                        //get these values ahead of the rest for comparison
                        String courseStart = textViewStartDate.getText().toString();
                        String courseEnd = textViewEndDate.getText().toString();
                        //parse start and end for comparison
                        String[] ssd = courseStart.split("-");
                        String[] sed = courseEnd.split("-");
                        Log.d("split values", ssd[2]+","+sed[2]);

                        //compare start and end
                        boolean startLessThanEnd = false; //default
                        if( Integer.parseInt(ssd[2])<(Integer.parseInt(sed[2])) )
                            {startLessThanEnd = true;}
                        else if ( Integer.parseInt(ssd[2])==(Integer.parseInt(sed[2]))
                                && Integer.parseInt(ssd[0])<(Integer.parseInt(sed[0])) )
                                {startLessThanEnd = true;}
                        else if ( Integer.parseInt(ssd[2])==(Integer.parseInt(sed[2]))
                                && Integer.parseInt(ssd[0])==(Integer.parseInt(sed[0]))
                                && Integer.parseInt(ssd[1])<=(Integer.parseInt(sed[1])) )
                                {startLessThanEnd = true;}
                        //use startLessThanEnd to determine next
                        if(startLessThanEnd) {
                            //get values to save changes
                            String courseName = titleText.getText().toString();
                            //change boolean radiobutton values to strings for storage
                            //for the inProgress radio
                            Boolean inProgressRadioState = inProgressRadio.isChecked();
                            String status = ""; //default
                            if (inProgressRadioState) {
                                status = "in progress";
                            }
                            //for the completed radio
                            Boolean completedRadioState = completedRadio.isChecked();
                            if (completedRadioState) {
                                status = "completed";
                            }
                            //for the dropped radio
                            Boolean droppedRadioState = droppedRadio.isChecked();
                            if (droppedRadioState) {
                                status = "dropped";
                            }
                            //for the planToTake radio
                            Boolean planToTakeRadioState = planToTakeRadio.isChecked();
                            if (planToTakeRadioState) {
                                status = "plan to take";
                            }
                            //for a start alert
                            Boolean startSwitchState = sAlert.isChecked();
                            int startAlert = 0; //default
                            if (startSwitchState) {
                                //delete previous reminder and set new one anytime switchState is true on save
                                cancelReminder("start");
                                startAlert = setStartReminder(); //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
                            } else {  //cancel old reminder since no longer checked
                                cancelReminder("start");
                            }
                            //for an end alert
                            Boolean endSwitchState = eAlert.isChecked();
                            int endAlert = 0; //default
                            if (endSwitchState) {
                                //delete previous reminder and set new one anytime switchState is true on save
                                cancelReminder("stop");
                                endAlert = setEndReminder(); //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
                            } else {  //cancel old reminder since no longer checked
                                cancelReminder("stop");
                            }
                            String courseProfessor = professor.getText().toString();
                            String courseProfessorPhone = professorPhone.getText().toString();
                            String courseProfessorEmail = professorEmail.getText().toString();

                            //test for all fields filled
                            if(courseName.equals("")||courseStart.equals("")||courseEnd.equals("")||status.equals("")||courseProfessor.equals("")||courseProfessorPhone.equals("")
                                    ||courseProfessorEmail.equals("")) {
                                Toast.makeText(CourseDetail.this, "ERROR: All fields must be filled.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //save the changes
                                Boolean checkSavedChanges = dbHelper.updateCourseData(courseID, courseName, courseStart, courseEnd, status, startAlert, endAlert, courseProfessor, courseProfessorPhone, courseProfessorEmail);
                                //test for success
                                if (checkSavedChanges) {
                                    Toast.makeText(CourseDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                                    refreshList();
                                } else {
                                    Toast.makeText(CourseDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        }
                        else{
                            Toast.makeText(CourseDetail.this, "ERROR: End is before Start.", Toast.LENGTH_SHORT).show();
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
        Cursor cursor = dbHelper.viewData("assessment");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //display name and start/end dates
            while (cursor.moveToNext()) {
                if(cursor.getInt(7)==courseID) { //index 7 is the courseID foreign key for AssessmentInfo DB
                    String nameAndDates = (cursor.getString(1)) + "\n" + (cursor.getString(2)) + " to " + (cursor.getString(3));
                    listItem.add(nameAndDates);
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
    public void openAddNoteDialog(){
        AddNoteDialog addNoteDialog = new AddNoteDialog(courseID);
        addNoteDialog.show(getSupportFragmentManager(), "Add Note Dialog");
    }

    //for opening addAssessmentDialog, passes 7 arguments
    @Override
    public void applyTexts(String assessmentName, String assessmentStartDate,String assessmentEndDate,String assessmentType,int assessmentStartAlert,int assessmentEndAlert,int courseID) {

        Boolean checkInsertData = dbHelper.insertUserData(assessmentName,assessmentStartDate,assessmentEndDate,assessmentType, assessmentStartAlert,assessmentEndAlert,courseID);
        if(checkInsertData)
            Toast.makeText(CourseDetail.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(CourseDetail.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    //this is called when the add note dialog closes, saves note to DB
    @Override
    public void applyTexts(String courseNote, int shared) {

        Boolean checkInsertData = dbHelper.insertCourseNote(courseNote,shared, courseID);
        if(checkInsertData)
            Toast.makeText(CourseDetail.this, "New Note Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(CourseDetail.this, "New Note Not Inserted", Toast.LENGTH_SHORT).show();
    }
    //this is called when the add note dialog closes, saves contact info to DB
    @Override
    public void applyTexts(String contactName, String contactPhone) {

        Boolean checkInsertData = dbHelper.insertContact(contactName,contactPhone, courseID);
        if(checkInsertData)
            Toast.makeText(CourseDetail.this, "New Contact Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(CourseDetail.this, "New Contact Not Inserted", Toast.LENGTH_SHORT).show();
    }
    public void openAssessmentDetailActivity(int ID){
        Intent intent =new Intent(this, AssessmentDetail.class);
        intent.putExtra(ASSESSMENT_ID, ID);
        startActivity(intent);
    }

    //////////////for notifications//////////////////
    private void createNotificationChannel() {
        CharSequence name = "ReminderChannel";
        String description = "Channel for reminders";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("dueAlert", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private int setStartReminder(){
        //get present time for testing that reminder time is in the future, alarms set for the past will go off immediately
        long presentTime= System.currentTimeMillis();
        //variables for calendar
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(startYear,startMonth,startDate,startHour,startMinute,0);
        long startConvertedToMillis = calendar1.getTimeInMillis();
        //compare present millis to new reminder time
        if(startConvertedToMillis>presentTime) {
            //set value of assessmentInfo to be passed as intent extra
            notificationInfo = "Your course '" + (titleText.getText().toString()).toUpperCase() + "' has begun.";
            intent.putExtra(COURSE_NOTIFICATION_INFO, notificationInfo);
            intent.setAction("detailReminder");
            //random number for request code for intent
            Random r = new Random();
            int randomRequestCode = r.nextInt(10000 - 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(CourseDetail.this, randomRequestCode, intent, 0);
            AlarmManager alarmManager = (AlarmManager)(CourseDetail.this).getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startConvertedToMillis, pendingIntent);
            //for testing
            //Toast.makeText(CourseDetail.this, "start code: "+Integer.toString(randomRequestCode), Toast.LENGTH_SHORT).show();
            //return the randomRequestCode to store for later deletion of intent
            return randomRequestCode;
        }
        else return 1; //indicates time was in the past, reminder not set
    }

    private int setEndReminder(){
        //get present time for testing that reminder time is in the future, alarms set for the past will go off immediately
        long presentTime= System.currentTimeMillis();
        //variables for calendar
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(endYear,endMonth,endDate,endHour,endMinute,0);
        long endConvertedToMillis = calendar1.getTimeInMillis();
        //compare present millis to new reminder time
        if(endConvertedToMillis>presentTime) {
            //set value of assessmentInfo to be passed as intent extra
            notificationInfo = "Your course '" + (titleText.getText().toString()).toUpperCase() + "' has ended.";
            intent.putExtra(COURSE_NOTIFICATION_INFO, notificationInfo);
            intent.setAction("detailReminder");
            //random number for request code for intent
            Random r = new Random();
            int randomRequestCode = r.nextInt(10000 - 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(CourseDetail.this, randomRequestCode, intent, 0);
            AlarmManager alarmManager = (AlarmManager)(CourseDetail.this).getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, endConvertedToMillis, pendingIntent);
            //for testing
            //Toast.makeText(CourseDetail.this, "end code: "+Integer.toString(randomRequestCode), Toast.LENGTH_SHORT).show();
            //return the randomRequestCode to store for later deletion of intent
            return randomRequestCode;
        }
        else return 1; //indicates time was in the past, reminder not set
    }

    private void cancelReminder(String reminderType){
        int requestCode = 0; //for holding code stored in startAlert or endAlert
        Cursor cursor = dbHelper.getDataByName(nameOfCourseSelected, "CourseInfo");
        if (cursor.getCount() == 0) {
            //for testing
            //Toast.makeText(context, "Request Code Error.", Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()) {
                if (reminderType.equals("start")) {
                    requestCode = cursor.getInt(5); //5 is startAlert
                    //for testing
                    //Toast.makeText(context, "cancel start code: "+String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                }
                if (reminderType.equals("stop")) {
                    requestCode = cursor.getInt(6); //6 is endAlert
                    //for testing
                    //Toast.makeText(context, "Cancel end code: "+String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                }
            }
        }
        //use request code to cancel reminder
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void goHome(){
        Intent intent =new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}