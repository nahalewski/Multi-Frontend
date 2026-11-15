package com.example.blades360;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;

/** Temporary read grants to app-created captures, never arbitrary device files. */
public final class MediaShareProvider extends ContentProvider {
  public boolean onCreate(){return true;}
  private File resolve(Uri uri)throws FileNotFoundException {
    List<String> parts=uri.getPathSegments();
    if(parts.size()!=2||!(parts.get(0).equals("pictures")||parts.get(0).equals("movies")))throw new FileNotFoundException();
    boolean movie=parts.get(0).equals("movies");
    File root=getContext().getExternalFilesDir(movie?Environment.DIRECTORY_MOVIES:Environment.DIRECTORY_PICTURES);
    if(root==null)root=new File(getContext().getFilesDir(),movie?"Movies":"Pictures");
    File dir=new File(root,"BladesCaptures");
    try{File target=new File(dir,parts.get(1));if(!target.getCanonicalFile().getParentFile().equals(dir.getCanonicalFile())||!target.isFile()||!target.getName().matches(movie?"Blades-.*\\.mp4":"Blades-.*\\.png"))throw new FileNotFoundException();return target;}catch(IOException e){throw new FileNotFoundException();}
  }
  public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException {if(!mode.equals("r"))throw new FileNotFoundException("Read-only");return ParcelFileDescriptor.open(resolve(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
  public String getType(Uri uri){return uri.getPath().startsWith("/movies/")?"video/mp4":"image/png";}
  public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder){try{File f=resolve(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor c=new MatrixCursor(columns);Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)row[i]=columns[i].equals(OpenableColumns.DISPLAY_NAME)?f.getName():columns[i].equals(OpenableColumns.SIZE)?f.length():null;c.addRow(row);return c;}catch(FileNotFoundException e){return null;}}
  public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
  public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
  public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
