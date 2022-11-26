package com.example.schoolplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddContactDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextNote;
    private Switch shareSwitch;
    //for db connectivity
    DBHelper dbHelper;
    //listener
    private AddNoteDialogListener listener;
    //for holding ID of course to get note info
    private int parentCourseID=0;
    //constructor for getting selected course from activity that launched this dialog
    public AddContactDialog(int courseIDThatOwnsMe) {parentCourseID = courseIDThatOwnsMe;
    }
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        //database connection
        dbHelper = new DBHelper(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_note_dialog, null);
        //build the dialog
        builder.setView(view)
                .setTitle("Manage Contacts")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //nothing needed here, only closing the dialog
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String courseNote = editTextNote.getText().toString();
                        int shareBinarySwitch=0;
                        //set to 1 if switch is on
                        if(shareSwitch.isChecked()){
                            shareBinarySwitch=1;
                        }
                        listener.applyTexts(courseNote,shareBinarySwitch);
                    }
                });

        //use courseID to get corresponding data
        String courseNoteContent="";
        int toShareOrNotToShare=0;
        Cursor cursor = dbHelper.getDataByID(parentCourseID, "CourseNotes");
        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                courseNoteContent = cursor.getString(1);
                toShareOrNotToShare = cursor.getInt(2);
            }
            //for testing
            //Toast.makeText(TermDetail.this, "" + courseNameOnly, Toast.LENGTH_SHORT).show();
        }
        //set values to items
        editTextNote = view.findViewById(R.id.noteEditText);
        editTextNote.setText(courseNoteContent);
        shareSwitch = view.findViewById(R.id.sendToContactsSwitch);
        if(toShareOrNotToShare==1)shareSwitch.setChecked(true);
        else shareSwitch.setChecked(false);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddNoteDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SchoolPlannerDialogListener");
        }
    }

    public interface AddNoteDialogListener{
        void applyTexts(String courseNote, int shared);
    }
}
