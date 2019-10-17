package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**=
 * @author Owen
 * @since 2019-05-27
 */
@Data
@NoArgsConstructor
@TableName("t_emp_area_grp")
@ApiModel("员工账号-区域分组实体类")
public class EmpAreaGrp extends Model<EmpAreaGrp> {

    private Integer empId;

    private Integer areaGrpId;

    public EmpAreaGrp(Integer empId, Integer areaGrpId) {
        this.empId = empId;
        this.areaGrpId = areaGrpId;
    }
}
