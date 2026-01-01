
$code = @"
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

public class ImageProcessor {
    public static void ProcessImage(string filePath, int threshold) {
        Console.WriteLine("Processing: " + filePath);
        string tempPath = filePath + ".tmp.png";
        
        try {
            using (Bitmap original = new Bitmap(filePath)) {
                using (Bitmap newBmp = new Bitmap(original.Width, original.Height)) {
                    // transfer resolution
                    newBmp.SetResolution(original.HorizontalResolution, original.VerticalResolution);

                    for (int y = 0; y < original.Height; y++) {
                        for (int x = 0; x < original.Width; x++) {
                            Color c = original.GetPixel(x, y);
                            // Simple threshold check for "White"
                            if (c.R >= threshold && c.G >= threshold && c.B >= threshold) {
                                newBmp.SetPixel(x, y, Color.Transparent);
                            } else {
                                newBmp.SetPixel(x, y, c);
                            }
                        }
                    }
                    newBmp.Save(tempPath, ImageFormat.Png);
                }
            }
            
            // Swap files
            if (File.Exists(tempPath)) {
                File.Delete(filePath);
                File.Move(tempPath, filePath);
                Console.WriteLine("Saved updated image.");
            }
        } catch (Exception ex) {
            Console.WriteLine("Error: " + ex.Message);
        }
    }
}
"@

Add-Type -TypeDefinition $code -ReferencedAssemblies System.Drawing

$files = @(
    "sticker_real_car.png", 
    "sticker_real_cat.png", 
    "sticker_real_flower.png", 
    "sticker_real_pizza.png"
)

$directory = "D:\projects\antigravity\Workmate\app\src\main\res\drawable"

foreach ($file in $files) {
    $fullPath = Join-Path $directory $file
    if (Test-Path $fullPath) {
        [ImageProcessor]::ProcessImage($fullPath, 240)
    } else {
        Write-Host "File not found: $fullPath"
    }
}
