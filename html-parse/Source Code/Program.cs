using System;
using System.Collections.Generic;
using System.IO;

using ExcelDataReader;

namespace KariyerDotNet
{
    class Program
    {
        public static List<List<string>> ilanlar = new List<List<string>>();
        public static List<List<string>> SWkelimeler = new List<List<string>>();
        public static List<List<string>> STkelimeler = new List<List<string>>();

        static void Main(string[] args)
        {
            int n = 0;
            if (args == null)
            {
                Console.Error.WriteLine("args is null");
            }
            else if (args.Length < 2 || !Int32.TryParse(args[0], out n))
            {
                Console.Error.WriteLine("Usage: KariyerDotNet.exe n filepath \n" +
                                        "n -> Number of rows to be read from source file (-1 for all file)\n" + 
                                        "filepath-> Name of the source file");
                //Stop execution
                return;
            }
            testFileWrite(n, args[1]);
        }

        public static void testFileWrite(int n, string filePath)
        {
            //Dosyayı okuyacağımı ve gerekli izinlerin ayarlanması.
            FileStream stream = File.Open(filePath, FileMode.Open, FileAccess.Read);
            //Encoding 1252 hatasını engellemek için;

            //System.Text.Encoding.RegisterProvider(System.Text.CodePagesEncodingProvider.Instance);

            IExcelDataReader excelReader;
            //Gönderdiğim dosya xls'mi xlsx formatında mı kontrol ediliyor.
            if (Path.GetExtension(filePath).ToUpper() == ".XLS")
            {
                //Reading from a binary Excel file ('97-2003 format; *.xls)
                excelReader = ExcelReaderFactory.CreateBinaryReader(stream);
            }
            else
            {
                //Reading from a OpenXml Excel file (2007 format; *.xlsx)
                excelReader = ExcelReaderFactory.CreateOpenXmlReader(stream);
            }
            List<List<string>> parsedLists = new List<List<string>>();
            excelReader.Read();
            if(n < 0 || n > excelReader.RowCount)
            {
                n = excelReader.RowCount;
            }
            // Read first n records for testing
            for (int i = 0; i < n; i++)
            {
                excelReader.Read();
                string text = excelReader.GetString(7).ToString();
                parsedLists.Add(HtmlToListParser.parse(text));
            }
            excelReader.Close();
            string outputFilePath = "parse.txt";
            using (System.IO.StreamWriter file =
                new System.IO.StreamWriter(outputFilePath))
            {
                foreach (var list in parsedLists)
                {
                    foreach (string item in list)
                    {
                        if (!String.IsNullOrEmpty(item))
                        {
                            file.WriteLine(item.Trim());
                        }
                    }
                }
            }
        }
    }
}
