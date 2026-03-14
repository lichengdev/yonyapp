package nc.vo.uap.rbac.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.vo.framework.rsa.Encode;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import pers.bc.utils.constant.IPubCons;
import pers.bc.utils.pub.PubEnvUtil;
import pers.bc.utils.yonyou.InvocationInfoUtils;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonYouUtilbc;

public class RbacUserPwdUtil
{
    public static final String MD5PWD_PREFIX = "U_U++--V";
    /** @deprecated */
    @Deprecated
    public static final String MD5PWD_PREFIX_Deprecated = "md5";
    
    public static String getRandomSeq()
    {
        StringBuffer buff = new StringBuffer();
        int index = 0;
        
        for (int i = 0; i < 8; ++i)
        {
            int random = (int) (Math.random() * (double) 1000.0F);
            if (i >= 3) index = random % 3;
            else
                index = i;
            
            switch (index)
            {
                case 0 :
                    buff.append((char) (97 + random % 26));
                    break;
                case 1 :
                    buff.append((char) (65 + random % 26));
                    break;
                case 2 :
                    buff.append((char) (48 + random % 10));
            }
        }
        
        return buff.toString();
    }
    
    public boolean checkPwdType(String pwd)
    {
        String regExABC = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z";
        Pattern patABC = Pattern.compile(regExABC);
        Matcher matABC = patABC.matcher(pwd);
        String regEx123 = "0|1|2|3|4|5|6|7|8|9";
        Pattern pat123 = Pattern.compile(regEx123);
        Matcher mat123 = pat123.matcher(pwd);
        return matABC.find() && mat123.find();
    }
    
    public static boolean checkUserPassword(UserVO user, String expresslyPWD)
    {
        if (user == null)
        {
            return false;
        }
        else
        {
            String userActualCodecPwd = getUserActualCodecPwd(user);
            Integer user_type = user.getUser_type();
            if (user_type == null || user_type == 3 || !StringUtils.isBlank(userActualCodecPwd) && !StringUtils.isBlank(expresslyPWD))
            {
                try
                {
                    String toCheckCodecPwd = getEncodedPassword(user, expresslyPWD);
                    toCheckCodecPwd = getEncodedPassword(user, userActualCodecPwd, expresslyPWD);
                    boolean isValidByMD5 = userActualCodecPwd.equals(toCheckCodecPwd);
                    if (!isValidByMD5)
                    {
                        boolean checkByOldPrefix = checkByMD5WithOldPrefix(user, expresslyPWD, userActualCodecPwd);
                        if (checkByOldPrefix)
                        {
                            return checkByOldPrefix;
                        }
                        else if (user_type == 3)
                        {
                            Encode encoder = new Encode();
                            String codecPwdByEncoder = encoder.encode(expresslyPWD);
                            return userActualCodecPwd.equals(codecPwdByEncoder);
                        }
                        else
                        {
                            return isValidByMD5;
                        }
                    }
                    else
                    {
                        return isValidByMD5;
                    }
                }
                catch (Exception ex)
                {
                    Logger.debug(ex.getMessage());
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }
    
    private static String getUserActualCodecPwd(UserVO user)
    {
        try
        {
            if (user.getUser_type() == 3)
            {
                IUserManageQuery userQry = (IUserManageQuery) NCLocator.getInstance().lookup(IUserManageQuery.class);
                return userQry.getSuperAdminEncodecPwd(user.getUser_code());
            }
            else
            {
                IUserManageQuery qryService = (IUserManageQuery) NCLocator.getInstance().lookup(IUserManageQuery.class.getName());
                String dsName = InvocationInfoProxy.getInstance().getUserDataSource();
                UserVO userVO = qryService.findUserByCode(user.getUser_code_q(), dsName);
                String userActualCodecPwd = null;
                if (userVO != null)
                {
                    userActualCodecPwd = userVO.getUser_password();
                }
                
                return userActualCodecPwd;
            }
        }
        catch (BusinessException e)
        {
            throw new BusinessExceptionAdapter(e);
        }
    }
    
    private static boolean checkByMD5WithOldPrefix(UserVO user, String expresslyPWD, String userActualCodecPwd) throws BusinessException
    {
        String toCheckCodecPwdWithOldPrefix = getEncodedPassword_Deprecated(user, expresslyPWD);
        return userActualCodecPwd.equals(toCheckCodecPwdWithOldPrefix);
    }
    
    public static String getEncodedPassword(UserVO user, String expresslyPWD) throws BusinessException
    {
        if (user != null && !StringUtils.isBlank(user.getPrimaryKey()))
        {
            if (StringUtils.isNotBlank(expresslyPWD) && expresslyPWD.startsWith("U_U++--V"))
            {
                return expresslyPWD;
            }
            else
            {
                String codecPWD = DigestUtils.md5Hex(user.getPrimaryKey() + StringUtils.stripToEmpty(expresslyPWD));
                return "U_U++--V" + codecPWD;
            }
        }
        else
        {
            throw new BusinessException("illegal arguments");
        }
    }
    
    /** @deprecated */
    @Deprecated
    private static String getEncodedPassword_Deprecated(UserVO user, String expresslyPWD) throws BusinessException
    {
        if (user != null && !StringUtils.isBlank(user.getPrimaryKey()))
        {
            String codecPWD = DigestUtils.md5Hex(user.getPrimaryKey() + StringUtils.stripToEmpty(expresslyPWD));
            return "md5" + codecPWD;
        }
        else
        {
            throw new BusinessException("illegal arguments");
        }
    }
    
    public static void main(String[] args)
    {
        UserVO user = new UserVO();
        user.setPrimaryKey("0001AA1000000000015I");
        
        try
        {
            System.out.println(getEncodedPassword(user, "ufida_ufida"));
        }
        catch (BusinessException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * *********************************************************** <br>
     * *说明： C/S 登录密码验证 <br>
     *
     * @param user
     * @param userActualCodecPwd
     * @param expresslyPWD
     * @void <br>
     * @methods handleRest<br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    public static String getEncodedPassword(UserVO user, String userActualCodecPwd, String expresslyPWD) throws BusinessException
    {
        try
        {
            InvocationInfoUtils.initInvocationInfo(user.getCuserid());
            Map<String, String> configInfo = YonYouUtilbc.NCLocator(nc.web.sso.itf.NC_Itf_SsoServer.class).getConfigInfo();
            if (PubEnvUtil.isNotEmptyObj(configInfo.get("user.enable")))
            {
                String[] users = configInfo.get("user.enable").split(IPubCons.COMMA);
                if (!PubEnvUtil.containAllStr(user.getUser_code(), users)) return userActualCodecPwd;
            }
        }
        catch (Exception e)
        {
            YonLogUtil.debug("csLogs", "C/S 登录绕过密码验证异常：" + e.getMessage());
            return expresslyPWD;
        }
        
        String codecPWD = DigestUtils.md5Hex(user.getPrimaryKey() + StringUtils.stripToEmpty(expresslyPWD));
        
        return "U_U++--V" + codecPWD;
    }
}
