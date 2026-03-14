package nc.bs.mw.fm;

import java.io.Serializable;
import java.net.InetAddress;

import pers.bc.utils.pub.PropertiesUtil;

public abstract class LoggerAbs implements Serializable
{
    static String CRLF = "\r\n";
    private static final long serialVersionUID = 4696941928937971349L;
    
    public static StringBuffer getBeautyWomanStr()
    {
        StringBuffer womanStr = new StringBuffer();
        womanStr.append(CRLF);
        womanStr.append(CRLF);
        womanStr.append("                       .::::.                                      ").append(CRLF);
        womanStr.append("                     .::::::::.                                    ").append(CRLF);
        womanStr.append("                    :::::::::::                                    ").append(CRLF);
        womanStr.append("                 ..:::::::::::'                                    ").append(CRLF);
        womanStr.append("              '::::::::::::'                                       ").append(CRLF);
        womanStr.append("                .::::::::::                                        ").append(CRLF);
        womanStr.append("           '::::::::::::::..                                       ").append(CRLF);
        womanStr.append("                ..::::::::::::.                                    ").append(CRLF);
        womanStr.append("              ``::::::::::::::::                                   ").append(CRLF);
        womanStr.append("               ::::``:::::::::'        .:::.                       ").append(CRLF);
        womanStr.append("              ::::'   ':::::'       .::::::::.                     ").append(CRLF);
        womanStr.append("            .::::'      ::::     .:::::::'::::.                    ").append(CRLF);
        womanStr.append("           .:::'       :::::  .:::::::::' ':::::.                  ").append(CRLF);
        womanStr.append("          .::'        :::::.:::::::::'      ':::::.                ").append(CRLF);
        womanStr.append("         .::'         ::::::::::::::'         ``::::.              ").append(CRLF);
        womanStr.append("     ...:::           ::::::::::::'              ``::.             ").append(CRLF);
        womanStr.append("    ```` ':.          ':::::::::'                  ::::..          ").append(CRLF);
        womanStr.append("                       '.:::::'                    ':'````..       ").append(CRLF);
        womanStr.append(CRLF);
        womanStr.append(CRLF);
        
        return womanStr;
    }
    
    public static StringBuffer getWomanStr()
    {
        StringBuffer womanStr = new StringBuffer();
        womanStr.append("").append(CRLF);
        womanStr.append("                  _.._        ,------------.").append(CRLF);
        womanStr.append("               ,'      `.    (  I want you! )").append(CRLF);
        womanStr.append("              /  __) __` \\    `-,----------'").append(CRLF);
        womanStr.append("             (  (`-`(-')  ) _.-'").append(CRLF);
        womanStr.append("             /)  \\  = /  (").append(CRLF);
        womanStr.append("            /'    |--' . \\ \\").append(CRLF);
        womanStr.append("           (  ,---|  `-.)__`").append(CRLF);
        womanStr.append("            )(  `-.,--'   _`-.").append(CRLF);
        womanStr.append("           '/,'          (  Uu\",").append(CRLF);
        womanStr.append("            (_       ,    `/,-' )").append(CRLF);
        womanStr.append("            `.__,  : `-'/  /`--'").append(CRLF);
        womanStr.append("              |     `--'  |").append(CRLF);
        womanStr.append("              `   `-._   /").append(CRLF);
        womanStr.append("               \\        (\\").append(CRLF);
        womanStr.append("               /\\ .      \\.  ").append(CRLF);
        womanStr.append("              / |` \\     ,-\\").append(CRLF);
        womanStr.append("             /  \\| .)   /   \\").append(CRLF);
        womanStr.append("            ( ,'|\\    ,'     :").append(CRLF);
        womanStr.append("            | \\,`.`--\"/      }").append(CRLF);
        womanStr.append("            `,'    \\  |,'    /").append(CRLF);
        womanStr.append("           / \"-._   `-/      |").append(CRLF);
        womanStr.append("           \"-.   \"-.,'|     ;").append(CRLF);
        womanStr.append("          /        _/[\"---'\"\"]").append(CRLF);
        womanStr.append("         :        /  |\"-     '").append(CRLF);
        womanStr.append("         '           |      /").append(CRLF);
        womanStr.append("                     `      |").append(CRLF);
        womanStr.append("").append(CRLF);
        
        return womanStr;
    }
    
    public static StringBuffer getDangerStr()
    {
        StringBuffer dangerStr = new StringBuffer();
        dangerStr.append("").append(CRLF);
        dangerStr.append("  **************************************************************").append(CRLF);
        dangerStr.append("  *                                                            *").append(CRLF);
        dangerStr.append("  *    .=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-.      *").append(CRLF);
        dangerStr.append("  *    |                     ______                     |      *").append(CRLF);
        dangerStr.append("  *    |                  .-\"      \"-.                  |      *").append(CRLF);
        dangerStr.append("  *    |                 /            \\                 |      *").append(CRLF);
        dangerStr.append("  *    |     _          |              |          _     |      *").append(CRLF);
        dangerStr.append("  *    |    ( \\         |,  .-.  .-.  ,|         / )    |      *").append(CRLF);
        dangerStr.append("  *    |     > \"=._     | )(__/  \\__)( |     _.=\" <     |      *").append(CRLF);
        dangerStr.append("  *    |    (_/\"=._\"=._ |/     /\\     \\| _.=\\\"_.=\"\\_)   |      *").append(CRLF);
        dangerStr.append("  *    |           \"=._\"(_     ^^     _)\"_.=\"           |      *").append(CRLF);
        dangerStr.append("  *    |               \"=\\__|IIIIII|__/=\"               |      *").append(CRLF);
        dangerStr.append("  *    |              _.=\"| \\IIIIII/ |\"=._              |      *").append(CRLF);
        dangerStr.append("  *    |    _     _.=\"_.=\"\\          /\"=._\"=._     _    |      *").append(CRLF);
        dangerStr.append("  *    |   ( \\_.=\"_.=\"     `--------`     \"=._\"=._/ )   |      *").append(CRLF);
        dangerStr.append("  *    |    > _.=\"                            \"=._ <    |      *").append(CRLF);
        dangerStr.append("  *    |   (_/                                    \\_)   |      *").append(CRLF);
        dangerStr.append("  *    |                                                |      *").append(CRLF);
        dangerStr.append("  *    '-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-='      *").append(CRLF);
        dangerStr.append("  *                                                            *").append(CRLF);
        dangerStr.append("  *           LASCIATE OGNI SPERANZA, VOI CH'ENTRATE           *").append(CRLF);
        dangerStr.append("  **************************************************************").append(CRLF);
        dangerStr.append("").append(CRLF);
        
        return dangerStr;
    }
    
    public static StringBuffer getOfficeStr()
    {
        StringBuffer bookStr = new StringBuffer();
        
        bookStr.append("                 ,----------------,              ,---------,").append(CRLF);
        bookStr.append("            ,-----------------------,          ,\"        ,\"|").append(CRLF);
        bookStr.append("          ,\"                      ,\"|        ,\"        ,\"  |").append(CRLF);
        bookStr.append("         +-----------------------+  |      ,\"        ,\"    |").append(CRLF);
        bookStr.append("         |  .-----------------.  |  |     +---------+      |").append(CRLF);
        bookStr.append("         |  |                 |  |  |     | -==----'|      |").append(CRLF);
        bookStr.append("         |  |  I LOVE DOS!    |  |  |     |         |      |").append(CRLF);
        bookStr.append("         |  |  Bad command or |  |  |/----|`---=    |      |").append(CRLF);
        bookStr.append("         |  |  C:\\>_          |  |  |   ,/|==== ooo |      ;").append(CRLF);
        bookStr.append("         |  |                 |  |  |  // |(((( [33]|    ,\"").append(CRLF);
        bookStr.append("         |  `-----------------'  |,\" .;'| |((((     |  ,\"").append(CRLF);
        bookStr.append("         +-----------------------+  ;;  | |         |,\"").append(CRLF);
        bookStr.append("            /_)______________(_/  //'   | +---------+").append(CRLF);
        bookStr.append("       ___________________________/___  `,").append(CRLF);
        bookStr.append("      /  oooooooooooooooo  .o.  oooo /,   \\,\"-----------").append(CRLF);
        bookStr.append("     / ==ooooooooooooooo==.o.  ooo= //   ,`\\--{)B     ,\"").append(CRLF);
        bookStr.append("    /_==__==========__==_ooo__ooo=_/'   /___________,\"").append(CRLF);
        bookStr.append("   ").append(CRLF);
        
        return bookStr;
    }
    
    public static StringBuffer getBookStr()
    {
        StringBuffer bookStr = new StringBuffer();
        bookStr.append("   ").append(CRLF);
        bookStr.append("                    .-~~~~~~~~~-._       _.-~~~~~~~~~-.").append(CRLF);
        bookStr.append("                __.'              ~.   .~              `.__").append(CRLF);
        bookStr.append("              .'//                 \\./                  \\\\`.").append(CRLF);
        bookStr.append("            .'//                     |                     \\\\`.").append(CRLF);
        bookStr.append("          .'// .-~\"\"\"\"\"\"\"~~~~-._     |     _,-~~~~\"\"\"\"\"\"\"~-. \\\\`.").append(CRLF);
        bookStr.append("        .'//.-\"                 `-.  |  .-'                 \"-.\\\\`.").append(CRLF);
        bookStr.append("      .'//______.============-..   \\ | /   ..-============.______\\\\`.").append(CRLF);
        bookStr.append("    .'______________________________\\|/______________________________`.").append(CRLF);
        bookStr.append("").append(CRLF);
        bookStr.append("").append(CRLF);
        
        return bookStr;
    }
    
    public static StringBuffer getKeyBoardStr()
    {
        StringBuffer str = new StringBuffer();
        
        str.append("   ┌───┐   ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐").append(CRLF);
        str.append("   │Esc│   │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│  ┌┐    ┌┐    ┌┐").append(CRLF);
        str.append("   └───┘   └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘  └┘    └┘    └┘").append(CRLF);
        str.append("   ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐ ┌───┬───┬───┐ ┌───┬───┬───┬───┐").append(CRLF);
        str.append("   │~ `│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp │ │Ins│Hom│PUp│ │N L│ / │ * │ - │").append(CRLF);
        str.append("   ├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤ ├───┼───┼───┤ ├───┼───┼───┼───┤").append(CRLF);
        str.append("   │ Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \\ │ │Del│End│PDn│ │ 7 │ 8 │ 9 │   │").append(CRLF);
        str.append("   ├─────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤ └───┴───┴───┘ ├───┼───┼───┤ + │").append(CRLF);
        str.append("   │ Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│\" '│ Enter  │               │ 4 │ 5 │ 6 │   │").append(CRLF);
        str.append("   ├──────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤     ┌───┐     ├───┼───┼───┼───┤").append(CRLF);
        str.append("   │ Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │     │ ↑ │     │ 1 │ 2 │ 3 │   │").append(CRLF);
        str.append("   ├─────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤ ┌───┼───┼───┐ ├───┴───┼───┤ E││").append(CRLF);
        str.append("   │ Ctrl│    │Alt │         Space         │ Alt│    │    │Ctrl│ │ ← │ ↓ │ → │ │   0   │ . │←─┘│").append(CRLF);
        str.append("   └─────┴────┴────┴───────────────────────┴────┴────┴────┴────┘ └───┴───┴───┘ └───────┴───┴───┘").append(CRLF);
        str.append("     勇敢的少年啊快去创造奇迹এ⁵²º ").append(CRLF);
        str.append("").append(CRLF);
        
        return str;
    }
    
    public static StringBuffer getXDDStr()
    {
        StringBuffer xddStr = new StringBuffer();
        xddStr.append("").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⠟⠋⠄⠄⠄⠄⠄⠄⠄⢁⠈⢻⢿⣿⣿⣿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⠃⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠈⡀⠭⢿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⡟⠄⢀⣾⣿⣿⣿⣷⣶⣿⣷⣶⣶⡆⠄⠄⠄⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⡇⢀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧⠄⠄⢸⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣇⣼⣿⣿⠿⠶⠙⣿⡟⠡⣴⣿⣽⣿⣧⠄⢸⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⣾⣿⣿⣟⣭⣾⣿⣷⣶⣶⣴⣶⣿⣿⢄⣿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⣿⣿⣿⡟⣩⣿⣿⣿⡏⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⣿⣹⡋⠘⠷⣦⣀⣠⡶⠁⠈⠁⠄⣿⣿⣿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⣿⣍⠃⣴⣶⡔⠒⠄⣠⢀⠄⠄⠄⡨⣿⣿⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⣿⣿⣿⣦⡘⠿⣷⣿⠿⠟⠃⠄⠄⣠⡇⠈⠻⣿⣿⣿⣿  ").append(CRLF);
        xddStr.append("  ⣿⣿⣿⣿⡿⠟⠋⢁⣷⣠⠄⠄⠄⠄⣀⣠⣾⡟⠄⠄⠄⠄⠉⠙⠻  ").append(CRLF);
        xddStr.append("  ⡿⠟⠋⠁⠄⠄⠄⢸⣿⣿⡯⢓⣴⣾⣿⣿⡟⠄⠄⠄⠄⠄⠄⠄⠄  ").append(CRLF);
        xddStr.append("  ⠄⠄⠄⠄⠄⠄⠄⣿⡟⣷⠄⠹⣿⣿⣿⡿⠁⠄⠄⠄⠄⠄⠄⠄⠄  ").append(CRLF);
        xddStr.append("").append(CRLF);
        
        return xddStr;
    }
    
    public static StringBuffer getFoStr()
    {
        StringBuffer msg = new StringBuffer();
        msg.append(CRLF);
        msg.append("\\\\ \\\\ \\\\ \\\\ \\\\ \\\\ \\\\ \\\\ || || || || || || // // // // // // // //").append(CRLF);
        msg.append("\\\\ \\\\ \\\\ \\\\ \\\\ \\\\ \\\\        _ooOoo_          // // // // // // //").append(CRLF);
        msg.append("\\\\ \\\\ \\\\ \\\\ \\\\ \\\\          o8888888o            // // // // // //").append(CRLF);
        msg.append("\\\\ \\\\ \\\\ \\\\ \\\\             88\" . \"88               // // // // //").append(CRLF);
        msg.append("\\\\ \\\\ \\\\ \\\\                (| -_- |)                  // // // //").append(CRLF);
        msg.append("\\\\ \\\\ \\\\                   O\\  =  /O                     // // //").append(CRLF);
        msg.append("\\\\ \\\\                   ____/`---'\\____                     // //").append(CRLF);
        msg.append("\\\\                    .'  \\\\|     |//  `.                      //").append(CRLF);
        msg.append("==                   /  \\\\|||  :  |||//  \\                     ==").append(CRLF);
        msg.append("==                  /  _||||| -:- |||||-  \\                    ==").append(CRLF);
        msg.append("==                  |   | \\\\\\  -  /// |   |                    ==").append(CRLF);
        msg.append("==                  | \\_|  ''\\---/''  |   |                    ==").append(CRLF);
        msg.append("==                  \\  .-\\__  `-`  ___/-. /                    ==").append(CRLF);
        msg.append("==                ___`. .'  /--.--\\  `. . ___                  ==").append(CRLF);
        msg.append("==              .\"\" '<  `.___\\_<|>_/___.'  >'\"\".               ==").append(CRLF);
        msg.append("==            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |              \\\\").append(CRLF);
        msg.append("//            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /              \\\\").append(CRLF);
        msg.append("//      ========`-.____`-.___\\_____/___.-`____.-'========      \\\\").append(CRLF);
        msg.append("//                           `=---='                           \\\\").append(CRLF);
        msg.append("// //   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  \\\\ \\\\").append(CRLF);
        msg.append("// // //      佛祖保佑      永无BUG      永不修改        \\\\ \\\\ \\\\").append(CRLF);
        msg.append("// // // // // // || || || || || || || || || || \\\\ \\\\ \\\\ \\\\ \\\\ \\\\").append(CRLF);
        msg.append(CRLF);
        return msg;
    }
    
    public static StringBuffer getThrowStr()
    {
        StringBuffer throwStr = new StringBuffer();
        throwStr.append(CRLF);
        throwStr.append(CRLF);
        throwStr.append(" ...........................我佛慈悲................................ ").append(CRLF);
        throwStr.append("//                          _ooOoo_                               //").append(CRLF);
        throwStr.append("//                         o8888888o                              //").append(CRLF);
        throwStr.append("//                         88\" . \"88                              //").append(CRLF);
        throwStr.append("//                         (| ^_^ |)                              //").append(CRLF);
        throwStr.append("//                         O\\  =  /O                              //").append(CRLF);
        throwStr.append("//                      ____/`---'\\____                           //").append(CRLF);
        throwStr.append("//                    .'  \\\\|     |//  `.                         //").append(CRLF);
        throwStr.append("//                   /  \\\\|||  :  |||//  \\                        //").append(CRLF);
        throwStr.append("//                  /  _||||| -卍-|||||-  \\                       //").append(CRLF);
        throwStr.append("//                  |   | \\\\\\  -  /// |   |                       //").append(CRLF);
        throwStr.append("//                  | \\_|  ''\\---/''  |   |                       //").append(CRLF);
        throwStr.append("//                  \\  .-\\__  `-`  ___/-. /                       //").append(CRLF);
        throwStr.append("//                ___`. .'  /--.--\\  `. . ___                     //").append(CRLF);
        throwStr.append("//              .\"\" '<  `.___\\_<|>_/___.'  >'\"\".                  //").append(CRLF);
        throwStr.append("//            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 //").append(CRLF);
        throwStr.append("//            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 //").append(CRLF);
        throwStr.append("//      ========`-.____`-.___\\_____/___.-`____.-'========         //").append(CRLF);
        throwStr.append("//                           `=---='                              //").append(CRLF);
        throwStr.append("//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        //").append(CRLF);
        throwStr.append("//                佛祖保佑       永无BUG     永不修改             //").append(CRLF);
        throwStr.append(CRLF);
        throwStr.append(CRLF);
        
        return throwStr;
    }
    
    public static StringBuffer getNacosStr()
    {
        StringBuffer womanStr = new StringBuffer();
        womanStr.append(CRLF);
        womanStr.append(CRLF);
        womanStr.append(" ,--.").append(CRLF);
        womanStr.append(" ,--.'|").append(CRLF);
        womanStr.append(" ,--,:  : |                                             ").append(CRLF);
        womanStr.append(" ,`--.'`|  ' :                       ,---.              ").append(CRLF);
        womanStr.append(" |   :  :  | |                      '   ,'\\   .--.--.   ").append(CRLF);
        womanStr.append(" :   |   \\ | :  ,--.--.     ,---.  /   /   | /  /    '  ").append(CRLF);
        womanStr.append(" |   : '  '; | /       \\   /     \\.   ; ,. :|  :  /`./  ").append(CRLF);
        womanStr.append(" '   ' ;.    ;.--.  .-. | /    / ''   | |: :|  :  ;_").append(CRLF);
        womanStr.append(" |   | | \\   | \\__\\/: . ..    ' / '   | .; : \\  \\    `. ").append(CRLF);
        womanStr.append(" '   : |  ; .' ,\" .--.; |'   ; :__|   :    |  `----.   \\").append(CRLF);
        womanStr.append(" |   | '`--'  /  /  ,.  |'   | '.'|\\   \\  /  /  /`--'  /").append(CRLF);
        womanStr.append(" '   : |     ;  :   .'   \\   :    : `----'  '--'.     /").append(CRLF);
        womanStr.append(" ;   |.'     |  ,     .-./\\   \\  /            `--'---'").append(CRLF);
        womanStr.append(" '---'        `--`---'     `----'").append(CRLF);
        womanStr.append(" ").append(CRLF);
        womanStr.append(" ").append(CRLF);
        womanStr.append(CRLF);
        womanStr.append(CRLF);
        
        return womanStr;
    }
    
    public static StringBuffer getMsgStr()
    {
        StringBuffer msgStr = new StringBuffer();
        msgStr.append(CRLF);
        msgStr.append("    _._").append(CRLF);
        msgStr.append("           _.-``__ ''-._").append(CRLF);
        msgStr.append("      _.-``    `.  `_.  ''-._           ").append(CRLF);
        msgStr.append("  .-`` .-```.  ```\\/    _.,_ ''-._     ").append(CRLF);
        msgStr.append(" (    '      ,       .-`     | `, )     ").append(CRLF);
        msgStr.append(" |`-._`-...-` __...-.``-._|'` _.-'|     ").append(CRLF);
        msgStr.append(" |    `-._   `._     /     _.-'   |     ").append(CRLF);
        msgStr.append("  `-._    `-._  `-./  _.-'    _.-'      ").append(CRLF);
        msgStr.append(" |`-._`-._    `-.__.-'    _.-'_.-'|     ").append(CRLF);
        msgStr.append(" |    `-._`-._        _.-'_.-'    |     ").append(CRLF);
        msgStr.append("  `-._    `-._`-.__.-'_.-'    _.-'      ").append(CRLF);
        msgStr.append(" |`-._`-._    `-.__.-'    _.-'_.-'|     ").append(CRLF);
        msgStr.append(" |    `-._`-._        _.-'_.-'    |     ").append(CRLF);
        msgStr.append("  `-._    `-._`-.__.-'_.-'    _.-'      ").append(CRLF);
        msgStr.append("      `-._    `-.__.-'    _.-'          ").append(CRLF);
        msgStr.append("          `-._        _.-'").append(CRLF);
        msgStr.append("  `-.__.-'").append(CRLF);
        msgStr.append(CRLF);
        
        return msgStr;
    }
    
    public static StringBuffer getSpringStr()
    {
        StringBuffer womanStr = new StringBuffer();
        womanStr.append("").append(CRLF);
        womanStr.append("  .   ____          _            __ _ _").append(CRLF);
        womanStr.append(" /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\").append(CRLF);
        womanStr.append("( ( )\\\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\").append(CRLF);
        womanStr.append(" \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )").append(CRLF);
        womanStr.append("  '  |____| .__|_| |_|_| |_\\__, | / / / /").append(CRLF);
        womanStr.append(" =========|_|==============|___/=/_/_/_/").append(CRLF);
        womanStr.append(CRLF);
        
        return womanStr;
    }
    
    public static StringBuffer getYonYouStr()
    {
        StringBuffer yonyouStr = new StringBuffer();
        yonyouStr.append(CRLF);
        yonyouStr.append(" __     ______  _   ___     ______  _    _   ").append(CRLF);
        yonyouStr.append(" \\ \\   / / __ \\| \\ | \\ \\   / / __ \\| |  | |  ").append(CRLF);
        yonyouStr.append("  \\ \\_/ / |  | |  \\| |\\ \\_/ / |  | | |  | |  ").append(CRLF);
        yonyouStr.append("   \\   /| |  | | . ` | \\   /| |  | | |  | |  ").append(CRLF);
        yonyouStr.append("    | | | |__| | |\\  |  | | | |__| | |__| |  ").append(CRLF);
        yonyouStr.append("    |_|  \\____/|_| \\_|  |_|  \\____/ \\____/   ").append(CRLF);
        yonyouStr.append("                                             ").append(CRLF);
        yonyouStr.append(" Yonyou V1.0 ").append(CRLF);
        yonyouStr.append(CRLF);
        
        return yonyouStr;
    }
    
    public static StringBuffer getYonYouCStr()
    {
        StringBuffer yonyouStr = new StringBuffer();
        yonyouStr.append(CRLF);
        yonyouStr.append(" __     __     __     __          _____ _                 _  ").append(CRLF);
        yonyouStr.append(" \\ \\   / /     \\ \\   / /         / ____| |               | | ").append(CRLF);
        yonyouStr.append("  \\ \\_/ /__  _ _\\ \\_/ /__  _   _| |    | | ___  _   _  __| | ").append(CRLF);
        yonyouStr.append("   \\   / _ \\| '_ \\   / _ \\| | | | |    | |/ _ \\| | | |/ _` | ").append(CRLF);
        yonyouStr.append("    | | (_) | | | | | (_) | |_| | |____| | (_) | |_| | (_| | ").append(CRLF);
        yonyouStr.append("    |_|\\___/|_| |_|_|\\___/ \\__,_|\\_____|_|\\___/ \\__,_|\\__,_| ").append(CRLF);
        yonyouStr.append(" ").append(CRLF);
        yonyouStr.append(" YonyouCloud V1.0 ").append(CRLF);
        yonyouStr.append(CRLF);
        
        return yonyouStr;
    }
    
    public static StringBuffer getYonYouWenjian()
    {
        StringBuffer yonyouStr = new StringBuffer();
        yonyouStr.append(CRLF);
        // yonyouStr.append("
        // ==============================================================================").append(CRLF);
        yonyouStr.append(" __     __                                                 _ _              ").append(CRLF);
        yonyouStr.append(" \\ \\   / /                                                (_|_)             ").append(CRLF);
        yonyouStr.append("  \\ \\_/ /__  _ __  _   _  ___  _   _   __      _____ _ __  _ _  __ _ _ __   ").append(CRLF);
        yonyouStr.append("   \\   / _ \\| '_ \\| | | |/ _ \\| | | |  \\ \\ /\\ / / _ \\ '_ \\| | |/ _` | '_ \\  ").append(CRLF);
        yonyouStr.append("    | | (_) | | | | |_| | (_) | |_| |   \\ V  V /  __/ | | | | | (_| | | | | ").append(CRLF);
        yonyouStr.append("    |_|\\___/|_| |_|\\__, |\\___/ \\__,_|    \\_/\\_/ \\___|_| |_| |_|\\__,_|_| |_| ").append(CRLF);
        yonyouStr.append("                    __/ |                                _/ |               ").append(CRLF);
        yonyouStr.append("                   |___/                                |__/                ").append(CRLF);
        yonyouStr.append(" ").append(CRLF);
        yonyouStr.append(" Yonyou Wenjian V1.0 ").append(CRLF);
        // yonyouStr.append("
        // ===============================================================================").append(CRLF);
        yonyouStr.append(CRLF);
        
        return yonyouStr;
    }
    
    public static StringBuffer getXisoftStr()
    {
        StringBuffer xisoft = new StringBuffer();
        xisoft.append(" ___    ___ ___  ________  ________  ________ _________   ").append(CRLF);
        xisoft.append("|\\  \\  /  /|\\  \\|\\   ____\\|\\   __  \\|\\  _____\\\\___   ___\\ ").append(CRLF);
        xisoft.append("\\ \\  \\/  / | \\  \\ \\  \\___|\\ \\  \\|\\  \\ \\  \\__/\\|___ \\  \\_| ").append(CRLF);
        xisoft.append(" \\ \\    / / \\ \\  \\ \\_____  \\ \\  \\\\\\  \\ \\   __\\    \\ \\  \\  ").append(CRLF);
        xisoft.append("  /     \\/   \\ \\  \\|____|\\  \\ \\  \\\\\\  \\ \\  \\_|     \\ \\  \\ ").append(CRLF);
        xisoft.append(" /  /\\   \\    \\ \\__\\____\\_\\  \\ \\_______\\ \\__\\       \\ \\__\\").append(CRLF);
        xisoft.append("/__/ /\\ __\\    \\|__|\\_________\\|_______|\\|__|        \\|__|").append(CRLF);
        xisoft.append("|__|/ \\|__|        \\|_________|   ");
        xisoft.append(CRLF);
        // xisoft.append(CRLF).append(getHXWSplitLine()).append(CRLF);
        xisoft.append(CRLF);
        
        return xisoft;
    }
    
    public static StringBuffer getJVMInfo()
    {
        StringBuffer jvmStr = new StringBuffer();
        jvmStr.append("java版本号：").append(System.getProperty("java.version")); // java版本号
        jvmStr.append(CRLF).append("Java版本号(runtime)：").append(System.getProperty("java.runtime.version")); // java版本号
        jvmStr.append(CRLF).append("Java提供商名称：").append(System.getProperty("java.vendor")); // Java提供商名称
        jvmStr.append(CRLF).append("Java提供商网站 ：").append(System.getProperty("java.vendor.url")); // Java提供商网站
        jvmStr.append(CRLF).append("Java，哦，应该是jre目录：").append(System.getProperty("java.home")); // Java，哦，应该是jre目录
        jvmStr.append(CRLF).append("Java虚拟机规范版本号 ：").append(System.getProperty("java.vm.specification.version")); // Java虚拟机规范版本号
        jvmStr.append(CRLF).append("Java虚拟机规范提供商 ：").append(System.getProperty("java.vm.specification.vendor")); // Java虚拟机规范提供商
        jvmStr.append(CRLF).append("Java虚拟机规范名称 ：").append(System.getProperty("java.vm.specification.name")); // Java虚拟机规范名称
        jvmStr.append(CRLF).append("Java虚拟机版本号 ：").append(System.getProperty("java.vm.version")); // Java虚拟机版本号
        jvmStr.append(CRLF).append("Java虚拟机 Bit size: ").append(System.getProperty("sun.arch.data.model")); // Java虚拟机位
        jvmStr.append(CRLF).append("Java虚拟机提供商 ：").append(System.getProperty("java.vm.vendor")); // Java虚拟机提供商
        jvmStr.append(CRLF).append("Java虚拟机名称 ：").append(System.getProperty("java.vm.name")); // Java虚拟机名称
        jvmStr.append(CRLF).append("Java规范版本号 ：").append(System.getProperty("java.specification.version")); // Java规范版本号
        jvmStr.append(CRLF).append("Java规范提供商 ：").append(System.getProperty("java.specification.vendor")); // Java规范提供商
        jvmStr.append(CRLF).append("Java规范名称 ：").append(System.getProperty("java.specification.name")); // Java规范名称
        jvmStr.append(CRLF).append("Java类版本号 ：").append(System.getProperty("java.class.version")); // Java类版本号
        // jvmStr.append(CRLF).append("Java类路径 ：").append(System.getProperty("java.class.path")); //Java类路径
        jvmStr.append(CRLF).append("Java lib路径 ：").append(System.getProperty("java.library.path")); // Java
                                                                                                    // lib路径
        jvmStr.append(CRLF).append("Java输入输出临时路径 ：").append(System.getProperty("java.io.tmpdir")); // Java输入输出临时路径
        jvmStr.append(CRLF).append("Java编译器 ：").append(System.getProperty("java.compiler")); // Java编译器
        jvmStr.append(CRLF).append("Java执行路径：").append(System.getProperty("java.ext.dirs")); // Java执行路径
        jvmStr.append(CRLF).append("操作系统名称：").append(System.getProperty("os.name")); // 操作系统名称
        jvmStr.append(CRLF).append("操作系统arch：").append(System.getProperty("os.arch")); //
        jvmStr.append(CRLF).append("版本号：").append(System.getProperty("os.version")); // 版本号
        jvmStr.append(CRLF).append("文件分隔符 ：").append(System.getProperty("file.separator")); // 文件分隔符
        jvmStr.append(CRLF).append("路径分隔符 ：").append(System.getProperty("path.separator")); // 路径分隔符
        jvmStr.append(CRLF).append("直线分隔符 ：").append(System.getProperty("line.separator")); // 直线分隔符
        jvmStr.append(CRLF).append("用户名 ：").append(System.getProperty("user.name")); // 用户名
        jvmStr.append(CRLF).append("：").append(System.getProperty("user.home"));
        jvmStr.append(CRLF).append("：").append(System.getProperty("user.dir"));
        
        return jvmStr;
    }
    
    public static StringBuffer getSystemInfo()
    {
        StringBuffer systemStr = new StringBuffer();
        try
        {
            systemStr.append(CRLF);
            systemStr.append("🖥 host_ip：").append(InetAddress.getLocalHost().getHostAddress()).append(CRLF);
            systemStr.append("   host_name：").append(InetAddress.getLocalHost().getHostName()).append(CRLF);
            systemStr.append("   file.encoding：").append(System.getProperty("file.encoding")).append(CRLF);
            systemStr.append("   java.version：").append(System.getProperty("java.version")).append(CRLF);
            systemStr.append("   java.runtime.version：").append(PropertiesUtil.key("java.runtime.version")).append(CRLF);
            systemStr.append("   java.Memory：").append(Runtime.getRuntime().totalMemory() / 1024).append(CRLF);
            systemStr.append("   java.io.tmpdir：").append(System.getProperty("java.io.tmpdir")).append(CRLF);
            systemStr.append("   os.arch：").append(System.getProperty("os.arch")).append(CRLF);
            systemStr.append("   os.name：").append(System.getProperty("os.name")).append(CRLF);
            systemStr.append("   os.version：").append(System.getProperty("os.version")).append(CRLF);
            systemStr.append("   JVM Bit size: ").append(PropertiesUtil.key("sun.arch.data.model")).append(CRLF);
            systemStr.append("   sun.desktop：").append(System.getProperty("sun.desktop")).append(CRLF);
            systemStr.append("   user.name：").append(System.getProperty("user.name")).append(CRLF);
            systemStr.append("   totalMemory：").append(Runtime.getRuntime().totalMemory() / 1024 / 1024).append(" MB").append(CRLF);
            systemStr.append(CRLF);
        }
        catch (Throwable e)
        {
            StringBuffer log = new StringBuffer();
            int count = 0;
            while ((e != null) && (count < 50))
            {
                count++;
                log.append(e.toString()).append(CRLF);
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++)
                {
                    log.append("      at ").append(trace[i]).append(CRLF);
                }
                
                e = e.getCause();
            }
            log.toString();
        }
        
        return systemStr;
    }
    
    public static String getPatternStr()
    {
        StringBuffer str = new StringBuffer();
        str.append("🀙🀚🀛🀜🀝🀞🀟🀠🀡   🀏🀎🀍🀌🀋🀊🀉🀇  🀐🀔🀕🀖🀗🀘  🀅🀁🀆🀢🀣 ");
        str.append("👨🏻👴👦👩😍🥰😘 👉👈🖐🏻👴🏻🖐🏻");
        str.append("🖥⌨️🖱☎🎬📱");
        str.append("🌲🚔🚺🏃🏼‍💺");
        str.append("📚🪙📝️📙📘📐💵");
        str.append("🌕🍺🍜🍊🍐🍬🧬");
        str.append("🎹🎼♩♩♪ ♬♩♪ ♫ ♪ ");
        str.append("📦🎁👕");
        str.append("🐞🕊💞❣❤");
        str.append("📌🚀💥🌏📥🔔️⭐");
        str.append("☀☂❁☁🌧⚡🌙🌃");
        str.append("🛠🚽🏗️🎋️🪑🍳");
        str.append("▏▎▍▌▋▊▉ ▉▊▋▌▍▎");
        str.append("☞∴ღ ঞ✲❈✿✲❈");
        str.append("´¯`•´`•.¸¸.•´´༺o༻`••.¸¸.•´´¯`•´");
        str.append("༊ღ᭄夣醒ꦿ꯭心亦碎꧔ꦿ᭄💞ꦿ ⁵²º⅓¼ ※ ");
        str.append("ꕥ᭄ঞ এ⁵²ºꕥꦿ ༺o༻ ∧＿∧ ❀ ✿");
        str.append("❌(◕‿◕)(◡‿◡)");
        
        return str.toString();
    }
    
    public static String getHXWSplitLine()
    {
        String msg = "👉🏻————ꕥ😍🥰😘(◡‿◡✿)༺ཌ༈魑魅魍魉༈ད༻ ꧁ღ❦ꦿ勇敢的少年啊快去创造奇迹এ⁵²ºஐఌ꧂ 🌲🚔🚺🏃🏼‍♂️❤————" + CRLF//
            + "☞∴ღ ঞ✲❈✿✲❈☀☂❁☁ 🚀🌏💥 🐞  🀙🀚🀛🀜🀝🀞🀟🀠🀡 🀢🀣🀥 🀗🀐 🀏🀎🀍🀌🀋🀊🀉  📱❌💻🖐🏻👴🏻💵🍜  🎹🎼♩♩♪ " + " ♬♩  ♪♫ ♫ ♬♪" + CRLF//
            + "🖥⌨️🖱☎💺👨🏻‍🍳✦💞ꦿ兲檤絒懄এ⁵²º❣🕊️--༊⁵²º⅓¼ " + CRLF//
            + "👇🏻´¯`•´`•.¸¸.•´´༺o༻`••.¸¸.•´´¯`•´ꕥꦿ";
        return msg;
    }
    
    public static String getSplitLine2()
    {
        return "✄----------- 剪切线 ---------- 剪切线 ---------- 剪切线 ---------------" + CRLF;
    }
    
    public static final String DEBUG_FORMAT = "[DEBUG] %s%n";
    public static final String DIVISION_LINE = "================================================================";
    public static final String PREVIOUS_LINE = "↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑";
    public static final String NEXT_LINE = "↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓";
    public static final String SPLIT_LINE = "📌✄—————————(◕‿◕✿)💞ꦿ 完美分割线❤split line ❀ ———————————————" + CRLF;
    
    public static String getSplitLine()
    {
        return SPLIT_LINE;
    }
    
}
