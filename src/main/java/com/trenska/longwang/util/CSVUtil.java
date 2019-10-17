//package com.trenska.longwang.util;
//
//import java.io.BufferedWriter;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import com.monitorjbl.xlsx.StreamingReader;
//
//
//
//public class CSVUtil {
//	public static <T> List<T> transferExcelToList(String path,Class<T> clz){
//		return null;
//	}
//	/**
//	 * 转化.xls文件为集合的方法
//	 * @param path .xls文件的路径
//	 * @param clz 要转化的List的泛型
//	 * @return 转化后的集合
//	 */
//	public static<T> List<T> transferXlsToList(String path,Class<T> clz){
//		return null;
//	}
//	public static<T> List<T> transferXlsxToList(String path,Class<T> clz){
//		return null;
//	}
//
//	public static void readXlsx(String path){
//		XSSFWorkbook xssf = null;
//		try{
//			xssf = new XSSFWorkbook(new FileInputStream(path));
//			XSSFSheet sheet = xssf.getSheetAt(0);
//			//处理行
//			for(int i = 0 ; i < sheet.getLastRowNum();i++){
////				Row row = sheet.getRow(i);
//				XSSFRow row = sheet.getRow(i);
//				for(int j = 0; j < row.getLastCellNum() ; j++){
//					Cell cell = row.getCell(j);
//					if(cell.getCellTypeEnum() == CellType.STRING)
//						System.out.println("cell "+ (j+1) + " : "+ cell.getStringCellValue());
//					if(cell.getCellTypeEnum() == CellType.NUMERIC)
//						System.out.println("cell " + (j+1) + " : " + cell.getNumericCellValue());
//				}
//			}
//
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally{
//			try {
//				if(null != xssf)	xssf.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	public static void readXls(String path){
//		HSSFWorkbook hssf = null;
//		try {
//			hssf = new HSSFWorkbook(new FileInputStream(path));
//			System.out.println("sheetNumber : "+hssf.getNumberOfSheets());
//			HSSFSheet sheet = hssf.getSheetAt(0);
//			for(int i = 0 ; i < sheet.getLastRowNum() ; i ++){
//				HSSFRow row = sheet.getRow(i);
//				for(int j = 0 ; j < row.getLastCellNum() ; j++){
//					HSSFCell cell = row.getCell(j);
//					if(cell.getCellTypeEnum() == CellType.STRING) System.out.println("cell " + (j+1) + " : " + cell.getStringCellValue());
//					if(cell.getCellTypeEnum() == CellType.NUMERIC) System.out.println("cell " + (j+1) + " : " + cell.getNumericCellValue());
//				}
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	/**
//	 *
//	 * @param src
//	 * @param des
//	 */
//	public static void xlsxToCsv(String src,String des){
//		Workbook workbook = null;
//		BufferedWriter bw = null;
//		try {
//			workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(new FileInputStream(src));
//			try {
//				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(des), "UTF-8"), 4096);
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//			Sheet sheet = workbook.getSheetAt(0);
//			for(Row row : sheet){
//				for(Cell cell : row){
//					if(cell.getCellTypeEnum() == CellType.STRING)	bw.write(cell.getStringCellValue());
//					else if(cell.getCellTypeEnum() == CellType.NUMERIC)	bw.write(cell.getNumericCellValue()+"");
//					else if(cell.getCellTypeEnum() == CellType.BOOLEAN)	bw.write(cell.getBooleanCellValue()+"");
//					bw.write(',');
//				}
//				bw.newLine();
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally{
//			try {
//				if(null != workbook)	workbook.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}finally {
//				try {
//					if(null != bw)	bw.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	public static void main(String[] args) {
////		readXlsx("D://book.xlsx");
////		readXls("d://book.xls");
//		xlsxToCsv("e://tmp.xlsx", "e://tmp.csv");
//	}
//}