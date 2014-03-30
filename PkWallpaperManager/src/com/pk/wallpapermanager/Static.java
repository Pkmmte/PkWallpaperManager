/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Pkmmte Xeleon
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.pk.wallpapermanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;

/** Static Functions - Do not require instance */
class Static
{
	/**
	 * Sets the system wallpaper to the Wallpaper object passed.
	 * May throw an exception if the data is invalid.
	 * <p>
	 * Note: Do NOT call this from the main UI thread if you're 
	 * setting it to a cloud wallpaper! Call setWallpaperAsync instead.
	 * 
	 * @param context
	 * @param mWall
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void setWallpaper(Context context, Wallpaper mWall) throws NumberFormatException, IOException
	{
		android.app.WallpaperManager wallManager = android.app.WallpaperManager.getInstance(context);
		
		if(mWall.isLocal())
			wallManager.setResource(mWall.getFullResource());
		else {
			Bitmap bitmap = getBitmapFromURL(mWall.getFullURL());
			wallManager.setBitmap(bitmap);
		}
	}
	
	/**
	 * Sets the system wallpaper to the Wallpaper object passed.
	 * May throw an exception if the data is invalid.
	 * <p>
	 * This does not guarantee success. For further detail, use 
	 * the instance method along with the interface.
	 * 
	 * @param context
	 * @param mWall
	 */
	public static void setWallpaperAsync(final Context context, final Wallpaper mWall)
	{
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					setWallpaper(context, mWall);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		}.execute();
	}
	
	public static void downloadWallpaper(Context context, Wallpaper mWall) throws IOException
	{
		downloadWallpaper(context, mWall, null);
	}
	
	/**
	 * Downloads the wallpaper.
	 * Requires context and may throw an exception.
	 * <p>
	 * Note: Do NOT run this on the main UI thread or it may crash.
	 * Use the Async variant instead.
	 * 
	 * @param context
	 * @param mWall
	 * @throws IOException
	 */
	public static void downloadWallpaper(Context context, Wallpaper mWall, String saveLocation) throws IOException
	{
		// Returns and does nothing if the wallpaper is null or local
		if(mWall == null || mWall.isLocal()) 
			return;
		
        // Connect to the server
        URL url = new URL(mWall.getFullURL());
        URLConnection conection = url.openConnection();
        conection.connect();
        InputStream input = new BufferedInputStream(url.openStream(), 8192);

        // Output stream
        String fileName = mWall.getTitle().length() > 0 ? mWall.getTitle() + mWall.getRelativeFullURL().substring(mWall.getRelativeFullURL().lastIndexOf(".")) : mWall.getRelativeFullURL();
        String saveLoc = saveLocation == null ? Environment.getExternalStorageDirectory().getAbsolutePath() + "/.theme_wallpapers" : saveLocation;
        File file = new File(saveLoc + "/" + fileName);
        file.getParentFile().mkdirs();
        OutputStream output = new FileOutputStream(file);
        
        byte data[] = new byte[2048];
		int count;
        while ((count = input.read(data)) != -1) {
            // Writing data to file
            output.write(data, 0, count);
        }

        // Flushing output
        output.flush();

        // Closing streams
        output.close();
        input.close();
        
        // Scan for newly downloaded media
        MediaScannerConnection.scanFile(context, new String[] {file.getAbsolutePath()}, null, null);
	}
	
	/**
	 * Downloads the wallpaper asynchronously. Will not throw exceptions.
	 * <p>
	 * Note: Use the instance version of this method for progress details.
	 * 
	 * @param context
	 * @param mWall
	 */
	public static void downloadWallpaperAsync(final Context context, final Wallpaper mWall)
	{
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					downloadWallpaper(context, mWall);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		}.execute();
	}
	
	protected static Bitmap getBitmapFromURL(String source)
	{
		try {
	        URL url = new URL(source);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        Bitmap myBitmap = BitmapFactory.decodeStream(input);
	        return myBitmap;
	    }
		catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}