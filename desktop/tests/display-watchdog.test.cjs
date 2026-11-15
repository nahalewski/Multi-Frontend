const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path'),{spawn}=require('node:child_process');
test('independent display watchdog reverts timeout and persists only explicit keep',{skip:process.platform!=='win32',timeout:45000},async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'b360-watchdog-'));assert(path.resolve(dir).startsWith(path.resolve(os.tmpdir())+path.sep));
 try{const body=await fs.readFile(path.join(__dirname,'../assets/display-trial.ps1'),'utf8');
 for(const keep of [false,true]){const marker=path.join(dir,'confirm'),request=Buffer.from(JSON.stringify({device:'fixture',kind:'mode',width:1920,height:1080,hz:60,marker})).toString('base64');
 // Stub native operations only; this test never invokes display mutation APIs.
 const setup="$ErrorActionPreference='Stop';Add-Type -TypeDefinition @'\nusing System; public class B360Display { public static string Log=\"\"; public static byte[] Read(string d,int n){return new byte[]{0};} public static object Hdr(string d){return null;} public static byte[] Find(string d,int w,int h,int hz){return new byte[]{1};} public static int Raw(string d,byte[] b,uint flags){Log+=b[0]+\":\"+flags+\",\";return 0;} public static void SetHdr(string d,bool b){} }\n'@\n";
 const script=setup+"$requestData='"+request+"'\n"+body.replace("if(-not (@([System.Windows.Forms.Screen]::AllScreens.DeviceName) -contains $r.device)){throw 'Display disconnected'}","")+"\nWrite-Output ([B360Display]::Log)";
 const result=await new Promise((resolve,reject)=>{const child=spawn('powershell.exe',['-NoProfile','-NonInteractive','-EncodedCommand',Buffer.from(script,'utf16le').toString('base64')],{windowsHide:true});let output='',error='',answered=false;child.stdout.on('data',chunk=>{output+=chunk;if(keep&&!answered&&output.includes('"ready":true')){answered=true;fs.writeFile(marker,'keep').catch(reject);}});child.stderr.on('data',chunk=>error+=chunk);child.on('error',reject);child.on('close',code=>code?reject(Error(error)):resolve(output));});
 assert.match(result,keep?/1:2,1:0,1:1,/:/1:2,1:0,0:0,/);
 }
 }finally{await fs.rm(dir,{recursive:true,force:true});}
});
