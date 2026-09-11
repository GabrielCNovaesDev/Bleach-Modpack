param(
    [Parameter(Mandatory = $true)]
    [string]$Source,

    [Parameter(Mandatory = $true)]
    [string]$Destination,

    [int]$MaxWidth = 1280,
    [int]$TargetHeight = 110
)

Add-Type -AssemblyName System.Drawing

$sourcePath = (Resolve-Path -LiteralPath $Source).Path
$sourceImage = [System.Drawing.Bitmap]::FromFile($sourcePath)

try {
    $minX = $sourceImage.Width
    $minY = $sourceImage.Height
    $maxX = -1
    $maxY = -1

    for ($y = 0; $y -lt $sourceImage.Height; $y++) {
        for ($x = 0; $x -lt $sourceImage.Width; $x++) {
            # Ignore near-transparent generation artifacts outside the actual rail.
            if ($sourceImage.GetPixel($x, $y).A -gt 16) {
                if ($x -lt $minX) { $minX = $x }
                if ($y -lt $minY) { $minY = $y }
                if ($x -gt $maxX) { $maxX = $x }
                if ($y -gt $maxY) { $maxY = $y }
            }
        }
    }

    if ($maxX -lt 0) {
        throw "The source image contains no visible pixels: $sourcePath"
    }

    $cropWidth = $maxX - $minX + 1
    $cropHeight = $maxY - $minY + 1
    # Every fill uses the same texture dimensions so the renderer can share
    # coordinates and calculate the visible width from a normalized value.
    $outputWidth = $MaxWidth
    $outputHeight = $TargetHeight

    $outputImage = New-Object System.Drawing.Bitmap($outputWidth, $outputHeight, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($outputImage)
        try {
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $graphics.DrawImage(
                $sourceImage,
                (New-Object System.Drawing.Rectangle(0, 0, $outputWidth, $outputHeight)),
                (New-Object System.Drawing.Rectangle($minX, $minY, $cropWidth, $cropHeight)),
                [System.Drawing.GraphicsUnit]::Pixel
            )
        }
        finally {
            $graphics.Dispose()
        }

        $destinationDirectory = Split-Path -Parent $Destination
        if (-not (Test-Path -LiteralPath $destinationDirectory)) {
            New-Item -ItemType Directory -Path $destinationDirectory | Out-Null
        }
        $outputImage.Save($Destination, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $outputImage.Dispose()
    }
}
finally {
    $sourceImage.Dispose()
}

[PSCustomObject]@{
    Source = $sourcePath
    Destination = (Resolve-Path -LiteralPath $Destination).Path
    Width = $outputWidth
    Height = $outputHeight
}
