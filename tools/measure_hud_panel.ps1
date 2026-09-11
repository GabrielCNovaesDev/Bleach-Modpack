Add-Type -AssemblyName System.Drawing
$src = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_panel_full.png"
$bmp = [System.Drawing.Bitmap]::FromFile($src)

function IsRed($p) { if ($p.A -lt 80) { return $false }; $r=$p.R;$g=$p.G;$b=$p.B; return ($r -gt 150 -and $g -lt 100 -and $b -lt 110) }
function IsCyan($p) { if ($p.A -lt 80) { return $false }; $r=$p.R;$g=$p.G;$b=$p.B; return ($r -lt 110 -and $g -gt 150 -and $b -gt 170) }
function IsBlue($p) { if ($p.A -lt 80) { return $false }; $r=$p.R;$g=$p.G;$b=$p.B; return ($r -lt 130 -and $g -lt 160 -and $b -gt 160 -and $b -gt $r + 30) }
function IsWhite($p) { if ($p.A -lt 80) { return $false }; $r=$p.R;$g=$p.G;$b=$p.B; return ($r -gt 220 -and $g -gt 220 -and $b -gt 220) }

# Skip Hollow emblem by starting at x=600.
function SpanKind($bmp, $yLo, $yHi, $xLo, $xHi, $kind) {
    $first = -1; $last = -1
    for ($x = $xLo; $x -le $xHi; $x++) {
        $n = 0
        for ($y = $yLo; $y -le $yHi; $y++) {
            $p = $bmp.GetPixel($x, $y)
            $ok = $false
            switch ($kind) {
                "red"  { $ok = IsRed $p }
                "cyan" { $ok = IsCyan $p }
                "blue" { $ok = IsBlue $p }
            }
            if ($ok) { $n++ }
        }
        if ($n -gt 5) {
            if ($first -lt 0) { $first = $x }
            $last = $x
        }
    }
    return @{First=$first; Last=$last; Width=if ($first -lt 0) { 0 } else { $last - $first + 1 } }
}

# VIDA fill band: y=244..298 (solid red), start AFTER label
$vidaFill = SpanKind $bmp 244 298 600 2150 "red"
$reiatsuFill = SpanKind $bmp 373 414 600 2150 "cyan"
$transformFill = SpanKind $bmp 497 557 600 2150 "blue"

Write-Output ("VIDA red fill x-range (skip hollow): $($vidaFill.First)..$($vidaFill.Last) w=$($vidaFill.Width)")
Write-Output ("REIATSU cyan fill x-range: $($reiatsuFill.First)..$($reiatsuFill.Last) w=$($reiatsuFill.Width)")
Write-Output ("TRANSFORM blue fill x-range: $($transformFill.First)..$($transformFill.Last) w=$($transformFill.Width)")

# Find solid top/bot per color in their respective slot bands (skip Hollow).
function SolidYRange($bmp, $kind, $xLo, $xHi, $yLo, $yHi, $minDensity) {
    $top = -1; $bot = -1
    for ($y = $yLo; $y -le $yHi; $y++) {
        $n = 0
        for ($x = $xLo; $x -le $xHi; $x++) {
            $p = $bmp.GetPixel($x, $y)
            $ok = $false
            switch ($kind) {
                "red"  { $ok = IsRed $p }
                "cyan" { $ok = IsCyan $p }
                "blue" { $ok = IsBlue $p }
            }
            if ($ok) { $n++ }
        }
        if ($n -gt $minDensity) {
            if ($top -lt 0) { $top = $y }
            $bot = $y
        }
    }
    return @{Top=$top; Bot=$bot; H=if ($top -lt 0) { 0 } else { $bot - $top + 1 } }
}

# Slot bbox for each (without hollow). Use whole slot area.
$vidaSolid = SolidYRange $bmp "red" 600 1900 200 320 80
$reiatsuSolid = SolidYRange $bmp "cyan" 600 1900 320 440 80
$transformSolid = SolidYRange $bmp "blue" 600 1900 440 600 80
Write-Output ("VIDA solid red: y=$($vidaSolid.Top)..$($vidaSolid.Bot) h=$($vidaSolid.H)")
Write-Output ("REIATSU solid cyan: y=$($reiatsuSolid.Top)..$($reiatsuSolid.Bot) h=$($reiatsuSolid.H)")
Write-Output ("TRANSFORM solid blue: y=$($transformSolid.Top)..$($transformSolid.Bot) h=$($transformSolid.H)")

# Label end: rightmost WHITE pixel inside the LABEL tab (above solid fill, inside slot)
function LabelRightEdge($bmp, $yLo, $yHi, $xLo, $xHi) {
    $last = -1; $lastY = -1
    for ($y = $yLo; $y -le $yHi; $y++) {
        for ($x = $xLo; $x -le $xHi; $x++) {
            $p = $bmp.GetPixel($x, $y)
            if (IsWhite $p) {
                if ($x -gt $last) { $last = $x; $lastY = $y }
            }
        }
    }
    return @{X=$last; Y=$lastY}
}

# Look for label end per band. The label sits just above the solid fill, in a slightly higher y range.
# VIDA label is roughly y=200..240 (above solid y=244..298)
$vL = LabelRightEdge $bmp 195 245 600 1200
$rL = LabelRightEdge $bmp 330 375 600 1200
$tL = LabelRightEdge $bmp 455 500 600 1200
Write-Output ("VIDA label right edge: x=$($vL.X) at y=$($vL.Y)")
Write-Output ("REIATSU label right edge: x=$($rL.X) at y=$($rL.Y)")
Write-Output ("TRANSFORM label right edge: x=$($tL.X) at y=$($tL.Y)")

# Frame: find the WHITE outline that bounds each slot. The slot's outline appears as
# a thin white line. Scan horizontally at fixed y to find leftmost and rightmost WHITE
# pixel inside the panel area.
function WhiteFrameSpan($bmp, $y, $xLo, $xHi) {
    $l = -1; $r = -1
    for ($x = $xLo; $x -le $xHi; $x++) {
        $p = $bmp.GetPixel($x, $y)
        if (IsWhite $p) {
            if ($l -lt 0) { $l = $x }
            $r = $x
        }
    }
    return @{L=$l; R=$r}
}

# Try several y values to bracket the slot vertical range.
foreach ($probe in @(195, 210, 230, 250, 290, 305, 320, 345, 365, 385, 420, 435, 460, 480, 510, 540, 560, 580)) {
    $r = WhiteFrameSpan $bmp $probe 600 2150
    Write-Output ("white scan y=$probe : l=$($r.L) r=$($r.R)")
}

$bmp.Dispose()
