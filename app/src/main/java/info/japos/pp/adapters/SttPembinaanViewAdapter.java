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
import info.japos.pp.models.kbm.pembinaan.Pembinaan;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.utils.BabushkaText;
import info.japos.utils.Utils;

public class SttPembinaanViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private WeakReference<Context> mContextWeakReference;
    private ArrayList<ItemSectionInterface> dataAndSectionList;
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

    public SttPembinaanViewAdapter(Context context, OnItemSelected onItemSelectedListener, ArrayList<ItemSectionInterface> items) {
        this.mContextWeakReference = new WeakReference<>(context);
        this.onItemSelectedListener = onItemSelectedListener;
        this.dataAndSectionList = items;

        // init
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = mContextWeakReference.get();
        if (viewType == SECTION_VIEW) {
            return new SectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pembinaan_group, parent, false));
        }
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pembinaan, parent, false), context);
    }

    @Override
    public int getItemViewType(int position) {
        if (dataAndSectionList.get(position).isSection()) {
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
            SectionGroupTitle sectionItem = ((SectionGroupTitle) dataAndSectionList.get(position));

            sectionViewHolder.title.setText(sectionItem.title);
            return;
        }

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Pembinaan pembinaan = (Pembinaan) dataAndSectionList.get(position);
        String[] splitNama = pembinaan.getPembinaan().split(" ", 2);
        String strInisial = (splitNama.length == 1) ? String.valueOf(pembinaan.getPembinaan().charAt(0)) : String.valueOf(splitNama[0].charAt(0)) + String.valueOf(splitNama[1].charAt(0));
        TextDrawable drawable = tBuilder.build(strInisial, mColorGenerator.getColor(pembinaan.getPembinaan()));
        myViewHolder.imageView.setImageDrawable(drawable);
        myViewHolder.pembinaan.setText(pembinaan.getPembinaan());

        // total peserta
        myViewHolder.ttlKelas.reset();
        myViewHolder.ttlKelas.addPiece(new BabushkaText.Piece.Builder("  " + pembinaan.getTotalKelas() + " Kelas  ")
                .backgroundColor(Color.parseColor("#01a0c4"))
                .textColor(Color.WHITE)
                .build());
        myViewHolder.ttlKelas.display();

        // total kbm
        myViewHolder.ttlKBM.reset();
        if (pembinaan.getTotalKBM() == null) {
            myViewHolder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  N/A  ")
                    .backgroundColor(Color.parseColor("#f4ac41"))
                    .textColor(Color.WHITE)
                    .build());
        } else {
            myViewHolder.ttlKBM.addPiece(new BabushkaText.Piece.Builder("  " + pembinaan.getTotalKBM() + " KBM  ")
                    .backgroundColor(Color.parseColor("#53ef8f"))
                    .textColor(Color.WHITE)
                    .build());
        }
        myViewHolder.ttlKBM.display();

        // to remove selection
        onListItemSelect(myViewHolder, pembinaan);

        myViewHolder.itemView.setOnClickListener(view -> {
            // remove past selection
            for (int i=0; i<mSelectedItemsIds.size(); i++) {
                if (mSelectedItemsIds.keyAt(i) != pembinaan.getId()) mSelectedItemsIds.delete(mSelectedItemsIds.keyAt(i));
            }
            notifyDataSetChanged();

            toggleSelectionState(myViewHolder, pembinaan);
            if (getSelectedCount() > 0) {
                onItemSelectedListener.itemSelectionChanged(Boolean.TRUE);
            } else {
                onItemSelectedListener.itemSelectionChanged(Boolean.FALSE);
            }
        });
    }

    private void toggleSelectionState(MyViewHolder holder, final Pembinaan pembinaan) {
        boolean selection = !mSelectedItemsIds.get(pembinaan.getId());
        if (selection) {
            mSelectedItemsIds.put(pembinaan.getId(), Boolean.TRUE);
        } else {
            mSelectedItemsIds.delete(pembinaan.getId());
        }
        notifyItemChanged(holder.getAdapterPosition());
    }

    private void onListItemSelect(MyViewHolder holder, final Pembinaan pembinaan) {
        Context context = mContextWeakReference.get();
        boolean selection = mSelectedItemsIds.get(pembinaan.getId());
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

    public Pembinaan getSelectedItem() {
        if (getSelectedCount() == 0) return null;
        return  (Pembinaan) dataAndSectionList.get(dataAndSectionList.indexOf(new Pembinaan(mSelectedItemsIds.keyAt(0))));
    }

    // remove selection
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    };

    @Override
    public int getItemCount() {
        return dataAndSectionList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView, checkIcon;
        TextView pembinaan;
        BabushkaText ttlKBM, ttlKelas;

        public MyViewHolder(View itemView, final Context context) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkIcon = itemView.findViewById(R.id.check_icon);
            pembinaan = itemView.findViewById(R.id.tv_pembinaan);
            ttlKelas = itemView.findViewById(R.id.tv_ttl_kelas);
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
