package info.japos.pp.view.statistik;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;
import info.japos.pp.R;
import info.japos.pp.models.statistik.StatistikGeneral;
import info.japos.pp.models.enums.PresensiKet;

/**
 * Created by HWAHYUDI on 18-Feb-18.
 */

public class SortableStatistikPesertaTableView extends SortableTableView<StatistikGeneral> {

    public SortableStatistikPesertaTableView(Context context) {
        this(context, null);
    }

    public SortableStatistikPesertaTableView(Context context, AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
    }

    public SortableStatistikPesertaTableView(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);

        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context, "Nama Lengkap", "Hadir", "Alpa", "Izin");
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.table_header_text));
        simpleTableHeaderAdapter.setTextSize(14);
        setHeaderAdapter(simpleTableHeaderAdapter);

        final int rowColorEven = ContextCompat.getColor(context, R.color.table_data_row_even);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(4);
        tableColumnWeightModel.setColumnWeight(0, 4);
        tableColumnWeightModel.setColumnWeight(1, 2);
        tableColumnWeightModel.setColumnWeight(2, 2);
        tableColumnWeightModel.setColumnWeight(3, 2);
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, StatistikPesertaComparator.getSttNamaPesertaComparator());
        setColumnComparator(1, StatistikPesertaComparator.getSttHadirComparator(PresensiKet.H.ordinal()));
        setColumnComparator(2, StatistikPesertaComparator.getSttHadirComparator(PresensiKet.A.ordinal()));
        setColumnComparator(3, StatistikPesertaComparator.getSttHadirComparator(PresensiKet.I.ordinal()));
    }
}
