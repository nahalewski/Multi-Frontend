package com.example.blades360;
import android.app.*;
import android.os.Bundle;
import org.json.*;
import java.nio.file.Files;
import java.io.File;
public final class VpnInstrumentation extends Instrumentation {
 int checks;
 void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){Bundle out=new Bundle();try{
  JSONArray regions=PiaApi.regions();check(regions.length()>0,"Live US server list");
  JSONObject florida=PiaApi.choose(regions,"us_florida");check(florida.getString("country").equals("US"),"Florida country");
  check(florida.getJSONObject("servers").getJSONArray("wg").length()>0,"Florida WireGuard endpoint");
  boolean rejected=false;try{PiaApi.choose(regions,"invalid-region");}catch(Exception e){rejected=true;}check(rejected,"Missing region rejected");
  String fixture="vpn-encryption-test-value";PrivateMediaFile.write(getTargetContext(),"vpn-test.bin",fixture);
  check(PrivateMediaFile.read(getTargetContext(),"vpn-test.bin").equals(fixture),"Encrypted storage round trip");
  check(!new String(Files.readAllBytes(new File(getTargetContext().getFilesDir(),"vpn-test.bin").toPath()),"UTF-8").contains(fixture),"No plaintext on disk");
  BladesVpn vpn=BladesVpn.get(getTargetContext());check(!vpn.active&&!vpn.handshake,"No false connected state");
  out.putString("stream","PASS: "+checks+" VPN checks");
 }catch(Throwable e){out.putString("stream","FAIL: "+e.getClass().getSimpleName());}finally{getTargetContext().deleteFile("vpn-test.bin");}finish(out.getString("stream").startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,out);}
}
