package com.loyid.weatherforecast.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int WEATHER = 100;
    static final int WEATHER_DIR = 101;
    static final int WEATHER_ITEM = 102;

    private WeatherDbHelper mDbHelper = null;

    public WeatherProvider() {
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_DIR);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/#", WEATHER_ITEM);
        return matcher;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case WEATHER:
                break;
            case WEATHER_ITEM:
                String weatherId = uri.getPathSegments().get(1);
                if (selection != null) {
                    selection += " AND " + WeatherContract.WeatherColumns._ID + " = " + weatherId;
                }

            default:
                throw new UnsupportedOperationException("Unknown uri = " + uri);
        }

        rowsDeleted = db.delete(WeatherContract.WeatherColumns.TABLE_NAME, selection, selectionArgs);

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER:
            case WEATHER_DIR:
                return WeatherContract.WeatherColumns.CONTENT_TYPE;
            case WEATHER_ITEM:
                return WeatherContract.WeatherColumns.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri = " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri retUri;

        switch (sUriMatcher.match(uri)) {
            case WEATHER:
                long id = db.insert(WeatherContract.WeatherColumns.TABLE_NAME, null, values);
                if (id > 0) {
                    retUri = WeatherContract.WeatherColumns.buildWeatherUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri = " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherColumns.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case WEATHER:
            case WEATHER_DIR:
                break;
            case WEATHER_ITEM:
                String weatherId = uri.getPathSegments().get(1);
                if (selection != null) {
                    selection += " AND " + WeatherContract.WeatherColumns._ID + " = " + weatherId;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri = " + uri);
        }

        retCursor = mDbHelper.getReadableDatabase().query(
                WeatherContract.WeatherColumns.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherColumns.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherColumns.COLUMN_DATE);
            values.put(WeatherContract.WeatherColumns.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated;

        switch (sUriMatcher.match(uri)) {
            case WEATHER:
            case WEATHER_DIR:
                break;
            case WEATHER_ITEM:
                String weatherId = uri.getPathSegments().get(1);
                if (selection != null) {
                    selection += " AND " + WeatherContract.WeatherColumns._ID + " = " + weatherId;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri = " + uri);
        }

        normalizeDate(values);
        rowUpdated = db.update(WeatherContract.WeatherColumns.TABLE_NAME, values, selection, selectionArgs);

        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowUpdated;
    }
}
