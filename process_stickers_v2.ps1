
$code = @"
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.IO;

public class ImageProcessor {
    public static void ProcessImage(string filePath, int tolerance) {
        Console.WriteLine("Processing: " + filePath);
        string tempPath = filePath + ".tmp.png";
        
        try {
            using (Bitmap original = new Bitmap(filePath)) {
                // Ensure 32-bit ARGB
                using (Bitmap bmp = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb)) {
                    bmp.SetResolution(original.HorizontalResolution, original.VerticalResolution);
                    
                    using (Graphics g = Graphics.FromImage(bmp)) {
                        g.DrawImage(original, 0, 0);
                    }

                    // Flood Fill Algorithm
                    BitmapData data = bmp.LockBits(new Rectangle(0, 0, bmp.Width, bmp.Height), ImageLockMode.ReadWrite, bmp.PixelFormat);
                    int bytesPerPixel = 4;
                    int stride = data.Stride;
                    int height = bmp.Height;
                    int width = bmp.Width;
                    
                    IntPtr ptr = data.Scan0;
                    int bytes = Math.Abs(data.Stride) * bmp.Height;
                    byte[] rgbValues = new byte[bytes];
                    Marshal.Copy(ptr, rgbValues, 0, bytes);

                    // Get background color from (0,0)
                    int bIndex = 0;
                    byte refB = rgbValues[bIndex];
                    byte refG = rgbValues[bIndex + 1];
                    byte refR = rgbValues[bIndex + 2];
                    byte refA = rgbValues[bIndex + 3];

                    // BFS Queue
                    Queue<Point> q = new Queue<Point>();
                    bool[,] visited = new bool[width, height];

                    if (IsSimilar(refR, refG, refB, refR, refG, refB, tolerance)) {
                        q.Enqueue(new Point(0, 0));
                        q.Enqueue(new Point(width - 1, 0));
                        q.Enqueue(new Point(0, height - 1));
                        q.Enqueue(new Point(width - 1, height - 1));
                        visited[0, 0] = true;
                        visited[width - 1, 0] = true;
                        visited[0, height - 1] = true;
                        visited[width - 1, height - 1] = true;
                    }

                    while (q.Count > 0) {
                        Point p = q.Dequeue();
                        int idx = (p.Y * stride) + (p.X * bytesPerPixel);

                        // Set transparent
                        rgbValues[idx + 3] = 0; 
                        
                        // Check neighbors
                        CheckNeighbor(p.X + 1, p.Y, width, height, rgbValues, stride, bytesPerPixel, visited, q, refR, refG, refB, tolerance);
                        CheckNeighbor(p.X - 1, p.Y, width, height, rgbValues, stride, bytesPerPixel, visited, q, refR, refG, refB, tolerance);
                        CheckNeighbor(p.X, p.Y + 1, width, height, rgbValues, stride, bytesPerPixel, visited, q, refR, refG, refB, tolerance);
                        CheckNeighbor(p.X, p.Y - 1, width, height, rgbValues, stride, bytesPerPixel, visited, q, refR, refG, refB, tolerance);
                    }

                    Marshal.Copy(rgbValues, 0, ptr, bytes);
                    bmp.UnlockBits(data);
                    bmp.Save(tempPath, ImageFormat.Png);
                }
            }
            
            if (File.Exists(tempPath)) {
                File.Delete(filePath);
                File.Move(tempPath, filePath);
                Console.WriteLine("Saved updated image.");
            }
        } catch (Exception ex) {
            Console.WriteLine("Error: " + ex.Message);
        }
    }

    private static void CheckNeighbor(int x, int y, int width, int height, byte[] rgbValues, int stride, int bpp, bool[,] visited, Queue<Point> q, byte r, byte g, byte b, int tolerance) {
        if (x < 0 || x >= width || y < 0 || y >= height || visited[x, y]) return;

        int idx = (y * stride) + (x * bpp);
        byte cb = rgbValues[idx];
        byte cg = rgbValues[idx + 1];
        byte cr = rgbValues[idx + 2];

        if (IsSimilar(cr, cg, cb, r, g, b, tolerance)) {
            visited[x, y] = true;
            q.Enqueue(new Point(x, y));
        }
    }

    private static bool IsSimilar(byte r1, byte g1, byte b1, byte r2, byte g2, byte b2, int tolerance) {
        return Math.Abs(r1 - r2) <= tolerance &&
               Math.Abs(g1 - g2) <= tolerance &&
               Math.Abs(b1 - b2) <= tolerance;
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
        [ImageProcessor]::ProcessImage($fullPath, 50) # Tolerance of 50
    }
    else {
        Write-Host "File not found: $fullPath"
    }
}
