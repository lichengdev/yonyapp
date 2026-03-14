package nccloud.web.baseapp.verfiy.login;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nc.bcmanage.bs.IBusiCenterManageService;
import nc.bcmanage.bs.ISuperAdminService;
import nc.bcmanage.vo.BusiCenterVO;
import nc.bcmanage.vo.SuperAdminVO;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.exception.ComponentNotFoundException;
import nc.bs.framework.server.ServerConfiguration;
import nc.bs.framework.util.KeyUtil;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.itf.bcmanage.bs.IDataSourceQuery;
import nc.itf.uap.rbac.userpassword.IUserPasswordChecker;
import nc.itf.uap.sfapp.SecurityLogInfo;
import nc.itf.uap.sfapp.SecurityLogVarItem;
import nc.itf.uap.sfapp.securityLog.SecurityLogManageFacade;
import nc.login.bs.IIPConfigService;
import nc.login.bs.ILoginStateInform;
import nc.login.bs.INCUserQueryService;
import nc.login.bs.IServerEnvironmentService;
import nc.login.vo.LoginFailureVO;
import nc.pub.securityLog.SecurityLogDBUtil;
import nc.vo.framework.rsa.Encode;
import nc.vo.org.GroupVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.sm.UserExVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.userpassword.PasswordSecurityLevelFinder;
import nc.vo.uap.rbac.userpassword.PasswordSecurityLevelVO;
import nc.vo.uap.rbac.util.RbacUserPwdUtil;
import nc.vo.uap.rbac.util.UserExManageUtil;
import nccloud.commons.lang.StringUtils;
import nccloud.core.baseapp.vo.AuthUserVO;
import nccloud.dto.riart.login.ResultProcess;
import nccloud.framework.service.ServiceLocator;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;

public class VerfiyTool
{
    public static BusiCenterVO findBusiCenter(String bcCode) throws BusinessException
    {
        IBusiCenterManageService service = (IBusiCenterManageService) NCLocator.getInstance().lookup(IBusiCenterManageService.class);
        return service.getBusiCenterByCode(bcCode);
    }
    
    public static int checkBusiCenter(BusiCenterVO bc)
    {
        UFDate today = getToday();
        if (bc.isLocked())
        {
            return 22;
        }
        UFDate effectDate = bc.getEffectDate();
        if (today.before(effectDate))
        {
            return 23;
        }
        UFDate expirDate = bc.getExpireDate();
        if (today.after(expirDate))
        {
            return 24;
        }
        return 21;
    }
    
    public static UserVO findUser(String dsName, String userCode) throws BusinessException
    {
        INCUserQueryService service = (INCUserQueryService) NCLocator.getInstance().lookup(INCUserQueryService.class);
        return service.findUserVO(dsName, userCode);
    }
    
    public static ResultProcess doStaticPWDVerify(AuthUserVO auth, UserVO user) throws BusinessException
    {
        String clientPWD = auth.getUserPWD();
        // 破解
        if (PubEnvUtil.equals("develop", System.getProperty("nc.runMode")))
        {
            String msg = "当前登录用户：code[" + user.getUser_code() + "]，name[" + user.getUser_name() + "]，登录时间："
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            LoggerUtil.getInstance("loginLogs").info(msg);
            return ResultProcess.success();
        }
        if (RbacUserPwdUtil.checkUserPassword(user, clientPWD))
        {
            getServerEnvironmentServiceInstance().clearAllFailure("7", auth.getDsname(), user.getUser_code());
            return ResultProcess.success();
        }
        return PsswordWrongOper(auth, user);
    }
    
    public static int checkUser(UserVO user) throws BusinessException
    {
        if (user.getUser_type().intValue() == 3)
        {
            String securityDSname = SecurityLogDBUtil.getSecurityLogDSName();
            if (securityDSname == null)
            {
                return 33;
            }
            String oldDs = InvocationInfoProxy.getInstance().getUserDataSource();
            InvocationInfoProxy.getInstance().setUserDataSource(securityDSname);
            IDataSourceQuery dsqry = (IDataSourceQuery) NCLocator.getInstance().lookup(IDataSourceQuery.class);
            
            String[] existtables = dsqry.quetyExist(new String[]{"sm_securitylogname", "sm_securitylog_strategy"});
            if ((existtables == null) || (existtables.length <= 0))
            {
                return 33;
            }
            InvocationInfoProxy.getInstance().setUserDataSource(oldDs);
            if ((user.getIsLocked() != null) && (user.getIsLocked().booleanValue()))
            {
                return 4;
            }
        }
        UFDate today = getToday();
        if ((user.getAbledate() != null) && (today.before(user.getAbledate())))
        {
            return 6;
        }
        if ((user.getDisabledate() != null) && (today.after(user.getDisabledate())))
        {
            return 7;
        }
        if ((user.getEnablestate() != null) && (user.getEnablestate().intValue() != 2))
        {
            if (user.getEnablestate().intValue() == 3)
            {
                return 10;
            }
            return 9;
        }
        return 1;
    }
    
    public static int checkClient(UserVO user, String clientIP)
    {
        if (user.getUser_type() == null)
        {
            return 1;
        }
        boolean islegal = true;
        String rootip = clientIP;
        IIPConfigService ipserv = (IIPConfigService) NCLocator.getInstance().lookup(IIPConfigService.class);
        try
        {
            islegal = ipserv.checkRootIP(rootip);
        }
        catch (BusinessException e)
        {
            islegal = false;
            Logger.error(e.getMessage(), e);
        }
        if (islegal)
        {
            return 1;
        }
        return 11;
    }
    
    public static int identifyUserIsAutoLock(UserVO user) throws BusinessException
    {
        PasswordSecurityLevelVO plvo = PasswordSecurityLevelFinder.getPWDLV(user);
        int unlockTime = 0;
        boolean isAutoLock = false;
        if (plvo != null)
        {
            unlockTime = plvo.getUnlocktime().intValue();
            isAutoLock = plvo.getIsautolock().booleanValue();
        }
        else
        {
            unlockTime = -1;
            isAutoLock = true;
        }
        if (isAutoLock)
        {
            boolean lockStatus = UserExManageUtil.getInstance().getUserIsLockStatus(user.getCuserid(), unlockTime);
            if (!lockStatus)
            {
                return 4;
            }
        }
        else if (UserExManageUtil.getInstance().isUserLocked(user.getCuserid()))
        {
            return 4;
        }
        return 0;
    }
    
    public static String convertMsg(int rslCode)
    {
        String resultstr = "some error occur,result NO. is " + rslCode;
        String m = NCLangResOnserver.getInstance().getString("loginui", resultstr, "loginresult-" + rslCode);
        m = m == null ? String.valueOf(rslCode) : m;
        return m;
    }
    
    public static UFDate getToday()
    {
        UFDate today = new UFDate(System.currentTimeMillis());
        return today;
    }
    
    public static ResultProcess doPasswordCheck(UserVO user, AuthUserVO auth) throws BusinessException
    {
        PasswordSecurityLevelVO pwdLevel = PasswordSecurityLevelFinder.getPWDLV(user);
        if (!"0000".equals(auth.getBusiCenterCode()))
        {
            boolean isInit = UserExManageUtil.getInstance().isInitUser(user.getCuserid());
            if (isInit)
            {
                return ResultProcess.msg("2", NCLangResOnserver.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0002"));
            }
            if (UserExManageUtil.getInstance().isPwdResetUser(user.getCuserid()))
            {
                return ResultProcess.msg("2", NCLangResOnserver.getInstance().getStrByID("sfbase", "StaticPWDVerifySuccessClient-0004"));
            }
        }
        String implicitPwd = new Encode().encode(auth.getUserPWD());
        IUserPasswordChecker upchecher = (IUserPasswordChecker) ServiceLocator.find(IUserPasswordChecker.class);
        String pwdCheckMsg = upchecher.getPwdCheckMsg(user, pwdLevel, implicitPwd);
        if (!"ok".equals(pwdCheckMsg))
        {
            return ResultProcess.msg("2", pwdCheckMsg);
        }
        if (!"0000".equals(auth.getBusiCenterCode()))
        {
            try
            {
                upchecher.checkNewpassword(user, auth.getUserPWD(), pwdLevel, user.getUser_type().intValue());
            }
            catch (Exception e)
            {
                return ResultProcess.msg("2", e.getCause().getCause().getMessage());
            }
        }
        String hinttip = upchecher.getValidateTip(user.getPwdparam(), pwdLevel);
        if ((hinttip != null) && (!auth.isNotips()))
        {
            return ResultProcess.msg("3", hinttip);
        }
        return ResultProcess.success();
    }
    
    public static IServerEnvironmentService getServerEnvironmentServiceInstance()
    {
        return (IServerEnvironmentService) NCLocator.getInstance().lookup(IServerEnvironmentService.class);
    }
    
    public static ResultProcess PsswordWrongOper(AuthUserVO vo, UserVO user) throws BusinessException
    {
        PasswordSecurityLevelVO pwdLevel = PasswordSecurityLevelFinder.getPWDLV(user);
        int failureCount = getServerEnvironmentServiceInstance().getFailureCount("7", vo.getDsname(), user.getUser_code());
        if ((pwdLevel != null) && (pwdLevel.getErrorloginThreshold() != null)
            && (failureCount >= pwdLevel.getErrorloginThreshold().intValue()))
        {
            user.setIsLocked(UFBoolean.TRUE);
            lockUser(user);
            getServerEnvironmentServiceInstance().clearAllFailure("7", vo.getDsname(), user.getUser_code());
            return ResultProcess.error(convertMsg(4));
        }
        doPasswordFailLog(vo, user);
        return ResultProcess.error(convertMsg(2));
    }
    
    public static void lockUser(UserVO user) throws BusinessException
    {
        Integer userType = user.getUser_type();
        if ((userType != null) && (userType.intValue() == 3))
        {
            SuperAdminVO adminvo = new SuperAdminVO();
            adminvo.setAdmCode(user.getUser_code());
            adminvo.setAdmName(user.getUser_name());
            adminvo.setIdentify(user.getIdentityverifycode());
            adminvo.setIsLocked(UFBoolean.TRUE);
            adminvo.setPassword(user.getUser_password());
            adminvo.setPwdinuse(user.getPwdparam());
            adminvo.setPwdlvl(user.getPwdlevelcode());
            
            ISuperAdminService service = (ISuperAdminService) NCLocator.getInstance().lookup(ISuperAdminService.class);
            service.lockSuperAdminByCode(adminvo.getAdmCode());
        }
        else
        {
            UserExVO exVO = new UserExVO();
            exVO.setExstatuscode("2");
            exVO.setExtreason("islocked");
            exVO.setUser_id(user.getCuserid());
            exVO.setExtime(new UFDateTime().toStdString());
            UserExManageUtil.getInstance().addUserEx(exVO);
            try
            {
                ILoginStateInform ilsi = (ILoginStateInform) NCLocator.getInstance().lookup(ILoginStateInform.class);
                ilsi.lockPortalUser(user);
            }
            catch (ComponentNotFoundException ce)
            {
                Logger.error("havn't install portal product" + ce.getMessage());
            }
        }
    }
    
    public static void doPasswordFailLog(AuthUserVO auth, UserVO user) throws BusinessException
    {
        BaseDAO dao = new BaseDAO(auth.getDsname());
        GroupVO groupVO = null;
        if (!StringUtils.isEmpty(user.getPk_group()))
        {
            groupVO = (GroupVO) dao.retrieveByPK(GroupVO.class, user.getPk_group());
        }
        LoginFailureVO failureVO = new LoginFailureVO();
        failureVO.setBcCode(auth.getBusiCenterCode());
        IBusiCenterManageService service = (IBusiCenterManageService) NCLocator.getInstance().lookup(IBusiCenterManageService.class);
        failureVO.setBcName(service.getBusiCenterByCode(auth.getBusiCenterCode()).getName());
        failureVO.setClientIP(auth.getClientIP());
        failureVO.setFailureType(3);
        failureVO.setGroupCode(user.getPk_group());
        failureVO.setGroupCode(groupVO == null ? "0001" : groupVO.getCode());
        failureVO.setGroupName(groupVO == null ? "0001" : groupVO.getName());
        failureVO.setServerName(ServerConfiguration.getServerConfiguration().getServerName());
        failureVO.setTime(new Date().getTime());
        failureVO.setUserCode(user.getUser_code());
        failureVO.setUserID(user.getCuserid());
        failureVO.setUserName(user.getUser_name());
        failureVO.setUserType(user.getUser_type().intValue());
        getServerEnvironmentServiceInstance().registerFailure("7", auth.getDsname(), user.getUser_code(), failureVO);
    }
    
    public static void writeSecurityLog(ResultProcess result, UserVO user)
    {
        SecurityLogManageFacade service = (SecurityLogManageFacade) NCLocator.getInstance().lookup(SecurityLogManageFacade.class);
        SecurityLogInfo logInfo = new SecurityLogInfo();
        List<SecurityLogVarItem> logItem = new ArrayList();
        logInfo.setAppTag("NCC");
        logInfo.setLogType("Authentication");
        if (result.isSuceess())
        {
            logInfo.setLogLevel(60);
            logInfo.setLogAbs("Authentication success");
            logInfo.setOpResult("success");
            SecurityLogVarItem item = new SecurityLogVarItem();
            item.setAttributeName("Authentication");
            item.setAttributeVar(user.getIdentityverifycode());
            item.setSecret(false);
            logItem.add(item);
            SecurityLogVarItem item1 = new SecurityLogVarItem();
            item1.setAttributeName("Certifide result");
            item1.setAttributeVar(result.getRslCode());
            item1.setSecret(false);
            logItem.add(item1);
            SecurityLogVarItem item2 = new SecurityLogVarItem();
            item2.setAttributeName("Token");
            item2.setAttributeVar(NetStreamContext.getToken() == null ? "" : KeyUtil.encodeToken(NetStreamContext.getToken()));
            item2.setSecret(true);
            logItem.add(item2);
            logInfo.setOpDescript(logItem);
        }
        else
        {
            logInfo.setLogLevel(40);
            logInfo.setLogAbs("Authentication failed");
            logInfo.setOpResult("failure");
        }
        service.writeSecurityLogSync(logInfo);
    }
}
