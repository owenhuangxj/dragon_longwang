package com.trenska.longwang.model.prints;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class WebPrintModel implements Serializable {

	@ApiModelProperty(value = "纸张宽度，使用时需要*100")
	private String weight = "";
	
	@ApiModelProperty(value = "纸张高度，使用时需要*100")
	private String height = "";
	
	@ApiModelProperty(value = "打印类型 1:按单打印 2:连续打印 4:表头固定",required=true)
	private int type =1;

	@ApiModelProperty(value = "打印的HTML内容",required=true)
	private String html;

	
}
