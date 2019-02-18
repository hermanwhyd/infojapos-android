package info.japos.pp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import info.japos.pp.R;
import info.japos.pp.models.kbm.common.ItemSectionInterface;
import info.japos.pp.models.kbm.common.SectionGroupTitle;
import info.japos.pp.models.kbm.kelas.Kelas;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.utils.BabushkaText;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 19-Dec-17.
 */

public class SttJadwalViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private WeakReference<Context> mContextWeakReference;
    private ArrayList<ItemSectionInterface> mKelasAndSectionList;
    private OnItemSelected onItemSelectedListener;
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

    public SttJadwalViewAdapter(Context context, OnItemSelected onItemSelectedListener, ArrayList<ItemSectionInterface> items) {
        this.mContextWeakReference = new WeakReference<>(context);
        this.onItemSelectedListener = onItemSelectedListener;
        this.mKelasAndSectionList = items;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = mContextWeakReference.get();
        if (viewType == SECTION_VIEW) {
            return new SectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kelas_group, parent, false));
        }
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kelas, parent, false), context);
    }

    @Override
    public int getItemViewType(int position) {
        if (mKelasAndSectionList.get(position).isSection()) {
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
            SectionGroupTitle sectionItem = ((SectionGroupTitle) mKelasAndSectionList.get(position));

            sectionViewHolder.title.setText(sectionItem.title);
            return;
        }

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Kelas kelas = (Kelas) mKelasAndSectionList.get(position);
        String[] splitNama = kelas.getKelas().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(kelas.getKelas().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(kelas.getKelas()));
        myViewHolder.imageView.setImageDrawable(drawable);
        myViewHolder.kelas.setText(kelas.getKelas());

        // total peserta
        myViewHolder.ttlPeserta.reset();
        myViewHolder.ttlPeserta.addPiece(new BabushkaText.Piece.Builder("  " + kelas.getTotalPeserta() + " Peserta  ")
                .backgroundColor(Color.parseColor("#01a0c4"))
                .textColor(Color.WHITE)
                .build());
        myViewHolder.ttlPeserta.display();

        // total kbm
        myViewHolder.ttlKBM.reset();
        if (kelas.getTotalKBM() == null) {
            myViewHolder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  N/A  ")
                    .backgroundColor(Color.parseColor("#f4ac41"))
                    .textColor(Color.WHITE)
                    .build());
        } else {
            myViewHolder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  " + kelas.getTotalKBM() + " KBM  ")
                    .backgroundColor(Color.parseColor("#53ef8f"))
                    .textColor(Color.WHITE)
                    .build());
        }
        myViewHolder.ttlKBM.display();

        // to remove selection
        onListItemSelect(myViewHolder, kelas);

        myViewHolder.itemView.setOnClickListener(view -> {
            // remove past selection
            for (int i=0; i<mSelectedItemsIds.size(); i++) {
                if (mSelectedItemsIds.keyAt(i) != kelas.getId()) mSelectedItemsIds.delete(mSelectedItemsIds.keyAt(i));
            }
            notifyDataSetChanged();

            toggleSelectionState(myViewHolder, kelas);
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
    private void toggleSelectionState(MyViewHolder holder, final Kelas kelas) {
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
    private void onListItemSelect(MyViewHolder holder, final Kelas kelas) {
        Context context = mContextWeakReference.get();
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

    public Kelas getSelectedKelas() {
        if (getSelectedCount() == 0) return null;
        return  (Kelas) mKelasAndSectionList.get(mKelasAndSectionList.indexOf(new Kelas(mSelectedItemsIds.keyAt(0))));
    }

    // remove selection
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    };

    @Override
    public int getItemCount() {
        return mKelasAndSectionList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView, checkIcon;
        TextView kelas;
        BabushkaText ttlKBM, ttlPeserta;

        public MyViewHolder(View itemView, final Context context) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkIcon = itemView.findViewById(R.id.check_icon);
            kelas = itemView.findViewById(R.id.tv_kelas);
            ttlPeserta = itemView.findViewById(R.id.tv_ttl_peserta);
            ttlKBM = itemView.findViewById(R.id.tv_ttl_kbm);

            this.view = itemView;
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
