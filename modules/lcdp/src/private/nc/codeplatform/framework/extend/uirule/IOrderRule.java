package nc.codeplatform.framework.extend.uirule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nc.md.model.IAttribute;
import nc.md.model.IBusinessEntity;
import nc.vo.platform.appsystemplate.AreaVO;
import nc.vo.platform.appsystemplate.FormPropertyVO;
import nc.vo.pub.lang.UFBoolean;
import org.apache.commons.lang3.StringUtils;

public class IOrderRule extends ArrayList<FormPropertyVO> implements IRule
{
    private List<String> orderProps = new ArrayList();
    private List<FormPropertyVO> busiProps = new ArrayList();
    private Map<String, String> allItfMap = new HashMap();
    private Map<String, FormPropertyVO> propMap = new HashMap();
    private IBusinessEntity entity;
    private AreaVO area;
    
    public void execute(IBusinessEntity entity, AreaVO area)
    {
        if (entity != null && area != null)
        {
            this.entity = entity;
            this.area = area;
            List<String> itfs = new ArrayList();
            itfs.add("nc.vo.bd.meta.IBDObject");
            itfs.add("nc.itf.uap.pf.metadata.IHeadBodyQueryItf");
            itfs.add("nc.itf.pubapp.pub.bill.IMakeTime");
            itfs.add("nc.itf.pubapp.pub.bill.IAuditInfo");
            itfs.add("nc.itf.uap.pf.metadata.IFlowBizItf");
            itfs.add("nc.itf.pubapp.pub.bill.IOrgInfo");
            itfs.add("nc.itf.pubapp.pub.bill.IRowNo");
            itfs.add("nc.vo.pub.pf.IPfBillLock");
            this.allItfMap = this.getItfMap((String[]) itfs.toArray(new String[0]));
            this.trans2Map();
            this.initOrderProps();
            this.sort(new PropComparator());
            this.area.setFormPropertyList(this);
        }
    }
    
    public void initOrderProps()
    {
        List<IAttribute> memoTypeList = new ArrayList();
        if (this.area.getAreatype() == 2 && this.entity.isImplementBizInterface("nc.itf.pubapp.pub.bill.IRowNo"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IRowNo");
            String rowno = (String) map.get("rowno");
            if (!StringUtils.isEmpty(rowno))
            {
                IAttribute attr = this.entity.getAttributeByPath(rowno);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(rowno);
                }
            }
        }
        
        if (this.entity.isImplementBizInterface("nc.vo.bd.meta.IBDObject"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.vo.bd.meta.IBDObject");
            String pk_group = (String) map.get("pk_group");
            if (!StringUtils.isEmpty(pk_group))
            {
                IAttribute attr = this.entity.getAttributeByPath(pk_group);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(pk_group);
                }
            }
            
            String pk_org = (String) map.get("pk_org");
            if (!StringUtils.isEmpty(pk_org))
            {
                IAttribute attr = this.entity.getAttributeByPath(pk_org);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(pk_org);
                }
            }
            
            String code = (String) map.get("code");
            if (!StringUtils.isEmpty(code))
            {
                IAttribute attr = this.entity.getAttributeByPath(code);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(code);
                }
            }
            
            String name = (String) map.get("name");
            if (!StringUtils.isEmpty(name))
            {
                IAttribute attr = this.entity.getAttributeByPath(name);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(name);
                }
            }
            
            String pid = (String) map.get("pid");
            if (!StringUtils.isEmpty(pid))
            {
                IAttribute attr = this.entity.getAttributeByPath(pid);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(pid);
                }
            }
        }
        
        List<String> allAreaProps =
            (List) this.allItfMap.entrySet().stream().map((entry) -> (String) entry.getValue()).collect(Collectors.toList());
        this.area.getFormPropertyList().stream().forEach((prop) -> {
            String busicode = prop.getCode();
            if (!allAreaProps.contains(busicode))
            {
                IAttribute attr = this.entity.getAttributeByPath(busicode);
                if (null != attr)
                {
                    if (30 == attr.getDataType().getTypeType())
                    {
                        memoTypeList.add(attr);
                    }
                    else
                    {
                        FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                        if (vo != null)
                        {
                            vo.setPosition(this.orderProps.size());
                            this.orderProps.add(busicode);
                            this.busiProps.add(prop);
                        }
                    }
                }
            }
        });
        if (this.entity.isImplementBizInterface("nc.itf.uap.pf.metadata.IFlowBizItf"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.uap.pf.metadata.IFlowBizItf");
            String billtype = (String) map.get("billtype");
            if (!StringUtils.isEmpty(billtype))
            {
                IAttribute attr = this.entity.getAttributeByPath(billtype);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(billtype);
                }
            }
            
            String transTypepk = (String) map.get("transtypepk");
            if (!StringUtils.isEmpty(transTypepk))
            {
                IAttribute attr = this.entity.getAttributeByPath(transTypepk);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(transTypepk);
                }
            }
            
            String transType = (String) map.get("transtype");
            if (!StringUtils.isEmpty(transType))
            {
                IAttribute attr = this.entity.getAttributeByPath(transType);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(transType);
                }
            }
            
            String busiType = (String) map.get("busitype");
            if (!StringUtils.isEmpty(busiType))
            {
                IAttribute attr = this.entity.getAttributeByPath(busiType);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(busiType);
                }
            }
            
            String srcBillType = (String) map.get("srcbilltype");
            if (!StringUtils.isEmpty(srcBillType))
            {
                IAttribute attr = this.entity.getAttributeByPath(srcBillType);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(srcBillType);
                }
            }
            
            String approveStatus = (String) map.get("approvestatus");
            if (!StringUtils.isEmpty(approveStatus))
            {
                IAttribute attr = this.entity.getAttributeByPath(approveStatus);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(approveStatus);
                }
            }
            
            String approver = (String) map.get("approver");
            if (!StringUtils.isEmpty(approver))
            {
                IAttribute attr = this.entity.getAttributeByPath(approver);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(approver);
                }
            }
            
            String approveDate = (String) map.get("approvedate");
            if (!StringUtils.isEmpty(approveDate))
            {
                IAttribute attr = this.entity.getAttributeByPath(approveDate);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(approveDate);
                }
            }
            
            String note = (String) map.get("approvenote");
            if (!StringUtils.isEmpty(note))
            {
                IAttribute attr = this.entity.getAttributeByPath(note);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(note);
                }
            }
            
            String billId = (String) map.get("billid");
            if (!StringUtils.isEmpty(billId))
            {
                IAttribute attr = this.entity.getAttributeByPath(billId);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(billId);
                }
            }
            
            String srcBillId = (String) map.get("srcbillid");
            if (!StringUtils.isEmpty(srcBillId))
            {
                IAttribute attr = this.entity.getAttributeByPath(srcBillId);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(srcBillId);
                }
            }
            
            String billMaker = (String) map.get("billmaker");
            if (!StringUtils.isEmpty(billMaker))
            {
                IAttribute attr = this.entity.getAttributeByPath(billMaker);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(billMaker);
                }
            }
            
            String billVersionId = (String) map.get("billversionpk");
            if (!StringUtils.isEmpty(billVersionId))
            {
                IAttribute attr = this.entity.getAttributeByPath(billVersionId);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(billVersionId);
                }
            }
            
            String saga_status = (String) map.get("saga_status");
            if (!StringUtils.isEmpty(saga_status))
            {
                IAttribute attr = this.entity.getAttributeByPath(saga_status);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(saga_status);
                }
            }
        }
        
        if (this.entity.isImplementBizInterface("nc.itf.pubapp.pub.bill.IMakeTime"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IMakeTime");
            String billMakeTime = (String) map.get("maketime");
            if (!StringUtils.isEmpty(billMakeTime))
            {
                IAttribute attr = this.entity.getAttributeByPath(billMakeTime);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(billMakeTime);
                }
            }
            
            String lastMakeTime = (String) map.get("lastmaketime");
            if (!StringUtils.isEmpty(lastMakeTime))
            {
                IAttribute attr = this.entity.getAttributeByPath(lastMakeTime);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(lastMakeTime);
                }
            }
        }
        
        if (this.area.getAreatype() == 2 && this.entity.isImplementBizInterface("nc.itf.pubapp.pub.bill.IRowNo"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IRowNo");
            String srcRowno = (String) map.get("srcrowno");
            if (!StringUtils.isEmpty(srcRowno))
            {
                IAttribute attr = this.entity.getAttributeByPath(srcRowno);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(srcRowno);
                }
            }
        }
        
        if (this.area.getAreatype() == 1)
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IRowNo");
            String rowno = (String) map.get("rowno");
            if (!StringUtils.isEmpty(rowno))
            {
                IAttribute attr = this.entity.getAttributeByPath(rowno);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(rowno);
                }
            }
            
            String srcRowno = (String) map.get("srcrowno");
            if (!StringUtils.isEmpty(srcRowno))
            {
                IAttribute attr = this.entity.getAttributeByPath(srcRowno);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(srcRowno);
                }
            }
        }
        
        if (this.entity.isImplementBizInterface("nc.itf.pubapp.pub.bill.IOrgInfo"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IOrgInfo");
            String pk_org_v = (String) map.get("pk_org_v");
            if (!StringUtils.isEmpty(pk_org_v))
            {
                IAttribute attr = this.entity.getAttributeByPath(pk_org_v);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(pk_org_v);
                }
            }
        }
        
        if (this.entity.isImplementBizInterface("nc.itf.pubapp.pub.bill.IAuditInfo"))
        {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo("nc.itf.pubapp.pub.bill.IAuditInfo");
            String creator = (String) map.get("creator");
            if (!StringUtils.isEmpty(creator))
            {
                IAttribute attr = this.entity.getAttributeByPath(creator);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(creator);
                }
            }
            
            String creationtime = (String) map.get("creationtime");
            if (!StringUtils.isEmpty(creationtime))
            {
                IAttribute attr = this.entity.getAttributeByPath(creationtime);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(creationtime);
                }
            }
            
            String modifier = (String) map.get("modifier");
            if (!StringUtils.isEmpty(modifier))
            {
                IAttribute attr = this.entity.getAttributeByPath(modifier);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(modifier);
                }
            }
            
            String modifiedtime = (String) map.get("modifiedtime");
            if (!StringUtils.isEmpty(modifiedtime))
            {
                IAttribute attr = this.entity.getAttributeByPath(modifiedtime);
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attr.getName());
                if (vo != null)
                {
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(modifiedtime);
                }
            }
        }
        
        if (memoTypeList.size() > 0)
        {
            memoTypeList.forEach((attrx) -> {
                FormPropertyVO vo = (FormPropertyVO) this.propMap.get(attrx.getName());
                if (vo != null)
                {
                    vo.setIsnextrow(UFBoolean.TRUE);
                    vo.setColnum("5");
                    vo.setPosition(this.orderProps.size());
                    this.orderProps.add(vo.getCode());
                }
                
            });
        }
        
        this.addAll(this.propMap.values());
    }
    
    public void trans2Map()
    {
        this.area.getFormPropertyList().stream().forEach((prop) -> this.propMap.put(prop.getCode(), prop));
    }
    
    public Map<String, String> getItfMap(String[] itfs)
    {
        Map<String, String> itfMap = new HashMap();
        Arrays.stream(itfs).forEach((itf) -> {
            Map<String, String> map = this.entity.getBizInterfaceMapInfo(itf);
            if (map != null && map.size() > 0)
            {
                itfMap.putAll(map);
            }
            
        });
        return itfMap;
    }
}
