package com.example.schoolplanner;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DBHelperAndroidTest {

    //this test confirms that the test configuration is correct and working
    @Test
    public void useAppContext() {
        //context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.schoolplanner", appContext.getPackageName());
    }

    //test to see if accurate record count is being returned
    @Test
    public void isRecordCountCorrect(){
        //context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //create object containing method to test
        DBHelper DB = new DBHelper(appContext);
        String tableName = "CourseInfo";
        int termID = 1;
        //set 'expected' to the number of records in the CourseInfo table with TermID
        assertEquals(4, DB.getRecordCount(tableName, termID) );
    }
}
