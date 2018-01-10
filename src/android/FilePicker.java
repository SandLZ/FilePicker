package com.handsmap.filepicker;

import android.content.Intent;
import android.util.Log;

import com.handsmap.filepicker.utils.Constant;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * This class echoes a string called from JavaScript.
 */
public class FilePicker extends CordovaPlugin {

    private final static int REQUESTCODE_FROM_ACTIVITY = 1000;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("chooseFile")) {
            String message = args.getString(0);
            this.callbackContext = callbackContext;
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        praseConfig(message);
        if (message == null || message.length() == 0) {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void praseConfig(String message) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(message);
            String title = jsonObj.getString("title");
            String titleBg = jsonObj.getString("titleBg");
            String noFound = jsonObj.getString("noChooseTips");
            String backText = jsonObj.getString("backText");
            String maxNumTips = jsonObj.getString("maxNumTips");
            String sureSelectText = jsonObj.getString("sureSelectText");
            int maxNum = jsonObj.getInt("maxNum");
            int chooseMode = jsonObj.getInt("chooseMode");
            boolean mitilyMode = jsonObj.getBoolean("multiSelect");
            String filter = jsonObj.getString("fileFilter");
            if (filter != null && filter.length() > 0) {
                String[] filters = filter.split(",");
                startFileChooseActivity(title, titleBg, maxNum, chooseMode, noFound,
                        mitilyMode, filters, backText, maxNumTips, sureSelectText);
            } else {
                startFileChooseActivity(title, titleBg, maxNum, chooseMode, noFound,
                        mitilyMode, null, backText, maxNumTips, sureSelectText);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startFileChooseActivity(String title, String titleBg, int maxNum, int chooseMode,
                                         String noFound, boolean mitilyMode, String[] filters,
                                         String backText, String maxNumTips, String sureSelectText) {
        cordova.setActivityResultCallback (this);
        new LFilePicker()
                .withActivity(cordova.getActivity())
                .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                .withTitle(title)
                .withBackgroundColor(titleBg)
                .withIconStyle(Constant.ICON_STYLE_YELLOW2)
                .withBackIcon(Constant.BACKICON_STYLETHREE)
                .withMutilyMode(mitilyMode)
                .withMaxNum(maxNum)
                .withAddText(sureSelectText)
                .withBackText(backText)
                .withMaxNumTips(maxNumTips)
                .withNotFoundBooks(noFound)
                .withChooseMode(chooseMode)//文件选择模式
                .withFileFilter(filters)
                .start();
        // .withFileFilter(new String[]{"txt", "png", "docx"})
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    jsonArray.put(list.get(i));
                }
                try {
                    jsonObject.put("fileList", jsonArray);
                    callbackContext.success(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callbackContext.error("解析错误");
                }
                String path = data.getStringExtra("path");
                Log.i("LeonFilePicker", path);
            }
        }
    }


}
