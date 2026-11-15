package com.example.blades360;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;

final class EmulationSetup {
  static final int BIOS=3910,PLAY=3911;
  static void pickBios(MainActivity a){a.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true),BIOS);}
  static boolean result(MainActivity a,int request,int result,Intent data){
    if(request==PLAY){a.library.load();a.dashboard.invalidate();return true;}
    if(request!=BIOS)return false;if(result!=Activity.RESULT_OK||data==null)return true;
    ArrayList<Uri> files=new ArrayList<>();if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount();i++)files.add(data.getClipData().getItemAt(i).getUri());}else if(data.getData()!=null)files.add(data.getData());
    new Thread(()->{int imported=0;String error="";for(Uri uri:files){try{String name="";try(Cursor c=a.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())name=c.getString(0);}if(name.isEmpty()||name.contains("/")||name.contains("\\")||name.equals(".."))throw new IOException("Invalid filename");File dir=new File(a.getExternalFilesDir(null),"Emulation/system");if(name.toLowerCase(Locale.ROOT).startsWith("dc_"))dir=new File(dir,"dc");dir.mkdirs();File target=new File(dir,name);if(target.exists())throw new IOException(name+" already exists; existing BIOS kept");File part=new File(dir,name+".part");try(InputStream in=a.getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(part)){byte[] b=new byte[65536];long total=0;int n;while((n=in.read(b))!=-1){total+=n;if(total>64L*1024*1024)throw new IOException("BIOS exceeds 64 MB");out.write(b,0,n);}}catch(Exception e){part.delete();throw e;}if(!part.renameTo(target))throw new IOException("Could not save BIOS");imported++;}catch(Exception e){error=e.getMessage();}}
      String message="Imported "+imported+" BIOS file(s). "+error;a.runOnUiThread(()->a.tools.show("Emulation setup",message,a.tools::openHome,a.tools.action("Back",a.tools::openHome)));
    },"BiosImport").start();return true;
  }
}
