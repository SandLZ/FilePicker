package com.handsmap.filepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handsmap.filepicker.FakeR;
import com.handsmap.filepicker.adapter.PathAdapter;
import com.handsmap.filepicker.filter.LFileFilter;
import com.handsmap.filepicker.model.ParamEntity;
import com.handsmap.filepicker.utils.Constant;
import com.handsmap.filepicker.utils.FileUtils;
import com.handsmap.filepicker.widget.EmptyRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LFilePickerActivity extends Activity {

    private final String TAG = "FilePickerLeon";
    private EmptyRecyclerView mRecylerView;
    private View mEmptyView;
    private TextView mTvPath;
    private TextView mTvBack;
    private Button mBtnAddBook;
    private String mPath;
    private List<File> mListFiles;
    // 导航栏
    private RelativeLayout rel_header;
    private RelativeLayout rel_back;
    // 返回按钮
    private TextView mTvBack2;
    private ImageView mImgBack;
    // 标题
    private TextView mTvTitle;
    // 存放选中条目的数据地址
    private ArrayList<String> mListNumbers = new ArrayList<String>();
    private PathAdapter mPathAdapter;
    private ParamEntity mParamEntity;
    private LFileFilter mFilter;
    private boolean mIsAllSelected = false;
    // 位置信息
    private List<String> mPositionList = new ArrayList<>();
    private FakeR fakeR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fakeR = new FakeR(this);
        final View view = View.inflate(this, fakeR.getId("layout", "activity_lfile_picker"), null);
        setContentView(view);
        mParamEntity = (ParamEntity) getIntent().getExtras().getSerializable("param");
        initView();
        initToolbar();
        updateAddButton();
        if (!checkSDState()) {
            Toast.makeText(this, "SD 存储卡状态不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mTvPath.setText(mPath);
        mFilter = new LFileFilter(mParamEntity.getFileTypes());
        mListFiles = getFileList(mPath);
        mPathAdapter = new PathAdapter(mListFiles, this, mFilter, mParamEntity.isMutilyMode(),
                mParamEntity.getMaxNum(), mParamEntity.getChooseMode(), fakeR);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mPathAdapter.setmIconStyle(mParamEntity.getIconStyle());
        mRecylerView.setAdapter(mPathAdapter);
        mRecylerView.setmEmptyView(mEmptyView);
        initListener();
    }

    /**
     * 更新Toolbar展示
     */
    private void initToolbar() {
        if (mParamEntity.getTitle() != null) {
            mTvTitle.setText(mParamEntity.getTitle());
        }
        if (mParamEntity.getTitleColor() != null) {
            mTvTitle.setTextColor(Color.parseColor(mParamEntity.getTitleColor())); //设置标题颜色
        }
        if (mParamEntity.getBackgroundColor() != null) {
            rel_header.setBackgroundColor(Color.parseColor(mParamEntity.getBackgroundColor()));
        }
        switch (mParamEntity.getBackIcon()) {
            case Constant.BACKICON_STYLEONE:
                mImgBack.setImageResource(fakeR.getId("mipmap", "backincostyleone"));
                break;
            case Constant.BACKICON_STYLETWO:
                mImgBack.setImageResource(fakeR.getId("mipmap", "backincostyletwo"));
                break;
            case Constant.BACKICON_STYLETHREE:
                //默认风格
                mImgBack.setImageResource(fakeR.getId("mipmap", "backincostyleone"));
                break;
        }
        rel_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void updateAddButton() {
        if (!mParamEntity.isMutilyMode()) {
            mBtnAddBook.setVisibility(View.GONE);
        }
        if (mParamEntity.getChooseMode() == 1) {
            mBtnAddBook.setVisibility(View.VISIBLE);
            mBtnAddBook.setText("确定");
        }
    }

    /**
     * 添加点击事件处理
     */
    private void initListener() {
        // 返回目录上一级
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPath = new File(mPath).getParent();
                if (tempPath == null) {
                    mTvBack.setVisibility(View.GONE);
                    return;
                } else {
                    if (mTvBack.getVisibility() == View.GONE) {
                        mTvBack.setVisibility(View.VISIBLE);
                    }
                }
                if (tempPath.equals("/")) {
                    mTvBack.setVisibility(View.GONE);
                }
                mPath = tempPath;
                mListFiles = getFileList(mPath);
                mPathAdapter.setmListData(mListFiles);
                mPathAdapter.updateAllSelelcted(false);
                mIsAllSelected = false;
                updateMenuTitle();
                mBtnAddBook.setText("选中");
                if (mPositionList.size() > 0) {
                    String posAndOffset = mPositionList.get(mPositionList.size() - 1);
                    String[] posAndOffsetArr = posAndOffset.split(",");
                    int pos = Integer.parseInt(posAndOffsetArr[0]);
                    int offset = Integer.parseInt(posAndOffsetArr[1]);
                    ((LinearLayoutManager) mRecylerView.getLayoutManager())
                            .scrollToPositionWithOffset(pos, offset);
                    // 移除最后一条数据
                    mPositionList.remove(mPositionList.size() - 1);
                } else {
                    mRecylerView.scrollToPosition(0);
                }
                setShowPath(mPath);
                //清除添加集合中数据
                mListNumbers.clear();
                if (mParamEntity.getAddText() != null) {
                    mBtnAddBook.setText(mParamEntity.getAddText());
                } else {
                    mBtnAddBook.setText("选中");
                }
            }
        });
        mPathAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
            @Override
            public void click(int position, boolean isCheckBox) {
                if (mParamEntity.isMutilyMode()) {
                    if (mListFiles.get(position).isDirectory()) {
                        if (isCheckBox) {
                            // 选中文件夹
                            // 获取此文件夹下文件
                            File file = mListFiles.get(position);
                            File[] files = file.listFiles(mFilter);
                            for (int i = 0; i < files.length; i++) {
                                String path = files[i].getAbsolutePath();
                                // 如果已经选择则取消，否则添加进来
                                if (mListNumbers.contains(path)) {
                                    mListNumbers.remove(path);
                                } else {
                                    mListNumbers.add(path);
                                }
                            }
                            if (mParamEntity.getAddText() != null) {
                                mBtnAddBook.setText(mParamEntity.getAddText() + "( " + mListNumbers.size() + " )");
                            } else {
                                mBtnAddBook.setText("选中" + "( " + mListNumbers.size() + " )");
                            }
                            //先判断是否达到最大数量，如果数量达到上限提示，否则继续添加
                            if (mParamEntity.getMaxNum() > 0 && mListNumbers.size() > mParamEntity.getMaxNum()) {
                                Toast.makeText(LFilePickerActivity.this, "已达到最大选择数量,请移除部分选择的文件", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            // 进入继续查看目录 清除已选文件/文件夹
                            mPathAdapter.updateAllSelelcted(false);
                            clearSelectFileOrDic();
                            chekInDirectory(position);
                            mPathAdapter.updateAllSelelcted(false);
                            mIsAllSelected = false;
                            updateMenuTitle();
                            if (mParamEntity.getAddText() != null) {
                                mBtnAddBook.setText(mParamEntity.getAddText());
                            } else {
                                mBtnAddBook.setText("选中");
                            }
                        }
                    } else {
                        //如果已经选择则取消，否则添加进来
                        if (mListNumbers.contains(mListFiles.get(position).getAbsolutePath())) {
                            mListNumbers.remove(mListFiles.get(position).getAbsolutePath());
                        } else {
                            mListNumbers.add(mListFiles.get(position).getAbsolutePath());
                        }
                        if (mParamEntity.getAddText() != null) {
                            mBtnAddBook.setText(mParamEntity.getAddText() + "( " + mListNumbers.size() + " )");
                        } else {
                            mBtnAddBook.setText("选中" + "( " + mListNumbers.size() + " )");
                        }
                        //先判断是否达到最大数量，如果数量达到上限提示，否则继续添加
                        if (mParamEntity.getMaxNum() > 0 && mListNumbers.size() > mParamEntity.getMaxNum()) {
                            Toast.makeText(LFilePickerActivity.this, mParamEntity.getMaxNumTips(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else {
                    //单选模式直接返回
                    if (mListFiles.get(position).isDirectory()) {
                        chekInDirectory(position);
                        return;
                    }
                    if (mParamEntity.getChooseMode() == 0) {
                        //选择文件模式,需要添加文件路径，否则为文件夹模式，直接返回当前路径
                        mListNumbers.add(mListFiles.get(position).getAbsolutePath());
                        chooseDone();
                    } else if (mParamEntity.getChooseMode() == 1) {
                        Toast.makeText(LFilePickerActivity.this, "请选择文件夹", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListNumbers.size() < 1) {
                    String info = "";
                    if (mParamEntity.getChooseMode() == 0) {
                        // 文件选择模式
                        info = mParamEntity.getNotFoundFiles();
                    } else if (mParamEntity.getChooseMode() == 1) {
                        info = "请至少选择一个文件夹";
                    } else {
                        info = "请选择文件或文件夹";
                    }
                    if (TextUtils.isEmpty(info)) {
                        Toast.makeText(LFilePickerActivity.this, "请选择文件或文件夹", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LFilePickerActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    chooseDone();
                }
            }
        });
    }


    /**
     * 点击进入目录
     *
     * @param position
     */
    private void chekInDirectory(int position) {
        int pos = ((LinearLayoutManager) mRecylerView.getLayoutManager()).findFirstVisibleItemPosition();
        View view = ((LinearLayoutManager) mRecylerView.getLayoutManager()).getChildAt(0);
        if (view != null) {
            int top = view.getTop();
            mPositionList.add(pos + "," + top);
        }
        mPath = mListFiles.get(position).getAbsolutePath();
        setShowPath(mPath);
        //更新数据源
        mListFiles = getFileList(mPath);
        mPathAdapter.setmListData(mListFiles);
        mPathAdapter.notifyDataSetChanged();
        mRecylerView.scrollToPosition(0);
        // 设置返回上一级
        String tempPath = new File(mPath).getParent();
        if (tempPath == null) {
            mTvBack.setVisibility(View.GONE);
            return;
        } else {
            if (mTvBack.getVisibility() == View.GONE) {
                mTvBack.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 完成提交
     */
    private void chooseDone() {
        //判断是否数量符合要求
        if (mParamEntity.getMaxNum() > 0 && mListNumbers.size() > mParamEntity.getMaxNum()) {
            Toast.makeText(LFilePickerActivity.this, mParamEntity.getMaxNumTips(), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("paths", mListNumbers);
        intent.putExtra("path", mTvPath.getText().toString().trim());
        setResult(RESULT_OK, intent);
        this.finish();
    }

    /**
     * 根据地址获取当前地址下的所有目录和文件，并且排序
     *
     * @param path
     * @return List<File>
     */
    private List<File> getFileList(String path) {
        List<File> list = FileUtils.getFileListByDirPath(path, mFilter);
        return list;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mRecylerView = (EmptyRecyclerView) findViewById(fakeR.getId("id", "recylerview"));
        mTvPath = (TextView) findViewById(fakeR.getId("id", "tv_path"));
        mTvBack = (TextView) findViewById(fakeR.getId("id", "tv_back"));
        mBtnAddBook = (Button) findViewById(fakeR.getId("id", "btn_addbook"));
        mEmptyView = findViewById(fakeR.getId("id", "empty_view"));
        mTvBack2 = (TextView) findViewById(fakeR.getId("id", "file_btn_back"));
        mTvTitle = (TextView) findViewById(fakeR.getId("id", "file_tv_title"));
        rel_header = (RelativeLayout) findViewById(fakeR.getId("id", "toolbar"));
        rel_back = (RelativeLayout) findViewById(fakeR.getId("id", "rel_file_back"));
        mImgBack = (ImageView) findViewById(fakeR.getId("id", "img_file_back"));
        if (mParamEntity.getAddText() != null) {
            mBtnAddBook.setText(mParamEntity.getAddText());
        }
        mTvBack.setText(mParamEntity.getBackText());
    }

    /**
     * 检测SD卡是否可用
     */
    private boolean checkSDState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 显示顶部地址
     *
     * @param path
     */
    private void setShowPath(String path) {
        mTvPath.setText(path);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        updateOptionsMenu(menu);
        return true;
    }

    /**
     * 更新选项菜单展示，如果是单选模式，不显示全选操作
     *
     * @param menu
     */
    private void updateOptionsMenu(Menu menu) {
//        mMenu.findItem(R.id.action_selecteall_cancel).setVisible(mParamEntity.isMutilyMode());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_selecteall_cancel) {
//            //将当前目录下所有文件选中或者取消
//            mPathAdapter.updateAllSelelcted(!mIsAllSelected);
//            mIsAllSelected = !mIsAllSelected;
//            if (mIsAllSelected) {
//                for (File mListFile : mListFiles) {
//                    //不包含再添加，避免重复添加
//                    if (!mListFile.isDirectory() && !mListNumbers.contains(mListFile.getAbsolutePath())) {
//                        mListNumbers.add(mListFile.getAbsolutePath());
//                    }
//                    if (mParamEntity.getAddText() != null) {
//                        mBtnAddBook.setText(mParamEntity.getAddText() + "( " + mListNumbers.size() + " )");
//                    } else {
//                        mBtnAddBook.setText("选中" + "( " + mListNumbers.size() + " )");
//                    }
//                }
//            } else {
//                mListNumbers.clear();
//                mBtnAddBook.setText("选中");
//            }
//            updateMenuTitle();
//        }
        return true;
    }

    /**
     * 更新选项菜单文字
     */
    public void updateMenuTitle() {

//        if (mIsAllSelected) {
//            mMenu.getItem(0).setTitle(getString(R.string.Cancel));
//        } else {
//            mMenu.getItem(0).setTitle(getString(R.string.SelectAll));
//        }
    }

    /**
     * 清除已选的文件/文件夹
     */
    private void clearSelectFileOrDic() {
        mListNumbers.clear();
    }

}
