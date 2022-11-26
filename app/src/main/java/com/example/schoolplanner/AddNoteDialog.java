package com.example.schoolplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddNoteDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextNote;
    private Switch shareSwitch;
    private Button addContactsButton;
    private static final int RESULT_PICK_CONTACT =1;
    private TextView showPhoneNumber;

    //for db connectivity
    DBHelper dbHelper;
    //listener
    private AddNoteDialogListener listener;
    //for holding ID of course to get note info
    private int parentCourseID=0;
    //constructor for getting selected course from activity that launched this dialog
    public AddNoteDialog(int courseIDThatOwnsMe) {
        parentCourseID = courseIDThatOwnsMe;
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
                .setTitle("Add/Edit Note")
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
        addContactsButton = view.findViewById(R.id.addContactsButton);

        //listener for button to add contacts
        addContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                someActivityResultLauncher.launch(intent);
            }
        });

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

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        contactPicked (data);
                        //Toast.makeText (getActivity(), data.toString(), Toast.LENGTH_SHORT).show ();
                        Log.i("***********************************",data.toString());
                    }
                    else
                    {
                        Toast.makeText (getActivity(), "Failed To pick contact", Toast.LENGTH_SHORT).show ();
                    }
                }
            });
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(resultCode==RESULT_OK)
        {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked (data);
                    break;
            }
        }
        else
        {
            Toast.makeText (getActivity(), "Failed To pick contact", Toast.LENGTH_SHORT).show ();
        }
    }

     */

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        ContentResolver contentResolver = getActivity().getContentResolver();

        try {
            String phoneNo = "";
            int phoneIndex = 0;
            Uri uri = data.getData ();
            cursor = contentResolver.query (uri, null, null,null,null);
            cursor.moveToFirst ();
            phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);

            phoneNo = cursor.getString (phoneIndex);
            showPhoneNumber.setText (phoneNo);
            Toast.makeText (getActivity(), phoneNo, Toast.LENGTH_SHORT).show ();

        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public interface AddNoteDialogListener{
        void applyTexts(String courseNote, int shared);
    }
}