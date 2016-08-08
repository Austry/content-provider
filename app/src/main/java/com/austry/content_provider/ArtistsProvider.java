package com.austry.content_provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.austry.content_provider.model.Artist;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Austry on 05/08/16.
 */
public class ArtistsProvider extends ContentProvider {
    private static final String TAG = "ArtistsProvider";

    @Override
    public boolean onCreate() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream jsonInputStream = getContext().getResources().openRawResource(R.raw.artists);
        try {
            List<Artist> artists = mapper.readValue(jsonInputStream, new TypeReference<List<Artist>>() {});
            Log.d(TAG, "onCreate: artists " + artists.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate : ");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {

        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
