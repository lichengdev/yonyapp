package nccloud.framework.web.action.entry;

import nc.lightapp.framework.web.action.attachment.IUploadAction;
import nc.lightapp.framework.web.action.billcard.IBillCardAction;
import nc.lightapp.framework.web.action.billcard.IBillCardBodyAfterEditAction;
import nc.lightapp.framework.web.action.billcard.IBillCardHeadAfterEditAction;
import nc.lightapp.framework.web.action.billcard.IBillQueryKeyAction;
import nc.lightapp.framework.web.action.billlist.IBillListAction;
import nc.lightapp.framework.web.action.billlist.IBillQuerySchemeAction;
import nc.lightapp.framework.web.action.system.IBillTemplateAction;
import nc.lightapp.framework.web.action.volist.IVOListAction;
import nc.lightapp.framework.web.action.volist.IVOListAfterEditAction;
import nc.lightapp.framework.web.action.volist.IVOQueryKeyAction;
import nc.lightapp.framework.web.action.volist.IVOQuerySchemeAction;
import nc.lightapp.framework.web.navigation.entry.attachment.UploadActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billcard.BillCardActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billcard.BillCardBodyAfterEditActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billcard.BillCardHeadAfterEditActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billcard.BillQueryKeyActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billlist.BillListActionExcutor;
import nc.lightapp.framework.web.navigation.entry.billlist.BillQuerySchemeActionExcutor;
import nc.lightapp.framework.web.navigation.entry.system.BillTemplateActionExcutor;
import nc.lightapp.framework.web.navigation.entry.volist.VOListActionExcutor;
import nc.lightapp.framework.web.navigation.entry.volist.VOListAfterEditActionExcutor;
import nc.lightapp.framework.web.navigation.entry.volist.VOQueryKeyActionExcutor;
import nc.lightapp.framework.web.navigation.entry.volist.VOQuerySchemeActionExcutor;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.web.action.excutor.AuthenticateActionExcutor;
import nccloud.framework.web.action.excutor.CommonActionExcutor;
import nccloud.framework.web.action.excutor.IActionExcutor;
import nccloud.framework.web.action.itf.IAuthenticateAction;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IHttpOperator;

/**
 * 
 **
 * @qualiFild nccloud.framework.web.action.entry.ActionExcutor.java<br>
 * @author：LiBencheng<br>
 * @date Created on 2000年1月1日<br>
 * @version 1.0<br>
 */
public class ActionExcutor implements IActionExcutor
{
    public Object excute(Object instance, IHttpOperator operator)
    {
        IActionExcutor excutor = null;
        if (instance instanceof ICommonAction)
            excutor = new CommonActionExcutor();
        else if (instance instanceof IAuthenticateAction) 
            excutor = new AuthenticateActionExcutor();
        else if (instance instanceof IBillTemplateAction) 
            excutor = new BillTemplateActionExcutor();
        else if (instance instanceof IBillCardAction) 
            excutor = new BillCardActionExcutor();
        else if (instance instanceof IBillCardHeadAfterEditAction) 
            excutor = new BillCardHeadAfterEditActionExcutor();
        else if (instance instanceof IBillCardBodyAfterEditAction)
            excutor = new BillCardBodyAfterEditActionExcutor();
        else if (instance instanceof IBillQueryKeyAction) 
            excutor = new BillQueryKeyActionExcutor();
        else if (instance instanceof IBillListAction)
            excutor = new BillListActionExcutor();
        else if (instance instanceof IBillQuerySchemeAction)
            excutor = new BillQuerySchemeActionExcutor();
        else if (instance instanceof IVOListAction)
            excutor = new VOListActionExcutor();
        else if (instance instanceof IVOQueryKeyAction) 
            excutor = new VOQueryKeyActionExcutor();
        else if (instance instanceof IVOQuerySchemeAction) 
            excutor = new VOQuerySchemeActionExcutor();
        else if (instance instanceof IVOListAfterEditAction)
            excutor = new VOListAfterEditActionExcutor();
        else
        {
            if (!(instance instanceof IUploadAction))
            {
                ExceptionUtils.unSupported();
                return null;
            }
            
            excutor = new UploadActionExcutor();
        }
        
        // Object value = ((IActionExcutor) excutor).excute(instance, operator);
        // return value;
        
       return YonYouActionExtUtil.getLocalThread().excute(excutor, instance, operator);
    }
}
