using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using HtmlAgilityPack;

namespace KariyerDotNet
{
    class HtmlToListParser
    {

        private static List<string> jobDescDelimiters = new List<string>()
        {
            "İŞ TANIMI",
            "İŞ TANITIMI",
            "İŞ GEREKSİNİMLERİ",
            "GENEL İŞ TANIMI",
            "İŞ ÖZELLİKLERİ",
            "İŞ AÇIKLAMASI"
        };

        private static List<string> employeeDescDelimiters = new List<string>()
        {
            "GENEL TANITIM",
            "GENEL TANIM",
            "GENEL ÖZELLİKLER",
            "GENEL NİTELİKLER",
            "GENEL YETENEKLER",
            "İSTENEN YETENEK VE UZMANLIKLAR"
        };

        private static int FAIL_COUNTER = 0;

        /**
         * This function will decide to parse the string as;
         *      Job and Employee texts differently
         *      HTML Lists or Break Lines 
         * 
         */
        public static List<string> parse(string metin)
        {
            string jobDescDelimiter = String.Empty;
            string employeeDescDelimiter = String.Empty;
            foreach (string delimiter in jobDescDelimiters)
            {
                
                if (metin.ToUpper().Contains(delimiter))
                {
                    jobDescDelimiter = delimiter;
                    break;
                }
            }
            string jobText = String.Empty;
            string employeeText = String.Empty;
            bool hasDoubleText = true;
            if (!String.IsNullOrEmpty(jobDescDelimiter))
            {
                string[] temp = Regex.Split(metin, jobDescDelimiter, RegexOptions.IgnoreCase);
                if (temp[0].Trim() != String.Empty)
                {
                    // İş tanıtımı bölümü genel nitelikler bölümünden sonra geliyor.
                    jobText = temp[0];
                    employeeText = temp[1];
                }
                else
                {
                    // İş tanıtımı bölümü genel nitelikler bölümünden önce geliyor
                    // Genel nitelikler göz önününde bulundurularak ayrıştırılma yapılması gerek
                    foreach (string delimiter in employeeDescDelimiters)
                    {
                        if (metin.ToUpper().Contains(delimiter))
                        {
                            employeeDescDelimiter = delimiter;
                            break;
                        }
                    }
                    if (!String.IsNullOrEmpty(employeeDescDelimiter))
                    {
                        string[] empTemp = Regex.Split(metin, employeeDescDelimiter, RegexOptions.IgnoreCase);
                        if (empTemp[0].Trim() != String.Empty)
                        {
                            jobText = empTemp[1];
                            employeeText = empTemp[0];
                        }
                        else
                        {
                            // This case shouldn't be happening that much;
                            hasDoubleText = false;
                            FAIL_COUNTER++;
                        }
                    }
                    else
                    {
                        hasDoubleText = false;
                    }
                }
            }
            else
            {
                hasDoubleText = false;
            }
            List<string> result = null;
            if (!hasDoubleText)
            {
                result = textParse(metin);
            }
            else
            {
                result = textParse(jobText);
                //Console.WriteLine(jobText);
                //result.AddRange(textParse(employeeText));
            }
            return result;
        }


        private static List<string> textParse(string text)
        {
            List<string> items = new List<string>();
            bool containsLists = text.Contains("<li>");
            bool containsBreakLines = text.Contains("<br>");

            if (containsLists)
            {
                loadListTags(items, text);
            }

            if (containsBreakLines)
            {
                string[] texts = Regex.Split(text, "<br>", RegexOptions.IgnoreCase | RegexOptions.CultureInvariant);
                HashSet<int> listIndexes = new HashSet<int>();
                // Try to parse by list items, in break lines splitted texts.
                // If there is none, do nothing. Continue with break line listing.
                try
                {
                    for (int i = 0; i < texts.Length; i++)
                    {
                        string _text = texts[i];
                        // If that portion doesn't contain lists, include it with tags removed.
                        if (!_text.Contains("<li>") && !_text.Contains("</li>"))
                        {
                            //This regex replaces all tags with empty string.
                            _text = changeHTMLChars(_text).Trim();
                            _text = Regex.Replace(_text, "<.*?>", String.Empty);
                            // Dont include any text that is longer than 170 characters AND is ALL upper case
                            if(_text.Length <= 170 && !_text.ToUpper().Equals(_text))
                            {
                                items.Add(_text);
                            }
                        }
                    }
                }
                catch (NullReferenceException) { /* DO NOTHING. THIS CATCH SHOULDN'T BE WORKING AT ALL */ }
            }
            return items;
        }

        private static void loadListTags(List<string> list, string text)
        {
            var htmlDoc = new HtmlDocument();
            htmlDoc.LoadHtml(text);
            // Select all <li> tags
            var listItemNodes = htmlDoc.DocumentNode.SelectNodes("//li");

            foreach (var listItemNode in listItemNodes)
            {
                String txt = changeHTMLChars(listItemNode.InnerText);
                // Dont include any text that is longer than 240 characters AND is ALL upper case
                if (txt.Length < 240 && !txt.ToUpper().Equals(txt))
                {
                    list.Add(txt);
                }
            }
        }
        /**
         * Change such HTML characters such as
         *      &lt; &gt; &nbsp;
         *      to actual characters.
         */ 
        private static string changeHTMLChars(string text)
        {
            text = text.Replace("&nbsp;", " ").Replace("&lt;", "<").Replace("&gt;", ">").Replace("·", String.Empty);
            return text;
        }
    }
}
