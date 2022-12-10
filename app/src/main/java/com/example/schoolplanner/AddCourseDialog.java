package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Calendar;
import java.util.Random;

public class AddCourseDialog extends AppCompatDialogFragment {

    //for passing values to another activity
    public static final String COURSE_NOTIFICATION_INFO = "com.example.schoolplanner.COURSE_DIALOG_NOTIFICATION_INFO";
    String notificationInfo;
    //for user to enter values
    private EditText editTextName, editTextCourseProfessor, editTextCourseProfessorPhone, editTextCourseProfessorEmail;
    //start and end dates
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageEndCalendar;
    //for holding dates and times for reminders
    private int startDate, startMonth,startYear, startHour, startMinute;
    private int endDate, endMonth,endYear, endHour, endMinute;
    //for holding start and end times in millis
    long startConvertedToMillis;
    long endConvertedToMillis;
    //for date picker
    private int mDate, mMonth, mYear;
    //for course status
    private RadioButton inProgressRadio, completedRadio, droppedRadio,planToTakeRadio;
    //for choosing alerts on or off
    private Switch switchStartAlert, switchEndAlert;
    //listener
    private AddCourseDialogListener listener;
    //for holding name of term to save to new course when added
    private int parentTermID=0;
    //private String parentTerm="";
    //constructor for getting selected term from activity that launched this dialog
    public AddCourseDialog(String termThatOwnsMe, int termIDThatOwnsMe) {
        parentTermID = termIDThatOwnsMe;
        //do I still need this?
        //parentTerm = termThatOwnsMe;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_course_dialog, null);
        //build the dialog
        builder.setView(view)
                .setTitle("Add New Course")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //nothing needed here, only closing the dialog
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //overriding this in onResume()
                    }
                });

        editTextName = view.findViewById(R.id.courseName);
        textViewStartDate = view.findViewById(R.id.courseStartDate);
        imageStartCalendar = view.findViewById(R.id.courseStartCalendar);
        textViewEndDate = view.findViewById(R.id.courseEndDate);
        imageEndCalendar = view.findViewById(R.id.courseEndCalendar);
        inProgressRadio = (RadioButton) view.findViewById(R.id.inProgressRadioButton);
        completedRadio = (RadioButton) view.findViewById(R.id.completedRadioButton);
        droppedRadio = (RadioButton) view.findViewById(R.id.droppedRadioButton);
        planToTakeRadio = (RadioButton) view.findViewById(R.id.planToTakeRadioButton);
        switchStartAlert = (Switch) view.findViewById(R.id.courseStartAlert);
        switchEndAlert = (Switch) view.findViewById(R.id.courseEndAlert);
        editTextCourseProfessor = view.findViewById(R.id.courseProfessor);
        editTextCourseProfessorPhone = view.findViewById(R.id.courseProfessorPhone);
        editTextCourseProfessorEmail = view.findViewById(R.id.courseProfessorEmail);

        notificationInfo = "Default Alert";
        //create notification channel for assessment notifications
        createNotificationChannel();

        //preset start times to 8AM, end times to 8PM, no need for user to set these times for courses
        startHour = 8;
        startMinute = 0;
        endHour = 20;
        endMinute = 0;
        //initialize start and end millis
        startConvertedToMillis=0;
        endConvertedToMillis=0;

        ////////////////////listeners////////////////////////////////////////

        //listener for start date

        imageStartCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                        //set values for reminder
                        startDate = dayOfMonth;
                        startMonth = month;
                        startYear = year;
                        //get start time in millis to confirm start is before end
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(startYear,startMonth,startDate,startHour,startMinute,0);
                        startConvertedToMillis = calendar1.getTimeInMillis();
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewEndDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                        //set values for reminder
                        endDate = dayOfMonth;
                        endMonth = month;
                        endYear = year;
                        //get end time in millis to confirm start is before end
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(endYear,endMonth,endDate,endHour,endMinute,0);
                        endConvertedToMillis = calendar1.getTimeInMillis();
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddCourseDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SchoolPlannerDialogListener");
        }
    }
    //override pos button to control when dialog closes
    @Override
    public void onResume()
    {
        super.onResume();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                performOkButtonAction();
            }
        });
    }

    private void performOkButtonAction() {
        if(startConvertedToMillis < endConvertedToMillis) {
            String courseName = editTextName.getText().toString();
            String startDate = textViewStartDate.getText().toString();
            String endDate = textViewEndDate.getText().toString();
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
            Boolean switchState = switchStartAlert.isChecked();
            int startAlert = 0; //default
            if (switchState) {
                startAlert = setStartReminder();
            } //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
            //for an end alert
            switchState = switchEndAlert.isChecked();
            int endAlert = 0; //default
            if (switchState) {
                endAlert = setEndReminder();
            } //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
            String professor = editTextCourseProfessor.getText().toString();
            String phone = editTextCourseProfessorPhone.getText().toString();
            String email = editTextCourseProfessorEmail.getText().toString();
            //this is the foreign key for the courseInfo DB
            int termID = parentTermID;
            //do I still need this?
            //String termName = parentTerm;

            listener.applyTexts(courseName, startDate, endDate, status, startAlert, endAlert, professor, phone, email, termID);
            //refresh list after adding data
            ((TermDetail) getActivity()).refreshList();
            //close dialog
            dismiss();
        }
        else{
            Toast.makeText(getActivity(), "ERROR: End is before Start.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {

        CharSequence name = "ReminderChannel";
        String description = "Channel for reminders";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("dueAlert", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
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
            notificationInfo = "The course named " + editTextName.getText().toString() + " has begun.";
            Intent intent = new Intent(getActivity(), CourseReminderBroadcast.class);
            intent.putExtra(COURSE_NOTIFICATION_INFO, notificationInfo);
            intent.setAction("dialogReminder");
            //random number for request code for intent
            Random r = new Random();
            int randomRequestCode = r.nextInt(10000 - 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), randomRequestCode, intent, 0);
            AlarmManager alarmManager = (AlarmManager) ((TermDetail) getActivity()).getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startConvertedToMillis, pendingIntent);
            //for testing
            Toast.makeText(getActivity(), String.valueOf(startConvertedToMillis), Toast.LENGTH_SHORT).show();
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
            notificationInfo = "Your course '" + (editTextName.getText().toString()).toUpperCase() + "' has ended.";
            Intent intent = new Intent(getActivity(), CourseReminderBroadcast.class);
            intent.putExtra(COURSE_NOTIFICATION_INFO, notificationInfo);
            intent.setAction("dialogReminder");
            //random number for request code for intent
            Random r = new Random();
            int randomRequestCode = r.nextInt(10000 - 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), randomRequestCode, intent, 0);
            AlarmManager alarmManager = (AlarmManager) ((TermDetail) getActivity()).getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, endConvertedToMillis, pendingIntent);
            //for testing
            Toast.makeText(getActivity(), String.valueOf(endConvertedToMillis), Toast.LENGTH_SHORT).show();
            //return the randomRequestCode to store for later deletion of intent
            return randomRequestCode;
        }
        else return 1; //indicates time was in the past, reminder not set
    }

    public interface AddCourseDialogListener{
        void applyTexts(String courseName, String startDate, String endDate, String status, int startAlert, int endAlert, String professor, String phone, String email, int termID);
    }
}
