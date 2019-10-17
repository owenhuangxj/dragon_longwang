package com.trenska.longwang.entity;

import com.baomidou.mybatisplus.plugins.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@Data
@ApiModel
public class PageHelper<T> {
	@NotNull
	@ApiModelProperty(value = "每页显示多少条记录数目", required = true)
	private Integer size = 10;
	@NotNull
	@ApiModelProperty(value = "当前页", required = true)
	private Integer current = 1;
	@ApiModelProperty("记录总条数")
	private Integer total;
	@ApiModelProperty("页数")
	private Integer pages;
	@ApiModelProperty("分页返回的数据")
	private List<T> records;

	private Object summarizing;

	private PageHelper() {
	}
	public PageHelper(Integer current,Integer size){
		this.current = current;
		this.size = size;
	}

	public static PageHelper getInstance() {
		return new PageHelper();
	}

	private PageHelper setReturnCurrent(Integer current) {
		this.current = current;
		return this;
	}

	private PageHelper setReturnSize(Integer size) {
		this.size = size;
		return this;
	}

	public PageHelper setTotal(Integer total) {
		this.total = total;
		return this;
	}

	public PageHelper setPages(Integer pages) {
		this.pages = pages;
		return this;
	}

	public PageHelper setRecords(List<T> records) {
		this.records = records;
		return this;
	}

	public PageHelper pageData(Page pageInfo) {
		this.setTotal(pageInfo.getTotal())
			.setReturnCurrent(pageInfo.getCurrent())
			.setPages(pageInfo.getPages())
			.setReturnSize(pageInfo.getSize())
			.setRecords(pageInfo.getRecords());
		return this;
	}

	public PageHelper summarizing(Object summarizing){
		this.summarizing = summarizing;
		return this;
	}

}
