package com.austry.content_provider.provider;

import android.net.Uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.austry.content_provider.db.DbBackend;
import com.austry.content_provider.db.DbOpenHelper;
import com.austry.content_provider.provider.ArtistsProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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


}