package edu.deu.resumeie.training.datareader;

import edu.deu.resumeie.training.nlp.informationextraction.InformationExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static edu.deu.resumeie.shared.SharedObjects.educationStatusValues;

public class JobDataReader implements DataReader{

    private final Sheet sheet;
    private final Workbook workbook;
    private final List<InformationExtractor.ListItem> list;
    private boolean closed;

    public JobDataReader(File file) throws IOException{
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
    public void readRows(int n){
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
            String text = nextRow.getCell(7).getStringCellValue().trim();
            String id = String.valueOf((int)nextRow.getCell(2).getNumericCellValue()).trim();
            int exp = (int) nextRow.getCell(3).getNumericCellValue();
            int maxExp = (int) nextRow.getCell(4).getNumericCellValue();
            String jobInfo = String.valueOf(nextRow.getCell(6).getStringCellValue()).trim();
            String cities = nextRow.getCell(5).getStringCellValue().replace(',', '|').trim();
            int educationStatus = getEdStatusNumericalValue(nextRow.getCell(8).getStringCellValue());


            InformationExtractor.ListItem item = new InformationExtractor.ListItem(id, exp, maxExp, jobInfo, cities, educationStatus);
            item.addText(text);
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
            return educationStatusValues.get(edStatus.toLowerCase());
        }
        String minEducationStatus = edStatus.substring(0, edStatus.indexOf(',')).toLowerCase();
        return educationStatusValues.get(minEducationStatus);
    }

    /**
     * Return the constructed list from the file
     * @return List of info from the file
     */
    public List<InformationExtractor.ListItem> exportAsList(){
        return list;
    }
}
