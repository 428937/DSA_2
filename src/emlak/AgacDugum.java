package emlak;

public class AgacDugum<T extends Comparable<T>> {
    T veri;
    AgacDugum<T> sol, sag;

    public AgacDugum(T veri) {
        this.veri = veri;
    }
}
