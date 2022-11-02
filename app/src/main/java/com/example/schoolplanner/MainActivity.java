package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddTermDialog.AddTermDialogListener {

    //for passing values to another activity
    public static final String TERM_ID = "com.example.schoolplanner.TERM_ID";
    public static final String ASSESSMENT_INFO = "com.example.schoolplanner.ASSESSMENT_INFO";

    TextView titleText;
    Button addNew, update, delete;
    //for db connectivity
    DBHelper dbHelper;
    //for displaying data in a list
    ArrayList<String> listItem;
    ArrayAdapter adapter;
    ListView userList;
    //variable for holding index of selected item
    int currentlySelectedItem;
    //variables for parsing name of selected item
    String nameOfSelectedItem;
    String termNameOnly;
    //for term ID, start and end dates
    int termID;
    String termStartDate;
    String termEndDate;
    //for passing assessment info to notifications
    String assessmentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set name for this activity in title bar
        titleText = findViewById(R.id.textTitle);
        titleText.setText("Term List");
        //buttons
        addNew = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        delete = findViewById(R.id.btnDelete);
        //database connection
        dbHelper = new DBHelper(this);
        //for displaying data in a list
        listItem = new ArrayList<>();
        userList = findViewById(R.id.displayTermNames);
        //for holding index and name of selected item
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";
        termNameOnly = "";
        //for holding term ID, start and end dates
        termID = 0;
        termStartDate = "";
        termEndDate = "";

        //call local viewData method
        viewData();

        //set up notifications from assessment start and stop times
        //setReminders();

        //set listeners
        //for clicking on list items
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                nameOfSelectedItem = userList.getItemAtPosition(i).toString();
                //highlight selected item
                parent.getChildAt(i).setBackgroundColor(Color.YELLOW);
                //reset previously selected item to transparent
                if (currentlySelectedItem != -1 && currentlySelectedItem != i) {
                    parent.getChildAt(currentlySelectedItem).setBackgroundColor(Color.TRANSPARENT);
                }
                currentlySelectedItem = i;
                //get term name only
                String[] separated = nameOfSelectedItem.split("\n");
                termNameOnly = separated[0];
                //pass term name to database to get term ID and assign to termID variable
                Cursor cursor = dbHelper.getDataByName(termNameOnly, "TermInfo");
                if (cursor.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "No matches found", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        termID = cursor.getInt(0);
                    }
                    //toast item
                    Toast.makeText(MainActivity.this, termID + " " + termNameOnly, Toast.LENGTH_SHORT).show();
                }
            }
        });

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pass name of selected term next activity
                openTermDetailActivity(termID, termNameOnly,termStartDate,termEndDate);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parse name off of listItem
                String[] separated = nameOfSelectedItem.split("\n");
                Boolean checkDeleteData = dbHelper.deleteData(separated[0],"TermInfo");
                //Boolean checkDeleteData = dbHelper.deleteData(nameOfSelectedItem);
                if(checkDeleteData) {
                    Toast.makeText(MainActivity.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                    nameOfSelectedItem = "";
                    refreshList();
                }
                else{
                    if(nameOfSelectedItem == ""){
                        Toast.makeText(MainActivity.this, "Please make a selection.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //this causes everything to reload so the data is up to date when the back arrow is used
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    //methods
    //refresh the list after making changes to data
    public void refreshList(){
        listItem.clear();
        viewData();
    }
    private void viewData(){
        Cursor cursor = dbHelper.viewData("term");
        if(cursor.getCount() == 0)
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //display name and start/end dates
            while (cursor.moveToNext()) {
                //save start date and end dates for passing to child activity in openTermDetailActivity method
                termStartDate = cursor.getString(2);
                termEndDate = cursor.getString(3);
                String nameAndDates = (cursor.getString(1))+ "\n"+termStartDate+" to "+termEndDate;
                listItem.add(nameAndDates); //index 0 is name
                //listItem.add(cursor.getString(0)); //index 0 is name
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
        }
    }
    public void  openDialog(){
        AddTermDialog addTermDialog = new AddTermDialog();
        addTermDialog.show(getSupportFragmentManager(), "Add Term Dialog");
    }
    @Override
    public void applyTexts(String termName, String startDate, String endDate) {

        Boolean checkInsertData = dbHelper.insertUserData(termName, startDate, endDate);
        if(checkInsertData)
            Toast.makeText(MainActivity.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
    }
    public void openTermDetailActivity(int termID, String termName, String startDate, String endDate){
        Intent intent =new Intent(this, TermDetail.class);
        intent.putExtra(TERM_ID, termID );
        startActivity(intent);
    }
}