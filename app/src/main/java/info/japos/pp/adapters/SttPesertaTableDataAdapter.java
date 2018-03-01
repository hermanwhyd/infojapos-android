package info.japos.pp.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;
import info.japos.pp.models.statistik.StatistikGeneral;

/**
 * Created by HWAHYUDI on 18-Feb-18.
 *
 */

public class SttPesertaTableDataAdapter extends TableDataAdapter<StatistikGeneral> {
    private static final int TEXT_SIZE = 12;

    public SttPesertaTableDataAdapter(Context context, List<StatistikGeneral> data) {
        super(context, data);
    }


    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final StatistikGeneral sttPeserta = getRowData(rowIndex);

        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderNamaPeserta(sttPeserta);
                break;
            case 1:
                renderedView = renderHadir(sttPeserta);
                break;
            case 2:
                renderedView = renderAlpa(sttPeserta);
                break;
            case 3:
                renderedView = renderIzin(sttPeserta);
                break;
        }

        return renderedView;
    }

    private View renderNamaPeserta(StatistikGeneral stt) {
        return renderString(stt.getLabel());
    }

    private View renderHadir(StatistikGeneral stt) {
        return renderNumber(stt.getStatistik().getHadir());
    }

    private View renderAlpa(StatistikGeneral stt) {
        return renderNumber(stt.getStatistik().getAlpa());
    }

    private View renderIzin(StatistikGeneral stt) {
        return renderNumber(stt.getStatistik().getIzin());
    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 10, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setSingleLine(Boolean.TRUE);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private View renderNumber(final int value) {
        final TextView textView = new TextView(getContext());
        textView.setText(String.valueOf(value));
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return textView;
    }
}
