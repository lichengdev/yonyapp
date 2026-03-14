package nccloud.web.baseapp.verfiy.login;

import com.alibaba.fastjson.JSON;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import nc.bcmanage.vo.BusiCenterVO;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.framework.server.ServerConfiguration;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.pub.operatelog.itf.IOperatelogService;
import nc.itf.org.IPowerOrgQryService;
import nc.itf.uap.cil.ICilService;
import nc.login.bs.IServerEnvironmentService;
import nc.login.vo.NCSession;
import nc.pubitf.para.LicForUnLogin;
import nc.security.NCAuthenticatorToolkit;
import nc.security.VerifyFactory;
import nc.security.itf.IVerify;
import nc.security.vo.CAContext;
import nc.security.vo.CARegisterCenter;
import nc.secutity.toolkit.AuthenticatorResultMessage;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.GroupVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFTime;
import nc.vo.pub.operatelog.OperateLogVO;
import nc.vo.sm.UserRefreshTokenVO;
import nc.vo.sm.UserVO;
import nccloud.baseapp.core.log.NCCForUAPLogger;
import nccloud.core.baseapp.ModelServerConfig;
import nccloud.core.baseapp.vo.AuthUserVO;
import nccloud.dto.riart.login.LoginConst;
import nccloud.dto.riart.login.ResultProcess;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.container.ClientInfo;
import nccloud.framework.web.container.SessionContext;
import nccloud.pubitf.baseapp.login.IIntalizeLoginClass;
import nccloud.pubitf.baseapp.login.IUserFirVerify;
import nccloud.pubitf.baseapp.login.IUserLoginHandler;
import nccloud.pubitf.baseapp.login.IUserSecVerfiy;
import nccloud.pubitf.baseapp.oauth.IUserRefreshTokenService;
import nccloud.vo.util.MultiLangNameUtil;
import org.apache.commons.codec.binary.Base64;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;

public class ComLoginHandler
  implements IUserLoginHandler
{
  private AuthUserVO auth;
  private BusiCenterVO bcVO;
  private UserVO user;
  private ClientInfo info;
  
  @Deprecated
  public Map<String, String> doAuth(AuthUserVO vo)
    throws BusinessException
  {
    return null;
  }
  
  public ResultProcess doAuthority(AuthUserVO vo)
    throws BusinessException
  {
    ResultProcess result = null;
    this.auth = vo;
    this.info = new ClientInfo();
    this.info.setNeedBind(Boolean.valueOf(false));
    InvocationInfoProxy.getInstance().setLangCode(vo.getLangCode());
    
    result = verfiyBusiCenter();
    if (!result.isSuceess()) {
      return result;
    }
    result = verfiyUser();
    if (!result.isSuceess())
    {
      dealLoginFail(this.user, this.auth, result.getRslMsg());
      return result;
    }
    if (this.auth.isNeedFirstverify())
    {
      result = doFirstVerfiy();
      if (!result.isSuceess())
      {
        dealLoginFail(this.user, this.auth, result.getRslMsg());
        return result;
      }
    }
    if ((vo.isNeedSecondverify()) && (
      (this.user.getSecondverify() != null) || ("ncca".equals(this.user.getIdentityverifycode()))))
    {
      result = doSecondVerify();
      if (!result.isSuceess())
      {
        dealLoginFail(this.user, this.auth, result.getRslMsg());
        return result;
      }
    }
    if ((vo.isNeedagreement()) && (LicForUnLogin.getInstance().isCloud() == 1))
    {
      result = doAgreementCheck();
      if (!result.isSuceess()) {
        return result;
      }
    }
    // 破解
    if ((vo.isNeedFirstverify()) && (!PubEnvUtil.equals("develop", System.getProperty("nc.runMode"))))
    {
      result = VerfiyTool.doPasswordCheck(this.user, this.auth);
      String msg = "当前登录用户：code[" + this.user.getUser_code() + "]，name[" + this.user.getUser_name() + "]，登录时间：" + 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      if (!result.isSuceess())
      {
        LoggerUtil.getInstance("loginLogs").info(msg);
        if (LoginConst.RESET_PASSWORD.equals(result.getRslCode())) {
          result.setDsName(this.bcVO.getDataSourceName());
        }
        dealLoginFail(this.user, this.auth, result.getRslMsg());
        
        return result;
      }
    }
    initSession();
    
    result = regOnlineUser();
    if (!result.isSuceess())
    {
      dealLoginFail(this.user, this.auth, result.getRslMsg());
      return result;
    }
    LoginSuccessLog();
    result = ResultProcess.success();
    result.setIndex(ModelServerConfig.getProp("LOGIN", "INDEXURL"));
    return result;
  }
  
  private ResultProcess verfiyBusiCenter()
    throws BusinessException
  {
    this.bcVO = VerfiyTool.findBusiCenter(this.auth.getBusiCenterCode());
    if (this.bcVO == null) {
      return ResultProcess.error(VerfiyTool.convertMsg(25));
    }
    int rs = VerfiyTool.checkBusiCenter(this.bcVO);
    if (rs != 21) {
      return ResultProcess.error(VerfiyTool.convertMsg(rs));
    }
    this.auth.setDsname(this.bcVO.getDataSourceName());
    return ResultProcess.success();
  }
  
  public ResultProcess verfiyUser()
    throws BusinessException
  {
    String dsName = this.auth.getDsname();
    InvocationInfoProxy.getInstance().setUserDataSource(dsName);
    int result = -1;
    
    this.user = VerfiyTool.findUser(dsName, this.auth.getUserCode());
    if (this.user == null) {
      return ResultProcess.error(VerfiyTool.convertMsg(2));
    }
    this.auth.setUsertype(this.user.getUser_type().intValue());
    
    result = VerfiyTool.checkUser(this.user);
    if (result != 1) {
      return ResultProcess.error(VerfiyTool.convertMsg(result));
    }
    result = VerfiyTool.checkClient(this.user, this.auth.getClientIP());
    if (result != 1) {
      return ResultProcess.error(VerfiyTool.convertMsg(result));
    }
    if (this.user.getUser_type().intValue() != 3)
    {
      result = VerfiyTool.identifyUserIsAutoLock(this.user);
      if (result == 4) {
        return ResultProcess.error(VerfiyTool.convertMsg(result));
      }
    }
    return ResultProcess.success();
  }
  
  private ResultProcess doFirstVerfiy()
    throws BusinessException
  {
    if (("staticpwd".equals(this.user.getIdentityverifycode())) || 
      ("ncca".equals(this.user.getIdentityverifycode()))) {
      return VerfiyTool.doStaticPWDVerify(this.auth, this.user);
    }
    Properties classprop = VerifyConfAccessor.getInstance().getFirstclassprop();
    String classname = (String)classprop.get(this.user.getIdentityverifycode());
    IIntalizeLoginClass intalizer = (IIntalizeLoginClass)ServiceLocator.find(IIntalizeLoginClass.class);
    IUserFirVerify verify = (IUserFirVerify)intalizer.newInstance(IUserFirVerify.class, classname);
    Map<String, String> map = verify.doVerfiy(this.user, this.auth, this.bcVO.getDataSourceName());
    String resultStr = JSON.toJSONString(map);
    return resultStr != null ? (ResultProcess)JSON.parseObject(resultStr, ResultProcess.class) : 
      ResultProcess.error(classname + "has no response");
  }
  
  public ResultProcess doSecondVerify()
    throws BusinessException
  {
    String classname = "";
    if ("ncca".equals(this.user.getIdentityverifycode())) {
      return doCAVerify();
    }
    Properties classprop = VerifyConfAccessor.getInstance().getClassprop();
    classname = (String)classprop.get(this.user.getSecondverify());
    
    IIntalizeLoginClass intalizer = (IIntalizeLoginClass)ServiceLocator.find(IIntalizeLoginClass.class);
    IUserSecVerfiy verify = (IUserSecVerfiy)intalizer.newInstance(IUserSecVerfiy.class, classname);
    Map<String, String> map = verify.doVerfiy(this.user, this.auth);
    String resultStr = JSON.toJSONString(map);
    return resultStr != null ? (ResultProcess)JSON.parseObject(resultStr, ResultProcess.class) : 
      ResultProcess.error(classname + "has no response");
  }
  
  public ResultProcess doAgreementCheck()
    throws BusinessException
  {
    if ((this.user.getAgreementstatus() != null) && ("1.0".equals(this.user.getAgreementstatus()))) {
      return ResultProcess.success();
    }
    if ("1.0".equals(this.auth.getAgreementstatus()))
    {
      this.user.setAgreementstatus(this.auth.getAgreementstatus());
      BaseDAO dao = new BaseDAO(this.bcVO.getDataSourceName());
      dao.updateVO(this.user);
      return ResultProcess.success();
    }
    return ResultProcess.msg(LoginConst.AGREEMENT_STATUS, "");
  }
  
  public void initSession()
    throws BusinessException
  {
    this.info.setUsercode(this.user.getUser_code());
    this.info.setUserid(this.user.getCuserid());
    this.info.setUserType(this.user.getUser_type());
    this.info.setBusicentercode(this.bcVO.getCode());
    this.info.setClientip(this.auth.getClientIP());
    this.info.setDeviceId(String.valueOf(this.auth.getDeviceType()));
    this.info.setLangcode(this.auth.getLangCode());
    String projectcode = System.getProperty("projectcode");
    this.info.setProjectcode(projectcode);
    this.info.setDatasource(this.bcVO.getDataSourceName());
    
    InvocationInfoProxy.getInstance().setUserCode(this.user.getUser_code());
    ISecurityTokenCallback sc = (ISecurityTokenCallback)ServiceLocator.find(ISecurityTokenCallback.class);
    byte[] token = sc.token("7".getBytes(), this.user.getUser_code().getBytes());
    this.info.setToken(token);
    SessionContext.getInstance().setClientInfo(this.info);
    IPowerOrgQryService powerQry = (IPowerOrgQryService)ServiceLocator.find(IPowerOrgQryService.class);
    GroupVO[] groups = powerQry.getGroupVOsByUserID(this.user.getCuserid());
    GroupVO groupVO = null;
    String user_belongGroup = this.user.getPk_group();
    int count = groups == null ? 0 : groups.length;
    for (int i = 0; i < count; i++) {
      if (groups[i].getPk_group().equals(user_belongGroup))
      {
        groupVO = groups[i];
        break;
      }
    }
    if (this.user.getUser_type().intValue() == 2)
    {
      this.info.setPk_group("0001");
      this.info.setGroupNumber("0001");
    }
    else
    {
      this.info.setNeedBind(Boolean.valueOf(checkNeedBind()));
      this.info.setPk_group(this.user.getPk_group());
      this.info.setGroupNumber(groupVO == null ? "0001" : groupVO.getGroupno());
      this.info.setGroupname(groupVO == null ? "" : groupVO.getName());
      this.info.setTenantid(groupVO == null ? "" : groupVO.getTenantid());
    }
    this.info.setUsername(MultiLangNameUtil.getNameByMultiLang(this.user, "user_name"));
    
    SessionContext.getInstance().setClientInfo(this.info);
  }
  
  private boolean checkNeedBind()
    throws DAOException
  {
    if (this.user.getBase_doc_type().intValue() != 0) {
      return false;
    }
    ICilService service = (ICilService)ServiceLocator.find(ICilService.class);
    int flag = service.getLicenseCount("REGISTMODE");
    if (flag == 1)
    {
      this.info.setIs_cloud_register(Boolean.TRUE);
      IUserRefreshTokenService tokenService = 
        (IUserRefreshTokenService)ServiceLocator.find(IUserRefreshTokenService.class);
      UserRefreshTokenVO tokenVO = tokenService.getVOByUserId(this.user.getCuserid());
      if (tokenVO == null) {
        return true;
      }
    }
    return false;
  }
  
  public ResultProcess regOnlineUser()
    throws BusinessException
  {
    int result = 0;
    
    String dsName = InvocationInfoProxy.getInstance().getUserDataSource();
    
    String srvName = ServerConfiguration.getServerConfiguration().getServerName();
    NCSession session = new NCSession();
    session.setDsName(dsName);
    session.setSessionID(this.auth.getSessionID());
    session.setUserCode(this.info.getUsercode());
    session.setUserID(this.info.getUserid());
    session.setUserName(this.info.getUsername());
    session.setGroupPK(this.info.getPk_group());
    session.setBusiCenterCode(this.bcVO.getCode());
    session.setBusiCenterName(this.bcVO.getName());
    IPowerOrgQryService powerQry = (IPowerOrgQryService)ServiceLocator.find(IPowerOrgQryService.class);
    GroupVO[] groups = powerQry.getGroupVOsByUserID(this.info.getUserid());
    GroupVO groupVO = null;
    String user_belongGroup = this.info.getPk_group();
    int count = groups == null ? 0 : groups.length;
    for (int i = 0; i < count; i++) {
      if (groups[i].getPk_group().equals(user_belongGroup))
      {
        groupVO = groups[i];
        break;
      }
    }
    if ((this.user.getUser_type().intValue() != 2) && (groupVO == null)) {
      return ResultProcess.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("rbac", "0RBAC0066"));
    }
    session.setGroupCode(groupVO == null ? null : groupVO.getCode());
    session.setGroupName(groupVO == null ? null : groupVO.getName());
    session.setClientHostIP(this.info.getClientip());
    session.setServerHostName(srvName);
    session.setUserType(this.user.getUser_type().intValue());
    try
    {
      result = VerfiyTool.getServerEnvironmentServiceInstance().registerUserSession("7", session, 
        this.auth.isForcelogin());
    }
    catch (BusinessException e)
    {
      NCCForUAPLogger.error("registe online user failed", e);
    }
    if (result == 5) {
      return ResultProcess.msg("5", 
        NCLangResOnserver.getInstance().getStrByID("sysframev5", "UPPsysframev5-000059"));
    }
    return ResultProcess.success();
  }
  
  private void LoginSuccessLog()
  {
    OperateLogVO logVO = new OperateLogVO();
    logVO.setDevice("1");
    logVO.setLogintype("7");
    logVO.setType(Integer.valueOf(0));
    logVO.setEntersystemresult(Integer.valueOf(0));
    logVO.setDetail("login success");
    logVO.setLogdate(new UFDate());
    logVO.setLogtime(new UFTime());
    logVO.setIp(this.info.getClientip());
    logVO.setPk_user(this.info.getUserid());
    logVO.setUser_name(this.user.getUser_name());
    logVO.setUsertype(this.user.getUser_type());
    logVO.setPk_group(this.info.getPk_group());
    try
    {
      IOperatelogService logService = 
        (IOperatelogService)NCLocator.getInstance().lookup(IOperatelogService.class);
      logService.insertVO(logVO, this.info.getDatasource());
    }
    catch (Exception e)
    {
      Logger.error(e.getMessage(), e);
    }
  }
  
  private void dealLoginFail(UserVO uservo, AuthUserVO vo, String msg)
  {
    if (uservo == null) {
      return;
    }
    OperateLogVO logVO = new OperateLogVO();
    
    logVO.setDevice("1");
    logVO.setLogintype("7");
    logVO.setType(Integer.valueOf(0));
    logVO.setEntersystemresult(Integer.valueOf(1));
    logVO.setDetail("login failure:" + msg);
    logVO.setLogdate(new UFDate());
    logVO.setLogtime(new UFTime());
    logVO.setIp(vo.getClientIP());
    
    logVO.setPk_user(uservo.getCuserid());
    logVO.setPk_group(uservo.getPk_group());
    logVO.setUser_name(vo.getUserCode());
    logVO.setUsertype(Integer.valueOf(vo.getUsertype()));
    IOperatelogService logService = (IOperatelogService)ServiceLocator.find(IOperatelogService.class);
    try
    {
      logService.insertVO(logVO, vo.getDsname());
    }
    catch (BusinessException e)
    {
      Logger.error(e.getMessage(), e);
    }
  }
  
  public ResultProcess doCAVerify()
  {
    int UNKNOWN_ERROR = 1;
    int LOGIN_LEGALIDENTITY = 2;
    if ("ncca".equals(this.user.getIdentityverifycode()))
    {
      String signdata = this.auth.getP_signdata();
      String sn = this.auth.getP_sn();
      if ((signdata == null) || ("".equals(signdata)))
      {
        ResultProcess result = ResultProcess.msg("4", "");
        result.setChllid(this.auth.getSessionID());
        return result;
      }
      String challlid = this.auth.getSessionID();
      int result = UNKNOWN_ERROR;
      boolean isSucc = false;
      String msg = "CA验证失败";
      try
      {
        String caRegEntryId = NCAuthenticatorToolkit.getCARegisterCenter().getLoginProviderID();
        
        byte[] signBytes = Base64.decodeBase64(signdata);
        
        CAContext context = new CAContext(this.user.getPrimaryKey(), this.user.getUser_code(), 
          NCAuthenticatorToolkit.getCARegisterCenter().getCARegEntryByID(caRegEntryId));
        IVerify verify = VerifyFactory.createVerify(context);
        int verifyValue = verify.verify(sn, challlid.getBytes(), signBytes);
        if (201 == verifyValue) {
          isSucc = true;
        }
        msg = AuthenticatorResultMessage.getMLResultMessage(verifyValue);
      }
      catch (SecurityException e)
      {
        NCCForUAPLogger.error(e);
      }
      catch (Exception e)
      {
        NCCForUAPLogger.error(e);
      }
      if (isSucc) {
        result = LOGIN_LEGALIDENTITY;
      } else {
        result = UNKNOWN_ERROR;
      }
      if (LOGIN_LEGALIDENTITY != result) {
        return ResultProcess.error(msg);
      }
    }
    return ResultProcess.success();
  }
}
