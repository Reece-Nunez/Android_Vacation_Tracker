package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Entity.Vacation;
import com.example.myapplication.R;
import java.util.List;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {
    private List<Vacation> vacations;
    private OnVacationListener onVacationListener;

    public VacationAdapter(List<Vacation> vacations, OnVacationListener onVacationListener) {
        this.vacations = vacations;
        this.onVacationListener = onVacationListener;
    }

    @Override
    public VacationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vacation, parent, false);
        return new VacationViewHolder(view, onVacationListener);
    }

    @Override
    public void onBindViewHolder(VacationViewHolder holder, int position) {
        holder.bind(vacations.get(position));
    }

    @Override
    public int getItemCount() {
        return vacations.size();
    }

    class VacationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewHotel, textViewStartDate, textViewEndDate;
        Button editButton, deleteButton;

        VacationViewHolder(View itemView, OnVacationListener listener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewVacationTitle);
            textViewHotel = itemView.findViewById(R.id.textViewHotel);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewEndDate = itemView.findViewById(R.id.textViewEndDate);

            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);

            editButton.setOnClickListener(v -> listener.onEditClicked(vacations.get(getAdapterPosition())));
            deleteButton.setOnClickListener(v -> listener.onDeleteClicked(vacations.get(getAdapterPosition())));

        }

        void bind(Vacation vacation) {
            textViewTitle.setText(vacation.getTitle());
            textViewHotel.setText(vacation.getHotel());
            textViewStartDate.setText(vacation.getStartDate());
            textViewEndDate.setText(vacation.getEndDate());
        }
    }

    public interface OnVacationListener {
        void onEditClicked(Vacation vacation);
        void onDeleteClicked(Vacation vacation);
    }

    public void setVacations(List<Vacation> vacations) {
        this.vacations = vacations;
        notifyDataSetChanged();
    }
}
