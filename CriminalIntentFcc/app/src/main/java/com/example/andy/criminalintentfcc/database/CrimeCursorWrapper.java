package com.example.andy.criminalintentfcc.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.example.andy.criminalintentfcc.database.CrimeDbSchema.CrimeTable;

import com.example.andy.criminalintentfcc.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor c) {
        super(c);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String severity = getString(getColumnIndex(CrimeTable.Cols.SEVERITY));

        Crime crime = new Crime(UUID.fromString(uuidString), severity);
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        //crime.setSeverity(severity);

        return crime;
    }
}
