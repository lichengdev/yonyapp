package nc.login.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.UserExit;
import nc.bs.logging.Logger;
import nc.desktop.ui.Workbench;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.desktop.ui.WorkbenchGenerator;
import nc.identityverify.bs.itf.IIdentitiVerifyService;
import nc.identityverify.itf.IAfterVerifySuccessClient;
import nc.identityverify.itf.IClientHandler;
import nc.identityverify.vo.AuthenSubject;
import nc.identityverify.vo.IAConfEntry;
import nc.login.bs.INCLoginService;
import nc.login.bs.LoginToken;
import nc.login.identify.ui.AfterVerifySuccessClientFactory;
import nc.login.identify.ui.ClientHandlerFactory;
import nc.login.identify.ui.ResultMSGTranslator;
import nc.login.vo.AttachedProps;
import nc.login.vo.ILoginConstants;
import nc.login.vo.LoginRequest;
import nc.login.vo.LoginResponse;
import nc.sfbase.client.ClientToolKit;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.sm.clientsetup.ClientSetup;
import nc.ui.sm.clientsetup.ClientSetupCache;
import nc.vo.org.GroupVO;

public class LoginAssistant
{
    
    protected LoginRequest request = null;
    private IAConfEntry entry = null;
    private boolean isForceStaticPwdVerify = false;
    
    public LoginAssistant(LoginRequest request)
    {
        super();
        this.request = request;
    }
    
    public LoginAssistant(LoginRequest request, boolean forceStaticPwdVerify)
    {
        super();
        this.request = request;
        this.isForceStaticPwdVerify = forceStaticPwdVerify;
    }
    
    public LoginResponse login() throws Exception
    {
        validateRequest();
        LoginResponse response = loginImple(false);
        // modify by maokun userCode应该以数据库中的为准
        if (response.getNcSession() != null && StringUtils.isNotBlank(response.getNcSession().getUserCode()))
        {
            UserExit.getInstance().setUserCode(response.getNcSession().getUserCode());
            //
        }
        
        int resultCode = response.getLoginResult();
        if (resultCode == ILoginConstants.USER_ALREADY_ONLINE)
        {
            final List<Boolean> booleanList = new ArrayList<Boolean>();
            synchronized (this)
            {
                Runnable run = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (MessageDialog.showOkCancelDlg(ClientToolKit.getApplet(),
                                NCLangRes.getInstance().getStrByID("sysframev5",
                                    "UPPsysframev5-000058")/* 提示 */,
                                NCLangRes.getInstance().getStrByID("sysframev5",
                                    "UPPsysframev5-000059")/* 该用户已在线，是否强制登录？ */) == MessageDialog.ID_OK)
                            {
                                booleanList.add(Boolean.TRUE);
                            }
                            else
                            {
                                booleanList.add(Boolean.FALSE);
                            }
                        }
                        catch (Throwable th)
                        {
                            Logger.error(th.getMessage(), th);
                        }
                        finally
                        {
                            synchronized (LoginAssistant.this)
                            {
                                LoginAssistant.this.notifyAll();
                            }
                        }
                    }
                    
                };
                SwingUtilities.invokeLater(run);
                this.wait();
            }
            if (booleanList.size() > 0 && booleanList.get(0))
            {
                response = loginImple(true);
            }
        }
        return response;
    }
    
    private void processClientHandler() throws Exception
    {
        if (request.getAttachedProp(LoginToken.class.getName()) != null)
        {
            return;
        }
        if (!isForceStaticPwdVerify)
        {
            AuthenSubject subject = createAuthenSubject();
            IClientHandler handler = ClientHandlerFactory.createClientHandler(getConfEntry());
            handler.handle(subject);
            request.putAttachProp(AuthenSubject.class.getName(), subject);
        }
    }
    
    private boolean processAfterVerifySuccessClient(Object obj)
    {
        boolean goon = true;
        IAfterVerifySuccessClient avsc;
        try
        {
            avsc = AfterVerifySuccessClientFactory.createAfterVerifySuccessClient(getConfEntry());
            if (avsc != null)
            {
                goon = avsc.doVerifySuccess(obj);
            }
            
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage(), e);
            goon = false;
        }
        return goon;
    }
    
    private AuthenSubject createAuthenSubject()
    {
        AuthenSubject subject = new AuthenSubject();
        subject.setBusiCenterCode(request.getBusiCenterCode());
        subject.setUserCode(request.getUserCode());
        subject.setUserPWD(request.getUserPWD());
        return subject;
    }
    
    protected void validateRequest() throws Exception
    {
        String userCode = request.getUserCode();
        if (userCode == null || userCode.equals(""))
        {
            throw new Exception(
                nc.ui.ml.NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000231")/* 您没有输入用户编码！ */);
        }
    }
    
    private LoginResponse loginImple(boolean isForceLogin) throws Exception
    {
        processClientHandler();
        INCLoginService loginService = NCLocator.getInstance().lookup(INCLoginService.class);
        LoginResponse response = null;
        if (isForceStaticPwdVerify)
        {
            response = loginService.loginForceStaticPWD(request, isForceLogin);
        }
        else
        {
            response = loginService.login(request, isForceLogin);
        }
        return response;
    }
    
    public IAConfEntry getConfEntry() throws Exception
    {
        if (entry == null)
        {
            IIdentitiVerifyService verifyServ = NCLocator.getInstance().lookup(IIdentitiVerifyService.class);
            entry = verifyServ.getIAModeVOByUser(request.getUserCode());
        }
        return entry;
    }
    
    public String getResultMessage(int intResult) throws Exception
    {
        return ResultMSGTranslator.translateMessage(intResult, getConfEntry().getResultMsgHandlerClsName());
    }
    
    public void showWorkbench(LoginResponse response) throws Exception
    {
        AttachedProps props = response.getAttachedProps();
        props.putAttachProp(IAConfEntry.class.getName(), getConfEntry());
        // Map<String, List<PluginInfo>> pluginsMap =(Map<String,
        // List<PluginInfo>>)props.getAttachedProp("_client_plugins_");
        // ClientPluginInfoCenter.getInstance().setPluginsMap(pluginsMap);
        
        JApplet applet = ClientToolKit.getApplet();
        Container con = applet.getContentPane();
        // 20250620
        showWorkbenchInContainer(response, con);
        
        //
        WorkbenchEnvironment env = WorkbenchEnvironment.getInstance();
        GroupVO group = env.getGroupVO();
        String bcCode = env.getLoginBusiCenter().getCode();
        String userCode = env.getLoginUser().getUser_code();
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        
        setup.put("loginGroup" + bcCode + userCode, group == null ? null : group.getCode());
        ClientSetupCache.storeGlobalClientSetup();
        
    }
    
    private void showWorkbenchInContainer(final LoginResponse response, final Container parent)
    {
        final Workbench workbench = WorkbenchGenerator.generatorNewWorkbench(response);
        Object obj = response.getAttachedProps().getAttachedProp("_afterVerifySuccessObj_");
        String ssokey = ClientToolKit.getAppletParam("ssoKey");
        final boolean isSSO = !ClientToolKit.isEmptyStr(ssokey);
        boolean isProtectedLogin = request.getAttachedProp(LoginToken.class.getName()) != null;
        // 20250620 去掉修改密码弹框
        // final boolean goon = isProtectedLogin ? true : processAfterVerifySuccessClient(obj);
        Runnable run = new Runnable()
        {
            public void run()
            {
                parent.removeAll();
                parent.setLayout(new BorderLayout());
                parent.add(workbench, BorderLayout.CENTER);
                ClientToolKit.updateComponentTree(parent);
                
                // if(!goon && !isSSO){
                // workbench.logout(true);
                // }else{
                workbench.start();
                // }
            }
        };
        ClientToolKit.invokeInDispatchThread(run);
    }
    
}
