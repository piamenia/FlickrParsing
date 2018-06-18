package kr.co.hoon.a180530flickr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowImage extends AppCompatActivity {
    ImageView thumbImage;
    ImageView bigImage;
    TextView title;

    // 이미지파일의 경로를 받아스 Bitmap을 리턴하는 메소드
    public Bitmap getImageFromURL(final String photoURL){
        // 유효성 검사
        if(photoURL == null){
            return null;
        }
        try{
            URL url = new URL(photoURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setConnectTimeout(30000);
            con.setUseCaches(false);

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            Bitmap bitmap = BitmapFactory.decodeStream(bis);

            bis.close();
            con.disconnect();

            return bitmap;
        }catch (Exception e){
            Log.e("비트맵 변환 예외", e.getMessage());
        }
        return null;
    }

    // AsyncTask를 상속받은 클래스
    class ShowImageTask extends AsyncTask<String, Void, Bitmap[]>{
        ProgressDialog progressDialog;

        // 스레드가 수행되기 전에 호출되는 메소드
        // 메인스레드에서 동작: UI갱신 가능
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // ProgressDialog 출력
            progressDialog = new ProgressDialog(ShowImage.this);
            progressDialog.setMessage("기다려주세요.");
            progressDialog.show();
        }

        // 스레드로 수행할 메소드
        // 매개변수는 객체를 생성하고 execute 할 때 넘겨준 데이터
        // strings 는 배열
        // Thread 클래스의 run은 리턴타입이 void이기 때문에 스레드 작업 종료 후 데이터를 리턴할수 없어서 공유변수를 사용해야하지만
        // AsyncTask의 doInBackground는 리턴타입이 있어서 공유변수를 사용하지 않고도 데이터를 넘길수 있기 때문에 객체지향설계 측면에서 우수
        @Override
        protected Bitmap[] doInBackground(String... strings) {
            String thumb = strings[0];
            String big = strings[1];

            Bitmap [] bitmaps = new Bitmap[2];
            bitmaps[0] = getImageFromURL(thumb);
            bitmaps[1] = getImageFromURL(big);

            return bitmaps;
        }

        // 스레드 수행중 호출할 수 있는 메소드, 메인스레드에서 동작
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        // 스레드 수행 후 호출되는 메소드, 메인스레드에서 동작
        // 매개변수는 doInBackground 에서 리턴한 데이터
        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);

            // 다운로드 받은 이미지를 이미지뷰에 출력
            thumbImage.setImageBitmap(bitmaps[0]);
            bigImage.setImageBitmap(bitmaps[1]);

            progressDialog.dismiss();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showimage);

        thumbImage = (ImageView)findViewById(R.id.thumbImage);
        bigImage = (ImageView)findViewById(R.id.bigImage);

        // 이전 Activity에서 넘겨준 데이터
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString("title");
        String id = bundle.getString("id");
        String secret = bundle.getString("secret");
        String server = bundle.getString("server");
        String farm = bundle.getString("farm");

        // 다운로드 받을 주소 만들기
        String thumbAddr = "http://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+"_t.jpg";
        String bigAddr = "http://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+"_b.jpg";

//        Log.e("thumb",thumbAddr);
//        Log.e("big", bigAddr);

        // 스레드 객체 생성 및 실행
        ShowImageTask task = new ShowImageTask();
        task.execute(thumbAddr, bigAddr);

        // 버튼을 누르면 이전으로 돌아가기
        findViewById(R.id.back).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 현재 액티비티를 종료
                finish();
            }
        });
    }
}
