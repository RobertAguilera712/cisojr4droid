package io.github.robertaguilera712.cisojr4droid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.robertaguilera712.cisojr4droid.utils.Callback;
import io.github.robertaguilera712.cisojr4droid.R;
import io.github.robertaguilera712.cisojr4droid.databinding.RomBinding;
import io.github.robertaguilera712.cisojr4droid.model.Rom;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final ArrayList<Rom> roms;
    private final boolean isDecompression;
    private final Context context;
    private final Callback.OnItemClick clickListener;

    public MyAdapter(ArrayList<Rom> roms, boolean isDecompression, Context context, Callback.OnItemClick clickListener) {
        this.roms = roms;
        this.isDecompression = isDecompression;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(RomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), isDecompression, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Rom currentRom = roms.get(position);
        toggleCardClickable(holder, currentRom);
        holder.binding.tvInputFilename.setText(currentRom.getInputFilename());
        holder.binding.tvOutputFilename.setText(currentRom.getOutputFilename());
        holder.binding.tvCompressionLevel.setText(String.valueOf(currentRom.getCompressionLevel()));
        holder.binding.tvDelete.setText(currentRom.isDelete() ? context.getString(R.string.delete_true) : context.getString(R.string.delete_false));
        holder.binding.tvStatus.setText(currentRom.getStatus());
        holder.binding.compressionProgressBar.setProgress(currentRom.getIntProgress());
    }

    private void toggleCardClickable(MyViewHolder holder, Rom rom) {
        if (!rom.getStatus().equals(context.getString(R.string.status_added))) {
            holder.binding.getRoot().setClickable(false);
            holder.binding.getRoot().setFocusable(false);
        } else {
            holder.binding.getRoot().setClickable(true);
            holder.binding.getRoot().setFocusable(true);
        }
    }

    @Override
    public int getItemCount() {
        return roms.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RomBinding binding;

        public MyViewHolder(RomBinding binding, boolean isDecompression, Callback.OnItemClick clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            if (isDecompression){
                this.binding.tvCompressionLevel.setVisibility(View.GONE);
                this.binding.lbCompressionLevel.setVisibility(View.GONE);
            }
            this.binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
