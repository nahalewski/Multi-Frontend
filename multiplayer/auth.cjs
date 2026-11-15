const crypto=require('node:crypto'),{promisify}=require('node:util'),scrypt=promisify(crypto.scrypt);
const digest=s=>crypto.createHash('sha256').update(s).digest('hex');
function createAuth(store){
 const pub=u=>({id:u.id,name:u.name,username:u.username||null,guest:!u.username});
 async function issue(user){const token=crypto.randomBytes(32).toString('base64url');store.data.sessions[digest(token)]={user:user.id,expires:Date.now()+90*86400000};await store.save();return{token,user:pub(user)};}
 function resolve(token){if(typeof token!=='string'||token.length>200)return null;const s=store.data.sessions[digest(token)];if(!s||s.expires<Date.now())return null;return store.data.users[s.user]||null;}
 async function guest(name='Guest'){if(Object.keys(store.data.users).length>=10000)throw Error('Server account limit reached');const id=crypto.randomUUID(),user={id,name:String(name).trim().slice(0,32)||'Guest',created:Date.now()};store.data.users[id]=user;return issue(user);}
 async function register({username,password,name},current){username=String(username||'').trim();if(!/^[A-Za-z0-9_]{3,24}$/.test(username))throw Error('Use 3–24 letters, numbers or underscores.');if(typeof password!=='string'||password.length<10||password.length>128)throw Error('Use a password between 10 and 128 characters.');const key=username.toLowerCase();if(Object.values(store.data.users).some(u=>u.username?.toLowerCase()===key))throw Error('Username is unavailable.');const salt=crypto.randomBytes(16).toString('hex'),hashed=(await scrypt(password,salt,64)).toString('hex');if(Object.values(store.data.users).some(u=>u.username?.toLowerCase()===key))throw Error('Username is unavailable.');const user=current&&!current.username?current:{id:crypto.randomUUID(),created:Date.now()};Object.assign(user,{name:String(name||username).slice(0,32),username,salt,password:hashed});store.data.users[user.id]=user;return issue(user);}
 async function login({username,password}){if(typeof password!=='string'||password.length>128)throw Error('Username or password is incorrect.');const user=Object.values(store.data.users).find(u=>u.username?.toLowerCase()===String(username).toLowerCase());const salt=user?.salt||'00000000000000000000000000000000',hashed=await scrypt(password,salt,64);if(!user||!crypto.timingSafeEqual(Buffer.from(user.password,'hex'),hashed))throw Error('Username or password is incorrect.');return issue(user);}
 async function logout(token){delete store.data.sessions[digest(token)];await store.save();}
 return{pub,issue,resolve,guest,register,login,logout};
}
module.exports={createAuth,digest};
