package edu.deu.seniorproject.excelreader;

import edu.deu.seniorproject.nlp.informationextraction.InformationExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelReader {

    private final Sheet sheet;
    private final List<InformationExtractor.ListItem> list;

    public ExcelReader(File file) throws IOException{
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        sheet = workbook.getSheetAt(0);
        list = new ArrayList<>();
    }

    /**
     * Read 'n' rows from the specified spreadsheet file
     * @param n Number of rows to read
     */
    public void readNRows(int n){
        if (n <= 0 || n >= sheet.getPhysicalNumberOfRows()){
            n = sheet.getPhysicalNumberOfRows();
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        // First row is header
        rowIterator.next();
        for(int i = 0; i < n; i++){
            Row nextRow = rowIterator.next();
            String text = nextRow.getCell(7).getStringCellValue();
            String id = String.valueOf(nextRow.getCell(2).getNumericCellValue());
            int exp = (int) nextRow.getCell(3).getNumericCellValue();
            int maxExp = (int) nextRow.getCell(4).getNumericCellValue();
            String jobInfo = String.valueOf(nextRow.getCell(6).getStringCellValue());

            InformationExtractor.ListItem item = new InformationExtractor.ListItem(id, exp, maxExp, jobInfo, text);
            list.add(item);
        }
    }

    /**
     * Return the constructed list from the file
     * @return List of info from the file
     */
    public List<InformationExtractor.ListItem> exportAsList(){
        return list;
    }
}
