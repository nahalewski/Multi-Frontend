package com.example.blades360;
import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import org.json.*;
import java.util.Arrays;
public final class FullBackupInstrumentation extends Instrumentation {
 int checks;
 void check(boolean condition){checks++;if(!condition)throw new AssertionError();}
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){Bundle out=new Bundle();try{MainActivity a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Thread.sleep(1000);FullSettingsBackup backup=new FullSettingsBackup(a);JSONObject snapshot=backup.snapshot();backup.validate(snapshot);check(snapshot.getJSONObject("stores").has("emulator_library"));check(snapshot.getJSONObject("stores").has("blades_profile"));byte[] plain=snapshot.toString().getBytes("UTF-8");char[] password="instrumentation-only-passphrase".toCharArray();byte[] encrypted=SettingsCipher.crypt(plain,password,true);check(Arrays.equals(plain,SettingsCipher.crypt(encrypted,password,false)));boolean rejected=false;try{SettingsCipher.crypt(encrypted,"incorrect-passphrase".toCharArray(),false);}catch(Exception e){rejected=true;}check(rejected);encrypted[encrypted.length-1]^=1;rejected=false;try{SettingsCipher.crypt(encrypted,password,false);}catch(Exception e){rejected=true;}check(rejected);Arrays.fill(plain,(byte)0);Arrays.fill(password,'\0');out.putString("stream","PASS: "+checks+" on-device full settings checks; IPTV configured: "+!a.iptv.source().isEmpty()+"; VPN active: "+a.vpn.active);}catch(Throwable e){out.putString("stream","FAIL: "+e.getClass().getSimpleName());}finish(out.getString("stream").startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,out);}
}
