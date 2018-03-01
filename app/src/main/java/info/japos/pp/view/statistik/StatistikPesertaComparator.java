package info.japos.pp.view.statistik;

import java.util.Comparator;

import info.japos.pp.models.statistik.StatistikGeneral;

/**
 * Created by HWAHYUDI on 18-Feb-18.
 */

public class StatistikPesertaComparator {
    public static Comparator<StatistikGeneral> getSttNamaPesertaComparator() {
        return new namaComparator();
    }

    public static Comparator<StatistikGeneral> getSttHadirComparator(int type) {
        return new numberComparator(type);
    }

    private static class namaComparator implements Comparator<StatistikGeneral> {
        @Override
        public int compare(StatistikGeneral t1, StatistikGeneral t2) {
            return t1.getLabel().compareTo(t2.getLabel());
        }
    }

    private static class numberComparator implements Comparator<StatistikGeneral> {
        private int type = 0;
        public numberComparator(int type) {
            this.type = type;
        }
        @Override
        public int compare(StatistikGeneral t1, StatistikGeneral t2) {
            switch (type) {
                case 0: // Hadir
                    return t1.getStatistik().getHadir().compareTo(t2.getStatistik().getHadir());
                case 1: // Alpa
                    return t1.getStatistik().getAlpa().compareTo(t2.getStatistik().getAlpa());
                case 2: // Izin
                    return t1.getStatistik().getIzin().compareTo(t2.getStatistik().getIzin());
                default:
                    return 1;
            }
        }
    }
}
