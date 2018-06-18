package kr.co.hoon.a180530flickr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ProgressDialog progressDialog;
    EditText keyword;
    // 주소에서 파라미터로 입력할 부분은 맨뒤로 빼는것이 좋음
    // 중간에 있으면 스트링에서 +를 여러번 해야함
    String addr = "https://secure.flickr.com/services/rest/?method=flickr.photos.search&api_key=c11e334f6be2c8584beb0e8837c085f5&safe_search=1&content_type=1&sort=interestingness-desc&per_page=50&format=json&text=";

    // List 출력을 위한 변수
    SimpleAdapter adapter;
    List<HashMap<String,String>> photoinfoList;

    // 다운로드 받은 문자열을 textView에 출력하는 핸들러
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // progressDialog 닫기
            progressDialog.dismiss();
//            // 받은 데이터를 텍스트뷰에 출력
//            textView.setText(msg.obj.toString());
            // listView에 데이터가 바뀌었음을 알려줌
            adapter.notifyDataSetChanged();
//            Log.e("핸들러에서 리스트", photoinfoList.toString());
        }
    };

    // 버튼을 눌렀을 때 호출되는 메소드
    public void getJSON() {
        // 스레드 객체 생성
        Thread th = new Thread(){
            // 스레드로 동작할 내용
            @Override
            public void run() {
                // 결과를 저장할 변수
                String result = null;
                try{
                    // 주소 만들기 - 인코딩
                    String keywordEn = URLEncoder.encode(keyword.getText().toString().trim(), "UTF-8");
                    addr += keywordEn;

                    // URL 생성
                    URL url = new URL(addr);

                    // Connection 만들기
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    // 옵션설정
                    conn.setUseCaches(false);
                    conn.setConnectTimeout(30000);

                    // 다운로드받을 Stream
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    // 줄단위로 읽어서 StringBuilder 객체에 저장
                    StringBuilder sb = new StringBuilder();
                    while(true){
                        String line = br.readLine();
                        if(line == null){
                            break;
                        }
                        sb.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    result = sb.toString();

                    // 핸들러 호출
                    //                    Message msg = new Message();
                    //                    msg.obj = result;
                    //                    handler.sendMessage(msg);


                }catch(Exception e){
                    Log.e("다운로드 예외: ", e.getMessage());
                }


                // JSON 파싱
                if(result != null){
                    result = result.replace("jsonFlickrApi(", "");
                    result = result.replace(")", "");

//                    Log.e("result",result);

                    try{
                        JSONObject root = new JSONObject(result);
//                        Log.e("root", root.toString());
                        JSONObject photos = root.getJSONObject("photos");
                        JSONArray photo = photos.getJSONArray("photo");
                        // list 초기화
                        photoinfoList.clear();
                        for(int i = 0; i<photo.length(); i=i+1){
                            JSONObject item = photo.getJSONObject(i);

                            HashMap<String,String> map = new HashMap<>();
                            map.put("id",item.getString("id"));
                            map.put("secret", item.getString("secret"));
                            map.put("server", item.getString("server"));
                            map.put("farm", item.getString("farm"));
                            map.put("title", item.getString("title"));

                            photoinfoList.add(map);
//                            Log.e("map", map.toString());
                        }
                    }catch(Exception e){
                        Log.e("파싱 예외", e.getMessage());
                    }
//                    Log.e("list", photoinfoList.toString());
                    handler.sendEmptyMessage(0);
                }
            }
        };
        // 스래드 시작
        th.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        textView = (TextView)findViewById(R.id.textView);
        keyword = (EditText)findViewById(R.id.keyword);

        ListView listView = (ListView)findViewById(R.id.listView);

        photoinfoList = new ArrayList<>();
        // 아이템 항목이름 배열
        String[] from = {"id", "title", "secret", "server", "farm"};
        // 아이템 항목뷰의 아이디를 정수배열로 생성
        int [] to = {R.id.id, R.id.title, R.id.secret, R.id.server, R.id.farm};
        // adapter 생성
        // R.layout.listview_item 레이아웃에 photoinfoList의 각 항목의 from 키값을 to 배열에 있는 아이디에 맞춰서 출력
        adapter = new SimpleAdapter(this, photoinfoList, R.layout.listview_items, from, to);
        // listView에 adapter 연결
        listView.setAdapter(adapter);

        findViewById(R.id.search).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // progressDialog 출력
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("기달");
                progressDialog.show();
                // 다운로드 받는 메소드 호출
                getJSON();
            }
        });

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView listTitle = (TextView)findViewById(R.id.title);
                TextView listId = (TextView)findViewById(R.id.id);
                TextView listSecret = (TextView)findViewById(R.id.secret);
                TextView listServer = (TextView)findViewById(R.id.server);
                TextView listFarm = (TextView)findViewById(R.id.farm);

                Bundle bundle = new Bundle();
                bundle.putString("title", listTitle.getText().toString());
                bundle.putString("id", listId.getText().toString());
                bundle.putString("secret", listSecret.getText().toString());
                bundle.putString("server", listServer.getText().toString());
                bundle.putString("farm", listFarm.getText().toString());

                // 하위액티비티 호출
                Intent intent = new Intent(MainActivity.this, ShowImage.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
