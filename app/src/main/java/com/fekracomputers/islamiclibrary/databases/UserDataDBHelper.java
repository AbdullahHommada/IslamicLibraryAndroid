package com.fekracomputers.islamiclibrary.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fekracomputers.islamiclibrary.R;
import com.fekracomputers.islamiclibrary.model.Bookmark;
import com.fekracomputers.islamiclibrary.model.BooksCollection;
import com.fekracomputers.islamiclibrary.model.Highlight;
import com.fekracomputers.islamiclibrary.model.PageInfo;

import java.util.ArrayList;

/**
 * Database Helper class to Store user data (BookMarks ,Notes ,Highlights and comments)
 * one instance per book
 * one file on disk per book
 */

public class UserDataDBHelper {
    private static final String TAG = "UserDataDBHelper";
    private static GlobalUserDBHelper sGlobalUserDBHelper;
    private final Context context;
    private int bookId;

    public UserDataDBHelper(Context context, int bookId) {

        this.context = context;
        this.bookId = bookId;
    }


    public static synchronized UserDataDBHelper getInstance(Context context, int bookId) {
        if (sGlobalUserDBHelper == null) {
            sGlobalUserDBHelper = new GlobalUserDBHelper(context);
        }
        return new UserDataDBHelper(context, bookId);
    }

    public static synchronized GlobalUserDBHelper getInstance(Context context) {
        if (sGlobalUserDBHelper == null) {
            sGlobalUserDBHelper = new GlobalUserDBHelper(context);
        }
        return sGlobalUserDBHelper;
    }


    public void addBookmark(int pageId) {
        sGlobalUserDBHelper.addBookmark(pageId, bookId);
    }

    public void RemoveBookmark(int pageId) {
        sGlobalUserDBHelper.RemoveBookmark(pageId, bookId);
    }

    public boolean isPageBookmarked(int pageId) {
        return sGlobalUserDBHelper.isPageBookmarked(pageId, bookId);
    }

    public Highlight getHighlightById(int highlightId, int pageRowId) {
        return sGlobalUserDBHelper.getHighlightById(highlightId, pageRowId, context, bookId);
    }

    public void addNoteToHighlight(Highlight mSelectedHighlight) {
        sGlobalUserDBHelper.addNoteToHighlight(mSelectedHighlight, bookId);
    }

    public void setSerializedHighlights(PageInfo pageInfo, String serializedHighlights) {
        sGlobalUserDBHelper.setSerializedHighlights(pageInfo, serializedHighlights, bookId);
    }

    public String getSerializedHighlights(int pageRowId) {
        return sGlobalUserDBHelper.getSerializedHighlights(pageRowId, bookId);
    }

    public String getDisplayPreferenceValue(String preferenceKey, String s) {
        return sGlobalUserDBHelper.getDisplayPreferenceValue(preferenceKey, s, bookId);
    }

    public void setDisplayPreferenceValue(String preferenceKey, String s) {
        sGlobalUserDBHelper.setDisplayPreferenceValue(preferenceKey, s, bookId);
    }

    public ArrayList<Bookmark> getAllBookmarks(String order) {
        return sGlobalUserDBHelper.getAllBookmarks(order, context, bookId);
    }

    public ArrayList<Highlight> getAllHighlights() {
        return sGlobalUserDBHelper.getAllHighlights(context, bookId);
    }

    public void logBookAccess() {
        sGlobalUserDBHelper.logBookAccess(bookId);
    }

    public void logPageAccess(PageInfo pageId) {
        sGlobalUserDBHelper.logPageAccess(pageId, bookId);
    }


    public PageInfo getLastPageInfo() {
        return sGlobalUserDBHelper.getLastPageInfo(bookId, context);
    }


    public static class GlobalUserDBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "user_data";
        private static final int DATABASE_VERSION = 2;
        private final Context context;

        private GlobalUserDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }


        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(UserDataDBContract.BookmarkEntry.CREATE_STATEMENT);
            sqLiteDatabase.execSQL(UserDataDBContract.SerializedHighlightEntry.CREATE_STATEMENT);
            sqLiteDatabase.execSQL(UserDataDBContract.HighlightEntry.TABLE_HIGHLIGHTS_CREATE);
            sqLiteDatabase.execSQL(UserDataDBContract.DisplayPreferenceEntry.CREATE_STATEMENT);
            sqLiteDatabase.execSQL(UserDataDBContract.AccessInformationEntry.CREATE_STATEMENT);
            version2(sqLiteDatabase);
        }

        private void version2(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(UserDataDBContract.BooksCollectionEntry.CREATE_STATEMENT);
            sqLiteDatabase.execSQL(UserDataDBContract.BooksCollectionJoinEntry.CREATE_STATEMENT);
            sqLiteDatabase.execSQL(SQL.CREATE_INDEX_IF_NOT_EXISTS +
                    UserDataDBContract.BooksCollectionJoinEntry.Table_NAME +
                    "collectionIdIndex" + SQL.ON +
                    UserDataDBContract.BooksCollectionJoinEntry.Table_NAME +
                    "(" + UserDataDBContract.BooksCollectionJoinEntry.COLLECTION_ID + ")");
            sqLiteDatabase.execSQL(SQL.CREATE_INDEX_IF_NOT_EXISTS +
                    UserDataDBContract.BooksCollectionJoinEntry.Table_NAME +
                    "bookIdIndex" + SQL.ON +
                    UserDataDBContract.BooksCollectionJoinEntry.Table_NAME +
                    "(" + UserDataDBContract.BooksCollectionJoinEntry.BOOK_ID + ")");
            insertBasicCollections(sqLiteDatabase);
        }

        private void insertBasicCollections(SQLiteDatabase sqLiteDatabase) {

            String[] standardBooksCollection = context.getResources().getStringArray(R.array.standard_books_collection);
            int[] standardBooksCollectionAUtmaticIds = context.getResources().getIntArray(R.array.standard_books_collection_automatic_id);
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < 3; i++) {
                String booksCollection = standardBooksCollection[i];
                int booksCollectionAutomaticId = standardBooksCollectionAUtmaticIds[i];
                contentValues.put(UserDataDBContract.BooksCollectionEntry.COLUMN_NAME, booksCollection);
                contentValues.put(UserDataDBContract.BooksCollectionEntry.COLUMN_AUTOMATIC_ID, booksCollectionAutomaticId);
                sqLiteDatabase.insert(UserDataDBContract.BooksCollectionEntry.Table_NAME,
                        null,
                        contentValues);
            }
            contentValues = new ContentValues();
            contentValues.put(UserDataDBContract.BooksCollectionEntry.COLUMN_NAME, context.getResources().getString(R.string.book_collection_favourite));
            sqLiteDatabase.insert(UserDataDBContract.BooksCollectionEntry.Table_NAME,
                    null,
                    contentValues);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    version2(sqLiteDatabase);
                    // we want both updates, so no break statement here...
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        void addBookmark(int pageNumber, int bookId) {
            ContentValues bookmark = new ContentValues();
            bookmark.put(UserDataDBContract.BookmarkEntry.COLUMN_NAME_BOOK_ID, bookId);
            bookmark.put(UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID, pageNumber);
            getWritableDatabase().insert(UserDataDBContract.BookmarkEntry.TABLE_NAME, null, bookmark);
        }

        void RemoveBookmark(int pageId, int bookId) {
            getWritableDatabase().
                    delete(UserDataDBContract.BookmarkEntry.TABLE_NAME,
                            UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID + "=? and " + UserDataDBContract.BookmarkEntry.COLUMN_NAME_BOOK_ID + "=?",
                            new String[]{String.valueOf(pageId), String.valueOf(bookId)});
        }

        boolean isPageBookmarked(int pageNumber, int bookId) {
            return getReadableDatabase().query(
                    UserDataDBContract.BookmarkEntry.TABLE_NAME, new String[]{"1"},
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID + "=? and " + UserDataDBContract.BookmarkEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(pageNumber), String.valueOf(bookId)},
                    null, null, null).moveToFirst();

        }


        /**
         * @param order    must be {@link UserDataDBContract.BookmarkEntry#COLUMN_NAME_PAGE_ID} or
         *                 {@link UserDataDBContract.BookmarkEntry#COLUMN_NAME_TIME_STAMP}
         * @param mContext
         * @param bookId
         * @return list of all book marks in the book ordered by page containing the book mark
         */
        public ArrayList<Bookmark> getAllBookmarks(String order, Context mContext, int bookId) throws IllegalArgumentException {
            if (!order.equals(UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID) ||
                    !order.equals(UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID)) {
                throw new IllegalArgumentException("order must be {@link UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID} or" +
                        " UserDataDBContract.BookmarkEntry.COLUMN_NAME_TIME_STAMP}");

            }

            ArrayList<Bookmark> bookmarksList = new ArrayList<>();

            Cursor c = getReadableDatabase().query(UserDataDBContract.BookmarkEntry.TABLE_NAME,
                    new String[]{UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID,
                            UserDataDBContract.BookmarkEntry.COLUMN_NAME_TIME_STAMP},
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(bookId)},
                    null,
                    null,
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID
            );
            final int INDEX_PAGE_ID = c.getColumnIndex(UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID);
            final int INDEX_TIME_STAMP = c.getColumnIndex(UserDataDBContract.BookmarkEntry.COLUMN_NAME_TIME_STAMP);
            BookDatabaseHelper bookDatabaseHelper = BookDatabaseHelper.getInstance(mContext, bookId);
            while (c.moveToNext()) {
                int pageId = c.getInt(INDEX_PAGE_ID);
                PageInfo pageInfo = bookDatabaseHelper.getPageInfoByPageId(pageId);

                bookmarksList.add(new Bookmark(bookId, pageId, pageInfo.pageNumber, pageInfo.partNumber, c.getString(INDEX_TIME_STAMP), bookDatabaseHelper.getParentTitle(pageId)));
            }
            c.close();
            return bookmarksList;
        }

        void setSerializedHighlights(PageInfo pageInfo, String serializedHighlights, int bookId) {

            ContentValues highlights = new ContentValues();
            highlights.put(UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_BOOK_ID, bookId);
            highlights.put(UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_PAGE_ID, pageInfo.pageId);
            highlights.put(UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_SERIALIZED_HIGHLIGHTS, serializedHighlights);
            getWritableDatabase().replace(UserDataDBContract.SerializedHighlightEntry.TABLE_NAME, null, highlights);
            deserializeHighlightsAndSave(serializedHighlights, pageInfo, bookId);
            //getReadableDatabase().insert(UserDataDBContract.SerializedHighlightEntry.TABLE_NAME, null, highlights);

        }

        public ArrayList<Highlight> getAllHighlights(Context mContext, int bookId) {
            ArrayList<Highlight> highlightArrayList = new ArrayList<>();

            Cursor c = getReadableDatabase().query(UserDataDBContract.HighlightEntry.TABLE_NAME,
                    new String[]{
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_PAGE_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_CLASS_NAME,
                            UserDataDBContract.HighlightEntry.COLUMN_CONTAINER_ELEMENT_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_TEXT,
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_TIME_STAMP,
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_ROW_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_NOTE_TEXT
                    },
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(bookId)},
                    null,
                    null,
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID
            );

            final int INDEX_PAGE_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_PAGE_ID);
            final int INDEX_HIGHLIGHT_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID);
            final int INDEX_CLASS_NAME = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_CLASS_NAME);
            final int INDEX_ELEMENT_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_CONTAINER_ELEMENT_ID);
            final int INDEX_TEXT = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_TEXT);
            final int INDEX_NOTE_TEXT = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NOTE_TEXT);
            final int INDEX_TIME_STAMP = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_TIME_STAMP);
            final int INDEX_ROW_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_ROW_ID);
            BookDatabaseHelper bookDatabaseHelper = BookDatabaseHelper.getInstance(mContext, bookId);

            while (c.moveToNext()) {
                int pageId = c.getInt(INDEX_PAGE_ID);
                PageInfo pageInfo = bookDatabaseHelper.getPageInfoByPageId(pageId);
                int highlightId = c.getInt(INDEX_HIGHLIGHT_ID);
                String className = c.getString(INDEX_CLASS_NAME);
                int elementId = c.getInt(INDEX_ELEMENT_ID);
                String timeStamp = c.getString(INDEX_TIME_STAMP);
                String text = c.getString(INDEX_TEXT);
                String noteText = c.getString(INDEX_NOTE_TEXT);
                long rowId = c.getLong(INDEX_ROW_ID);

                highlightArrayList.add(new Highlight(text, highlightId, className, elementId, timeStamp, bookId, pageId, pageInfo.partNumber, pageInfo.pageNumber, bookDatabaseHelper.getParentTitle(pageId), rowId, noteText));
            }
            c.close();
            return highlightArrayList;
        }

        private void deserializeHighlightsAndSave(String serializedHighlights, PageInfo pageInfo, int bookId) {
            ArrayList<ContentValues> highlights = Highlight.deserializeGeneric(serializedHighlights, pageInfo, ContentValues.class, bookId);
            ArrayList<Integer> existingHighlightsId = new ArrayList<>(highlights.size());

            for (ContentValues highlight_current : highlights) {
                existingHighlightsId.add(highlight_current.getAsInteger(UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID));
            }
            String existingHighlightsIdString = existingHighlightsId.toString();

            existingHighlightsIdString = existingHighlightsIdString.replace('[', '(');
            existingHighlightsIdString = existingHighlightsIdString.replace(']', ')');
            SQLiteDatabase db = getWritableDatabase();

            db.beginTransaction();
            try {
                db.delete(UserDataDBContract.HighlightEntry.TABLE_NAME,
                        UserDataDBContract.HighlightEntry.COLUMN_NAME_BOOK_ID + "=?" + " and " +
                                UserDataDBContract.HighlightEntry.COLUMN_NAME_PAGE_ID + "=?" + " and "
                                + UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID + " NOT IN  " + existingHighlightsIdString,
                        new String[]{String.valueOf(bookId), String.valueOf(pageInfo.pageId)}
                );

                for (ContentValues highlight : highlights) {
                    db.insertWithOnConflict(
                            UserDataDBContract.HighlightEntry.TABLE_NAME,
                            null,
                            highlight,
                            SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(TAG, "deserializeHighlightsAndSave: ", e);
            } finally {
                db.endTransaction();
            }

        }

        String getSerializedHighlights(int mPageId, int bookId) {

            Cursor c = getReadableDatabase().query(UserDataDBContract.SerializedHighlightEntry.TABLE_NAME,
                    new String[]{UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_SERIALIZED_HIGHLIGHTS},
                    UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_PAGE_ID + "=? and " +
                            UserDataDBContract.SerializedHighlightEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(mPageId), String.valueOf(bookId)}, null, null, null
            );
            String serializedHighlights = "";
            if (c.moveToFirst()) {
                serializedHighlights = c.getString(0);
            }
            c.close();
            return serializedHighlights;
        }

        public Highlight getHighlightById(int highlightId, int pageId, Context mContext, int bookId) {
            Cursor c = getReadableDatabase().query(UserDataDBContract.HighlightEntry.TABLE_NAME,
                    new String[]{
                            UserDataDBContract.HighlightEntry.COLUMN_CLASS_NAME,
                            UserDataDBContract.HighlightEntry.COLUMN_CONTAINER_ELEMENT_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_TEXT,
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_TIME_STAMP,
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_ROW_ID,
                            UserDataDBContract.HighlightEntry.COLUMN_NOTE_TEXT
                    },
                    UserDataDBContract.HighlightEntry.COLUMN_NAME_BOOK_ID + "=?" + " and " +
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_PAGE_ID + "=?" + " and " +
                            UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID + "=?",
                    new String[]{String.valueOf(bookId), String.valueOf(pageId), String.valueOf(highlightId)},
                    null,
                    null,
                    UserDataDBContract.BookmarkEntry.COLUMN_NAME_PAGE_ID
            );


            final int INDEX_CLASS_NAME = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_CLASS_NAME);
            final int INDEX_ELEMENT_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_CONTAINER_ELEMENT_ID);
            final int INDEX_TEXT = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_TEXT);
            final int INDEX_NOTE_TEXT = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NOTE_TEXT);
            final int INDEX_TIME_STAMP = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_TIME_STAMP);
            final int INDEX_ROW_ID = c.getColumnIndex(UserDataDBContract.HighlightEntry.COLUMN_NAME_ROW_ID);
            BookDatabaseHelper bookDatabaseHelper = BookDatabaseHelper.getInstance(mContext, bookId);
            Highlight highlight = null;
            if (c.moveToFirst()) {
                PageInfo pageInfo = bookDatabaseHelper.getPageInfoByPageId(pageId);
                String className = c.getString(INDEX_CLASS_NAME);
                int elementId = c.getInt(INDEX_ELEMENT_ID);
                String timeStamp = c.getString(INDEX_TIME_STAMP);
                String text = c.getString(INDEX_TEXT);
                String noteText = c.getString(INDEX_NOTE_TEXT);
                long rowId = c.getLong(INDEX_ROW_ID);
                highlight = new Highlight(text, highlightId, className, elementId, timeStamp, bookId, pageId, pageInfo.partNumber, pageInfo.pageNumber, bookDatabaseHelper.getParentTitle(pageId), rowId, noteText);
            }
            c.close();
            return highlight;
        }

        void addNoteToHighlight(@NonNull Highlight highlight, int bookId) {
            if (highlight.hasNote()) {
                ContentValues highlightContent = new ContentValues();
                highlightContent.put(UserDataDBContract.HighlightEntry.COLUMN_NOTE_TEXT, highlight.noteText);
                getWritableDatabase().update(UserDataDBContract.HighlightEntry.TABLE_NAME, highlightContent,
                        UserDataDBContract.HighlightEntry.COLUMN_NAME_BOOK_ID + "=? and " +
                                UserDataDBContract.HighlightEntry.COLUMN_NAME_HIGHLIGHT_ID + "=? and " +
                                UserDataDBContract.HighlightEntry.COLUMN_NAME_PAGE_ID + "=?",
                        new String[]{String.valueOf(bookId), String.valueOf(highlight.id), String.valueOf(highlight.pageInfo.pageId)}
                );
            }

        }

        String getDisplayPreferenceValue(String key, String defaultValue, int bookId) {
            Cursor c = getReadableDatabase()
                    .query(UserDataDBContract.DisplayPreferenceEntry.Table_NAME,
                            new String[]{UserDataDBContract.DisplayPreferenceEntry.COLUMN_VALUE},
                            UserDataDBContract.DisplayPreferenceEntry.COLUMN_NAME_BOOK_ID + "=? and " +
                                    UserDataDBContract.DisplayPreferenceEntry.COLUMN_KEY + "=?",
                            new String[]{String.valueOf(bookId), key},
                            null,
                            null,
                            null);
            String value;
            if (c.moveToFirst()) {
                value = c.getString(0);
            } else {
                value = defaultValue;
                setDisplayPreferenceValue(key, value, bookId);
            }
            c.close();
            return value;
        }

        public void setDisplayPreferenceValue(String key, String value, int bookId) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(UserDataDBContract.DisplayPreferenceEntry.COLUMN_NAME_BOOK_ID, bookId);
            contentValue.put(UserDataDBContract.DisplayPreferenceEntry.COLUMN_KEY, key);
            contentValue.put(UserDataDBContract.DisplayPreferenceEntry.COLUMN_VALUE, value);
            getWritableDatabase().insertWithOnConflict(UserDataDBContract.DisplayPreferenceEntry.Table_NAME,
                    null, contentValue, SQLiteDatabase.CONFLICT_REPLACE);
        }


        void logBookAccess(int bookId) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValue = new ContentValues();
            contentValue.put(UserDataDBContract.AccessInformationEntry.COLUMN_NAME_BOOK_ID, bookId);
            db.insertWithOnConflict(UserDataDBContract.AccessInformationEntry.Table_NAME,
                    null,
                    contentValue,
                    SQLiteDatabase.CONFLICT_IGNORE);

            db.execSQL(" UPDATE " +
                            UserDataDBContract.AccessInformationEntry.Table_NAME +
                            " SET " + UserDataDBContract.AccessInformationEntry.COLUMN_ACCESS_COUNT + "=" +
                            UserDataDBContract.AccessInformationEntry.COLUMN_ACCESS_COUNT + "+1" +
                            SQL.WHERE + UserDataDBContract.AccessInformationEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(bookId)});
        }

        void logPageAccess(PageInfo pageInfo, int bookId) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValue = new ContentValues();
            contentValue.put(UserDataDBContract.AccessInformationEntry.COLUMN_NAME_BOOK_ID, bookId);
            db.insertWithOnConflict(UserDataDBContract.AccessInformationEntry.Table_NAME,
                    null,
                    contentValue,
                    SQLiteDatabase.CONFLICT_IGNORE);

            db.execSQL(" UPDATE " +
                            UserDataDBContract.AccessInformationEntry.Table_NAME +
                            " SET " + UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_ID + "=?" + SQL.COMMA +
                            UserDataDBContract.AccessInformationEntry.LAST_OPENED_PART_NUMBER + "=?" + SQL.COMMA +
                            UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_NUMBER + "=?" + SQL.COMMA +
                            UserDataDBContract.AccessInformationEntry.LAST_OPENED_TIME_STAMP + " = datetime('now','localtime')" +
                            SQL.WHERE + UserDataDBContract.AccessInformationEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(pageInfo.pageId),
                            String.valueOf(pageInfo.partNumber),
                            String.valueOf(pageInfo.pageNumber),
                            String.valueOf(bookId)});
        }

        public PageInfo getLastPageInfo(int bookId, Context context) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.query(UserDataDBContract.AccessInformationEntry.Table_NAME,
                    new String[]{UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_ID,
                            UserDataDBContract.AccessInformationEntry.LAST_OPENED_PART_NUMBER,
                            UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_NUMBER
                    },
                    UserDataDBContract.AccessInformationEntry.COLUMN_NAME_BOOK_ID + "=?",
                    new String[]{String.valueOf(bookId)}, null, null, null
            );
            final int LAST_OPENED_COULMN_INDEX = c.getColumnIndex(UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_ID);

            PageInfo pageInfo;
            if (c.moveToFirst() && !c.isNull(LAST_OPENED_COULMN_INDEX)) {

                pageInfo = new PageInfo(
                        c.getInt(LAST_OPENED_COULMN_INDEX),
                        c.getInt(c.getColumnIndex(UserDataDBContract.AccessInformationEntry.LAST_OPENED_PART_NUMBER)),
                        c.getInt(c.getColumnIndex(UserDataDBContract.AccessInformationEntry.LAST_OPENED_PAGE_NUMBER))
                );
            } else {
                BookDatabaseHelper bookDatabaseHelper = BookDatabaseHelper.getInstance(context, bookId);
                pageInfo = bookDatabaseHelper.getFirstPageInfo();
            }
            c.close();
            return pageInfo;

        }

        public ArrayList<BooksCollection> getBooksCollections(boolean viewdOnly) {
            ArrayList<BooksCollection> booksCollectionArrayList = new ArrayList<>();

            Cursor c = getReadableDatabase().query(UserDataDBContract.BooksCollectionEntry.Table_NAME,
                    new String[]{
                            UserDataDBContract.BooksCollectionEntry.COLUMN_ID,
                            UserDataDBContract.BooksCollectionEntry.COLUMN_AUTOMATIC_ID,
                            UserDataDBContract.BooksCollectionEntry.COLUMN_ORDER,
                            UserDataDBContract.BooksCollectionEntry.COLUMN_VISIBILITY,
                            UserDataDBContract.BooksCollectionEntry.COLUMN_NAME
                    },
                    UserDataDBContract.BooksCollectionEntry.COLUMN_VISIBILITY + "=?",
                    new String[]{String.valueOf(viewdOnly ? 1 : 0)},
                    null,
                    null,
                    UserDataDBContract.BooksCollectionEntry.COLUMN_ORDER + "," + UserDataDBContract.BooksCollectionEntry.COLUMN_ID
            );

            final int INDEX_COLLECTION_ID = c.getColumnIndex(UserDataDBContract.BooksCollectionEntry.COLUMN_ID);
            final int INDEX_AUTOMATIC_ID = c.getColumnIndex(UserDataDBContract.BooksCollectionEntry.COLUMN_AUTOMATIC_ID);
            final int INDEX_ORDER = c.getColumnIndex(UserDataDBContract.BooksCollectionEntry.COLUMN_ORDER);
            final int INDEX_COLUMN_VISIBILITY = c.getColumnIndex(UserDataDBContract.BooksCollectionEntry.COLUMN_VISIBILITY);
            final int INDEX_NAME = c.getColumnIndex(UserDataDBContract.BooksCollectionEntry.COLUMN_NAME);

            while (c.moveToNext()) {
                int collectionId = c.getInt(INDEX_COLLECTION_ID);
                int automaticID = c.getInt(INDEX_AUTOMATIC_ID);
                String name = c.getString(INDEX_NAME);
                boolean visibility = c.getInt(INDEX_COLUMN_VISIBILITY) != 0;
                int order = c.getInt(INDEX_ORDER);
                booksCollectionArrayList.add(new BooksCollection(order, visibility, automaticID, name, collectionId));
            }
            c.close();
            return booksCollectionArrayList;
        }

        public Cursor getBooksCollectionCursor(BooksCollection booksCollection) {
            if (booksCollection.isAutomatic()) {
                return getAutomaticBooksCollectionCursor(booksCollection.getAutomaticId());
            } else {
                return BooksInformationDbHelper.getInstance(context).getBooksFilteredwithAttachDatabase(DATABASE_NAME,
                        context.getDatabasePath(DATABASE_NAME).toString(),
                        UserDataDBContract.BooksCollectionJoinEntry.Table_NAME,
                        UserDataDBContract.BooksCollectionJoinEntry.BOOK_ID,
                        new String[]{String.valueOf(booksCollection.getCollectionsId())},
                        DATABASE_NAME + SQL.DOT + UserDataDBContract.BooksCollectionJoinEntry.Table_NAME + SQL.DOT + UserDataDBContract.BooksCollectionJoinEntry.COLLECTION_ID + SQL.EQUALS + "?",
                        null);
            }

        }

        private Cursor getAutomaticBooksCollectionCursor(int automaticId) {
            switch (automaticId) {
                case 1://book_collection_most_recent
                    //select * from AccESSiNFORMATION where lastOpened is not null order by lastOpened desc
                    return BooksInformationDbHelper.getInstance(context).getBooksFilteredwithAttachDatabase(DATABASE_NAME,
                            context.getDatabasePath(DATABASE_NAME).toString(),
                            UserDataDBContract.AccessInformationEntry.Table_NAME,
                            UserDataDBContract.BooksCollectionJoinEntry.BOOK_ID,
                            null,
                            DATABASE_NAME + SQL.DOT + UserDataDBContract.AccessInformationEntry.Table_NAME + SQL.DOT +
                                    UserDataDBContract.AccessInformationEntry.LAST_OPENED_TIME_STAMP + SQL.IS_NOT_NULL,
                            DATABASE_NAME + SQL.DOT + UserDataDBContract.AccessInformationEntry.Table_NAME + SQL.DOT +
                                    UserDataDBContract.AccessInformationEntry.LAST_OPENED_TIME_STAMP + SQL.DECS);
                case 2://book_collection_most_opened
                    return BooksInformationDbHelper.getInstance(context).getBooksFilteredwithAttachDatabase(DATABASE_NAME,
                            context.getDatabasePath(DATABASE_NAME).toString(),
                            UserDataDBContract.AccessInformationEntry.Table_NAME,
                            UserDataDBContract.BooksCollectionJoinEntry.BOOK_ID,
                            null,
                            DATABASE_NAME + SQL.DOT + UserDataDBContract.AccessInformationEntry.Table_NAME + SQL.DOT +
                                    UserDataDBContract.AccessInformationEntry.COLUMN_ACCESS_COUNT + SQL.IS_NOT_NULL,
                            DATABASE_NAME + SQL.DOT + UserDataDBContract.AccessInformationEntry.Table_NAME + SQL.DOT +
                                    UserDataDBContract.AccessInformationEntry.COLUMN_ACCESS_COUNT + SQL.DECS);
                case 3://book_collection_recent_download
                    return BooksInformationDbHelper.getInstance(context).getRecentDownloads(0);

            }
            return null;
        }
    }


}
