package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchCoursesDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText searchString;
    //for db connectivity
    DBHelper dbHelper;
    //for displaying data in a list
    ArrayList<String> listItem;
    ArrayAdapter adapter;
    ListView matchList;

    //listener
    private SearchCoursesDialogListener listener;

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {

        //database connection
        dbHelper = new DBHelper(getActivity());
        //for displaying data in a list
        listItem = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_search_courses_dialog, null);
        //build the dialog
        builder.setView(view)
                .setTitle("Search Your Schedule for a Course")
                .setNegativeButton("Finished", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //nothing needed here, only closing the dialog
                    }
                })
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //overriding this in onResume()
                    }
                });
        //get hold of layout items
        matchList = view.findViewById(R.id.displayCoursesFound);
        searchString = view.findViewById(R.id.courseName);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SearchCoursesDialogListener)context;
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

        ///////////////////////the methods////////////////////////

    private void performOkButtonAction() {
        //use pos button to search for courses in schedule
        viewMatches();
    }

    private void viewMatches(){
        //clear list so data is fresh
        listItem.clear();
        //get value entered to search on
        String searchThis = searchString.getText().toString();
        //step through data
        int hits = 0;
        Cursor cursor = dbHelper.viewData("course");
        if(cursor.getCount() == 0)
            //nothing in the database
            Toast.makeText(getActivity(), "No matches found.", Toast.LENGTH_SHORT).show();
        else{
            while (cursor.moveToNext()) {
                if(cursor.getString(1).contains(searchThis)) { //index 1 is the courseName for CourseInfo DB
                    hits=1;
                    //get termID that owns matching course
                    int termID = cursor.getInt(10); //index 10 is the termID foreign key for CourseInfo DB
                    //for testing
                    Log.d("termID=", String.valueOf(termID));
                    //use termID to get term name
                    Cursor cursor2 = dbHelper.getDataByID(termID,"TermInfo");
                    String termName = "";
                    while (cursor2.moveToNext()) {
                        termName = cursor2.getString(1); //index 1 is the termName for TermInfo DB
                    }
                    //for testing
                    Log.d("termName=", termName);
                    String nameAndTerm = "'"+cursor.getString(1)+"' found in term '"+termName+"'";
                    //add searchCoursesDialog connection to other activities
                    listItem.add(nameAndTerm);
                }
            }
            if(hits==0) Toast.makeText(getActivity(), "No matches found.", Toast.LENGTH_SHORT).show();
            //test for nothing entered, in which case we do not want to show results
            if(searchThis.equals("")){
                Toast.makeText(getActivity(), "Enter a name or part of a name.", Toast.LENGTH_SHORT).show();
            }
            else {
                //for testing
                Log.d("searchThis=", String.valueOf(searchThis));
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItem);
                matchList.setAdapter(adapter);
                //programatically reset height of listview each time viewData is called
                ListViewHelper.setListViewHeightBasedOnChildren(matchList);
            }
        }
    }

    public interface SearchCoursesDialogListener{
        void applyTexts();
    }
}
