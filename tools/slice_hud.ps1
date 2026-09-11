Add-Type -AssemblyName System.Drawing
$src = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_panel_full.png"
$bmp = [System.Drawing.Bitmap]::FromFile($src)

$out = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\run\hud_slice"
if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }

function Crop($src, $rect, $destPath) {
    $dst = New-Object System.Drawing.Bitmap($rect.Width, $rect.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($dst)
    $g.DrawImage($src, 0, 0, $rect, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    $dst.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $dst.Dispose()
}

Crop $bmp (New-Object System.Drawing.Rectangle(280, 180, 1900, 130)) "$out/vida_band.png"
Crop $bmp (New-Object System.Drawing.Rectangle(280, 320, 1900, 130)) "$out/reiatsu_band.png"
Crop $bmp (New-Object System.Drawing.Rectangle(280, 450, 1900, 130)) "$out/transform_band.png"

$bmp.Dispose()
Get-ChildItem $out
