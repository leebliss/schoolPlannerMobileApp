package com.example.schoolplanner;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddNoteDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextNote;
    private Switch shareSwitch;
    private static final int RESULT_PICK_CONTACT =1;
    //for db connectivity
    DBHelper dbHelper;
    //for displaying school planner contacts in a list
    ArrayList<String> listItem;
    ArrayList<Integer> selectedItems; //to tack selected items

    ArrayAdapter adapter;
    ListView userList;
    //for holding index of selected item
    int currentlySelectedItem;
    //for parsing name of selected item
    String nameOfSelectedItem;
    String courseNameOnly;
    int courseID;
    //for displaying all of user's contacts in a list to choose from
    ArrayList<String> allContactslistItem;
    String[] allContactItem;
    ArrayAdapter allContactsAdapter;
    //ListView allContactsUserList;
    ListView allContactsUserList;
    ImageButton upButton, downButton;
    //for changing size of view accordingly
    int viewSize;
    //listener
    private AddNoteDialogListener listener;
    //for holding ID of course to get note info
    private int parentCourseID=0;
    //forManaging scroll of listView, which item is selected
    int firstVisible;
    int lastVisible;
    ArrayList<Integer> selectedContacts;

    //constructor for getting selected course from activity that launched this dialog
    public AddNoteDialog(int courseIDThatOwnsMe) {
        parentCourseID = courseIDThatOwnsMe;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //database connection
        dbHelper = new DBHelper(getActivity());
        //for displaying school planner contacts in a list
        listItem = new ArrayList<>();
        //for displaying all of user's contacts in a list to choose from
        allContactslistItem = new ArrayList<>();
        //for holding index and name of selected items
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";
        courseNameOnly = "";
        //for holding course ID
        courseID = 0;
        //selectedItems = new ArrayList();  // to track the selected items
        //ArrayList<int> currentColor = "transparent";
        selectedContacts = new ArrayList<Integer>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_note_dialog, null);

        //////////////////TEST VERSION/////////////////////////////

        // add listeners for up and down buttons
        upButton = view.findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //For testing
                Toast.makeText(getActivity(), "up button pressed", Toast.LENGTH_SHORT).show();
            }
        });
        downButton = view.findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //For testing
                Toast.makeText(getActivity(), "down button pressed", Toast.LENGTH_SHORT).show();
            }
        });

        //for clicking on list items in all contacts list
        allContactsUserList = view.findViewById(R.id.displayAllContactNames);
        allContactsUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                nameOfSelectedItem = allContactsUserList.getItemAtPosition(i).toString();
                int test1 = parent.getChildCount();
                //highlight item when clicked
                //For testing
                Toast.makeText(getActivity(), i+" "+ test1, Toast.LENGTH_SHORT).show();
                int stepper;
                //adjust position relative to visible scroll space
                for (stepper=0;firstVisible+stepper<=lastVisible; stepper++) {
                    //System.out.println(n);
                    if (firstVisible + stepper == i) {
                        parent.getChildAt(stepper).setBackgroundColor(Color.YELLOW);

                        /*
                        //check to see if already selected
                        Iterator<Integer> iterator = selectedContacts.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next() == i) {
                                //remove this position from selected items array list, set to transparent
                                selectedContacts.remove(i);
                                parent.getChildAt(stepper).setBackgroundColor(Color.TRANSPARENT);
                            /*code here to remove this position value i from an int array list of
                            selected items. This will be used for putting selected items into
                            the userList from the allContactsList.

                            } else { //add to list and set color to yellow
                                selectedContacts.add(i);
                                parent.getChildAt(stepper).setBackgroundColor(Color.YELLOW);
                            }

                         */
                            break;
                        }
                    }
                }
                /*
                parent.getChildCount();
                parent.getChildAt(i).setBackgroundColor(Color.YELLOW);
                */
                /*
                //reset previously selected item to transparent when clicked
                if (currentlySelectedItem != -1 && currentlySelectedItem != stepper){
                    parent.getChildAt(currentlySelectedItem).setBackgroundColor(Color.TRANSPARENT);
                }
                currentlySelectedItem = stepper;
                 */

        });
        allContactsUserList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                   firstVisible = view.getFirstVisiblePosition();
                   lastVisible = view.getLastVisiblePosition();

                /*
                for (int i = 0; i < size; i++) {
                    View v = view.getChildAt(i);
                    if (v instanceof CheckedTextView)
                        ((CheckedTextView) v).refreshDrawableState();
                }
                */
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


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
                })
                .setMultiChoiceItems(allContactItem, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // user checked or unchecked a box
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
        userList = view.findViewById(R.id.displayContactNames);
        //allContactsUserList = view.findViewById(R.id.displayAllContactNames);
        //get list of all contacts from user's phone
        getContactsList();
        //show contacts that are already saved to database
        viewData();

        return builder.create();
    }

    ////////////////////////original/////////////////////////
        /*
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
        userList = view.findViewById(R.id.displayContactNames);
        allContactsUserList = view.findViewById(R.id.displayAllContactNames);

        //get list of all contacts from user's phone
        getContactsList();
        //show contacts that are already saved to database
        viewData();

        return builder.create();
    }
*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddNoteDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SchoolPlannerDialogListener");
        }
    }

    //////////////////////methods////////////////////////////

    private void getContactsList() {
        //check permissions
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 0);
        //get contacts and safe to list
        } else {
            ContentResolver resolver = getActivity().getContentResolver();
            Map<Long, List<String>> phones = new HashMap<>();
            Cursor getContactsCursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, null);

            if (getContactsCursor != null) {
                while (getContactsCursor.moveToNext()) {
                    long contactId = getContactsCursor.getLong(0);
                    String phone = getContactsCursor.getString(1);
                    List<String> list;
                    if (phones.containsKey(contactId)) {
                        list = phones.get(contactId);
                    } else {
                        list = new ArrayList<>();
                        phones.put(contactId, list);
                    }
                    list.add(phone);
                }
                getContactsCursor.close();
            }
            getContactsCursor = resolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    new String[]{
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_URI},
                    null, null, null);
            while (getContactsCursor != null &&
                    getContactsCursor.moveToNext()) {
                long contactId = getContactsCursor.getLong(0);
                String name = getContactsCursor.getString(1);
                String photo = getContactsCursor.getString(2);
                List<String> contactPhones = phones.get(contactId);
                if (contactPhones != null) {
                    for (String phone :
                            contactPhones) {
                        //show list of contacts so use can make multiple selections
                        String nameAndPhone = name +" "+ phone;
                        allContactslistItem.add(nameAndPhone);
                        //addContact(contactId, name, phone, photo);
                        //for testing
                        Log.i("************Contact_Provider_Demo",phone);
                    }
                    //for passing into checkBoxBuilder, needs a String array to convert to charSequence array
                    allContactItem = new String[allContactslistItem.size()];
                    allContactslistItem.toArray(allContactItem);
                }
            }
            allContactsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, allContactslistItem);
            allContactsUserList.setAdapter(allContactsAdapter);
            //programmatically reset height of listview
            ListViewHelper.setListViewHeightBasedOnChildren(allContactsUserList);
        }
    }

    private void viewData(){
        Cursor cursor = dbHelper.viewData("contacts");
        if(cursor.getCount() == 0)
            Toast.makeText(getActivity(), "No data to show", Toast.LENGTH_SHORT).show();
        else{
            //display name and phone number
            while (cursor.moveToNext()) {
                if(cursor.getInt(3)==parentCourseID) { //index 3 is the courseID foreign key for ContactInfo DB
                    String nameAndPhone = ( (cursor.getString(1)) + "\n" + (cursor.getString(2)) );
                    listItem.add(nameAndPhone);
                    //listItem.add(cursor.getString(0)); //index 0 is name
                }
            }
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItem);
            userList.setAdapter(adapter);
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(userList);
        }
    }
    public interface AddNoteDialogListener{
        void applyTexts(String courseNote, int shared);
        void applyTexts(String contactName, String contactPhone);
    }
}