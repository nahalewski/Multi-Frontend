package com.example.blades360;
import java.io.*;
import java.net.*;
import java.util.*;
final class MediaHttp {
 static byte[] get(String address,Map<String,String> headers,int limit)throws Exception{
  URL original=new URL(address),url=original;
  if(!Arrays.asList("http","https").contains(url.getProtocol())||url.getUserInfo()!=null)throw new IOException("Unsupported URL");
  for(int redirects=0;redirects<5;redirects++){HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept-Encoding","identity");if(RommClient.sameOrigin(original,url))for(Map.Entry<String,String> h:headers.entrySet())c.setRequestProperty(h.getKey(),h.getValue());
   try{int code=c.getResponseCode();if(code>=300&&code<400){URL next=new URL(url,c.getHeaderField("Location"));if(!Arrays.asList("http","https").contains(next.getProtocol())||next.getUserInfo()!=null||url.getProtocol().equals("https")&&!next.getProtocol().equals("https"))throw new IOException("Unsafe redirect");url=next;continue;}if(code!=200)throw new IOException("Server returned HTTP "+code);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Response too large");out.write(b,0,n);}return out.toByteArray();}}finally{c.disconnect();}
  }throw new IOException("Too many redirects");
 }
}
