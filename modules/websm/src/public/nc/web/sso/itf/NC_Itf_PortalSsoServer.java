package nc.web.sso.itf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.vo.ClogVO;

public interface NC_Itf_PortalSsoServer
{
    String testString();
    
    /**
     * *********************************************************** <br>
     * *说明： B/S ncportal 单点登录，校验统一认证平台 <br>
     * http://82.219.253.40:7777/portal 001028718/Brdc13571
     * 原行内网关域名调用方式由http://gw.inner.icbc.com.cn:8081调整为http://gateway.internal.icbc:8081，
     * 并申请开通到76.196.81.117:8081（嘉定）、84.196.113.109:8081（外高桥）的火墙。
     *
     * @param req
     * @param resp
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
    ClogVO cheeckThirdPartyAccess(HttpServletRequest req, HttpServletResponse resp);
    
}
