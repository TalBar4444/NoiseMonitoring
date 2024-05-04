package com.myapps.mobilesecurityapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.myapps.mobilesecurityapp.R;
import com.myapps.mobilesecurityapp.interfaces.RecyclerViewInterface;
import com.myapps.mobilesecurityapp.model.Audio;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private List<Audio> audioList;

    public AudioAdapter(List<Audio> audioList, RecyclerViewInterface recyclerViewInterface) {
        this.audioList = audioList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record,parent,false);
        return new AudioViewHolder(itemView, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = audioList.get(position);
        holder.record_LBL_date.setText(audio.getDateRecorded());
        holder.record_LBL_name.setText(audio.getGenericName());

    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder{
        MaterialTextView record_LBL_date;
        MaterialTextView record_LBL_name;

        public AudioViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            record_LBL_date = itemView.findViewById(R.id.record_LBL_date);
            record_LBL_name = itemView.findViewById(R.id.record_LBL_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
