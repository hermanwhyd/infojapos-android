package info.japos.pp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

import info.japos.pp.R;
import info.japos.pp.models.Peserta;
import info.japos.pp.models.enums.PresensiKet;
import info.japos.utils.BabushkaText;
import info.japos.utils.Utils;
import info.japos.vendor.SwipeToAction;

/**
 * Created by HWAHYUDI on 19-Dec-17.
 */

public class PresensiViewAdapter extends RecyclerView.Adapter<PresensiViewAdapter.PresensiViewHolder> {

    private List<Peserta> dataSet;
    private Context context;
    private OnItemSelectedListener listener;
    private SparseBooleanArray mSelectedItemsIds;

    // TextDrawable
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL; // or use DEFAULT
    private TextDrawable.IBuilder tBuilder = TextDrawable.builder()
            .beginConfig()
            .bold()
            .toUpperCase()
            .endConfig()
            .rect();
    public PresensiViewAdapter(List<Peserta> dataSet, Context context, OnItemSelectedListener listener) {
        this.dataSet = dataSet;
        this.context = context;
        this.listener = listener;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public PresensiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_presensi, parent, false);

        return new PresensiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PresensiViewHolder holder, int position) {
        final Peserta peserta = dataSet.get(position);
        String[] splitNama = peserta.getNamaLengkap().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(peserta.getNamaLengkap().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(peserta.getNamaLengkap()));

        // init getId, agar ikut di serialize oleh Gson
        peserta.getJamaahId();
        holder.imageView.setImageDrawable(drawable);
        holder.namaPanggilan.setText(peserta.getNamaPanggilan());
        holder.namaLengkap.setText(peserta.getNamaLengkap());
        holder.otherInfo.setText(context.getString(R.string.presensi_otherinfo, peserta.getKelompok(), peserta.getGender()));

        // reset dulu
        holder.keterangan.reset();
        holder.keterangan.addPiece(new BabushkaText.Piece.Builder(PresensiKet.valueOf(peserta.getKeterangan()).getValue())
                .textColor(Color.parseColor(PresensiKet.valueOf(peserta.getKeterangan()).getColor()))
                .style(Typeface.BOLD)
                .build());
        if (peserta.getKeterangan().equalsIgnoreCase(PresensiKet.I.name())) {
            holder.keterangan.addPiece(new BabushkaText.Piece.Builder("\n" + peserta.getAlasan())
                    .textSizeRelative(0.7f).textColor(Utils.getColor(context, R.color.text_sub_gray))
                    .build());
        }
        holder.keterangan.display();

        // to remove selection
        onListItemSelect(holder, peserta);

        // binding data into swipe action
        holder.data = peserta;

        holder.imageView.setOnClickListener((view)  -> {
                listener.onImageClick(holder, peserta);
            }
        );
    }

    /**
     * Hilight itemview yang di klik
     * @param holder
     * @param peserta
     */
    public void toggleSelectionState(PresensiViewHolder holder, final Peserta peserta) {
        boolean selection = !mSelectedItemsIds.get(peserta.getJamaahId());
        if (selection) {
            mSelectedItemsIds.put(peserta.getJamaahId(), Boolean.TRUE);
        } else {
            mSelectedItemsIds.delete(peserta.getJamaahId());
        }
        notifyItemChanged(holder.getAdapterPosition());
    }

    private void onListItemSelect(PresensiViewHolder holder, final Peserta peserta) {
        boolean selection = mSelectedItemsIds.get(peserta.getJamaahId());
        if (selection) {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.getFront().setBackgroundColor(Utils.getColor(context, R.color.item_selected_background));
        } else {
            holder.checkIcon.setVisibility(View.GONE);
            holder.getFront().setBackgroundColor(Utils.getColor(context, R.color.item_background));
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
    public List<Peserta> getSelectedPeserta() {
        List<Peserta> result = new ArrayList<>();
        for(int i=0; i<mSelectedItemsIds.size(); i++) {
            result.add(dataSet.get(dataSet.indexOf(new Peserta(mSelectedItemsIds.keyAt(i)))));
        }

        return result;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface OnItemSelectedListener {
        void onPresensiMenuAction(Peserta peserta, MenuItem item);
        void onImageClick(PresensiViewHolder view, Peserta peserta);
    }

    public class PresensiViewHolder extends SwipeToAction.ViewHolder<Peserta>
                        implements View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
        public View view;
        TextView namaLengkap, namaPanggilan, otherInfo;
        ImageButton menuOpts, menuInfo;
        BabushkaText keterangan;
        ImageView imageView, checkIcon;
        PresensiViewHolder(View v) {
            super(v);

            namaLengkap = v.findViewById(R.id.tv_nama_lengkap);
            namaPanggilan = v.findViewById(R.id.tv_nama_panggilan);
            otherInfo = v.findViewById(R.id.tv_info_others);
            menuOpts = v.findViewById(R.id.tv_options);
            imageView = v.findViewById(R.id.iv_presensi);
            checkIcon = v.findViewById(R.id.check_icon);
            keterangan = v.findViewById(R.id.tv_keterangan);
            this.view = v;

            menuOpts.setOnClickListener((view) -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                //inflating menu from xml resource
                popup.inflate(R.menu.presensi_options);
                //adding click listener
                popup.setOnMenuItemClickListener(PresensiViewHolder.this);
                //displaying the popup
                popup.show();
            });

            // on longpress
//            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                Peserta peserta = dataSet.get(getAdapterPosition());
                listener.onPresensiMenuAction(peserta, item);
            }
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            //inflating menu from xml resource
            popup.inflate(R.menu.presensi_options);
            //adding click listener
            popup.setOnMenuItemClickListener(this);
            //displaying the popup
            popup.show();
        }
    }
}
