package info.japos.pp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import info.japos.pp.R;
import info.japos.pp.models.kbm.common.ItemSectionInterface;
import info.japos.pp.models.kbm.jadwal.Jadwal;
import info.japos.pp.models.kbm.common.SectionGroupTitle;
import info.japos.utils.BabushkaText;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 17-Dec-17.
 */

public class JadwalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private WeakReference<Context> mContextWeakReference;
    private ArrayList<ItemSectionInterface> mJadwalAndSectionList;
    private OnItemSelectedListener listener;
    private SparseBooleanArray mSelectedItemsIds;

    public static final int SECTION_VIEW = 0;
    private static final int CONTENT_VIEW = 1;

    // TextDrawable
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL; // or use DEFAULT
    private TextDrawable.IBuilder tBuilder = TextDrawable.builder()
            .beginConfig()
            .bold()
            .toUpperCase()
            .endConfig()
            .rect();

    public JadwalAdapter(ArrayList<ItemSectionInterface> dataSet, Context context, OnItemSelectedListener listener) {
        this.mContextWeakReference = new WeakReference<>(context);
        this.listener = listener;
        mJadwalAndSectionList = dataSet;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public interface OnItemSelectedListener {
        void onMenuAction(Jadwal jadwal, MenuItem menuItem);
        void itemSelectionChanged(Boolean isAnyItemSelected);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = mContextWeakReference.get();
        if (viewType == SECTION_VIEW) {
            return new SectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jadwal_group, parent, false));
        }
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jadwal, parent, false), context);
    }

    @Override
    public int getItemViewType(int position) {
        if (mJadwalAndSectionList.get(position).isSection()) {
            return SECTION_VIEW;
        } else {
            return CONTENT_VIEW;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Context context = mContextWeakReference.get();
        if (context == null) return;

        if (SECTION_VIEW == getItemViewType(position)) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            SectionGroupTitle sectionItem = ((SectionGroupTitle) mJadwalAndSectionList.get(position));

            sectionViewHolder.title.setText(sectionItem.title);
            return;
        }

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Jadwal sJadwal = (Jadwal)mJadwalAndSectionList.get(position);

        String[] splitNama = sJadwal.getKelas().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(sJadwal.getKelas().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(sJadwal.getKelas()));
        myViewHolder.imageView.setImageDrawable(drawable);
        myViewHolder.kelas.setText(sJadwal.getKelas());
        myViewHolder.lokasi.setText(sJadwal.getLokasi());
        myViewHolder.jam.setText(sJadwal.getJamMulai() + " \u2014 " + sJadwal.getJamSelesai());

        // babuskatext statistik
        myViewHolder.statistik.reset();
        if (!TextUtils.isEmpty(sJadwal.getStatus())) {
            myViewHolder.statistik.addPiece(new BabushkaText.Piece.Builder(sJadwal.getHadir() + " hdr")
                    .textColor(Color.parseColor("#52e9ef"))
                    .textSizeRelative(0.75f)
                    .build());
            myViewHolder.statistik.addPiece(new BabushkaText.Piece.Builder(" ")
                    .build());
            myViewHolder.statistik.addPiece(new BabushkaText.Piece.Builder(sJadwal.getAlpa() + " alp")
                    .textColor(Color.parseColor("#FF3D00"))
                    .textSizeRelative(0.75f)
                    .build());
            myViewHolder.statistik.addPiece(new BabushkaText.Piece.Builder(" ")
                    .build());
            myViewHolder.statistik.addPiece(new BabushkaText.Piece.Builder(sJadwal.getIzin() + " izn")
                    .textColor(Color.parseColor("#FF9100"))
                    .textSizeRelative(0.75f)
                    .build());
            myViewHolder.statistik.display();
        }

        // babuskatext presensi
        myViewHolder.presensi.reset();
        if (TextUtils.isEmpty(sJadwal.getStatus()) || sJadwal.getStatus().equalsIgnoreCase("")) {
            myViewHolder.presensi.addPiece(new BabushkaText.Piece.Builder("  N/A  ")
                    .backgroundColor(Color.parseColor("#f4ac41"))
                    .textColor(Color.WHITE)
                    .build());
        } else {
            myViewHolder.presensi.addPiece(new BabushkaText.Piece.Builder("  " + sJadwal.getTotalPeserta() + " siswa  ")
                    .backgroundColor(Color.parseColor("#00E676"))
                    .textColor(Color.WHITE)
                    .build());
        }
        myViewHolder.presensi.display();

        // to remove selection
        onListItemSelect(myViewHolder, sJadwal);

        // set item click listener
        myViewHolder.itemView.setOnClickListener(view -> {
            // remove past selection
            for (int i=0; i<mSelectedItemsIds.size(); i++) {
                if (mSelectedItemsIds.keyAt(i) != sJadwal.getId()) mSelectedItemsIds.delete(mSelectedItemsIds.keyAt(i));
            }
            notifyDataSetChanged();

            toggleSelectionState(myViewHolder, sJadwal);
            if (getSelectedCount() > 0) {
                listener.itemSelectionChanged(Boolean.TRUE);
            } else {
                listener.itemSelectionChanged(Boolean.FALSE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mJadwalAndSectionList.size();
    }

    // Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    // Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    // remove selection
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Return all ids
    public Jadwal getSelectedItem() {
        Jadwal result;
        result = (Jadwal) mJadwalAndSectionList.get(mJadwalAndSectionList.indexOf(new Jadwal(mSelectedItemsIds.keyAt(0))));

        return result;
    }

    /**
     * Hilight itemview yang di klik
     *
     * @param holder
     * @param jadwal
     */
    public void toggleSelectionState(MyViewHolder holder, final Jadwal jadwal) {
        boolean selection = !mSelectedItemsIds.get(jadwal.getId());
        if (selection) {
            mSelectedItemsIds.put(jadwal.getId(), Boolean.TRUE);
        } else {
            mSelectedItemsIds.delete(jadwal.getId());
        }
        notifyItemChanged(holder.getAdapterPosition());
    }

    private void onListItemSelect(MyViewHolder holder, final Jadwal jadwal) {
        boolean selection = mSelectedItemsIds.get(jadwal.getId());
        Context context = mContextWeakReference.get();
        if (selection) {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_selected_background));
        } else {
            holder.checkIcon.setVisibility(View.GONE);
            holder.view.setBackgroundColor(Utils.getColor(context, R.color.item_background));
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        View view;
        ImageView imageView, checkIcon;
        TextView kelas, lokasi, jam;
        BabushkaText presensi, statistik;
        ImageButton menuOpts;

        public MyViewHolder(View itemView, final Context context) {
            super(itemView);
            menuOpts = itemView.findViewById(R.id.tv_options);
            imageView = itemView.findViewById(R.id.image_view);
            checkIcon = itemView.findViewById(R.id.check_icon);
            kelas = itemView.findViewById(R.id.tv_kelas);
            lokasi = itemView.findViewById(R.id.tv_lokasi);
            jam = itemView.findViewById(R.id.tv_jam);
            presensi = itemView.findViewById(R.id.tv_presensi);
            statistik = itemView.findViewById(R.id.tv_statistik);

            this.view = itemView;

            menuOpts.setOnClickListener((view) -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                //inflating menu from xml resource
                popup.inflate(R.menu.jadwal_options);
                //adding click listener
                popup.setOnMenuItemClickListener(MyViewHolder.this);
                //displaying the popup
                popup.show();
            });
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                Jadwal s = (Jadwal) mJadwalAndSectionList.get(getAdapterPosition());
                listener.onMenuAction(s, item);
            }
            return false;
        }
    }

    public class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public SectionViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_group_title);
        }
    }
}
