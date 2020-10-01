package com.bchwangdev.studyplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewMainAdapter extends RecyclerView.Adapter<RecyclerViewMainAdapter.ViewHolder> {

    private int currentPosition = -1;
    //아이템리스트
    private ArrayList<mMusic> mData = new ArrayList<>();
    // 생성자에서 데이터 리스트 객체를 전달받음.
    public RecyclerViewMainAdapter(ArrayList<mMusic> mData) {
        this.mData = mData;
    }

    //클릭리스너 등록
    private RecyclerViewClickListener mListner;
    public interface RecyclerViewClickListener {
        void onSongNameClicked(int position);
        void onSongInfoClicked(int position);
    }
    public void setOnClickListener2(RecyclerViewClickListener listener) {
        mListner = listener;
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public RecyclerViewMainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_main, parent, false);
        return new RecyclerViewMainAdapter.ViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewMainAdapter.ViewHolder holder, final int position) {
        mMusic item = mData.get(position);
        holder.tvMusicNo.setText(item.getMusicNo());
        holder.tvMusicName.setText(item.getMusicName());

        if (mListner != null) {
            holder.tvMusicName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPosition = position;
                    mListner.onSongNameClicked(position);
                    notifyDataSetChanged(); //이거 없으면 리스트에 클릭한 노래 강조표시가 안됨
                }
            });
            holder.tvMusicName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListner.onSongInfoClicked(position);
                    notifyDataSetChanged(); //이거 없으면 리스트에 클릭한 노래 강조표시가 안됨
                    return true;
                }
            });
            holder.ivMusicInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListner.onSongInfoClicked(position);
                    notifyDataSetChanged();
                }
            });
            //선택한 음악 강조하기 & 선택안된 음악 다시 되돌리기 -> notifyDataSetChanged();필요
            if (currentPosition == position) {
                holder.tvMusicNo.setTypeface(null, Typeface.BOLD);
                holder.tvMusicName.setTypeface(null, Typeface.BOLD);
                holder.tvMusicNo.setTextColor(Color.WHITE);
                holder.tvMusicName.setTextColor(Color.WHITE);
                //holder.tvMusicName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                //holder.tvMusicName.setSelected(true);
            } else {
                holder.tvMusicNo.setTypeface(null, Typeface.NORMAL);
                holder.tvMusicNo.setTextColor(Color.parseColor("#D6D7D7"));
                holder.tvMusicName.setTypeface(null, Typeface.NORMAL);
                holder.tvMusicName.setTextColor(Color.parseColor("#D6D7D7"));
            }
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMusicNo;
        TextView tvMusicName;
        ImageView ivMusicInfo;

        ViewHolder(final View itemView) {
            super(itemView);
            //int pos = getAdapterPosition() ; //여기서도 setOnClickListener 코드 가능
            //https://recipes4dev.tistory.com/168?category=790402
            // 뷰 객체에 대한 참조. (hold strong reference)
            tvMusicNo = itemView.findViewById(R.id.tvMusicNo);
            tvMusicName = itemView.findViewById(R.id.tvMusicName);
            ivMusicInfo = itemView.findViewById(R.id.ivMusicInfo);
        }
    }
}
