package com.austry.content_provider.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.austry.content_provider.R;
import com.austry.content_provider.db.DbBackend;
import com.austry.content_provider.model.Artist;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ArtistsProvider extends ContentProvider {
    private static final String TAG = "ArtistsProvider";

    static final String AUTHORITY = "com.austry.artistsProvider";
    static final String ARTISTS_PATH = "artists";
    public static final Uri ARTIST_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ARTISTS_PATH);

    static final String ARTIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + ARTISTS_PATH;

    static final int ARTISTS_URI_INDEX = 1;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, ARTISTS_PATH, ARTISTS_URI_INDEX);
    }

    private DbBackend dbBackend;

    @VisibleForTesting
    ArtistsProvider(DbBackend dbBackend) {
        this.dbBackend = dbBackend;
    }


    @Override
    public boolean onCreate() {
        dbBackend = new DbBackend(getContext());
        ObjectMapper mapper = new ObjectMapper();
        InputStream jsonInputStream = getContext().getResources().openRawResource(R.raw.artists);
        try {
            List<Artist> artists = mapper.readValue(jsonInputStream, new TypeReference<List<Artist>>(){});
            for (int i = 0, size = artists.size(); i < size; i++) {
                dbBackend.insertArtist(artists.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        checkUri(uri);
        return dbBackend.getAllArtists(projection, selection, selectionArgs, orderBy);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        String result = "";
        switch (uriMatcher.match(uri)) {
            case ARTISTS_URI_INDEX:
                result = ARTIST_CONTENT_TYPE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        return result;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        checkUri(uri);
        dbBackend.insertArtist(contentValues);
        tryNotifyChanges(uri);
        return uri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        checkUri(uri);
        int rowAffected = dbBackend.delete(selection,selectionArgs);
        tryNotifyChanges(uri);
        return rowAffected;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        checkUri(uri);
        int affected = dbBackend.update(contentValues, selection, selectionArgs);
        tryNotifyChanges(uri);
        return affected;
    }

    //нужно ли это?
    private void tryNotifyChanges(Uri uri) {
        Context context = getContext();
        if (context != null) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                resolver.notifyChange(uri, null);
            }
        }
    }

    private void checkUri(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ARTISTS_URI_INDEX:
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
    }

}
