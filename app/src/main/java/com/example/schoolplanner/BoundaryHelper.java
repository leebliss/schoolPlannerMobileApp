package com.example.schoolplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class BoundaryHelper {

    //check is start is before end
    public static boolean isStartBeforeEnd(long whenItStarts, long whenItEnds, Context context){
        if(whenItStarts<whenItEnds){
            return true;
        }
        else{
            Toast.makeText(context, "Start must come before end.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
