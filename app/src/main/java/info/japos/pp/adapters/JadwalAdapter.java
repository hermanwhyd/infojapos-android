package info.japos.pp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

import info.japos.pp.R;
import info.japos.pp.models.Jadwal;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.pp.models.view.SelectableJadwal;
import info.japos.utils.BabushkaText;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 17-Dec-17.
 */

public class JadwalAdapter extends RecyclerView.Adapter<JadwalAdapter.JadwalHolder> {
    private Context context;
    private List<SelectableJadwal> mValues;
    private boolean isMultiSelectionEnable = false;
    private static final int HIGHLIGHT_COLOR = 0x999be6ff;
    private OnItemSelected onItemSelectedListener;

    // TextDrawable
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL; // or use DEFAULT
    private TextDrawable.IBuilder tBuilder = TextDrawable.builder()
            .beginConfig()
                .bold()
                .toUpperCase()
            .endConfig()
            .rect();

    public JadwalAdapter(Context context, OnItemSelected onItemSelectedListener, List<SelectableJadwal> items, boolean isMultiSelectionEnable) {
        this.context = context;
        this.onItemSelectedListener = onItemSelectedListener;
        this.isMultiSelectionEnable = isMultiSelectionEnable;
        mValues = items;
    }


    @Override
    public JadwalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jadwal, parent,false);
        return new JadwalHolder(view);
    }

    @Override
    public void onBindViewHolder(final JadwalHolder holder, int position) {
        final SelectableJadwal sJadwal = mValues.get(position);

        if (!isMultiSelectionEnable) updateSelectedState(holder, sJadwal);
        String[] splitNama = sJadwal.getKelas().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(sJadwal.getKelas().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(sJadwal.getKelas()));
        holder.imageView.setImageDrawable(drawable);
        holder.kelas.setText(sJadwal.getKelas());
        holder.lokasi.setText(sJadwal.getLokasi());
        holder.jam.setText( sJadwal.getJamMulai() + " \u2014 " + sJadwal.getJamSelesai());

        // Babushka Text
        holder.presensi.reset();
        if (TextUtils.isEmpty(sJadwal.getStatus()) || sJadwal.getStatus().equalsIgnoreCase("")) {
            holder.presensi.addPiece(new BabushkaText.Piece.Builder("  N/A  ")
                    .backgroundColor(Color.parseColor("#f4ac41"))
                    .textColor(Color.WHITE)
                    .build());
        } else {
            holder.presensi.addPiece(new BabushkaText.Piece.Builder("  " + sJadwal.getStatus() + "  ")
                    .backgroundColor(Color.parseColor("#53ef8f"))
                    .textColor(Color.WHITE)
                    .build());
        }
        holder.presensi.display();

        // set item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMultiSelectionEnable) {
                    for (SelectableJadwal item : mValues) {
                        if (!item.equals(sJadwal) && item.isSelected()) item.setSelected(Boolean.FALSE);
                    }
                    notifyDataSetChanged();
                }

                Boolean isSelected = !sJadwal.isSelected();
                sJadwal.setSelected(isSelected);
                updateSelectedState(holder, sJadwal);
                if (isSelected && isMultiSelectionEnable) {
                    onItemSelectedListener.itemSelectionChanged(Boolean.TRUE);
                } else {
                    onItemSelectedListener.itemSelectionChanged(isAnyKelasSelected());
                }
            }
        });
    }

    public List<Jadwal> getSelectedJadwal() {
        List<Jadwal> result = new ArrayList<>(0);
        for (SelectableJadwal item : mValues) {
            if (item.isSelected()) result.add(item);
        }

        return result;
    }

    /**
     * Get single selected jadwal
     * @return
     */
    public SelectableJadwal getSingleSelectedJadwal() {
        for (SelectableJadwal item : mValues) {
            if (item.isSelected()) return  item;
        }

        return null;
    }

    public Boolean isAnyKelasSelected() {
        for (SelectableJadwal item : mValues) {
            if (item.isSelected()) return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void updateSelectedState(JadwalHolder holder, SelectableJadwal sJadwal) {
        Log.d(this.getClass().getSimpleName(), sJadwal.getKelas() + "->" + sJadwal.isSelected());
        if (sJadwal.isSelected()) {
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_selected_background));
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_background));
            holder.checkIcon.setVisibility(View.GONE);
        }
    }

    class JadwalHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView, checkIcon;
        TextView kelas, lokasi, jam;
        BabushkaText presensi;

        public JadwalHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkIcon = itemView.findViewById(R.id.check_icon);
            kelas = itemView.findViewById(R.id.tv_kelas);
            lokasi = itemView.findViewById(R.id.tv_lokasi);
            jam = itemView.findViewById(R.id.tv_jam);
            presensi = itemView.findViewById(R.id.tv_presensi);

            this.view = itemView;
        }
    }
}
