package com.example.blades360;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

/** Strict, bounded archive format. No archive path is ever used as a disk destination. */
final class BackupArchive {
  static final int MAX_ENTRY=8*1024*1024,MAX_FILES=5001;
  static final long MAX_TOTAL=256L*1024*1024;
  static String hash(byte[] bytes)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder out=new StringBuilder();for(byte b:d)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
  static byte[] bounded(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[16384];int n,total=0;while((n=in.read(b))!=-1){total+=n;if(total>max)throw new IOException("Backup entry exceeds size limit");out.write(b,0,n);}return out.toByteArray();}
  static void read(InputStream input,EntryConsumer consumer)throws Exception{Set<String> names=new HashSet<>();long total=0;try(ZipInputStream zip=new ZipInputStream(input)){ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName();if(entry.isDirectory()||!(name.equals("manifest.json")||name.matches("artwork/[0-9a-f]{64}\\.img")))throw new IOException("Unexpected backup entry");if(!names.add(name)||names.size()>MAX_FILES)throw new IOException("Duplicate or excessive backup entries");byte[] bytes=bounded(zip,MAX_ENTRY);total+=bytes.length;if(total>MAX_TOTAL)throw new IOException("Backup exceeds total size limit");if(name.startsWith("artwork/")&&!name.equals("artwork/"+hash(bytes)+".img"))throw new IOException("Artwork checksum mismatch");consumer.accept(name,bytes);zip.closeEntry();}}if(!names.contains("manifest.json"))throw new IOException("Backup manifest missing");}
  interface EntryConsumer{void accept(String name,byte[] data)throws Exception;}
}
