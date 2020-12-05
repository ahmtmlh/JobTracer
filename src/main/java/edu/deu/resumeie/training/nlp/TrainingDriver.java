package edu.deu.resumeie.training.nlp;

import edu.deu.resumeie.training.datareader.DataReader;
import edu.deu.resumeie.training.datareader.JobDataReader;
import edu.deu.resumeie.training.nlp.informationextraction.InformationExtractor;
import edu.deu.resumeie.training.nlp.informationextraction.ProcessException;
import edu.deu.resumeie.training.parser.JobInfoHtmlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TrainingDriver {

    private static final String DONE = "\tDone";

    public void start() {
        startV2();
    }

    private void startV2(){
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

    private List<InformationExtractor.ListItem> htmlParse(int n) throws IOException {
        File datasetFile = new File("./data/dataset.xlsx");
        DataReader excelReader = new JobDataReader(datasetFile);
        excelReader.readRows(n);
        JobInfoHtmlParser parser = new JobInfoHtmlParser();
        List<InformationExtractor.ListItem> items = new ArrayList<>();
        for (InformationExtractor.ListItem item : excelReader.exportAsList()){
            List<String> words = parser.parse(item.getTexts().get(0));
            InformationExtractor.ListItem newItem = new InformationExtractor.ListItem(item.getId(), item.getExp(), item.getMaxExp(), item.getJobInfo(),
                    item.getCities(), item.getEducationStatus());
            words.forEach(newItem::addText);
            items.add(newItem);
        }
        excelReader.close();
        return items;
    }

    // This function requires python to be installed in the system, and specified in PATH
    private void clustering() throws InterruptedException, IOException, ProcessException {
        Process p = Runtime.getRuntime().exec("python cluster/training/doc-cluster.py");
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
