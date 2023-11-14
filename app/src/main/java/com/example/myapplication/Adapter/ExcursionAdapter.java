package com.example.myapplication.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Excursion;
import com.example.myapplication.R;

import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {
    private List<Excursion> excursions;
    private final OnExcursionListener onExcursionListener;

    public ExcursionAdapter(List<Excursion> excursions, OnExcursionListener onExcursionListener) {
        this.excursions = excursions;
        this.onExcursionListener = onExcursionListener;
    }

    @Override
    public ExcursionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_excursion, parent, false);
        return new ExcursionViewHolder(view, onExcursionListener);

    }

    @Override
    public void onBindViewHolder(ExcursionViewHolder holder, int position) {
        holder.bind(excursions.get(position));

    }

    @Override
    public int getItemCount() {
        return excursions != null ? excursions.size() : 0;
    }

    public void setExcursions(List<Excursion> excursions) {
        this.excursions = excursions;
        Log.d("ExcursionAdapter", "Number of excursions set: " + excursions.size());
        notifyDataSetChanged();
    }



    class ExcursionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDate;
        Button editButton, deleteButton, saveButton, alertButton;

        ExcursionViewHolder(View itemView, OnExcursionListener listener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewExcursionTitle);
            textViewDate = itemView.findViewById(R.id.textViewExcursionDate);

            editButton = itemView.findViewById(R.id.buttonExcursionEdit);
            deleteButton = itemView.findViewById(R.id.buttonExcursionDelete);
            saveButton = itemView.findViewById(R.id.buttonExcursionSave);
            alertButton = itemView.findViewById(R.id.buttonSetExcursionAlert);

            editButton.setOnClickListener(v -> listener.onEditExcursionClicked(excursions.get(getAdapterPosition())));
            deleteButton.setOnClickListener(v -> listener.onDeleteExcursionClicked(excursions.get(getAdapterPosition())));
            saveButton.setOnClickListener(v -> listener.onSaveExcursionClicked(excursions.get(getAdapterPosition())));
            alertButton.setOnClickListener(v -> listener.onSetExcursionAlertClicked(excursions.get(getAdapterPosition())));
        }

        void bind(Excursion excursion) {
            textViewTitle.setText(excursion.getTitle());
            textViewDate.setText(excursion.getDate());
        }
    }

    public interface OnExcursionListener {
        void onEditExcursionClicked(Excursion excursion);

        void onDeleteExcursionClicked(Excursion excursion);

        void onSaveExcursionClicked(Excursion excursion);

        void onSetExcursionAlertClicked(Excursion excursion);

    }
}