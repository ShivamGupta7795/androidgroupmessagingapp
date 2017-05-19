package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {
    static final String TAG = GroupMessengerProvider.class.getSimpleName();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        //Intent i = new Intent(GroupMessengerProvider.this, GroupMessengerActivity.class);
        String key = new String();
        String val = new String();

        key = values.get("key").toString();
        val = values.get("value").toString();


        if(key==null || val==null){
            Log.d(TAG, "key/val is null");
            return uri;
        }

        String filename= key;
        String content = val;
        FileOutputStream outputstream;
        Context context = getContext();
        try{
            Log.d(TAG, "key: "+filename.toString()+" Val: "+content.toString());
            outputstream = context.openFileOutput(filename, context.MODE_PRIVATE);
            outputstream.write(content.getBytes());
        }
        catch(Exception e){
            Log.d(TAG, "Exception encountered while inserting: "+e);
        }

        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        String[] columns = {"key", "value"};
        MatrixCursor cursor = new MatrixCursor(columns);
        Context context = getContext();
        FileInputStream f_stream;
        try {
            f_stream = context.openFileInput(selection);
            InputStreamReader i_reader = new InputStreamReader(f_stream);
            BufferedReader b_reader = new BufferedReader(i_reader);
            String[] message = new String[2];
            message[0] = selection;
            message[1] = b_reader.readLine().toString();
            //  Log.d(TAG,"message[0]aka key: "+message[0]+"message[1] aka value: "+message[1]);
            cursor.addRow(message);
            f_stream.close();
        }
        catch(Exception e){
            Log.d(TAG, "Exception encountered in query: "+e);
        }


        Log.v("query", selection);
        return cursor;
    }
}

