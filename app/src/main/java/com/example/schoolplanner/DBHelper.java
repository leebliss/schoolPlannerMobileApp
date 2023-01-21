package com.example.schoolplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "Userdata.db", null, 1);
    }

    //create the databases
    @Override
    public void onCreate(SQLiteDatabase DB) {
        //user login
        DB.execSQL("create Table LoginData (userID INTEGER primary key AUTOINCREMENT, userLogin TEXT NOT NULL, userPassword TEXT NOT NULL)");
        //table for term info
        DB.execSQL("create Table TermInfo (termID INTEGER primary key AUTOINCREMENT, termName TEXT NOT NULL, startDate TEXT NOT NULL, endDate TEXT NOT NULL)");
        //table for course info
        DB.execSQL("create Table CourseInfo (courseID INTEGER primary key AUTOINCREMENT, courseName TEXT NOT NULL,startDate TEXT NOT NULL, endDate TEXT NOT NULL, status TEXT NOT NULL, startAlert INTEGER, endAlert INTEGER," +
                "professor TEXT, phone TEXT, email TEXT, termID INTEGER,FOREIGN KEY(termID) REFERENCES TermInfo(termID))");
        //table for assessment info
        DB.execSQL("create Table AssessmentInfo (assessmentID INTEGER primary key AUTOINCREMENT, assessmentName TEXT NOT NULL, startDate TEXT NOT NULL, endDate TEXT NOT NULL, type TEXT NOT NULL, startAlert INTEGER, endAlert INTEGER, courseID INTEGER,"+
                "FOREIGN KEY(courseID) REFERENCES CourseInfo(courseID))");
        //table for course notes
        DB.execSQL("create Table CourseNotes (courseNoteID INTEGER primary key AUTOINCREMENT, courseNote TEXT, shared INTEGER, courseID INTEGER, FOREIGN KEY(courseID) REFERENCES CourseInfo(courseID))");
        //table for contacts
        DB.execSQL("create Table ContactInfo (ContactID INTEGER primary key AUTOINCREMENT, contactName TEXT NOT NULL, contactPhone TEXT NOT NULL, courseID INTEGER, FOREIGN KEY(courseID) REFERENCES CourseInfo(courseID))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int ii) {
        DB.execSQL("drop Table if exists LoginData");
        DB.execSQL("drop Table if exists TermInfo");
        DB.execSQL("drop Table if exists CourseInfo");
        DB.execSQL("drop Table if exists AssessmentInfo");
        DB.execSQL("drop Table if exists CourseNotes");
        DB.execSQL("drop Table if exists ContactInfo");
    }

    public int getRecordCount(String tableName, int ID) {
        String countQuery = "SELECT  * FROM "+ tableName+ " WHERE termID = "+ID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public Boolean insertCourseNote(String courseNote, int shared, int courseID){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("courseNote", courseNote);
        contentValues.put("shared", shared);
        contentValues.put("courseID", courseID);
        long result = DB.insert("CourseNotes", null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    public Boolean insertContact(String contactName, String contactPhone, int courseID){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("contactName", contactName);
        contentValues.put("contactPhone", contactPhone);
        contentValues.put("courseID", courseID);
        //insert new contact values
        long result = DB.insert("ContactInfo", null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    //implementation for adding a new user, takes 2 arguments
    public Boolean insertUserData(String name, String password){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userLogin", name);
        contentValues.put("userPassword", password);
        long result = DB.insert("LoginData", null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    //implementation for adding term data, takes 3 arguments
    public Boolean insertUserData(String name, String startDate, String endDate){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("termName", name);
        contentValues.put("startDate", startDate);
        contentValues.put("endDate", endDate);
        long result = DB.insert("TermInfo", null, contentValues);
            if (result == -1){
                return false;
            }
            else {
                return true;
            }
    }

    //implementation for adding assessment data, takes 7 arguments
    public Boolean insertUserData(String name, String startDate, String endDate, String type, int startAlert, int endAlert, int courseID){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("assessmentName", name);
        contentValues.put("startDate", startDate);
        contentValues.put("endDate", endDate);
        contentValues.put("type", type);
        contentValues.put("startAlert", startAlert);
        contentValues.put("endAlert", endAlert);
        contentValues.put("courseID", courseID); //course assessment belongs to

        long result = DB.insert("AssessmentInfo", null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    //implementation for adding course data, takes 10 arguments
    public Boolean insertUserData(String name, String startDate, String endDate, String status, int startAlert, int endAlert, String professor, String phone, String email, int termID){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("courseName", name);
        contentValues.put("startDate", startDate);
        contentValues.put("endDate", endDate);
        contentValues.put("status", status);
        contentValues.put("startAlert", startAlert);
        contentValues.put("endAlert", endAlert);
        contentValues.put("professor", professor);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("termID", termID);

        long result = DB.insert("CourseInfo", null, contentValues);
        if (result == -1){
            return false;
        }
        else {
            return true;
        }
    }

    public Boolean updateTermData(int termID, String name, String start, String end){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("termName", name);
        contentValues.put("startDate", start);
        contentValues.put("endDate", end);
        Cursor cursor = DB.rawQuery("Select * from TermInfo where termID = ?", new String[]{String.valueOf(termID)} );
        if(cursor.getCount()>0) {
            long result = DB.update("TermInfo", contentValues, "termID=?", new String[]{String.valueOf(termID)} );

            if (result == -1) {
                return false;
            } else {
                return true;  
            }
        }
        else{
            return false;
        }
    }

    public Boolean updateCourseData(int courseID, String name, String start, String end, String status, int startAlert, int endAlert, String professor, String phone, String email){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("courseName", name);
        contentValues.put("startDate", start);
        contentValues.put("endDate", end);
        contentValues.put("status", status);
        contentValues.put("startAlert", startAlert);
        contentValues.put("endAlert", endAlert);
        contentValues.put("professor", professor);
        contentValues.put("phone", phone);
        contentValues.put("email", email);

        Cursor cursor = DB.rawQuery("Select * from CourseInfo where courseID = ?", new String[]{String.valueOf(courseID)} );
        if(cursor.getCount()>0) {
            long result = DB.update("CourseInfo", contentValues, "courseID=?", new String[]{String.valueOf(courseID)} );
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        else{
            return false;
        }
    }
    public Boolean updateAssessmentData(int assessmentID, String name,String start,String end,String type,int startAlert,int endAlert){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("assessmentName", name);
        contentValues.put("startDate", start);
        contentValues.put("endDate", end);
        contentValues.put("type", type);
        contentValues.put("startAlert", startAlert);
        contentValues.put("endAlert", endAlert);

        Cursor cursor = DB.rawQuery("Select * from AssessmentInfo where assessmentID = ?", new String[]{String.valueOf(assessmentID)} );
        if(cursor.getCount()>0) {
            long result = DB.update("AssessmentInfo", contentValues, "assessmentID=?", new String[]{String.valueOf(assessmentID)} );

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        else{
            return false;
        }
    }

    public Boolean deleteContactsByCourseID(int courseID) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from ContactInfo where courseID = ?", new String[]{String.valueOf(courseID)} );
        if(cursor.getCount()>0) {
            long result = DB.delete("ContactInfo", "courseID=?", new String[]{String.valueOf(courseID)} );
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        else{
            return false;
        }
    }

    public Boolean deleteData(String name, String DbToDeleteFrom){
        SQLiteDatabase DB = this.getWritableDatabase();
        String tableName="";
        String primaryName="";
        //select correct table and field
        switch(DbToDeleteFrom){
            case "TermInfo":
                tableName = "TermInfo";
                primaryName = "termName";
                break;
            case "CourseInfo":
                tableName = "CourseInfo";
                primaryName = "courseName";
                break;
            case "AssessmentInfo":
                tableName = "AssessmentInfo";
                primaryName = "assessmentName";
                break;
            default:
                break;
        }
        String queryString = new StringBuilder().append("Select * from ").append(tableName).append(" where ").append(primaryName).append(" = ?").toString();
        Cursor cursor = DB.rawQuery(queryString, new String[]{name} );
        //delete if there are hits and return true, otherwise return false
        if(cursor.getCount()>0) {
            long result = DB.delete(tableName, primaryName+" =?", new String[]{name});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        else{
            return false;
        }
    }

    //returns rows from DB with matching names
    public Cursor getDataByName(String name, String tableToGetFrom) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String tableName="";
        String primaryName="";
        //selects correct table to display
        switch(tableToGetFrom){
            case "TermInfo":
                tableName = "TermInfo";
                primaryName = "termName";
                break;
            case "CourseInfo":
                tableName = "CourseInfo";
                primaryName = "courseName";
                //cursor = DB.rawQuery("Select * from CourseInfo where courseName = ?", new String[]{name} );
                break;
            case "AssessmentInfo":
                tableName = "AssessmentInfo";
                primaryName = "assessmentName";
                break;
            case "ContactInfo":
                tableName = "ContactInfo";
                primaryName = "contactName";
                break;
            case "LoginData":
                tableName = "LoginData";
                primaryName = "userLogin";
                break;
            default:
                break;
        }
        String queryString = new StringBuilder().append("Select * from ").append(tableName).append(" where ").append(primaryName).append(" = ?").toString();
        Cursor cursor = DB.rawQuery(queryString, new String[]{name} );
        return cursor;
    }

    //returns rows from DB with matching IDs
    public Cursor getDataByID(int ID, String tableToGetFrom) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String tableName="";
        String primaryID="";
        //selects correct table to display
        switch(tableToGetFrom){
            case "TermInfo":
                tableName = "TermInfo";
                primaryID = "termID";
                break;
            case "CourseInfo":
                tableName = "CourseInfo";
                primaryID = "courseID";
                break;
            case "AssessmentInfo":
                tableName = "AssessmentInfo";
                primaryID = "assessmentID";
                break;
            case "CourseNotes":
                tableName = "CourseNotes";
                primaryID = "courseID"; //searching the CourseNotes DB on foreign key courseID
                break;
            case "ContactInfo":
                tableName = "ContactInfo";
                primaryID = "courseID"; //searching the CourseNotes DB on foreign key courseID
                break;
            default:
                break;
        }
        String queryString = new StringBuilder().append("Select * from ").append(tableName).append(" where ").append(primaryID).append(" = ?").toString();
        Cursor cursor = DB.rawQuery(queryString, new String[]{String.valueOf(ID)} );
        return cursor;
    }

    public Cursor viewData(String tableToView){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "";
        //selects correct table to display
        switch(tableToView){
            case "term":
                query = "Select * from TermInfo";
                break;
            case "course":
                query = "Select * from CourseInfo";
                break;
            case "assessment":
                query = "Select * from AssessmentInfo";
                break;
            case "contacts":
                query = "Select * from ContactInfo";
                break;
            default:
                break;
        }
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }
}
