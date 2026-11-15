using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
public class B360Display {
 [DllImport("user32.dll",CharSet=CharSet.Unicode)] static extern bool EnumDisplaySettings(string device,int mode,IntPtr data);
 [DllImport("user32.dll",CharSet=CharSet.Unicode)] static extern int ChangeDisplaySettingsEx(string device,IntPtr data,IntPtr hwnd,uint flags,IntPtr param);
 [DllImport("user32.dll")] static extern int GetDisplayConfigBufferSizes(uint flags,out uint paths,out uint modes);
 [DllImport("user32.dll")] static extern int QueryDisplayConfig(uint flags,ref uint paths,IntPtr path,ref uint modes,IntPtr mode,IntPtr topology);
 [DllImport("user32.dll")] static extern int DisplayConfigGetDeviceInfo(IntPtr info);
 [DllImport("user32.dll")] static extern int DisplayConfigSetDeviceInfo(IntPtr info);
 public class Mode { public int width,height,hz; public Mode(byte[] b){width=BitConverter.ToInt32(b,172);height=BitConverter.ToInt32(b,176);hz=BitConverter.ToInt32(b,184);} }
 public class Color { public bool known,supported,enabled;public int bits; }
 static IntPtr Zero(int n){IntPtr p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 public static byte[] Read(string device,int mode){IntPtr p=Zero(220);try{Marshal.WriteInt16(p,68,220);if(!EnumDisplaySettings(device,mode,p))return null;byte[] b=new byte[220];Marshal.Copy(p,b,0,220);return b;}finally{Marshal.FreeHGlobal(p);}}
 public static Mode Current(string device){byte[] b=Read(device,-1);return b==null?null:new Mode(b);}
 public static Mode[] Modes(string device){var list=new List<Mode>();var seen=new HashSet<string>();for(int i=0;i<4096;i++){byte[] b=Read(device,i);if(b==null)break;var m=new Mode(b);if(BitConverter.ToInt32(b,168)!=32||m.width<640||m.height<480||m.hz<20)continue;string k=m.width+"x"+m.height+"@"+m.hz;if(seen.Add(k))list.Add(m);}return list.ToArray();}
 public static int Raw(string device,byte[] b,uint flags){if(b==null)throw new Exception("Original display mode unavailable");IntPtr p=Zero(220);try{Marshal.Copy(b,0,p,220);return ChangeDisplaySettingsEx(device,p,IntPtr.Zero,flags,IntPtr.Zero);}finally{Marshal.FreeHGlobal(p);}}
 public static byte[] Find(string device,int w,int h,int hz){for(int i=0;i<4096;i++){byte[] b=Read(device,i);if(b==null)break;var m=new Mode(b);if(m.width==w&&m.height==h&&m.hz==hz&&BitConverter.ToInt32(b,168)==32){BitConverter.GetBytes(0x5c0000).CopyTo(b,72);return b;}}throw new Exception("Mode is not advertised by this display");}
 static IntPtr Header(int type,int size,IntPtr path,int adapter,int id){IntPtr p=Zero(size);Marshal.WriteInt32(p,0,type);Marshal.WriteInt32(p,4,size);Marshal.WriteInt64(p,8,Marshal.ReadInt64(path,adapter));Marshal.WriteInt32(p,16,Marshal.ReadInt32(path,id));return p;}
 static byte[] Target(string device){uint pc,mc;if(GetDisplayConfigBufferSizes(2,out pc,out mc)!=0||pc>128||mc>512)return null;IntPtr paths=Zero((int)pc*72),modes=Zero((int)mc*64);try{if(QueryDisplayConfig(2,ref pc,paths,ref mc,modes,IntPtr.Zero)!=0)return null;for(int i=0;i<pc;i++){IntPtr path=IntPtr.Add(paths,i*72),name=Header(1,84,path,0,8);try{if(DisplayConfigGetDeviceInfo(name)==0&&Marshal.PtrToStringUni(IntPtr.Add(name,20))==device){byte[] result=new byte[72];Marshal.Copy(path,result,0,72);return result;}}finally{Marshal.FreeHGlobal(name);}}return null;}finally{Marshal.FreeHGlobal(paths);Marshal.FreeHGlobal(modes);}}
 public static Color Hdr(string device){var result=new Color();byte[] path=Target(device);if(path==null)return result;IntPtr p=Zero(72);Marshal.Copy(path,0,p,72);IntPtr info=Header(15,36,p,20,28);try{if(DisplayConfigGetDeviceInfo(info)==0){int flags=Marshal.ReadInt32(info,20);result.known=true;result.supported=(flags&16)!=0&&(flags&8)==0;result.enabled=Marshal.ReadInt32(info,32)==2;result.bits=Marshal.ReadInt32(info,28);}return result;}finally{Marshal.FreeHGlobal(info);Marshal.FreeHGlobal(p);}}
 public static void SetHdr(string device,bool enabled){var current=Hdr(device);if(!current.known||!current.supported)throw new Exception("HDR control is unavailable on this display");byte[] path=Target(device);if(path==null)throw new Exception("Display disconnected");IntPtr p=Zero(72);Marshal.Copy(path,0,p,72);IntPtr info=Header(16,24,p,20,28);try{Marshal.WriteInt32(info,20,enabled?1:0);int error=DisplayConfigSetDeviceInfo(info);if(error!=0)throw new Exception("Windows rejected HDR change: "+error);}finally{Marshal.FreeHGlobal(info);Marshal.FreeHGlobal(p);}}
}
