package com.austry.content_provider.db;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.austry.content_provider.db.contracts.ArtistContract;
import com.austry.content_provider.db.contracts.ArtistGenreContract;
import com.austry.content_provider.db.contracts.CoverContract;
import com.austry.content_provider.db.contracts.GenreContract;
import com.austry.content_provider.model.Artist;
import com.austry.content_provider.model.Cover;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

@RunWith(RobolectricTestRunner.class)
public class DbBackendTest {

    private DbOpenHelper helper;
    private DbBackend backend;
    private Artist firstArtistFromDb;

    @Before
    public void setUp() {
        helper = new DbOpenHelper(RuntimeEnvironment.application);
        backend = new DbBackend(helper);
    }

    @Test
    public void initDb() {
        assertThat(backend.getArtistsCount()).isEqualTo(0);
    }

    @Test
    public void insertionCount() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Artist testArtist = buildTestArtist();

        backend.insertArtist(testArtist);
        assertThat(getCount(db, ArtistContract.TABLE_NAME)).isEqualTo(1);
    }

    @Test
    public void fullInsert() {
        Artist testArtist = buildTestArtist();

        long artistId = backend.insertArtist(testArtist);

        Artist artistFromDb = getFirstArtistFromDb(artistId);

        assertThat(artistFromDb).isEqualsToByComparingFields(testArtist);

    }

    private Artist buildTestArtist() {
        Artist testArtist = new Artist();
        testArtist.setName("name_me");
        testArtist.setCover(new Cover("url_small", "url_big"));
        testArtist.setAlbums(4);
        testArtist.setTracks(2);
        testArtist.setDescription("desc");
        testArtist.setGenres(asList("rock", "punk"));
        testArtist.setLink("link");
        return testArtist;
    }

    private Artist getFirstArtistFromDb(long artistId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = new String[]{
                ArtistContract.COLUMN_DESCRIPTION,
                ArtistContract.COLUMN_ALBUMS,
                ArtistContract.COLUMN_LINK,
                ArtistContract.COLUMN_NAME,
                ArtistContract.COLUMN_TRACKS,
                ArtistContract.COLUMN_COVER_ID};

        Cursor cursor = db.query(ArtistContract.TABLE_NAME, columns,
                ArtistContract.COLUMN_ID + " = ?",
                new String[]{String.valueOf(artistId)}, null, null, null);
        Artist artist = new Artist();
        artist.setId(artistId);
        if (cursor != null && cursor.moveToFirst()) {

            artist.setDescription(cursor.getString(0));
            artist.setAlbums(cursor.getInt(1));
            artist.setLink(cursor.getString(2));
            artist.setName(cursor.getString(3));
            artist.setTracks(cursor.getInt(4));

            int coverId = cursor.getInt(5);
            artist.setCover(getCoverFromDb(db, coverId));

            artist.setGenres(getGenresFromBd(db, artist.getId()));

            cursor.close();
        }

        return artist;

    }

    private List<String> getGenresFromBd(SQLiteDatabase db, long id) {
        List<String> genres = new LinkedList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ArtistGenreContract.TABLE_NAME + " JOIN " + GenreContract.TABLE_NAME + " ON " +
                ArtistGenreContract.TABLE_NAME + "." + ArtistGenreContract.COLUMN_GENRE_ID + " = " +
                GenreContract.TABLE_NAME + "." + GenreContract.COLUMN_ID);
        qb.appendWhere(ArtistGenreContract.COLUMN_ARTIST_ID + " = " + id);
        Cursor cursor = qb.query(db, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameColumnIndex = 4;
            do {
                genres.add(cursor.getString(nameColumnIndex));
            } while (cursor.moveToNext());
        }

        return genres;
    }

    private Cover getCoverFromDb(SQLiteDatabase db, int coverId) {
        Cover result = new Cover();
        String[] coverColumns = new String[]{
                CoverContract.COLUMN_ID,
                CoverContract.COLUMN_URL_SMALL,
                CoverContract.COLUMN_URL_BIG};
        Cursor cursor = db.query(CoverContract.TABLE_NAME, coverColumns, "id = ?",
                new String[]{String.valueOf(coverId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            result.setId(cursor.getInt(0));
            result.setSmall(cursor.getString(1));
            result.setBig(cursor.getString(2));

            cursor.close();
        }

        return result;
    }


    private int getCount(SQLiteDatabase db, String table) {
        return DbUtils.getResultLongAndClose(
                db.rawQuery("select count(*) from " + table, null)).intValue();
    }

}
