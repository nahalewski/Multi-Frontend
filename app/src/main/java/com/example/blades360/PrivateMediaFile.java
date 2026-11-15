package com.example.blades360;
import android.content.Context;
import android.security.keystore.*;
import android.util.AtomicFile;
import java.io.*;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
final class PrivateMediaFile {
 static javax.crypto.SecretKey key()throws Exception{KeyStore k=KeyStore.getInstance("AndroidKeyStore");k.load(null);String alias="blades-private-media";if(k.containsAlias(alias))return (javax.crypto.SecretKey)k.getKey(alias,null);KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(alias,3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());return g.generateKey();}
 static void write(Context c,String name,String value)throws Exception{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());AtomicFile file=new AtomicFile(new File(c.getFilesDir(),name));FileOutputStream out=null;try{out=file.startWrite();out.write(cipher.getIV().length);out.write(cipher.getIV());out.write(cipher.doFinal(value.getBytes("UTF-8")));file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 static String read(Context c,String name)throws Exception{byte[] data=new AtomicFile(new File(c.getFilesDir(),name)).readFully();int n=data[0]&255;if(n!=12||data.length<n+17)throw new IOException();Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,java.util.Arrays.copyOfRange(data,1,n+1)));return new String(cipher.doFinal(data,n+1,data.length-n-1),"UTF-8");}
}
