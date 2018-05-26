package com.example.christ.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by christ on 2018/5/17.
 */

public class MediaUtil {

    private static final String TAG = "MusicLoader";
    private static String[] projection = {
            Media._ID,
            Media.TITLE,
            Media.DATA,
            Media.ALBUM,
            Media.ARTIST,
            Media.DURATION,
            Media.SIZE,
            Media.ALBUM_ID,
            Media.ALBUM_KEY
    };
    public static ArrayList<MusicInfo> query(ContentResolver pContentResolver,
                                        String selection, String[] selectionArgs){
        ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
        Cursor cursor = pContentResolver.query(Media.EXTERNAL_CONTENT_URI, projection, selection,
                selectionArgs, Media.TITLE);
        if(cursor == null){
            Log.e(TAG, "Media cursor == null.");
        } else{
            while(cursor.moveToNext()) {
                musicList.add(new MusicInfo(cursor.getLong(cursor.getColumnIndex(Media._ID)),
                        cursor.getString(cursor.getColumnIndex(Media.TITLE)),
                        cursor.getString(cursor.getColumnIndex(Media.ALBUM)),
                        cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)),
                        cursor.getInt(cursor.getColumnIndex(Media.DURATION)),
                        cursor.getString(cursor.getColumnIndex(Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(Media.DATA))));
            }
            cursor.close();
        }
        return musicList;
    }

    public static Uri getMusicUriById(long id){
        Uri uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
        return uri;
    }

    // 获取专辑图片
    public static Bitmap getAlbumArt(Context mContext, int album_id){
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = getMediaStoreAlbumCoverUri(album_id);
        Log.e("uri", uri.toString());
        InputStream is;
        try {
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException ignored) {
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play_page_default_cover);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }
	private static Uri getMediaStoreAlbumCoverUri(long albumId) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

    // 刷新媒体库
    public static void updateMedia(final Context context, String[] music_uris) {
        MediaScannerConnection.scanFile(context, music_uris, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
//                        Log.e("scan", path + " - " + uri.toString());
                    }
                });
    }

}
