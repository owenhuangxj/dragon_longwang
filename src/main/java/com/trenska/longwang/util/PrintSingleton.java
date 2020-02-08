package com.trenska.longwang.util;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.trenska.longwang.model.prints.WebPrintModel;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public enum PrintSingleton {

	INSTNACE;

	private PrintModel instance;

	PrintSingleton() {
		this.instance = new PrintModel();
	}

	public PrintModel getInstance() {
		return this.instance;
	}

	public class PrintModel {

		/**
		 * 生成打印数据返回前端
		 *
		 * @param html   打印的内容 HTML的
		 * @param wight  宽度
		 * @param height 高度
		 * @return
		 */
		public WebPrintModel retOk(String html, String wight, String height) {
			WebPrintModel ret = new WebPrintModel();
			ret.setWeight(wight);
			ret.setHeight(height);
			ret.setType(4);
			ret.setHtml(html);
			return ret;
		}

		public void createPdf(String htmlFilePath, String fontPath, OutputStream out) throws IOException, DocumentException {
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(new File(htmlFilePath).toURI().toURL().toString());

			// 解决中文不显示问题
			ITextFontResolver fontResolver = renderer.getFontResolver();
			fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

			renderer.layout();
			renderer.createPDF(out);
			//删除html文件
			FileUtil.deleteFile(htmlFilePath);
		}
	}
}