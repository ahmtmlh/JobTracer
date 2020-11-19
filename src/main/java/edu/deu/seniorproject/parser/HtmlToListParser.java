package edu.deu.seniorproject.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlToListParser {

    private static final List<String> jobDescDelimiters = Arrays.asList(
            "İŞ TANIMI",
            "İŞ TANITIMI",
            "İŞ GEREKSİNİMLERİ",
            "GENEL İŞ TANIMI",
            "İŞ ÖZELLİKLERİ",
            "İŞ AÇIKLAMASI");

    private static final List<String> employeeDescDelimiters = Arrays.asList(
            "GENEL TANITIM",
            "GENEL TANIM",
            "GENEL ÖZELLİKLER",
            "GENEL NİTELİKLER",
            "GENEL YETENEKLER",
            "İSTENEN YETENEK VE UZMANLIKLAR");


    private int failCount;

    public HtmlToListParser() {
        failCount = 0;
    }

    public List<String> parse(String text) {
        String jobDescDelimiter = "";
        String employeeDescDelimiter = "";
        String jobText = "";
        String employeeText = "";

        boolean hasDoubleText = true;

        for (String delimiter : jobDescDelimiters) {
            if (text.toUpperCase().contains(delimiter)) {
                jobDescDelimiter = delimiter;
                break;
            }
        }

        if (!jobDescDelimiter.isEmpty()) {
            //Regex for splitting  case insensitive
            Pattern pattern = Pattern.compile(jobDescDelimiter, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            String[] temp = pattern.split(text);
            if (!temp[0].trim().isEmpty()) {
                jobText = temp[0];
                employeeText = temp[1];
            } else {
                // Job description comes after wanted qualifications
                // Splitting must be done in terms of qualifications, not job description
                for (String delimiter : employeeDescDelimiters) {
                    if (text.toUpperCase().contains(delimiter)) {
                        employeeDescDelimiter = delimiter;
                        break;
                    }
                }
                if (!employeeDescDelimiter.isEmpty()) {
                    pattern = Pattern.compile(employeeDescDelimiter, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                    String[] empTemp = pattern.split(text);
                    if (!empTemp[0].trim().isEmpty()) {
                        jobText = empTemp[1];
                        employeeText = empTemp[0];
                    } else {
                        // This case is extremely rare for the give job ads, and should not happen that much
                        // This case is ignored and contents of it isn't parsed.
                        hasDoubleText = false;
                        failCount++;
                    }
                } else {
                    hasDoubleText = false;
                }
            }
        } else {
            hasDoubleText = false;
        }

        List<String> result;
        if (!hasDoubleText) {
            // Parse the whole text
            result = textParse(text);
        } else {
            // Only parse the job information / description
            result = textParse(jobText);
        }
        return result;
    }

    private List<String> textParse(String text) {

        List<String> items = new ArrayList<>();
        boolean containsLists = text.contains("<li>");
        boolean containsBreakLines = text.contains("<br>");

        if (containsLists) {
            loadListTags(items, text);
        }

        if (containsBreakLines) {
            Pattern pattern = Pattern.compile("<br>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            String[] words = pattern.split(text);
            try {
                for (String word : words) {
                    // Double check for HTML listings
                    if (!word.contains("<li>") && !word.contains("</li>")) {
                        // This regex replaces all tags with an empty string
                        word = changeHTMLChars(word);
                        word = word.replaceAll("<.*?>", "");
                        // Don't include any text that is longer than 170 characters AND is ALL upper case
                        if (word.length() <= 170 && !word.toUpperCase().equals(word)) {
                            items.add(word);
                        }
                    }
                }

            } catch (Exception ignore) { /* Ignore this exception */ }
        }
        return items;
    }


    private void loadListTags(List<String> list, String text) {
        Document doc = Jsoup.parse(text);
        // This gets all list elements
        Elements elements = doc.body().select("li");
        for (Element element : elements) {
            String innerText = changeHTMLChars(element.text());
            if (innerText.length() < 240 && !innerText.toUpperCase().equals(innerText)) {
                list.add(innerText);
            }
        }
    }

    private String changeHTMLChars(String text) {
        text = text.replace("&nbsp;", " ").replace("&lt;", "<").
                replace("&gt;", ">").replace("·", "");
        return text.trim();
    }

}
