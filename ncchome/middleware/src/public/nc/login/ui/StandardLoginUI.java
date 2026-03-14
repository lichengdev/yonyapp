package nc.login.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import nc.bcmanage.vo.BusiCenterVO;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.clientplugin.ui.ClientPluginInfoCenter;
import nc.clientplugin.ui.ClientPluginToolkit;
import nc.clientplugin.vo.ClientPluginInfoPack;
import nc.clientplugin.vo.PluginInfo;
import nc.login.bs.INCLoginService;
import nc.login.vo.AttachedProps;
import nc.sfbase.client.ClientToolKit;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.style.Style;
import nc.ui.sm.clientsetup.ClientSetup;
import nc.ui.sm.clientsetup.ClientSetupCache;
import nc.uitheme.pub.Theme;
import nc.uitheme.ui.ThemeResourceCenter;
import nc.vo.ml.Language;

public class StandardLoginUI extends JPanel
{
    private static final long serialVersionUID = -4044921699046135152L;
    private LoginUISupport uiSupport = null;
    
    public StandardLoginUI()
    {
        super();
        initialize();
    }
    
    private void initialize()
    {
        try
        {
            String cpiVersion = null;
            ClientPluginInfoPack cpiPack = ClientPluginToolkit.loadClientPluginPackFromClientCache();
            if (cpiPack != null)
            {
                cpiVersion = cpiPack.getVersion();
            }
            AttachedProps params = new AttachedProps();
            params.putAttachProp("LoginUIConfig.configFilePath", LoginUIConfig.CONFIG_FILE_PATH);
            params.putAttachProp("clientPluginInfoVersion", cpiVersion);
            INCLoginService service = NCLocator.getInstance().lookup(INCLoginService.class);
            AttachedProps retrValues = service.loginUIData(params);
            Theme theme = (Theme) retrValues.getAttachedProp(Theme.class.getName());
            ThemeResourceCenter.getInstance().setCurrTheme(theme);
            //
            Style.refreshStyle();
            //
            ClientPluginInfoPack pack = (ClientPluginInfoPack) retrValues.getAttachedProp(ClientPluginInfoPack.class.getName());
            String version = pack.getVersion();
            Map<String, List<PluginInfo>> pluginMap = null;
            if (cpiPack != null && version.equals(cpiVersion))
            {
                pluginMap = cpiPack.getPluginInfoMap();
            }
            else
            {
                pluginMap = pack.getPluginInfoMap();
                ClientPluginToolkit.serializeClientPluginInfoPack(pack);
            }
            ClientPluginInfoCenter.getInstance().setPluginsMap(pluginMap);
            //
            byte[] loginUIConfigXMLData = (byte[]) retrValues.getAttachedProp("LoginuiConfig.configXMLData");
            LoginUIConfig.getInstance().setConfigXMLDocumentBytes(loginUIConfigXMLData);
            //
            Language[] langs = (Language[]) retrValues.getAttachedProp("Language Array");
            NCLangRes.getInstance().setAllLanguages(langs);
            //
            BusiCenterVO[] bcVOs = (BusiCenterVO[]) retrValues.getAttachedProp("BusiCenterVO Array");
            
            uiSupport = new LoginUISupport(this)
            {
                private static final long serialVersionUID = -5549201755538381727L;
                
                @Override
                protected void loginEnd(int loginResult, String resultMsg)
                {
                    super.loginEnd(loginResult, resultMsg);
                    if (ClientToolKit.isAdminConsole())
                    {
                        getCbbBusiCenter().setEnabled(false);
                    }
                    else
                    {
                        getCbbBusiCenter().setEnabled(true);
                    }
                    getCbbLanguage().setEnabled(true);
                    getTfUserCode().setEnabled(true);
                    getPfUserPWD().setEnabled(true);
                    // 2014-05-21 sujb modify ����궨λ�������
                    // if(loginResult == ILoginConstants.USER_NAME_WRONG){
                    // getTfUserCode().requestFocus();
                    // getTfUserCode().selectAll();
                    // }else if(loginResult == ILoginConstants.USER_NAME_RIGHT_PWD_WRONG){
                    getPfUserPWD().requestFocus();
                    getPfUserPWD().selectAll();
                    // }
                    warmupWhenLoginEnd();
                }
                
                @Override
                protected void loginStart()
                {
                    super.loginStart();
                    getCbbBusiCenter().setEnabled(false);
                    getCbbLanguage().setEnabled(false);
                    getTfUserCode().setEnabled(false);
                    getPfUserPWD().setEnabled(false);
                    warmupWhenLoginStart();
                }
                
            };
            uiSupport.initCbbBusiCenter(bcVOs);
            uiSupport.initCbbLanguage(langs);
            uiSupport.initFromCookie();
            setLayout(uiSupport.getLoginUILayoutManager());
            initUI();
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage(), e);
        }
        initCodeWarmup();
    }
    
    private void initCodeWarmup()
    {
        String clsName = "nc.ui.uif2.LoginWarmup";
        try
        {
            Class<?> cls = Class.forName(clsName);
            Method m = cls.getMethod("warmup", boolean.class);
            m.invoke(cls, uiSupport.getCbbBusiCenter().getItemCount() == 1);
        }
        catch (Exception th)
        {
            Logger.error(th.getMessage(), th);
        }
    }
    
    private void warmupWhenLoginStart()
    {
        String clsName = "nc.ui.uif2.LoginWarmup";
        try
        {
            Class<?> cls = Class.forName(clsName);
            Method m = cls.getMethod("loginStarted");
            m.invoke(cls);
        }
        catch (Exception th)
        {
            Logger.error(th.getMessage(), th);
        }
    }
    
    private void warmupWhenLoginEnd()
    {
        String clsName = "nc.ui.uif2.LoginWarmup";
        try
        {
            Class<?> cls = Class.forName(clsName);
            Method m = cls.getMethod("loginEnded");
            m.invoke(cls);
        }
        catch (Exception th)
        {
            Logger.error(th.getMessage(), th);
        }
    }
    
    private void initUI()
    {
        add(uiSupport.getLblBusiCenter());
        add(uiSupport.getCbbBusiCenter());
        add(uiSupport.getLblUserCode());
        add(uiSupport.getTfUserCode());
        add(uiSupport.getLblPWD());
        add(uiSupport.getPfUserPWD());
        if (!ClientToolKit.isAdminConsole())
        {
            add(uiSupport.getForgetPWD());
        }
        add(uiSupport.getBtnLogin());
        add(uiSupport.getMenuOption());
        // add(uiSupport.getBtnOption());
        // add(uiSupport.getOptionPanel());
        add(uiSupport.getCbbLanguage());
        add(uiSupport.getLblResultMessage());
        add(uiSupport.getLblLoginFlash());
        add(uiSupport.getLblCopyright());
        if (needShowWebStartPanel())
        {
            add(uiSupport.getWebStartPanel());
        }
    }
    
    private boolean needShowWebStartPanel()
    {
        boolean show = ClientToolKit.isRunInNavigator();
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        boolean b = setup.getBoolean("webstartpanel.closed", false);
        return show && (!b);
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        ILoginUIBackGroundPainter painter = uiSupport.getUIBGPainter();
        painter.paintLoginUIBackGround((Graphics2D) g, this);
        
    }
    
}
