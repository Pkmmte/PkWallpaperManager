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

import android.net.Uri;

public class Wallpaper {
	// Source for thumbnail and full wallpaper
	private String RelativeFullURL;
	private String RelativeThumbURL;
	private Uri fullUri;
	private Uri thumbUri;
	private int fullResource;
	private int thumbResource;
	
	//
	private String pathURL;
	
	// Extra (Optional) Properties
	private String Title;
	private String byLine;
	private long fileSize;
	
	// Local or Cloud
	private boolean Local;
	
	/** Default Constructor **/
	public Wallpaper() {
		this.RelativeFullURL = "";
		this.RelativeThumbURL = "";
		this.fullUri = Uri.parse("");
		this.thumbUri = Uri.parse("");
		this.fullResource = 0;
		this.thumbResource = 0;
		
		this.pathURL = "";
		
		this.Title = "";
		this.byLine = "";
		this.fileSize = 0;
		
		this.Local = false;
	}
	
	/** Local Constructor **/
	public Wallpaper(Uri fullUri, Uri thumbUri, int fullResource, int thumbResource, String Title, String byLine) {
		this.RelativeFullURL = "";
		this.RelativeThumbURL = "";
		this.fullUri = fullUri;
		this.thumbUri = thumbUri;
		this.fullResource = fullResource;
		this.thumbResource = thumbResource;
		
		this.Title = Title;
		this.byLine = byLine;
		this.fileSize = 0;
		
		this.Local = true;
	}
	
	/** Cloud Constructor **/
	public Wallpaper(String RelativeFullURL, String RelativeThumbURL, Uri fullUri, Uri thumbUri, String Title, String byLine, long fileSize) {
		this.RelativeFullURL = RelativeFullURL;
		this.RelativeThumbURL = RelativeThumbURL;
		this.fullUri = fullUri;
		this.thumbUri = thumbUri;
		this.fullResource = 0;
		this.thumbResource = 0;
		
		this.Title = Title;
		this.byLine = byLine;
		this.fileSize = fileSize;
		
		this.Local = false;
	}
	
	/** Full Constructor **/
	public Wallpaper(String RelativeFullURL, String RelativeThumbURL, Uri fullUri, Uri thumbUri, int fullResource, int thumbResource, String Title, String byLine, long fileSize, boolean Local) {
		this.RelativeFullURL = RelativeFullURL;
		this.RelativeThumbURL = RelativeThumbURL;
		this.fullUri = fullUri;
		this.thumbUri = thumbUri;
		this.fullResource = fullResource;
		this.thumbResource = thumbResource;
		
		this.Title = Title;
		this.byLine = byLine;
		this.fileSize = fileSize;
		
		this.Local = Local;
	}
	
	/** SETTERS **/
	public void setPathURL(String pathURL) {
		this.pathURL = pathURL;
	}
	
	public void setRelativeFullURL(String RelativeFullURL) {
		this.RelativeFullURL = RelativeFullURL;
	}
	
	public void setRelativeThumbURL(String RelativeThumbURL) {
		this.RelativeThumbURL = RelativeThumbURL;
	}
	
	public void setFullUri(Uri fullUri) {
		this.fullUri = fullUri;
	}
	
	public void setThumbUri(Uri thumbUri) {
		this.thumbUri = thumbUri;
	}
	
	public void setFullResource(int fullResource) {
		this.fullResource = fullResource;
	}
	
	public void setThumbResource(int thumbResource) {
		this.thumbResource = thumbResource;
	}
	
	public void setTitle(String Title) {
		this.Title = Title;
	}
	
	public void setByLine(String byLine) {
		this.byLine = byLine;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public void setLocal(boolean Local) {
		this.Local = Local;
	}
	
	/** GETTERS **/
	public String getFullURL() {
		return (this.pathURL + this.RelativeFullURL);
	}
	
	public String getThumbURL() {
		return (this.pathURL + this.RelativeThumbURL);
	}

	public String getRelativeFullURL() {
		return this.RelativeFullURL;
	}
	
	public String getRelativeThumbURL() {
		return this.RelativeThumbURL;
	}
	
	public Uri getFullUri() {
		return this.fullUri;
	}
	
	public Uri getThumbUri() {
		return this.thumbUri;
	}
	
	public int getFullResource() {
		return this.fullResource;
	}
	
	public int getThumbResource() {
		return this.thumbResource;
	}
	
	public String getTitle() {
		return this.Title;
	}
	
	public String getByLine() {
		return this.byLine;
	}
	
	public long getFileSize() {
		return this.fileSize;
	}
	
	public boolean isLocal() {
		return this.Local;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Relative Full URL: " + this.RelativeFullURL + "\n");
		builder.append("Relative Thumb URL: " + this.RelativeThumbURL + "\n");
		builder.append("Full Uri: " + this.fullUri.toString() + "\n");
		builder.append("Thumb Uri: " + this.thumbUri.toString() + "\n");
		builder.append("Full Resource: " + this.fullResource + "\n");
		builder.append("Thumb Resource: " + this.thumbResource + "\n");

		builder.append("Title: " + this.Title + "\n");
		builder.append("By Line: " + this.byLine + "\n");
		builder.append("File Size: " + this.fileSize + "\n");
		
		builder.append("Local: " + this.Local + "\n");
		
		return builder.toString();
	}
}