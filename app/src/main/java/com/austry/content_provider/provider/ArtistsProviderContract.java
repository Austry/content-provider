package com.austry.content_provider.provider;

import com.austry.content_provider.db.contracts.ArtistContract;
import com.austry.content_provider.db.contracts.CoverContract;
import com.austry.content_provider.db.contracts.GenreContract;

import static com.austry.content_provider.db.DbUtils.addTablePrefix;

public class ArtistsProviderContract {
    public static final String ID = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ID);
    public static final String NAME = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_NAME);
    public static final String ALBUMS = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_ALBUMS);
    public static final String TRACKS = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_TRACKS);
    public static final String DESCRIPTION = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_DESCRIPTION);
    public static final String LINK = addTablePrefix(ArtistContract.TABLE_NAME, ArtistContract.COLUMN_LINK);
    public static final String URL_SMALL = addTablePrefix(CoverContract.TABLE_NAME, CoverContract.COLUMN_URL_SMALL);
    public static final String URL_BIG = addTablePrefix(CoverContract.TABLE_NAME, CoverContract.COLUMN_URL_BIG);
    public static final String GENRES = "GROUP_CONCAT(" + addTablePrefix(GenreContract.TABLE_NAME, GenreContract.COLUMN_NAME) + ", ',')";
}
