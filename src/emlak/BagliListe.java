package emlak;

public class BagliListe<T> {
    private Dugum<T> bas;
    private int boyut;

    public void basaEkle(T veri) {
        Dugum<T> yeni = new Dugum<>(veri);
        yeni.sonraki = bas;
        bas = yeni;
        boyut++;
    }

    public void sonaEkle(T veri) {
        Dugum<T> yeni = new Dugum<>(veri);
        if (bas == null) {
            bas = yeni;
        } else {
            Dugum<T> gec = bas;
            while (gec.sonraki != null) gec = gec.sonraki;
            gec.sonraki = yeni;
        }
        boyut++;
    }

    public T basSil() {
        if (bas == null) return null;
        T silinen = bas.veri;
        bas = bas.sonraki;
        boyut--;
        return silinen;
    }

    public boolean veriSil(T hedef) {
        if (bas == null || hedef == null) return false;

        if (bas.veri.equals(hedef)) {
            basSil();
            return true;
        }

        Dugum<T> gec = bas;
        while (gec.sonraki != null) {
            if (gec.sonraki.veri.equals(hedef)) {
                gec.sonraki = gec.sonraki.sonraki;
                boyut--;
                return true;
            }
            gec = gec.sonraki;
        }
        return false;
    }

    public Dugum<T> basDugum() { return bas; }
    public int boyut() { return boyut; }

    public Object[] diziyeCevir() {
        Object[] dizi = new Object[boyut];
        Dugum<T> gec = bas;
        int i = 0;
        while (gec != null) {
            dizi[i++] = gec.veri;
            gec = gec.sonraki;
        }
        return dizi;
    }

    public void dizidenYukle(T[] dizi) {
        bas = null;
        boyut = 0;
        for (T eleman : dizi) sonaEkle(eleman);
    }

    public void temizle() { 
        bas = null; 
        boyut = 0; 
    }
}
