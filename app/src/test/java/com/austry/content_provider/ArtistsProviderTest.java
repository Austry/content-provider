package com.austry.content_provider;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.austry.content_provider.db.DbBackend;
import com.austry.content_provider.db.DbOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ArtistsProviderTest {
    private DbOpenHelper helper;
    private DbBackend backend;
    private ArtistsProvider artistsProvider;


    @Before
    public void setUp() {
        backend = mock(DbBackend.class);
        artistsProvider = new ArtistsProvider(backend);

    }

    @Test
    public void illegalQueryUrl() {
        String testWrongUriString = "content://awsome.stuff/buff";
        Uri wrongUri = Uri.parse(testWrongUriString);
        try {
            artistsProvider.query(wrongUri, null, null, null, null);
        } catch (Exception e) {
            assertThat(e).hasMessage("Wrong URI: " + testWrongUriString)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // не нужный тест
    @Test
    public void queryAllArtists() {
        Cursor mockCursor = mock(Cursor.class);
        when(backend.getAllArtists(null, null, null, null)).thenReturn(mockCursor);
        Cursor cursor = artistsProvider.query(ArtistsProvider.ARTIST_CONTENT_URI, null, null, null, null);
    }


}