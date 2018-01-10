# FilePicker - 文件选择器

版权声明：此项目基于开源库：[LFilePicker](https://github.com/leonHua/LFilePicker) 修改而来. 特此感谢LFilePicker作者。

支持平台

- Android

## 用法

```
cordova plugin add /xxxx/xxx/FilePicker
```

```
# 添加样式
styles.xml中添加resources目录下styles.xml中的样式

```

默认配置

```javascript
var defaultConfig = {
		// 标题
		title: '选择文件',
		// 导航栏背景色
		titleBg: '#1baaeb',
		// 文件图标样式
		iconStyle: '',
		// 返回按钮样式
		backIconStyle: '',
		// 多选
		multiSelect: true,
		// 返回上一级目录显示的文字
		backText: '上一级',
		// 文件最大数
		maxNum: 10,
		// 超出选择文件数提示
		maxNumTips: '已达到最大选择数量,请移除部分选择的文件',
		// 文件过滤器(默认不过滤)
		fileFilter: '',
		// 未选择提示
		noChooseTips: '至少选择一个文件',
		// 选择模式 0 文件模式 1 文件夹模式 2 Both
		chooseMode: 2
	};
```

调用

```javascript
if (window.plugins && window.plugins.FilePicker) {
          var options = {

          };
          window.plugins.FilePicker.chooseFile(options,
            function (data) {
              console.log(data.fileList);
            }, function (error) {
              console.log(error);
          })
        }
```

返回

```
路径数组
# 示例
[
    "/storage/0/xxxx.jpg",
    "/storage/0/xxxx2.jpg"
]
```

