package edu.deu.seniorproject.datareader;

import edu.deu.seniorproject.nlp.informationextraction.InformationExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class DataReader {

    private final Sheet sheet;
    private final Workbook workbook;
    private final List<InformationExtractor.ListItem> list;
    private boolean closed;

    private static final Map<String, Integer> edStatusValues = new HashMap<>();
    static{
        edStatusValues.put("ilköğretim mezunu", 1);
        edStatusValues.put("lise öğrencisi", 2);
        edStatusValues.put("lise mezunu", 3);
        edStatusValues.put("meslek yüksekokulu öğrencisi", 4);
        edStatusValues.put("meslek yüksekokulu mezunu", 5);
        edStatusValues.put("üniversite öğrencisi", 6);
        edStatusValues.put("üniversite mezunu", 7);
        edStatusValues.put("master öğrencisi", 8);
        edStatusValues.put("master mezunu", 9);
        edStatusValues.put("doktora öğrencisi", 10);
        edStatusValues.put("doktora mezunu", 11);

    }

    public DataReader(File file) throws IOException{
        FileInputStream fileInputStream = new FileInputStream(file);
        workbook = new XSSFWorkbook(fileInputStream);
        sheet = workbook.getSheetAt(0);
        list = new ArrayList<>();
        closed = false;
    }

    /**
     * Read 'n' rows from the specified spreadsheet file
     * @param n Number of rows to read
     */
    public void readNRows(int n){
        if(closed){
            throw new RuntimeException("Data reader is closed");
        }

        if (n <= 0 || n >= sheet.getPhysicalNumberOfRows()){
            n = sheet.getPhysicalNumberOfRows()-1;
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        // First row is header
        rowIterator.next();
        for(int i = 0; i < n; i++){
            Row nextRow = rowIterator.next();
            String text = nextRow.getCell(7).getStringCellValue();
            String id = String.valueOf((int)nextRow.getCell(2).getNumericCellValue());
            int exp = (int) nextRow.getCell(3).getNumericCellValue();
            int maxExp = (int) nextRow.getCell(4).getNumericCellValue();
            String jobInfo = String.valueOf(nextRow.getCell(6).getStringCellValue());
            String cities = nextRow.getCell(5).getStringCellValue().replace(',', '|');
            int educationStatus = getEdStatusNumericalValue(nextRow.getCell(8).getStringCellValue());


            InformationExtractor.ListItem item = new InformationExtractor.ListItem(id, exp, maxExp, jobInfo, cities, educationStatus, text);
            list.add(item);
        }
        System.out.println("List Size: " + list.size());
    }

    public void close() throws IOException {
        workbook.close();
        closed = true;
    }

    private int getEdStatusNumericalValue(String edStatus){
        if (edStatus.indexOf(',') == -1){
            return edStatusValues.get(edStatus.toLowerCase());
        }
        String minEducationStatus = edStatus.substring(0, edStatus.indexOf(',')).toLowerCase();
        return edStatusValues.get(minEducationStatus);
    }

    /**
     * Return the constructed list from the file
     * @return List of info from the file
     */
    public List<InformationExtractor.ListItem> exportAsList(){
        return list;
    }
}
