package info.japos.pp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import info.japos.pp.models.ClassParticipant;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 19-Dec-17.
 */

public class SttPesertaViewAdapter extends RecyclerView.Adapter<SttPesertaViewAdapter.PesertaViewHolder> {

    private List<ClassParticipant> dataSet;
    private Context context;
    private OnItemSelected onItemSelectedListener;
    private SparseBooleanArray mSelectedItemsIds;

    // TextDrawable
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL; // or use DEFAULT
    private TextDrawable.IBuilder tBuilder = TextDrawable.builder()
            .beginConfig()
            .bold()
            .toUpperCase()
            .endConfig()
            .rect();

    public SttPesertaViewAdapter(List<ClassParticipant> dataSet, Context context, OnItemSelected listener) {
        this.dataSet = dataSet;
        this.context = context;
        this.onItemSelectedListener = listener;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public SttPesertaViewAdapter.PesertaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peserta, parent, false);

        return new SttPesertaViewAdapter.PesertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SttPesertaViewAdapter.PesertaViewHolder holder, int position) {
        final ClassParticipant peserta = dataSet.get(position);
        String[] splitNama = peserta.getNamaLengkap().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(peserta.getNamaLengkap().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(peserta.getNamaLengkap()));

        // init getId, agar ikut di serialize oleh Gson
        peserta.getId();
        holder.imageView.setImageDrawable(drawable);
        holder.namaPanggilan.setText(peserta.getNamaPanggilan());
        holder.namaLengkap.setText(peserta.getNamaLengkap());
        holder.otherInfo.setText(context.getString(R.string.presensi_otherinfo, peserta.getKelompok(), peserta.getJenisKelamin()));

        // to remove selection
        onListItemSelect(holder, peserta);

        holder.itemView.setOnClickListener((view) -> {
            // reset for single selection
            for (int i=0; i<mSelectedItemsIds.size(); i++) {
                if (mSelectedItemsIds.keyAt(i) != peserta.getId()) mSelectedItemsIds.delete(mSelectedItemsIds.keyAt(i));
            }
            notifyDataSetChanged();

            toggleSelectionState(holder, peserta);
            onItemSelectedListener.itemSelectionChanged((getSelectedCount() > 0) ? Boolean.TRUE : Boolean.FALSE);
        });
    }

    /**
     * Hilight itemview yang di klik
     * @param holder
     * @param peserta
     */
    private void toggleSelectionState(PesertaViewHolder holder, final ClassParticipant peserta) {
        boolean selection = !mSelectedItemsIds.get(peserta.getId());
        if (selection) {
            mSelectedItemsIds.put(peserta.getId(), Boolean.TRUE);
        } else {
            mSelectedItemsIds.delete(peserta.getId());
        }
        notifyItemChanged(holder.getAdapterPosition());
    }

    /**
     * handler for notifydatachanged
     * @param holder
     * @param peserta
     */
    private void onListItemSelect(PesertaViewHolder holder, @NonNull final ClassParticipant peserta) {
        boolean selection = mSelectedItemsIds.get(peserta.getId());
        if (selection) {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_selected_background));
        } else {
            holder.checkIcon.setVisibility(View.GONE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_background));
        }
    }

    // remove selection
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    };

    // Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    // Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    // Return all ids
    public List<ClassParticipant> getSelectedPeserta() {
        List<ClassParticipant> result = new ArrayList<>();
        for(int i=0; i<mSelectedItemsIds.size(); i++) {
            result.add(dataSet.get(dataSet.indexOf(new ClassParticipant(mSelectedItemsIds.keyAt(i)))));
        }

        return result;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class PesertaViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView namaLengkap, namaPanggilan, otherInfo;
        ImageView imageView, checkIcon;

        PesertaViewHolder(View v) {
            super(v);
            namaLengkap = v.findViewById(R.id.tv_nama_lengkap);
            namaPanggilan = v.findViewById(R.id.tv_nama_panggilan);
            otherInfo = v.findViewById(R.id.tv_info_others);
            imageView = v.findViewById(R.id.iv_presensi);
            checkIcon = v.findViewById(R.id.check_icon);

            this.view = v;
        }
    }
}
