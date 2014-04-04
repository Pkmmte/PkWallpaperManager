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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class PkWallpaperManager extends Static
{
	//	TODO
	// 	- Cache wallpaper list in SharedPreferences in the onStop of MainActivity and WallpaperActivity
	//	- Search and restore wallpaper list stored locally
	//	- Add timer interval to fetch wallpapers only when needed
	//	- Create parameter to override timer interval
	
	// General Public Constants
	public static final String DEFAULT_METADATA_FILE_NAME = "wallpapers.json";
	public static final String RESOURCE_URI_BASE = "android.resource://";
	public static final int MAX_PROGRESS = 100;
	
	// JSON Constants
	private final String FULL_SRC = "full_src";
	private final String THUMB_SRC = "thumb_src";
	private final String TITLE = "title";
	private final String BYLINE = "byline";
	private final String FILE_SIZE = "file_size";
	
	// Keep a single instance throughout the app for simplicity
	private static PkWallpaperManager mInstance = null;
	
	// Lists of wallpapers
	private List<Wallpaper> mLocalWallpapers;
	private List<Wallpaper> mCloudWallpapers;
	
	// For issue tracking purposes
	private boolean debugEnabled;
	private static final String LOG_TAG = "PkWallpaperManager";
	
	// Custom request configuration data
	private WallpaperSettings mSettings;
	
	// Context is always useful for some reason.
	private Context mContext;
	
	// Background threads
	private AsyncTask<Void, Void, Void> localWallpapersTask;
	private AsyncTask<Void, Void, Void> cloudWallpapersTask;
	
	// Listeners for various loading events
	private List<LocalWallpaperListener> mLocalWallpaperListeners;
	private List<CloudWallpaperListener> mCloudWallpaperListeners;
	private List<WallpaperSetListener> mWallpaperSetListeners;
	private List<WallpaperDownloadListener> mWallpaperDownloadListeners;
	
	// Our handy client for getting API JSON data
	private HttpClient httpClient;
	
	/**
	 * Creates a global WallpaperManager instance.
	 * 
	 * @param context
	 */
	public static void createInstance(Context context)
	{
		if (mInstance == null)
			mInstance = new PkWallpaperManager(context.getApplicationContext());
	}
	
	/**
	 * Returns the global instance of this WallpaperManager.
	 * If you don't remember whether or not you already created 
	 * a previous instance, add the context as a parameter. 
	 * 
	 * @return
	 */
	public static PkWallpaperManager getInstance()
	{
		return mInstance;
	}
	
	/**
	 * Returns the global instance of this WallpaperManager.
	 * 
	 * @param context
	 * @return
	 */
	public static PkWallpaperManager getInstance(Context context)
	{
		if (mInstance == null)
			mInstance = new PkWallpaperManager(context.getApplicationContext());
		
		return mInstance;
	}
	
	/**
	 * Standard WallpaperManager constructor.
	 * 
	 * 
	 * @param context
	 */
	public PkWallpaperManager(Context context)
	{
		this.debugEnabled = false;
		this.mSettings = new WallpaperSettings();
		this.mContext = context;
		this.mLocalWallpapers = new ArrayList<Wallpaper>();
		this.mCloudWallpapers = new ArrayList<Wallpaper>();
		this.mLocalWallpaperListeners = new ArrayList<LocalWallpaperListener>();
		this.mCloudWallpaperListeners = new ArrayList<CloudWallpaperListener>();
		this.mWallpaperSetListeners = new ArrayList<WallpaperSetListener>();
		this.mWallpaperDownloadListeners = new ArrayList<WallpaperDownloadListener>();
		this.initHttpClient();
		this.initLocalWallpapersTask();
		this.initCloudWallpapersTask();
	}
	
	/**
	 * Loads wallpapers stored locally.<br>
	 * Make sure to have set your String Array of Local Wallpapers in 
	 * your settings before calling this method.
	 */
	public void fetchLocalWallpapers()
	{
		final String[] localWallpapers = mSettings.getLocalWallpapers();
		final String packageName = mSettings.getPackageName();
		final String thumbSuffix = mSettings.getThumbSuffix();
		
		if(debugEnabled)
			Log.d(LOG_TAG, "Loading local wallpapers...");
		
		// Basic resources
		Resources resources = mContext.getResources();
		mLocalWallpapers.clear();
		Wallpaper mWall = null;
		
		// Loop through all listeners notifying them
        for(LocalWallpaperListener mListener : mLocalWallpaperListeners) {
        	mListener.onLocalWallpapersLoading();
        }
		
		// Loop through extras looking for local wallpapers.
        for (String localWallpaper : localWallpapers) {
            int res = resources.getIdentifier(localWallpaper, "drawable", packageName);
            
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(localWallpaper + thumbSuffix, "drawable", packageName);
            	mWall = new Wallpaper();

            	mWall.setFullResource(res);
            	mWall.setFullUri(Uri.parse(RESOURCE_URI_BASE + packageName  + "/drawable/" + localWallpaper));
                mWall.setThumbResource(thumbRes);
            	mWall.setThumbUri(Uri.parse(RESOURCE_URI_BASE + packageName  + "/drawable/" + localWallpaper + thumbSuffix));
            	mWall.setLocal(true);
            	
            	mLocalWallpapers.add(mWall);
                mWall = null;
            }
        }
        
        if(debugEnabled)
        	Log.d(LOG_TAG, "Finished loading " + mLocalWallpapers.size() + " local wallpapers!");
        
        // Loop through all listeners notifying them
        for(LocalWallpaperListener mListener : mLocalWallpaperListeners) {
        	mListener.onLocalWallpapersLoaded();
        }
	}
	
	/**
	 * Loads wallpapers stored locally asynchronously in parallel.<br>
	 * Make sure to have set your String Array of Local Wallpapers in 
	 * your settings before calling this method.
	 * <p>
	 * This will not throw any exceptions but it does not guarantee success either.
	 * Use this as a lazy way of loading stuff in the background.
	 * 
	 */
	public void fetchLocalWallpapersAsync()
	{
		fetchLocalWallpapersAsync(true);
	}
	
	/**
	 * Loads wallpapers stored locally asynchronously.<br>
	 * Make sure to have set your String Array of Local Wallpapers in 
	 * your settings before calling this method.
	 * <p>
	 * This will not throw any exceptions but it does not guarantee success either.
	 * Use this as a lazy way of loading stuff in the background.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void fetchLocalWallpapersAsync(boolean parallel)
	{
		if(localWallpapersTask.getStatus() == AsyncTask.Status.PENDING) {
			// Execute task if it's ready to go!
			localWallpapersTask.executeOnExecutor(parallel ? AsyncTask.THREAD_POOL_EXECUTOR : AsyncTask.SERIAL_EXECUTOR);
		}
		else if(localWallpapersTask.getStatus() == AsyncTask.Status.RUNNING && debugEnabled) {
			// Don't execute if already running
			Log.d(LOG_TAG, "Task is already running...");
		}
		else if(localWallpapersTask.getStatus() == AsyncTask.Status.FINISHED) {
			// Okay, this is not supposed to happen. Reset and recall.
			if(debugEnabled)
				Log.d(LOG_TAG, "Uh oh, it appears the task has finished without being reset. Resetting task...");
			
			initLocalWallpapersTask();
			fetchLocalWallpapersAsync(parallel);
		}
	}
	
	/**
	 * Loads wallpapers stored on your cloud repository.
	 * May throw an exception so watch out and handle it carefully.
	 * 
	 * Note: Do NOT call this from the main UI thread of it will force close!
	 * 		 Call this from a separate thread instead.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public void fetchCloudWallpapers() throws ClientProtocolException, IOException, JSONException
	{
		// Cancel if not enabled
		if(!mSettings.getWebEnabled()) {
			if(debugEnabled)
				Log.d(LOG_TAG, "Cloud wallpapers aren't enabled in settings! Canceling task...");
			
			return;
		}
		
		// Retrieve Metadata URL or build default (if null)
		String metadataURL = mSettings.getMetadataURL();
		if(metadataURL == null) {
			metadataURL = mSettings.getStorageURL() + "/" + mSettings.getWallpaperPath() + "/" + DEFAULT_METADATA_FILE_NAME;
		}
		
		// Make a request to the Metadata URL and wait for the JSON response
		HttpGet get = new HttpGet(metadataURL);
		
        if(debugEnabled)
        	Log.d(LOG_TAG, "Sending wallpaper info data request to " + metadataURL + "...");
        
        String response = "";
        try {
        	response = httpClient.execute(get, new BasicResponseHandler());
        	
            if(debugEnabled)
            	Log.d(LOG_TAG, "Response: " + response);
        }
        catch (Exception e) {
        	if(debugEnabled) {
        		Log.d(LOG_TAG, "Unable to make a request to metadata URL! Are you sure you specified your metadata URL in settings correctly?");
        	}
        }
        
        // Loop through all listeners notifying them
        for(CloudWallpaperListener mListener : mCloudWallpaperListeners) {
        	mListener.onCloudWallpapersLoading();
        }
        
        // Convert response into JSONArray and loop through it
        JSONArray jsonResponse = new JSONArray(response);
        int responseLength = jsonResponse.length();
        mCloudWallpapers.clear();
        Wallpaper mWall = null;
        
        for(int index = 0; index < responseLength; index++) {
        	JSONObject jsonWallpaper = jsonResponse.getJSONObject(index);
        	
        	mWall = new Wallpaper();
        	mWall.setPathURL(mSettings.getStorageURL() + "/" + mSettings.getWallpaperPath() + "/");
        	mWall.setRelativeFullURL(jsonWallpaper.getString(FULL_SRC));
        	mWall.setRelativeThumbURL(jsonWallpaper.getString(THUMB_SRC));
        	mWall.setFullUri(Uri.parse(mWall.getFullURL()));
        	mWall.setThumbUri(Uri.parse(mWall.getThumbURL()));
        	mWall.setTitle(jsonWallpaper.getString(TITLE));
        	mWall.setByLine(jsonWallpaper.getString(BYLINE));
        	mWall.setFileSize(jsonWallpaper.getLong(FILE_SIZE));
        	mWall.setLocal(false);
        	mCloudWallpapers.add(mWall);
        	
        	if(debugEnabled)
        		Log.d(LOG_TAG, mWall.toString());
        	
        	mWall = null;
        }
        
        if(debugEnabled)
        	Log.d(LOG_TAG, "Finished loading " + mCloudWallpapers.size() + " cloud wallpapers!");
        
        // Loop through all listeners notifying them
        for(CloudWallpaperListener mListener : mCloudWallpaperListeners) {
        	mListener.onCloudWallpapersLoaded();
        }
	}
	
	/**
	 * Loads wallpapers stored on your cloud repository asynchronously in parallel. 
	 * It's safe to call this on the main UI thread.
	 * <p>
	 * This will not throw any exceptions but it does not guarantee success either.
	 * Use this as a lazy way of loading stuff in the background.
	 */
	public void fetchCloudWallpapersAsync()
	{
		fetchCloudWallpapersAsync(true);
	}
	
	/**
	 * Loads wallpapers stored on your cloud repository asynchronously. It's safe to 
	 * call this on the main UI thread.
	 * <p>
	 * This will not throw any exceptions but it does not guarantee success either.
	 * Use this as a lazy way of loading stuff in the background.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void fetchCloudWallpapersAsync(boolean parallel)
	{
		if(cloudWallpapersTask.getStatus() == AsyncTask.Status.PENDING) {
			// Execute task if it's ready to go!
			cloudWallpapersTask.executeOnExecutor(parallel ? AsyncTask.THREAD_POOL_EXECUTOR : AsyncTask.SERIAL_EXECUTOR);
		}
		else if(cloudWallpapersTask.getStatus() == AsyncTask.Status.RUNNING && debugEnabled) {
			// Don't execute if already running
			Log.d(LOG_TAG, "Task is already running...");
		}
		else if(cloudWallpapersTask.getStatus() == AsyncTask.Status.FINISHED) {
			// Okay, this is not supposed to happen. Reset and recall.
			if(debugEnabled)
				Log.d(LOG_TAG, "Uh oh, it appears the task has finished without being reset. Resetting task...");
			
			initCloudWallpapersTask();
			fetchCloudWallpapersAsync(parallel);
		}
	}
	
	/**
	 * Loads all wallpapers. If you have cloud wallpapers enabled, 
	 * this will load those too but might throw some exceptions.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public void fetchWallpapers() throws ClientProtocolException, IOException, JSONException
	{
		fetchLocalWallpapers();
		fetchCloudWallpapers();
	}
	
	/**
	 * Loads all wallpapers asynchronously in parallel. 
	 * If you have cloud wallpapers enabled, this will load those too.
	 * <p>
	 * No exceptions will be thrown but nothing is guaranteed.
	 */
	public void fetchWallpapersAsync()
	{
		fetchWallpapersAsync(true);
	}
	
	/**
	 * Loads all wallpapers asynchronously in parallel. 
	 * If you have cloud wallpapers enabled, this will load those too.
	 * <p>
	 * No exceptions will be thrown but nothing is guaranteed.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void fetchWallpapersAsync(boolean parallel)
	{
		fetchLocalWallpapersAsync(parallel);
		fetchCloudWallpapersAsync(parallel);
	}
	
	/**
	 * Returns an ArrayList of Wallpaper objects loaded locally
	 * and from the cloud combined. This list will never be null.
	 * 
	 * @return
	 */
	public List<Wallpaper> getWallpapers()
	{
		List<Wallpaper> mWallpapers = new ArrayList<Wallpaper>();
		mWallpapers.addAll(mLocalWallpapers);
		mWallpapers.addAll(mCloudWallpapers);
		
		return mWallpapers;
	}
	
	/**
	 * Returns an ArrayList of Wallpaper objects loaded locally.
	 * 
	 * @return
	 */
	public List<Wallpaper> getLocalWallpapers()
	{
		return this.mCloudWallpapers;
	}
	
	/**
	 * Returns an ArrayList of Wallpaper objects loaded from the cloud.
	 * 
	 * @return
	 */
	public List<Wallpaper> getCloudWallpapers()
	{
		return this.mCloudWallpapers;
	}
	
	/**
	 * Returns the amount of wallpapers currently loaded
	 * on the manager. This applies to both, local and cloud.
	 * 
	 * If the either wallpaper list is null for some odd 
	 * reason, this returns -1.
	 * 
	 * @return
	 */
	public int getNumWallpapers()
	{
		if(mLocalWallpapers == null || mCloudWallpapers == null)
			return -1;
		else
			return (mLocalWallpapers.size() + mCloudWallpapers.size());
	}
	
	/**
	 * Adds an LocalWallpaperListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addLocalWallpaperListener(LocalWallpaperListener listener)
	{
		mLocalWallpaperListeners.add(listener);
	}
	
	/**
	 * Removes an LocalWallpaperListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeLocalWallpaperListener(LocalWallpaperListener listener)
	{
		mLocalWallpaperListeners.remove(listener);
	}
	
	/**
	 * Removes all LocalWallpaperListeners from this global instance.
	 */
	public void removeAllLocalWallpaperListeners()
	{
		mLocalWallpaperListeners.clear();
	}
	
	/**
	 * Adds an CloudWallpaperListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addCloudWallpaperListener(CloudWallpaperListener listener)
	{
		mCloudWallpaperListeners.add(listener);
	}
	
	/**
	 * Removes an CloudWallpaperListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeCloudWallpaperListener(CloudWallpaperListener listener)
	{
		mCloudWallpaperListeners.remove(listener);
	}
	
	/**
	 * Removes all CloudWallpaperListeners from this global instance.
	 */
	public void removeAllCloudWallpaperListeners()
	{
		mCloudWallpaperListeners.clear();
	}
	
	/**
	 * Adds an WallpaperSetListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addWallpaperSetListener(WallpaperSetListener listener)
	{
		mWallpaperSetListeners.add(listener);
	}
	
	/**
	 * Removes an WallpaperSetListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeWallpaperSetListener(WallpaperSetListener listener)
	{
		mWallpaperSetListeners.remove(listener);
	}
	
	/**
	 * Removes all WallpaperSetListeners from this global instance.
	 */
	public void removeAllWallpaperSetListeners()
	{
		mWallpaperSetListeners.clear();
	}
	
	/**
	 * Adds an WallpaperDownloadListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addWallpaperDownloadListener(WallpaperDownloadListener listener)
	{
		mWallpaperDownloadListeners.add(listener);
	}
	
	/**
	 * Removes an WallpaperDownloadListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeWallpaperDownloadListener(WallpaperDownloadListener listener)
	{
		mWallpaperDownloadListeners.remove(listener);
	}
	
	/**
	 * Removes all WallpaperDownloadListeners from this global instance.
	 */
	public void removeAllWallpaperDownloadListeners()
	{
		mWallpaperDownloadListeners.clear();
	}
	
	/**
	 * Removes all listeners from this global instance.
	 */
	public void removeAllListeners()
	{
		mLocalWallpaperListeners.clear();
		mCloudWallpaperListeners.clear();
		mWallpaperSetListeners.clear();
		mWallpaperDownloadListeners.clear();
	}
	
	/**
	 * Downloads the wallpaper. May throw an exception.
	 * <p>
	 * You can see download progress for the wallpaper passed 
	 * through the interface.
	 * <p>
	 * Note: Do NOT call this from the main UI thread! Call 
	 * downloadWallpaperAsync instead.
	 * 
	 * @param mWall
	 * @throws IOException
	 */
	public void downloadWallpaper(Wallpaper mWall, NotificationManager notification, Builder builder) throws IOException
	{
		// Returns and does nothing if the wallpaper is null or local
		if(mWall == null || mWall.isLocal()) {
			if(debugEnabled)
				Log.d(LOG_TAG, "Unable to download image. " + mWall == null ? "Wallpaper object is null." : "Wallpaper object is local.");
			
			return;
		}
		
		// Loop through all listeners notifying them
        for(WallpaperDownloadListener mListener : mWallpaperDownloadListeners) {
        	mListener.onWallpaperDownloading(mWall, 0);
        }
		
        // Connect to the server
        URL url = new URL(mSettings.getStorageURL() + "/" + mSettings.getWallpaperPath() + "/" + mWall.getRelativeFullURL());
        URLConnection connection = url.openConnection();
        connection.connect();
        InputStream input = new BufferedInputStream(url.openStream(), 8192);
        
        // Output stream
        String fileName = mWall.getTitle().length() > 0 ? mWall.getTitle() + mWall.getRelativeFullURL().substring(mWall.getRelativeFullURL().lastIndexOf(".")) : mWall.getRelativeFullURL();
        File file = new File(mSettings.getSaveLocation() + "/" + fileName);
        file.getParentFile().mkdirs();
        OutputStream output = new FileOutputStream(file);

        byte data[] = new byte[mSettings.getByteBuffer()];
		int count, progress, total = 0;
        while ((count = input.read(data)) != -1) {
            total += count;
            progress = (int) ((total * 100) / mWall.getFileSize());
            Log.d(LOG_TAG, "Download Progress: " + progress);
            
            // Loop through all listeners notifying them
            for(WallpaperDownloadListener mListener : mWallpaperDownloadListeners) {
            	mListener.onWallpaperDownloading(mWall, progress);
            }

            // Writing data to file
            output.write(data, 0, count);
        }

        // Flushing output
        output.flush();

        // Closing streams
        output.close();
        input.close();
        
        // Scan media for newly downloaded image
        new SingleMediaScanner(mContext, file).scanMedia();
        
        // Loop through all listeners notifying them
        for(WallpaperDownloadListener mListener : mWallpaperDownloadListeners) {
        	mListener.onWallpaperDownloaded(mWall);
        }
        
        if(debugEnabled)
        	Log.d(LOG_TAG, "Finished downloading wallpaper!");
	}
	
	/**
	 * Downloads the wallpaper. Will not thrown an exception. 
	 * Check the interface for failed status.
	 * <p>
	 * You can see download progress for the wallpaper passed 
	 * through the interface.
	 * 
	 * @param mWall
	 */
	public void downloadWallpaperAsync(final Wallpaper mWall, final NotificationManager notification, final Builder builder)
	{
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					downloadWallpaper(mWall, notification, builder);
				} catch (Exception e) {
					// Loop through all listeners notifying them
			        for(WallpaperDownloadListener mListener : mWallpaperDownloadListeners) {
			        	mListener.onWallpaperDownloadFailed(mWall);
			        }
			        
					e.printStackTrace();
				}
				
				return null;
			}
		}.execute();
	}
	
	/**
	 * Sets the system wallpaper to the Wallpaper object passed.
	 * May throw an exception if the data is invalid.
	 * <p>
	 * Note: Do NOT call this from the main UI thread if you're 
	 * setting it to a cloud wallpaper! Call setWallpaperAsync instead.
	 * 
	 * @param mWall
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void setWallpaper(Wallpaper mWall) throws NumberFormatException, IOException
	{
		if(debugEnabled)
			Log.d(LOG_TAG, "Setting wallpaper...");
		
		// Load the original system WallpaperManager
		WallpaperManager wallManager = WallpaperManager.getInstance(mContext);
		Bitmap bitmap = null;
		
		// Set resource if local, otherwise download it and set it
		if(mWall.isLocal())
			wallManager.setResource(mWall.getFullResource());
		else {
			bitmap = getBitmapFromURL(mWall.getFullURL());
			wallManager.setBitmap(bitmap);
		}
		
		// Loop through all listeners notifying them
        for(WallpaperSetListener mListener : mWallpaperSetListeners) {
        	mListener.onWallpaperSet(bitmap);
        }
        
		if(debugEnabled)
			Log.d(LOG_TAG, "Successfully set wallpaper!");
	}
	
	/**
	 * Sets the system wallpaper to the Wallpaper object passed. 
	 * <p>
	 * This does not guarantee success. For further detail, set a 
	 * listener
	 * 
	 * @param mWall
	 */
	public void setWallpaperAsync(final Wallpaper mWall)
	{
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					setWallpaper(mWall);
				} catch (Exception e) {
					// Loop through all listeners notifying them
			        for(WallpaperSetListener mListener : mWallpaperSetListeners) {
			        	mListener.onWallpaperSetFailed();
			        }
			        
					e.printStackTrace();
				}
				
				return null;
			}
		}.execute();
	}
	
	/**
	 * Returns a WallpaperSettings object with all values set.
	 * 
	 * @return
	 */
	public WallpaperSettings getSettings()
	{
		return this.mSettings;
	}
	
	/**
	 * Applies new settings using your own custom WallpaperSettings object.
	 * 
	 * @param settings
	 */
	public void setSettings(WallpaperSettings settings)
	{
		this.mSettings = settings;
	}
	
	/**
	 * Set the debug status for this manager.
	 * If true, it will periodically print logs of current 
	 * progress so you can see what's going on.
	 * <p>
	 * I suggest you disable this during production as it
	 * will consume unnecessary processing power. Besides,
	 * you don't want to spam your users' logs.
	 * 
	 * @param debug
	 */
	public void setDebugging(boolean debug)
	{
		this.debugEnabled = debug;
	}
	
	/**
	 * Initializes our handy little client for cloud calls.
	 * Don't modify this unless you know what you're doing.
	 */
	private void initHttpClient()
	{
		// Basic HTTP parameters & manager set up
		HttpParams parameters = new BasicHttpParams();
		HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(parameters, false);
		HttpConnectionParams.setTcpNoDelay(parameters, true);
		HttpConnectionParams.setSocketBufferSize(parameters, 8192);
		
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ClientConnectionManager tsccm = new ThreadSafeClientConnManager(parameters, schReg);
		
		// Finally, initialize our client with these parameters and manager
		this.httpClient = new DefaultHttpClient(tsccm, parameters);
	}
	
	/**
	 * Initializes our local wallpapers thread.
	 */
	private void initLocalWallpapersTask()
	{
		this.localWallpapersTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					fetchLocalWallpapers();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void p)
			{
				initLocalWallpapersTask();
			}
		};
	}
	
	/**
	 * Initializes our cloud wallpapers thread.
	 */
	private void initCloudWallpapersTask()
	{
		this.cloudWallpapersTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					fetchCloudWallpapers();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void p)
			{
				initCloudWallpapersTask();
			}
		};
	}
}