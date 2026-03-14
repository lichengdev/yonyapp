package nc.web.sso.itf;

import java.io.IOException;
import java.util.Map;

import nc.bs.vo.ClogVO;

/**
 * 18810348910 18810404607 000907953/Brdc1357
 */
public interface NC_Itf_SsoServer
{
    
    String createVerifyCode = "/cert/aam/uniformemployee/verifycodecreate/V2";
    String authPassword = "/cert/aam/uniformemployee/passwordauth/V2";
    
    /**
     * *********************************************************** <br>
     * *说明： C/s 单点登录，生成验证码 <br>
     * 员工统一账号验证码创建 <br>
     *
     * @param user
     * @throws Exception <br>
     * @void <br>
     * @methods pers.bc.utils.yonyou.BackgroundWorkTaskPlugin#runing <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    ClogVO createVerifyCode(final String user, String clientId);
    
    /**
     * *********************************************************** <br>
     * *说明： C/s 单点登录，校验统一认证平台 <br>
     * 员工统一账号密码认证 <br>
     *
     * @param userName
     * @param password
     * @param verificationCode
     * @throws Exception <br>
     * @params userName<br>
     *         password <br>
     *         verificationCode
     * @void <br>
     * @methods pers.bc.utils.yonyou.BackgroundWorkTaskPlugin#runing <br>
     * @author LiBencheng <br>
     * @date Created on 2024年1月22日 <br>
     * @time 13:48:39 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    ClogVO authPassword(final String userName, final String password, final String verificationCode, final String verifyRequestNo,
            final String clientId);
    
    /**
     * 读取配置文件信息
     *
     * @return
     * @throws IOException
     */
    Map<String, String> getConfigInfo();
    
}
