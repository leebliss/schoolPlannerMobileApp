package com.example.schoolplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddAssessmentDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextName;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private EditText editTextType;
    private EditText editTextStartAlert;
    private EditText editTextEndAlert;

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
                        String startDate = editTextStartDate.getText().toString();
                        String endDate = editTextEndDate.getText().toString();
                        String type = editTextType.getText().toString();
                        String startAlert = editTextStartAlert.getText().toString();
                        String endAlert = editTextEndAlert.getText().toString();
                        //this is the foreign key for the assessmentInfo DB
                        int courseID = parentCourseID;
                        //do I still need this?
                        String courseName = parentCourse;
                        listener.applyTexts(assessmentName,startDate,endDate,type,startAlert,endAlert,courseID);
                        //refresh list after adding data
                        ((CourseDetail)getActivity()).refreshList();
                    }
                });

        editTextName = view.findViewById(R.id.assessmentName);
        editTextStartDate = view.findViewById(R.id.assessmentStartDate);
        editTextEndDate = view.findViewById(R.id.assessmentEndDate);
        editTextType = view.findViewById(R.id.assessmentType);
        editTextStartAlert = view.findViewById(R.id.assessmentStartAlert);
        editTextEndAlert = view.findViewById(R.id.assessmentEndAlert);

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

    public interface AddAssessmentDialogListener{
        void applyTexts(String assessmentName, String assessmentStartDate,String assessmentEndDate,String assessmentType,String assessmentStartAlert,String assessmentEndAlert,int courseID);
    }
}
