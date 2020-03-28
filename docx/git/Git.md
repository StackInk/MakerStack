## Git总结

### Git暂存区，工作区，版本库

**工作区：**电脑的工作目录。.git所在的文件夹

**暂存区：**又称索引区。是`git add`操作以后将添加的文件存放的区域。在`.git`文件夹中以`index`文件存在

**版本库：**提交以后的文件。`git commit`以后的文件

### Git常用命令

```shell
#初始化 .gitconfig
    git config user.name='username' # 初始化用户名
    git config user.emil='emial' #初始化邮箱
#初始化git仓库
	git init # 会在当前文件夹下生成一个.git的隐藏文件夹，里面存放了各种git库的信息
#添加，提交，状态查看
	vim a.txt # 新建一个文件，并修改
	git add a.txt # 将add添加到暂存区(也称为索引区)
	git status # 查看当前文件修改状态。主要是比较暂存区和工作区的文件信息状态是否相同
	git diff a.txt # 查看当前文件在暂存区的信息与工作区是否相同
# 查看提交版本日志
	git log a.txt # 一条提交记录三行显示
	git log --pretty=oneline a.txt # 单行显示提交记录
# 版本回退
	#假设现在已经提交了三次，需要将提交版本回退为第二次
		git reset --hard HEAD^ # 回退一次
	# 假设现在已经提交了100次，需要将提交版本回退为第10次
		git reset --hard HEAD~90
	# 时光回溯
		git reflog a.txt # 记录所有操作a.txt的日志,存在一个7位的标识
		git reset --hard 7位的标识
# 分支管理（相当于复制了一份master分支的数据）
	git branch dev # 创建一个名为dev的分支
	git checkout dev # 将分支切换到dev
	git merge # 合并分支
	# 版本冲突：
		# 第一种：两个分支修改同一个数据，合并的时候出现错误
		# 解决方法，当合并分支的时候，会显示其他人修改以后的数据，将这个文件的数据重新提交一次即可，提交的时候不能添加文件名，全部进行提交
	git branch -d dev # 删除名为dev的分支
	git checkout -b dev # 创建一个dev分支并且切换到dev分支	
```

### Git连接GitHub

```shell
#创建一个SSH秘钥和公钥
ssh-keygen -t rsa -C "email"
#将生成的pub公钥内容拷贝到GitHub的SSH验证中
#测试连通性,会生成一个knowhost文件
ssh -T git@github.com
#在GitHub中新建一个仓库，与本地关联
 git remote add origin 仓库地址 #这个仓库地址必须书写为https的，SSH的无法进行推送
 # 将本地分支内容推送到远程GitHub
 git push -u origin master
 # 第一次推送master分支的时候，加上了-u参数，Git不但会将本地的master分支内容推送到远程新的master分支，还会将本地的master分支和远程的master分支关联起来，在以后的操作中就不需要添加这个参数了。
 git pull origin master #将远程仓库中的推送到本地
 
 git clone 仓库地址 #克隆一份项目
```

### Fork

GitHub上面的一个参与开源项目的工具，Fork会将该项目直接拷贝到自己的仓库中，开发者可以直接对当前仓库进行修改和提交，同时通过GitHub提供的Pull Repository 提交给该项目的作者。