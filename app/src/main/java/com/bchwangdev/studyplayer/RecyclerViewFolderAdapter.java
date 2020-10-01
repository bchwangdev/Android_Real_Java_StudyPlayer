package com.bchwangdev.studyplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewFolderAdapter extends RecyclerView.Adapter<RecyclerViewFolderAdapter.ViewHolder> {

    //아이템리스트
    private ArrayList<mFolder> mData = new ArrayList<>();

    //생성자
    public RecyclerViewFolderAdapter(ArrayList<mFolder> mData) {
        this.mData = mData;
    }

    //클릭리스너 등록
    private RecyclerViewCheckListener mListner = null;

    public interface RecyclerViewCheckListener {
        void onFolderCheck(int position, boolean checked);
    }

    public void setOnCheckListener2(RecyclerViewCheckListener listener) {
        mListner = listener;
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public RecyclerViewFolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_folder, parent, false);
        return new RecyclerViewFolderAdapter.ViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewFolderAdapter.ViewHolder holder, final int position) {
        //★중요 -> 리사이클러뷰를 스크롤 하면 위에서 체크한항목이 언체크로 변하면서 onCheckedChanged가 실행되면서 코드 고장남
        //          때문에 스크롤 언체크 문제가 있었는데 setIsRecyclable(false)로 해결했음.
        holder.setIsRecyclable(false);
        mFolder item = mData.get(position);
        holder.tvFolderSongsCount.setText(item.getFolderInSongCount() + "");
        holder.tvFolderName.setText(item.getFolderName());
        holder.chkFolder.setChecked(item.isFolderChecked());

        if (mListner != null) {
            holder.chkFolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mListner.onFolderCheck(position, isChecked);
                    if (isChecked) mData.get(position).setFolderChecked(isChecked);
                    else mData.get(position).setFolderChecked(!isChecked);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderSongsCount;
        TextView tvFolderName;
        CheckBox chkFolder;

        ViewHolder(final View itemView) {
            super(itemView);
            //int pos = getAdapterPosition() ; //여기서도 setOnClickListener 코드 가능
            //https://recipes4dev.tistory.com/168?category=790402
            // 뷰 객체에 대한 참조. (hold strong reference)
            tvFolderSongsCount = itemView.findViewById(R.id.tvFolderSongsCount);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            chkFolder = itemView.findViewById(R.id.chkFolder);
        }
    }
}
