package nc.uap.portal.login;

import nc.uap.portal.login.itf.LoginInterruptedException;
import java.util.HashMap;
import nc.uap.portal.login.vo.AuthenticationUserVO;
import nc.uap.portal.login.itf.ILfwIntegrationHandler;
import nc.uap.portal.user.impl.PortalLoginHandler;
import nc.uap.portal.login.itf.ILoginHandler;
import nc.uap.portal.deploy.vo.PtSessionBean;
import nc.uap.lfw.login.vo.LfwSessionBean;
import nc.uap.portal.login.itf.LoginHelper;
import nc.uap.portal.login.vo.LfwFunNodeVO;
import java.util.Map;
import nc.uap.portal.login.vo.LfwTreeFunNodeVO;
import nc.uap.portal.login.authfield.PasswordExtAuthField;
import nc.uap.portal.login.authfield.UserIdExtAuthField;
import pers.bc.utils.pub.PubEnvUtil;
import uap.lfw.core.ml.LfwResBundle;
import nc.uap.portal.login.authfield.ExtAuthField;
import nc.uap.portal.login.itf.AbstractLfwIntegrateProvider;

public class PortalLoginProvider extends AbstractLfwIntegrateProvider
{
    public ExtAuthField[] getAuthFields()
    {
        final ExtAuthField[] fields = {
            (ExtAuthField) new UserIdExtAuthField(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginProvider-000000"), "userid",
                true),
            (ExtAuthField) new PasswordExtAuthField(LfwResBundle.getInstance().getStrByID("pserver", "PortalLoginProvider-000001"),
                "password", false)};
        return fields;
    }
    
    public LfwTreeFunNodeVO[] getFunNodes()
    {
        return null;
    }
    
    public LfwFunNodeVO[] getFunNodes(final Map<String, String> param)
    {
        return null;
    }
    
    public LoginHelper<? extends LfwSessionBean> getLoginHelper()
    {
        final LoginHelper<PtSessionBean> helper = new LoginHelper<PtSessionBean>()
        {
            public ILoginHandler<PtSessionBean> createLoginHandler()
            {
                final ILoginHandler<PtSessionBean> handler = (ILoginHandler<PtSessionBean>) new PortalLoginHandler();
                return handler;
            }
            
            public ILfwIntegrationHandler createIntegrationHandler()
            {
                final ILfwIntegrationHandler handler = (ILfwIntegrationHandler) new ILfwIntegrationHandler()
                {
                    public AuthenticationUserVO getSsoAuthenticateVO(final Map<String, String> param) throws LoginInterruptedException
                    {
                        final AuthenticationUserVO userVO = new AuthenticationUserVO();
                        userVO.setUserID((String) param.get("userid"));
                        userVO.setPassword((String) param.get("password"));
                        final Map<String, String> extMap = new HashMap<String, String>();
                        // extMap.put("p_language", "simpchn");
                        // extMap.put("p_maxwin", "N");
                        PubEnvUtil.setValNotNON(extMap, "p_language", "simpchn");
                        PubEnvUtil.setValNotNON(extMap, "p_maxwin", "N");
                        // PubEnvUtil.setValNotNON(extMap, "needverifypasswd", param.get("needverifypasswd"));
                        PubEnvUtil.setValNotNON(extMap, "needverifypasswd",
                            PubEnvUtil.equals(PubEnvUtil.Y, param.get("loginsuccess")) ? "N" : null);
                        
                        userVO.setExtInfo((Object) extMap);
                        return userVO;
                    }
                };
                return handler;
            }
        };
        return (LoginHelper<? extends LfwSessionBean>) helper;
    }
}
