####	SVN的项目结构
	trunk：开发主目录
	branches：开发的分支目录
	tags：为tag存档目录(不建议修改)
####	同步代码--checkout
前提条件：在客户端机器上创建本地仓库

	第一种：在本地仓库中，右单击-->SVN Checkout -->输入项目地址-->选择本地仓库
	
	第二种：在本地仓库中，右单击-->TortoiseSVN-->Repo Broswer查看远程仓库的工程结构，选择要同步的文件夹，右单击-->check out
####	向本地仓库中添加内容--add
	1、将要上传到远程仓库的文件添加到本地仓库中
	2、可以在仓库中，右单击-->TortoiseSVN-->Add-->选择要添加的文件，ok
add的主要目的是将要提交到远程仓库的文件标记为新增的，为在commit时提交到服务器，不标记不能提交

####	向服务端提交内容--commit
	在本地仓库中右单击-->SVN commit -->输入log信息，确认即可

####	更新--update
将本地仓库跟远程仓库对比，将远程仓库中新增的修改内容同步到本地中来

	在本地仓库中，右单击-->SVN update
建议：在每次提交之前，建议先更新本地仓库，检测是否有冲突
####	解决冲突
当本地仓库中修改的内容和远程仓库中新增的修改的位置一样时，就会出现冲突

	1、update本地仓库
	2、如果有提示冲突，在冲突的文件上，右单击-->TortoiseSVN-->Edit conflict
	3、在冲突的编辑界面的meger(合并区域)中，针对冲突的行，选择保留服务端（Theirs）或者自己的(Mine)或者两个都保留(哪个在前)
	4、标记为已解决(mark as resolve)
	5、提交

####	加锁--get lock
当正在修改的文件，不希望其他的修改先提交，则可以对该文件加锁

	在文件上，右单击-->TortoiseSVN-->get lock-->输入加锁信息
加锁的文件上只有加锁者解锁或者提交之后(自动解锁)其他修改才可以提交
####	解锁
	可以在加锁的文件上，右单击-->TortoiseSVN -->release lock
####	删除--delete
	1、在本地仓库中，对需要删除的文件，右单击-->TortoiseSVN-->delete
	2、提交
####	版本回退
查看log记录，在本地仓库中，右单击-->TortoiseSVN-->show log-->选中要回退的版本，右单击选择回退方式

	Revert to this revision：回退到选中的版本，如：历史发展为1，2，3，4，5，当前选择了3，然后
	Revert to this revision，则整个历史发展将变成1，2，3

	Revert change from this revision：将选中的版本的改变移除，如：历史发展为1，2，3，4，5当前选择了3，
	则Revert change from this revision之后，历史记录将变成1，2，4，5
	
####	Studio导入svn项目
安装svn客户端

	1、检测studio中是否安装了svn插件：setting-->version control-->subversion
	2、setting-->version control-->subversion将默认的选项去掉
	3、从远程仓库同步代码到studio中，VCS-->Browse VCS repository-->subvision-->输入地址-->用户名和密码
	4、选择要同步的文件夹-->右单击-->check out（指定一个本地仓库）

	直接在studio上开发，如果要更新，直接在项目文件夹上右单击-->subversion-->update , commite..

如果studio第一次将studio中的项目(直接将studio的开发工程目录作为本地仓库)提交到服务器端
	
	VCS-->Import into Version Control-->Share Project(subversion) -->选择或者新建远程地址-->share-->选择要提交的文件，填写说明
