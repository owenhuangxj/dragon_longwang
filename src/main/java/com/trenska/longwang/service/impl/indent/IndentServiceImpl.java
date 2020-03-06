package com.trenska.longwang.service.impl.indent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.DataAuthVerification;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.dao.goods.ActiveGoodsMapper;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.dao.goods.GoodsPriceGrpMapper;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.dao.stock.GoodsStockMapper;
import com.trenska.longwang.dao.stock.StockDetailMapper;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.goods.*;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.enums.PaymentStat;
import com.trenska.longwang.model.indent.IndentInfoModel;
import com.trenska.longwang.model.indent.IndentNoCustIdNameModel;
import com.trenska.longwang.model.report.*;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.service.goods.IUnitService;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.service.sys.IEmpAreaGrpService;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Owen
 * @since 2019-04-22
 */
@Service
@SuppressWarnings("all")
public class IndentServiceImpl extends ServiceImpl<IndentMapper, Indent> implements IIndentService {

	@Autowired
	private IStockService stockService;

	@Autowired
	private IStockDetailService stockDetailService;

	@Autowired
	private ActiveGoodsMapper activeGoodsMapper;

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@Autowired
	private GoodsPriceGrpMapper goodsPriceGrpMapper;

	@Autowired
	private StockMapper stockMapper;

	@Autowired
	private StockDetailMapper stockDetailMapper;

	@Autowired
	private GoodsMapper goodsMapper;

	@Autowired
	private IndentDetailMapper indentDetailMapper;

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private DealDetailMapper dealDetailMapper;

	@Autowired
	private IReceiptService receiptService;

	@Resource(name = "redisTemplate")
	private RedisTemplate redisTemplate;

	@Resource(name = "redisJsonTemplate")
	private RedisTemplate<String, Object> jsonRedisTemplate;

//	@Resource(name = "redissonClient")
//	private RedissonClient redissonClient;

	@Autowired
	private IGoodsService goodsService;

	@Autowired
	private ICustomerService customerService;

	@Autowired
	private ISysEmpService empService;

	@Autowired
	private IEmpAreaGrpService empAreaGrpService;

	@Autowired
	private IUnitService unitService;

	@Autowired
	private GoodsStockMapper goodsStockMapper;

	@Override
	@Transactional
	public ResponseModel saveIndent(Indent indent) {
		/**
		 * 处理订货单
		 */
		Integer empIdInToken = SysUtil.getEmpId();
		indent.setEmpId(empIdInToken);

		String prefix = Constant.DH_TITLE;
		String indentType = Constant.DHD_CHINESE;
		String indentNo = IndentUtil.getIndentNo(indentType, prefix, super.baseMapper);
		indent.setIndentNo(indentNo);

		// 数据库默认状态就是待审核
		indent.setStat(IndentStat.WAIT_CONFIRM.getName());
		indent.setIndentTime(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));

		indent = IndentUtil.saveIndentInfos(indent);

		this.save(indent);

		return ResponseModel.getInstance().succ(true).msg("新建订货单成功");
	}

	/**
	 * 删除订/退货单
	 *
	 * @param indentId
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel removeIndentById(Long indentId, String indentType) {

		Indent dbIndent = this.getById(indentId);

		if (null == dbIndent) {
			return ResponseModel.getInstance().succ(false).msg("删除成功失败 : 无此".concat(indentType));
		}

		if (IndentStat.CANCELLED.getName().equals(dbIndent.getStat()) || IndentStat.INVALID.getName().equals(dbIndent.getStat())) {
			super.baseMapper.deleteById(indentId);
		} else {
			return ResponseModel.getInstance().succ(false).msg("已作废或已取消的".concat(indentType).concat("才可被删除"));
		}

		String indentNo = dbIndent.getIndentNo();

		/************************************** 删除订/退货单详情  **************************************/

		this.deleteRelativeInfos(indentNo);

		return ResponseModel.getInstance().succ(true).msg("删除".concat(indentType).concat("成功"));

	}

	/**
	 * 批量删除订/退货单
	 *
	 * @param indentIds
	 * @param indentType
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel removeIndentByIds(Collection<Long> indentIds, String indentType) {

		Collection<Indent> dbIndents = this.listByIds(indentIds);
		if (dbIndents.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("请选择要删除的".concat(indentType));
		}
		// 筛选出已作废或者已取消的订货单
		List<Indent> indents = dbIndents.stream().filter(indent -> {
			return IndentStat.CANCELLED.getName().equals(indent.getStat()) || IndentStat.INVALID.getName().equals(indent.getStat());
		}).collect(Collectors.toList());
		// 如果两个集合的size不相等代表包括未完成的订货单，不能批量删除
		if (!(indents.size() == indentIds.size())) {
			return ResponseModel.getInstance().succ(false).msg("您选择了未完成的".concat(indentType).concat("，已作废或已取消的").concat(indentType).concat("才可被删除."));
		}
		// 删除
		this.removeByIds(indentIds);

		List<String> indentNos = dbIndents.stream().map(Indent::getIndentNo).collect(Collectors.toList());

		indentNos.forEach(indentNo -> this.deleteRelativeInfos(indentNo)); // 删除关联记录

		return ResponseModel.getInstance().succ(true).msg("批量删除".concat(indentType).concat("成功"));
	}

	/**
	 * 更新订货单
	 * 主要需要处理订货单详情信息
	 *
	 * @param indent
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel updateIndent(Indent indent) {

		Long indentId = indent.getIndentId();

		Indent dbIndent = this.getById(indentId);

		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg("无效的订货单信息!");
		}

		if (!IndentStat.WAIT_CONFIRM.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("待审核的订货单才可编辑!!");
		}

		indent.setIndentTime(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));

		String indentNo = indent.getIndentNo();

		/************************************ 直接删除订货单详情 ************************************/
		indentDetailMapper.actualDeleteIndentDetail(indentNo);

		/************************************ 处理新的订货单详情 ************************************/
		indent = IndentUtil.saveIndentInfos(indent);

		indent.updateById();

		return ResponseModel.getInstance().succ(true).msg("修改订货单成功");

	}

	/**
	 * 审核订货单
	 * 客户应收欠款增加
	 * 待出库库存增加
	 *
	 * @param indentNo
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel confirmIndent(String indentNo) {

		List<IndentDetail> indentDetails = indentDetailMapper.selectList(
				new LambdaQueryWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo, indentNo)
		);

		List<Integer> goodsIds = indentDetails.stream().map(IndentDetail::getGoodsId).distinct().collect(Collectors.toList());

		List<Goods> goodsList = goodsMapper.selectList(
				new LambdaQueryWrapper<Goods>()
						.in(Goods::getGoodsId, goodsIds)
		);

		List<Goods> shortStockGoods = new ArrayList<>();

		for (IndentDetail indentDetail : indentDetails) {
			Goods gd = goodsList.stream().filter(goods -> goods.getGoodsId().equals(indentDetail.getGoodsId())).findFirst().get();
			int stock = indentDetail.getNum().intValue() * indentDetail.getMulti().intValue();
			if (gd != null && stock > gd.getStock().intValue()) {
				shortStockGoods.add(gd);
			}
		}
		if (shortStockGoods.size() > 0) {
			return ResponseModel.getInstance().succ(false).data(shortStockGoods).msg("订单中有商品库存不足，不能审核！");
		}

		// 1.更新订货单状态为待出库
		Indent indent = new Indent();
		indent.setStat(IndentStat.WAIT_STOCKOUT.getName());
		indent.update(
				new LambdaQueryWrapper<Indent>()
						.eq(Indent::getIndentNo, indentNo)
		);

		// 2.增加客户欠款
		// 2.1 获取订单金额
		//Indent dbIndent = this.getOne(
		//		new LambdaQueryWrapper<Indent>()
		//				.eq(Indent::getIndentNo, indentNo)
		//);

		// 2.2 增加客户欠款 ==>改到了出完库才增加欠款
		//Customer customer = new Customer(custId).selectById();
		//String oldDebt = customer.getDebt();
		//BigDecimal amount = new BigDecimal(indent.getIndentTotal());
		//CustomerUtil.addCustomerDebt(custId, oldDebt, amount);

		// 记录交易明细 ,增加应收款,前缀 Constant.PLUS ==>改到了出完库才记账
		//String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE, indentNo);
		//String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		//String newDebt = new BigDecimal(customer.getDebt()).add(new BigDecimal(indent.getIndentTotal())).toString();
		//DealDetailUtil.saveDealDetail(custId,nameNo,currentTime,Constant.PLUS.concat(amount.toString()),newDebt,Constant.XSSP);

		// 3.待出库库存增加
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return ResponseModel.getInstance().succ(true).msg("订货单审核成功，可以发货了");

	}

	/**
	 * 取消订货单 , 待审核的订货单才能被取消
	 *
	 * @param indentNo 订货单编号
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel cancelIndentByNo(String indentNo) {
		Indent indent = this.getOne(
				new LambdaQueryWrapper<Indent>()
						.eq(Indent::getIndentNo, indentNo)
		);
		if (Objects.isNull(indent)) {
			return ResponseModel.getInstance().succ(false).msg("无此订货单");
		}

		if (!IndentStat.WAIT_CONFIRM.getName().equals(indent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("待审核的订货单才能被取消");
		}

		if (IndentStat.CANCELLED.getName().equals(indent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("订货单已取消，不能撤销");
		}

		Long indentId = indent.getIndentId();
		Indent updatingIndent = new Indent(indentId, IndentStat.CANCELLED.getName(), PaymentStat.CANCELLED.getName());
		updatingIndent.setPayedAmnt("0.00");
		updatingIndent.setReceivedAmnt("0.00");
		this.updateById(updatingIndent);

		return ResponseModel.getInstance().succ(true).msg("订货单取消成功");
	}

	/**
	 * 撤销审核订货单 ->"已作废"、"已取消"状态的订单不能“撤销审核”
	 * 作废出库单
	 * 客户应收欠款减少
	 * 待出库库存减少
	 * 欠条不变
	 * 已部分出库
	 * 1.找出出库部分
	 * 2.更新库存
	 * 2.1 更新商品总库存
	 * 2.2 更新已出库库存为0
	 * 2.3 更新库存表对应批次的库存
	 * 3.作废收/付款单
	 * 3.1 客户欠款+
	 * 3.2 作废收/付款单
	 * 3.3 记录欠款明细
	 *
	 * @param indent 订货单
	 *               indent{
	 *               indentId,
	 *               indentNo,
	 *               custId,
	 *               receivedAmnt,已收金额-> 0
	 *               indentDetails:[
	 *               detailId,
	 *               goodsId,
	 *               stockout,已出库量->0
	 *               ]
	 *               }
	 */
	@Override
	@Transactional
	public ResponseModel repealIndent(long indentId) {

		Indent dbIndent = this.getById(indentId);
		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg("无此订货单信息");
		}

//		if(!IndentStat.WAIT_STOCKOUT.getName().equals(dbIndent.getStat())){
//			return ResponseModel.getInstance().succ(false).msg("待发货和待出库状态的订单才能撤销审核");
//		}

		String stat = dbIndent.getStat();

		if (IndentStat.INVALID.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("不能撤销已作废的订单");
		}
		if (IndentStat.CANCELLED.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("不能撤销已取消的订单");
		}
		if (IndentStat.WAIT_CONFIRM.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("不能撤销待审核的订单");
		}
		if (IndentStat.STOCKOUTED.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("不能撤销已出库的订单");
		}
		if (IndentStat.FINISHED.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("不能撤销已完成的订单");
		}

		Indent updatingIndent = new Indent();
		String iouAmnt = dbIndent.getIouAmnt();
		updatingIndent.setIouStat(false); // 交账状态为未交账
		updatingIndent.setAuditable(false);// 不可财务审核
		updatingIndent.setAuditStat(false);// 财务审核不通过

		String indentNo = dbIndent.getIndentNo();

		updatingIndent.setIndentId(dbIndent.getIndentId());
		updatingIndent.setIouAmnt("0.00");// 欠条重置为0
		updatingIndent.setPayedAmnt("0.00"); // 订货单已付金额重置为0
		updatingIndent.setReceivedAmnt("0.00"); //订货单已收金额重置为0
		updatingIndent.setStat(IndentStat.WAIT_CONFIRM.getName()); // 状态重置为待审核
		updatingIndent.setReceiptStat(PaymentStat.WAIT_RECEIPT.getName()); // 收款状态为待收款

		/****************************************** 1.更新订货单状态 ******************************************/
		this.updateById(updatingIndent);

		/***************************************** 2.同步更新订货单详情表*****************************************/
		IndentDetail updatingIndentDetail = new IndentDetail();
		updatingIndentDetail.setStockout(0); //已出库数量重置为0
		indentDetailMapper.update(updatingIndentDetail,
				new LambdaQueryWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo, indentNo)
		);

		Customer dbCustomer = null;
		BigDecimal amount = new BigDecimal(dbIndent.getIndentTotal());

		Integer custId = dbIndent.getCustId();
		String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE, dbIndent.getIndentNo());
		if (IndentStat.STOCKOUTED.getName().equals(dbIndent.getStat())) {
			// 2.如果为已出库状态，已出库表示已经记账 --> 减少客户应收款
			dbCustomer = new Customer().selectById(custId);
			// 2.1 获取订单金额
			String oldDebt = dbCustomer.getDebt();
			// 2.2 减少应收款，作废收款单
			/**************************************** 更新客户欠款，客户欠款减少************************************/
			CustomerUtil.subtractCustomerDebt(custId, oldDebt, amount);
			// 保存交易明细 减少客户应收款 加前缀 -
			String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
			String debt = new BigDecimal(dbCustomer.getDebt()).subtract(amount).toString();

			String oper = Constant.XSSP_CX;
			DealDetailUtil.saveDealDetail(custId, nameNo, currentTime, amount.negate().toPlainString(), debt, oper, "", "");
		}

		String receivedAmnt = dbIndent.getReceivedAmnt();
		List<Receipt> updatingReceipts = new ArrayList<>();
		boolean receivedSome = new BigDecimal(receivedAmnt).compareTo(new BigDecimal(0)) > 0;
		// 如果已部分收款，则需要处理收款单
		if (receivedSome) {
			// 获取所有关联收款单信息
			List<Receipt> receipts = new Receipt().selectList(
					new LambdaQueryWrapper<Receipt>()
							.eq(Receipt::getBusiNo, indentNo)
							.eq(Receipt::getCustId, custId)
							.eq(Receipt::getStat, true)
							.eq(Receipt::getType, Constant.SKD_CHINESE)
			);
			updatingReceipts.addAll(receipts);
		}

		String payedAmnt = dbIndent.getPayedAmnt();
		boolean payedSome = new BigDecimal(payedAmnt).compareTo(new BigDecimal(0)) > 0;
		if (payedSome) {
			// 获取所有关联付款单信息
			List<Receipt> receipts = new Receipt().selectList(
					new LambdaQueryWrapper<Receipt>()
							.eq(Receipt::getBusiNo, indentNo)
							.eq(Receipt::getCustId, custId)
							.eq(Receipt::getStat, true)
							.eq(Receipt::getType, Constant.FKD_CHINESE)
			);
			updatingReceipts.addAll(receipts);
		}
		if (!updatingReceipts.isEmpty()) {
			dbCustomer = new Customer().selectById(custId);
			ReceiptUtil.cancelReceipts(updatingReceipts, dbCustomer); //作废收/付款单 -> 订货单收/付款时会减少客户欠款，作废收/付款单时会增加客户欠款
		}
		// 3 减少待出库库存
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/*************** 1.作废出库信息 2.还回库存(商品总库存和批次库存) 3.记录作废***************/
		Set<String> stockNos = stockMapper.selectStockNoByBusiNo(indentNo);
		// 如果已经部分出库 --> 作废出库单，还回商品库存和批次库存
		if (!Objects.isNull(stockNos) && !stockNos.isEmpty()) {
			stockNos.forEach(stockNo -> stockDetailService.cancelStockout(stockNo));
		}
		return ResponseModel.getInstance().succ(true).msg("订货单撤销成功");
	}

	/**
	 * @param indent
	 * @return indent{
	 * custId,
	 * empId,
	 * sum,订货单商品订货总量
	 * indentId,
	 * indentNo,
	 * shipmanId,
	 * indentDetails:[
	 * detailId,
	 * goodsId,
	 * multi,
	 * unitId,
	 * stockout,已出库数量
	 * stockoutMadedates:[
	 * madeDate,
	 * stockPrice,
	 * num
	 * ]
	 * ]
	 * }
	 */
	@Override
	@Transactional
	public ResponseModel stockoutIndent(Indent indent) {
		int custId = indent.getCustId();
		Long indentId = indent.getIndentId();
		String indentNo = indent.getIndentNo();
		Indent dbIndent = super.getById(indentId);
		if (dbIndent == null) {
			return ResponseModel.getInstance().succ(false).msg("无效的订单信息，不能出库！");
		}

		synchronized (indentNo) {
			String stat = dbIndent.getStat();
			if (IndentStat.FINISHED.getName().equals(stat) || IndentStat.STOCKOUTED.getName().equals(stat)) {
				return ResponseModel.getInstance().succ(false).msg("订单" + stat + "，不能出库！");
			}
		}
		ResponseModel responseModel = CustomerUtil.checkDebtLimit(indentNo, custId);
		if (!responseModel.getSucc()) {
			return responseModel;
		}
		String stockTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		String stockNo = StockUtil.getStockNo(Constant.CK_TITLE, Constant.CKD_CHINESE, stockMapper);

		Integer empIdInToken = SysUtil.getEmpId();
		if (null == empIdInToken) {
			return ResponseModel.getInstance().succ(false).msg(Constant.ACCESS_TIMEOUT_MSG);
		}
		Stock stockout = new Stock();
		stockout.setStockNo(stockNo);
		stockout.setEmpId(empIdInToken);
		stockout.setStockTime(stockTime);
		stockout.setCustId(indent.getCustId());
		stockout.setBusiNo(indent.getIndentNo());
		stockout.setStockType(Constant.CKD_CHINESE);
		stockout.setOperType(Constant.XSCK_CHINESE);
		stockout.setShipmanId(indent.getShipmanId());

		// 订单量
		int sum = indent.getSum();

		// 总出库量 = 已出库量 + 本次出库量
		int totalStockoutNum = 0;

		// 处理出库详情
		List<IndentDetail> indentDetails = indent.getIndentDetails();

		List<StockDetail> stockoutDetails = new ArrayList<>();
		for (IndentDetail indentDetail : indentDetails) {
			StockDetail stkoutDetail = new StockDetail();
			stkoutDetail.setEmpId(empIdInToken);
			stkoutDetail.setBusiNo(indent.getIndentNo()); // 关联业务单号:订货单号
			stkoutDetail.setMulti(indentDetail.getMulti());
			stkoutDetail.setSalesPrice(indentDetail.getPrice()); // 保存出库时商品的销售单价，方便统计送货金额/出库金额
			stkoutDetail.setStockType(Constant.CKD_CHINESE); // 库存类型 : 出库单
			stkoutDetail.setOperType(Constant.XSCK_CHINESE); // 操作类型 : 销售出库
			stkoutDetail.setUnitId(indentDetail.getUnitId());
			stkoutDetail.setGoodsId(indentDetail.getGoodsId());
			// 商品出库数量
			int stockoutNum = 0;
			/************************************** 按照批次出库 **************************************/
			List<StockMadedate> stockMadedates = indentDetail.getStockoutMadedates();
			if (CollectionUtils.isNotEmpty(stockMadedates)) {
				stkoutDetail.setStockoutMadedates(stockMadedates);
				for (StockMadedate stockMadedate : stockMadedates) {
					Integer num = stockMadedate.getNum();
					Integer multi = indentDetail.getMulti();
					stockoutNum += num * multi;
				}
			}
			stockoutDetails.add(stkoutDetail);
			//更新订货单出库数量
			// 方式一
			//indentDetailMapper.updateStockoutNum(indentDetail.getDetailId(),stockoutNum);
			//方式二
			Long detailId = indentDetail.getDetailId();
			IndentDetail dbIndentDetail = indentDetailMapper.selectByDetailId(detailId);
			IndentDetail updatingIndentDetail = new IndentDetail();
			updatingIndentDetail.setDetailId(detailId);
			Integer stockouted = dbIndentDetail.getStockout();
			updatingIndentDetail.setStockout(stockouted + stockoutNum);
			indentDetailMapper.updateById(updatingIndentDetail);

		}
		/******************************************** 添加出库详情 ********************************************/
		stockout.setStockouts(stockoutDetails);
		/******************************************** 出库 ********************************************/
		ResponseModel stockoutResult = StockUtil.stockout(stockout, goodsMapper);
		// 如果出库失败返回出库失败原因
		Boolean succ = stockoutResult.getSucc();
		if (!succ) {
			return stockoutResult;
		}
		/**************************判断订货单是否出库完成->比较出库数量和订单数量是否相等***************************/
		/**
		 * 判断订货单是否收款完成->首先看收款状态已经为"已收款"，没有则计算已收款额是否等于订单应该收款额
		 * 是并且为"已出库"状态则将订单状态设置为"待财审"状态
		 * */
		//订单详情->获取总订货数量
		List<IndentDetail> dbIndentDetails = indentDetailMapper.selectList(
				new LambdaQueryWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo, indent.getIndentNo())
		);
		//出库详情->获取总出库数量
		List<StockDetail> dbStockoutDetails = stockDetailMapper.selectList(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getStat, true)
						.eq(StockDetail::getBusiNo, indent.getIndentNo())
						.eq(StockDetail::getStockType, Constant.CKD_CHINESE)
		);
		int indentNum = 0;
		int histroyStockoutNum = 0;

		for (IndentDetail indentDetail : dbIndentDetails) {
			indentNum += indentDetail.getNum() * indentDetail.getMulti();
		}

		for (StockDetail stockDetail : dbStockoutDetails) {
			histroyStockoutNum += stockDetail.getNum() * stockDetail.getMulti();
		}

		// 已出库完成 订单量 == 出库量
		boolean allStockouted = (indentNum == histroyStockoutNum);
		boolean payed = PaymentStat.RECEIPRED.getName().equals(dbIndent.getReceiptStat());
		Boolean audited = dbIndent.getAuditStat();
		// 如果已付款并且已出库->设置状态为"已出库"、"已收款"、auditable=true(不管之前的状态)
		if (allStockouted) { // 如果已出完库
			String dbIndentTotal = dbIndent.getIndentTotal();
			// 1.增加客户欠款 ==>改到了出完库才增加欠款
			Customer dbCustomer = new Customer(custId).selectById();
			String oldDebt = dbCustomer.getDebt();
			BigDecimal amount = new BigDecimal(dbIndentTotal);
			CustomerUtil.addCustomerDebt(custId, oldDebt, amount);

			// 2.记录交易明细 ,增加应收款,前缀 Constant.PLUS ==>改到了出完库才记账
			String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE, indentNo);
			String newDebt = new BigDecimal(dbCustomer.getDebt()).add(amount).toString();
			String auditRemarks = dbIndent.getAuditRemarks();
			Indent updatingIndent = new Indent();
			String dealDetailTime = "";
			// 如果订单销售时间未设置过才设置，否则保留为第一次出库时的时间,明细的记录时间也同步
			if (StringUtils.isEmpty(dbIndent.getSalesTime())) {
				String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
				updatingIndent.setSalesTime(currentTime); // 设置销售时间为订单完成出库时间
				dealDetailTime = currentTime;
			} else {
				dealDetailTime = dbIndent.getSalesTime();
			}
			DealDetailUtil.saveDealDetail(custId, nameNo, dealDetailTime, Constant.PLUS.concat(amount.toString()),
					newDebt, Constant.XSSP, "", "", auditRemarks);
			if (payed) { //如果已经付款
				updatingIndent.setAuditable(true);
				updatingIndent.setIndentId(indentId);
				updatingIndent.setStat(IndentStat.STOCKOUTED.getName());
				updatingIndent.setReceiptStat(PaymentStat.RECEIPRED.getName());
				/********************************** 确保完成状态 **********************************/
				if (!Objects.isNull(audited) && BooleanUtils.isTrue(audited)) {
					updatingIndent.setStat(IndentStat.FINISHED.getName());
				}
				// 如果已出库未付款完成->比较收款金额+付款金额是否等于代收款金额
			} else {
				BigDecimal iouAmnt = new BigDecimal(dbIndent.getIouAmnt());
				BigDecimal payedAmnt = new BigDecimal(dbIndent.getPayedAmnt());
				BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());
				BigDecimal receivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());
				// 再次计算比较收款金额+付款金额+欠条>=应收款 确保"可以财审"状态;收款金额+付款金额==应收款 确保"已收款"状态
				boolean auditable = indentTotal.compareTo(payedAmnt.add(receivedAmnt).add(iouAmnt)) <= 0;
				if (auditable) {
					updatingIndent.setAuditable(true);
					updatingIndent.setIndentId(indentId);
					updatingIndent.setStat(IndentStat.STOCKOUTED.getName());
					payed = indentTotal.compareTo(payedAmnt.add(receivedAmnt)) == 0;
					if (payed) {
						updatingIndent.setReceiptStat(PaymentStat.RECEIPRED.getName());
					}
				} else {
					updatingIndent.setAuditable(false);
					updatingIndent.setIndentId(indentId);
					updatingIndent.setStat(IndentStat.STOCKOUTED.getName());
				}
			}
			updatingIndent.updateById();
//				RLock lock = redissonClient.getLock(updatingIndent.getIndentNo());
//				lock.lock(30, TimeUnit.SECONDS);
//				try{
//				updatingIndent.updateById();
//				}finally {
//					lock.unlock();
//				}
		}
		return ResponseModel.getInstance().succ(true).msg("订货单出库成功！");
	}

	/**
	 * 添加退货单
	 * 商品入库
	 * 添加入库记录
	 * 添加具体入库记录
	 * 商品总库存增加
	 * 客户应收欠款减少
	 *
	 * @param indent
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel saveSalesReturn(Indent indent) {

		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		indent.setIndentTime(currentTime);
		indent.setSalesTime(currentTime);
		String indentNo = IndentUtil.getIndentNo(Constant.THD_CHINESE, Constant.TH_TITLE, super.baseMapper);
		indent.setIndentNo(indentNo);

		Integer custId = indent.getCustId();

		// 龙旺新建退货单直接完成 :
		// 1. 直接退款
		// 2. 直接入库
		indent.setStat(IndentStat.FINISHED.getName());
		// 设置类型为退货
		indent.setIndentType(Constant.THD_CHINESE);
		// 直接为已退款
		indent.setReceiptStat(PaymentStat.PAYBACKED.getName());

		/**
		 * 处理订货单详细信息
		 * 退货单总金额 odrAmnt 退货单金额合计:总金额之和
		 * 数量合计 sum
		 */
		// 商品总数量
		Integer sum = 0;
		// 退货单金额合计
		BigDecimal odrAmount = new BigDecimal(0);

		// 保存入库记录
		Stock stock = new Stock();
		String stockNo = StockUtil.getStockNo(Constant.RK_TITILE, Constant.RKD_CHINESE, stockMapper);
		stock.setStockNo(stockNo);
		stock.setBusiNo(indentNo);
		stock.setStockTime(currentTime);
		stock.setStockType(Constant.RKD_CHINESE);
		stock.setOperType(Constant.THRK_CHINESE);
		stock.setCustId(indent.getCustId());
		stock.setEmpId(indent.getEmpId());
//		stock.setStockRemarks(Constant.THD_CHINESE.concat(indentNo).concat(Constant.THRK_CHINESE));
		stock.insert();

		if (!indent.getIndentDetails().isEmpty()) {
			for (IndentDetail indentDetail : indent.getIndentDetails()) {
				// 单一商品价格总额
				BigDecimal sglGoodsAmount = new BigDecimal(indentDetail.getPrice()).multiply(new BigDecimal(indentDetail.getNum()));
				// 增加商品总数量
				sum += indentDetail.getNum();
				indentDetail.setAmount(sglGoodsAmount.toString());
				indentDetail.setIndentNo(indentNo);
				// 制单人id
				indentDetail.setEmpId(indent.getEmpId());
				// 保存退货单详情
				indentDetail.insert();

				Integer goodsId = indentDetail.getGoodsId();
				// 入库数量
				int stockNum = indentDetail.getNum() * indentDetail.getMulti();
				// 增加商品总库存
				Goods goods = new Goods().selectById(goodsId);
				int goodsNewStock = goods.getStock() + stockNum;
				new Goods(goodsId, goods.getBrandName(), goodsNewStock).updateById();

				//增加商品批次库存
				String madeDate = indentDetail.getMadeDate();
				BigDecimal stockPrice = new BigDecimal(indentDetail.getPrice());

				GoodsStock dbGoodsStock = new GoodsStock().selectOne(
						new LambdaQueryWrapper<GoodsStock>()
								.eq(GoodsStock::getGoodsId, goodsId)
								.eq(GoodsStock::getMadeDate, madeDate)
								.eq(GoodsStock::getStockPrice, stockPrice)
				);

				if (!Objects.isNull(dbGoodsStock)) {
					int goodsBatchStockNewNum = dbGoodsStock.getNum() + stockNum;
					dbGoodsStock.setNum(goodsBatchStockNewNum);
					dbGoodsStock.updateById();
				} else {
					GoodsStock insertingGoodsStock = new GoodsStock();
					insertingGoodsStock.setNum(stockNum);
					insertingGoodsStock.setGoodsId(goodsId);
					insertingGoodsStock.setMadeDate(madeDate);
					insertingGoodsStock.setStockPrice(stockPrice.toString());
					insertingGoodsStock.insert();
				}

				BigDecimal num = new BigDecimal(indentDetail.getNum());
				// 商品入库
				StockDetail stockDetail = new StockDetail();
				stockDetail.setGoodsId(goodsId);
				stockDetail.setStockNo(stockNo);
				stockDetail.setBusiNo(indentNo);
				stockDetail.setHistory(stockNum);
				stockDetail.setNum(num.intValue());
				stockDetail.setStock(goodsNewStock);
				stockDetail.setStockTime(currentTime);
				stockDetail.setMulti(indentDetail.getMulti());
				stockDetail.setStockType(Constant.RKD_CHINESE);
				stockDetail.setOperType(Constant.THRK_CHINESE);
				stockDetail.setUnitId(indentDetail.getUnitId());
				stockDetail.setStockPrice(stockPrice.toString());
				stockDetail.setDetailRemarks(Constant.THRK_CHINESE);
				stockDetail.setMadeDate(madeDate);
				stockDetail.insert(); // 保存入库单详细
				/******************************************保存库存明细******************************************/
				StockDetailsUtil.dbLogStockDetail(stockDetail);
				// 总额 += 商品总价
				odrAmount = odrAmount.add(stockPrice.multiply(num));
			}
		}
		// 处理退货退款付款单
		int empIdInToken = SysUtil.getEmpId();
//		String receiptNo = ReceiptUtil.getReceiptNo(Constant.FK_TITLE, Constant.FKD_CHINESE, receiptMapper);
//		Receipt receipt = new Receipt();
//		receipt.setCustId(custId);
//		receipt.setBusiNo(indentNo);
//		receipt.setEmpId(empIdInToken);
//		receipt.setReceiptNo(receiptNo);
//		receipt.setCreateTime(currentTime);
//		receipt.setReceiptTime(currentTime);
//		receipt.setChargemanId(empIdInToken);
//		receipt.setAccountType(Constant.THTK);
//		receipt.setType(Constant.FKD_CHINESE);
//		receipt.setReceiptAmount(odrAmount.toString());
//		receipt.insert();

		// 客户欠款减少
		Customer customer = customerMapper.selectById(custId);
		String oldDebt = customer.getDebt();
		CustomerUtil.subtractCustomerDebt(custId, oldDebt, odrAmount);

		String nameNo = StringUtil.makeNameNo(Constant.THD_CHINESE, indentNo);
		String newDebt = new BigDecimal(customer.getDebt()).subtract(odrAmount).toString();

		// 保存交易明细 客户应收欠款减少 加前缀 -
		DealDetailUtil.saveDealDetail(custId, nameNo, currentTime, Constant.MINUS.concat(odrAmount.toString()), newDebt, Constant.THTK, "", "");

		indent.setOdrAmnt(odrAmount.toString());
		indent.setIndentTotal(odrAmount.toString()); // 退货单的订单总额和应收款总额相同
		indent.setSum(sum);
		indent.insert();

		return ResponseModel.getInstance().succ(true).msg("创建退货单成功.");
	}

	/**
	 * 作废退货单
	 * 作废关联入库单 -> stat = 0
	 * 商品总库存减少
	 * 增加客户应收欠款
	 *
	 * @param indent
	 * @return
	 */
	@Override
	public ResponseModel invalidSalseReturn(Indent indent) {
		Integer custId = indent.getCustId();
		String indentNo = indent.getIndentNo();
		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);

		if (Objects.isNull(indentNo)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}

		// 获取关联入库单
		Stock stockin = stockMapper.selectOne(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getStat, true)
						.eq(Stock::getBusiNo, indentNo)
		);

		// 获取入库单详情
		List<StockDetail> stockinDetails = stockDetailMapper.selectList(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getBusiNo, indentNo)
						.eq(StockDetail::getStat, true)
						.eq(StockDetail::getStockType, Constant.RKD_CHINESE)
		);

		if (Objects.isNull(stockinDetails) || stockinDetails.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}

		List<Stock> stockins = new ArrayList<>();

		stockins.add(stockin);

		stockin.setStockins(stockinDetails);

		// 作废入库单: 作废入库单、减少总库存、减少批次库存、保存库存明细
		StockUtil.cancelStockin(stockins, Constant.RKDZF_CHINESE);

		// 增加客户应收欠款
		Customer customer = customerMapper.selectById(custId);

		String oldDebt = customer.getDebt();

		// 订货单应收款总额/订单总额(退货单 : 应收款总额==订单总额)
		BigDecimal indentTotal = new BigDecimal(indent.getIndentTotal());

		CustomerUtil.addCustomerDebt(indent.getCustId(), oldDebt, indentTotal);

		String newDebt = new BigDecimal(oldDebt).add(indentTotal).toString();

		// 保存交易明细 客户应收欠款增加 加前缀 +
		String nameNo = StringUtil.makeNameNo(Constant.THD_CHINESE, indent.getIndentNo());

		String oper = Constant.THTK_ZF;

		// 作废退货单 客户欠款+
		DealDetailUtil.saveDealDetail(custId, nameNo, currentTime, Constant.PLUS.concat(indentTotal.toString()), newDebt, oper, "", "");

		// 作废退货单
		new Indent(indent.getIndentId(), IndentStat.INVALID.getName()).updateById();

		// 作废对应付款单
//		Receipt updatingReceipt = new Receipt();
//		updatingReceipt.setStat(false);
//		receiptService.update(
//				updatingReceipt,
//				new LambdaQueryWrapper<Receipt>()
//						.eq(Receipt::getBusiNo,indentNo)
//						.eq(Receipt::getType,Constant.FKD_CHINESE)
//		);

		return ResponseModel.getInstance().succ(true).msg("作废退货单成功");

	}

	@Override
	public ResponseModel removeReturnSalesById(Long indentId) {
		Indent indent = getById(indentId);

		if (IndentStat.INVALID.getName().equals(indent.getStat()) || IndentStat.CANCELLED.getName().equals(indent.getStat())) {
			super.baseMapper.deleteById(indentId);
		} else {
			return ResponseModel.getInstance().succ(false).msg("已作废或已取消的退货单才可被删除.");
		}
		indentDetailMapper.delete(
				new UpdateWrapper<IndentDetail>()
						.eq("indent_no", indent.getIndentNo())
						.eq("stat", indent.getStat())
		);
		return ResponseModel.getInstance().succ(true).msg("删除退货单成功.");
	}

	@Override
	public ResponseModel removeReturnSalesByIds(Collection<Long> indentIds) {
		for (Long indentId : indentIds) {
			removeReturnSalesById(indentId);
		}
		return ResponseModel.getInstance().succ(true).msg("批量删除退货单成功.");
	}

	/**
	 * 增加欠条-->只能添加一次欠条
	 *
	 * @param indentId
	 * @param iouAmnt
	 * @return
	 */
	@Override
	public ResponseModel addIou(Long indentId, String iouAmnt) {
		String msg = "增加欠条成功 ";
		Indent dbIndent = this.getById(indentId);

		if (null == dbIndent) {
			return ResponseModel.getInstance().succ(false).msg("无效的订单");
		}

		/**
		 * 判断欠条是否为0，保证只能打一次欠条
		 */
		if (Double.valueOf(dbIndent.getIouAmnt()) > 0) {
			return ResponseModel.getInstance().succ(false).msg("欠条不可修改");
		}

		String stat = dbIndent.getStat();
		String receiptStat = dbIndent.getReceiptStat();

		if (IndentStat.WAIT_CONFIRM.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("待审核的订单不能添加欠条");
		}

		if (IndentStat.FINISHED.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("已完成的订单不能添加欠条");
		}

		if (PaymentStat.RECEIPRED.getName().equals(receiptStat)) {
			return ResponseModel.getInstance().succ(false).msg("已收款的订单不能添加欠条");
		}

		// 已收款
		BigDecimal receivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());

		// 应收款
		BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());

		// 已付款
		BigDecimal payedAmnt = new BigDecimal(dbIndent.getPayedAmnt());

		// 欠条
		BigDecimal newIouAmnt = new BigDecimal(iouAmnt);

		Indent updatingIndent = new Indent();
		if (indentTotal.compareTo(receivedAmnt.add(payedAmnt).add(newIouAmnt)) < 0) {
			return ResponseModel.getInstance().succ(false).msg("欠条金额+收款金额+付款金额不能大于应收金额");
		} else if (indentTotal.compareTo(receivedAmnt.add(payedAmnt).add(newIouAmnt)) == 0) { // 如果欠条金额+收款金额+付款金额 == 应收金额
			// 1.设置是否可以财务审核状态为true->表示可以进行财务审核了
			updatingIndent.setAuditable(true);
			updatingIndent.setIouStat(true); //打欠条的时候已经交账
		}

		updatingIndent.setIndentId(indentId);
		updatingIndent.setIouAmnt(iouAmnt);
		updatingIndent.updateById();

		return ResponseModel.getInstance().succ(true).msg(msg);
	}

	/**
	 * 更新或修改欠条
	 *
	 * @param indentId
	 * @param iouAmnt
	 * @param iouTime
	 * @param iouRemarks
	 * @return
	 */
	@Override
	public ResponseModel addOrUpdateIou(Long indentId, String iouAmnt, String iouTime, String iouRemarks) {
		String msg = "欠条新建成功！";
		Indent dbIndent = this.getById(indentId);

		if (null == dbIndent) {
			return ResponseModel.getInstance().succ(false).msg("无效的订单");
		}

		String stat = dbIndent.getStat();
		String receiptStat = dbIndent.getReceiptStat();
		Boolean auditStat = dbIndent.getAuditStat();

		if (true == auditStat) {
			return ResponseModel.getInstance().succ(false).msg("订单已财审，不能操作欠条！");
		}
		if (IndentStat.FINISHED.getName().equals(stat)) {
			return ResponseModel.getInstance().succ(false).msg("订单已完成，不能操作欠条！");
		}

		// 已收款
		BigDecimal receivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());

		// 应收款
		BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());

		// 已付款
		BigDecimal payedAmnt = new BigDecimal(dbIndent.getPayedAmnt());

		// 欠条
		BigDecimal newIouAmnt = new BigDecimal(iouAmnt);

		Indent updatingIndent = new Indent();

		/**
		 * 如果欠条金额+已收金额+已付金额 >= 应收金额
		 * 	1.设置是否可以财务审核状态为true->表示可以进行财务审核了
		 * 	2.已经交账
		 */
		BigDecimal addedAmount = receivedAmnt.add(payedAmnt).add(newIouAmnt);
		boolean isAuditable = indentTotal.compareTo(addedAmount) <= 0;
		if (isAuditable) {
			updatingIndent.setAuditable(true);
			updatingIndent.setIouStat(true);
		}
		updatingIndent.setIndentId(indentId);
		// 保证欠条说明(清空时)能被更新
		if (StringUtils.isEmpty(iouRemarks)) {
			updatingIndent.setIouRemarks("");
		} else {
			updatingIndent.setIouRemarks(iouRemarks);
		}
		updatingIndent.setIouTime(iouTime);
		updatingIndent.setIouAmnt(iouAmnt);
		updatingIndent.updateById();
		if (StringUtils.isNotEmpty(dbIndent.getIndentTime())) {
			msg = "欠条更新成功！";
		}
		return ResponseModel.getInstance().succ(true).msg(msg);
	}

	/**
	 * 订货单付款
	 *
	 * @param indentId  订货单id
	 * @param payAmount 付款额
	 * @param oper      业务类型
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel indentPay(Long indentId, String payAmount, String oper, String payway, HttpServletRequest request) {

		Indent dbIndent = this.getById(indentId);

		if (null == dbIndent) {
			return ResponseModel.getInstance().succ(false).msg("无效的订单");
		}

		if (PaymentStat.RECEIPRED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("订单已收款");
		}

		// 已收款
		BigDecimal receivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());

		// 应收款
		BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());

		// 已付款
		BigDecimal payedAmnt = new BigDecimal(dbIndent.getPayedAmnt());

		// 新付款
		BigDecimal newPayAmount = new BigDecimal(payAmount);

		if (receivedAmnt.add(payedAmnt).add(newPayAmount).compareTo(indentTotal) > 0) {
			return ResponseModel.getInstance().succ(false).msg("已收款+已付款+新增付款不能大于应收款");
		}

		// 更新订货单的已付金额
		Indent updatingIndent = new Indent();
		// 判断是否设置收款状态 => 已付款 + 已收款 == 应收款
		if (receivedAmnt.add(payedAmnt).add(newPayAmount).compareTo(indentTotal) == 0) {
			updatingIndent.setReceiptStat(PaymentStat.RECEIPRED.getName());
		}
		updatingIndent.setIndentId(indentId);
		updatingIndent.setPayedAmnt(payedAmnt.add(newPayAmount).toString());

		// 判断已收款+已付款+欠条 >= 应收款，满足则设置是否可以财务审核状态为true
		if (receivedAmnt.add(payedAmnt).add(newPayAmount).compareTo(indentTotal) == 0) {
			updatingIndent.setReceiptStat(PaymentStat.RECEIPRED.getName());
		}

		updatingIndent.updateById();

		// 添加付款单
		Receipt receipt = new Receipt();
		receipt.setCustId(dbIndent.getCustId());
		receipt.setReceiptAmount(payAmount);
		receipt.setBusiNo(dbIndent.getIndentNo());
		receipt.setAccountType(oper);
		receipt.setType(Constant.FKD_CHINESE);
		String token = request.getHeader("token");
		Integer empId = Integer.valueOf((String) redisTemplate.opsForValue().get(token.concat(Constant.EMP_ID_IDENTIFIER)));
		receipt.setChargemanId(empId);
		receipt.setEmpId(empId);
		receipt.setPayway(payway);
		receiptService.savePayReceipt(receipt);

		return ResponseModel.getInstance().succ(true).msg("订货单付款成功");
	}

	/*************************************************财务审核订货单****************************************************/

	@Override
	public ResponseModel auditIndentById(Long indentId) {
		Indent dbIndent = this.getById(indentId);
		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg("无效的订货单");
		}
		Boolean iouStat = dbIndent.getIouStat();
		Boolean auditStat = dbIndent.getAuditStat();
		if (true == auditStat) {
			return ResponseModel.getInstance().succ(false).msg("请不要重复进行财务审核");
		}

		if (IndentStat.FINISHED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("已完成的订货单不需要再进行财务审核");
		}
		if (false == iouStat) {
			return ResponseModel.getInstance().succ(false).msg("未交账的订货单不可以进行财务审核");
		}
		dbIndent.setAuditStat(true);

		boolean successful = IndentUtil.handleIndentStat(dbIndent);

		return ResponseModel.getInstance().succ(successful).msg(successful ? "财务审核成功" : "财务审核失败");
	}

	@Override
	public ResponseModel auditIndentById(Long indentId, String auditRemarks) {
		Indent dbIndent = this.getById(indentId);
		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg("无效的订货单");
		}
		Boolean iouStat = dbIndent.getIouStat();
		Boolean auditStat = dbIndent.getAuditStat();
		if (true == auditStat) {
			return ResponseModel.getInstance().succ(false).msg("请不要重复进行财务审核");
		}

		if (IndentStat.FINISHED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("已完成的订货单不需要再进行财务审核");
		}
		if (false == iouStat) {
			return ResponseModel.getInstance().succ(false).msg("未交账的订货单不可以进行财务审核");
		}
		Integer custId = dbIndent.getCustId();
		ResponseModel responseModel = CustomerUtil.checkDebtLimit(dbIndent.getIndentNo(), custId);
		if (!responseModel.getSucc()) {
			return responseModel;
		}
		dbIndent.setAuditStat(true);
		String dbAuditRemarks = dbIndent.getAuditRemarks();
		String indentStat = dbIndent.getStat();

		if (StringUtils.isEmpty(dbAuditRemarks)) {
			dbAuditRemarks = "";
		}
		// 保证不传此参数时或传null时清空备注
		if (StringUtils.isEmpty(auditRemarks)) {
			dbIndent.setAuditRemarks("");
		}
		boolean equals = dbAuditRemarks.equals(auditRemarks);

		// 如果旧财审备注和新财审备注不相同则更新财审备注
		if (false == equals) {
			dbIndent.setAuditRemarks(auditRemarks);
		}
		// 如果订货单已经出库并且旧财审备注和新财审备注不相同才同步财审备注到欠款明细里面去
		if (IndentStat.STOCKOUTED.getName().equals(indentStat) && false == equals) {
			String indentNo = dbIndent.getIndentNo();
			DealDetailUtil.saveOrUpdateAuditRemarks(auditRemarks, indentNo);
		}

		boolean successful = IndentUtil.handleIndentStat(dbIndent);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "财务审核成功" : "财务审核失败");
	}

	@Override
	public ResponseModel cancelReceipt(Receipt receipt) {

		/*****************************************处理订单金额 ，减少已收款金额****************************************/
		String busiNo = receipt.getBusiNo();
		Indent dbIndent = this.getOne(
				new LambdaQueryWrapper<Indent>()
						.eq(Indent::getIndentNo, busiNo)
		);
		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}
		if (IndentStat.FINISHED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INDENT_FORBIDDEN);
		}

		String historyReceivedAmnt = dbIndent.getReceivedAmnt();
		String minusReceiptAmnt = receipt.getReceiptAmount();
		BigDecimal newReceivedAmnt = new BigDecimal(historyReceivedAmnt).subtract(new BigDecimal(minusReceiptAmnt));
		dbIndent.setReceivedAmnt(newReceivedAmnt.toString());
		dbIndent.setReceiptStat(PaymentStat.WAIT_RECEIPT.getName());
		/******************************************** 更新订货单的状态和已收款 ****************************************/
		IndentUtil.refreshIndent(dbIndent);

		/*************************************************** 处理收款单 ***********************************************/
		List<Receipt> receipts = new ArrayList<>();
		receipts.add(receipt);
		int custId = receipt.getCustId();
		Customer dbCustomer = customerMapper.selectById(custId);
		ReceiptUtil.cancelReceipts(receipts, dbCustomer);

		return ResponseModel.getInstance().succ(true).msg("作废收款单成功");
	}

	@Override
	public ResponseModel cancelPayReceipt(Receipt pay) {
		/*****************************************处理订单金额 ，减少已付款金额****************************************/
		String busiNo = pay.getBusiNo();
		Indent dbIndent = this.getOne(
				new LambdaQueryWrapper<Indent>()
						.eq(Indent::getIndentNo, busiNo)
		);
		if (Objects.isNull(dbIndent)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}
		if (IndentStat.FINISHED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INDENT_FORBIDDEN);
		}

		String historyPayedAmnt = dbIndent.getPayedAmnt();
		String minusPayAmnt = pay.getReceiptAmount();
		BigDecimal newPayedAmnt = new BigDecimal(historyPayedAmnt).subtract(new BigDecimal(minusPayAmnt));
		dbIndent.setPayedAmnt(newPayedAmnt.toString());
		/******************************************** 更新订货单的状态和已收款 ****************************************/
		IndentUtil.refreshIndent(dbIndent);

		/*************************************************** 处理收款单 ***********************************************/
		List<Receipt> pays = new ArrayList<>();
		pays.add(pay);
		int custId = pay.getCustId();
		Customer dbCustomer = customerMapper.selectById(custId);
		ReceiptUtil.cancelReceipts(pays, dbCustomer);
		return ResponseModel.getInstance().succ(true).msg("作废付款单成功");
	}

	@Override
	public ResponseModel cancelIndentById(Long indentId) {
		return null;
	}

	/**
	 * 修改订单:
	 * 1.任何时候都可以修改订单
	 * 2.不操作收/付款单、欠条
	 * 3.获取出库记录，还回库存，删除出库记录
	 * 4.修改订单
	 * 4.1 删除原商品信息
	 * 4.2 保存新商品信息
	 * 4.3 判断是否已出库，已出库则保存一条核准修改 减少客户欠款的欠款明细
	 * 修改订单应收款，不处理财审状态，不处理已收款、已付款、欠条
	 *
	 * @param indent 前端传回的订单
	 * @return
	 */

	public ResponseModel changeIndent(Indent indent) {
		Long indentId = indent.getIndentId();

		if (NumberUtil.isLongNotUsable(indentId)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}

		Indent dbIndent = new Indent().selectById(indentId);

		if (null == dbIndent) {
			return ResponseModel.getInstance().succ(false).msg(Constant.INVALID_INDENT);
		}

		Boolean auditStat = dbIndent.getAuditStat();

		if (true == auditStat) {
			return ResponseModel.getInstance().succ(false).msg("订单已财审，不能核改");
		}

		if (IndentStat.FINISHED.getName().equals(dbIndent.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("订单已完成，不能核改");
		}

		String indentNo = dbIndent.getIndentNo();

		// 如果为已出库状态，则需要处理客户欠款问题
		if (IndentStat.STOCKOUTED.getName().equals(dbIndent.getStat())) {
			Integer custId = dbIndent.getCustId();
			BigDecimal dbIndentTotal = new BigDecimal(dbIndent.getIndentTotal());
			String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE, indentNo);
			String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
			Customer dbCustomer = new Customer(custId).selectById();
			BigDecimal oldDebt = new BigDecimal(dbCustomer.getDebt());
			BigDecimal newDebt = oldDebt.subtract(dbIndentTotal);
			// 减少客户欠款
			CustomerUtil.subtractCustomerDebt(custId, oldDebt.toString(), dbIndentTotal);
			//保存一条欠款明细
			DealDetailUtil.saveDealDetail(custId, nameNo, currentTime, dbIndentTotal.negate().toPlainString(), newDebt.toString(), Constant.XSSP_HG, "", "", "");
		}

		// 获取出库信息
		List<Stock> stockouts = new Stock().selectList(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getBusiNo, indentNo)
		);

		// 处理订货单商品信息
		if (CollectionUtils.isNotEmpty(stockouts)) {
			Map<String, Object> params = new HashMap<>();
			params.put("stat", Constant.VALID);
			params.put("stockType", Constant.CKD_CHINESE);
			params.put("operType", Constant.XSCK_CHINESE);
			stockouts.forEach(stock -> {
				params.put("stockNo", stock.getStockNo());
				List<StockDetail> stockoutDetails = stockDetailMapper.selectByParams(params);
				stock.setStockouts(stockoutDetails);
			});
		}
		Integer empIdInToken = SysUtil.getEmpId();

		// 删除出库信息
		StockUtil.deleteStockout(stockouts, empIdInToken);

		// 删除旧订货单信息
		new IndentDetail().delete(
				new LambdaQueryWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo, indentNo)
		);

		// 前端传来的订货商品参数包含了旧的参数，比如stockout(已出库数量)，需求进行清理
		List<IndentDetail> indentDetails = indent.getIndentDetails();
		for (IndentDetail indentDetail : indentDetails) {
			indentDetail.setStockout(0);
			indentDetail.setDetailId(null);
		}

		indent.setIndentDetails(indentDetails);

		// 保存订货单商品信息，处理订货单金额和商品数量
		Indent updatingIndent = IndentUtil.saveIndentInfos(indent);

		updatingIndent.setStat(IndentStat.WAIT_STOCKOUT.getName()); // 状态为待出库

		updatingIndent.updateById(); // 更新订货单

		return ResponseModel.getInstance().succ(true).msg("修改订货单成功");
	}

	@Override
//	@Cacheable(value = "key", keyGenerator = "redisCacheKeyGenerator")
	public Page<String> getDiscounts(Page page, Map<String, Object> params) {
		int total = super.baseMapper.selectDiscountsCount(params);
		List<String> discounts = super.baseMapper.selectDiscounts(page, params);
		page.setTotal(total);
		page.setRecords(discounts);
		return page;
	}

	@Override
	public DealDetailSummarizing getDealDetailSummarizingForAdd(Map<String, Object> params) {
		return super.baseMapper.selectDealDetailSummarizingForAdd(params);
	}

	/**
	 * 修改订货单的财审备注
	 *
	 * @param indentNo
	 * @param auditRemarks
	 * @return
	 */
	@Override
	public ResponseModel updateAuditRemarks(String indentNo, String auditRemarks) {
		this.update(
				new LambdaUpdateWrapper<Indent>()
						.eq(Indent::getIndentNo, indentNo)
						.set(Indent::getAuditRemarks, auditRemarks)
		);
		DealDetailUtil.saveOrUpdateAuditRemarks(auditRemarks, indentNo);
		return ResponseModel.getInstance().succ(true).msg(Constant.CHANGE_SUCC);
	}

	@Override
	public Page<DeliveryStaticsModel> getGoodsDeliveryStatics(Page page, Map<String, Object> params) {
		int total = super.baseMapper.selectGoodsDeliveryStaticsCount(params);
		List<DeliveryStaticsModel> deliveryStatics = super.baseMapper.selectGoodsDeliveryStatics(page, params);

		int retain = SysUtil.getSysConfigRetain();

		for (DeliveryStaticsModel deliveryStatic : deliveryStatics) {

			BigDecimal discountTotal = new BigDecimal(deliveryStatic.getDiscountTotal());
			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			deliveryStatic.setDiscountTotal(discountTotal.toString());

			BigDecimal indentTotal = new BigDecimal(deliveryStatic.getIndentTotal());
			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			deliveryStatic.setIndentTotal(indentTotal.toString());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			deliveryStatic.setSalesAmnt(salesAmnt.toString());
		}

		page.setTotal(total);
		page.setRecords(deliveryStatics);
		return page;
	}

	@Override
	public CommonSummation getGoodsDeliveryStaticsSummarizing(Map<String, Object> params) {

		CommonSummation summation = super.baseMapper.selectGoodsDeliveryStaticsSummarizing(params);
		if (summation == null) {
			return null;
		}
		int retain = SysUtil.getSysConfigRetain();

		return this.dealRetain(summation, retain);
	}

	@Override
	public DeliveryStaticsModel getGoodsDeliveryDetailsStaticsSummarizing(Map<String, Object> params) {
		DeliveryStaticsModel deliveryStaticsModel = super.baseMapper.selectGoodsDeliveryDetailsStaticsSummarizing(params);
		int retain = SysUtil.getSysConfigRetain();

		BigDecimal indentTotal = new BigDecimal(deliveryStaticsModel.getIndentTotal());
		BigDecimal discountTotal = new BigDecimal(deliveryStaticsModel.getDiscountTotal());

		BigDecimal salesAmnt = indentTotal.add(discountTotal);
		salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
		deliveryStaticsModel.setSalesAmnt(salesAmnt.toString());

		indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
		deliveryStaticsModel.setIndentTotal(indentTotal.toString());

		discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
		deliveryStaticsModel.setDiscountTotal(discountTotal.toString());

		return deliveryStaticsModel;
	}

	@Override
	public Page<DeliveryDetailsStaticsModel> getGoodsDeliveryDetailsStatics(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		List<DeliveryDetailsStaticsModel> records = super.baseMapper.selectGoodsDeliveryDetailsStatics(page, params);

		for (DeliveryDetailsStaticsModel record : records) {
			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());
			BigDecimal discountTotal = new BigDecimal(record.getDiscountTotal());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountTotal(discountTotal.toString());

		}

		int total = super.baseMapper.selectGoodsDeliveryDetailStaticsCount(params);

		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<String> getIndentNosSelective(Page page, String str) {
		List<String> records = super.baseMapper.selectIndentNos(page, str);
		int total = super.baseMapper.selectIndentNosCount(str);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<IndentNoCustIdNameModel> getIndentNoCustNameSelective(Page page, String str) {
		List<IndentNoCustIdNameModel> records = super.baseMapper.selectIndentNoCustName(page, str);
		int total = super.baseMapper.selectIndentNoCustNameCount(str);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<SalesmanSalesRankModel> getSalesmanSalesRank(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		int total = super.baseMapper.selectSalesmanSalesRankCount(params);

		List<SalesmanSalesRankModel> records = super.baseMapper.selectSalesmanSalesRank(page, params);

		for (SalesmanSalesRankModel record : records) {

			BigDecimal discountTotal = new BigDecimal(record.getDiscountTotal());

			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountTotal(discountTotal.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

		}

		page.setTotal(total);

		page.setRecords(records);

		return page;
	}

	@Override
	public CommonSummation getSalesmanSalesRankSummation(Map<String, Object> params) {

		int retain = SysUtil.getSysConfigRetain();

		CommonSummation summation = super.baseMapper.selectSalesmanSalesRankSummation(params);

		BigDecimal discountAmntSum = new BigDecimal(summation.getDiscountAmntSum());
		BigDecimal indentTotalSum = new BigDecimal(summation.getIndentTotalSum());

		BigDecimal salesAmntSum = indentTotalSum.add(discountAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setSalesAmntSum(salesAmntSum.toString());

		discountAmntSum = discountAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setDiscountAmntSum(discountAmntSum.toString());

		indentTotalSum = indentTotalSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setIndentTotalSum(indentTotalSum.toString());

		return summation;
	}

	/**
	 * 在服务层注入数据权限控制，通过empId查找对应的数据权限-> 可以产看的所有的用户id
	 *
	 * @param params
	 * @param page
	 * @param request
	 * @return
	 */
	@Override
	@DataAuthVerification
	public Page<Indent> getIndentPageSelective(Map<String, Object> params, Page page) {

		int empId = SysUtil.getEmpId();

		SysConfig sysConfig = SysUtil.getSysConfig(empId);
		int retain = sysConfig.getRetain();

		List<Indent> indents = super.baseMapper.selectIndentPageSelective(params, page);
		for (Indent indent : indents) {

			BigDecimal odrAmnt = new BigDecimal(indent.getOdrAmnt());

			odrAmnt = odrAmnt.setScale(retain, RoundingMode.HALF_UP);

			indent.setOdrAmnt(odrAmnt.toString());

			BigDecimal dicountTotal = new BigDecimal(indent.getDiscountTotal());

			dicountTotal = dicountTotal.setScale(retain, RoundingMode.HALF_UP);

			indent.setDiscountTotal(dicountTotal.toString());

			BigDecimal indentTotal = new BigDecimal(indent.getIndentTotal());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);

			indent.setIndentTotal(indentTotal.toString());

			BigDecimal receivedAmnt = new BigDecimal(indent.getReceivedAmnt());

			receivedAmnt = receivedAmnt.setScale(retain, RoundingMode.HALF_UP);

			indent.setReceivedAmnt(receivedAmnt.toString());

			BigDecimal payedAmnt = new BigDecimal(indent.getPayedAmnt());

			payedAmnt = payedAmnt.setScale(retain, RoundingMode.HALF_UP);

			indent.setPayedAmnt(payedAmnt.toString());

			BigDecimal dueAmnt = new BigDecimal(indent.getDueAmnt());

			dueAmnt = dueAmnt.setScale(retain, RoundingMode.HALF_UP);

			indent.setDueAmnt(dueAmnt.toString());

			BigDecimal iouAmnt = new BigDecimal(indent.getIouAmnt());

			iouAmnt = iouAmnt.setScale(retain, RoundingMode.HALF_UP);

			indent.setIouAmnt(iouAmnt.toString());

			// 处理出库详情
			for (IndentDetail indentDetail : indent.getIndentDetails()) {
				Integer goodsId = indentDetail.getGoodsId();
				String indentNo = indentDetail.getIndentNo();
				List<StockMadedate> stockMadedates = stockDetailMapper.selectStockoutDetailInfo(goodsId, indentNo);
				indentDetail.setStockoutMadedates(stockMadedates);
			}
		}

		// 从结果中筛选有两个弊端 ，1: 分页不准确 ，2: 多发sql
//		List<Indent> indentList = indents.stream().filter(indent -> custIds.contains(indent.getCustId())).collect(Collectors.toList());
//		page.setRecords(indentList);
		page.setRecords(indents);
		page.setTotal(super.baseMapper.selectIndentPageSelectiveCount(params));

		return page;
	}


	@Override
	public List<IndentDetail> getIndentDetails(String indentNo) {
		List<IndentDetail> indentDetails = indentDetailMapper.selectByIndentNo(indentNo);
		for (IndentDetail indentDetail : indentDetails) {
			List<StockMadedate> stockMadedates = stockDetailMapper.selectStockoutDetailInfo(indentDetail.getGoodsId(), indentDetail.getIndentNo());
			indentDetail.setStockoutMadedates(stockMadedates);
		}
		return indentDetails;
	}

	@Override
	public Map<String, Object> pdfAndPrint(Long indentId) {
		int retain = 2;
		SysConfig sysConfig = ApplicationContextHolder.getBean(SysConfig.class);
		if (null != sysConfig) {
			retain = sysConfig.getRetain();
		}

		Map<String, Object> params = new HashMap<>();
		Indent dbIndent = this.getById(indentId);

		if (null == dbIndent) {
			return params;
		}

		List<IndentDetail> indentDetails = this.getIndentDetails(dbIndent.getIndentNo());

		SysEmp sales = empService.getById(dbIndent.getSalesmanId());
		SysEmp emp = empService.getById(dbIndent.getEmpId());
		Customer dbCustomer = customerService.getById(dbIndent.getCustId());
		Integer areaGrpId = dbCustomer.getAreaGrpId();
		if (NumberUtil.isIntegerUsable(areaGrpId)) {
			AreaGrp dbAreaGrp = areaGrpMapper.selectOne(
					new LambdaQueryWrapper<AreaGrp>()
							.eq(AreaGrp::getAreaGrpId, areaGrpId)
			);
			if (dbAreaGrp != null) {
				params.put("areaGrp", dbAreaGrp.getAreaGrpName());
			}
		}
		params.put("custName", "");
		if (null != dbCustomer) {
			params.put("custName", dbCustomer.getCustName() + "  " + dbCustomer.getLinkPhone() + " " + dbCustomer.getAddr());
		}

		//构建数据
		params.put("dhdNo", dbIndent.getIndentNo());
		params.put("orderTime", dbIndent.getSalesTime()); //销售时间  不想改模板
		params.put("indentTime", dbIndent.getIndentTime());//下单时间
		List<Stock> stocks = stockService.list(new QueryWrapper<Stock>().eq("busi_no", dbIndent.getIndentNo()));
		// 送货人
		Set<String> shipMans = new HashSet<>(5);
		// 出库经手人
		Set<String> stockoutMans = new HashSet<>(5);

		if (CollectionUtils.isNotEmpty(stocks)) {
			for (Stock stock : stocks) {
				SysEmp sysEmp = empService.getById(stock.getShipmanId());
				shipMans.add(sysEmp.getEmpName());
				SysEmp stockoutMan = empService.getById(stock.getEmpId());
				stockoutMans.add(stockoutMan.getEmpName());
			}
			String shipMan = "";
			for (String man : shipMans) {
				shipMan += man + " ";
			}
			params.put("shipMan", shipMan); // 送货人

			StringBuilder stockoutMan = new StringBuilder();
			for (String man : stockoutMans) {
				stockoutMan.append(man).append(" ");
			}
			params.put("stockoutMan", stockoutMan.toString()); //出库经手人
		}
		params.put("ddremark", dbIndent.getIndentRemarks());//订单备注

		if (null != sales) {
			params.put("salesMan", sales.getEmpName());
		}

		if (null != emp) {
			params.put("empName", emp.getEmpName());
		}

		List<Map> details = new ArrayList<>(indentDetails.size());
		//总数量
		Integer totalNum = 0;

		//总金额
		BigDecimal totalAmount = BigDecimal.ZERO;

		//扣点金额合计
		BigDecimal totalDiscount = BigDecimal.ZERO;

		//优惠
		BigDecimal offerAmount = BigDecimal.ZERO;

		//表格数据
		for (IndentDetail detail : indentDetails) {
			Map<String, Object> dt = new HashMap<>();
			Goods goods = goodsService.getById(Long.valueOf(detail.getGoodsId()));
			Unit unit = unitService.getById(Long.valueOf(detail.getUnitId()));
			dt.put("gcode", goods.getGoodsNo()); //编号
			dt.put("gname", goods.getGoodsName());//名称
			dt.put("guige", goodsService.getGoodsPropsByGoodsId(detail.getGoodsId()));//规格  需要重新查询哦
			dt.put("unit", detail.getUnitName());//单位
			dt.put("num", detail.getNum());//数量
			/** 单价、金额、扣点金额 保留两位小数*/
			dt.put("price", new BigDecimal(detail.getPrice()).setScale(retain, RoundingMode.HALF_UP).toString());//单价
			//总金额 = 实际金额 + 扣点金额
//			Double amount = Double.valueOf(detail.getAmount()) + Double.valueOf(detail.getDiscountAmount());
			BigDecimal amount = new BigDecimal(detail.getAmount()).setScale(retain, RoundingMode.HALF_UP);
			BigDecimal discountAmnt = new BigDecimal(detail.getDiscountAmount()).setScale(retain, RoundingMode.HALF_UP);
			dt.put("amount", amount.add(discountAmnt).toString());//金额
			dt.put("discount", detail.getDiscount()); //扣点
			dt.put("discountAmount", amount.toString()); //扣点金额  就是总金额 - 去优惠金额
			String remarks = detail.getRemarks();
			// 保证remarks占宽足够
//			if(StringUtils.isEmpty(remarks)){
//				remarks = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
//			}else if (remarks.length() <= 2){
//				remarks = "&nbsp;&nbsp;" + remarks + "&nbsp;&nbsp;";
//			}else if(remarks.length() == 3){
//				remarks = "&nbsp;" + remarks + "&nbsp;";
//			}
			dt.put("remark", remarks); //备注

			totalNum += detail.getNum();
			//收款金额合计
			totalAmount = totalAmount.add(amount).add(discountAmnt);
			//优惠金额合计
			offerAmount = offerAmount.add(discountAmnt);

			//扣点金额合计
			totalDiscount = totalDiscount.add(amount);
			details.add(dt);
		}
		params.put("flow_list", details);
		params.put("totalNum", totalNum);

		DecimalFormat df = new DecimalFormat("#0.00");
		//收款金额合计
		params.put("totalAmount", df.format(totalAmount));

		//扣点金额合计
		params.put("totalDiscountAmount", df.format(totalDiscount));

		//优惠金额
		params.put("offer", offerAmount);
		params.put("lowAmount", df.format(totalDiscount));
		params.put("capAmount", RMBUtil.toUpper(df.format(totalDiscount)));
		return params;
	}

	/**
	 * 退货单
	 *
	 * @param indentId
	 * @return
	 */
	@Override
	public Map<String, Object> pdfAndPrintThd(Long indentId) {
		Indent indent = this.getById(indentId);
		Map<String, Object> params = new HashMap<>();
		if (null == indent) {
			return params;
		}

		List<IndentDetail> indentDetails = this.getIndentDetails(indent.getIndentNo());

		SysEmp sales = empService.getById(indent.getSalesmanId());
		SysEmp emp = empService.getById(indent.getEmpId());

		//构建数据
		Customer customer = customerService.getById(indent.getCustId());
		params.put("custName", "");
		if (null != customer) {
			params.put("custName", customer.getCustName() + "  " + customer.getLinkPhone() + " " + customer.getAddr());
		}
		params.put("thdNo", indent.getIndentNo());
		params.put("orderTime", indent.getIndentTime());
		params.put("ddremark", indent.getIndentRemarks());//订单备注

		if (null != sales) {
			params.put("salesMan", sales.getEmpName());
		}

		if (null != emp) {
			params.put("empName", emp.getEmpName());
		}


		List<Map> details = new ArrayList<>(indentDetails.size());
		//总数量
		Integer totalNum = 0;

		//总金额
		BigDecimal totalAmount = BigDecimal.ZERO;


		for (IndentDetail detail : indentDetails) {
			Map<String, Object> dt = new HashMap<>();
			Goods goods = goodsService.getGoodsByGoodsId(detail.getGoodsId());
			dt.put("gcode", goods.getGoodsNo()); //编号
			dt.put("gname", goods.getGoodsName());//名称
			Collection<GoodsSpec> goodsSpecs = goods.getGoodsSpecs();
			StringBuilder specStr = new StringBuilder();
			if (CollectionUtils.isNotEmpty(goodsSpecs)) {
				goodsSpecs.forEach(spec -> specStr.append(spec.getPropName()).append(","));
			}
			if (StringUtils.isNotEmpty(specStr.toString()) && StringUtils.contains(specStr.toString(), ",")) {
				String subSpecStr = StringUtils.substring(specStr.toString(), 0, specStr.lastIndexOf(","));
				dt.put("guige", subSpecStr);//规格
			} else {
				dt.put("guige", "");//规格
			}
			dt.put("unit", detail.getUnitName());//单位
			dt.put("num", detail.getNum());//数量
			dt.put("price", detail.getPrice());//单价
			dt.put("amount", detail.getAmount());//扣点金额
			dt.put("madeDate", detail.getMadeDate()); //批次
			dt.put("remark", detail.getRemarks()); //备注
			totalNum += detail.getNum();
			totalAmount = totalAmount.add(new BigDecimal(String.valueOf(detail.getAmount())));
			details.add(dt);
		}
		params.put("flow_list", details);
		params.put("totalNum", totalNum);

		DecimalFormat df = new DecimalFormat("#0.00");
		params.put("totalAmount", df.format(totalAmount));

		params.put("lowAmount", df.format(totalAmount));
		params.put("capAmount", RMBUtil.toUpper(df.format(totalAmount)));
		return params;
	}

	/**
	 * 客户销售总账
	 *
	 * @param params
	 * @param page
	 * @param request
	 * @return
	 */
	@Override
	@DataAuthVerification
	public Page<CustSalesBillModel> getCustSales(Map<String, Object> params, Page page) {

		SysConfig sysConfig = SysUtil.getSysConfig(SysUtil.getEmpId());
		int retain = sysConfig.getRetain();

		CustSalesSummationModel summation = super.baseMapper.selectCustSalesBillSummation(params);

		BigDecimal salesDiscountSum = new BigDecimal(summation.getSalesDiscountSum());

		BigDecimal receivableAmntSum = new BigDecimal(summation.getReceivableAmntSum());

		BigDecimal salesAmntSum = salesDiscountSum.add(receivableAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setSalesAmntSum(salesAmntSum.toString());

		salesDiscountSum = salesDiscountSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setSalesDiscountSum(salesDiscountSum.toString());

		receivableAmntSum = receivableAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setReceivableAmntSum(receivableAmntSum.toString());

		//设置销售应收/实收
		summation.setReceivableAmntSum(new BigDecimal(summation.getSalesAmntSum()).subtract(new BigDecimal(summation.getSalesDiscountSum())).toString());

		List<CustSalesBillRecordsModel> custSalesBillRecords = super.baseMapper.selectCustSalesBillPageSelective(params, page);

		for (CustSalesBillRecordsModel custSalesBillRecord : custSalesBillRecords) {

			BigDecimal indentTotal = new BigDecimal(custSalesBillRecord.getIndentTotal());
			BigDecimal discountTotal = new BigDecimal(custSalesBillRecord.getDiscountTotal());

			BigDecimal odrAmnt = indentTotal.add(discountTotal);
			odrAmnt = odrAmnt.setScale(retain, RoundingMode.HALF_UP);
			custSalesBillRecord.setOdrAmnt(odrAmnt.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			custSalesBillRecord.setIndentTotal(indentTotal.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			custSalesBillRecord.setDiscountTotal(discountTotal.toString());
		}

		int total = super.baseMapper.selectCustSalesBillCountSelective(params);

		List<CustSalesBillModel> records = new ArrayList<>();
		CustSalesBillModel custSalesBillModel = new CustSalesBillModel();
		custSalesBillModel.setRecords(custSalesBillRecords);
		custSalesBillModel.setSummation(summation);

		records.add(custSalesBillModel);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	/**
	 * 客户销售汇总
	 *
	 * @param params
	 * @param page
	 * @return
	 */
	@Override
	@DataAuthVerification
	public Page<CustSalesSummarizingModel> getCustSalesSummarizing(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		//获取数据
		List<CustSalesSummarizingModel> records = super.baseMapper.selectCustSalesSummarizingPageSelective(params, page);

		for (CustSalesSummarizingModel record : records) {

			BigDecimal discountAmnt = new BigDecimal(record.getDiscountAmnt());
			BigDecimal amount = new BigDecimal(record.getAmount());
			BigDecimal avgPrice = new BigDecimal(record.getAvgPrice());

			BigDecimal salesAmnt = amount.add(discountAmnt);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			discountAmnt = discountAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountAmnt(discountAmnt.toString());

			amount = amount.setScale(retain, RoundingMode.HALF_UP);
			record.setAmount(amount.toString());

			avgPrice = avgPrice.setScale(retain, RoundingMode.HALF_UP);
			record.setAvgPrice(avgPrice.toString());
		}

		int total = super.baseMapper.selectCustSalesSummarizingCountSelective(params);
		page.setRecords(records);
		page.setTotal(total);
		return page;

	}

	/**
	 * 销售账本的商品汇总 --> 指定客户的商品销售情况，所以数据权限和区域分组都不需要，
	 * 因为客户信息是销售账本筛选(区域分组)和数据权限筛选完之后的客户
	 *
	 * @param params
	 * @return
	 */
	@Override
	public CustSalesStatisticsSummationModel selectCustSalesStatisticsSummation(Map<String, Object> params) {

		CustSalesStatisticsSummationModel custSalesStatisticsSummationModel = super.baseMapper.selectCustSalesStatisticsSummation(params);

		int retain = SysUtil.getSysConfigRetain();

		BigDecimal indentTotalSum = new BigDecimal(custSalesStatisticsSummationModel.getIndentTotalSum());
		BigDecimal discountAmntSum = new BigDecimal(custSalesStatisticsSummationModel.getDiscountAmntSum());

		BigDecimal salesAmntSum = indentTotalSum.add(discountAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesStatisticsSummationModel.setSalesAmntSum(salesAmntSum.toString());

		indentTotalSum = indentTotalSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesStatisticsSummationModel.setIndentTotalSum(indentTotalSum.toString());

		discountAmntSum = discountAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesStatisticsSummationModel.setDiscountAmntSum(discountAmntSum.toString());

		return custSalesStatisticsSummationModel;
	}

	/**
	 * 销售账本的商品汇总 --> 指定客户的商品销售情况，所以数据权限和区域分组都不需要，
	 * 因为客户信息是销售账本筛选(区域分组)和数据权限筛选完之后的客户
	 *
	 * @param params
	 * @return
	 */
	@Override
	public Page<CustSalesStatisticsModel> getCustSalesStatistics(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		List<CustSalesStatisticsModel> records = super.baseMapper.selectCustSalesStatisticsPageSelective(params, page);

		int total = super.baseMapper.selectCustSalesStatisticsCountSelective(params);
		// 去重
		List<CustSalesStatisticsModel> uniqueGoodsIdAndCustId = records.stream().collect(
				Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getGoodsId() + Constant.SPLITTER + o.getCustId()))), ArrayList::new)
		);

		uniqueGoodsIdAndCustId.forEach(unique -> {
			String custId = unique.getCustId();
			String goodsId = unique.getGoodsId();
			params.put("goodsId", goodsId);
			params.put("custId", custId);
			// 查询单价
			String avgPrice = new BigDecimal(super.baseMapper.selectCustSalesStatisticsAvgPrice(params)).setScale(retain, RoundingMode.HALF_UP).toString();
			// 赋值均价
			records.stream().filter(record -> goodsId.equals(record.getGoodsId()) && custId.equals(record.getCustId())).forEach(record -> {
				record.setAvgPrice(avgPrice);
			});
		});

		for (CustSalesStatisticsModel record : records) {

			BigDecimal discountAmount = new BigDecimal(record.getDiscountAmount());

			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());

			BigDecimal salesAmnt = indentTotal.add(discountAmount);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			discountAmount = discountAmount.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountAmount(discountAmount.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

		}
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	/**
	 * 销售账本的商品汇总 --> 指定客户的商品销售情况，所以数据权限和区域分组都不需要，
	 * 因为客户信息是销售账本筛选(区域分组)和数据权限筛选完之后的客户
	 *
	 * @param params
	 * @return
	 */
	@Override
	public Page<CustSalesDetailModel> getCustSalesDetail(Map<String, Object> params, Page page) {

		List<CustSalesDetailModel> records = super.baseMapper.selectCustSalesDetailPageSelective(params, page);

		int retain = SysUtil.getSysConfigRetain();

		for (CustSalesDetailModel record : records) {

			BigDecimal price = new BigDecimal(record.getPrice());
			price = price.setScale(retain, RoundingMode.HALF_UP);
			record.setPrice(price.toString());

			BigDecimal discountAmount = new BigDecimal(record.getDiscountAmount());
			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());

			BigDecimal salesAmnt = indentTotal.add(discountAmount);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			discountAmount = discountAmount.setScale(retain, RoundingMode.HALF_UP);

			if (record.getIndentNo().startsWith("TH")) {
				salesAmnt = salesAmnt.negate();
				indentTotal = indentTotal.negate();
				discountAmount = discountAmount.negate();
				record.setSalesNum(Constant.MINUS.concat(record.getSalesNum()));
			}

			record.setSalesAmnt(salesAmnt.toString());
			record.setIndentTotal(indentTotal.toString());
			record.setDiscountAmount(discountAmount.toString());
		}

		int total = super.baseMapper.selectCustSalesDetailCountSelective(params);

		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public Page<AreaSalesRankModel> getFirstLevelAreaSalesRank(Page page) {

		/**
		 * 获取每个一级区域的客户id
		 *  1.获取所有的一级区域
		 *  2.获取每个一级区域下的所有子区域
		 *  3.通过每个一级区域下的所有子区域获取每个一级区域的所有客户
		 *  4.统计每个一级区域的所有客户的销售金额和销售数量
		 *  5. done
		 */

		// 1.获取所有一级区域
		List<AreaGrp> areaGrps = areaGrpMapper.selectFirstLevelAreaGrpPage(PageUtils.getPageParam(new PageHelper(1, 10)), 0);

		List<AreaSalesRankModel> areaSalesRanks = new ArrayList<>();

		if (!areaGrps.isEmpty()) {
			areaGrps.stream().map(AreaGrp::getAreaGrpId).limit(page.getSize()).collect(Collectors.toSet()).forEach(areaGrpId -> {
				// 2.获取到每一个一级区域下的所有子区域，包括自己
				// Set<Integer> subAreaGrpIds = areaGrpMapper.selectSubAreaGrpIds(custId);
				// 3.通过每个一级区域下的所有子区域获取每个一级区域的所有客户
				// 4.统计每个一级区域的所有客户的销售金额和销售数量

				// 2、3、4整合一起操作
				AreaSalesRankModel areaSalesRankModel = super.baseMapper.selectAreaSalesRankByAreaGrpId(areaGrpId);
				String areaGrpName = areaGrpMapper.selectAreaGrpNameByAreaGrpId(areaGrpId);
				areaSalesRankModel.setAreaGrpName(areaGrpName);
				areaSalesRankModel.setAreaGrpId(areaGrpId);
				areaSalesRanks.add(areaSalesRankModel);

			});
		}

		page.setRecords(
				areaSalesRanks.stream().sorted(
						(asr1, asr2) -> asr2.getSalesAmnt().compareTo(asr1.getSalesAmnt())
				).collect(Collectors.toList())
		);

		int total = areaGrpMapper.selectCount(
				new LambdaQueryWrapper<AreaGrp>()
						.eq(AreaGrp::getPid, 0)
		);

		page.setTotal(total);
		return page;
	}

	@Override
	@DataAuthVerification
	public Page<AreaSalesRankModel> getAreaSalesRank(Map<String, Object> params, Page page) {
		List<AreaSalesRankModel> records = super.baseMapper.selectAreaSalesRank(page, params);
		int retain = SysUtil.getSysConfigRetain();
		for (AreaSalesRankModel record : records) {
			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());
			BigDecimal discountTotal = new BigDecimal(record.getDiscountTotal());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountTotal(discountTotal.toString());
		}
		int total = super.baseMapper.selectAreaSalesRankCount(params);
		/**************************************************************************************************************/
		page.setRecords(records);
		page.setTotal(total);
		return page;

	}

	@Override
	public CommonSummation getAreaSalesRankSummation(Map<String, Object> params) {

		CommonSummation summation = super.baseMapper.selectAreaSalesRankSummation(params);

		int retain = SysUtil.getSysConfigRetain();

		summation = this.dealRetain(summation, retain);

		return summation;
	}

	@Override
	@DataAuthVerification
	public Page<CustSalesRankModel> getCustSalesRank(Map<String, Object> params, Page page) {
		int retain = SysUtil.getSysConfigRetain();
		List<CustSalesRankModel> records = super.baseMapper.selectCustSalesRank(params, page);
		for (CustSalesRankModel record : records) {

			BigDecimal discountTotal = new BigDecimal(record.getDiscountTotal());
			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountTotal(discountTotal.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());
		}
		page.setRecords(records);
		int total = super.baseMapper.selectCustSalesRankCount(params);
		page.setTotal(total);
		return page;
	}

	@Override
	public CommonSummation getCustSalesRankSummation(Map<String, Object> params) {

		CommonSummation summation = super.baseMapper.selectCustSalesRankSummation(params);

		int retain = SysUtil.getSysConfigRetain();

		summation = dealRetain(summation, retain);

		return summation;
	}

	/**
	 * 商品销售汇总不做数据权限控制
	 *
	 * @param params
	 * @param page
	 * @return
	 */
	@Override
//	@DataAuthVerification
	public Page<GoodsSalesSummarizingModel> getGoodsSalesSummarizing(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();
		List<GoodsSalesSummarizingModel> records = super.baseMapper.selectGoodsSalesSummarizing(params, page);

		for (GoodsSalesSummarizingModel record : records) {

			BigDecimal discount = new BigDecimal(record.getDiscount());
			discount = discount.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscount(discount.toString());

			BigDecimal price = new BigDecimal(record.getPrice());
			price = price.setScale(retain, RoundingMode.HALF_UP);
			record.setPrice(price.toString());

			BigDecimal discountTotal = new BigDecimal(record.getDiscountTotal());
			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());

			BigDecimal salesAmnt = indentTotal.add(discountTotal);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountTotal(discountTotal.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

		}

//		int total = super.baseMapper.selectGoodsSalesSummarizingCount(params);
		int total = super.baseMapper.selectGoodsSalesSummarizingCount(params);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public CommonSummation getGoodsSalesSummation(Map<String, Object> params) {
		CommonSummation summation = super.baseMapper.selectGoodsSalesSummation(params);
		int retain = SysUtil.getSysConfigRetain();
		return this.dealRetain(summation, retain);
	}

	/**
	 * 商品销售明细不做数据权限控制
	 * selectSingleGoodsSalesDetail(params,page) 只有一条记录，但是为了将分页数据一起返回，所以封装方法时类型为Page<SingleGoodsSalesDetailModel>
	 */
	@Override
	public Page<SingleGoodsSalesDetailModel> getSingleGoodsSalesDetail(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		SingleGoodsSalesDetailModel singleGoodsSalesDetailModel = super.baseMapper.selectSingleGoodsSalesDetail(params);

		BigDecimal salesAmntSum = new BigDecimal(singleGoodsSalesDetailModel.getSalesAmntSum());

		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);

		singleGoodsSalesDetailModel.setSalesAmntSum(salesAmntSum.toString());

		List<SingleGoodsSalesIndentDetailModel> singleGoodsSalesIndentDetailModels = indentDetailMapper.selectSingleGoodsIndentDetail(page, params);

		for (SingleGoodsSalesIndentDetailModel record : singleGoodsSalesIndentDetailModels) {

			BigDecimal discount = new BigDecimal(record.getDiscount());
			discount = discount.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscount(discount.toString());

			BigDecimal price = new BigDecimal(record.getPrice());
			price = price.setScale(retain, RoundingMode.HALF_UP);
			record.setPrice(price.toString());

			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());
			BigDecimal discountAmount = new BigDecimal(record.getDiscountAmount());
			BigDecimal salesAmnt = indentTotal.add(discountAmount);

			if (record.getIndentNo().startsWith("TH")) {
				salesAmnt = salesAmnt.negate();
				indentTotal = indentTotal.negate();
				discountAmount = discountAmount.negate();
				record.setNum(Constant.MINUS.concat(record.getNum()));
			}

			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			discountAmount = discountAmount.setScale(retain, RoundingMode.HALF_UP);

			record.setSalesAmnt(salesAmnt.toString());
			record.setIndentTotal(indentTotal.toString());
			record.setDiscountAmount(discountAmount.toString());
		}

		singleGoodsSalesDetailModel.setIndentDetails(singleGoodsSalesIndentDetailModels);

		int total = indentDetailMapper.selectSingleGoodsIndentDetailCount(params);

		List<SingleGoodsSalesDetailModel> records = new ArrayList<>();
		records.add(singleGoodsSalesDetailModel);
		page.setRecords(records);
		page.setTotal(total);

		return page;

	}

	/**
	 * 商品销售排名不做数据权限控制
	 */
	@Override
	public Page<GoodsSalesRankModel> getGoodsSalesRank(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		List<GoodsSalesRankModel> records = super.baseMapper.selectGoodsSalesRank(params, page);

		for (GoodsSalesRankModel record : records) {

			BigDecimal indentTotal = new BigDecimal(record.getIndentTotal());
			BigDecimal discountAmount = new BigDecimal(record.getDiscountAmount());

			BigDecimal salesAmnt = indentTotal.add(discountAmount);
			salesAmnt = salesAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setSalesAmnt(salesAmnt.toString());

			indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
			record.setIndentTotal(indentTotal.toString());

			discountAmount = discountAmount.setScale(retain, RoundingMode.HALF_UP);
			record.setDiscountAmount(discountAmount.toString());

		}

		int total = super.baseMapper.selectGoodsSalesRankCount(params);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	/**
	 * 商品销售排名不做数据权限控制
	 */
	@Override
	public CommonSummation getGoodsSalesRankSummation(Map<String, Object> params) {
		CommonSummation summation = super.baseMapper.selectGoodsSalesRankSummation(params);
		int retain = SysUtil.getSysConfigRetain();
		return this.dealRetain(summation, retain);
	}

	@Override
	public Page<IndentStatisticsModel> getIndentStatistics(Map<String, Object> params, Page page) {

		int retain = SysUtil.getSysConfigRetain();

		List<IndentStatisticsModel> records = super.baseMapper.selectIndentStatistics(params, page);
		// 计算金额合计=订货金额-退货金额
		records.forEach(record -> {

			BigDecimal orderAmnt = new BigDecimal(record.getOrderAmnt());
			BigDecimal returnAmnt = new BigDecimal(record.getReturnAmnt());

			BigDecimal total = orderAmnt.subtract(returnAmnt);
			total = total.setScale(retain, RoundingMode.HALF_UP);
			record.setTotal(total.toString());

			orderAmnt = orderAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setOrderAmnt(orderAmnt.toString());

			returnAmnt = returnAmnt.setScale(retain, RoundingMode.HALF_UP);
			record.setReturnAmnt(returnAmnt.toString());

		});
		int total = super.baseMapper.selectIndentStatisticsCount(params);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public IndentStatisticsSummationModel getIndentStatisticsSummation(Map<String, Object> params) {

		IndentStatisticsSummationModel summation = super.baseMapper.selectIndentStatisticsSummation(params);

		int retain = SysUtil.getSysConfigRetain();

		BigDecimal orderAmntSum = new BigDecimal(summation.getOrderAmntSum());
		BigDecimal returnAmntSum = new BigDecimal(summation.getReturnAmntSum());

		BigDecimal total = orderAmntSum.subtract(returnAmntSum);
		total = total.setScale(retain, RoundingMode.HALF_UP);
		summation.setTotal(total.toString());

		orderAmntSum = orderAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setOrderAmntSum(orderAmntSum.toString());

		returnAmntSum = returnAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setReturnAmntSum(returnAmntSum.toString());

		BigDecimal owedAmnt = new BigDecimal(summation.getOwedAmnt());
		owedAmnt = owedAmnt.setScale(retain, RoundingMode.HALF_UP);
		summation.setOwedAmnt(owedAmnt.toString());

		return summation;
	}

	/**
	 * 销售账本的商品汇总 --> 指定客户的商品销售情况，所以数据权限和区域分组都不需要，
	 * 因为客户信息是销售账本筛选(区域分组)和数据权限筛选完之后的客户
	 *
	 * @param params
	 * @param request
	 * @return
	 */
	@Override
//	@DataAuthVerification
	public CustSalesDetailSummarizingModel getCustSalesDetailSummarizing(Map<String, Object> params) {
		int retain = SysUtil.getSysConfigRetain();
		CustSalesDetailSummarizingModel custSalesDetailSummarizingModel = super.baseMapper.selectCustSalesDetailSummarizing(params);

		BigDecimal salesDiscountSum = new BigDecimal(custSalesDetailSummarizingModel.getSalesDiscountSum());
		BigDecimal receivableAmntSum = new BigDecimal(custSalesDetailSummarizingModel.getReceivableAmntSum());

		BigDecimal salesAmntSum = salesDiscountSum.add(receivableAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesDetailSummarizingModel.setSalesAmntSum(salesAmntSum.toString());

		salesDiscountSum = salesDiscountSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesDetailSummarizingModel.setSalesDiscountSum(salesDiscountSum.toString());

		receivableAmntSum = receivableAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesDetailSummarizingModel.setReceivableAmntSum(receivableAmntSum.toString());

		return custSalesDetailSummarizingModel;
	}

	@Override
	public CustSalesSummationModel getCustSalesSummation(Map<String, Object> params) {

		CustSalesSummationModel custSalesSummationModel = super.baseMapper.selectCustSalesSummation(params);

		int retain = SysUtil.getSysConfigRetain();

		BigDecimal receivableAmntSum = new BigDecimal(custSalesSummationModel.getReceivableAmntSum());
		BigDecimal salesDiscountSum = new BigDecimal(custSalesSummationModel.getSalesDiscountSum());

		BigDecimal salesAmntSum = salesDiscountSum.add(receivableAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesSummationModel.setSalesAmntSum(salesAmntSum.toString());

		salesDiscountSum = salesDiscountSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesSummationModel.setSalesDiscountSum(salesDiscountSum.toString());

		receivableAmntSum = receivableAmntSum.setScale(retain, RoundingMode.HALF_UP);
		custSalesSummationModel.setReceivableAmntSum(receivableAmntSum.toString());

		return custSalesSummationModel;
	}

	@Override
	public Indent getIndentInfo(String indentNo) {
		Indent indent = super.baseMapper.getIndentByNo(indentNo);
		List<IndentDetail> indentDetails = indent.getIndentDetails();
		SysConfig sysConfig = SysUtil.getSysConfig(SysUtil.getEmpId());
		int retain = sysConfig.getRetain();

		for (IndentDetail indentDetail : indentDetails) {

			BigDecimal price = new BigDecimal(indentDetail.getPrice());
			price = price.setScale(retain, RoundingMode.HALF_UP);
			indentDetail.setPrice(price.toString());

			BigDecimal amount = new BigDecimal(indentDetail.getAmount());
			BigDecimal discountAmount = new BigDecimal(indentDetail.getDiscountAmount());

			BigDecimal total = amount.add(discountAmount);
			total = total.setScale(retain, RoundingMode.HALF_UP);
			indentDetail.setTotal(total.toString());

			amount = amount.setScale(retain, RoundingMode.HALF_UP);
			indentDetail.setAmount(amount.toString());

			BigDecimal discount = new BigDecimal(indentDetail.getDiscount());
			discount = discount.setScale(retain, RoundingMode.HALF_UP);
			indentDetail.setDiscount(discount.toString());

			discountAmount = discountAmount.setScale(retain, RoundingMode.HALF_UP);
			indentDetail.setDiscountAmount(discountAmount.toString());

		}

		BigDecimal indentTotal = new BigDecimal(indent.getIndentTotal());
		BigDecimal discountTotal = new BigDecimal(indent.getDiscountTotal());

		BigDecimal odrAmnt = indentTotal.add(discountTotal);
		odrAmnt = odrAmnt.setScale(retain, RoundingMode.HALF_UP);
		indent.setOdrAmnt(odrAmnt.toString());

		indentTotal = indentTotal.setScale(retain, RoundingMode.HALF_UP);
		indent.setIndentTotal(indentTotal.toString());

		discountTotal = discountTotal.setScale(retain, RoundingMode.HALF_UP);
		indent.setDiscountTotal(discountTotal.toString());

		indent.setIndentDetailsCopy(indentDetails);
		return indent;
	}

	/**
	 * 作废订货单 完成状态的订货单才能作废
	 *
	 * @param indent
	 * @return 1.作废收款单
	 * 2.作废出库单
	 * 2.1作废出库单
	 * 2.2.作废出库单商品信息记录
	 * 2.2返回库存(批次库存、总库存)
	 * 2.3保存一条返回库存的信息
	 * 3.作废订货单
	 * indent{
	 * custId,
	 * empId,
	 * sum,
	 * <p>
	 * indentId,
	 * indentNo,
	 * indentDetails:[
	 * detailId,
	 * goodsId,
	 * multi,
	 * unitId,
	 * stockout,
	 * stockoutMadedates:[
	 * madeDate,
	 * history
	 * ]
	 * ]
	 * }
	 */
	@Override
	@Transactional
	public ResponseModel invalidIndent(Indent indent) {

		Integer custId = indent.getCustId();
		Long indentId = indent.getIndentId();
		String indentNo = indent.getIndentNo();

		receiptService.update(
				new LambdaUpdateWrapper<Receipt>()
						.eq(Receipt::getBusiNo, indentNo)
						.set(Receipt::getStat, false)
		);

		//todo 先实现业务......
//		List<Receipt> receipts = receiptMapper.selectList(
//			new LambdaQueryWrapper<Receipt>()
//				.eq(Receipt::getBusiNo, indentNo)
//				.eq(Receipt::getStat, true)
//		);
//
//		for (Receipt receipt : receipts) {
//			if (receipt.getType().equals(Constant.FKD_CHINESE)){
//				receiptService.cancelPayReceiptById(receipt.getReceiptId());
//			}else{
//				receiptService.cancelReceipt(receipt,request);
//			}
//		}

		// 2.1作废出库单
		// 找出关联出库记录
		List<Stock> stockouts = stockMapper.selectByBusiNo(indentNo);
		// 作废出库单
		StockUtil.invalidIndentStockout(stockouts);

		// 作废订货单
		Indent updatingIndent = new Indent();
		updatingIndent.setIndentId(indentId);
		updatingIndent.setStat(IndentStat.INVALID.getName());
		updatingIndent.setReceiptStat(PaymentStat.INVALID.getName());
		this.updateById(updatingIndent);

		return ResponseModel.getInstance().succ(true).msg("作废订货单成功");
	}

	/**
	 * 提供新建订货单时的商品信息、特价、价格分组和活动信息
	 *
	 * @param custId  客户id
	 * @param goodsId 商品id
	 */
	@Override
	public IndentInfoModel getIndentInfoModel(Integer custId, Integer goodsId) {

		IndentInfoModel indentInfoModel = new IndentInfoModel();

		// 获取商品信息
		indentInfoModel.setGoods(new Goods().selectById(goodsId));

		/**
		 * 1.通过客户id和商品id 找到满足 t_goods_cust_special.cust_id、t_goods_cust_special.goods_id找到GoodsCustSpecial，
		 * 如果有表示客户对该商品有特价/指定价，则不需要进行第2步
		 */
		GoodsCustSpecify goodsCustSpecify =
				new GoodsCustSpecify().selectOne(
						new LambdaQueryWrapper<GoodsCustSpecify>()
								.eq(GoodsCustSpecify::getCustId, custId)
								.eq(GoodsCustSpecify::getGoodsId, goodsId)
				);
		if (goodsCustSpecify != null) {
			indentInfoModel.setGoodsCustSpecify(goodsCustSpecify);
			return indentInfoModel;
		}
		/**
		 * 如果客户对该商品没有特价，则需要获取分组价
		 * 	==>通过商品id、客户id 查找t_customer.price_grp_id==t_active_goods.price_grp_id && t_goods.goods_id==t_goods_price_grp.goods_id满足以上条件的PriceGrp记录
		 */
		GoodsPriceGrp goodsPriceGrp = goodsPriceGrpMapper.selectByGoodsIdAndCustId(goodsId, custId);
		if (goodsPriceGrp != null) {
			indentInfoModel.setGoodsPriceGrp(goodsPriceGrp);
		}
		/**
		 * 2.通过goods_id ==> 查找满足
		 * 	where t_goods_id = #{goodsId}
		 *  and ta.deleted = 0
		 *  and tag.stat = 1
		 *  and tag.active_id = ta.active_id
		 *  and DATE(now()) >= DATE(ta.start_date)
		 *  and DATE(ta.end_date) >= DATE(now())
		 * 条件的记录 Set<ActiveGoods>
		 * 如果存在ActiveGoods那么通过activeId和giftId查找出对应Active和Gift
		 */
		Collection<Active> actives = new HashSet<>();
		/**
		 * 找出指定商品，指定客户所在的区域分组所关联的活动 : 集合
		 * 3.通过客户custId 找到 t_customer.area_grp_id
		 * 	通过 t_customer.area_grp_id  == t_active_area_grp.area_grp_id 找出 t_active_area_grp.active_id
		 * 	通过t_active的active_id == t_active_area_grp.active_id找到客户所在的区域分组所对应的活动Active
		 *
		 * 	涉及的表
		 * 		t_customer
		 * 		t_active
		 * 		t_goods
		 * 		t_active_goods
		 * 		t_active_area_grp
		 *
		 * 	有一些前提条件
		 * 		活动未过期
		 * 		活动在进行中
		 *
		 * 	注意事项 :
		 * 		客户区域属于大区下的一个小区，创建活动时选择的大区，所以需要找出客户所在的所有区域层级，从而找出客户所在的所有层级的活动
		 */
		Set<Active> areaActives = areaGrpMapper.selectCustAreaGrpActiveInfo(custId, goodsId);

		//遍历满足条件的活动-商品-礼品中间表对象 -> 找出活动、商品、礼品对应关系对象
		for (Active active : areaActives) {
			// 如果不是扣点活动discount==0, 就去找对应的礼品信息
			if (!NumberUtil.isIntegerUsable(active.getDiscount())) {
				// 通过activeId,goodsId在中间表t_active_goods表中找出giftId，并通过giftId找出赠品信息
				ActiveGoods atvGoods = activeGoodsMapper.selectOne(
						new LambdaQueryWrapper<ActiveGoods>()
								.eq(ActiveGoods::getActiveId, active.getActiveId())
								.eq(ActiveGoods::getGoodsId, goodsId)
				);
				if (ObjectUtils.isNotEmpty(atvGoods)) {
					Goods unDeletedgoods = goodsMapper.selectUnDeletedGoodsByGoodsId(atvGoods.getGiftId());
					active.setGift(unDeletedgoods);
				}
			}
			actives.add(active);
		}

		indentInfoModel.setActives(actives);
		return indentInfoModel;
	}

	private void deleteRelativeInfos(String indentNo) {
		indentDetailMapper.delete(
				new LambdaUpdateWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo, indentNo)
		);

		stockMapper.delete(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getBusiNo, indentNo)
		);

		stockDetailMapper.delete(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getBusiNo, indentNo)
		);

		receiptMapper.delete(
				new LambdaQueryWrapper<Receipt>()
						.eq(Receipt::getBusiNo, indentNo)
		);
	}

	private static CommonSummation dealRetain(CommonSummation summation, int retain) {

		BigDecimal discountAmntSum = new BigDecimal(summation.getDiscountAmntSum());
		BigDecimal indentTotalSum = new BigDecimal(summation.getIndentTotalSum());

		BigDecimal salesAmntSum = indentTotalSum.add(discountAmntSum);
		salesAmntSum = salesAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setSalesAmntSum(salesAmntSum.toString());

		discountAmntSum = discountAmntSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setDiscountAmntSum(discountAmntSum.toString());

		indentTotalSum = indentTotalSum.setScale(retain, RoundingMode.HALF_UP);
		summation.setIndentTotalSum(indentTotalSum.toString());

		return summation;
	}
}