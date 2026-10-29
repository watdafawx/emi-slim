# Waits for the TNP Limitless 8 client, waits until EMI has reloaded, then takes a live class histogram.
# Usage: pwsh tools/capture-histo.ps1 -Label before|after
param([Parameter(Mandatory)] [string] $Label, [int] $SettleSeconds = 60, [int] $TimeoutMinutes = 90)

$inst = 'G:\Games\Minecraft\Curse\Instances\TNP Limitless 8'
$jcmd = 'C:\Program Files\GraalVM\graalvm-jdk-21.0.9+7.1\bin\jcmd.exe'
$outDir = 'D:\heapdump\reports'
$deadline = (Get-Date).AddMinutes($TimeoutMinutes)

function Find-Game {
    Get-CimInstance Win32_Process -Filter "Name='javaw.exe' OR Name='java.exe'" |
        Where-Object { $_.CommandLine -like '*net.minecraft.client.main.Main*' -or $_.CommandLine -like '*TNP Limitless 8*' } | Sort-Object WorkingSetSize -Descending | Select-Object -First 1
}

Write-Output "[$Label] waiting for game process..."
while (-not ($proc = Find-Game)) {
    if ((Get-Date) -gt $deadline) { Write-Output 'timeout waiting for game'; exit 1 }
    Start-Sleep 5
}
$start = $proc.CreationDate
Write-Output "[$Label] game pid $($proc.ProcessId), started $start"

$log = Join-Path $inst 'logs\latest.log'
Write-Output "[$Label] waiting for 'Reloaded EMI in' (open the singleplayer world)..."
while ($true) {
    if ((Get-Date) -gt $deadline) { Write-Output 'timeout waiting for EMI reload'; exit 1 }
    if (-not (Get-Process -Id $proc.ProcessId -ErrorAction SilentlyContinue)) { Write-Output 'game closed before EMI reload'; exit 1 }
    $f = Get-Item $log -ErrorAction SilentlyContinue
    if ($f -and $f.LastWriteTime -gt $start) {
        $hit = Select-String -Path $log -Pattern 'Reloaded EMI in' -SimpleMatch | Select-Object -Last 1
        if ($hit) { break }
    }
    Start-Sleep 5
}
Write-Output "[$Label] $($hit.Line)"
Start-Sleep $SettleSeconds

$file = Join-Path $outDir "histo-$Label.txt"
& $jcmd $proc.ProcessId GC.class_histogram > $file
Write-Output "[$Label] histogram -> $file"
Select-String -Path $log -Pattern 'EMI Slim' | ForEach-Object { $_.Line } | Set-Content (Join-Path $outDir "emislim-log-$Label.txt")

$want = 'dev.emi.emi.api.stack.ItemEmiStack', 'net.minecraft.world.item.ItemStack', 'net.minecraft.core.component.PatchedDataComponentMap',
    'io.redspace.ironsspellbooks.capabilities.magic.SpellContainer', '[Ljava.lang.Object;', 'net.minecraft.core.component.DataComponentPatch',
    'dev.emi.emi.api.stack.TagEmiIngredient', 'java.util.ImmutableCollections$ListN'
Get-Content $file | ForEach-Object {
    $p = ($_ -split '\s+') | Where-Object { $_ }
    if ($p.Count -ge 4 -and ($want -contains $p[3])) { "{0,-60} {1,12} {2,10:N1} MB" -f $p[3], $p[1], ([double]$p[2] / 1MB) }
    elseif ($p.Count -ge 3 -and $p[0] -eq 'Total') { "{0,-60} {1,12} {2,10:N1} MB" -f 'Total', $p[1], ([double]$p[2] / 1MB) }
}
