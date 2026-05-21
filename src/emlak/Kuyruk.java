package emlak;

public class Kuyruk<T> {
    private BagliListe<T> liste = new BagliListe<>();

    public void ekle(T veri) { liste.sonaEkle(veri); }
    public T cikar() { return liste.basSil(); }
    public boolean bosMu() { return liste.boyut() == 0; }
}
