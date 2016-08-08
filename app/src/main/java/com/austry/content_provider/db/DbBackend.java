package com.austry.content_provider.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.austry.content_provider.db.contracts.ArtistContract;
import com.austry.content_provider.db.contracts.ArtistGenreContract;
import com.austry.content_provider.db.contracts.CoverContract;
import com.austry.content_provider.db.contracts.GenreContract;
import com.austry.content_provider.model.Artist;
import com.austry.content_provider.model.Cover;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DbBackend {
    private static final String TAG = "DbBackend";

    private static final String ALL_ARTISTS_TABLES = ArtistContract.TABLE_NAME +
            " JOIN " + CoverContract.TABLE_NAME + " ON " + addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_COVER_ID) + " = " + addTablePrefix(CoverContract.TABLE_NAME, CoverContract.COLUMN_ID) +
            " JOIN " + ArtistGenreContract.TABLE_NAME + " ON " + addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ID) + " = " + addTablePrefix(ArtistGenreContract.TABLE_NAME, ArtistGenreContract.COLUMN_ARTIST_ID) +
            " JOIN " + GenreContract.TABLE_NAME + " ON " + addTablePrefix(ArtistGenreContract.TABLE_NAME, ArtistGenreContract.COLUMN_GENRE_ID) + " = " + addTablePrefix(GenreContract.TABLE_NAME, GenreContract.COLUMN_ID) +
            " GROUP BY " + addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ID);

    private static final String[] ALL_ARTISTS_COLUMNS = new String[]{
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ID),
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_NAME),
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ALBUMS),
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_TRACKS),
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_DESCRIPTION),
            addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_LINK),
            addTablePrefix(CoverContract.TABLE_NAME, CoverContract.COLUMN_URL_SMALL),
            addTablePrefix(CoverContract.TABLE_NAME, CoverContract.COLUMN_URL_BIG),
            "GROUP_CONCAT(" + addTablePrefix(GenreContract.TABLE_NAME, GenreContract.COLUMN_NAME) + ", ',')"};

    private DbOpenHelper dbHelper;

    DbBackend(Context context) {
        dbHelper = new DbOpenHelper(context);
    }

    @VisibleForTesting
    DbBackend(DbOpenHelper helper) {
        dbHelper = helper;
    }

    public Cursor getAllArtists() {
        SQLiteDatabase base = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(ALL_ARTISTS_TABLES);

        return qb.query(base, ALL_ARTISTS_COLUMNS, null, null, null, null, null);
    }

    public void delete(long artistId) {
        SQLiteDatabase base = dbHelper.getWritableDatabase();
        base.beginTransaction();

        try {
            long coverId = getCoverId(base, artistId);
            base.delete(CoverContract.TABLE_NAME,
                    CoverContract.COLUMN_ID + " = ? ",
                    new String[]{String.valueOf(coverId)});
            base.delete(ArtistContract.TABLE_NAME,
                    ArtistContract.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(artistId)});
            base.setTransactionSuccessful();
        } finally {
            base.endTransaction();
        }
    }

    public void update(Artist artist) {
        SQLiteDatabase base = dbHelper.getWritableDatabase();
        base.beginTransaction();
        try{
            long coverId = getCoverId(base, artist.getId());
            base.update(CoverContract.TABLE_NAME,
                    fillCoverValues(artist.getCover()),
                    CoverContract.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(coverId)});
            base.update(ArtistContract.TABLE_NAME,
                    fillArtistValues(artist, coverId),
                    ArtistContract.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(artist.getId())});
            base.setTransactionSuccessful();
        }finally {
            base.endTransaction();
        }

    }

    private long getCoverId(SQLiteDatabase base, long artistId) {
        long result = -1;

        Cursor cursor = base.query(ArtistContract.TABLE_NAME,
                new String[]{ArtistContract.COLUMN_COVER_ID},
                ArtistContract.COLUMN_ID + " = ?",
                new String[]{String.valueOf(artistId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(0);
        }

        cursor.close();
        return result;
    }

    public long insertArtist(Artist artist) {
        SQLiteDatabase base = dbHelper.getWritableDatabase();
        long artistId = -1;
        base.beginTransaction();
        try {
            long coverId = base.insert(CoverContract.TABLE_NAME, null, fillCoverValues(artist.getCover()));

            List<Long> genresIds = getGenresIds(base, artist.getGenres());
            artistId = base.insert(ArtistContract.TABLE_NAME, null, fillArtistValues(artist, coverId));
            mapManyToMany(base, artistId, genresIds);

            base.setTransactionSuccessful();
            artist.setId(artistId);
        } finally {
            base.endTransaction();
        }
        return artistId;
    }

    public long getArtistsCount() {
        long result = 0;
        SQLiteDatabase base = dbHelper.getReadableDatabase();
        Cursor cursor = base.rawQuery("select count(id) from " + ArtistContract.TABLE_NAME, null);
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(0);
            cursor.close();
        }
        return result;
    }


    private List<Long> getGenresIds(SQLiteDatabase base, List<String> genres) {
        List<Long> results = new LinkedList<>();
        if (genres != null) {
            Cursor cursor = getGenresCursor(base, genres);
            if (cursor != null && cursor.moveToFirst()) {
                List<String> existedGenres = new ArrayList<>();
                do {
                    Log.d(TAG, "getGenresIds: id = " + cursor.getInt(0));
                    Log.d(TAG, "getGenresIds: name = " + cursor.getString(1));
                    results.add((long) cursor.getInt(0));
                    existedGenres.add(cursor.getString(1));
                } while (cursor.moveToNext());
                cursor.close();

                putNewGenresInBase(base, genres, results, existedGenres);
            } else {
                putNewGenresInBase(base, genres, results, null);
            }

        }
        return results;
    }

    private void putNewGenresInBase(SQLiteDatabase base, List<String> genres, List<Long> results, List<String> existedGenres) {
        int genresSize = genres.size();
        int existedGenresSize = existedGenres != null ? existedGenres.size() : 0;
        if (existedGenresSize != genresSize) {
            for (int i = 0; i < genresSize; i++) {
                String currentGenre = genres.get(i);
                if (existedGenres != null) {
                    if (!existedGenres.contains(currentGenre)) {
                        results.add(putGenreInBase(base, currentGenre));
                    }
                } else {
                    results.add(putGenreInBase(base, currentGenre));
                }
            }
        }
    }

    private void mapManyToMany(SQLiteDatabase base, long artistId, List<Long> genresIds) {
        for (int i = 0, size = genresIds.size(); i < size; i++) {
            ContentValues cv = new ContentValues();
            cv.put(ArtistGenreContract.COLUMN_ARTIST_ID, artistId);
            cv.put(ArtistGenreContract.COLUMN_GENRE_ID, genresIds.get(i));
            base.insert(ArtistGenreContract.TABLE_NAME, null, cv);
        }
    }

    private long putGenreInBase(SQLiteDatabase base, String currentGenre) {
        ContentValues cv = new ContentValues();
        cv.put(GenreContract.COLUMN_NAME, currentGenre);
        return base.insert(GenreContract.TABLE_NAME, null, cv);
    }

    private ContentValues fillCoverValues(Cover cover) {
        ContentValues values = new ContentValues();
        values.put(CoverContract.COLUMN_URL_BIG, cover.getBig());
        values.put(CoverContract.COLUMN_URL_SMALL, cover.getSmall());
        return values;
    }

    private ContentValues fillArtistValues(Artist artist, long coverId) {

        ContentValues values = new ContentValues();
        values.put(ArtistContract.COLUMN_NAME, artist.getName());
        values.put(ArtistContract.COLUMN_ALBUMS, artist.getAlbums());
        values.put(ArtistContract.COLUMN_TRACKS, artist.getTracks());
        values.put(ArtistContract.COLUMN_DESCRIPTION, artist.getDescription());
        values.put(ArtistContract.COLUMN_LINK, artist.getLink());
        values.put(ArtistContract.COLUMN_COVER_ID, coverId);
        return values;
    }


    private Cursor getGenresCursor(SQLiteDatabase base, List<String> genres) {
        int len = genres.size();
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return base.query(GenreContract.TABLE_NAME,
                new String[]{GenreContract.COLUMN_ID, GenreContract.COLUMN_NAME},
                GenreContract.COLUMN_NAME + " IN (" + sb.toString() + ")",
                genres.toArray(new String[genres.size()]),
                null, null, null);
    }

    private static String addTablePrefix(String tableName, String columnAlbums) {
        return tableName + "." + columnAlbums;
    }


}
