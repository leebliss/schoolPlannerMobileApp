package com.example.schoolplanner;

import android.content.Context;
import android.widget.Toast;

public class StartBeforeEndChecker {
    public static boolean isStartBeforeEnd(long whenItStarts, long whenItEnds, Context thisContext){
        if(whenItStarts<whenItEnds){
            return true;
        }
        else{
            Toast.makeText(thisContext, "Start must come before end.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
