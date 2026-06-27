package emlak;

public class Emlak implements Comparable<Emlak> {
    int id;
    String baslik, konum, tur, ilanTipi, resimYolu;
    double fiyat;
    int metrekare, odaSayisi, goruntulenmeSayisi;

    public Emlak(int id, String baslik, String konum, String tur, String ilanTipi, double fiyat, int metrekare, int odaSayisi, String resimYolu) {
        this.id = id;
        this.baslik = baslik;
        this.konum = konum;
        this.tur = tur;
        this.ilanTipi = ilanTipi;
        this.fiyat = fiyat;
        this.metrekare = metrekare;
        this.odaSayisi = odaSayisi;
        this.resimYolu = resimYolu == null ? "" : resimYolu;
        this.goruntulenmeSayisi = 0;
    }

    // Eski constructor ile uyumluluk için bırakıldı..
    public Emlak(int id, String baslik, String konum, String tur, double fiyat, int metrekare, int odaSayisi) {
        this(id, baslik, konum, tur, "Satılık", fiyat, metrekare, odaSayisi, "");
    }

    @Override
    public int compareTo(Emlak diger) {
        // BST sıralaması ID'ye göre yapılır.
        return Integer.compare(this.id, diger.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emlak emlak = (Emlak) o;
        return id == emlak.id;
    }

    @Override
    public String toString() {
        return id + " - " + baslik + " | " + tur + " | " + ilanTipi + " | " + fiyat + " TL | " + metrekare + " m² | "
                + odaSayisi + " oda | " + konum + " | Görüntülenme: " + goruntulenmeSayisi
                + (resimYolu == null || resimYolu.isEmpty() ? "" : " | Resim: " + resimYolu);
    }
}
