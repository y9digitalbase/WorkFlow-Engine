# 流程设计

## 1. 流程定义

新增流程，填写流程定义key，流程定义名称。

![image-20240613092455217](../images/docIntro/image-20240613092455217.png)

![image-20240613103203174](../images/docIntro/image-20240613103203174.png)

## 2. 流程设计

流程以【开始事件】开始，以【结束事件】结束。

![image-20230822142416455](../images/docIntro/image-20230822142416455.png)

用户任务，填写任务id，任务名称，任务处理用户固定。

![image-20230822142542949](../images/docIntro/image-20230822142542949.png)

多实例任务，用户任务包括【单实例任务】接收人多个会抢占式签收办理该件，【并行多实例任务】接收人多个可同时办理该件，【串行多实例任务】接收人多个会按顺序办理该件。

![image-20230822150806537](../images/docIntro/image-20230822150806537.png)

流转条件，排他网关出来的线路默认配置流转条件表达式。

![image-20230823145556539](../images/docIntro/image-20230823145556539.png)

设计完成后点击保存即可。

# 流程部署

## 1. 流程部署

在流程设计列表点击【部署】，部署当前流程，部署后可在【流程部署】列表查看，也可在【流程部署】列表直接上传文件进行部署。

![image-20240613093126838](../images/docIntro/image-20240613093126838.png)

![image-20240613093223834](../images/docIntro/image-20240613093223834.png)

# 新增事项

## 1. 新增事项

在【事项配置】菜单下新增事项，绑定相应的流程，配置应用url，系统中文名和英文名。

![image-20240613093424341](../images/docIntro/image-20240613093424341.png)

![image-20240613093622322](../images/docIntro/image-20240613093622322.png)

# 表单管理
## 1.业务表管理

选择对应的系统，可以添加数据库已有的表，表名需以y9_form_开头，也可创建新表。

![image-20240613093704950](../images/docIntro/image-20240613093704950.png)

![image-20240613093725714](../images/docIntro/image-20240613093725714.png)

填写表名，添加相应的字段，【保存】只保存数据，不会生成或修改数据库表结构；【新生成表】数据库如果已存在表结构，会清空数据，删除对应的表，重新生成表结构；【修改表结构】已存在的数据库表添加字段，不会清空数据。

![image-20240613093834795](../images/docIntro/image-20240613093834795.png)

![image-20240613093913391](../images/docIntro/image-20240613093913391.png)

## 2.数据字典

添加数据字典，用于表单绑定多选框，单选框，下拉框数据。

![image-20240613094037732](../images/docIntro/image-20240613094037732.png)

## 3. 表单设计

表单设计官方使用手册：https://form.making.link/docs/manual/introduction.html

### 3.1添加表单

添加表单后，点击【表单设计】，进入设计页面。

![image-20240613094807873](../images/docIntro/image-20240613094807873.png)

![image-20240613094945640](../images/docIntro/image-20240613094945640.png)

### 3.2表单预览

点击【预览】可预览表单设计结果。



![image-20240613095050161](../images/docIntro/image-20240613095050161.png)

### 3.3绑定数据库字段

表字段绑定数据库表字段，选择对应的元素，点击【字段标识】输入框，弹出字段绑定页面。

![image-20240613095210040](../images/docIntro/image-20240613095210040.png)

![image-20240613095238502](../images/docIntro/image-20240613095238502.png)

字段变蓝，或字段标识有已绑定，说明已绑定成功。

![image-20240613095302582](../images/docIntro/image-20240613095302582.png)

### 3.4默认字段

guid为默认隐藏字段，拖拉单行文本字段后勾选隐藏即可。

![image-20240613095342714](../images/docIntro/image-20240613095342714.png)





### 3.5动态默认值

输入框动态默认值，以$_加字段名，前端工程解析赋值。

![image-20240613095509465](../images/docIntro/image-20240613095509465.png)

也可勾选自定义默认值，自定义默认值后端接口已实现数据返回。

![image-20240613095535936](../images/docIntro/image-20240613095535936.png)

### 3.6数据字典使用

选择单选框字段，可使用静态数据或动态数据。

![image-20240613095739748](../images/docIntro/image-20240613095739748.png)

动态数据绑定数据字典，点击数据字典输入框绑定数据字典，方法函数可根据前端工程实现改变。

![image-20240613100017987](../images/docIntro/image-20240613100017987.png)

![image-20240613100037408](../images/docIntro/image-20240613100037408.png)

### 3.7字段必填

![image-20240613100121434](../images/docIntro/image-20240613100121434.png)

### 3.8意见框绑定

点击输入框选择意见框进行绑定。

![image-20240613100303590](../images/docIntro/image-20240613100303590.png)

![image-20240613100327681](../images/docIntro/image-20240613100327681.png)

### 3.9附件列表

附件列表字段标识值为固定值custom_file。

![image-20240613100414851](../images/docIntro/image-20240613100414851.png)

### 3.10打印表单

设计专门的打印表单。

![image-20240613100803708](../images/docIntro/image-20240613100803708.png)

# 意见框管理

## 1.添加意见框

添加意见框，用于与事项绑定。

![image-20240613100826330](../images/docIntro/image-20240613100826330.png)

#  动态角色
## 1.添加动态角色

添加动态角色，用于事项节点授权。

![image-20240613100958159](../images/docIntro/image-20240613100958159.png)

# 按钮管理

## 1.添加按钮

添加发送按钮，事项配置发送按钮后，会显示在发送下拉菜单，前端根据唯一标识实现按钮方法。

![image-20240613101019534](../images/docIntro/image-20240613101019534.png)

![image-20230822172612696](../images/docIntro/image-20230822172612696.png)

普通按钮直接显示。

![image-20230822172655841](../images/docIntro/image-20230822172655841.png)

# 视图类型

## 1.添加视图类型

可在视图配置中使用。

![image-20240613101058758](../images/docIntro/image-20240613101058758.png)

# 事项配置

## 1. 表单配置

新部署流程时，需要重新绑定表单。

![image-20240613101143439](../images/docIntro/image-20240613101143439.png)

绑定表单可以选择前端需要显示的页签，不勾选则不显示。

![image-20240613101218035](../images/docIntro/image-20240613101218035.png)

![image-20240613104144361](../images/docIntro/image-20240613104144361.png)

## 2. 权限配置

权限配置可以绑定多个类型的权限主体。

![image-20240613101352909](../images/docIntro/image-20240613101352909.png)

![image-20240613101410057](../images/docIntro/image-20240613101410057.png)

## 3.意见框配置

绑定意见框，不绑定角色则全部人可以填写，绑定角色则该角色人员可以填写，【必填意见】为是时，发送时会校验是否已填写意见，没有填写则提示。

![image-20240613101510874](../images/docIntro/image-20240613101510874.png)

## 4.打印模板配置

绑定打印表单

![image-20240613101544188](../images/docIntro/image-20240613101544188.png)

## 5.按钮配置

按钮不绑定角色则所有人可见，绑定角色则角色内的人可见。

![image-20240613101643235](../images/docIntro/image-20240613101643235.png)



![image-20240613104425147](../images/docIntro/image-20240613104425147.png)



## 6.视图配置

可以配置表单上已绑定的字段，也可自定义字段，自定义字段需要配合前后端代码实现。

![image-20240613101859303](../images/docIntro/image-20240613101859303.png)

![image-20240613101828334](../images/docIntro/image-20240613101828334.png)

配置后前端使用显示

![image-20240613104530041](../images/docIntro/image-20240613104530041.png)

# 应用访问

浏览器直接访问应用url。

![image-20240613101955884](../images/docIntro/image-20240613101955884.png)

![image-20240613104651421](../images/docIntro/image-20240613104651421.png)

![image-20240613104826102](../images/docIntro/image-20240613104826102.png)

