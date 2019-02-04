package info.japos.pp.models.kbm.common;

public class SectionGroupTitle implements ItemSectionInterface {
    public String title;

    public SectionGroupTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
