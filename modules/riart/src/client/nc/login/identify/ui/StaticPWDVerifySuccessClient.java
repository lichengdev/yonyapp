package nc.login.identify.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.desktop.ui.UserPwdUpdateDlg;
import nc.identityverify.itf.AbstractAfterVerifySuccessClient;
import nc.sfbase.client.ClientToolKit;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.uap.rbac.util.UserExManageUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;

public class StaticPWDVerifySuccessClient extends AbstractAfterVerifySuccessClient
{
    public boolean doVerifySuccess(Object obj)
    {
        Map<String, String> map = (Map) obj;
        boolean result = false;
        String userid = InvocationInfoProxy.getInstance().getUserId();
        String inituserinfo = (String) map.get("userpwdinit");
        String UserPwdValidateResult = (String) map.get("UserPwdValidateResult");
        String msg = "当前登录用户：code[" + userid + "]，name[" + inituserinfo + "]，登录时间："
            + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "\r\n" + UserPwdValidateResult;
        System.err.println(LoggerUtil.getSystemInfo());
        System.err.println(msg);
        // libdhc start 20210201
        if (PubEnvUtil.equals("develop", System.getProperty("nc.runMode")))
        {
            return true;
        }
        else if (inituserinfo != null)
        {
            result = this.processInitUser(inituserinfo);
            if (result)
            {
                try
                {
                    UserExManageUtil.getInstance().delInitUser(userid);
                }
                catch (Exception e)
                {
                    Logger.error(NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0000", (String) null,
                        new String[]{e.getMessage()}), e);
                    MessageDialog.showErrorDlg(ClientToolKit.getApplet(), NCLangRes.getInstance().getStrByID("sfbase", "HCH-0000"),
                        NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0001"));
                }
            }
            
            return result;
        }
        else
        {
            String resetinfo = (String) map.get("userpwdreset");
            if (resetinfo != null)
            {
                result = this.processRestPwdUser(resetinfo);
                if (result)
                {
                    UserExManageUtil.getInstance().delResetUserInfo(userid);
                }
                
                return result;
            }
            else
            {
                return this.checkPWDLevel(map);
            }
        }
    }
    
    private boolean processInitUser(String inituserinfo)
    {
        boolean result = false;
        if (inituserinfo.equals("Y"))
        {
            result = updatePwd(NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0002"), true);
        }
        else
        {
            MessageDialog.showErrorDlg(ClientToolKit.getApplet(), NCLangRes.getInstance().getStrByID("sfbase", "HCH-0000"),
                NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0003"));
            result = false;
        }
        
        return result;
    }
    
    private boolean processRestPwdUser(String resetinfo)
    {
        boolean result = false;
        if (resetinfo.equals("Y"))
        {
            result = updatePwd(NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0004"), true);
        }
        else
        {
            MessageDialog.showErrorDlg(ClientToolKit.getApplet(), NCLangRes.getInstance().getStrByID("sfbase", "HCH-0000"),
                NCLangRes.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0003"));
            result = false;
        }
        
        return result;
    }
    
    private boolean checkPWDLevel(Map<String, String> map)
    {
        if (map == null)
        {
            return false;
        }
        else
        {
            String validateResult = (String) map.get("UserPwdValidateResult");
            String validateHintTip = (String) map.get("validateHintTip");
            if (validateResult != null && !validateResult.equals("ok"))
            {
                return updatePwd(validateResult, false);
            }
            else if (validateResult != null && !validateHintTip.equalsIgnoreCase("noMessage"))
            {
                showvalidateHint(validateHintTip);
                return true;
            }
            else
            {
                return true;
            }
        }
    }
    
    private static boolean updatePwd(String changeHint, boolean forceupdate)
    {
        UserPwdUpdateDlg dlg =
            new UserPwdUpdateDlg(ClientToolKit.getApplet(), NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000272"), changeHint);
        Dimension screan = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgsize = dlg.getSize();
        dlg.setLocation((screan.width - dlgsize.width) / 2, (screan.height - dlgsize.height) / 2);
        dlg.setForceUpdate(forceupdate);
        int result = dlg.showModal();
        return result != 70;
    }
    
    private static void showvalidateHint(String hint)
    {
        MessageDialog.showHintDlg(ClientToolKit.getApplet(), (String) null, hint);
    }
}
