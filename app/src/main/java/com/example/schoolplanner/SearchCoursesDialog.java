package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
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

    private void viewMatches(){
        //clear list so data is fresh
        listItem.clear();
        //get value entered to search on
        String searchThis = searchString.getText().toString();
        //step through data
        Cursor cursor = dbHelper.viewData("course");
        if(cursor.getCount() == 0)
            Toast.makeText(getActivity(), "No matches found.", Toast.LENGTH_SHORT).show();
        else{
            while (cursor.moveToNext()) {
                if(cursor.getString(1).equals(searchThis)) { //index 1 is the courseName for CourseInfo DB
                    String nameAndTerm = "'"+searchThis+" found in term 'XXX'"; //need to use termID to get termName here
                    listItem.add(nameAndTerm);
                }
            }
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItem);
            matchList.setAdapter(adapter);
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(matchList);
        }
    }

    private void performOkButtonAction() {
        //use pos button to search for courses in schedule
        viewMatches();
        Toast.makeText(getActivity(), "Search for course here.", Toast.LENGTH_SHORT).show();
    }

    public interface SearchCoursesDialogListener{
        void applyTexts();
    }
}
