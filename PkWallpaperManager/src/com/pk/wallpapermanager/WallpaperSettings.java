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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Environment;

public class WallpaperSettings
{
	private String[] localWallpapers;
	private String appName;
	private String metadataURL;
	private String packageID;
	private String packageName;
	private String saveLocation;
	private String serverURL;
	private String storageURL;
	private String thumbSuffix;
	private String wallpaperPath;
	private boolean webEnabled;
	private int byteBuffer;
	
	public WallpaperSettings() {
		this.localWallpapers = new String[0];
		this.appName = "My Theme";
		this.metadataURL = null;
		this.packageID = "your_id";
		this.packageName = "com.example.name";
		this.saveLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.theme_wallpapers";
		this.serverURL = "http://www.the1template.com";
		this.storageURL = "http://storage.the1template.com";
		this.thumbSuffix = "_small";
		this.wallpaperPath = "/wallpapers/" + packageID;
		this.webEnabled = true;
		this.byteBuffer = 2048;
	}
	
	public WallpaperSettings(Builder builder) {
		this.localWallpapers = builder.localWallpapers.toArray(new String[builder.localWallpapers.size()]);
		this.appName = builder.appName;
		this.metadataURL = builder.metadataURL;
		this.packageID = builder.packageID;
		this.packageName = builder.packageName;
		this.saveLocation = builder.saveLocation;
		this.serverURL = builder.serverURL;
		this.storageURL = builder.storageURL;
		this.thumbSuffix = builder.thumbSuffix;
		this.wallpaperPath = builder.wallpaperPath;
		this.webEnabled = builder.webEnabled;
		this.byteBuffer = builder.byteBuffer;
	}
	
	public void setLocalWallpapers(String[] localWallpapers) {
		this.localWallpapers = localWallpapers;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public void setMetadataURL(String metadataURL) {
		this.metadataURL = metadataURL;
	}
	
	public void setPackageID(String packageID) {
		this.packageID = packageID;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setSaveLocation(String saveLocation) {
		this.saveLocation = saveLocation;
	}
	
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	
	public void setStorageURL(String storageURL) {
		this.storageURL = storageURL;
	}
	
	public void setThumbSuffix(String thumbSuffix) {
		this.thumbSuffix = thumbSuffix;
	}
	
	public void setWallpaperPath(String wallpaperPath) {
		this.wallpaperPath = wallpaperPath;
	}
	
	public void setWebEnabled(boolean webEnabled) {
		this.webEnabled = webEnabled;
	}
	
	public void setByteBuffer(int byteBuffer) {
		this.byteBuffer = byteBuffer;
	}
	
	public String[] getLocalWallpapers() {
		return this.localWallpapers;
	}
	
	public String getAppName() {
		return this.appName;
	}
	
	public String getMetadataURL() {
		return this.metadataURL;
	}
	
	public String getPackageID() {
		return this.packageID;
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	
	public String getSaveLocation() {
		return this.saveLocation;
	}
	
	public String getServerURL() {
		return this.serverURL;
	}
	
	public String getStorageURL() {
		return this.storageURL;
	}
	
	public String getThumbSuffix() {
		return this.thumbSuffix;
	}
	
	public String getWallpaperPath() {
		return this.wallpaperPath;
	}
	
	public boolean getWebEnabled() {
		return this.webEnabled;
	}
	
	public int getByteBuffer() {
		return this.byteBuffer;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Local Wallpapers: " + this.localWallpapers.toString() + "\n");
		builder.append("App Name: " + this.appName + "\n");
		builder.append("Metadata URL: " + this.metadataURL + "\n");
		builder.append("Package ID: " + this.packageID + "\n");
		builder.append("Package Name: " + this.packageName + "\n");
		builder.append("Save Location: " + this.saveLocation + "\n");
		builder.append("Server URL: " + this.serverURL + "\n");
		builder.append("Storage URL: " + this.storageURL + "\n");
		builder.append("Thumb Suffix: " + this.thumbSuffix + "\n");
		builder.append("Wallpaper Path: " + this.wallpaperPath + "\n");
		builder.append("Web Enabled: " + this.webEnabled + "\n");
		builder.append("Byte Buffer: " + this.byteBuffer + "\n");
		
		return builder.toString();
	}
	
	public static class Builder
	{
		private List<String> localWallpapers;
		private String appName;
		private String metadataURL;
		private String packageID;
		private String packageName;
		private String saveLocation;
		private String serverURL;
		private String storageURL;
		private String thumbSuffix;
		private String wallpaperPath;
		private boolean webEnabled;
		private int byteBuffer;
		
		public Builder() {
			this.localWallpapers = new ArrayList<String>();
			this.appName = "My Theme";
			this.metadataURL = null;
			this.packageID = "your_id";
			this.packageName = "com.example.name";
			this.saveLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.theme_wallpapers";
			this.serverURL = "http://www.the1template.com";
			this.storageURL = "http://storage.the1template.com";
			this.thumbSuffix = "_small";
			this.wallpaperPath = "wallpapers/" + packageID;
			this.webEnabled = true;
			this.byteBuffer = 2048;
		}
		
		public Builder addLocalWallpaper(String localWallpaper) {
			this.localWallpapers.add(localWallpaper);
			return this;
		}
		
		public Builder addLocalWallpapers(String[] localWallpapers) {
			this.localWallpapers.addAll(Arrays.asList(localWallpapers));
			return this;
		}
		
		public Builder appName(String appName) {
			this.appName = appName;
			return this;
		}
		
		public Builder metadataURL(String metadataURL) {
			this.metadataURL = metadataURL;
			return this;
		}
		
		public Builder packageID(String packageID) {
			this.packageID = packageID;
			return this;
		}
		
		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return this;
		}
		
		public Builder saveLocation(String saveLocation) {
			this.saveLocation = saveLocation;
			return this;
		}
		
		public Builder serverURL(String serverURL) {
			this.serverURL = serverURL;
			return this;
		}
		
		public Builder storageURL(String storageURL) {
			this.storageURL = storageURL;
			return this;
		}
		
		public Builder thumbSuffix(String thumbSuffix) {
			this.thumbSuffix = thumbSuffix;
			return this;
		}
		
		public Builder wallpaperPath(String wallpaperPath) {
			this.wallpaperPath = wallpaperPath;
			return this;
		}
		
		public Builder webEnabled(boolean webEnabled) {
			this.webEnabled = webEnabled;
			return this;
		}
		
		public Builder byteBuffer(int byteBuffer) {
			this.byteBuffer = byteBuffer;
			return this;
		}
		
		public WallpaperSettings build()  {
			return new WallpaperSettings(this);
		}
	}
}