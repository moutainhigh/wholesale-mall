package com.lhiot.mall.wholesale.invoice.service;

import com.leon.microx.util.SnowflakeId;
import com.lhiot.mall.wholesale.base.PageQueryObject;
import com.lhiot.mall.wholesale.demand.domain.DemandGoodsResult;
import com.lhiot.mall.wholesale.demand.domain.gridparam.DemandGoodsGridParam;
import com.lhiot.mall.wholesale.invoice.domain.Invoice;
import com.lhiot.mall.wholesale.invoice.domain.InvoiceTitle;
import com.lhiot.mall.wholesale.invoice.domain.gridparam.InvoiceGridParam;
import com.lhiot.mall.wholesale.invoice.mapper.InvoiceMapper;
import com.lhiot.mall.wholesale.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceMapper invoiceMapper;

    private final SnowflakeId snowflakeId;


    @Autowired
    public InvoiceService(InvoiceMapper invoiceMapper,SnowflakeId snowflakeId) {
        this.invoiceMapper = invoiceMapper;
        this.snowflakeId=snowflakeId;
    }


    public InvoiceTitle selectInvoiceTitle(long id){
        return invoiceMapper.selectInvoiceTitle(id);
    }

    public int saveOrUpdateInvoiceTitle(InvoiceTitle invoiceTitle){
        if (invoiceTitle.getId()>0){
            return invoiceMapper.updateInvoiceTitle(invoiceTitle);
        }else {
            return invoiceMapper.insertInvoiceTitle(invoiceTitle);
        }
    }

    public int applyInvoice(Invoice invoice){
        invoice.setCreateTime(new Timestamp(System.currentTimeMillis()));
        invoice.setInvoiceCode(snowflakeId.stringId());//发票业务编码
        return invoiceMapper.applyInvoice(invoice);
    }

    /**
     * 依据发票code查询发票信息
     * @param invoiceCode
     * @return
     */
    public Invoice findInvoiceByCode(String invoiceCode){
        return invoiceMapper.findInvoiceByCode(invoiceCode);
    }

    /**
     * 修改发票信息
     * @param invoice
     * @return
     */
    public int updateInvoiceByCode(Invoice invoice){
        return invoiceMapper.updateInvoiceByCode(invoice);
    }


    public List<Invoice> list(Invoice invoice){
        return this.invoiceMapper.list(invoice);
    }


    /**
     * 分页查询 开票信息
     * @return
     */
    public PageQueryObject pageQuery(InvoiceGridParam param){
        PageQueryObject result = new PageQueryObject();
        int count = invoiceMapper.pageQueryCount(param);
        int page = param.getPage();
        int rows = param.getRows();
        //起始行
        param.setStart((page-1)*rows);
        //总记录数
        int totalPages = (count%rows==0?count/rows:count/rows+1);
        if(totalPages < page){
            page = 1;
            param.setPage(page);
            param.setStart(0);
        }
        List<Invoice> invoiceList = invoiceMapper.pageQuery(param);
        result.setRows(invoiceList);
        result.setPage(page);
        result.setRecords(rows);
        result.setTotal(totalPages);
        return result;
    }

    /**
     * 查询开票信息详情
     * @return
     */
    public Invoice detail(Long id) {
        return invoiceMapper.select(id);
    }

    /**
     * 修改开票状态
     * @return
     */
    public int updateInvoiceStatus(Invoice invoice) {
        //开票状态改为已开票
        invoice.setInvoiceStatus("yes");
        return invoiceMapper.updateInvoiceStatus(invoice);
    }

    /**
     * 修改驳回原因
     * @return
     */
    public int updateInvoiceReason(Invoice invoice) {
        return invoiceMapper.updateInvoiceReason(invoice);
    }
}
