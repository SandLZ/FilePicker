/**
 * @ClassName FilePicker
 * @Author zliu
 * @Date 2017/12/22
 * @Email zliu@handsmap.cn
 */
var exec = require('cordova/exec');
// Constructor
function FilePicker() {
}
FilePicker.prototype.chooseFile = function (options, cb, errCb) {

	// 解析
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
		backText: '返回上一级',
		// 确认选择按钮文字
		sureSelectText: '确认',
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

	for (var key in defaultConfig) {
		if (typeof options[key] !== "undefined") {
			defaultConfig[key] = options[key];
		}
	}

	var callback = function (message) {
		if (message != 'error') {
			cb(message);
		} else {
			// TODO error popup?
		}
	};

	var errCallback = function (message) {
		if (typeof errCb === 'function') {
			errCb(message);
		}
	};

	exec(callback,
			errCallback,
			"FilePicker",
			"chooseFile",
			[defaultConfig]
	);
};

var filePicker = new FilePicker();
module.exports = filePicker;

// Make plugin work under window.plugins
if (!window.plugins) {
	window.plugins = {};
}
if (!window.plugins.FilePicker) {
	window.plugins.FilePicker = filePicker;
}
