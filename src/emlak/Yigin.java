package emlak;

public class Yigin<T> {
    private BagliListe<T> liste = new BagliListe<>();

    public void ekle(T veri) { liste.basaEkle(veri); }
    public T cikar() { return liste.basSil(); }
    public boolean bosMu() { return liste.boyut() == 0; }
}
