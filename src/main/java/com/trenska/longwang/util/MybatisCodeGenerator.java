package com.trenska.longwang.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * mybatis-plus 自动生成代码
 * 自动生成代码时需要依赖于模板
 */
public class MybatisCodeGenerator {
	public static void main(String[] args) throws InterruptedException {
		//自动生成器
		AutoGenerator autoGenerator = new AutoGenerator();

		// 全局配置
		GlobalConfig globalConfig = new GlobalConfig();
		/*
		 * 全局唯一标识符（GUID，Globally Unique Identifier）是一种由算法生成的二进制长度为128位的数字标识符。
		 * GUID主要用于在拥有多个节点、多台计算机的网络或系统中。在理想情况下，任何计算机和计算机集群都不会生成两个相同的GUID。
		 */
		globalConfig.setIdType(IdType.AUTO);//idWorker的IdType.ID_WORKER_STR 的字符串表示->分布式高效ID生产黑科技(sequence)只有当插入对象ID 为空，才自动填充
		globalConfig.setOutputDir("E://");// 默认是D盘
		globalConfig.setFileOverride(true); // 是否覆盖原有文件
		globalConfig.setActiveRecord(true); // 开启 ActiveRecord 模式
		globalConfig.setEnableCache(true);// 是否在xml中添加二级缓存配置
		globalConfig.setBaseResultMap(true);// 开启XML BaseResultMap
		globalConfig.setBaseColumnList(true);// XML columList
		globalConfig.setAuthor("Owen");

		// 自定义文件命名，注意 %s 会自动填充表实体属性！
		globalConfig.setMapperName("%sMapper");
		globalConfig.setXmlName("%sMapper");
		globalConfig.setServiceName("I%sService");
		globalConfig.setServiceImplName("%sServiceImpl");
		globalConfig.setControllerName("%sController");
		autoGenerator.setGlobalConfig(globalConfig);

		// 数据源配置
		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		dataSourceConfig.setDbType(DbType.MYSQL);
		/*
		 * dsc.setTypeConvert(typeConvert)(new MySqlTypeConvert(){ //
		 * 自定义数据库表字段类型转换【可选】
		 * 
		 * @Override public DbColumnType processTypeConvert(String fieldType) {
		 * System.out.println("转换类型：" + fieldType); return
		 * super.processTypeConvert(fieldType); } });
		 */

		dataSourceConfig.setDriverName("com.mysql.cj.jdbc.Driver");
		dataSourceConfig.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&userSSL=false&tinyIntlisBit=true&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL");
		dataSourceConfig.setUsername("root");
		dataSourceConfig.setPassword("root");
		autoGenerator.setDataSource(dataSourceConfig);

		// 策略配置
		StrategyConfig strategy = new StrategyConfig();
		strategy.setCapitalMode(true);// 全局大写命名 ORACLE 注意
		strategy.setTablePrefix(new String[] { "t_"});// 此处可以修改为你的表前缀
		strategy.setNaming(NamingStrategy.underline_to_camel);// 表名生成策略
		// strategy.setInclude(new String[] { "sys_user" }); // 需要生成的表
//		strategy.setExclude(new String[] { "sys_user" }); // 需要排除的表名，允许正则表达式
		autoGenerator.setStrategy(strategy);

		// 包配置
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.setParent("com.trenska");//配置父包名,如果为空，将下面子包名必须写全部， 否则就只需写子包名
		packageConfig.setModuleName("longwang");//配置父包模块名
		packageConfig.setMapper("dao");
		autoGenerator.setPackageInfo(packageConfig);
		// 执行生成
		autoGenerator.execute();
	}
}