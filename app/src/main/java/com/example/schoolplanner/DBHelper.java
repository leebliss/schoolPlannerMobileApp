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

    @Override
    public void onCreate(SQLiteDatabase DB) {
        //table for term info
        DB.execSQL("create Table TermInfo (termID INTEGER primary key AUTOINCREMENT, termName TEXT, startDate TEXT, endDate TEXT)");
        //table for course info
        DB.execSQL("create Table CourseInfo (courseID INTEGER primary key AUTOINCREMENT, courseName TEXT,startDate TEXT, endDate TEXT, status TEXT, professor TEXT, phone TEXT, email TEXT, termID INTEGER," +
                "FOREIGN KEY(termID) REFERENCES TermInfo(termID))");
        //table for assessment info
        DB.execSQL("create Table AssessmentInfo (assessmentID INTEGER primary key AUTOINCREMENT, assessmentName TEXT, startDate TEXT, endDate TEXT, type TEXT, startAlert TEXT, endAlert TEXT, courseID INTEGER,"+
                "FOREIGN KEY(courseID) REFERENCES CourseInfo(courseID))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int ii) {
        DB.execSQL("drop Table if exists TermInfo");
        DB.execSQL("drop Table if exists CourseInfo");
        DB.execSQL("drop Table if exists AssessmentInfo");
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
    public Boolean insertUserData(String name, String startDate, String endDate, String type, String startAlert, String endAlert, int courseID){
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

    //implementation for adding course data, takes 8 arguments
    public Boolean insertUserData(String name, String startDate, String endDate, String status, String professor, String phone, String email, int termID){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("courseName", name);
        contentValues.put("startDate", startDate);
        contentValues.put("endDate", endDate);
        contentValues.put("status", status);
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

    public Boolean updateCourseData(int courseID, String name, String start, String end, String status, String professor, String phone, String email){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("courseName", name);
        contentValues.put("startDate", start);
        contentValues.put("endDate", end);
        contentValues.put("status", status);
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
    public Boolean updateAssessmentData(int assessmentID, String name,String start,String end,String type,String startAlert,String endAlert){
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
                //cursor = DB.rawQuery("Select * from CourseInfo where courseName = ?", new String[]{name} );
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
            default:
                break;
        }
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }
}
