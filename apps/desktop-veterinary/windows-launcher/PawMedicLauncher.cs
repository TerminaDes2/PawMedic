using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

internal static class PawMedicLauncher
{
    [STAThread]
    private static void Main()
    {
        string directory = AppDomain.CurrentDomain.BaseDirectory;
        string java = Path.Combine(directory, "runtime", "bin", "javaw.exe");
        try
        {
            if (!File.Exists(java) || !Directory.Exists(Path.Combine(directory, "lib")))
            {
                throw new FileNotFoundException();
            }

            var start = new ProcessStartInfo
            {
                FileName = java,
                Arguments = "--enable-native-access=ALL-UNNAMED -cp \""
                    + Path.Combine(directory, "lib", "*")
                    + "\" com.pawsmedic.desktop.veterinary.MainKt",
                WorkingDirectory = directory,
                UseShellExecute = false,
                CreateNoWindow = true
            };
            using (Process process = Process.Start(start))
            {
                if (process == null || (process.WaitForExit(3000) && process.ExitCode != 0))
                {
                    throw new InvalidOperationException();
                }
            }
        }
        catch (Exception error)
        {
            if (!(error is IOException) && !(error is InvalidOperationException)
                && !(error is System.ComponentModel.Win32Exception)
                && !(error is UnauthorizedAccessException))
            {
                throw;
            }
            MessageBox.Show(
                "No se pudo iniciar PawMedic. Comprueba que se hayan extraído todos los archivos e inténtalo de nuevo.",
                "PawMedic", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
    }
}
