package info.japos.pp.adapters;

import android.content.Context;
import android.graphics.Color;
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
import info.japos.pp.models.Kelas;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.utils.BabushkaText;
import info.japos.utils.GsonUtil;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 19-Dec-17.
 */

public class StatistikViewAdapter extends RecyclerView.Adapter<StatistikViewAdapter.StatistikViewHolder> {
    private Context context;
    private List<Kelas> dataSet;
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

    public StatistikViewAdapter(Context context, OnItemSelected onItemSelectedListener, List<Kelas> items) {
        this.context = context;
        this.onItemSelectedListener = onItemSelectedListener;
        this.dataSet = items;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public StatistikViewAdapter.StatistikViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kelas, parent,false);
        return new StatistikViewAdapter.StatistikViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StatistikViewAdapter.StatistikViewHolder holder, int position) {
        Kelas kelas = dataSet.get(position);
        Log.d("SttPsertaAdapter", "Peserta" + position + " -> " + GsonUtil.getInstance().toJson(kelas));
        String[] splitNama = kelas.getKelas().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(kelas.getKelas().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(kelas.getKelas()));
        holder.imageView.setImageDrawable(drawable);
        holder.kelas.setText(kelas.getKelas());
        holder.pembinaan.setText(kelas.getLvPembinaan());
        holder.pembina.setText(kelas.getLvPembina() + " " + kelas.getNamaMajelisTaklim());

        // Babushka Text
        holder.ttlKBM.reset();
        if (kelas.getTotalKBM() == null) {
            holder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  N/A  ")
                    .backgroundColor(Color.parseColor("#f4ac41"))
                    .textColor(Color.WHITE)
                    .build());
        } else {
            holder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  " + kelas.getTotalKBM() + " KBM  ")
                    .backgroundColor(Color.parseColor("#53ef8f"))
                    .textColor(Color.WHITE)
                    .build());
        }
        holder.ttlKBM.display();

        // to remove selection
        onListItemSelect(holder, kelas);

        holder.itemView.setOnClickListener(view -> {
            // for single selection

            // remove past selection
            for (int i=0; i<mSelectedItemsIds.size(); i++) {
                if (mSelectedItemsIds.keyAt(i) != kelas.getId()) mSelectedItemsIds.delete(mSelectedItemsIds.keyAt(i));
            }
            notifyDataSetChanged();

            toggleSelectionState(holder, kelas);
            if (getSelectedCount() > 0) {
                onItemSelectedListener.itemSelectionChanged(Boolean.TRUE);
            } else {
                onItemSelectedListener.itemSelectionChanged(Boolean.FALSE);
            }
        });
    }

    /**
     * Hilight itemview yang di klik
     * @param holder
     * @param kelas
     */
    private void toggleSelectionState(StatistikViewHolder holder, final Kelas kelas) {
        boolean selection = !mSelectedItemsIds.get(kelas.getId());
        if (selection) {
            mSelectedItemsIds.put(kelas.getId(), Boolean.TRUE);
        } else {
            mSelectedItemsIds.delete(kelas.getId());
        }
        notifyItemChanged(holder.getAdapterPosition());
    }

    /**
     * Toggle state selected
     * @param holder
     * @param kelas
     */
    private void onListItemSelect(StatistikViewHolder holder, final Kelas kelas) {
        boolean selection = mSelectedItemsIds.get(kelas.getId());
        if (selection) {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_selected_background));
        } else {
            holder.checkIcon.setVisibility(View.GONE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_background));
        }
    }

    // Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public List<Kelas> getAllSelectedKelas() {
        List<Kelas> result = new ArrayList<>();
        for(int i=0; i<mSelectedItemsIds.size(); i++) {
            result.add(dataSet.get(dataSet.indexOf(new Kelas(mSelectedItemsIds.keyAt(i)))));
        }

        return result;
    }

    public Kelas getSelectedKelas() {
        if (getSelectedCount() == 0)
            return null;
        return getAllSelectedKelas().get(0);
    }

    // remove selection
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    };

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class StatistikViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView, checkIcon;
        TextView kelas, pembinaan, pembina;
        BabushkaText ttlKBM;

        public StatistikViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkIcon = itemView.findViewById(R.id.check_icon);
            kelas = itemView.findViewById(R.id.tv_kelas);
            pembinaan = itemView.findViewById(R.id.tv_lv_pembinaan);
            pembina = itemView.findViewById(R.id.tv_pembina);
            ttlKBM = itemView.findViewById(R.id.tv_ttl_kbm);

            this.view = itemView;
        }
    }
}
