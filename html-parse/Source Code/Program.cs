using System;
using System.Collections.Generic;
using System.IO;

using ExcelDataReader;

namespace KariyerDotNet
{
    class Program
    {

        public struct ListItem
        {
     
            public string id { get; }
            public List<string> text { get; }
            public string exp { get; }
            public string maxExp { get; }

            public string jobInfo { get; }

            public ListItem(string id, List<string> text, string exp, string maxExp, string jobInfo)
            {
                this.id = id;
                this.text = text;
                this.exp = exp;
                this.maxExp = maxExp;
                this.jobInfo = jobInfo;
            }
        }

        public static List<List<string>> ilanlar = new List<List<string>>();
        public static List<List<string>> SWkelimeler = new List<List<string>>();
        public static List<List<string>> STkelimeler = new List<List<string>>();

        static void Main(string[] args)
        {
            int n = 0;
            if (args == null)
            {
                Console.Error.WriteLine("args is null");
                return;
            }
            else if (args.Length < 2 || !Int32.TryParse(args[0], out n))
            {
                Console.Error.WriteLine("Usage: KariyerDotNet.exe n filepath \n" +
                                        "n -> Number of rows to be read from source file (-1 for all file)\n" + 
                                        "filepath-> Name of the source file");
                //Stop execution
                return;
            }
            ParseHtmlFirstNItems(args[1], n);
        }

        public static void ParseHtml(string filePath)
        {
            ParseHtmlFirstNItems(filePath, 15000);
        }

        public static void ParseHtmlFirstNItems(string filePath, int n)
        {
            testFileWrite(n, filePath);
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
            List<ListItem> parsedLists = new List<ListItem>();
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
                string id = excelReader.GetDouble(2).ToString();
                string exp = excelReader.GetDouble(3).ToString();
                string maxExp = excelReader.GetDouble(4).ToString();
                string jobInfo = excelReader.GetString(6).ToString();

                ListItem item = new ListItem(id, HtmlToListParser.parse(text), exp, maxExp, jobInfo);
                parsedLists.Add(item);
            }
            excelReader.Close();
            string outputFilePath = "parse.txt";
            using (System.IO.StreamWriter file =
                new System.IO.StreamWriter(outputFilePath))
            {
                string delimiter = "-_-";
                foreach (var listItem in parsedLists)
                {
                    foreach (string text in listItem.text)
                    {
                        if (!String.IsNullOrEmpty(text))
                        {
                            string line = listItem.id + delimiter + listItem.exp + delimiter + listItem.maxExp + delimiter + listItem.jobInfo + delimiter + text;
                            file.WriteLine(line.Trim());
                        }
                    }
                }
            }
        }
    }
}