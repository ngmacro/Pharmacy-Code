
package com.satech.pharmacy.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author erdem perendi
 */
public class LogToExcel {

    private static final String PATH = "d:\\RecordList.xlsx";

    public static void createFile() throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List of Records");
        Row heading = sheet.createRow(0);
        heading.createCell(0).setCellValue("DATE");
        heading.createCell(1).setCellValue("TIME");
        heading.createCell(2).setCellValue("BOX ID");
        heading.createCell(3).setCellValue("STATION1");
        heading.createCell(4).setCellValue("STATION2");
        heading.createCell(5).setCellValue("STATION3");
        heading.createCell(6).setCellValue("STATION4");
        heading.createCell(7).setCellValue("URGENT");
        

        for (int i = 0; i < 8; i++) {
            XSSFCellStyle stylerowHeading = workbook.createCellStyle();
            Font font = workbook.createFont();
            //font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setFontHeightInPoints((short) 12);
            stylerowHeading.setFont(font);
            heading.getCell(i).setCellStyle(stylerowHeading);
        }
        //save Excel File
        FileOutputStream out = new FileOutputStream(new File(PATH));
        workbook.write(out);

    }

    public static void appendRecords(String boxId, String rotationId, String currDay, String currTime) throws Exception {

        File file = new File(PATH);

        if (!file.exists()) {
            createFile();
        }

        InputStream ExcelFiletoRead = new FileInputStream(PATH);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFiletoRead);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow row = sheet.createRow((short) (sheet.getLastRowNum() + 1));

        char station1 = rotationId.charAt(0);
        char station2 = rotationId.charAt(1);
        char station3 = rotationId.charAt(2);
        char station4 = rotationId.charAt(3);
        char urgent = rotationId.charAt(4);
        
        row.createCell(0).setCellValue(currDay);
        row.createCell(1).setCellValue(currTime);
        row.createCell(2).setCellValue(boxId);
        row.createCell(3).setCellValue(station1 - 48);
        row.createCell(4).setCellValue(station2 - 48);
        row.createCell(5).setCellValue(station3 - 48);
        row.createCell(6).setCellValue(station4 - 48);
        row.createCell(7).setCellValue(urgent - 48);
        

        //AutoFit
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
        //save Excel File
        FileOutputStream out = new FileOutputStream(new File(PATH));
        workbook.write(out);

    }
}
