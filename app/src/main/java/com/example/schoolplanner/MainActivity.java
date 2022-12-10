package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddTermDialog.AddTermDialogListener {

    //for passing values to another activity
    public static final String TERM_ID = "com.example.schoolplanner.TERM_ID";
    //public static final String ASSESSMENT_INFO = "com.example.schoolplanner.ASSESSMENT_INFO";

    TextView titleText;
    //Button addNew, update, delete;
    //for bottom navigation menu
    private BottomNavigationView bottomNavigationView;
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //buttons
        /*
        addNew = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        delete = findViewById(R.id.btnDelete);
         */

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

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                switch (item.getItemId()){
                    case R.id.addNew:
                        openDialog();
                        return true;
                    case R.id.open:
                        if(termID==0){
                            Toast.makeText(MainActivity.this, "ERROR: Please select a term.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        else {
                            openTermDetailActivity(termID);
                            return true;
                        }
                    case R.id.delete:
                        //parse name off of listItem
                        String[] separated = nameOfSelectedItem.split("\n");
                        if(nameOfSelectedItem == ""){
                            Toast.makeText(MainActivity.this, "ERROR: Please select a Term.", Toast.LENGTH_SHORT).show();
                        }
                        else if (dbHelper.getRecordCount("CourseInfo",termID)>0){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Cannot Delete")
                                    .setMessage("Term '"+separated[0]+"' cannot be deleted because it contains courses.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(MainActivity.this, "Entry not deleted.", Toast.LENGTH_SHORT).show();
                                            nameOfSelectedItem = "";
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null) //does nothing
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Delete Entry?")
                                    .setMessage("Are you sure you want to delete '"+separated[0]+"'?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //delete item
                                            Boolean checkDeleteData = dbHelper.deleteData(separated[0], "TermInfo");
                                            if (checkDeleteData) {
                                                Toast.makeText(MainActivity.this, "Entry deleted.", Toast.LENGTH_SHORT).show();
                                                nameOfSelectedItem = "";
                                                refreshList();
                                            }
                                            else {
                                                Toast.makeText(MainActivity.this, "Error, entry not deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null) //does nothing
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        return true;
                }
                return false;
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.home:
            Toast.makeText(MainActivity.this, "This is the home screen.", Toast.LENGTH_SHORT).show();
            //add the function to perform here
            return(true);
        case R.id.back:
            Toast.makeText(MainActivity.this, "This is the home screen.", Toast.LENGTH_SHORT).show();
            return(true);
        case R.id.settings:
            //add the function to perform here
            return(true);
        case R.id.about:
            //add the function to perform here
            return(true);
        case R.id.exit:
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    ///////////////////methods/////////////////////////////////////////////////////
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
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(userList);
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
    public void openTermDetailActivity(int termID){
        Intent intent =new Intent(this, TermDetail.class);
        intent.putExtra(TERM_ID, termID );
        startActivity(intent);
    }
}