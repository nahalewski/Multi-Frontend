package com.example.blades360;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

/** Local HTTP fixture verifies real pagination and atomic retention, without touching the user's connection. */
public final class RommNetworkInstrumentation extends Instrumentation {
  int checks;void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
  @Override public void onCreate(Bundle args){super.onCreate(args);start();}
  @Override public void onStart(){Bundle result=new Bundle();MainActivity a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));RommClient client=new RommClient(a,"romm_network_test");
    try{org.json.JSONObject rich=new org.json.JSONObject();rich.put("id",1);rich.put("name","Large catalog fixture");rich.put("summary",new String(new char[8000]).replace('\0','x'));rich.put("igdb_metadata",new String(new char[20000]).replace('\0','y'));org.json.JSONObject compact=RommClient.compact(rich);check(!compact.has("igdb_metadata")&&compact.getString("summary").length()==4001,"Large provider payloads discarded and descriptions bounded");org.json.JSONArray catalog=new org.json.JSONArray();for(int i=0;i<5270;i++)catalog.put(RommClient.compact(rich));check(catalog.toString().length()<23000000,"Full-size catalog has bounded retained payload");}catch(Exception e){throw new RuntimeException(e);}
    try(ServerSocket server=new ServerSocket(0,8,InetAddress.getByName("127.0.0.1"))){server.setSoTimeout(15000);final Throwable[] failure={null};Thread responder=new Thread(()->{try{for(int i=0;i<3;i++){try(Socket socket=server.accept()){socket.setSoTimeout(10000);BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));String request=reader.readLine();boolean authenticated=false;for(String line;(line=reader.readLine())!=null&&!line.isEmpty();)if(line.equalsIgnoreCase("Authorization: Bearer fixture-token"))authenticated=true;if(!authenticated)throw new AssertionError("Authorization header missing");if(i==1&&!request.contains("offset=1"))throw new AssertionError("Pagination offset missing");String body=i==2?"{}":"{\"items\":[{\"id\":"+(i+1)+",\"name\":\"Fixture "+i+"\"}],\"total\":2}";byte[] bytes=body.getBytes("UTF-8");OutputStream out=socket.getOutputStream();out.write(("HTTP/1.1 "+(i==2?"503 Service Unavailable":"200 OK")+"\r\nContent-Type: application/json\r\nContent-Length: "+bytes.length+"\r\nConnection: close\r\n\r\n").getBytes("UTF-8"));out.write(bytes);out.flush();}}}catch(Throwable t){failure[0]=t;}});responder.start();
      CountDownLatch first=new CountDownLatch(1);runOnMainSync(()->{if(a.bootAnimation!=null)a.bootAnimation.finish();client.prefs.edit().clear().commit();client.server("http://127.0.0.1:"+server.getLocalPort());client.credential("fixture-token");client.changed=()->{if(!client.busy)first.countDown();};client.refresh();});check(first.await(15,TimeUnit.SECONDS),"Catalog request completed");check(client.games.length()==2,"All pages loaded");check(client.prefs.getString("catalog","").contains("Fixture 1"),"Complete catalog cached");
      CountDownLatch second=new CountDownLatch(1);runOnMainSync(()->{client.changed=()->{if(!client.busy)second.countDown();};client.refresh();});check(second.await(15,TimeUnit.SECONDS),"Failure completes");check(client.games.length()==2,"Failed refresh retains catalog");check(client.status.contains("503"),"Safe status reports HTTP failure");responder.join(1000);check(failure[0]==null,"Authenticated requests and pagination verified");result.putString("stream","PASS: "+checks+" RomM network checks");finish(Activity.RESULT_OK,result);
    }catch(Throwable t){result.putString("stream","FAIL: "+t);finish(Activity.RESULT_CANCELED,result);}finally{runOnMainSync(()->{client.close();client.prefs.edit().clear().commit();});}
  }
}
