package com.example.schoolplanner;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class DBHelperAndroidTest {

    //this test tests that the test conifguration is correct and working
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.schoolplanner", appContext.getPackageName());
    }

    //test to see if accurate record count is being returned
    @Test
    public void isRecordCountCorrect(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //create object containing method to test
        DBHelper DB = new DBHelper(appContext);
        String tableName = "CourseInfo";
        int termID = 1;
        //set 'expected' to the number of records in the CourseInfo table with TermID
        assertEquals(3, DB.getRecordCount(tableName, termID) );
    }



}
