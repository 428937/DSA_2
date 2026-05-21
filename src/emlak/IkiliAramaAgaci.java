package emlak;

public class IkiliAramaAgaci<T extends Comparable<T>> {
    private AgacDugum<T> kok;

    public void ekle(T veri) {
        kok = ekleR(kok, veri);
    }

    private AgacDugum<T> ekleR(AgacDugum<T> dugum, T veri) {
        if (dugum == null) return new AgacDugum<>(veri);
        if (veri.compareTo(dugum.veri) < 0)
            dugum.sol = ekleR(dugum.sol, veri);
        else
            dugum.sag = ekleR(dugum.sag, veri);
        return dugum;
    }

    public void siraliGezinti(java.util.function.Consumer<T> islem) {
        siraliGezintiR(kok, islem);
    }

    private void siraliGezintiR(AgacDugum<T> dugum, java.util.function.Consumer<T> islem) {
        if (dugum != null) {
            siraliGezintiR(dugum.sol, islem);
            islem.accept(dugum.veri);
            siraliGezintiR(dugum.sag, islem);
        }
    }

    public void temizle() {
        kok = null;
    }
}
