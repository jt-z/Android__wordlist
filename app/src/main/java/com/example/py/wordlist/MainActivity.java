package com.example.py.wordlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.py.wordlist.dummy.Words;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements WordItemFragment.OnFragmentInteractionListener, WordDetailFragment.OnFragmentInteractionListener {

    private static final String TAG = "myTag";
    private String reply=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //新增单词
                InsertDialog();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WordsDB wordsDB=WordsDB.getWordsDB();
        if (wordsDB != null)
            wordsDB.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //查找
                SearchDialog();
                return true;
            case R.id.action_insert:
                //新增单词
                InsertDialog();
                return true;
            case R.id.action_online:
                //在线查询
                onlineDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 更新单词列表 无参数
    private void RefreshWordItemFragment() {
        WordItemFragment wordItemFragment = (WordItemFragment) getFragmentManager().findFragmentById(R.id.wordslist);
        wordItemFragment.refreshWordsList();
    }

    // 更新单词列表 有参数
    private void RefreshWordItemFragment(String strWord) {
        WordItemFragment wordItemFragment = (WordItemFragment) getFragmentManager().findFragmentById(R.id.wordslist);
        wordItemFragment.refreshWordsList(strWord);
    }

    //新增对话框
    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();

                        //使用insert方法插入
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.Insert(strWord, strMeaning, strSample);

                        //单词已经插入到数据库，更新显示列表
                        RefreshWordItemFragment();

                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()//创建对话框
                .show();//显示对话框

    }


    //删除对话框
    private void DeleteDialog(final String strId) {
        new AlertDialog.Builder(this).setTitle("删除单词")
                .setMessage("是否真的删除单词?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //既可以使用Sql语句删除，也可以使用使用delete方法删除
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.DeleteUseSql(strId);

                        //单词已经删除，更新显示列表
                        RefreshWordItemFragment();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).create().show();
    }

    //修改对话框
    private void UpdateDialog(final String strId, final String strWord, final String strMeaning, final String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.txtSample)).setText(strSample);
        new AlertDialog.Builder(this)
                .setTitle("修改单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strNewSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();

                        //既可以使用Sql语句更新，也可以使用使用update方法更新
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.UpdateUseSql(strId, strNewWord, strNewMeaning, strNewSample);

                        //单词已经更新，更新显示列表
                        RefreshWordItemFragment();
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //查找对话框
    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.searchterm, null);
        new AlertDialog.Builder(this)
                .setTitle("查找单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txtSearchWord = ((EditText) tableLayout.findViewById(R.id.txtSearchWord)).getText().toString();

                        //单词已经插入到数据库，更新显示列表
                        RefreshWordItemFragment(txtSearchWord);
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框

    }

    /**
     * 当用户在单词详细Fragment中单击时回调此函数
     */
    @Override
    public void onWordDetailClick(Uri uri) {

    }

    /**
     * 当用户在单词列表Fragment中单击某个单词时回调此函数
     * 判断如果横屏的话，则需要在右侧单词详细Fragment中显示
     */
    @Override
    public void onWordItemClick(String id) {
        if(isLand()) {
            //横屏的话则在右侧的WordDetailFragment中显示单词详细信息
            ChangeWordDetailFragment(id);
        }else{
            Intent intent = new Intent(MainActivity.this,WordDetailActivity.class);
            intent.putExtra(WordDetailFragment.ARG_ID, id);
            startActivity(intent);
        }

    }

    //是否是横屏
    private boolean isLand(){

        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)
            return true;
        return false;
    }

    private void ChangeWordDetailFragment(String id){
        Bundle arguments = new Bundle();
        arguments.putString(WordDetailFragment.ARG_ID, id);
        Log.v(TAG, id);

        WordDetailFragment fragment = new WordDetailFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.worddetail, fragment).commit();
    }

    @Override
    public void onDeleteDialog(String strId) {
        DeleteDialog(strId);
    }

    @Override
    public void onUpdateDialog(String strId) {
        WordsDB wordsDB=WordsDB.getWordsDB();
        if (wordsDB != null && strId != null) {

            Words.WordDescription item = wordsDB.getSingleWord(strId);
            if (item != null) {
                UpdateDialog(strId, item.word, item.meaning, item.sample);
            }

        }
    }

    //在线查询
    private void onlineDialog(){
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.youdao, null);
        new AlertDialog.Builder(this)
                .setTitle("有道查询")//标题
                .setView(tableLayout)//设置视图
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String onlineSearchWord = ((EditText) tableLayout.findViewById(R.id.online_search_word)).getText().toString();

                        //有道查询
                        Dao(onlineSearchWord);
                    }

                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //有道翻译辅助函数
    private void Dao(final String word){

        final Handler errHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String notice = "";
                switch (msg.what) {
                    case 20:
                        notice = "要翻译的文本过长";
                        break;
                    case 30:
                        notice = "无法进行有效的翻译";
                        break;
                    case 40:
                        notice = "不支持的语言类型";
                        break;
                    case 50:
                        notice = "无效的key";
                        break;
                    case 60:
                        notice = "无词典结果";
                        break;
                }
                Toast.makeText(MainActivity.this, notice, Toast.LENGTH_SHORT).show();
            }
        };
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x123) {
                    Toast.makeText(MainActivity.this,reply,Toast.LENGTH_LONG).show();
                } else if (msg.what == 0x124) {
                    Toast.makeText(MainActivity.this, "查询失败，请核实", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Runnable askForYouDao = new Runnable() {
            @Override
            public void run() {
                try {
                    String url_path = "http://fanyi.youdao.com/openapi.do?keyfrom=WordList-TWD&key=871743932&type=data&doctype=json&version=1.1&q="
                            + URLEncoder.encode(word, "utf8");
                    URL getUrl = new URL(url_path);
                    HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.connect();
                    BufferedReader replyReader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"));//约定输入流的编码
                    reply = replyReader.readLine();
                    JSONObject replyJson = new JSONObject(reply);
                    String errorCode = replyJson.getString("errorCode");
                    if (errorCode.equals("0")) {
                        String query = replyJson.getString("query");
                        JSONArray translation
                                = replyJson.has("translation") ? replyJson.getJSONArray("translation") : null;
                        JSONObject basic
                                = replyJson.has("basic") ? replyJson.getJSONObject("basic") : null;
                        String phonetic=null;
                        String uk_phonetic=null;
                        String us_phonetic=null;
                        JSONArray explains=null;
                        if(basic!=null){
                            phonetic=basic.has("phonetic")? basic.getString("phonetic"):null;
                            uk_phonetic=basic.has("uk-phonetic")? basic.getString("uk-phonetic"):null;
                            us_phonetic=basic.has("us-phonetic")? basic.getString("us-phonetic"):null;
                            explains=basic.has("explains")? basic.getJSONArray("explains"):null;
                        }

                        String translationStr="";
                        if(translation!=null){
                            translationStr="\n翻译：\n";
                            for(int i=0;i<translation.length();i++){
                                translationStr+="\t【"+(i+1)+"】"+translation.getString(i)+"\n";
                            }
                        }
                        String phoneticStr=(phonetic!=null? "\n发音："+phonetic:"")
                                +(uk_phonetic!=null? "\n英式发音："+uk_phonetic:"")
                                +(us_phonetic!=null? "\n美式发音："+us_phonetic:"");
                        String explainStr="";
                        if(explains!=null){
                            explainStr="\n\n释义：\n";
                            for(int i=0;i<explains.length();i++){
                                explainStr+="\t【"+(i+1)+"】"+explains.getString(i)+"\n";
                            }
                        }

                        reply="单词："+query+"\n"+translationStr+phoneticStr+explainStr;

                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.Insert(query,explainStr,null);

                        handler.sendEmptyMessage(0x123);
                    } else {
                        int what = Integer.parseInt(errorCode);
                        errHandler.sendEmptyMessage(what);
                    }


                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                    handler.sendEmptyMessage(0x124);
                }

            }
        };
        Thread thread=new Thread(askForYouDao);
        thread.start();

    }

}