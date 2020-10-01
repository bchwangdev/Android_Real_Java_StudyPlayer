package com.bchwangdev.studyplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

// 참고한 사이트 ▶ https://www.tutorialspoint.com/android/android_mediaplayer.htm
public class MainActivity extends AppCompatActivity implements RecyclerViewMainAdapter.RecyclerViewClickListener {

    Toolbar toolbar;
    TextView tvMusicCurrentTime, tvMusicTotalTime, tvMusicSpeed, tvMusicSkip, tvSkipInBtn, tvSpeedInBtn;
    SeekBar sbSeekBar;
    ImageButton btnPlay, btnSkipUp, btnSkipDown, btnSpeedUp, btnSpeedDown;
    Menu menu;

    RecyclerView recyclerView;
    RecyclerViewMainAdapter mAdapter = null;
    LinearLayoutManager layoutManager = null;

    ArrayList<String> arrStrFullPath = new ArrayList<>();
    ArrayList<mMusic> arrSongsName = new ArrayList<>();

    int intMusicPosition, intCurrentPosition, intFinalPosition, intScrollPosition;
    String strCurrentTime, strFinalTime;
    Toast toast;

    MediaPlayer myMediaPlayer;
    Handler myHandler = new Handler();

    //환경설정 데이터
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    int intSetSkipTime;
    float fltSetSpeed;

    //툴바표시하기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    //툴바아이템클릭
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnFolder:
                //폴더 가져오기
                Intent myFileIntent = new Intent(this, FolderActivity.class);
                startActivityForResult(myFileIntent, 1);
                break;
            case R.id.btnLock:
                if (btnSkipDown.getVisibility() == View.VISIBLE) {
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock_black_24dp));
                    btnSpeedUp.setVisibility(View.INVISIBLE);
                    tvSpeedInBtn.setVisibility(View.INVISIBLE);
                    btnSpeedDown.setVisibility(View.INVISIBLE);
                    btnSkipUp.setVisibility(View.INVISIBLE);
                    tvSkipInBtn.setVisibility(View.INVISIBLE);
                    btnSkipDown.setVisibility(View.INVISIBLE);
                } else {
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_black_24dp));
                    btnSpeedUp.setVisibility(View.VISIBLE);
                    tvSpeedInBtn.setVisibility(View.VISIBLE);
                    btnSpeedDown.setVisibility(View.VISIBLE);
                    btnSkipUp.setVisibility(View.VISIBLE);
                    tvSkipInBtn.setVisibility(View.VISIBLE);
                    btnSkipDown.setVisibility(View.VISIBLE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //툴바 폴더 안에 파일 리스트 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            //★재생되는 음악 있으면 끄기
            if (myMediaPlayer != null) {
                myMediaPlayer.release();
                myMediaPlayer = null;
            }
            //재생버튼 초기표시
            btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            //기존 정보 지우기
            intMusicPosition = 0;
            intScrollPosition = 0;
            //현재 파일리스트 다 지우기
            arrStrFullPath = new ArrayList<>();
            arrSongsName = new ArrayList<>();
            //FolderActivity에서 가져온 데이터 사용해서 리사이클뷰에 보여줄 리스트 만들기
            arrStrFullPath = data.getStringArrayListExtra("result");
            for (int i = 0; i < arrStrFullPath.size(); i++) {
                arrSongsName.add(new mMusic((i + 1) + "", arrStrFullPath.get(i).substring(arrStrFullPath.get(i).lastIndexOf('/') + 1)));
            }
            //리사이클뷰에 데이터 만들어서 표시하기
            RecyclerViewGetData(arrSongsName);
        }
    }

    //첫시작
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMusicCurrentTime = findViewById(R.id.musicCurrentTime);
        tvMusicTotalTime = findViewById(R.id.musicTotalTime);
        tvMusicSpeed = findViewById(R.id.tvMusicSpeed);
        tvMusicSkip = findViewById(R.id.tvMusicSkip);
        tvSkipInBtn = findViewById(R.id.tvSkipInBtn);
        tvSpeedInBtn = findViewById(R.id.tvSpeedInBtn);
        sbSeekBar = findViewById(R.id.seekBar);
        btnPlay = findViewById(R.id.btnPlay);
        btnSkipUp = findViewById(R.id.btnSkipUp);
        btnSkipDown = findViewById(R.id.btnSkipDown);
        btnSpeedUp = findViewById(R.id.btnSpeedUp);
        btnSpeedDown = findViewById(R.id.btnSpeedDown);
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        //sd카드 권한주기
        runtimePermission();

        //툴바표시하기2/2
        toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        //리사이클러뷰 설정
        recyclerView = findViewById(R.id.recyclerView);
        //리사이클러뷰에 LinearLayoutManager 지정. (vertical)
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //리스트 항목에 구분선 넣어주기
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)); //색상지정없을때
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(new ColorDrawable(Color.WHITE));
        recyclerView.addItemDecoration(itemDecoration);

        //▼프리퍼런스
        setting = getSharedPreferences("setting", MODE_PRIVATE);
        editor = setting.edit();
        //프리퍼런스 마지막재생곡
        intMusicPosition = setting.getInt("intMusicPosition", 0);
        //프리퍼런스 스크롤위치
        intScrollPosition = setting.getInt("intScrollPosition", 0);
        recyclerView.scrollToPosition(intScrollPosition);
        //프리퍼런스 스킵타임
        intSetSkipTime = setting.getInt("skipTime", 3000);
        tvMusicSkip.setText(intSetSkipTime / 1000 + "");
        //프리퍼런스 스피드
        fltSetSpeed = setting.getFloat("speed", 1.0f);
        tvMusicSpeed.setText(String.format("%.1f", fltSetSpeed));
        //프리퍼런스 (기존데이터) 가져오기
        Gson gson = new Gson();
        String json = setting.getString("songList", null);
        if (json != null) {
            arrStrFullPath = gson.fromJson(json, new TypeToken<Collection<String>>() {
            }.getType());
            for (int i = 0; i < arrStrFullPath.size(); i++) {
                arrSongsName.add(new mMusic((i + 1) + "", arrStrFullPath.get(i).substring(arrStrFullPath.get(i).lastIndexOf('/') + 1)));
            }
            //리사이클뷰에 데이터 만들어서 표시하기
            RecyclerViewGetData(arrSongsName);
        }

        //▼시크바 움직이기
        sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (myMediaPlayer == null) return;
                myMediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    //리사이클러뷰에 데이터 표시하기
    public void RecyclerViewGetData(ArrayList<mMusic> items) {
        //리사이클러뷰에 RecyclerViewMainAdapter 객체 지정.
        mAdapter = new RecyclerViewMainAdapter(items);
        mAdapter.setOnClickListener2(this);
        recyclerView.setAdapter(mAdapter);
    }


    //버튼클릭(재생관련)
    public void btnClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay:
                //에러처리 : 처음 앱을 열어서 플레이버튼 눌렀을때
                if (arrStrFullPath.size() == 0) {
                    toast.cancel();
                    toast = Toast.makeText(this, "Please Insert Music", Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                if (myMediaPlayer == null) {
                    if (arrSongsName.size() <= intMusicPosition) {
                        recyclerView.findViewHolderForLayoutPosition(0).itemView.findViewById(R.id.tvMusicName).performClick();
                    } else {
                        recyclerView.findViewHolderForLayoutPosition(intMusicPosition).itemView.findViewById(R.id.tvMusicName).performClick();//★코드로 손으로 클릭한 효과줌
                    }
                    break;
                }
                //재생 or 정지
                if (myMediaPlayer.isPlaying()) {
                    btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    myMediaPlayer.pause();
                } else {
                    btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    myMediaPlayer.start();
                }
                break;
            case R.id.btnBack:
            case R.id.btnForward:
                //에러처리 : 재생중이 아닐때 엉뚱한 버튼 눌렀을때
                if (myMediaPlayer == null) {
                    toast.cancel();
                    toast = Toast.makeText(this, "Please Select Music", Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                if (view.getId() == R.id.btnBack) {
                    //뒤로 스킵
                    myMediaPlayer.seekTo(myMediaPlayer.getCurrentPosition() - intSetSkipTime);
                } else {
                    //앞으로 스킵
                    myMediaPlayer.seekTo(myMediaPlayer.getCurrentPosition() + intSetSkipTime);
                }
                break;
            case R.id.btnSkipUp:
            case R.id.btnSkipDown:
                boolean err1 = false;
                if (view.getId() == R.id.btnSkipUp) {
                    intSetSkipTime += 1000;
                    if (intSetSkipTime > 10000) {
                        intSetSkipTime = 10000;
                        err1 = true;
                    }
                } else {
                    intSetSkipTime -= 1000;
                    if (intSetSkipTime <= 2000) {
                        intSetSkipTime = 2000;
                        err1 = true;
                    }
                }
                if (err1) {
                    err1 = !err1;
                    toast.cancel(); //★토스트 중복방지
                    toast = Toast.makeText(this, "Set SkipTime 2~10", Toast.LENGTH_LONG);
                    toast.show();
                }
                //스킵타임 텍스트 설정
                tvMusicSkip.setText(intSetSkipTime / 1000 + "");
                break;
            case R.id.btnSpeedUp:
            case R.id.btnSpeedDown:
                boolean err2 = false;
                if (view.getId() == R.id.btnSpeedUp) {
                    fltSetSpeed += 0.1f;
                    if (fltSetSpeed > 2.0f) {
                        fltSetSpeed = 2.0f;
                        err2 = true;
                    }
                } else {
                    fltSetSpeed -= 0.1f;
                    if (fltSetSpeed <= 0.5f) {
                        fltSetSpeed = 0.5f;
                        err2 = true;
                    }
                }
                if (err2) {
                    err2 = !err2;
                    toast.cancel(); //★토스트 중복방지
                    toast = Toast.makeText(this, "Set Speed 0.5~2.0", Toast.LENGTH_LONG);
                    toast.show();
                }
                tvMusicSpeed.setText(String.format("%.1f", fltSetSpeed));
                break;
        }
    }


    @Override
    public void onSongNameClicked(int position) {
        //★재생되는 음악 있으면 끄기
        if (myMediaPlayer != null) {
            myMediaPlayer.release();
            myMediaPlayer = null;
        }
        //현재 누른 위치값 기억하기
        intMusicPosition = position;

        //▼음악재생
        //미디어플레이어 만들기
        Uri u = Uri.parse(arrStrFullPath.get(position));
        myMediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        intCurrentPosition = 0;
        intFinalPosition = myMediaPlayer.getDuration();
        myMediaPlayer.start();
        //플레이시간 설정 (참고사이트 ▶ https://codechacha.com/ko/java-convert-milliseconds-using-timeunit/)
        strFinalTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((long) intFinalPosition),
                TimeUnit.MILLISECONDS.toSeconds((long) intFinalPosition) % 60);
        tvMusicTotalTime.setText(" / " + strFinalTime);
        //시크바 설정
        sbSeekBar.setMax(intFinalPosition);
        //버튼 바꾸기
        btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);

        //★스레드시작
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    //★스레드
    float fltSetOriginSpeed = fltSetSpeed;
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (intCurrentPosition + 100 >= intFinalPosition) {
                myMediaPlayer.release();
                myMediaPlayer = null;
                intMusicPosition++;
                if (arrSongsName.size() <= intMusicPosition) {
                    //▼마지막곡 끝나면 핸들러 종류하고, 리사이클러뷰 스크롤 위치 변경하기
                    myHandler.removeCallbacks(UpdateSongTime);
                    recyclerView.scrollToPosition(0);
                    return;
                } else {
                    //▼다음곡시작
                    //★리사이클러뷰 항목 스타일을 MainActivity에서 수정하기
//                    RecyclerViewMainAdapter.ViewHolder viewHolder;
//                    viewHolder = (RecyclerViewMainAdapter.ViewHolder)recyclerView.findViewHolderForLayoutPosition(intMusicPosition);
//                    viewHolder.tvMusicName.setTypeface(null, Typeface.BOLD);
//                    viewHolder.tvMusicName.setTextColor(Color.WHITE);
//                    viewHolder.tvMusicNo.setTypeface(null, Typeface.BOLD);
//                    viewHolder.tvMusicNo.setTextColor(Color.WHITE);
//                    onSongNameClicked(intMusicPosition);

                    //에러처리 : 다음 음악이 실행될때, 현재 리스트에 없는 보이지 않는 음악 재생하려고 하면 에러남(view홀더가 없기 때문)
                    //★리사이클러뷰 스크롤 -> 때문에 미리 스크롤 해놓아야함.
                    layoutManager.smoothScrollToPosition(recyclerView, null, intMusicPosition + 5);
                    recyclerView.findViewHolderForLayoutPosition(intMusicPosition).itemView.findViewById(R.id.tvMusicName).performClick();
                }
            }

            //★스피드설정
            if (fltSetSpeed != fltSetOriginSpeed && myMediaPlayer.isPlaying()) {
                myMediaPlayer.setPlaybackParams(myMediaPlayer.getPlaybackParams().setSpeed(fltSetSpeed)); //코드 실행하면 중지음악이 지맘대로 재생됨
                fltSetOriginSpeed = fltSetSpeed;
            }
            //반복할 코드 -> 시크바갱신, 현재시간갱신
            if (myMediaPlayer != null) {
                intCurrentPosition = myMediaPlayer.getCurrentPosition();
                sbSeekBar.setProgress(intCurrentPosition); //현재 시크바위치
                strCurrentTime = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) intCurrentPosition),
                        TimeUnit.MILLISECONDS.toSeconds((long) intCurrentPosition) % 60);
                tvMusicCurrentTime.setText(strCurrentTime);
            }
            myHandler.postDelayed(this, 100);
        }
    };

    //노래 이름 표시하기
    @Override
    public void onSongInfoClicked(int position) {
        //Toast 글씨크기 확대
        toast.cancel(); //토스트 중복방지 & 작동안되는거 해결
        toast = Toast.makeText(this, arrSongsName.get(position).getMusicName().toString(), Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(17);
        toast.show();
    }


    //프리퍼런스 설정
    @Override
    protected void onStop() {
        super.onStop();
        //스킵 몇초 기억
        editor.putInt("skipTime", intSetSkipTime);
        //재생 스피드 기억
        editor.putFloat("speed", fltSetSpeed);
        //현재 리스트 기억
        if (arrStrFullPath.size() > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(arrStrFullPath);
            editor.putString("songList", json); //리스트 기억
            editor.putInt("intMusicPosition", intMusicPosition); //몇번째 음악이었는지 기억
            editor.putInt("intScrollPosition", layoutManager.findFirstVisibleItemPosition()); //스크롤 위치 기억
        }
        editor.commit();
    }

    //sd카드 접근 권한주기
    private void runtimePermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    //※참고코드 : 모든폴더안에 있는 mp3파일 가져오는 코드
    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav") || singleFile.getName().endsWith(".m4a")) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }
        return arrayList;
    }
}
