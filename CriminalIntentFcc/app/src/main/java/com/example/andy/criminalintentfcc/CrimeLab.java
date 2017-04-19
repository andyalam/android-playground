package com.example.andy.criminalintentfcc;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.andy.criminalintentfcc.database.CrimeBaseHelper;
import com.example.andy.criminalintentfcc.database.CrimeCursorWrapper;
import com.example.andy.criminalintentfcc.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
        /*for (int i = 0; i < 20; i++) {
            Crime c = new Crime();
            c.setTitle("Crime #" + (i+1));
            c.setSolved(i % 2 == 0);
            c.setSeverity("Misdemeanor");
            mCrimes.add(c);
        }*/
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ? ",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void addCrime(Crime c) {
        ContentValues v = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, v);
    }

    public void updateCrime(Crime c) {
        String uuidString = c.getId().toString();
        ContentValues values = getContentValues(c);
        mDatabase.update(
                CrimeTable.NAME,
                values,
                CrimeTable.Cols.UUID + " = ? ",
                new String[] { uuidString }
        );
    }

    public boolean deleteCrime(UUID id) {
        try {
            mDatabase.delete(
                    CrimeTable.NAME,
                    CrimeTable.Cols.UUID + " = ? ",
                    new String[] { id.toString() }
            );
            return true;
        } catch (Exception e) {
            Log.d("deleteCrime", e.toString());
            return false;
        }
    }

    private static ContentValues getContentValues(Crime c) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, c.getId().toString());
        values.put(CrimeTable.Cols.TITLE, c.getTitle());
        values.put(CrimeTable.Cols.DATE, c.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, c.isSolved() ? 1: 0);
        values.put(CrimeTable.Cols.SEVERITY, c.getSeverity());
        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor c = mDatabase.query(
                CrimeTable.NAME,
                null,  // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new CrimeCursorWrapper(c);
    }

}
