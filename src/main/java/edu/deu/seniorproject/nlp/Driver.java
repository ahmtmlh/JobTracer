package edu.deu.seniorproject.nlp;

import edu.deu.seniorproject.datareader.DataReader;
import edu.deu.seniorproject.nlp.informationextraction.InformationExtractor;
import edu.deu.seniorproject.nlp.informationextraction.ProcessException;
import edu.deu.seniorproject.parser.HtmlToListParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Driver {

    private static final String DONE = "\tDone";

    public void start() throws InterruptedException {
        startV2();
    }

    private void testV1(){
        String str = "<ul><li><strong>Tercihen çok iyi derecede İngilizce&nbsp;konuşabilen ve gerekli yazışmaları yapabilen (ikinci dil tercih sebebidir)</strong></li><li><strong>Dış ticaret konusunda deneyimli veya yetiştirilmek üzere</strong> </li><li><strong>İlgili yurt-dışı&nbsp;firmalarıyla gerekli yazışmaları ve görüşmeleri yapabilen</strong></li><li><strong><span>Mevcut müşteri ilişkilerini yürütebilen</span></strong> </li><li><strong><span>Tercihen Üniversite mezunu</span></strong> </li><li><strong><span>Konya'da ikamet eden</span></strong> </li><li><strong><span>İletişim yeteneği kuvvetli</span></strong> </li><li><strong><span>Bilgisayar kullanabilen, Microsoft Office programlarına hakim</span></strong></li><li><strong><span>Disiplinli, sonuç odaklı, analitik düşünebilen</span></strong> </li><li><strong><span>Seyahat engeli olmayan</span></strong> </li><li><strong><span>Bay / Bayan personel</span></strong> </li></ul><br><br><h3>İŞ TANIMI</h3><p><strong>Ekmek ve unlu mamul&nbsp;üretimi için gerekli olan&nbsp;makine ve ekipmanların&nbsp;önde gelen üreticilerinden biri olan&nbsp;ve 85'den fazla ülkeye ihracat yapan, firmamızın ihracat departmanında&nbsp;görevlendirilmek üzere çalışma arkadaşları alınacaktır.</strong></p><p><strong><span><span>Firmamızda;</span></span></strong></p><ul><li><strong><span><span>Hedef olarak belirlenen ülkelerde, ihracatın artırılması ve satış istikrarının sağlanması için gerekli çalışmaların yapılması</span></span></strong></li><li><strong>Mevcut yurt-dışı&nbsp;satış işlemlerinin takip edilmesi ve düzenli olarak yönetime rapor edilmesi</strong></li><li><strong><span><span>Şirketimizin belirlediği hedefler doğrultusunda yurt dışı pazarlarının araştırılması ve yeni&nbsp; müşterilerin bulunması, gerektiğinde yurt dışı fuarlar ve müşteri ziyaretlerinde bulunması</span></span></strong></li><li><strong><span><span>Dış ticaret stratejilerimizin uygulanması ve geliştirilmesi</span></span></strong></li></ul>";
        List<InformationExtractor.ListItem> list;
        try{
            list = htmlParse(1000);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        InformationExtractor ie = new InformationExtractor(3);
        ie.extractFromList(list);

    }

    private void startV2() throws InterruptedException {
        System.out.println("Html parsing...");
        List<InformationExtractor.ListItem> list;
        try{
            list = htmlParse(-1);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        System.out.println(DONE);
        System.out.println("Pattern Matching...");

        InformationExtractor ie = new InformationExtractor(3);
        ie.extractFromList(list);
        ie.saveToFile();
        System.out.println(DONE);

//        System.out.println("Clustering...");
//        try {
//            clustering();
//            System.out.println(DONE);
//        } catch (ProcessException | IOException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("Deleting Temp files...");
//            try {
//                ie.deleteTempFiles();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println(DONE);
    }

    /*
    private void startV1() throws InterruptedException {
        // Preprocessing part. Uncomment if data needs prepration
        // SET FILENAME IN THE PYTHON SCRIPT FILE
		/*
		System.out.println("Pre processing data...");
		try {
			preprocess();
		} catch (ProcessException | IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(DONE);

        System.out.println("Html Parsing...");
        try {
            htmlParseV1();
        } catch (ProcessException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println(DONE);
        InformationExtractor ie = new InformationExtractor(3);
        System.out.println("Pattern Matching...");
        ie.extractFromFile("parse.txt");
        ie.saveToFile();
        System.out.println(DONE);
        System.out.println("Clustering...");
        try {
            clustering();
            System.out.println(DONE);
        } catch (ProcessException | IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Deleting Temp files...");
            try {
                ie.deleteTempFiles();
                System.out.println(DONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    */

    private void htmlParseV1() throws IOException, InterruptedException, ProcessException {
        // Number of rows to be read
        // n = -1 for all file
        int n = 20000;
        // Source file name
        String fileName = "dataset.xlsx";
        Process p = Runtime.getRuntime().exec(String.format("html-parse/HtmlToListParser.exe %d %s", n, fileName));
        while (p.isAlive()) {
            // Sleep while waiting for the process to exit, instead of lazy looping
            Thread.sleep(250);
        }
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        if (errReader.ready()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Html-Parse has run into some errors. Detailed error message: \n");
            String line = "";
            while ((line = errReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            errReader.close();
            throw new ProcessException(sb.toString());
        }
    }


    private List<String> htmlParseFromString(String str){
        HtmlToListParser parser = new HtmlToListParser();
        return parser.parse(str);
    }

    private List<InformationExtractor.ListItem> htmlParse(int n) throws IOException {
        File datasetFile = new File("dataset.xlsx");
        DataReader excelReader = new DataReader(datasetFile);
        excelReader.readNRows(n);
        HtmlToListParser parser = new HtmlToListParser();
        List<InformationExtractor.ListItem> items = new ArrayList<>();
        for (InformationExtractor.ListItem item : excelReader.exportAsList()){
            List<String> words = parser.parse(item.getText());
            words.forEach(word -> items.add(new InformationExtractor.ListItem(item.getId(), item.getExp(), item.getMaxExp(), item.getJobInfo(),
                    item.getCities(), item.getEducationStatus(), word)));
        }
        excelReader.close();
        return items;
    }

    // This function requires python to be installed in the system, and specified in PATH
    private void clustering() throws InterruptedException, IOException, ProcessException {
        Process p = Runtime.getRuntime().exec("python cluster/doc-cluster.py");
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while (p.isAlive()) {
            // Sleep while waiting for the process to exit, instead of lazy looping
            Thread.sleep(250);
        }
        if (errReader.ready()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Clustering has run into some errors. Detailed error message: \n");
            String line = "";
            while ((line = errReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            errReader.close();
            throw new ProcessException(sb.toString());
        }
    }

    @SuppressWarnings("unused")
    private void preprocess() throws IOException, InterruptedException, ProcessException {
        String command = "python preprocess/preprocess.py";
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while (p.isAlive()) {
            // Sleep while waiting for the process to exit, instead of lazy looping
            Thread.sleep(250);
        }
        if (errReader.ready()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Preprocessing has run into some errors. Detailed error message: \n");
            String line = "";
            while ((line = errReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            errReader.close();
            throw new ProcessException(sb.toString());
        }
    }

}
