package nc.login.ui;

import com.dl.ui.CapsLockHintRegister;
import nc.bcmanage.vo.BusiCenterVO;
import nc.bs.framework.common.*;
import nc.bs.framework.comn.NetStreamConstants;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.framework.util.KeyUtil;
import nc.bs.logging.Log;
import nc.bs.logging.Logger;
import nc.bs.vo.ApiLogUtil;
import nc.bs.vo.ClogVO;
import nc.desktop.ui.actions.AbstractWorkbenchAction;
import nc.itf.uap.sfapp.SecurityLogInfo;
import nc.itf.uap.sfapp.SecurityLogVarItem;
import nc.itf.uap.sfapp.securityLog.SecurityLogManageFacade;
import nc.login.bs.LoginVerifyException;
import nc.login.ui.webstart.FlyPanel;
import nc.login.ui.webstart.WebStartAction;
import nc.login.ui.webstart.WebStartPanel;
import nc.login.vo.ILoginConstants;
import nc.login.vo.LoginRequest;
import nc.login.vo.LoginResponse;
import nc.sfbase.beans.SingleColorArrowIcon;
import nc.sfbase.client.CheckDevice;
import nc.sfbase.client.ClientToolKit;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.wizard.WizardStep;
import nc.ui.pub.style.Style;
import nc.ui.sm.clientsetup.ClientSetup;
import nc.ui.sm.clientsetup.ClientSetupCache;
import nc.uitheme.ui.ThemeResourceCenter;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.IProductCode;
import nc.vo.ml.Language;
import nc.vo.ml.LanguageTranslatorFactor;
import nc.vo.org.GroupVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.guid.UUIDGenerator;
import nc.vo.uap.rbac.profile.FunctionPermProfileManager;
import nc.vo.uap.rbac.profile.IFunctionPermProfileConst;
import nc.web.sso.impl.NC_Impl_SsoServer;
import nc.web.sso.itf.NC_Itf_SsoServer;
import pers.bc.utils.constant.IPubCons;
import pers.bc.utils.constant.InumberCons;
import pers.bc.utils.pub.*;
import pers.bc.utils.yonyou.InvocationInfoUtils;
import pers.bc.utils.yonyou.YonLogUtil;
import pers.bc.utils.yonyou.YonYouUtilbc;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.metal.MetalComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

public class LoginUISupport
{
    
    // 选择的语种
    private Map<String, Language> currLangMap = new HashMap<String, Language>();
    
    public LoginUISupport()
    {
        
    }
    
    public class MLNullTranslatorAction extends AbstractWorkbenchAction
    {
        private static final long serialVersionUID = -7013174837807978293L;
        
        public MLNullTranslatorAction()
        {
            super();
            String name = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000273")/* @res "显示资源ID号" */;
            setName(name);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean check = false;
            Object b = getValue(Action.SELECTED_KEY);
            if (b != null && b instanceof Boolean)
            {
                check = (Boolean) b;
            }
            LanguageTranslatorFactor.getInstance().setNullTrans(check);
            refreshUIContrl();
        }
        
    }
    
    public class CloseWebStartPanelAction extends AbstractAction
    {
        private static final long serialVersionUID = 8802805371561236728L;
        
        public CloseWebStartPanelAction()
        {
            super();
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JComponent parent = (JComponent) getWebStartPanel().getParent();
            if (parent != null)
            {
                ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
                setup.putBoolean("webstartpanel.closed", Boolean.TRUE);
                ClientSetupCache.storeGlobalClientSetup();
                
                JLayeredPane pane = parent.getRootPane().getLayeredPane();
                Rectangle rect = getWebStartPanel().getBounds();
                Rectangle rect2 = getMenuOption().getBounds();
                Point p1 = rect.getLocation();
                Point p2 = rect2.getLocation();
                p2.x += rect2.getWidth() / 2;
                fly(pane, rect.getSize(), p1, p2);
                
            }
            
        }
        
        private BufferedImage createImage(JComponent comp)
        {
            Dimension size = comp.getSize();
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2 = null;
            try
            {
                g2 = img.createGraphics();
                comp.paintComponents(g2);
            }
            finally
            {
                if (g2 != null)
                {
                    g2.dispose();
                }
            }
            return img;
        }
        
        private void fly(final JLayeredPane pane, Dimension size, final Point loc1, Point loc2)
        {
            final int dx = loc2.x - loc1.x;
            final int dy = loc2.y - loc1.y;
            final int dw = size.width;
            final int dh = size.height;
            BufferedImage img = createImage(getWebStartPanel());
            final FlyPanel flyPanel = new FlyPanel(img);
            flyPanel.setBounds(getWebStartPanel().getBounds());
            pane.add(flyPanel, JLayeredPane.POPUP_LAYER);
            loginUIContainer.remove(getWebStartPanel());
            
            SwingWorker<Object, Integer> sw = new SwingWorker<Object, Integer>()
            {
                int count = 100;
                int currValue = 0;
                
                @Override
                protected Object doInBackground() throws Exception
                {
                    while (currValue++ < count)
                    {
                        Thread.sleep(8);
                        publish(Integer.valueOf(currValue));
                    }
                    return null;
                }
                
                @Override
                protected void done()
                {
                    pane.remove(flyPanel);
                }
                
                @Override
                protected void process(List<Integer> chunks)
                {
                    int n = chunks.size();
                    int val = chunks.get(n - 1);
                    Rectangle rect = new Rectangle();
                    rect.x = loc1.x + dx * val / count;
                    rect.y = loc1.y + dy * val / count;
                    rect.width = dw * (count - val) / count;
                    rect.height = dh * (count - val) / count;
                    flyPanel.setBounds(rect);
                    flyPanel.repaint();
                    
                }
                
            };
            
            sw.execute();
            
        }
        
    }
    
    public static class BusiCenterCBBCellRender extends DefaultListCellRenderer
    {
        private static final long serialVersionUID = 6386474428568356047L;
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JLabel && value instanceof BusiCenterVO)
            {
                BusiCenterVO bcVO = (BusiCenterVO) value;
                if (bcVO.getCode().equals("0000"))
                {
                    String text = NCLangRes.getInstance().getString("sfbase", "系统管理",
                        "BusiCenterCache-000001")/* 系统管理 */;/*
                                                             * -= notranslate =-
                                                             */
                    ((JLabel) comp).setText(text);
                }
            }
            return comp;
        }
        
    }
    
    public static class MyTextFieldUI extends BasicTextFieldUI
    {
        private static class FocusHandler implements FocusListener
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                onFocusChange(e);
            }
            
            @Override
            public void focusLost(FocusEvent e)
            {
                onFocusChange(e);
            }
            
            private void onFocusChange(FocusEvent e)
            {
                Component comp = e.getComponent();
                if (comp != null)
                {
                    comp.repaint();
                }
            }
        }
        
        private static FocusHandler focusHandler = new FocusHandler();
        
        public static ComponentUI createUI(JComponent c)
        {
            return new MyTextFieldUI();
        }
        
        @Override
        public void installUI(JComponent c)
        {
            super.installUI(c);
            c.addFocusListener(focusHandler);
        }
        
        @Override
        public void uninstallUI(JComponent c)
        {
            super.uninstallUI(c);
            c.removeFocusListener(focusHandler);
        }
        
    }
    
    private static class ZipRemoteStreamAction extends AbstractAction
    {
        private static final long serialVersionUID = 4640550263604706078L;
        
        public ZipRemoteStreamAction()
        {
            super();
            super.putValue(Action.SELECTED_KEY, isNetStreamZip());
            String name = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000246")/* 压缩远程调用流 */;
            super.putValue(Action.NAME, name);
            
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Boolean isSel = (Boolean) super.getValue(Action.SELECTED_KEY);
            setNetStreamZipProp(isSel);
            ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
            setup.putBoolean("_isNetStreamZip", isSel);
            ClientSetupCache.storeGlobalClientSetup();
            
        }
    }
    
    private class LoginUIFocusTraversalPolicy extends LayoutFocusTraversalPolicy
    {
        private static final long serialVersionUID = -4634909526488382845L;
        
        @Override
        public Component getFirstComponent(Container container)
        {
            String text = getTfUserCode().getText().trim();
            if (text.length() == 0)
            {
                return getTfUserCode();
            }
            else if (!getPfUserPWD().isFocusOwner())
            {
                return getPfUserPWD();
            }
            else
            {
                return super.getFirstComponent(container);
            }
        }
        
        @Override
        public Component getComponentAfter(Container container, Component component)
        {
            int index = focusTraveList.indexOf(component);
            if (index != -1)
            {
                index = index == focusTraveList.size() - 1 ? 0 : index + 1;
                return focusTraveList.get(index);
                
            }
            return super.getComponentAfter(container, component);
        }
        
        @Override
        public Component getComponentBefore(Container container, Component component)
        {
            int index = focusTraveList.indexOf(component);
            if (index != -1)
            {
                index = index == 0 ? focusTraveList.size() - 1 : index - 1;
                return focusTraveList.get(index);
                
            }
            return super.getComponentBefore(container, component);
        }
        
        @Override
        protected boolean accept(Component component)
        {
            return focusTraveList.contains(component);
        }
        
    }
    
    private class KeyHandler extends KeyAdapter
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == 0)
            {
                Object source = e.getSource();
                // xull3 for captcha
                if (source.equals(getPfUserPWD()) || source.equals(getTfCaptcha()))
                {
                    getBtnLogin().doClick();
                }
                else if (source.equals(getTfUserCode()))
                {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                }
            }
        }
    }
    
    private class ItemHandler implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                Object source = e.getSource();
                if (getCbbBusiCenter().equals(source))
                {
                    doCbbBusiCenterSelectChange();
                }
                else if (getCbbLanguage().equals(source))
                {
                    doCbbLanguageSelectChange();
                }
            }
        }
        
    }
    
    private class LoginAction extends AbstractAction
    {
        private static final long serialVersionUID = -8198578873503150213L;
        
        public void actionPerformed(ActionEvent e)
        {
            Runnable run = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        String loginCode = getTfUserCode().getText().trim();
                        if (PubEnvUtil.isEmpty(loginCode))
                        {
                            loginEndInDT(-1, "用户名不能为空！");
                            return;
                        }
                        String loginWord = new String(getPfUserPWD().getPassword()).trim();
                        if (PubEnvUtil.isEmpty(loginWord))
                        {
                            loginEndInDT(-1, "用户密码不能为空！");
                            return;
                        }
                        // 20250610 libc
                        try
                        {
                            // 2）验证（/cert/aam/uniformemployee/passwordauth/V2）
                            // InvocationInfoUtils.initInvocationInfo("#UAP#");
                            
                            NC_Itf_SsoServer ncItfSsoServer = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class);
                            // NC_Itf_SsoServer ncItfSsoServer = new NC_Impl_SsoServer();
                            Map<String, String> configInfo = ncItfSsoServer.getConfigInfo();
                            
                            if (PubEnvUtil.isEmpty(InvocationInfoProxy.getInstance().getUserDataSource()))
                            {
                                InvocationInfo invocationInfo = InvocationInfoUtils.getInvocationInfo();
                                invocationInfo.setUserDataSource(configInfo.get("dataSource"));
                                InvocationInfoProxy.getInstance().setUserDataSource(configInfo.get("dataSource"));
                            }
                            // String sessionid = getSessionid();
                            String enableCsSSO = configInfo.get("enableCsSSO").toLowerCase();
                            Boolean enableSSO = Boolean.TRUE;
                            if (PubEnvUtil.isNotEmptyObj(configInfo.get("user.enable")))
                            {
                                String[] users = configInfo.get("user.enable").split(IPubCons.COMMA);
                                enableSSO = !PubEnvUtil.containAllStr(loginCode, users);
                            }
                            if (enableSSO && (PubEnvUtil.equals("y", enableCsSSO) || PubEnvUtil.equals("true", enableCsSSO)))
                            {
                                String verificationCode = getTfCaptcha().getText().trim();
                                if (PubEnvUtil.isEmpty(verificationCode))
                                {
                                    loginEnd(-2, "验证码不能为空，请获取输入！");
                                    return;
                                }
                                ClogVO clogVO = null;
                                try
                                {
                                    // clogVO = new NC_Impl_SsoServer().authPassword(loginCode, loginWord,
                                    // verificationCode,verifyRequestNo,
                                    // InetAddress.getLocalHost().getHostAddress());
                                    clogVO = ncItfSsoServer.authPassword(loginCode, loginWord, verificationCode, verifyRequestNo,
                                        InetAddress.getLocalHost().getHostAddress());
                                }
                                catch (UnknownHostException e)
                                {
                                    loginEnd(0, e.getMessage());
                                    return;
                                }
                                if (PubEnvUtil.unEquals(InumberCons.ZERO, clogVO.getDef01()))
                                {
                                    YonLogUtil.debug(Boolean.TRUE, "passwordauth: " + clogVO.getDef02());
                                    loginEnd(-1, clogVO.getDef02());
                                    // loginEndInDT( -1, clogVO.getDef02());
                                    return;
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            YonLogUtil.debug(Boolean.TRUE, StringUtil.toString(e));
                            Log.getInstance("gylc").info("LoginAction: " + StringUtil.toString(e));
                            loginEnd(0, e.getMessage());
                            return;
                        }
                        // 202500619
                        LoginRequest request = getLoginRequest();
                        //
                        UserExit.getInstance().setUserCode(request.getUserCode());
                        //
                        LoginAssistant loginAssi = createLoginAssistant(request);
                        LoginResponse response = loginAssi.login();
                        int result = response.getLoginResult();
                        
                        if (result == ILoginConstants.LOGIN_SUCCESS)
                        {
                            loginAssi.showWorkbench(response);
                            loginEndInDT(result, null);
                            storeLoginRequest(request);
                            // 安全日志记录
                            getService().writeSecurityLogSync(writeSecurityLog(true));
                        }
                        else
                        {
                            // 安全日志记录
                            getService().writeSecurityLogSync(writeSecurityLog(false));
                            String msg = loginAssi.getResultMessage(result);
                            loginEndInDT(result, "" + msg);
                            NetStreamContext.resetAll();
                        }
                    }
                    catch (LoginVerifyException ex)
                    {
                        // 安全日志记录
                        getService().writeSecurityLogSync(writeSecurityLog(false));
                        NetStreamContext.resetAll();
                        String errMsg = ex.getMessage();
                        if (errMsg == null || errMsg.trim().length() == 0)
                        {
                            errMsg = ex.getClass().getName() + ":" + errMsg;
                        }
                        Logger.error(ex.getMessage(), ex);
                        loginEndInDT(ex.getLoginResult(), errMsg);
                    }
                    catch (Exception ex)
                    {
                        // 安全日志记录
                        getService().writeSecurityLogSync(writeSecurityLog(false));
                        NetStreamContext.resetAll();
                        String errMsg = ex.getMessage();
                        if (errMsg == null || errMsg.trim().length() == 0)
                        {
                            errMsg = ex.getClass().getName() + ":" + errMsg;
                        }
                        Logger.error(ex.getMessage(), ex);
                        loginEndInDT(ILoginConstants.UNKNOWN_ERROR, errMsg);
                    }
                    
                }
            };
            loginStart();
            new Thread(run, "loginNC").start();
        }
        
    }
    
    private static class ResultMessageLabel extends JLabel
    {
        private static final long serialVersionUID = 3429379046855159898L;
        
        public ResultMessageLabel()
        {
            super();
            this.setOpaque(false);
            this.setForeground(
                ThemeResourceCenter.getInstance().getColor("themeres/login/loginuiResConf", "loginui.resultMsgLabelForegroundColor"));
            this.setHorizontalAlignment(JLabel.CENTER);
            this.setIconTextGap(6);
            // this.setBackground(new Color(0xfff0c2));
            ImageIcon icon = LoginUIConfig.getInstance().getErrorIcon();
            this.setIcon(icon);
            // this.setBorder(new AbstractBorder() {
            // private static final long serialVersionUID = 1L;
            // Color borderColor = new Color(0xb69204);
            // Color shadowColor = new Color(0xc6c8c7);
            //
            // public Insets getBorderInsets(Component c) {
            // return new Insets(1, 1, 3, 3);
            // }
            //
            // public boolean isBorderOpaque() {
            // return true;
            // }
            //
            // public void paintBorder(Component c, Graphics g, int x, int y,
            // int w, int h) {
            // Color oldColor = g.getColor();
            // g.setColor(shadowColor);
            // Graphics2D g2 = (Graphics2D) g;
            // Rectangle rect1 = new Rectangle(x, y, w - 3, h - 3);
            // Rectangle rect2 = new Rectangle(x + 3, y + 3, w - 3, h - 3);
            // Area a = new Area(rect2);
            // a.subtract(new Area(rect1));
            // g2.fill(a);
            // g.setColor(borderColor);
            // g2.draw(rect1);
            // g.setColor(oldColor);
            // }
            // });
        }
        
        public void setText(String text)
        {
            if (text == null) text = "null";
            super.setText(text);
            Font f = this.getFont();
            if (f != null)
            {
                FontMetrics fm = this.getFontMetrics(f);
                int w = SwingUtilities.computeStringWidth(fm, text);
                int h = fm.getHeight() + 2;
                this.setPreferredSize(new Dimension(w + 30, h));
            }
        }
        
        public void paintComponent(Graphics g)
        {
            // Insets inset = this.getInsets();
            // Color c = g.getColor();
            // Rectangle rect = this.getBounds();
            // g.setColor(this.getBackground());
            // g.fillRect(inset.left, inset.top, rect.width - inset.left -
            // inset.right, rect.height - inset.bottom - inset.top);
            // g.setColor(c);
            super.paintComponent(g);
            
        }
    }
    
    private static class LanguageComboboxUI extends MetalComboBoxUI
    {
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus)
        {
        }
        
        @Override
        protected JButton createArrowButton()
        {
            JButton btn = new JButton()
            {
                private static final long serialVersionUID = 9205662544175749923L;
                
                @Override
                public void paintComponent(Graphics g)
                {
                    Insets insets = new Insets(8, 1, 1, 3);
                    int width = getWidth() - (insets.left + insets.right);
                    int height = getHeight() - (insets.top + insets.bottom);
                    if (height <= 0 || width <= 0)
                    {
                        return;
                    }
                    Icon comboIcon = getIcon();
                    if (comboIcon != null)
                    {
                        int iconWidth = comboIcon.getIconWidth();
                        int iconLeft = (getWidth() / 2) - (iconWidth / 2);
                        int iconTop = insets.top;
                        comboIcon.paintIcon(this, g, iconLeft, iconTop);
                        
                    }
                    
                }
                
            };
            btn.setFocusable(false);
            btn.setRequestFocusEnabled(false);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setMargin(new Insets(15, 1, 1, 3));
            btn.setIcon(arrowIcon);
            // if (btn instanceof MetalComboBoxButton && arrowIcon != null) {
            // ((MetalComboBoxButton) btn).setComboIcon(arrowIcon);
            // }
            return btn;
        }
        
        @Override
        protected void installComponents()
        {
            super.installComponents();
            comboBox.setBorder(null);
        }
        
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus)
        {
            ListCellRenderer renderer = comboBox.getRenderer();
            Component c;
            
            if (hasFocus && !isPopupVisible(comboBox))
            {
                c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
            }
            else
            {
                c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setBackground(UIManager.getColor("ComboBox.background"));
            }
            Color foreground = LoginUIConfig.getInstance().getSysButtonFgColor();
            c.setFont(comboBox.getFont());
            boolean oldOpaque = true;
            if (c instanceof JComponent)
            {
                oldOpaque = ((JComponent) c).isOpaque();
                ((JComponent) c).setOpaque(false);
            }
            if (hasFocus && !isPopupVisible(comboBox))
            {
                c.setForeground(foreground);
                c.setBackground(listBox.getSelectionBackground());
            }
            else
            {
                if (comboBox.isEnabled())
                {
                    c.setForeground(foreground);
                    c.setBackground(comboBox.getBackground());
                }
                else
                {
                    c.setForeground(foreground);
                    c.setBackground(DefaultLookup.getColor(comboBox, this, "ComboBox.disabledBackground", null));
                }
            }
            
            // Fix for 4238829: should lay out the JPanel.
            boolean shouldValidate = false;
            if (c instanceof JPanel)
            {
                shouldValidate = true;
            }
            
            int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
            // if (padding != null) {
            // x = bounds.x + padding.left;
            // y = bounds.y + padding.top;
            // w = bounds.width - (padding.left + padding.right);
            // h = bounds.height - (padding.top + padding.bottom);
            // }
            
            currentValuePane.paintComponent(g, c, comboBox, x, y, w, h, shouldValidate);
            if (c instanceof JComponent)
            {
                ((JComponent) c).setOpaque(oldOpaque);
            }
        }
    }
    
    private class DocumentHandler implements DocumentListener
    {
        public DocumentHandler()
        {
            super();
            
        }
        
        @Override
        public void changedUpdate(DocumentEvent e)
        {
            dealEvent(e);
        }
        
        @Override
        public void insertUpdate(DocumentEvent e)
        {
            dealEvent(e);
        }
        
        @Override
        public void removeUpdate(DocumentEvent e)
        {
            dealEvent(e);
        }
        
        private void dealEvent(DocumentEvent e)
        {
            DefaultLoginUIBackGroundPainter painter = (DefaultLoginUIBackGroundPainter) getUIBGPainter();
            boolean b = getTfUserCode().getDocument().getLength() > 0 && getPfUserPWD().getDocument().getLength() > 0;
            if (b)
            {
                painter.setLoginBtnLightIcon(LoginUIConfig.getInstance().getLoginBtnLightIcon());
            }
            else
            {
                painter.setLoginBtnLightIcon(null);
            }
            loginUIContainer.repaint();
        }
    }
    
    private Container loginUIContainer = null;
    
    private JComboBox cbbBusiCenter = null;
    private UIComboBox cbbLanguage = null;
    private JTextField tfUserCode = null;
    private JPasswordField pfUserPWD = null;
    private JLabel lblBusiCenter = null;
    private JLabel lblUserCode = null;
    private JLabel lblPWD = null;
    private JLabel lblCopyright = null;
    //
    private JButton btnLogin = null;
    private JButton btnOption = null;
    private boolean isShowOptionPanel = false;
    private JPanel optionPanel = null;
    private JButton menuOption = null;
    private JPopupMenu popupOption = null;
    
    private JCheckBox ckbZipRemoteStream = null;
    // private JButton setHomePageBtn = null;
    private JMenuItem miEnableWebstart = null;
    private JCheckBoxMenuItem zipStreamCBMI = null;
    private JCheckBoxMenuItem nullTransCBMI = null;
    
    //
    private ResultMessageLabel resultLbl = null;
    private JLabel lblLoginFlash = null;
    // 忘记密码JButton
    private JButton btnForgetPWD = null;
    // 忘记密码JButton
    private JButton btnGetDxMessage = null;
    // 计时器
    private JLabel timerLabel = null;
    private Dimension ctrlSize = new Dimension(160, 20);
    private Dimension defCtrlSize = new Dimension(260, 35);
    private ItemHandler itemHandler = new ItemHandler();
    protected LoginUIFocusTraversalPolicy focusTraversalPolicy = new LoginUIFocusTraversalPolicy();
    private KeyHandler keyHandler = new KeyHandler();
    private List<Component> focusTraveList = new ArrayList<Component>();
    
    private ILoginUIBackGroundPainter uiBGPainter = null;
    private LayoutManager loginUILayoutManager = null;
    private Shape loginBtnShape = null;
    private DocumentHandler documentHandler = new DocumentHandler();
    
    private WebStartPanel wsPanel = null;
    
    private static Properties oslangMap = null;
    private static Icon arrowIcon = null;// new
    
    // SingleColorArrowIcon(LoginUIConfig.getInstance().getSysButtonFgColor(),
    // 10, 6);//
    // ClientToolKit.loadImageIcon("images/loginui6x/arrow.png");
    static
    {
        if (LoginUIConfig.getInstance().isCustomLoginUI())
        {
            arrowIcon = new SingleColorArrowIcon(LoginUIConfig.getInstance().getSysButtonFgColor(), 10, 6);// ClientToolKit.loadImageIcon("images/loginui6x/arrow.png");
        }
        else
        {
            arrowIcon = LoginUIConfig.getInstance().getDownArrowIcon();
        }
    }
    
    public LoginUISupport(Container loginUIContainer)
    {
        super();
        this.loginUIContainer = loginUIContainer;
        initialize();
    }
    
    private void initialize()
    {
        // 尽早地设置是否压缩远程调用流
        boolean isSel = isNetStreamZip();
        setNetStreamZipProp(isSel);
        //
        UserExit.getInstance().setKeepToken(true);
        //
        focusTraveList.add(getCbbBusiCenter());
        focusTraveList.add(getTfUserCode());
        focusTraveList.add(getPfUserPWD());
        focusTraveList.add(getTfCaptcha()); // xull3 for captcha;
        focusTraveList.add(getBtnLogin());
        //
        loginUIContainer.setFocusTraversalPolicyProvider(true);
        loginUIContainer.setFocusTraversalPolicy(focusTraversalPolicy);
        
    }
    
    public LayoutManager getLoginUILayoutManager()
    {
        if (loginUILayoutManager == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                loginUILayoutManager = new LoginUILayout(this);
                
            }
            else
            {
                loginUILayoutManager = new DefaultLoginUILayout(this);
            }
        }
        return loginUILayoutManager;
    }
    
    public ILoginUIBackGroundPainter getUIBGPainter()
    {
        if (uiBGPainter == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                uiBGPainter = new LoginUIBackGroundPainter(this);
                
            }
            else
            {
                uiBGPainter = new DefaultLoginUIBackGroundPainter(this);
            }
        }
        return uiBGPainter;
    }
    
    public JLabel getLblLoginFlash()
    {
        if (lblLoginFlash == null)
        {
            lblLoginFlash = new JLabel()
            {
                private long count = 0;
                private static final long serialVersionUID = -4629883810823712506L;
                
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
                {
                    boolean b = super.imageUpdate(img, infoflags, x, y, w, h);
                    try
                    {
                        Thread.sleep(count / 10);
                        if (count < 800)
                        {
                            count += 5;
                        }
                    }
                    catch (Exception e)
                    {
                    }
                    return b;
                }
            };
            ImageIcon icon = getLoginFlashImage();
            if (icon != null)
            {
                lblLoginFlash.setIcon(icon);
                lblLoginFlash.setSize(icon.getIconWidth(), icon.getIconHeight());
                lblLoginFlash.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            }
            lblLoginFlash.setOpaque(false);
            lblLoginFlash.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            lblLoginFlash.setVisible(false);
        }
        return lblLoginFlash;
    }
    
    private ImageIcon getLoginFlashImage()
    {
        ImageIcon icon = null;
        if (LoginUIConfig.getInstance().isCustomLoginUI())
        {
            icon = ClientToolKit.loadImageIcon("images/loginui6x/progress.gif");
        }
        else
        {
            icon = ThemeResourceCenter.getInstance().getImage("themeres/loading/login_loading.gif");// ("themeres/loading/loading_24_frames_login_ccw.gif");//ClientToolKit.loadImageIcon("images/loginui6x/progress.gif");
        }
        return icon;
        
    }
    
    public JComboBox getCbbBusiCenter()
    {
        if (cbbBusiCenter == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                cbbBusiCenter = new UIComboBox();
                cbbBusiCenter.setEditable(false);
                cbbBusiCenter.setPreferredSize(ctrlSize);
                cbbBusiCenter.setSize(ctrlSize);
            }
            else
            {
                cbbBusiCenter = new JComboBox();
                cbbBusiCenter.setUI(new CComboboxUI());
                cbbBusiCenter.setEditable(false);
                cbbBusiCenter.setPreferredSize(defCtrlSize);
                cbbBusiCenter.setSize(defCtrlSize);
            }
            cbbBusiCenter.setFont(getControlFont());
            cbbBusiCenter.setRenderer(new BusiCenterCBBCellRender());
            if (ClientToolKit.isAdminConsole())
            {
                cbbBusiCenter.setEnabled(false);
            }
            else
            {
                cbbBusiCenter.setEnabled(true);
            }
        }
        return cbbBusiCenter;
    }
    
    public UIComboBox getCbbLanguage()
    {
        if (cbbLanguage == null)
        {
            cbbLanguage = new UIComboBox()
            {
                private static final long serialVersionUID = 8114875352879555168L;
                
                @Override
                public void updateUI()
                {
                    setUI(new LanguageComboboxUI());
                }
            };
            cbbLanguage.setOpaque(false);
            cbbLanguage.setEditable(false);
            // cbbLanguage.setForeground(LoginUIConfig.getInstance().getSysButtonFgColor());
            
            // cbbLanguage.setPreferredSize(new Dimension(85,23));
            cbbLanguage.setSize(new Dimension(115, 23));
            cbbLanguage.setFont(getControlFont());
        }
        return cbbLanguage;
    }
    
    protected void doCbbLanguageSelectChange()
    {
        Language selLang = (Language) getCbbLanguage().getSelectedItem();
        NCLangRes.getInstance().setCurrLanguage(selLang);
        UserExit.getInstance().setLangCode(selLang.getCode());
        getLblResultMessage().setVisible(false);
        // preload funcode lang res
        new Thread()
        {
            @Override
            public void run()
            {
                NCLangRes.getInstance().getStrByID("MENUCODE", "10");
                NCLangRes.getInstance().getStrByID(IProductCode.PRODUCTCODE_FUNCODE, "10");
                
            }
            
        }.start();
        refreshUIContrl();
    }
    
    protected void doCbbBusiCenterSelectChange()
    {
        BusiCenterVO selBC = (BusiCenterVO) getCbbBusiCenter().getSelectedItem();
        InvocationInfoProxy.getInstance().reset();
        UserExit.getInstance().setUserDataSource(selBC.getDataSourceName());
        UserExit.getInstance().setBizCenterCode(selBC.getCode());
        
        // initCbbLanguage(getCurrBCEnableLanugae());
    }
    
    // private Language[] getCurrBCEnableLanugae() {
    // String currDSName = UserExit.getInstance().getUserDataSource();
    // if (currDSName == null || currDSName.trim().equals("")) {
    // return NCLangRes.getInstance().getAllLanguages();
    // } else {
    // LanguageVO[] enableLanguageVOs =
    // MultiLangContext.getInstance().getEnableLangVOs();
    // int count = enableLanguageVOs == null ? 0 : enableLanguageVOs.length;
    // List<Language> list = new ArrayList<Language>();
    // for (int i = 0; i < count; i++) {
    // Language lang =
    // NCLangRes.getInstance().getLanguage(enableLanguageVOs[i].getLangcode());
    // if (lang != null) {
    // list.add(lang);
    // }
    // }
    // return list.toArray(new Language[0]);
    // }
    // }
    
    public void initCbbBusiCenter(BusiCenterVO[] bcVOs)
    {
        getCbbBusiCenter().removeItemListener(itemHandler);
        getCbbBusiCenter().removeAllItems();
        int bcCount = bcVOs == null ? 0 : bcVOs.length;
        boolean isAdminConsole = ClientToolKit.isAdminConsole();
        for (int i = 0; i < bcCount; i++)
        {
            if (!RuntimeEnv.getInstance().isDevelopMode())
            {
                if (isAdminConsole ^ "0000".equals(bcVOs[i].getCode()))
                {
                    continue;
                }
            }
            getCbbBusiCenter().addItem(bcVOs[i]);
        }
        getCbbBusiCenter().setSelectedIndex(-1);
        getCbbBusiCenter().addItemListener(itemHandler);
        if (getCbbBusiCenter().getModel().getSize() > 0)
        {
            getCbbBusiCenter().setSelectedIndex(0);
        }
    }
    
    public void initCbbLanguage(Language[] langs)
    {
        getCbbLanguage().removeItemListener(itemHandler);
        getCbbLanguage().removeAllItems();
        int langCount = langs == null ? 0 : langs.length;
        int maxW = 0;
        FontMetrics fm = getCbbLanguage().getFontMetrics(getCbbLanguage().getFont());
        for (int i = 0; i < langCount; i++)
        {
            getCbbLanguage().addItem(langs[i]);
            String disName = langs[i].getDisplayName();
            if (disName != null)
            {
                int width = fm.stringWidth(disName);
                if (width > maxW)
                {
                    maxW = width;
                }
            }
        }
        getCbbLanguage().setPreferredSize(new Dimension(maxW + 20, 23));
        getCbbLanguage().setSelectedIndex(-1);
        getCbbLanguage().addItemListener(itemHandler);
        if (langCount > 0)
        {
            getCbbLanguage().setSelectedIndex(0);
        }
    }
    
    public JTextField getTfUserCode()
    {
        if (tfUserCode == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                tfUserCode = new JTextField();
                tfUserCode.setSize(ctrlSize);
                tfUserCode.setPreferredSize(ctrlSize);
                tfUserCode.setFont(getControlFont());
                tfUserCode.addKeyListener(keyHandler);
            }
            else
            {
                tfUserCode = new JTextField()
                {
                    private static final long serialVersionUID = 1203731583509013110L;
                    private Insets insets = new Insets(6, 6, 6, 6);
                    private Color borderColor = LoginUIConfig.getInstance().getInputCtrlBorderColor();
                    private Color borderFocusColor = LoginUIConfig.getInstance().getInputCtrlBorderFocusColor();
                    
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        Dimension size = getSize();
                        Shape shape = getRoundShape(0, 0, size.width, size.height, insets);
                        Color bColor = this.isFocusOwner() ? borderFocusColor : borderColor;
                        int w = this.isFocusOwner() ? 2 : 1;
                        paintCtrlBorder((Graphics2D) g, this, Color.white, bColor, shape, w);
                        super.paintComponent(g);
                    }
                    
                };
                tfUserCode.putClientProperty("caretWidth", Integer.valueOf(2));
                tfUserCode.setCaretColor(LoginUIConfig.getInstance().getInputCtrlBorderFocusColor());
                tfUserCode.setOpaque(false);
                tfUserCode.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
                tfUserCode.setFont(getControlFont());
                tfUserCode.addKeyListener(keyHandler);
                tfUserCode.setSize(defCtrlSize);
                tfUserCode.setPreferredSize(defCtrlSize);
                tfUserCode.getDocument().addDocumentListener(documentHandler);
            }
            tfUserCode.setUI(new MyTextFieldUI());
        }
        return tfUserCode;
    }
    
    public JPasswordField getPfUserPWD()
    {
        if (pfUserPWD == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                pfUserPWD = new JPasswordField();
                pfUserPWD.setSize(ctrlSize);
                pfUserPWD.setPreferredSize(ctrlSize);
                pfUserPWD.addKeyListener(keyHandler);
                CapsLockHintRegister.registerCapsLockHint(pfUserPWD,
                    NCLangRes.getInstance().getStrByID("sfbase", "sfbase-0015")/* CapsLock已启用 */);
            }
            else
            {
                pfUserPWD = new JPasswordField()
                {
                    private static final long serialVersionUID = -2364319261991684160L;
                    private Insets insets = new Insets(6, 6, 6, 6);
                    private Color borderColor = LoginUIConfig.getInstance().getInputCtrlBorderColor();
                    private Color borderFocusColor = LoginUIConfig.getInstance().getInputCtrlBorderFocusColor();
                    
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        Dimension size = getSize();
                        Shape shape = getRoundShape(0, 0, size.width, size.height, insets);
                        Color bColor = this.isFocusOwner() ? borderFocusColor : borderColor;
                        int w = this.isFocusOwner() ? 2 : 1;
                        paintCtrlBorder((Graphics2D) g, this, Color.white, bColor, shape, w);
                        super.paintComponent(g);
                    }
                    
                };
                pfUserPWD.putClientProperty("caretWidth", Integer.valueOf(2));
                pfUserPWD.setCaretColor(LoginUIConfig.getInstance().getInputCtrlBorderFocusColor());
                pfUserPWD.setOpaque(false);
                pfUserPWD.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
                pfUserPWD.addKeyListener(keyHandler);
                CapsLockHintRegister.registerCapsLockHint(pfUserPWD,
                    NCLangRes.getInstance().getStrByID("sfbase", "sfbase-0015")/* CapsLock已启用 */);
                pfUserPWD.setSize(defCtrlSize);
                pfUserPWD.setPreferredSize(defCtrlSize);
                pfUserPWD.getDocument().addDocumentListener(documentHandler);
            }
            
        }
        return pfUserPWD;
    }
    
    /**
     * 忘记密码按钮
     *
     * @return
     */
    public JButton getForgetPWD()
    {
        if (btnForgetPWD == null)
        {
            
            btnForgetPWD =
                new JButton(NCLangRes.getInstance().getStrByID("loginui", "loginuires-000007"));/* 忘记密码 */
            btnForgetPWD.setUI(new CustomUI());
            btnForgetPWD.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            btnForgetPWD.setOpaque(false);
            btnForgetPWD.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnForgetPWD.setRolloverEnabled(true);
            btnForgetPWD.setBorder(BorderFactory.createEmptyBorder());
            btnForgetPWD.setHorizontalAlignment(JButton.RIGHT);
            btnForgetPWD.addActionListener(new ActionListener()
            {
                
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    BusiCenterVO bcVO = (BusiCenterVO) cbbBusiCenter.getSelectedItem();
                    String dsName = bcVO.getDataSourceName();
                    InvocationInfoProxy.getInstance().setUserDataSource(dsName);
                    PassWordWizardDialog wd = new PassWordWizardDialog(loginUIContainer, getSteps(dsName), null);
                    Dimension screan = Toolkit.getDefaultToolkit().getScreenSize();
                    wd.setSize(647, 390);
                    Dimension dlgsize = wd.getSize();
                    wd.setLocation((screan.width - dlgsize.width) / 2, (screan.height - dlgsize.height) / 2 + 40);
                    wd.showModal();
                }
            });
        }
        return btnForgetPWD;
    }
    
    /**
     * 填充步骤
     *
     * @return
     */
    private List<WizardStep> getSteps(String dsName)
    {
        List<WizardStep> steps = new ArrayList<WizardStep>();
        String[] setpTitle = new String[]{
            NCLangRes.getInstance().getStrByID("loginui", "retrievepwd-000002"),
            NCLangRes.getInstance().getStrByID("loginui", "retrievepwd-000003"),
            NCLangRes.getInstance().getStrByID("loginui", "retrievepwd-000004"),
            NCLangRes.getInstance().getStrByID("loginui", "retrievepwd-000005")};
        for (int i = 0; i < 4; i++)
        {
            WizardStep step = new WizardStep();
            step.setTitle(NCLangRes.getInstance().getStrByID("loginui", "retrievepwd-000001"));
            step.setDescription(setpTitle[i]);
            if (i == 0) step.setComp(new PassWordWizardFirstStep(dsName));
            else if (i == 1) step.setComp(new PassWordWizarSecondStep(dsName));
            else if (i == 2) step.setComp(new PassWordWizardThirdStep(dsName));
            else if (i == 3) step.setComp(new PassWordWizardFourthStep(dsName));
            
            steps.add(step);
        }
        
        return steps;
    }
    
    /**
     * 渲染按钮
     *
     * @author sujb
     */
    private class CustomUI extends BasicButtonUI
    {
        protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text)
        {
            super.paintText(g, c, textRect, text);
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            if (!model.isRollover())
            {
                c.setForeground(new Color(25, 138, 232));// #198ae8
                g.drawLine(textRect.x, textRect.y + textRect.height - 1, textRect.x + textRect.width, textRect.y + textRect.height - 1);
            }
            else
            {
                c.setForeground(new Color(6, 108, 192));// #066cc0
                g.drawLine(textRect.x, textRect.y + textRect.height - 1, textRect.x + textRect.width, textRect.y + textRect.height - 1);
            }
        }
    }
    
    public JLabel getLblBusiCenter()
    {
        if (lblBusiCenter == null)
        {
            lblBusiCenter = new JLabel(getLblBusiCenterText());
            lblBusiCenter.setFont(getLblFont());
            lblBusiCenter.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        }
        return lblBusiCenter;
    }
    
    private String getLblBusiCenterText()
    {
        return NCLangRes.getInstance().getStrByID("loginui", "loginuires-000000")/* 系统 */;
    }
    
    public JLabel getLblUserCode()
    {
        if (lblUserCode == null)
        {
            lblUserCode = new JLabel(getLblUserCodeText());
            lblUserCode.setFont(getLblFont());
            lblUserCode.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        }
        return lblUserCode;
    }
    
    private String getLblUserCodeText()
    {
        return NCLangRes.getInstance().getStrByID("loginui", "loginuires-000001")/* 用户 */;
    }
    
    private String getWebStartPanelHintText()
    {
        return NCLangRes.getInstance().getStrByID("sfbase", "webstart-0001")/*
                                                                             * 使用全屏模式 ， 您将获得更好的体验 。
                                                                             */;
    }
    
    private String getStartWebStartBtnText()
    {
        return NCLangRes.getInstance().getStrByID("sfbase", "webstart-0002")/* 立即使用 */;
    }
    
    public JLabel getLblPWD()
    {
        if (lblPWD == null)
        {
            lblPWD = new JLabel(getLblPWDText());
            lblPWD.setFont(getLblFont());
            lblPWD.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        }
        return lblPWD;
    }
    
    private String getLblPWDText()
    {
        return NCLangRes.getInstance().getStrByID("loginui", "loginuires-000002")/* 密码 */;
    }
    
    public JLabel getLblCopyright()
    {
        if (lblCopyright == null)
        {
            lblCopyright = new JLabel(getCopyrightText());
            lblCopyright.setForeground(LoginUIConfig.getInstance().getCopyrightFgColor());
            lblCopyright.setBorder(BorderFactory.createEmptyBorder());
        }
        return lblCopyright;
    }
    
    private String getCopyrightText()
    {
        String str =
            NCLangRes.getInstance().getStrByID("loginui", "loginuires-000005")/* 版权所有 (c) 2012 用友软件股份有限公司 */;
        return str.trim();
    }
    
    public Shape getLoginBtnShape()
    {
        if (loginBtnShape == null)
        {
            Dimension size = getBtnLogin().getSize();
            GeneralPath path = new GeneralPath();
            path.moveTo(size.width - 1, 0);
            path.lineTo(size.width - 1, size.height - 66);
            path.curveTo(size.width - 1, size.height - 66 + 12, 42, size.height - 8, 0, size.height - 1);
            path.lineTo(0, 63);
            path.curveTo(3, 63 - 33, size.width - 21, 4, size.width, 0);
            loginBtnShape = path;
        }
        return loginBtnShape;
    }
    
    private JButton createDefaultLoginBtn()
    {
        final JButton btn = new JButton()
        {
            private static final long serialVersionUID = 340566138580274126L;
            
            @Override
            public void setText(String text)
            {
                super.setText(null);
            }
            
            @Override
            public boolean contains(int x, int y)
            {
                return getLoginBtnShape().contains(x, y);
            }
            
            @Override
            public void doClick(int pressTime)
            {
                model.setArmed(true);
                model.setPressed(true);
                try
                {
                    Thread.sleep(pressTime);
                }
                catch (InterruptedException ie)
                {
                }
                model.setPressed(false);
                model.setArmed(false);
            }
            
        };
        btn.setAction(new LoginAction());
        btn.setFont(getControlFont());
        btn.setMargin(new Insets(0, 0, 0, 0));
        ImageIcon icon = LoginUIConfig.getInstance().getLoginBtnIcon();
        Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        btn.setPreferredSize(size);
        btn.setSize(size);
        btn.setOpaque(false);
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setText(null);
        btn.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateLoginBtnBackground();
                // ButtonModel model = btn.getModel();
                // ImageIcon icon = null;
                // if(isSimpleChinese()){
                // icon = LoginUIConfig.getInstance().getLoginBtnIcon();
                // }else{
                // icon = LoginUIConfig.getInstance().getLoginBtnEnglishIcon();
                // }
                //
                // // ImageIcon lightIcon = null;
                // if (!model.isEnabled()) {
                // if (model.isSelected()) {
                // } else {
                // }
                // } else if (model.isPressed() && model.isArmed()) {
                // // lightIcon =
                // LoginUIConfig.getInstance().getLoginBtnLightIcon();
                // }
                // if (model.isRollover()) {
                // if(isSimpleChinese()){
                // icon = LoginUIConfig.getInstance().getLoginBtnOverIcon();
                // }else{
                // icon =
                // LoginUIConfig.getInstance().getLoginBtnEnglishOverIcon();
                // }
                // if (model.isSelected()) {
                // } else {
                // }
                // } else if (model.isSelected()) {
                // }
                // DefaultLoginUIBackGroundPainter painter =
                // (DefaultLoginUIBackGroundPainter)getUIBGPainter();
                // painter.setLoginBtnBGIcon(icon);
                // // painter.setLoginBtnLightIcon(lightIcon);
                // loginUIContainer.repaint();
                
            }
            
        });
        btn.setToolTipText(getBtnLoginToolTipText());
        // btnLogin.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        
        return btn;
    }
    
    private void updateLoginBtnBackground()
    {
        if (LoginUIConfig.getInstance().isCustomLoginUI())
        {
            return;
        }
        ButtonModel model = getBtnLogin().getModel();
        ImageIcon icon = null;
        if (isSimpleChinese())
        {
            icon = LoginUIConfig.getInstance().getLoginBtnIcon();
        }
        else
        {
            icon = LoginUIConfig.getInstance().getLoginBtnEnglishIcon();
        }
        
        // ImageIcon lightIcon = null;
        if (!model.isEnabled())
        {
            if (model.isSelected())
            {
            }
            else
            {
            }
        }
        else if (model.isPressed() && model.isArmed())
        {
            // lightIcon = LoginUIConfig.getInstance().getLoginBtnLightIcon();
        }
        if (model.isRollover())
        {
            if (isSimpleChinese())
            {
                icon = LoginUIConfig.getInstance().getLoginBtnOverIcon();
            }
            else
            {
                icon = LoginUIConfig.getInstance().getLoginBtnEnglishOverIcon();
            }
            if (model.isSelected())
            {
            }
            else
            {
            }
        }
        else if (model.isSelected())
        {
        }
        DefaultLoginUIBackGroundPainter painter = (DefaultLoginUIBackGroundPainter) getUIBGPainter();
        painter.setLoginBtnBGIcon(icon);
        // painter.setLoginBtnLightIcon(lightIcon);
        loginUIContainer.repaint();
        
    }
    
    private boolean isSimpleChinese()
    {
        return NCLangRes.getInstance().getCurrLanguage().getCode().equalsIgnoreCase("simpchn");
    }
    
    private JButton createCustomLoginUILoginBtn()
    {
        JButton btn = new JButton();
        btn = new JButton();
        btn.setAction(new LoginAction());
        btn.setFont(getControlFont());
        Dimension size = new Dimension(70, 20);
        btn.setPreferredSize(size);
        btn.setSize(size);
        btn.setText(getBtnLoginText());
        btn.setToolTipText(getBtnLoginToolTipText());
        btn.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        return btn;
    }
    
    public JButton getBtnLogin()
    {
        if (btnLogin == null)
        {
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                btnLogin = createCustomLoginUILoginBtn();
            }
            else
            {
                btnLogin = createDefaultLoginBtn();
            }
        }
        return btnLogin;
    }
    
    public JButton getBtnOption()
    {
        if (btnOption == null)
        {
            try
            {
                btnOption = new JButton();
                btnOption
                    .setText(NCLangRes.getInstance().getStrByID("sysframev5", "UPPsysframev5-000060")/* 选项 */);
                btnOption.setHorizontalTextPosition(JButton.LEFT);
                btnOption.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
                btnOption.setIcon(ClientToolKit.loadImageIcon("images/loginicon/rightarrow.gif"));
                setShowOptionPanel(false);
                btnOption.setPreferredSize(new Dimension(70, 20));
                btnOption.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setShowOptionPanel(!isShowOptionPanel());
                    }
                });
            }
            catch (Exception e)
            {
                Logger.error(e.getMessage(), e);
                ;
            }
        }
        return btnOption;
    }
    
    public boolean isShowOptionPanel()
    {
        return isShowOptionPanel;
    }
    
    public void setShowOptionPanel(boolean isShowOptionPanel)
    {
        this.isShowOptionPanel = isShowOptionPanel;
        if (isShowOptionPanel())
        {
            getBtnOption().setIcon(ClientToolKit.loadImageIcon("images/loginicon/leftarrow.gif"));
        }
        else
        {
            getBtnOption().setIcon(ClientToolKit.loadImageIcon("images/loginicon/rightarrow.gif"));
        }
        getOptionPanel().setVisible(this.isShowOptionPanel);
    }
    
    public JPanel getOptionPanel()
    {
        if (optionPanel == null)
        {
            optionPanel = new JPanel();
            optionPanel.setOpaque(false);
            optionPanel.setLayout(null);
            optionPanel.setFocusable(false);
            optionPanel.setRequestFocusEnabled(false);
            optionPanel.setPreferredSize(new Dimension(600, 46));
            optionPanel.add(getCkbZipRemoteStream());
            optionPanel.add(getMiEnableWebstart());
        }
        return optionPanel;
    }
    
    public WebStartPanel getWebStartPanel()
    {
        if (wsPanel == null)
        {
            wsPanel = new WebStartPanel();
            wsPanel.setWebStartPanelHintText(getWebStartPanelHintText());
            wsPanel.setStartWebStartBtnText(getStartWebStartBtnText());
            wsPanel.setCloseBtnAction(new CloseWebStartPanelAction());
        }
        return wsPanel;
    }
    
    public JButton getMenuOption()
    {
        if (menuOption == null)
        {
            menuOption = new JButton(getMenuOptionText())
            {
                private static final long serialVersionUID = -508472560900674587L;
                
                @Override
                public Dimension getPreferredSize()
                {
                    Dimension d = super.getPreferredSize();
                    d.height = 23;
                    return d;
                }
                
            };
            menuOption.setOpaque(false);
            menuOption.setIcon(arrowIcon);
            menuOption.setHorizontalTextPosition(JButton.LEFT);
            menuOption.setContentAreaFilled(false);
            menuOption.setBorderPainted(false);
            menuOption.setFocusable(false);
            menuOption.setRequestFocusEnabled(false);
            menuOption.setForeground(LoginUIConfig.getInstance().getSysButtonFgColor());
            menuOption.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Dimension size = getMenuOption().getPreferredSize();
                    getPopupOption().show(getMenuOption(), 0, size.height);
                    
                }
                
            });
        }
        return menuOption;
    }
    
    public JPopupMenu getPopupOption()
    {
        if (popupOption == null)
        {
            popupOption = new JPopupMenu();
            // JCheckBoxMenuItem zipStream = new JCheckBoxMenuItem(new
            // ZipRemoteStreamAction());
            popupOption.add(getZipStreamCBMI());
            popupOption.add(getMiEnableWebstart());
            // MLNullTranslatorAction action = new MLNullTranslatorAction();
            // action.putValue(Action.SELECTED_KEY, false);
            popupOption.add(getNullTransCBMI());
            // popupOption.add(setHomePage);
        }
        return popupOption;
    }
    
    public JCheckBox getCkbZipRemoteStream()
    {
        if (ckbZipRemoteStream == null)
        {
            String name = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000246")/* 压缩远程调用流 */;
            ckbZipRemoteStream = new JCheckBox(name);
            ckbZipRemoteStream.setOpaque(false);
            ckbZipRemoteStream.setFocusPainted(false);
            ckbZipRemoteStream.setBounds(0, 0, 115, 20);
            ckbZipRemoteStream.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            boolean isSel = isNetStreamZip();
            ckbZipRemoteStream.setSelected(isSel);
            setNetStreamZipProp(isSel);
            ckbZipRemoteStream.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    boolean isSel = ckbZipRemoteStream.isSelected();
                    setNetStreamZipProp(isSel);
                    ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
                    setup.putBoolean("_isNetStreamZip", isSel);
                    ClientSetupCache.storeGlobalClientSetup();
                }
            });
        }
        return ckbZipRemoteStream;
    }
    
    private JCheckBoxMenuItem getZipStreamCBMI()
    {
        if (zipStreamCBMI == null)
        {
            zipStreamCBMI = new JCheckBoxMenuItem(new ZipRemoteStreamAction());
            String name = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000246")/* 压缩远程调用流 */;
            zipStreamCBMI.setText(name);
        }
        return zipStreamCBMI;
    }
    
    private JCheckBoxMenuItem getNullTransCBMI()
    {
        if (nullTransCBMI == null)
        {
            MLNullTranslatorAction action = new MLNullTranslatorAction();
            action.putValue(Action.SELECTED_KEY, false);
            nullTransCBMI = new JCheckBoxMenuItem(action);
            String name = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000273")/* 显示资源ID号 */;
            nullTransCBMI.setText(name);
        }
        return nullTransCBMI;
    }
    
    public JMenuItem getMiEnableWebstart()
    {
        if (miEnableWebstart == null)
        {
            String name = getMiEnableWebstartText();
            miEnableWebstart = new JMenuItem(new WebStartAction());
            miEnableWebstart.setText(name);
            miEnableWebstart.setFocusPainted(false);
        }
        return miEnableWebstart;
    }
    
    private static void setNetStreamZipProp(boolean isZip)
    {
        try
        {
            NetStreamConstants.STREAM_NEED_COMPRESS = isZip;
            // ClassLoader cl = null;
            // if(RuntimeEnv.getInstance().isDevelopMode()){
            // cl = LoginUISupport.class.getClassLoader();
            // }else{
            // cl = LoginUISupport.class.getClassLoader().getParent();
            // }
            // if (cl == null)
            // cl = ClassLoader.getSystemClassLoader();
            // Class<?> clazz =
            // cl.loadClass("nc.bs.framework.comn.NetStreamConstants");
            // Field field = clazz.getDeclaredField("STREAM_NEED_COMPRESS");
            // field.set(null, isZip);
        }
        catch (Throwable thr)
        {
            Logger.error(thr.getMessage(), thr);
        }
    }
    
    private static boolean isNetStreamZip()
    {
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        return setup.getBoolean("_isNetStreamZip", true);
    }
    
    // public JButton getSetHomePageBtn() {
    // if (setHomePageBtn == null) {
    // setHomePageBtn = new
    // JButton(NCLangRes.getInstance().getStrByID("sysframev5",
    // "UPPsysframev5-000057")/*设为主页*/);
    // setHomePageBtn.setHorizontalAlignment(JButton.LEFT);
    // setHomePageBtn.setBounds(0, 26, 65, 20);
    // setHomePageBtn.setUI(new CustomUI());
    // setHomePageBtn.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
    // setHomePageBtn.setOpaque(false);
    // setHomePageBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    // setHomePageBtn.setRolloverEnabled(true);
    // setHomePageBtn.setBorder(BorderFactory.createEmptyBorder());
    // setHomePageBtn.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    // ClientToolKit.setHomePage();
    // }
    //
    // });
    // }
    // return setHomePageBtn;
    // }
    
    private String getBtnLoginText()
    {
        return NCLangRes.getInstance().getStrByID("loginui", "loginuires-000003")/* 登录 */;
    }
    
    private String getBtnLoginToolTipText()
    {
        return NCLangRes.getInstance().getStrByID("loginui", "loginuires-000004")/* 登录系统 */;
    }
    
    private String getMenuOptionText()
    {
        return NCLangRes.getInstance().getStrByID("sysframev5", "UPPsysframev5-000060")/* 选项 */;
    }
    
    protected void refreshUIContrl()
    {
        getLblBusiCenter().setFont(getLblFont());
        getLblUserCode().setFont(getLblFont());
        getLblPWD().setFont(getLblFont());
        getForgetPWD().setFont(getLblFont());
        getCbbBusiCenter().setFont(getControlFont());
        getCbbLanguage().setFont(getControlFont());
        getTfUserCode().setFont(getControlFont());
        getPfUserPWD().setFont(getControlFont());
        getBtnLogin().setFont(getControlFont());
        // //
        getLblBusiCenter().setText(getLblBusiCenterText());
        getLblUserCode().setText(getLblUserCodeText());
        getLblPWD().setText(getLblPWDText());
        getForgetPWD().setText(NCLangRes.getInstance().getStrByID("loginui", "loginuires-000007"));
        getLblCopyright().setText(getCopyrightText());
        getBtnLogin().setText(getBtnLoginText());
        getBtnLogin().setToolTipText(getBtnLoginToolTipText());
        getMenuOption().setText(getMenuOptionText());
        
        getWebStartPanel().setWebStartPanelHintText(getWebStartPanelHintText());
        getWebStartPanel().setStartWebStartBtnText(getStartWebStartBtnText());
        
        getMiEnableWebstart().setText(getMiEnableWebstartText());
        getNullTransCBMI().setText(NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000273")/* 显示资源ID号 */);
        getZipStreamCBMI().setText(NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000246")/* 压缩远程调用流 */);
        
        updateLoginBtnBackground();
    }
    
    private String getMiEnableWebstartText()
    {
        return NCLangRes.getInstance().getStrByID("sfbase", "webstart-0003")/* 全屏模式 */;
    }
    
    public ResultMessageLabel getLblResultMessage()
    {
        if (resultLbl == null)
        {
            resultLbl = new ResultMessageLabel();
            resultLbl.setVisible(false);
        }
        return resultLbl;
    }
    
    private Font getLblFont()
    {
        return new Font(Style.getFontname(), Font.PLAIN, 12);
    }
    
    private Font getControlFont()
    {
        return new Font(Style.getFontname(), Font.PLAIN, 12);
    }
    
    protected LoginRequest getLoginRequest()
    {
        LoginRequest request = new LoginRequest();
        BusiCenterVO bcVO = (BusiCenterVO) getCbbBusiCenter().getSelectedItem();
        if (bcVO != null)
        {
            request.setBusiCenterCode(bcVO.getCode());
        }
        String userCode = getTfUserCode().getText().trim();
        request.setUserCode(userCode);
        request.setUserPWD(new String(getPfUserPWD().getPassword()).trim());
        request.setLangCode(getCurrLangCode());
        // 设置入口设备类型
        request.setDeviceType(CheckDevice.check());
        request.setLoginType(ClientToolKit.isPortalClient());
        String profileVersion = FunctionPermProfileManager.getInstance().getProfileVersion(userCode);
        String profileRuleVersion = FunctionPermProfileManager.getInstance().getProfileRuleVersion(userCode);
        
        request.putAttachProp(IFunctionPermProfileConst.FUNCTIONPERM_PROFILEVERSION_KEY, profileVersion);
        request.putAttachProp(IFunctionPermProfileConst.FUNCTIONPERM_RULEVERSION_KEY, profileRuleVersion);
        
        request.putAttachProp(IFunctionPermProfileConst.NEED_QUERY_SIMPLE_PERM, true);
        
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        String groupCode = (String) setup.get("loginGroup" + bcVO.getCode() + userCode);
        request.putAttachProp(GroupVO.class.getName() + ".code", groupCode);
        return request;
    }
    
    protected LoginAssistant createLoginAssistant(LoginRequest request)
    {
        return new LoginAssistant(request);
    }
    
    public static String getCurrLangCode()
    {
        if (NCLangRes.getInstance().getCurrLanguage() != null) return NCLangRes.getInstance().getCurrLanguage().getCode();
        else
        {
            return AbstractNCLangRes.getDefaultLanguage().getCode();
        }
        
    }
    
    protected void loginStart()
    {
        
        getLblResultMessage().setText("");
        getLblResultMessage().setVisible(false);
        getLblLoginFlash().setVisible(true);
        getBtnLogin().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        getBtnLogin().setEnabled(false);
        getBtnOption().setEnabled(false);
        getCkbZipRemoteStream().setEnabled(false);
        getMiEnableWebstart().setEnabled(false);
        
        if (getUIBGPainter() instanceof DefaultLoginUIBackGroundPainter)
        {
            ((DefaultLoginUIBackGroundPainter) getUIBGPainter()).setLoginBtnLightIcon(LoginUIConfig.getInstance().getLoginBtnLightIcon());
        }
        getUIBGPainter().setIsLoginning(true);
        loginUIContainer.repaint();
        // getSetHomePageBtn().setEnabled(false);
    }
    
    private void loginEndInDT(final int result, final String resultMsg)
    {
        Runnable run = new Runnable()
        {
            public void run()
            {
                loginEnd(result, resultMsg);
            }
        };
        ClientToolKit.invokeInDispatchThread(run);
        // run.run();
    }
    
    protected void loginEnd(int loginResult, String resultMsg)
    {
        getBtnLogin().setCursor(Cursor.getDefaultCursor());
        getBtnLogin().setEnabled(true);
        getBtnOption().setEnabled(true);
        getCkbZipRemoteStream().setEnabled(true);
        getMiEnableWebstart().setEnabled(true);
        getLblLoginFlash().setVisible(false);
        if (getUIBGPainter() instanceof DefaultLoginUIBackGroundPainter && getPfUserPWD().getPassword().length == 0)
        {
            ((DefaultLoginUIBackGroundPainter) getUIBGPainter()).setLoginBtnLightIcon(null);
        }
        if (resultMsg != null)
        {
            getLblResultMessage().setText(resultMsg);
            getLblResultMessage().setVisible(true);
        }
        else
        {
            getLblResultMessage().setVisible(false);
        }
        
        // loadCaptchaImage();
        getUIBGPainter().setIsLoginning(false);
        loginUIContainer.repaint();
    }
    
    private void storeLoginRequest(LoginRequest request)
    {
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        setup.put("userCode", request.getUserCode());
        setup.put("bcCode", request.getBusiCenterCode());
        setup.put("langCode", request.getLangCode());
        ClientSetupCache.storeGlobalClientSetup();
    }
    
    protected void initFromCookie()
    {
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        String bcCode = (String) setup.get("bcCode");
        String userCode = (String) setup.get("userCode");
        String langCode = (String) setup.get("langCode");
        if (langCode == null || langCode.trim().length() == 0)
        {
            langCode = getLangCodeFromOsMap();
        }
        if (userCode != null)
        {
            getTfUserCode().setText(userCode);
        }
        if (bcCode != null)
        {
            ComboBoxModel model = getCbbBusiCenter().getModel();
            int size = model.getSize();
            for (int i = 0; i < size; i++)
            {
                BusiCenterVO vo = (BusiCenterVO) model.getElementAt(i);
                if (bcCode.equals(vo.getCode()))
                {
                    getCbbBusiCenter().setSelectedIndex(i);
                    break;
                }
            }
        }
        if (langCode != null)
        {
            ComboBoxModel model = getCbbLanguage().getModel();
            int size = model.getSize();
            for (int i = 0; i < size; i++)
            {
                Language vo = (Language) model.getElementAt(i);
                if (langCode.equals(vo.getCode()))
                {
                    getCbbLanguage().setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private synchronized static Properties getOSLangMap()
    {
        if (oslangMap == null)
        {
            oslangMap = new Properties();
            InputStream in = null;
            try
            {
                in = LoginUISupport.class.getClassLoader().getResourceAsStream("oslangmap.properties");
                oslangMap.load(in);
            }
            catch (Exception e)
            {
                Logger.error(e.getMessage(), e);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
        return oslangMap;
    }
    
    private static String getLangCodeFromOsMap()
    {
        Properties prop = getOSLangMap();
        String language = System.getProperty("user.language");
        String country = System.getProperty("user.country");
        String langCode = prop.getProperty(language + "_" + country);
        if (langCode == null)
        {
            langCode = prop.getProperty(language);
        }
        return langCode;
        
    }
    
    protected static boolean needLayout(Container parent, Component comp)
    {
        return comp.isVisible() && parent.isAncestorOf(comp);
    }
    
    protected Rectangle calculateLoginCenterSize(Container parent, int hgap, int vgap)
    {
        Dimension size = computerLoginCenterSize(parent, hgap, vgap);
        Dimension parentSize = parent.getSize();
        int x = (parentSize.width - size.width) / 2;
        ImageIcon icon = LoginUIConfig.getInstance().getCenterIcon();
        // int y = 225;
        int y = (parentSize.height - icon.getIconHeight()) / 3 + 129;
        Rectangle rect = new Rectangle(x, y, size.width, size.height);
        return rect;
    }
    
    protected Dimension computerLoginCenterSize(Container parent, int hgap, int vgap)
    {
        Component[] lblComps = {getLblBusiCenter(), getLblUserCode(), getLblPWD(), getForgetPWD()};
        Component[] ctrlComps = {getCbbBusiCenter(), getTfUserCode(), getPfUserPWD()};
        int lblsHeight = 0;
        int lblsMaxWidth = 0;
        for (int i = 0; i < lblComps.length; i++)
        {
            Component lblComp = lblComps[i];
            if (needLayout(parent, lblComp))
            {
                Dimension lblDim = lblComp.getPreferredSize();
                lblsHeight += lblDim.height + vgap;
                if (lblsMaxWidth < lblDim.width)
                {
                    lblsMaxWidth = lblDim.width;
                }
            }
        }
        if (lblsHeight > 0)
        {
            lblsHeight -= vgap;
        }
        int ctrlsHeight = 0;
        int ctrlsMaxWidth = 0;
        for (int i = 0; i < ctrlComps.length; i++)
        {
            Component ctrlComp = ctrlComps[i];
            if (needLayout(parent, ctrlComp))
            {
                Dimension ctrlDim = ctrlComp.getPreferredSize();
                ctrlsHeight += ctrlDim.height + vgap;
                if (ctrlsMaxWidth < ctrlDim.width)
                {
                    ctrlsMaxWidth = ctrlDim.width;
                }
            }
        }
        if (ctrlsHeight > 0)
        {
            ctrlsHeight -= vgap;
        }
        int w = lblsMaxWidth + hgap + ctrlsMaxWidth;
        int h = lblsHeight > ctrlsHeight ? lblsHeight : ctrlsHeight;
        return new Dimension(w, h);
    }
    
    public static Shape getRoundShape(int x, int y, int width, int height, Insets insets)
    {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + insets.left, y);
        path.lineTo(x + width - insets.right - 1, y);
        path.curveTo(x + width - insets.right / 2, y, x + width - 1, y + insets.top / 2, x + width - 1, y + insets.top);
        path.lineTo(x + width - 1, y + height - insets.bottom - 1);
        path.curveTo(x + width - 1, y + height - insets.bottom / 2 - 1, x + width - insets.right / 2 - 1, y + height - 1,
            x + width - insets.right - 1, y + height - 1);
        path.lineTo(x + insets.left, y + height - 1);
        path.curveTo(x + insets.left / 2, y + height - 1, x, y + height - insets.bottom / 2, x, y + height - insets.bottom - 1);
        path.lineTo(x, y + insets.top);
        path.curveTo(x, y + insets.top / 2, x + insets.left / 2, y, x + insets.left, y);
        return path;
    }
    
    public static void paintCtrlBorder(Graphics2D g, JComponent c, Color fillColor, Color borderColor, Shape shape, int w)
    {
        Object oldValue = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();
        BasicStroke bs = new BasicStroke(w);
        g.setStroke(bs);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fillColor);
        g.fill(shape);
        g.setColor(borderColor);
        g.draw(shape);
        g.setPaint(oldPaint);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValue);
        g.setStroke(oldStroke);
    }
    
    /**
     * 构造日志信息
     *
     * @param isSuccess
     * @return
     */
    private SecurityLogInfo writeSecurityLog(boolean isSuccess)
    {
        SecurityLogInfo logInfo = new SecurityLogInfo();
        List<SecurityLogVarItem> logItem = new ArrayList<SecurityLogVarItem>();
        SecurityLogVarItem item = new SecurityLogVarItem();
        logInfo.setAppTag("UAP");
        logInfo.setLogType("login");
        if (isSuccess)
        {
            logInfo.setLogLevel(60);
            logInfo.setLogAbs("Login successful");
            logInfo.setOpResult("success");
            item.setAttributeName("Token");
            item.setAttributeVar(NetStreamContext.getToken() == null ? "" : KeyUtil.encodeToken(NetStreamContext.getToken()));
            item.setSecret(true);
            logItem.add(item);
            logInfo.setOpDescript(logItem);
        }
        else
        {
            logInfo.setLogLevel(40);
            logInfo.setLogAbs("Login failed");
            logInfo.setOpResult("failure");
        }
        return logInfo;
    }
    
    /**
     * 获取服务
     *
     * @return
     */
    private SecurityLogManageFacade getService()
    {
        return NCLocator.getInstance().lookup(SecurityLogManageFacade.class);
    }
    
    public String loginActionForWebindex(String args)
    {
        return actionPerformed(args);
        
    }
    
    private String actionPerformed(String args)
    {
        
        String returnStr = "";
        try
        {
            LoginRequest request = testGetLoginRequest(args);
            
            setLoginContextVariables(args);
            //
            // UserExit.getInstance().setUserCode(request.getUserCode());
            //
            LoginAssistant loginAssi = createLoginAssistant(request);
            LoginResponse response = loginAssi.login();
            int result = response.getLoginResult();
            
            if (result == ILoginConstants.LOGIN_SUCCESS)
            {
                loginAssi.showWorkbench(response);
                loginEndInDT(result, null);
                storeLoginRequest(request);
            }
            else
            {
                String msg = loginAssi.getResultMessage(result);
                loginEndInDT(result, "" + msg);
                NetStreamContext.resetAll();
                returnStr = msg;
            }
        }
        catch (LoginVerifyException ex)
        {
            NetStreamContext.resetAll();
            String errMsg = ex.getMessage();
            if (errMsg == null || errMsg.trim().length() == 0)
            {
                errMsg = ex.getClass().getName() + ":" + errMsg;
            }
            Logger.error(ex.getMessage(), ex);
            loginEndInDT(ex.getLoginResult(), errMsg);
            returnStr = errMsg;
        }
        catch (Exception ex)
        {
            NetStreamContext.resetAll();
            String errMsg = ex.getMessage();
            if (errMsg == null || errMsg.trim().length() == 0)
            {
                errMsg = ex.getClass().getName() + ":" + errMsg;
            }
            Logger.error(ex.getMessage(), ex);
            loginEndInDT(ILoginConstants.UNKNOWN_ERROR, errMsg);
            returnStr = errMsg;
        }
        return returnStr + " ";
        
    }
    
    private void setLoginContextVariables(String arg)
    {
        String[] args = arg.split(",");
        InvocationInfoProxy.getInstance().reset();
        UserExit.getInstance().setUserCode(args[2].trim());
        UserExit.getInstance().setUserDataSource(args[4]);
        UserExit.getInstance().setBizCenterCode(args[1]);
    }
    
    private LoginRequest testGetLoginRequest(String arg)
    {
        String[] args = arg.split(",");
        LoginRequest request = new LoginRequest();
        BusiCenterVO bcVO = new BusiCenterVO();
        bcVO.setCode(args[1]);
        bcVO.setDataSourceName(args[4]);
        bcVO.setEffectDate(args[5]);
        bcVO.setExpireDate(args[6]);
        bcVO.setLocked((args[7] == null || args[7].equals("")) ? false : (args[7] == "true" ? true : false));
        bcVO.setName(args[8]);
        if (bcVO != null)
        {
            request.setBusiCenterCode(bcVO.getCode());
        }
        String userCode = args[2].trim();
        request.setUserCode(userCode);
        // 未输密码
        try
        {
            RSAUtil rsaUtil = new RSAUtil();
            request.setUserPWD(rsaUtil.decryptString(args[3].trim()));
        }
        catch (Exception e)
        {
            request.setUserPWD("");
        }
        
        request.setLangCode(args[0]);
        // 设置语种
        setLangCode(args[0].trim());
        String profileVersion = FunctionPermProfileManager.getInstance().getProfileVersion(userCode);
        request.putAttachProp(IFunctionPermProfileConst.FUNCTIONPERM_PROFILEVERSION_KEY, profileVersion);
        
        ClientSetup setup = ClientSetupCache.getGlobalClientSetup();
        String groupCode = (String) setup.get("loginGroup" + bcVO.getCode() + userCode);
        request.putAttachProp(GroupVO.class.getName() + ".code", groupCode);
        return request;
    }
    
    /**
     * 忘记密码，供web界面调用
     */
    public void forgetPwd(String para)
    {
        // BusiCenterVO bcVO = (BusiCenterVO)cbbBusiCenter.getSelectedItem();
        // String dsName = bcVO.getDataSourceName();
        String paras[] = para.split(",");
        // 设置语种
        setLangCode(paras[1].trim());
        InvocationInfoProxy.getInstance().setUserDataSource(paras[0].trim());
        PassWordWizardDialog wd = new PassWordWizardDialog(loginUIContainer, getSteps(paras[0].trim()), null);
        Dimension screan = Toolkit.getDefaultToolkit().getScreenSize();
        wd.setSize(647, 390);
        Dimension dlgsize = wd.getSize();
        wd.setLocation((screan.width - dlgsize.width) / 2, (screan.height - dlgsize.height) / 2 + 40);
        wd.showModal();
    }
    
    /**
     * 设置语种
     *
     * @param code
     */
    public String[] setLangCode(String code)
    {
        Language currLang = currLangMap.get(code);
        if (currLang == null)
        {
            currLang = NCLangRes.getInstance().getLanguage(code);
            currLangMap.put(code, NCLangRes.getInstance().getLanguage(code));
        }
        NCLangRes.getInstance().setCurrLanguage(currLang);
        UserExit.getInstance().setLangCode(code);
        new Thread()
        {
            @Override
            public void run()
            {
                NCLangRes.getInstance().getStrByID("MENUCODE", "10");
                NCLangRes.getInstance().getStrByID(IProductCode.PRODUCTCODE_FUNCODE, "10");
                
            }
            
        }.start();
        return getLoginjspLang();
    }
    
    private String[] getLoginjspLang()
    {
        String disText[] = new String[18];
        String webstart = NCLangRes.getInstance().getStrByID("sfbase", "webstart-0001")/* 使用全屏模式，您将获得更好的体验。 */;
        String forgetPwd = NCLangRes.getInstance().getStrByID("loginui", "loginuires-000007")/* 忘记密码 */;
        String disid = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000273")/* 显示资源ID号 */;
        String ckbZipStream = NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000246")/* 压缩远程调用流 */;
        
        disText[0] = NCLangRes.getInstance().getStrByID("loginui", "loginuires-000000");
        disText[1] = NCLangRes.getInstance().getStrByID("loginui", "loginuires-000001");
        disText[2] = NCLangRes.getInstance().getStrByID("loginui", "loginuires-000002")/* 密码 */;
        disText[3] = (webstart.replaceAll("，", ",")).replaceAll("。", ".");/* -=notranslate=- */
        disText[4] = NCLangRes.getInstance().getStrByID("sfbase", "webstart-0002")/* 立即使用 */;
        disText[5] =
            NCLangRes.getInstance().getStrByID("loginui", "loginuires-000005")/* 版权所有 (c) 2012 用友软件股份有限公司 */;
        disText[6] = forgetPwd.replaceAll("？", "?");/* -=notranslate=- */
        disText[7] = ckbZipStream + " ";// 处理jsp乱码/*-=notranslate=-*/
        disText[8] = disid + " ";/* -=notranslate=- */
        disText[9] = NCLangRes.getInstance().getStrByID("sfbase", "webstart-0003")/* 全屏模式 */;
        disText[10] = NCLangRes.getInstance().getStrByID("sysframev5", "UPPsysframev5-000060")/* 选项 */;
        disText[11] = NCLangRes.getInstance().getStrByID("loginui",
            "loginuiresExt-000001")/*
                                    * 如果您已安装了Jave Runtime Environment，可能是浏览器还没有正常加载JRE，请重启浏览器后重试。如果您还未安装Jave
                                    * Runtime Environment，请先安装。
                                    */;
        disText[12] = NCLangRes.getInstance().getStrByID("loginui",
            "loginuiresExt-000002")/* 您电脑上可能没有安装JRE插件或者该插件已被浏览器拦截，如果您确认没有安装JRE插件，请单击\"确定\"，页面将跳转到插件下载地址 */;
        disText[13] = NCLangRes.getInstance().getStrByID("loginui",
            "loginuiresExt-000003")/* 如果不安装Jave Runtime Environment，将不能正常登录系统。单击\"确定\"跳转到下载地址，是否继续？ */;
        disText[14] = NCLangRes.getInstance().getStrByID("loginui", "loginuiresExt-000004")/* 请选择系统 */;
        disText[15] = NCLangRes.getInstance().getStrByID("loginui", "loginuiresExt-000005")/* 用户不能为空 */;
        disText[16] = NCLangRes.getInstance().getStrByID("loginui", "loginuiresExt-000006")/* 密码不能为空 */;
        disText[17] = NCLangRes.getInstance().getStrByID("loginui",
            "loginuiresExt-000007");/* Applet初始化超时，请检查JRE插件是否已被浏览器阻止 */
        return disText;
    }
    
    private JButton btnUserNotice = null;
    private JButton btnOffice = null;
    private JButton btnFinance = null;
    String currentPath = this.getClass().getResource("urlconfig.properties").getPath();
    
    /**
     * *********************************************************** <br>
     * *说明： 新华社财务资产管理系统<br>
     *
     * @return <br>
     * @JButton <br>
     * @methods nc.login.ui.LoginUISupport#getFinance <br>
     * @author LiBencheng <br>
     * @date Created on 2023年12月12日 <br>
     * @time 17:26:38 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    public JButton getFinance()
    {
        if (btnFinance == null)
        {
            // btnFinance = new JButton("财务资产管理系统");
            try
            {
                btnFinance = new JButton(PropertiesUtil.getValueByKey(currentPath, "batname1"));
            }
            catch (IOException e)
            {
            }
            btnFinance.setUI(new CustomUI());
            btnFinance.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            btnFinance.setOpaque(false);
            btnFinance.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnFinance.setRolloverEnabled(true);
            btnFinance.setBorder(BorderFactory.createEmptyBorder());
            btnFinance.setHorizontalAlignment(JButton.CENTER);
            btnFinance.addActionListener(new ActionListener()
            {
                
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        System.out.println("当前路径：" + currentPath);
                        String url = PropertiesUtil.getValueByKey(currentPath, "url1");
                        // 创建URI对象
                        URI uri = new URI(url);
                        // 调用默认浏览器打开网页
                        Desktop.getDesktop().browse(uri);
                    }
                    catch (Exception e)
                    {
                        LoggerUtil.thr("新华社财务资产管理系统-调用默认浏览器打开网页异常：", e);
                    }
                }
            });
        }
        return btnFinance;
    }
    
    /**
     * *********************************************************** <br>
     * *说明：移动办公平台下载按钮 <br>
     *
     * @return <br>
     * @JButton <br>
     * @methods nc.login.ui.LoginUISupport#getOffice <br>
     * @author LiBencheng <br>
     * @date Created on 2023年12月12日 <br>
     * @time 17:26:31 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    public JButton getOffice()
    {
        if (btnOffice == null)
        {
            // btnOffice = new JButton("移动办公平台下载");
            try
            {
                btnOffice = new JButton(PropertiesUtil.getValueByKey(currentPath, "batname2"));
            }
            catch (IOException e)
            {
            }
            btnOffice.setUI(new CustomUI());
            btnOffice.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            btnOffice.setOpaque(false);
            btnOffice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnOffice.setRolloverEnabled(true);
            btnOffice.setBorder(BorderFactory.createEmptyBorder());
            btnOffice.setHorizontalAlignment(JButton.CENTER);
            btnOffice.addActionListener(new ActionListener()
            {
                
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        System.out.println("当前路径：" + currentPath);
                        String url = PropertiesUtil.getValueByKey(currentPath, "url2");
                        // 创建URI对象
                        URI uri = new URI(url);
                        // 调用默认浏览器打开网页
                        Desktop.getDesktop().browse(uri);
                    }
                    catch (Exception e)
                    {
                        LoggerUtil.thr("移动办公平台下载-调用默认浏览器打开网页异常：", e);
                    }
                }
            });
        }
        return btnOffice;
    }
    
    /**
     * *********************************************************** <br>
     * *说明： 用户须知按钮<br>
     *
     * @return <br>
     * @JButton <br>
     * @methods nc.login.ui.LoginUISupport#getUserNotice <br>
     * @author LiBencheng <br>
     * @date Created on 2023年12月12日 <br>
     * @time 17:26:21 <br>
     * @version 1.0 <br>
     *          ************************************************************ <br>
     * @see <br>
     */
    public JButton getUserNotice()
    {
        if (btnUserNotice == null)
        {
            
            // btnOffice = new JButton("用户须知");
            try
            {
                btnUserNotice = new JButton(PropertiesUtil.getValueByKey(currentPath, "batname3"));
            }
            catch (IOException e)
            {
            }
            btnUserNotice.setUI(new CustomUI());
            btnUserNotice.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            btnUserNotice.setOpaque(false);
            btnUserNotice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnUserNotice.setRolloverEnabled(true);
            btnUserNotice.setBorder(BorderFactory.createEmptyBorder());
            btnUserNotice.setHorizontalAlignment(JButton.CENTER);
            btnUserNotice.addActionListener(new ActionListener()
            {
                
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        System.out.println("当前路径：" + currentPath);
                        String url = PropertiesUtil.getValueByKey(currentPath, "url3");
                        // 创建URI对象
                        URI uri = new URI(url);
                        // 调用默认浏览器打开网页
                        Desktop.getDesktop().browse(uri);
                    }
                    catch (Exception e)
                    {
                        LoggerUtil.thr("用户须知-调用默认浏览器打开网页异常：", e);
                    }
                }
            });
        }
        return btnUserNotice;
    }
    
    // { xull3 增加验证码
    private JTextField tfCaptcha = null;
    private JPanel pnlCaptcha = null;
    private JLabel lblCaptcha = null;
    
    private String sessionid = null;;
    // private JLabel lblCaptchaImg = null;
    private String captchaKey = null;
    // } xull3 增加验证码
    
    String groupId = "0001A71000000000037S";
    
    /**
     * 设置token
     */
    private void setToken()
    {
        Map<String, String> configInfo = CollectionUtil.newHashMap();
        try
        {
            configInfo = PropertiesUtil.getAllProperties(currentPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        byte[] token = "06864af8f99181976d12d66a24e5916e".getBytes();
        NetStreamContext.setToken(token);
        InvocationInfoProxy.getInstance().setUserDataSource(configInfo.get("groupId"));
        InvocationInfoProxy.getInstance().setGroupId(configInfo.get("groupId"));
        InvocationInfoProxy.getInstance().setUserCode(configInfo.get("userCode"));
        // InvocationInfoProxy.getInstance().setUserCode("#UAP#");
        
    }
    
    // 增加验证码 xull3
    public JTextField getTfCaptcha()
    {
        if (tfCaptcha == null)
        {
            Dimension myctrlSize = new Dimension(ctrlSize.width / 2, ctrlSize.height);
            Dimension mydefCtrlSize = new Dimension(defCtrlSize.width / 2, defCtrlSize.height);
            if (LoginUIConfig.getInstance().isCustomLoginUI())
            {
                tfCaptcha = new JTextField();
                tfCaptcha.setSize(myctrlSize);
                tfCaptcha.setPreferredSize(myctrlSize);
                tfCaptcha.setFont(getControlFont());
                tfCaptcha.addKeyListener(keyHandler);
            }
            else
            {
                tfCaptcha = new JTextField()
                {
                    private static final long serialVersionUID = 1L;
                    private Insets insets = new Insets(6, 6, 6, 6);
                    private Color borderColor = LoginUIConfig.getInstance().getInputCtrlBorderColor();
                    private Color borderFocusColor = LoginUIConfig.getInstance().getInputCtrlBorderFocusColor();
                    
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        Dimension size = getSize();
                        Shape shape = getRoundShape(0, 0, size.width, size.height, insets);
                        Color bColor = this.isFocusOwner() ? borderFocusColor : borderColor;
                        int w = this.isFocusOwner() ? 2 : 1;
                        paintCtrlBorder((Graphics2D) g, this, Color.white, bColor, shape, w);
                        super.paintComponent(g);
                    }
                    
                };
                tfCaptcha.putClientProperty("caretWidth", Integer.valueOf(2));
                tfCaptcha.setCaretColor(LoginUIConfig.getInstance().getInputCtrlBorderFocusColor());
                tfCaptcha.setOpaque(false);
                tfCaptcha.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
                tfCaptcha.setFont(getControlFont());
                tfCaptcha.addKeyListener(keyHandler);
                tfCaptcha.setSize(mydefCtrlSize);
                tfCaptcha.setPreferredSize(mydefCtrlSize);
                tfCaptcha.getDocument().addDocumentListener(documentHandler);
            }
            tfCaptcha.setUI(new MyTextFieldUI());
        }
        return tfCaptcha;
    }
    
    public String getSessionid()
    {
        return sessionid;
    }
    
    public void setSessionid(String sessionid)
    {
        this.sessionid = sessionid;
    }
    
    public JLabel getLblCaptcha()
    {
        if (lblCaptcha == null)
        {
            lblCaptcha = new JLabel("验证码"); // TODO 若需要支持多语，修改此处
            lblCaptcha.setFont(getLblFont());
            lblCaptcha.setForeground(LoginUIConfig.getInstance().getMainCtrlFgColor());
        }
        return lblCaptcha;
    }
    
    public JPanel getPnlCaptcha()
    {
        if (pnlCaptcha == null)
        {
            pnlCaptcha = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            pnlCaptcha.setSize(ctrlSize);
            pnlCaptcha.setPreferredSize(defCtrlSize);
            pnlCaptcha.setOpaque(false);
            pnlCaptcha.add(getTfCaptcha());
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
            btnPanel.add(getbtnGetDxMessage());
            // btnPanel.add(getTimerLaber());
            // getTimerLaber().setVisible(false);
            pnlCaptcha.add(btnPanel);
        }
        return pnlCaptcha;
    }
    
    /**
     * 获取校验码
     *
     * @return
     */
    public JButton getbtnGetDxMessage()
    {
        if (btnGetDxMessage == null)
        {
            
            btnGetDxMessage = new JButton("发送验证码");
            btnGetDxMessage.setUI(new CustomUI());
            btnGetDxMessage.setFont(new Font(Style.getFontname(), Font.PLAIN, 12));
            btnGetDxMessage.setOpaque(false);
            btnGetDxMessage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnGetDxMessage.setRolloverEnabled(true);
            btnGetDxMessage.setBorder(BorderFactory.createEmptyBorder());
            btnGetDxMessage.setHorizontalAlignment(JButton.RIGHT);
            btnGetDxMessage.setBorderPainted(false);
            btnGetDxMessage.setContentAreaFilled(false);
            btnGetDxMessage.addActionListener(new ActionListener()
            {
                
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    
                    // 调用短信平台发送验证码
                    String loginCode = getTfUserCode().getText().trim();
                    if (loginCode == null || loginCode.equals(""))
                    {
                        loginEndInDT(-1, "用户名不能为空！");
                        return;
                    }
                    String loginWord = new String(getPfUserPWD().getPassword()).trim();
                    if (loginWord == null || loginWord.equals(""))
                    {
                        loginEndInDT(-1, "用户密码不能为空！");
                        return;
                    }
                    try
                    {
                        // 20250610 libc yy3
                        // 1)生成（/cert/aam/uniformemployee/verifycodecreate/V2）
                        // setToken();
                        
                        // ClogVO clogVO2 = ApiLogUtil.newInitCLog();
                        // clogVO2.setAction("createVerifyCode");
                        // clogVO2.setName("xxxxxxxxxx");
                        // ApiLogUtil.getInstance().insertSendLog(clogVO2);
                        
                        NC_Itf_SsoServer ncItfSsoServer = YonYouUtilbc.NCLocator(NC_Itf_SsoServer.class);
                        // NC_Itf_SsoServer ncItfSsoServer = new NC_Impl_SsoServer();
                        Map<String, String> configInfo = ncItfSsoServer.getConfigInfo();
                        
                        if (PubEnvUtil.isEmpty(InvocationInfoProxy.getInstance().getUserDataSource()))
                        {
                            InvocationInfo invocationInfo = InvocationInfoUtils.getInvocationInfo();
                            invocationInfo.setUserDataSource(configInfo.get("dataSource"));
                            InvocationInfoProxy.getInstance().setUserDataSource(configInfo.get("dataSource"));
                        }
                        String enableCsSSO = configInfo.get("enableCsSSO").toLowerCase();
                        Boolean enableSSO = Boolean.TRUE;
                        if (PubEnvUtil.isNotEmptyObj(configInfo.get("user.enable")))
                        {
                            String[] users = configInfo.get("user.enable").split(IPubCons.COMMA);
                            enableSSO = !PubEnvUtil.containAllStr(loginCode, users);
                        }
                        if (enableSSO && PubEnvUtil.equals("y", enableCsSSO) || PubEnvUtil.equals("true", enableCsSSO))
                        {
                            
                            ClogVO clogVO = null;
                            try
                            {
                                // clogVO = new NC_Impl_SsoServer().createVerifyCode(loginCode,
                                // InetAddress.getLocalHost().getHostAddress());
                                clogVO = ncItfSsoServer.createVerifyCode(loginCode, InetAddress.getLocalHost().getHostAddress());
                            }
                            catch (UnknownHostException e)
                            {
                                YonLogUtil.debug(Boolean.TRUE, StringUtil.toString(e));
                                loginEnd(0, e.getMessage());
                                return;
                            }
                            if (PubEnvUtil.unEquals(InumberCons.ZERO, clogVO.getDef01()))
                            {
                                YonLogUtil.debug(Boolean.TRUE, "verifycodecreate: " + clogVO.getDef02());
                                loginEnd(-1, clogVO.getDef02());
                                // loginEndInDT( -1, clogVO.getDef02());
                                return;
                            }
                            else
                            {
                                verifyRequestNo = clogVO.getToken();
                                loginEnd(0, clogVO.getDef02());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        YonLogUtil.debug(Boolean.TRUE, StringUtil.toString(e));
                        Log.getInstance("gylc").info("getbtnGetDxMessage: " + StringUtil.toString(e));
                        loginEnd(0, e.getMessage());
                    }
                }
            });
        }
        return btnGetDxMessage;
    }
    
    String verifyRequestNo = "";
    
    public String getCaptchaKey()
    {
        if (captchaKey == null)
        {
            captchaKey = UUIDGenerator.getInstance().generateTimeBasedUUID().toString().replace("-", "");
        }
        return captchaKey;
    }
    
    // public JLabel getTimerLaber(){
    // if(timerLabel==null){
    // timerLabel=new JLabel();
    // }
    // return timerLabel;
    // }
    
    // public Timer getTimer(JLabel componet){
    // final JLabel label=componet;
    // Timer timer = new Timer(1000, new ActionListener() {
    // int time=180;
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // // 显示日期时间到JLabel标签中
    // label.setText(""+time);
    // time--;
    // }
    // });
    // return timer;
    // }
    
    // public JLabel getLblCaptchaImg() {
    // if (lblCaptchaImg == null) {
    // lblCaptchaImg = new JLabel();
    // lblCaptchaImg.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    // lblCaptchaImg.setSize(new Dimension(ctrlSize.width / 2 + 20, ctrlSize.height));
    // lblCaptchaImg.setPreferredSize(new Dimension(defCtrlSize.width / 2 + 20, defCtrlSize.height));
    // lblCaptchaImg.setToolTipText("点击图片刷新验证码");
    // lblCaptchaImg.addMouseListener(new MouseAdapter() {
    // public void mouseClicked(MouseEvent e) {
    // loadCaptchaImage();
    // }
    // });
    // }
    // return lblCaptchaImg;
    // }
    
    // public void loadCaptchaImage() {
    // try {
    // byte[] imageData =
    // NCLocator.getInstance().lookup(ICaptchaService.class).getCaptchaImage(getCaptchaKey());
    // getLblCaptchaImg().setIcon(new ImageIcon(imageData));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}
