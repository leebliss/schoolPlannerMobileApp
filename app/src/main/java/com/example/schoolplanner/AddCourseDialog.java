package com.example.schoolplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddCourseDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextName, editTextStartDate, editTextEndDate, editTextCourseStatus, editTextCourseProfessor, editTextCourseProfessorPhone, editTextCourseProfessorEmail;
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
                        String courseName = editTextName.getText().toString();
                        String startDate = editTextStartDate.getText().toString();
                        String endDate = editTextEndDate.getText().toString();
                        String status = editTextCourseStatus.getText().toString();
                        String professor = editTextCourseProfessor.getText().toString();
                        String phone = editTextCourseProfessorPhone.getText().toString();
                        String email = editTextCourseProfessorEmail.getText().toString();
                        //this is the foreign key for the courseInfo DB
                        int termID = parentTermID;
                        //do I still need this?
                        //String termName = parentTerm;

                        listener.applyTexts(courseName,startDate,endDate,status,professor,phone,email,termID);
                        //refresh list after adding data
                        ((TermDetail)getActivity()).refreshList();
                    }
                });

        editTextName = view.findViewById(R.id.courseName);
        editTextStartDate = view.findViewById(R.id.courseStartDate);
        editTextEndDate = view.findViewById(R.id.courseEndDate);
        editTextCourseStatus = view.findViewById(R.id.courseStatus);
        editTextCourseProfessor = view.findViewById(R.id.courseProfessor);
        editTextCourseProfessorPhone = view.findViewById(R.id.courseProfessorPhone);
        editTextCourseProfessorEmail = view.findViewById(R.id.courseProfessorEmail);

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

    public interface AddCourseDialogListener{
        void applyTexts(String courseName, String startDate, String endDate, String status, String professor, String phone, String email, int termID);
    }
}
