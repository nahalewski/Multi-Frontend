param([Parameter(Mandatory=$true)][ValidatePattern('^[a-fA-F0-9-]{36}$')][string]$TunnelId, [Parameter(Mandatory=$true)][ValidatePattern('^[a-zA-Z0-9.-]+$')][string]$Hostname)
$ErrorActionPreference = 'Stop'
$serverHome = Join-Path $env:LOCALAPPDATA 'Blades360 Multiplayer'
$appFolder = Join-Path $serverHome 'app'
New-Item -ItemType Directory -Force -Path $appFolder | Out-Null
Get-ChildItem -LiteralPath $PSScriptRoot -File | Where-Object { $_.Extension -in '.cjs','.py','.json','.ps1','.md' } | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $appFolder -Force }
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'public') -Destination $appFolder -Recurse -Force
New-Item -ItemType Directory -Force -Path (Join-Path $appFolder 'node_modules') | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'node_modules\ws') -Destination (Join-Path $appFolder 'node_modules') -Recurse -Force
$credential = Join-Path $env:USERPROFILE (".cloudflared\" + $TunnelId + ".json")
if (-not (Test-Path -LiteralPath $credential)) { throw 'This PC does not have the configured multiplayer tunnel credentials.' }
$config = @"
tunnel: $TunnelId
credentials-file: '$credential'
ingress:
  - hostname: $Hostname
    service: http://127.0.0.1:8788
  - service: http_status:404
"@
[IO.File]::WriteAllText((Join-Path $serverHome 'tunnel.yml'), $config)
$script = Join-Path $appFolder 'run-server.ps1'
$launcher = Join-Path $serverHome 'start.vbs'
$vbs = 'CreateObject("WScript.Shell").Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File ""' + $script + '""", 0, False'
[IO.File]::WriteAllText($launcher, $vbs)
$runKey = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Run'
New-ItemProperty -Path $runKey -Name 'Blades360 Multiplayer' -Value ('wscript.exe "' + $launcher + '"') -PropertyType String -Force | Out-Null
Start-Process -FilePath 'wscript.exe' -ArgumentList ('"' + $launcher + '"') -WindowStyle Hidden
Write-Output 'Installed independent multiplayer server and current-user Windows sign-in startup.'
