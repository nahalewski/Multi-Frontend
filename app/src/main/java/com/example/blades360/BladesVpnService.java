package com.example.blades360;
import android.app.*;
import android.content.*;
import android.os.IBinder;

/** Keeps the app-owned tunnel alive, with a visible disconnect action. */
public final class BladesVpnService extends Service {
 static BladesVpnService instance;static final int ID=3861;BladesVpn vpn;
 @Override public void onCreate(){super.onCreate();instance=this;vpn=BladesVpn.get(this);NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("blades-vpn","PIA VPN",NotificationManager.IMPORTANCE_LOW));}
 Notification notification(){PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,BladesVpnService.class).setAction("disconnect"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return new Notification.Builder(this,"blades-vpn").setSmallIcon(R.drawable.ic_blades_monochrome).setContentTitle("Blades 360 · PIA VPN").setContentText(vpn.status).setContentIntent(open).setOngoing(true).addAction(new Notification.Action.Builder(null,"Disconnect",stop).build()).build();}
 void update(){if(instance==this)getSystemService(NotificationManager.class).notify(ID,notification());}
 @Override public int onStartCommand(Intent intent,int flags,int id){startForeground(ID,notification());if(intent!=null&&"disconnect".equals(intent.getAction()))vpn.disconnect();else vpn.connect();return START_NOT_STICKY;}
 @Override public void onDestroy(){instance=null;super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}
