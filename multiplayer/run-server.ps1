$ErrorActionPreference = 'Stop'
$serverHome = Split-Path -Parent $PSScriptRoot
$mutex = [System.Threading.Mutex]::new($false, 'Local\Blades360MultiplayerSupervisor')
if (-not $mutex.WaitOne(0)) { exit }
$node = 'C:\Program Files\nodejs\node.exe'
$cloudflared = Join-Path $env:USERPROFILE 'bin\cloudflared.exe'
$env:B360_MULTIPLAYER_DATA = Join-Path $serverHome 'data'
$env:B360_KARAOKE_PYTHON = Join-Path $serverHome 'python\Scripts\python.exe'
$env:B360_FFMPEG = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Links\ffmpeg.exe'
$env:B360_FFPROBE = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Links\ffprobe.exe'
$env:PYTHONUTF8 = '1'
$config = Join-Path $serverHome 'tunnel.yml'
$logs = Join-Path $serverHome 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null
$nodeProcess = $null
$tunnelProcess = $null
try {
 while ($true) {
  if (-not $nodeProcess -or $nodeProcess.HasExited) {
   $healthy = $false
   try { $healthy = (Invoke-RestMethod 'http://127.0.0.1:8788/health' -TimeoutSec 2).service -eq 'Blades360 Multiplayer' } catch {}
   if (-not $healthy) {
    $nodeProcess = Start-Process -FilePath $node -ArgumentList @('"' + (Join-Path $PSScriptRoot 'server.cjs') + '"') -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logs 'server.log') -RedirectStandardError (Join-Path $logs 'server-error.log')
   }
  }
  if (-not $tunnelProcess -or $tunnelProcess.HasExited) {
   $tunnelProcess = Start-Process -FilePath $cloudflared -ArgumentList ('tunnel --config "' + $config + '" run') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logs 'tunnel.log') -RedirectStandardError (Join-Path $logs 'tunnel-error.log')
  }
  Start-Sleep -Seconds 10
 }
} finally { $mutex.ReleaseMutex(); $mutex.Dispose() }
