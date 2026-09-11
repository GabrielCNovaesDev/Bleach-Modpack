Add-Type -AssemblyName System.Drawing
$paths = @{
    panel    = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_panel_full.png"
    health   = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_health_fill.png"
    reiatsu  = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_reiatsu_fill.png"
    transform = "C:\Users\Gabriel\Desktop\PROJETOS\Bleach-Modpack\src\main\resources\assets\bleachmod\textures\gui\hud\hud_transform_fill.png"
}
foreach ($k in $paths.Keys) {
    $bmp = [System.Drawing.Image]::FromFile($paths[$k])
    Write-Output ("{0} = {1}x{2}" -f $k, $bmp.Width, $bmp.Height)
    $bmp.Dispose()
}
