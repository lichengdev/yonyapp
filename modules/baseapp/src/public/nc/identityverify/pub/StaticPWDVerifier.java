package nc.identityverify.pub;

import nc.identityverify.itf.AbstractIdentityVerifier;
import nc.identityverify.vo.AuthenSubject;
import nc.login.vo.ILoginConstants;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.util.RbacUserPwdUtil;
import pers.bc.utils.pub.LoggerUtil;
import pers.bc.utils.pub.PubEnvUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StaticPWDVerifier extends AbstractIdentityVerifier {

    @Override
    public int verify(AuthenSubject subject, UserVO user) throws Exception {

        if (user != null) {
//            if (PubEnvUtil.equals("develop", System.getProperty("nc.runMode"))) {
//                String msg = "当前登录用户：code[" + user.getUser_code() + "]，name[" + user.getUser_name() + "]，登录时间：" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
//                System.err.println(msg);
//                LoggerUtil.getInstance("loginLogs").info(msg);
//                return ILoginConstants.USER_IDENTITY_LEGAL;
//            } else {
                return RbacUserPwdUtil.checkUserPassword(user, subject.getUserPWD()) ? ILoginConstants.USER_IDENTITY_LEGAL : ILoginConstants.USER_NAME_RIGHT_PWD_WRONG;
//            }
        } else {
            return ILoginConstants.USER_NAME_WRONG;
        }

//        if (PubEnvUtil.isNotEmptyObj(subject.getVeritfCode())) {
//            String veritfCode = subject.getVeritfCode();
//            //
//            if (user != null) {
//                if (RbacUserPwdUtil.checkUserPassword(user, subject.getUserPWD())) {// 身份合法
//                    return ILoginConstants.USER_IDENTITY_LEGAL;
//
//                } else {// 密码错误，身份不合法.
//                    return ILoginConstants.USER_NAME_RIGHT_PWD_WRONG;
//                }
//            } else { // 说明用户名称错误
//                return ILoginConstants.USER_NAME_WRONG;
//            }
//
//        } else { // 说明用户名称错误
//            return ILoginConstants.USER_NAME_WRONG;
//        }
    }
}
