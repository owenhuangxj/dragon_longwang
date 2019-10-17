package com.trenska.longwang.model.sys;

import com.baomidou.mybatisplus.plugins.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 2019/4/4
 * 创建人:Owen
 * 封装的相应类，这样设计一方面是方便进行链式调用，另一方面是为了Swagger生成接口文档，
 * 因为像ResultModel类的设计Swagger不能生成文档
 */
@ApiModel
public class ResponseModel<T> {
	@ApiModelProperty("返回的消息")
	private String msg;
	@ApiModelProperty("操作成功与否")
	private Boolean succ;
	@ApiModelProperty("响应数据")
	private Object data;
	@ApiModelProperty("原因")
	private String reason;
	@ApiModelProperty("错误码")
	private int code;

	private int total;
	private int pages;
	private Object summarizing;
	private List<T> records = new ArrayList<>();

	public ResponseModel page(Page page){
		this.total = page.getTotal();
		this.pages = page.getPages();
		this.records = page.getRecords();
		return this;
	}

	/**
	 * 以下所有操作都是为了链式调用
	 */
	private ResponseModel(){}

	public static ResponseModel getInstance(){
		return new ResponseModel();
	}

	public ResponseModel succ(Boolean succ){
		this.succ = succ;
		return this;
	}

	public ResponseModel msg(String msg){
		this.msg = msg;
		return this;
	}

	public ResponseModel reason(String reason){
		this.reason = reason;
		return this;
	}

	public ResponseModel data(Object data){
		this.data = data;
		return this;
	}

	public ResponseModel code(int code){
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Boolean getSucc() {
		return succ;
	}

	public Object getData() {
		return data;
	}

	public String getReason() {
		return reason;
	}

	public int getCode() {
		return code;
	}

	public int getTotal() {
		return total;
	}

	public int getPages() {
		return pages;
	}

	public Object getSummarizing() {
		return summarizing;
	}

	public List<T> getRecords() {
		return records;
	}
}
