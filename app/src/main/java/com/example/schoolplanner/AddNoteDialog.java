package com.example.schoolplanner;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNoteDialog extends AppCompatDialogFragment {
    //for user to enter values
    private EditText editTextNote;
    private Switch shareSwitch;
    private static final int RESULT_PICK_CONTACT =1;
    //for db connectivity
    DBHelper dbHelper;

    /////////////////for displaying school planner contacts in a list/////////////////
    ArrayList<String> contactListItem;
    //ArrayList<Integer> selectedItems; //to tack selected items
    ArrayAdapter contactListAdapter;
    ListView contactList;

    //for holding index of selected item
    int currentlySelectedItem;
    //for parsing name of selected item
    String nameOfSelectedItem;
    String courseNameOnly;
    int courseID;

    ////////////////for displaying all of user's contacts in a list to choose from///////////
    ArrayList<String> allContactslistItem;
    String[] allContactItem;   // ??
    ArrayAdapter allContactsAdapter;
    ListView allContactsList;


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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //inflate layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_note_dialog, null);

        //database connection
        dbHelper = new DBHelper(getActivity());
        //for displaying school planner contacts in a list
        contactListItem = new ArrayList<>();
        //for displaying all of user's contacts in a list to choose from
        allContactslistItem = new ArrayList<>();
        //allContactsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, allContactslistItem);
        //allContactsUserList.setAdapter(allContactsAdapter);

        //for holding index and name of selected items
        currentlySelectedItem = -1;
        nameOfSelectedItem = "";
        courseNameOnly = "";
        //for holding course ID
        courseID = 0;
        //selectedItems = new ArrayList();  // to track the selected items
        selectedContacts = new ArrayList();

        //get hold of lists from layout
        allContactsList = view.findViewById(R.id.displayAllContactNames);
        contactList = view.findViewById(R.id.displayContactNames);
        //set contactList adapter
        contactListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, contactListItem);
        contactList.setAdapter(contactListAdapter);

        //for clicking on list items in allContacts list
        allContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                String selectedListItem =  parent.getItemAtPosition(i).toString();  //get the string from the selected list item
                if(contactListItem.contains(selectedListItem)){  //already moved from all contacts to contacts
                    Toast.makeText(getActivity(), "This contact has already been added.", Toast.LENGTH_SHORT).show();
                }
                else { //add from all contacts to contacts
                    contactListAdapter.add(selectedListItem);
                    contactListAdapter.notifyDataSetChanged();
                }
            }
        });
        //for clicking on list items in contactsList
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                String selectedListItem =  parent.getItemAtPosition(i).toString();  //get the string from the selected list item
                contactListAdapter.remove(selectedListItem);
                contactListAdapter.notifyDataSetChanged();
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
                        addContactsToDatabase();
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
        contactList = view.findViewById(R.id.displayContactNames);
        //allContactsUserList = view.findViewById(R.id.displayAllContactNames);
        //get list of all contacts from user's phone
        getContactsList();
        //show contacts that are already saved to database for this
        viewData();

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
                        //combine name and phone, then add to allContactsList
                        String nameAndPhone = name +": "+ phone;
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
            allContactsList.setAdapter(allContactsAdapter);
            //programmatically reset height of listview
            ListViewHelper.setListViewHeightBasedOnChildren(allContactsList);
        }
    }
    private void addContactsToDatabase(){

        //dbHelper needs a method to delete all with a certain courseID, call that here

        //parse name and phone number so they can be added to db separately
        for(int i = 0; i < contactListItem.size();i++) {
            String nameAndPhone = contactListItem.get(i);
            String[] separated = nameAndPhone.split(": ");
            String name = separated[0];
            String phone = separated[1];
            listener.applyTexts(name, phone);
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
                    //combine name and phone for list display
                    String nameAndPhone = ( (cursor.getString(1)) + ": " + (cursor.getString(2)) );
                    contactListItem.add(nameAndPhone);
                }
            }
            contactListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, contactListItem);
            contactList.setAdapter(contactListAdapter);
            //contactListAdapter.notifyDataSetChanged();
            //programatically reset height of listview each time viewData is called
            ListViewHelper.setListViewHeightBasedOnChildren(contactList);
        }
    }
    public interface AddNoteDialogListener{
        void applyTexts(String contactName, String contactPhone);
        void applyTexts(String courseNote, int shared);

    }
}