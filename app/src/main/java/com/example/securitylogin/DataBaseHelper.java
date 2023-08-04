package com.example.securitylogin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Base64;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "data.db";

    public static final String TABLE_NAME = "user";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_IV = "iv";
    public static final String COLUMN_PASSWORD = "password";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_IV + " BLOB, " +
                COLUMN_PASSWORD + " BLOB)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public long insertUser(String email, String password,byte[] iv) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_IV,iv);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE_NAME, null, values);
    }

    public boolean checkIfUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String[] projection = {COLUMN_EMAIL};
            String selection = COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }


}