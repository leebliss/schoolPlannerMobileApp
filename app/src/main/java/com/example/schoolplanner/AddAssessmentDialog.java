package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.time.Clock;
import java.util.Calendar;

public class AddAssessmentDialog extends AppCompatDialogFragment {

    //for passing values to another activity
    public static final String ASSESSMENT_INFO = "com.example.schoolplanner.ASSESSMENT_INFO";
    String assessmentInfo;
    //for db connectivity
    DBHelper dbHelper;
    //for user to enter values
    private EditText editTextName;
    private Switch switchStartAlert, switchEndAlert;
    private RadioButton performanceAssessmentRadio, objectiveAssessmentRadio;
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageStartClock,imageEndCalendar, imageEndClock;
    //for holding dates and times for reminders
    private int startDate, startMonth,startYear, startHour, startMinute;
    private int endDate, endMonth,endYear, endHour, endMinute;

    //for date picker
    private int mDate, mMonth, mYear, mHour, mMinute;
    private boolean dateSet;
    //listener
    private AddAssessmentDialogListener listener;
    //for holding ID of course to save to new course when added
    private int parentCourseID=0;
    private String parentCourse="";
    //constructor for getting selected course from activity that launched this dialog
    public AddAssessmentDialog(String courseThatOwnsMe, int courseIDThatOwnsMe) {
        parentCourseID = courseIDThatOwnsMe;
        parentCourse = courseThatOwnsMe;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_assessment_dialog, null);

        //build the dialog
        builder.setView(view)
                .setTitle("Add New Assessment")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //nothing needed here, only closing the dialog
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String assessmentName = editTextName.getText().toString();
                        String startDate = textViewStartDate.getText().toString();
                        String endDate = textViewEndDate.getText().toString();
                        //change boolean radiobutton values to strings for storage
                        //for the performance assessment radio
                        Boolean performanceRadioState = performanceAssessmentRadio.isChecked();
                        String assessmentType = ""; //default
                        if(performanceRadioState) {assessmentType = "performance";}
                        //for the objective assessment radio
                        Boolean objectiveRadioState = objectiveAssessmentRadio.isChecked();
                        if(objectiveRadioState) {assessmentType = "objective";}
                        //change boolean switch values to strings for storage
                        //for a start alert
                        Boolean switchState = switchStartAlert.isChecked();
                        String startAlert = "false"; //default
                        if(switchState) {startAlert = "true";
                            //set a reminder
                            setStartReminder();
                        }
                        //for an end alert
                        switchState = switchEndAlert.isChecked();
                        String endAlert = "false"; //default
                        if(switchState) {endAlert = "true";}
                        //this is the foreign key for the assessmentInfo DB
                        int courseID = parentCourseID;
                        //do I still need this?
                        String courseName = parentCourse;
                        listener.applyTexts(assessmentName,startDate,endDate,assessmentType,startAlert,endAlert,courseID);
                        //refresh list after adding data
                        ((CourseDetail)getActivity()).refreshList();
                        //set up notifications from assessment start and stop times
                        //setStartReminder();
                        //setNotifications();
                    }
                });

        editTextName = view.findViewById(R.id.assessmentName);
        textViewStartDate = view.findViewById(R.id.assessmentStartDate);
        imageStartCalendar = view.findViewById(R.id.assessmentStartCalendar);
        imageStartClock = view.findViewById(R.id.assessmentStartClock);
        textViewEndDate = view.findViewById(R.id.assessmentEndDate);
        imageEndCalendar = view.findViewById(R.id.assessmentEndCalendar);
        imageEndClock = view.findViewById(R.id.assessmentEndClock);
        performanceAssessmentRadio = (RadioButton) view.findViewById(R.id.assessmentPerformanceRadioButton);
        objectiveAssessmentRadio = (RadioButton) view.findViewById(R.id.assessmentObjectiveRadioButton);
        switchStartAlert = (Switch) view.findViewById(R.id.assessmentStartAlert);
        switchEndAlert = (Switch) view.findViewById(R.id.assessmentEndAlert);

        //set assessmentInfo
        assessmentInfo = "Funky Alert!";
        //set boolean for date picker
        dateSet = false;
        //create notification channel for assessment notifications
        createNotificationChannel();

        //listener for start date
        imageStartCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                //launch date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year); //jan month 0, so must add 1
                        //set values for reminder
                        startDate = dayOfMonth;
                        startMonth = month;
                        startYear = year;
                        //pass to notification
                        //assessmentInfo = textViewStartDate.getText().toString();    //this is only set once when app starts
                    }
                }, mYear, mMonth, mDate);
                //set dateSet to true
                dateSet=true;
                datePickerDialog.show();
            }
        });
        //listener for start time
        imageStartClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSet) {
                    final Calendar Cal = Calendar.getInstance();
                    mHour = Cal.get(Calendar.HOUR_OF_DAY);
                    mMinute = Cal.get(Calendar.MINUTE);
                    // Launch time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            textViewStartDate.setText((textViewStartDate.getText()) + " " + hourOfDay + ":" + minute);
                            //set values for reminder
                            startHour = hourOfDay;
                            startMinute = minute;
                        }
                    }, mHour, mMinute, false);
                    timePickerDialog.show();
                }
                else{
                    Toast.makeText(getActivity(), "Please choose the date first.", Toast.LENGTH_SHORT).show();
                }
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
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
        imageEndClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSet) {
                    final Calendar Cal = Calendar.getInstance();
                    mHour = Cal.get(Calendar.HOUR_OF_DAY);
                    mMinute = Cal.get(Calendar.MINUTE);
                    // Launch time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            textViewEndDate.setText((textViewEndDate.getText()) + " " + hourOfDay + ":" + minute);
                            //set values for reminder
                            endHour = hourOfDay;
                            endMinute = minute;
                      }
                    }, mHour, mMinute, false);
                    timePickerDialog.show();
                }
                else{
                    Toast.makeText(getActivity(), "Please choose the date first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddAssessmentDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SchoolPlannerDialogListener");
        }
    }

    private void createNotificationChannel() {

        CharSequence name = "AssessmentReminderChannel";
        String description = "Channel for assessment reminder";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("assessmentAlert", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
    private void setStartReminder(){

        //variables for calendar
        Calendar calendar1 = Calendar.getInstance();
        //test for if user wants an assessment start reminder

        //calendar1.set(2022,10,9,20,38, 0);
        calendar1.set(startYear,startMonth,startDate,startHour,startMinute,0);
        long startConvertedToMillis = calendar1.getTimeInMillis();

        /*
            calendar1.set(2022,9,30,12,42, 0);
            long startConvertedToMillis = calendar1.getTimeInMillis();
            long endConvertedToMillis = calendar1.getTimeInMillis();

         */

        Intent intent = new Intent(getActivity(),ReminderBroadcast.class);
        intent.putExtra(ASSESSMENT_INFO, assessmentInfo );
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,intent,0);
        AlarmManager alarmManager = (AlarmManager) ((CourseDetail)getActivity()).getSystemService(Context.ALARM_SERVICE);
        //long timeWhenMethodCalled = System.currentTimeMillis();
        //long tenSecondsInMillis = 1000*10;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startConvertedToMillis,pendingIntent);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeWhenMethodCalled + tenSecondsInMillis,pendingIntent);

        Toast.makeText(getActivity(), String.valueOf(startConvertedToMillis), Toast.LENGTH_SHORT).show();

    }

    private void setNotifications(){

        Calendar sevendayalarm = Calendar.getInstance();

        //sevendayalarm.add(Calendar.DATE, 7);
        sevendayalarm.set(2022,9,29,19,42);

        Intent intent = new Intent(getActivity(), AssessmentNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 001, intent, 0);

        AlarmManager am = (AlarmManager) ((CourseDetail)getActivity()).getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, sevendayalarm.getTimeInMillis(), pendingIntent);

        Toast.makeText(getActivity(), String.valueOf(sevendayalarm.getTimeInMillis()), Toast.LENGTH_SHORT).show();

    }

    public interface AddAssessmentDialogListener{
        void applyTexts(String assessmentName, String assessmentStartDate,String assessmentEndDate,String assessmentType,String assessmentStartAlert,String assessmentEndAlert,int courseID);
    }
}
