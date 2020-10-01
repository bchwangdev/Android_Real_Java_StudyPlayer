package com.bchwangdev.studyplayer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity implements RecyclerViewFolderAdapter.RecyclerViewCheckListener {

    Button btnGetFolder;

    RecyclerView recyclerView;
    RecyclerViewFolderAdapter mAdapter = null;

    ArrayList<String> arrFolderFiles = new ArrayList<>();
    ArrayList<mFolder> arrInfoFolder = new ArrayList<>();
    ArrayList<Integer> arrCheckedFolder = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        btnGetFolder = findViewById(R.id.btnGetFolder);
        recyclerView = findViewById(R.id.recyclerView);

        //리스트뷰에 표시할 폴더 표시
        getFolders(new File(Environment.getExternalStorageDirectory().getParentFile().getParent())); //외장메모리(sd카드) -> ("/storage")
        getFolders(new File(Environment.getExternalStorageDirectory().getPath())); //내장메모리

        //리사이클러뷰에 LinearLayoutManager 지정. (vertical)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //리스트 항목에 구분선 넣어주기
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(new ColorDrawable(Color.WHITE));
        recyclerView.addItemDecoration(itemDecoration);

        //리사이클러뷰에 RecyclerViewFolderAdapter 객체 지정.
        mAdapter = new RecyclerViewFolderAdapter(arrInfoFolder);
        mAdapter.setOnCheckListener2(this);
        recyclerView.setAdapter(mAdapter);
    }

    //스마트폰의 mp3의 폴더를 전부 가져온다
    String folderPath = "";
    String folderName = "";
        public ArrayList<File> getFolders(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden() && singleFile.listFiles() != null) {
                    //재귀함수
                    arrayList.addAll(getFolders(singleFile));
                }else{
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav") || singleFile.getName().endsWith(".m4a")) {
                        //폴더안의 파일 갯수 가져오기
                        Integer filesCount = 0;
                        for(File singleFile2 : singleFile.getParentFile().listFiles()){
                            if(singleFile2.getPath().endsWith(".mp3") || singleFile2.getPath().endsWith(".wav") || singleFile2.getPath().endsWith(".m4a")){
                                filesCount++;
                            }
                        }
                        //폴더명 가져오기
                        folderPath = singleFile.getParentFile().getPath();
                        folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1);
                        arrInfoFolder.add(new mFolder(filesCount, folderName, folderPath));
                        break;
                    }
                }
            }
        }
        return arrayList;
    }

    //btnGetFolder 실행
    public void btnClick(View view) {
        //에러처리 : 폴더 선택이 없을 경우
        if (arrCheckedFolder.size() == 0) {
            Toast.makeText(this, "Please Select Folder", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.btnGetFolder:
                //리스트뷰에서 선택한 폴더안의 음악들을 list로 보내기
                getFolderFiles(arrCheckedFolder);
                Intent intent = new Intent();
                intent.putStringArrayListExtra("result", arrFolderFiles); //Arraylist<String>
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
    //리스트뷰에서 선택한 폴더의 mp3파일을 가져온다
    public void getFolderFiles(ArrayList<Integer> items) {
        for (int i = 0; i < items.size(); i++) {
            File file = new File(arrInfoFolder.get(items.get(i)).getFolderPath());
            for(File singleFile : file.listFiles()){
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav") || singleFile.getName().endsWith(".m4a")) {
                    arrFolderFiles.add(singleFile.getPath());
                }
            }
        }
    }


    //체크한 폴더를 기억
    @Override
    public void onFolderCheck(int position, boolean checked) {
        if (checked) {
            arrCheckedFolder.add(position);
        } else {
            for (int i = 0; i < arrCheckedFolder.size(); i++) {
                if (arrCheckedFolder.get(i).equals(position)) {
                    arrCheckedFolder.remove(i);
                }
            }
        }
    }

    //    //※참고코드 : 모든폴더안에 있는 mp3파일 가져오는 코드
//    public ArrayList<File> findSong(File file) {
//        ArrayList<File> arrayList = new ArrayList<>();
//        File[] files = file.listFiles();
//        if (files != null) {
//            for (File singleFile : files) {
//                if (singleFile.isDirectory() && !singleFile.isHidden()) {
//                    arrayList.addAll(findSong(singleFile));
//                } else {
//                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav") || singleFile.getName().endsWith(".m4a")) {
//                        arrayList.add(singleFile);
//                    }
//                }
//            }
//        }
//        return arrayList;
//    }
}
