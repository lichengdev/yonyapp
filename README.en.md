# yonyou(用友)相关的一些东东 nc65、ncc、YonBip高级版系列，ncchome相关代码



<div align="center">
    <img src="icons/00.png" alt="项目Logo" width="16">
    <img src="icons/01.png" alt="项目Logo" width="16">
    <img src="icons/02.png" alt="项目Logo" width="16">
    <img src="icons/03.png" alt="项目Logo" width="16">
    <img src="icons/04.png" alt="项目Logo" width="16">
    <img src="icons/05.png" alt="项目Logo" width="16">
    <img src="icons/06.png" alt="项目Logo" width="16">
    <img src="icons/07.png" alt="项目Logo" width="16">
    <img src="icons/08.png" alt="项目Logo" width="16">
    <img src="icons/09.png" alt="项目Logo" width="16"> 
    <img src="icons/10.png" alt="项目Logo" width="16"> 
    <br>
    <!-- 徽章示例：构建状态、版本、许可证、下载量等 -->
    <img src="https://img.shields.io/badge/version-1.0.0-blue.svg" alt="Version 1.0.0">
    <img src="https://img.shields.io/badge/license-MIT-green.svg" alt="MIT License">
</div>



#### 介绍 


#### 安装 | Installation

###### github: https://github.com/lichengdev/yonyapp.git
###### gitee: https://gitee.com/lichengdev/yonyapp.git
 

#### 软件架构
   下面是这三个产品的核心区别：  
   | 对比维度 |	NC65 |	NCC (NC Cloud) |	YonBIP高级版 |
   | 产品定位 |	传统ERP软件，企业信息化管理系统  |	大型企业数字化平台，是NC的云化升级版  |	商业创新平台，集工具、能力和资源于一体的云服务群 | 
   | 核心理念 |	流程驱动，聚焦于企业内部资源计划与管控，提升运营效率  |	数据驱动，聚焦于数智化管理、经营与商业，帮助大型企业落地数智化 	商业创新，支撑和重构企业的发展力，实现社会化协同与生态化 | 
   | 技术架构 |	基于J2EE的套件式架构，部分模块化  |	云原生架构，开始采用微服务、中台化理念  |	云原生、微服务、中台化（业务中台、数据中台、AI中台）、数用分离的架构 | 
   | 核心价值 |	优化流程，解决企业内部信息孤岛问题  |	全面数智化转型，重塑企业核心竞争力  |	支撑业务创新、管理变革，帮助企业实现高质量发展和商业创新 | 
   | 部署方式 |	以本地化部署为主 | 	支持混合云、公有云/专属云等多种部署模式 |	主要为公有云/专属云模式，提供SaaS化订阅服务，持续迭代 | 
   | 商业价值 |	IT资产，注重功能实现 |	业务赋能，驱动数智化经营 |	重构发展力，支撑商业模式创新 | 
   | 协同范围 |	企业内部协同 |	企业级应用，企业内部及产业链初步协同	社会化协同 | 连接产业链和社会化资源，构建价值网 |

#### 📜 产品演进路线解析
   NC65：作为用友在传统信息化时代的旗舰产品，NC系列的核心是为大型企业提供一体化的信息管理解决方案。NC65在2016年发布时，也强调了共享服务和互联网化的特性，但整体上仍属于需要本地部署或私有云的传统ERP套件，其价值在于帮助企业实现流程优化和效率提升 。  
   NCC (NC Cloud)：这是NC系列的云化升级版。它继承了NC深厚的大型企业管理经验，但底层架构转向了云原生。NCC不再仅仅是一个ERP，而是升级为“数字化平台”，强调“数据驱动”，帮助企业实现财务共享、全球司库等数智化应用场景，是企业从信息化向数智化转型的关键一步 。  

   YonBIP高级版：这是用友3.0-II战略阶段的核心产品，代表了未来的方向。它的理念发生了根本性变化，从“管理软件”跃升为“商业创新平台”。YonBIP的核心是利用云原生、中台化等最新技术，构建一个开放的、生态化的平台，将企业的能力（如财务、人力、供应链）以“云服务”的形式提供，目的是赋能企业进行商业创新，而不仅仅是管理好内部资源 。官方资料明确指出，YonBIP适用于从成长型到大型的各类企业集团 。  

#### 安装教程

1.  ncchome放的代码，不包含modules里面的代码



#### 使用说明

   git config --global user.name "你的姓名"  
   git config --global user.email "你的邮箱@example.com"  
   
   … 或者在命令行创建一个新的仓库  
   echo "# devplugins" >> README.md  
   git init  
   git add README.md  
   git commit -m "first commit"  
   git branch -M main  
   git remote add origin https://github.com/lichengdev/yonyapp.git  
   git push -u origin main  
   .........................  
  
   … 或者从命令行推送已有的仓库  
   git remote add origin https://github.com/lichengdev/yonyapp.git  
   git branch -M main  
   git push -u origin main  


   git clone https://github.com/lichengdev/yonyapp.git  从远程库中克隆  
   git branch  查看当前所有的分支  
   git checkout 分支名  作用是切换分支  
   
   git pull 
   git add XX       把xx文件添加到暂存区去。 
   git commit –m “XX”  提交文件 –m 后面的是注释。 
   
   git push origin master  Git会把master分支推送到远程库对应的远程分支上  
   
#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)

