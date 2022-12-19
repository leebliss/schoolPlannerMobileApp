package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import java.util.Calendar;

public class AddTermDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextName;
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageEndCalendar;
    //for holding dates and times for comparing start and end
    private int startDate, startMonth,startYear;
    private int endDate, endMonth,endYear;

    //for holding start and end times in millis
    long startConvertedToMillis;
    long endConvertedToMillis;
    //for date picker
    private int mDate, mMonth, mYear;
    //listener
    private AddTermDialogListener listener;

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_term_dialog, null);
        //build the dialog
        builder.setView(view)
                .setTitle("Add New Term")
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

        editTextName = view.findViewById(R.id.termName);
        textViewStartDate = view.findViewById(R.id.termStartDate);
        imageStartCalendar = view.findViewById(R.id.termStartCalendar);
        textViewEndDate = view.findViewById(R.id.termEndDate);
        imageEndCalendar = view.findViewById(R.id.termEndCalendar);

        //initialize start and end millis
        startConvertedToMillis=0;
        endConvertedToMillis=0;

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
                        //set values for comparing start to end
                        startDate = dayOfMonth;
                        startMonth = month;
                        startYear = year;
                        //get start time in millis
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(startYear,startMonth,startDate);
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
                        //set values for comparing start to end
                        endDate = dayOfMonth;
                        endMonth = month;
                        endYear = year;
                        //get start time in millis
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(endYear,endMonth,endDate);
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
            listener = (AddTermDialogListener)context;
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
        if (startConvertedToMillis <= endConvertedToMillis) {
            String termName = editTextName.getText().toString();
            String termStart = textViewStartDate.getText().toString();
            String termEnd = textViewEndDate.getText().toString();
            //test for all fields being filled
            if(termName.equals("") || termStart.equals("") || termEnd.equals("")){
                Toast.makeText(getActivity(), "ERROR: All fields must be filled.", Toast.LENGTH_SHORT).show();
            }
            else {
                listener.applyTexts(termName, termStart, termEnd);
                //refresh list after adding data
                ((MainActivity) getActivity()).refreshList();
                //close dialog
                dismiss();
            }
        }
        else{
            Toast.makeText(getActivity(), "ERROR: End is before Start.", Toast.LENGTH_SHORT).show();
        }
    }

    public interface AddTermDialogListener{
        void applyTexts(String termName, String startDate, String endDate);
    }
}
