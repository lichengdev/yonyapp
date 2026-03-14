package nc.login.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class DefaultLoginUILayout implements LayoutManager
{
    private LoginUISupport uiSupport = null;
    
    int hgap = 19;
    int vgap = 11;
    
    public DefaultLoginUILayout(LoginUISupport support)
    {
        super();
        this.uiSupport = support;
    }
    
    public void addLayoutComponent(String name, Component comp)
    {
        
    }
    
    public void layoutContainer(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Insets insets = parent.getInsets();
            Dimension parentDim = parent.getSize();
            ImageIcon centerIcon = LoginUIConfig.getInstance().getCenterIcon();
            Rectangle centerIconRect = new Rectangle();
            centerIconRect.x = (parentDim.width - centerIcon.getIconWidth()) / 2;
            centerIconRect.y = (parentDim.height - centerIcon.getIconHeight()) / 3;
            centerIconRect.width = centerIcon.getIconWidth();
            centerIconRect.height = centerIcon.getIconHeight();
            Rectangle centerRect = uiSupport.calculateLoginCenterSize(parent, hgap, vgap);
            int compsLeft = centerRect.x;
            int compsTop = centerRect.y;
            int x = compsLeft;
            int y = compsTop - 25; // xull3 for captcha
            Component comp = null;
            
            int maxW = 0;
            Component[] lblComps =
                {uiSupport.getLblBusiCenter(), uiSupport.getLblUserCode(), uiSupport.getLblPWD()/* xull3 */, uiSupport.getLblCaptcha()};
            Component[] ctrlComps =
                {uiSupport.getCbbBusiCenter(), uiSupport.getTfUserCode(), uiSupport.getPfUserPWD()/* xull3 */, uiSupport.getPnlCaptcha()};
            for (int i = 0; i < lblComps.length; i++)
            {
                JLabel lblComp = (JLabel) lblComps[i];
                Component ctrlComp = ctrlComps[i];
                if (LoginUISupport.needLayout(parent, lblComp))
                {
                    Dimension lblDim = lblComp.getPreferredSize();
                    Dimension ctrlDim = ctrlComp.getPreferredSize();
                    int hDelta = ctrlDim.height - lblDim.height;
                    int lbly = hDelta < 0 ? y : y + hDelta / 2;
                    lblComp.setBounds(x, lbly, lblDim.width, lblDim.height);
                    y += (hDelta < 0 ? lblDim.height : ctrlDim.height) + vgap;
                    if (maxW < lblDim.width)
                    {
                        maxW = lblDim.width;
                    }
                }
            }
            x += maxW + hgap;
            y = insets.top + compsTop - 25;
            for (int i = 0; i < ctrlComps.length; i++)
            {
                Component lblComp = lblComps[i];
                Component ctrlComp = ctrlComps[i];
                if (LoginUISupport.needLayout(parent, ctrlComp))
                {
                    Dimension lblDim = lblComp.getPreferredSize();
                    Dimension ctrlDim = ctrlComp.getPreferredSize();
                    int hDelta = ctrlDim.height - lblDim.height;
                    int ctrly = hDelta < 0 ? y + hDelta / 2 : y;
                    ctrlComp.setBounds(x, ctrly, ctrlDim.width, ctrlDim.height);
                    y += (hDelta < 0 ? lblDim.height : ctrlDim.height) + vgap;
                }
            }
            
            // 20231209 satrt
            // 新华社财务资产管理系统
            uiSupport.getFinance().setBounds(x - 138, y + 45, 120, 30);
            // 移动办公平台下载
            uiSupport.getOffice().setBounds(x, y + 45, 120, 30);
            // 用户须知
            uiSupport.getUserNotice().setBounds(x + 138, y + 45, 120, 30);
            // end
            // // 20231209 satrt
            // // 新华社财务资产管理系统
            // uiSupport.getFinance().setBounds(x - 138, y + 45, 120, 30);
            // // 移动办公平台下载
            // uiSupport.getOffice().setBounds(x- 138, y + 75, 120, 30);
            // // 用户须知
            // uiSupport.getUserNotice().setBounds(x - 138 , y+105, 120, 30);
            // // end
            // 忘记密码
            uiSupport.getForgetPWD().setBounds(x + 138, y - 14, 120, 40);
            /////
            comp = uiSupport.getLblResultMessage();
            if (LoginUISupport.needLayout(parent, comp))
            {
                y = y - vgap + 20;
                Dimension compDim = comp.getPreferredSize();
                comp.setBounds(x, y, compDim.width, compDim.height);
            }
            // y = y-vgap+12;
            Component loginBtn = uiSupport.getBtnLogin();
            Dimension btnDim = loginBtn.getPreferredSize();
            // x = x + (uiSupport.getPfUserPWD().getPreferredSize().width - btnDim.width)/2;
            x = centerIconRect.x + 445;
            y = centerIconRect.y + 83;
            loginBtn.setBounds(x, y, btnDim.width, btnDim.height);
            // Component[] btnComps = {uiSupport.getBtnLogin(),uiSupport.getBtnOption()};//
            // for (int i = 0; i < btnComps.length; i++) {
            // Component btnComp = btnComps[i];
            // if(LoginUISupport.needLayout(parent, btnComp)){
            // Dimension btnDim = btnComp.getPreferredSize();
            // btnComp.setBounds(x, y,btnDim.width ,btnDim.height);
            // x += btnDim.width+hgap;
            // }
            // }
            
            y = uiSupport.getBtnLogin().getLocation().y + uiSupport.getBtnLogin().getPreferredSize().height + 25;
            comp = uiSupport.getOptionPanel();
            if (LoginUISupport.needLayout(parent, comp))
            {
                Dimension compDim = comp.getPreferredSize();
                x = uiSupport.getBtnLogin().getLocation().x;
                comp.setBounds(x, y, compDim.width, compDim.height);
                y += compDim.height + vgap;
            }
            
            ////////
            comp = uiSupport.getLblLoginFlash();
            if (LoginUISupport.needLayout(parent, comp))
            {
                Dimension compDim = comp.getPreferredSize();
                Rectangle loginRect = uiSupport.getBtnLogin().getBounds();
                x = loginRect.x + (loginRect.width - compDim.width) / 2;
                y = loginRect.y + 60;
                // x = centerIconRect.x + centerIconRect.width/2 - compDim.width/2;
                // y = centerIconRect.y + centerIconRect.height + 20;
                
                comp.setBounds(x, y, compDim.width, compDim.height);
            }
            
            ///////////
            Component[] comps = {uiSupport.getMenuOption(), uiSupport.getCbbLanguage()};
            x = parentDim.width - 42;
            for (int i = 0; i < comps.length; i++)
            {
                comp = comps[i];
                Dimension compSize = comp.getPreferredSize();
                x -= compSize.width;
                comp.setBounds(x, 22, compSize.width, compSize.height);
                x -= 8;
            }
            // Component langComp = uiSupport.getCbbLanguage();
            // Dimension langDim = langComp.getPreferredSize();
            // langComp.setBounds(parentDim.width - langDim.width-50, 22, langDim.width, langDim.height);
            /////
            Component lblCopyright = uiSupport.getLblCopyright();
            Dimension lblCopyrightDim = lblCopyright.getPreferredSize();
            lblCopyright.setBounds(19, parentDim.height - 17 - lblCopyrightDim.height, lblCopyrightDim.width, lblCopyrightDim.height);
            
            comp = uiSupport.getWebStartPanel();
            if (LoginUISupport.needLayout(parent, comp))
            {
                Dimension compSize = comp.getPreferredSize();
                int compX = (parentDim.width - compSize.width) / 2;
                int compY = parentDim.height - compSize.height;
                comp.setBounds(compX, compY, compSize.width, compSize.height);
            }
        }
        
    }
    
    public Dimension minimumLayoutSize(Container parent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Dimension preferredLayoutSize(Container parent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void removeLayoutComponent(Component comp)
    {
        // TODO Auto-generated method stub
        
    }
    
}
