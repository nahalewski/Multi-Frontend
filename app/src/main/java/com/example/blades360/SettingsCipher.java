package com.example.blades360;
import java.io.*;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
/** Portable authenticated envelope: B360SET1, salt(16), nonce(12), AES-GCM ciphertext. */
final class SettingsCipher {
 static final byte[] MAGIC={'B','3','6','0','S','E','T','1'};
 static final int MAX=64*1024*1024;
 static byte[] crypt(byte[] input,char[] password,boolean encrypt)throws Exception{
  if(input.length>MAX)throw new IOException("Backup exceeds 64 MiB");
  byte[] salt=new byte[16],iv=new byte[12];
  if(encrypt){SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);}
  else{if(input.length<52||!Arrays.equals(MAGIC,Arrays.copyOf(input,8)))throw new IOException("Unsupported backup");System.arraycopy(input,8,salt,0,16);System.arraycopy(input,24,iv,0,12);}
  PBEKeySpec spec=new PBEKeySpec(password,salt,600000,256);byte[] key=null;
  try{key=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(encrypt?Cipher.ENCRYPT_MODE:Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));cipher.updateAAD(MAGIC);
   if(!encrypt)return cipher.doFinal(input,36,input.length-36);
   ByteArrayOutputStream out=new ByteArrayOutputStream();out.write(MAGIC);out.write(salt);out.write(iv);out.write(cipher.doFinal(input));return out.toByteArray();
  }finally{spec.clearPassword();if(key!=null)Arrays.fill(key,(byte)0);}
 }
}
