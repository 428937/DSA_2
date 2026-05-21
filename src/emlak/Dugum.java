package emlak;

public class Dugum<T> {
    T veri;
    Dugum<T> sonraki;

    public Dugum(T veri) {
        this.veri = veri;
    }
}
