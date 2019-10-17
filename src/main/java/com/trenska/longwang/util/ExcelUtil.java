package com.trenska.longwang.util;

import com.trenska.longwang.model.report.CustomerInfoModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;

public class ExcelUtil {
	/**
	 * 导出Excel通用模板
	 *
	 * @param sheetName sheet名称
	 * @param title     标题 key-value 对 key是实体类的属性名称 ；value是对应的中文字段 ,标题和字段的值填充的时候都要用上key
	 * @param contents  内容 : 从数据库获取到的数据
	 * @param wb        HSSFWorkbook对象 直接返回给前端封装好的数据
	 */

	public static <T> HSSFWorkbook getHSSFWorkbook(
			String sheetName, boolean headable, Map<String, String> summarizing, Map<String, Object> query, Map<String, String> title, List<T> contents, HSSFWorkbook wb)
			throws NoSuchFieldException, IllegalAccessException {

		final short titleFontSize = 16;
		final short fontSize = 11;
		final String font = "宋体";
		final short color = 0;
		final float heightMulti  = 1.5F;
		// 如果内容为null,返回空HSSFWorkbook对象
		// 如果数据长度是0，返回空HSSFWorkbook对象
		if (CollectionUtils.isEmpty(contents)) {
			return new HSSFWorkbook();
		}

		// 第一步，创建一个HSSFWorkbook，对应一个Excel文件
		if (wb == null) {
			wb = new HSSFWorkbook();
		}

		// 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet(sheetName);

		// 四种单元格样式
		// 1.表格头
		CellStyle headerCellStyle = cellStyle(wb, font, BorderStyle.NONE, titleFontSize, color, false, false, false, HorizontalAlignment.CENTER);
		// 2.标题
		CellStyle columnTitleCellStyle = cellStyle(wb, font, BorderStyle.THIN, fontSize, color, false, false, false, HorizontalAlignment.CENTER);
		// 3.查询
		CellStyle queryCellStyle = cellStyle(wb, font, BorderStyle.NONE, fontSize, color, false, false, false, HorizontalAlignment.LEFT);
		// 4.统计
		CellStyle summarizingCellStyle = cellStyle(wb, font, BorderStyle.NONE, fontSize, color, false, false, false, HorizontalAlignment.LEFT);
		// 5.内容
		CellStyle contentCellStyle = cellStyle(wb, font, BorderStyle.THIN, fontSize, color, false, false, false, HorizontalAlignment.CENTER);

		// 声明行对象
		HSSFRow row = null;

		// 声明列对象
		HSSFCell cell = null;

		int rowIndex = 0;

		int cellIndex = 0;
		/***************************************************处理表头***************************************************/
		if (headable) { //如果允许显示表头
			// 添加表头，和sheet名称相同
			row = sheet.createRow(rowIndex);
			float height = row.getHeightInPoints() * heightMulti;
			row.setHeightInPoints(height);
			cell = row.createCell(cellIndex);
			// 设置表头样式
			cell.setCellStyle(headerCellStyle);
			cell.setCellValue(sheetName);

			//作用是能够在合并列后保存边框
			for (cellIndex = 1; cellIndex < title.keySet().size(); cellIndex++) {
				cell = row.createCell(cellIndex);
				cell.setCellStyle(queryCellStyle);
			}

			//将表头合并为一行,表头标题行所占列数为内容列标题的列数之和
			CellRangeAddress titleCellRangeType = new CellRangeAddress(0, 0, 0, title.keySet().size() - 1);
			sheet.addMergedRegion(titleCellRangeType);
		}

		/**********************************************处理统计数据**********************************************/
		// 可以使用MapUtils.isNotEmpty(summarizing)来判断 ，但是是将null和size一起判断，不方便调试
		if (!Objects.isNull(summarizing)) { // 如果不为null
			if (!summarizing.isEmpty()) { // 并且不为空
				rowIndex = getValidFillingRowNum(sheet);
				cellIndex = 0;
				row = sheet.createRow(rowIndex);
				float height = row.getHeightInPoints() * heightMulti;
				row.setHeightInPoints(height);
				Set<String> keys = summarizing.keySet();
				String summaringStr = "";
				for (String key : keys) {
					summaringStr = summaringStr.concat(key).concat(" : ").concat(String.valueOf((String)summarizing.get(key))).concat("; ");
				}
				summaringStr = summaringStr.substring(0, summaringStr.lastIndexOf("; "));// 清除最后一个";"符号
				cell = row.createCell(cellIndex);
				// 填充查询条件
				cell.setCellValue(summaringStr);
				// 设置查询单元格样式
				// 同理 : 作用是能够在合并列后保存边框
				for (cellIndex = 1; cellIndex < title.keySet().size(); cellIndex++) {
					cell = row.createCell(cellIndex);
					cell.setCellStyle(summarizingCellStyle);
				}
				//合并查询行
				CellRangeAddress queryCellRangeType = new CellRangeAddress(rowIndex, rowIndex, 0, title.keySet().size() - 1);

				sheet.addMergedRegion(queryCellRangeType);
			}
		}

		/**********************************************处理查询条件**********************************************/
		if (MapUtils.isNotEmpty(query)) {
			rowIndex = getValidFillingRowNum(sheet);
			row = sheet.createRow(rowIndex);
			float height = row.getHeightInPoints() * heightMulti;
			row.setHeightInPoints(height);
			Set<String> keys = query.keySet();
			String queries = "";
			for (String key : keys) {
				queries = queries.concat(key).concat(" : ").concat(String.valueOf(query.get(key))).concat("; ");
			}
			queries = queries.substring(0, queries.lastIndexOf("; "));// 清除最后一个";"符号

			cell = row.createCell(0);
			// 填充查询条件
			cell.setCellValue(queries);
			// 设置查询单元格样式
			// 同理 : 作用是能够在合并列后保存边框
			for (cellIndex = 1; cellIndex < title.keySet().size(); cellIndex++) {
				cell = row.createCell(cellIndex);
				cell.setCellStyle(queryCellStyle);
			}
			//合并查询调降行
			CellRangeAddress queryCellRangeType = new CellRangeAddress(rowIndex, rowIndex, 0, title.keySet().size() - 1);
			sheet.addMergedRegion(queryCellRangeType);
		}

		/****************************************处理显示的列标题****************************************/

		rowIndex = getValidFillingRowNum(sheet);
		// 定义列宽度数组，数字长度是标题的个数，数组保存内容是标题字符个数
		int[] titleWidth = new int[title.entrySet().size()];

		// 创建列标题行
		row = sheet.createRow(rowIndex);
		if("商品销售明细".equals(sheetName)){
			row.setHeightInPoints(row.getHeightInPoints() * heightMulti * 2F);
		}else{
			row.setHeightInPoints(row.getHeightInPoints() * heightMulti);
		}
		Set<Map.Entry<String, String>> titleEntries = title.entrySet();
		Iterator<Map.Entry<String, String>> titleIterator = titleEntries.iterator();
		/********************************************** 设值列标题内容***********************************************/
		cellIndex = 0;//置列标题下标为0
		while (titleIterator.hasNext()) {
			cell = row.createCell(cellIndex);
			String chinesePartOfTitle = titleIterator.next().getValue();// 列标题中文部分
			// 获取每个标题单元格的字符个数
			titleWidth[cellIndex] = chinesePartOfTitle.length();
			cell.setCellValue(chinesePartOfTitle);
			// 设置标题单元格样式
			cell.setCellStyle(columnTitleCellStyle);
			cellIndex++;
		}
		/**********************************************处理要显示的内容**********************************************/
		rowIndex = getValidFillingRowNum(sheet);// 确保0行时是空行(没有内容)

		int size = contents.size(); // 记录数量

		for (int index = 0; index < size; index++) {
			row = sheet.createRow(rowIndex++);
			float height = row.getHeightInPoints() * heightMulti;
			row.setHeightInPoints(height);
			T value = contents.get(index);// 获取行数据
			titleIterator = titleEntries.iterator();
			// 处理每一行的每一列
			cellIndex = 0;
			while (titleIterator.hasNext()) {
				Map.Entry<String, String> next = titleIterator.next();
				String fieldName = next.getKey();
				Class valueClass = value.getClass();
				Field field = valueClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				if (field.get(value) instanceof Set) {
					Set<String> fieldValues = (Set) field.get(value);
					String str = "";
					for (String fieldValue : fieldValues) {
						str = str.concat(fieldValue).concat("、");
					}
					if (StringUtils.isNotEmpty(str)) {
						str = str.substring(0, str.lastIndexOf("、"));
					}
					cell = row.createCell(cellIndex);
					cell.setCellValue(str);

				} else if (field.get(value) instanceof String) {
					String fieldValue = (String) field.get(value);
					cell = row.createCell(cellIndex);
					cell.setCellValue(fieldValue);
				}
				cellIndex++;
				// 设置内容单元格的样式
				cell.setCellStyle(contentCellStyle);
			}
		}

		// 设置列宽
		for (int i = 0; i < titleWidth.length; i++) {
			sheet.autoSizeColumn(i);
//			sheet.setColumnWidth(i,sheet.getColumnWidth(i) * 13 / 10);
			sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
		}
		return wb;
	}

	private static CellStyle cellStyle(
			HSSFWorkbook wb, String fontName, BorderStyle style , short size, short color, boolean bold, boolean italic, boolean strikeout, HorizontalAlignment horizontalAlignment
	) {
		if (null == wb) {
			wb = new HSSFWorkbook();
		}
		Font font = font(wb, fontName, size, color, bold, italic, strikeout);
		//设置标题单元格类型->通过HSSFWorkbook对象获取CellStyle对象
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFont(font);//设置字体
		cellStyle.setWrapText(true);//设置自动换行
//		cellStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());//设置前景颜色
//		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);//前景样式
		cellStyle.setAlignment(horizontalAlignment); //水平布局：居中
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中

		cellStyle.setBorderTop(style);//上边框
		cellStyle.setBorderLeft(style);//左边框
		cellStyle.setBorderRight(style);//右边框
		cellStyle.setBorderBottom(style); //下边框
		cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		return cellStyle;
	}

	private static Font font(HSSFWorkbook wb, String fontName, short size, short color, boolean bold, boolean italic, boolean strikeout) {
		if (wb == null) {
			wb = new HSSFWorkbook();
		}
		//设置标题字体->通过HSSFWorkbook对象获取Font对象
		Font font = wb.createFont();
		font.setBold(bold); //粗体显示
		font.setColor(color); //颜色
		font.setItalic(italic); //是否使用斜体
		font.setFontName(fontName); //字体
		font.setStrikeout(strikeout); //是否使用划线
		font.setFontHeightInPoints(size); //字体大小
		return font;
	}

	/**
	 * 处理获取的行号为0的情况，如果为0->确保0行没有内容
	 *
	 * @param sheet
	 * @return
	 */
	private static int getValidFillingRowNum(HSSFSheet sheet) {
		if (sheet.getLastRowNum() == 0) {
			return sheet.getPhysicalNumberOfRows();
		} else {
			return sheet.getLastRowNum() + 1;
		}
	}

	public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Class<CustomerInfoModel> customerInfoModelClass = CustomerInfoModel.class;

		CustomerInfoModel customerInfoModel = customerInfoModelClass.newInstance();
		Field custNoField = customerInfoModelClass.getDeclaredField("custNo");
		custNoField.setAccessible(true);
		custNoField.set(customerInfoModel, "0001");
		System.out.println(customerInfoModel);
	}
}