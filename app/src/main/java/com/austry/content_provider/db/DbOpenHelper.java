package com.austry.content_provider.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.austry.content_provider.db.contracts.ArtistContract;
import com.austry.content_provider.db.contracts.ArtistGenreContract;
import com.austry.content_provider.db.contracts.CoverContract;
import com.austry.content_provider.db.contracts.GenreContract;

public class DbOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "artists";
    private final static int DB_VERSION = 1;

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + ArtistContract.TABLE_NAME + "(" +
                ArtistContract.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                ArtistContract.COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                ArtistContract.COLUMN_TRACKS + " INTEGER, " +
                ArtistContract.COLUMN_ALBUMS + " INTEGER, " +
                ArtistContract.COLUMN_LINK + " TEXT, " +
                ArtistContract.COLUMN_DESCRIPTION + " TEXT, " +
                ArtistContract.COLUMN_COVER_ID + " INTEGER " +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + CoverContract.TABLE_NAME + "(" +
                CoverContract.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                CoverContract.COLUMN_URL_BIG + " TEXT, " +
                CoverContract.COLUMN_URL_SMALL + " TEXT " +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + GenreContract.TABLE_NAME + "(" +
                GenreContract.COLUMN_ID + " INTEGER PRIMARY KEY," +
                GenreContract.COLUMN_NAME + " TEXT UNIQUE NOT NULL" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + ArtistGenreContract.TABLE_NAME + "(" +
                ArtistGenreContract.COLUMN_ARTIST_ID + " INTEGER NOT NULL," +
                ArtistGenreContract.COLUMN_GENRE_ID + " INTEGER NOT NULL," +
                "PRIMARY KEY ("+ArtistGenreContract.COLUMN_ARTIST_ID+","+ArtistGenreContract.COLUMN_GENRE_ID+")"+
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //dev me
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GenreContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistGenreContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CoverContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
