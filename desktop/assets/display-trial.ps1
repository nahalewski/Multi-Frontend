$r=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($requestData))|ConvertFrom-Json
if(-not (@([System.Windows.Forms.Screen]::AllScreens.DeviceName) -contains $r.device)){throw 'Display disconnected'}
$old=[B360Display]::Read($r.device,-1);$oldHdr=[B360Display]::Hdr($r.device);$applied=$false;$keep=$false
try {
 if($r.kind -eq 'mode'){$next=[B360Display]::Find($r.device,$r.width,$r.height,$r.hz);if([B360Display]::Raw($r.device,$next,2) -ne 0){throw 'Windows rejected display mode'};if([B360Display]::Raw($r.device,$next,0) -ne 0){throw 'Display change failed'}}else{[B360Display]::SetHdr($r.device,$r.enabled)}
 $applied=$true;Write-Output '{"ready":true}';[Console]::Out.Flush();$until=[DateTime]::UtcNow.AddSeconds(20)
 while([DateTime]::UtcNow -lt $until){if(Test-Path -LiteralPath $r.marker){$answer=Get-Content -LiteralPath $r.marker -Raw;$keep=$answer -eq 'keep';break};Start-Sleep -Milliseconds 200}
 if($keep -and $r.kind -eq 'mode'){if([B360Display]::Raw($r.device,$next,1) -ne 0){$keep=$false}}
} finally {
 if($applied -and -not $keep){if($r.kind -eq 'mode'){[void][B360Display]::Raw($r.device,$old,0)}else{[B360Display]::SetHdr($r.device,$oldHdr.enabled)}}
 if(Test-Path -LiteralPath $r.marker){Remove-Item -LiteralPath $r.marker -Force}
}
Write-Output (@{kept=$keep}|ConvertTo-Json -Compress)
