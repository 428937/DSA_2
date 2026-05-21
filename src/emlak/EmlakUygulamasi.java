package emlak;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmlakUygulamasi extends JFrame {
    private final BagliListe<Emlak> emlakListesi = new BagliListe<>();
    private final BagliListe<Emlak> favoriListesi = new BagliListe<>();
    private final Yigin<Emlak> silinenlerYigini = new Yigin<>();
    private final Kuyruk<ZiyaretTalebi> talepKuyrugu = new Kuyruk<>();
    private final IkiliAramaAgaci<Emlak> fiyatAgaci = new IkiliAramaAgaci<>();

    private DefaultTableModel tabloModeli;
    private JTable tablo;
    private JTextField txtBaslik, txtKonum, txtFiyat, txtMetrekare, txtOda, txtArama, txtResimYolu;
    private JComboBox<String> cmbTur, cmbIlanTipi;
    private int sonId = 0;

    private JLabel lblToplamEmlak, lblFavoriler, lblToplamGoruntulenme, lblOrtalamaFiyat, lblEnPahali, lblEnUcuz;

    private final String[] kolonlar = {"ID", "Başlık", "Tür", "İlan", "Fiyat", "m²", "Oda", "Konum", "Görüntülenme", "Resim"};


    private final Color ANA_RENK = new Color(21, 39, 70);
    private final Color ACIK_MAVI = new Color(43, 108, 176);
    private final Color ARKA_PLAN = new Color(240, 244, 248);
    private final Color KART_ARKA_PLAN = new Color(255, 255, 255);
    private final Color YAZI_RENK = new Color(45, 55, 72);

    private final boolean yoneticiMi;
    private final String aktifKullanici;
    private static final String VERI_DOSYASI = "emlak_verileri.txt";
    private static final String KULLANICI_DOSYASI = "kullanicilar.txt";
    private static final String TALEP_DOSYASI = "ziyaret_talepleri.txt";
    private static final String ONAYLI_ZIYARET_DOSYASI = "onaylanan_ziyaretler.txt";
    private static final String FAVORI_DOSYASI = "favoriler.txt";

    public EmlakUygulamasi(boolean yoneticiMi) {
        this(yoneticiMi, yoneticiMi ? "Yönetici" : "Kullanıcı");
    }

    public EmlakUygulamasi(boolean yoneticiMi, String aktifKullanici) {
        this.yoneticiMi = yoneticiMi;
        this.aktifKullanici = aktifKullanici == null || aktifKullanici.trim().isEmpty() ? (yoneticiMi ? "Yönetici" : "Kullanıcı") : aktifKullanici.trim();

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Emlak Listeleme Uygulaması - " + (yoneticiMi ? "Yönetici Paneli" : "Kullanıcı Paneli") + " - " + this.aktifKullanici);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                favorileriKaydet();
                verileriKaydet();
                System.exit(0);
            }
        });
        setSize(1300, 820);
        setLocationRelativeTo(null);

        JPanel anaPanel = new JPanel(new BorderLayout(15, 15));
        anaPanel.setBackground(ARKA_PLAN);
        anaPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(anaPanel);

        JPanel solMenu = solMenuOlustur();
        anaPanel.add(solMenu, BorderLayout.WEST);

        JPanel sagIcerik = new JPanel(new BorderLayout(15, 15));
        sagIcerik.setBackground(ARKA_PLAN);
        anaPanel.add(sagIcerik, BorderLayout.CENTER);

        JPanel ustIcerikGrup = new JPanel(new BorderLayout(15, 15));
        ustIcerikGrup.setBackground(ARKA_PLAN);

        JPanel istatistikPanel = istatistikPaneliOlustur();
        ustIcerikGrup.add(istatistikPanel, BorderLayout.NORTH);

        if (yoneticiMi) {
            JPanel formPanel = formPaneliOlustur();
            ustIcerikGrup.add(formPanel, BorderLayout.CENTER);
        } else {
            ustIcerikGrup.add(kullaniciBilgiPaneliOlustur(), BorderLayout.CENTER);
        }

        sagIcerik.add(ustIcerikGrup, BorderLayout.NORTH);

        JPanel merkezPanel = tabloPaneliOlustur();
        sagIcerik.add(merkezPanel, BorderLayout.CENTER);

        JPanel altIslemlerPanel = islemlerPaneliOlustur();
        sagIcerik.add(altIslemlerPanel, BorderLayout.SOUTH);

        verileriYukle();
        favorileriYukle();
        tabloyuGuncelle();
        istatistikleriGuncelle();

        setVisible(true);
    }

    private JPanel solMenuOlustur() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(ANA_RENK);
        menu.setPreferredSize(new Dimension(240, 0));
        // Sol boşluğu azalttım: butonlar ve başlık daha sola yakın durur.
        menu.setBorder(new EmptyBorder(25, 6, 20, 6));

        JLabel lblLogo = new JLabel("■", SwingConstants.LEFT);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblLogo.setForeground(new Color(100, 180, 255));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(0, 22, 0, 0));
        menu.add(lblLogo);

        JLabel lblBaslikUyg = new JLabel(yoneticiMi ? "Yönetici Paneli" : "Kullanıcı Paneli", SwingConstants.LEFT);
        lblBaslikUyg.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBaslikUyg.setForeground(Color.WHITE);
        lblBaslikUyg.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBaslikUyg.setMaximumSize(new Dimension(228, 32));
        lblBaslikUyg.setBorder(new EmptyBorder(0, 18, 0, 0));
        menu.add(lblBaslikUyg);
        menu.add(Box.createVerticalStrut(28));

        // Uzun yazıları kısaltmadan gösterdim ve hizalamayı sola aldım.
        String[] menuElemanlari = {
                "►  Emlaklar", "♥  Favoriler", "▲  Filtrele",
                "▼  En Çok Görüntülenen", "○  Konuma Göre Arama", "♦  Ayarlar", "←  Çıkış"
        };

        for (int i = 0; i < menuElemanlari.length; i++) {
            JButton btn = new JButton(menuElemanlari[i]);
            btn.setMaximumSize(new Dimension(228, 45));
            btn.setPreferredSize(new Dimension(228, 45));
            btn.setMinimumSize(new Dimension(228, 45));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            // İç boşluğu küçülttüm: yazı ve ikon daha sola yaklaştı.
            btn.setBorder(new EmptyBorder(0, 8, 0, 6));
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setFocusPainted(false);
            btn.setRolloverEnabled(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setToolTipText(null);

            final boolean cikisButonu = menuElemanlari[i].contains("Çıkış");
            final boolean aktifButon = i == 0;
            final Color normalRenk = aktifButon ? ACIK_MAVI : ANA_RENK;
            final Color hoverRenk = cikisButonu ? new Color(185, 28, 28) : new Color(35, 61, 98);

            btn.setBackground(normalRenk);
            btn.setForeground(cikisButonu ? new Color(255, 75, 75) : Color.WHITE);

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverRenk);
                    if (cikisButonu) btn.setForeground(Color.WHITE);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(normalRenk);
                    btn.setForeground(cikisButonu ? new Color(255, 75, 75) : Color.WHITE);
                }
            });

            btn.addActionListener(e -> yanMenuTiklandi(btn.getText()));

            menu.add(btn);
            menu.add(Box.createVerticalStrut(8));
        }

        return menu;
    }

    private JPanel istatistikPaneliOlustur() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 15, 0));
        panel.setBackground(ARKA_PLAN);

        lblToplamEmlak = new JLabel("0", SwingConstants.RIGHT);
        lblFavoriler = new JLabel("0", SwingConstants.RIGHT);
        lblToplamGoruntulenme = new JLabel("0", SwingConstants.RIGHT);
        lblOrtalamaFiyat = new JLabel("0", SwingConstants.RIGHT);
        lblEnPahali = new JLabel("0", SwingConstants.RIGHT);
        lblEnUcuz = new JLabel("0", SwingConstants.RIGHT);

        panel.add(kartOlustur("Toplam Emlak", lblToplamEmlak, "■", new Color(225, 239, 254)));
        panel.add(kartOlustur("Favoriler", lblFavoriler, "♥", new Color(254, 226, 226)));
        panel.add(kartOlustur("Görüntülenme", lblToplamGoruntulenme, "♦", new Color(222, 247, 236)));
        panel.add(kartOlustur("Ortalama Fiyat", lblOrtalamaFiyat, "₺", new Color(254, 243, 199)));
        panel.add(kartOlustur("En Pahalı", lblEnPahali, "▲", new Color(237, 233, 254)));
        panel.add(kartOlustur("En Ucuz", lblEnUcuz, "▼", new Color(224, 242, 254)));

        return panel;
    }

    private JPanel kartOlustur(String baslik, JLabel veriEtiketi, String emoji, Color iconBg) {
        JPanel kart = new JPanel(new BorderLayout(15, 10));
        kart.setBackground(KART_ARKA_PLAN);
        kart.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblEmoji = new JLabel(emoji, SwingConstants.CENTER);
        lblEmoji.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblEmoji.setOpaque(true);
        lblEmoji.setBackground(iconBg);
        lblEmoji.setForeground(ACIK_MAVI);
        lblEmoji.setPreferredSize(new Dimension(50, 50));

        JPanel metinPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        metinPanel.setBackground(KART_ARKA_PLAN);

        JLabel lblBaslik = new JLabel(baslik);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBaslik.setForeground(new Color(113, 128, 150));

        veriEtiketi.setFont(new Font("Segoe UI", Font.BOLD, 22));
        veriEtiketi.setForeground(YAZI_RENK);
        veriEtiketi.setHorizontalAlignment(SwingConstants.LEFT);

        metinPanel.add(lblBaslik);
        metinPanel.add(veriEtiketi);

        kart.add(lblEmoji, BorderLayout.WEST);
        kart.add(metinPanel, BorderLayout.CENTER);

        return kart;
    }

    private JPanel formPaneliOlustur() {
        JPanel anaFormPanel = new JPanel(new BorderLayout(10, 10));
        anaFormPanel.setBackground(KART_ARKA_PLAN);
        anaFormPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblFormBaslik = new JLabel("Emlak Bilgileri Sistemi");
        lblFormBaslik.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormBaslik.setForeground(ANA_RENK);
        anaFormPanel.add(lblFormBaslik, BorderLayout.NORTH);

        JPanel alanlarPaneli = new JPanel(new GridLayout(2, 8, 10, 8));
        alanlarPaneli.setBackground(KART_ARKA_PLAN);

        txtBaslik = new JTextField();
        txtKonum = new JTextField();
        txtFiyat = new JTextField();
        txtMetrekare = new JTextField();
        txtOda = new JTextField();
        txtArama = new JTextField();
        txtResimYolu = new JTextField();
        txtResimYolu.setEditable(false);
        cmbTur = new JComboBox<>(new String[]{"Daire", "Villa", "Ofis", "Arsa", "Dükkan"});
        cmbIlanTipi = new JComboBox<>(new String[]{"Satılık", "Kiralık"});

        Font alanFontu = new Font("Segoe UI", Font.PLAIN, 13);
        txtBaslik.setFont(alanFontu); txtKonum.setFont(alanFontu); txtFiyat.setFont(alanFontu);
        txtMetrekare.setFont(alanFontu); txtOda.setFont(alanFontu); txtArama.setFont(alanFontu); txtResimYolu.setFont(alanFontu);
        cmbTur.setFont(alanFontu); cmbIlanTipi.setFont(alanFontu);

        String[] etiketler = {"Başlık", "Konum", "Tür", "İlan Tipi", "Fiyat (₺)", "m²", "Oda", "Arama (ID)"};
        for (String etiket : etiketler) {
            JLabel lbl = new JLabel(etiket);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(74, 85, 104));
            alanlarPaneli.add(lbl);
        }

        alanlarPaneli.add(txtBaslik);
        alanlarPaneli.add(txtKonum);
        alanlarPaneli.add(cmbTur);
        alanlarPaneli.add(cmbIlanTipi);
        alanlarPaneli.add(txtFiyat);
        alanlarPaneli.add(txtMetrekare);
        alanlarPaneli.add(txtOda);
        alanlarPaneli.add(txtArama);

        JPanel formIciGrup = new JPanel(new BorderLayout(15, 0));
        formIciGrup.setBackground(KART_ARKA_PLAN);
        formIciGrup.add(alanlarPaneli, BorderLayout.CENTER);

        JPanel sagHizliButonlar = new JPanel(new GridLayout(3, 1, 0, 8));
        sagHizliButonlar.setBackground(KART_ARKA_PLAN);

        JButton btnHizliEkle = new JButton("+ Ekle");
        btnHizliEkle.setBackground(ACIK_MAVI);
        btnHizliEkle.setForeground(Color.WHITE);
        btnHizliEkle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHizliEkle.setBorder(new LineBorder(ACIK_MAVI, 1, true));
        btnHizliEkle.setActionCommand("Ekle");
        btnHizliEkle.addActionListener(this::butonTiklandi);

        JButton btnHizliTemizle = new JButton("○ Temizle");
        btnHizliTemizle.setBackground(Color.WHITE);
        btnHizliTemizle.setForeground(YAZI_RENK);
        btnHizliTemizle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHizliTemizle.setBorder(new LineBorder(new Color(200, 210, 220), 1, true));
        btnHizliTemizle.setActionCommand("Temizle");
        btnHizliTemizle.addActionListener(this::butonTiklandi);

        JButton btnResimSec = new JButton("📷 Resim Seç");
        btnResimSec.setBackground(Color.WHITE);
        btnResimSec.setForeground(YAZI_RENK);
        btnResimSec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnResimSec.setBorder(new LineBorder(new Color(200, 210, 220), 1, true));
        btnResimSec.setActionCommand("Resim Seç");
        btnResimSec.addActionListener(this::butonTiklandi);

        sagHizliButonlar.add(btnHizliEkle);
        sagHizliButonlar.add(btnResimSec);
        sagHizliButonlar.add(btnHizliTemizle);
        formIciGrup.add(sagHizliButonlar, BorderLayout.EAST);

        anaFormPanel.add(formIciGrup, BorderLayout.CENTER);
        return anaFormPanel;
    }

    private JPanel kullaniciBilgiPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(KART_ARKA_PLAN);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel baslik = new JLabel("Kullanıcı Paneli - Hoş geldin " + aktifKullanici);
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 18));
        baslik.setForeground(ANA_RENK);

        JLabel aciklama = new JLabel("Bu panelde emlakları görüntüleyebilir, filtreleyebilir, favorilere ekleyebilir ve ziyaret talebi oluşturabilirsiniz.");
        aciklama.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aciklama.setForeground(new Color(74, 85, 104));

        panel.add(baslik, BorderLayout.NORTH);
        panel.add(aciklama, BorderLayout.CENTER);
        return panel;
    }

    private JPanel tabloPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(KART_ARKA_PLAN);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(10, 15, 15, 15)
        ));

        JLabel lblTabloBaslik = new JLabel("Güncel Emlak Listesi");
        lblTabloBaslik.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTabloBaslik.setForeground(YAZI_RENK);
        panel.add(lblTabloBaslik, BorderLayout.NORTH);

        tabloModeli = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablo = new JTable(tabloModeli);
        tablo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablo.setRowHeight(35);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablo.setGridColor(new Color(230, 235, 245));

        JTableHeader header = tablo.getTableHeader();
        header.setBackground(ANA_RENK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablo.getColumnCount(); i++) {
            if(i != 1 && i != 6) {
                tablo.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        tablo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (yoneticiMi) seciliEmlagiFormaYukle();
                if (e.getClickCount() == 2) detayGoster();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel islemlerPaneliOlustur() {
        JPanel anaIslemlerPanel = new JPanel(new BorderLayout(5, 5));
        anaIslemlerPanel.setBackground(KART_ARKA_PLAN);
        anaIslemlerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        anaIslemlerPanel.setPreferredSize(new Dimension(0, yoneticiMi ? 235 : 125));

        JLabel lblIslemBaslik = new JLabel("Veri Yapıları ve Algoritma İşlemleri");
        lblIslemBaslik.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIslemBaslik.setForeground(ANA_RENK);
        anaIslemlerPanel.add(lblIslemBaslik, BorderLayout.NORTH);

        JPanel butonIzgara = new JPanel(yoneticiMi ? new GridLayout(5, 3, 8, 6) : new GridLayout(2, 4, 8, 6));
        butonIzgara.setBackground(KART_ARKA_PLAN);

        String[] butonlar = yoneticiMi ? new String[]{
                "-  Sil", "* Güncelle", "<  Geri Al (Yığın)",
                "i  Detay Göster", "♥  Favorilere Ekle", "♡  Favoriden Kaldır",
                "📋  Onaylanan Ziyaret Listesi", "►  Talebi İşle", "▲  Doğrusal Arama",
                "▼  İkili Arama (ID)", "↕  Fiyata Göre Sırala", "♫  m² Göre Sırala",
                "☼  Oda Sayısına Göre", "♣  BST Sıralı Göster", "📊  İstatistik Raporu"
        } : new String[]{
                "i  Detay Göster", "♥  Favorilere Ekle", "♡  Favoriden Kaldır", "📩  Ziyaret Talebi Gönder",
                "▲  Doğrusal Arama", "↕  Fiyata Göre Sırala", "♫  m² Göre Sırala", "☼  Oda Sayısına Göre"
        };

        for (String isim : butonlar) {
            JButton btn = new JButton(isim);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(Color.WHITE);
            btn.setForeground(YAZI_RENK);
            btn.setBorder(new LineBorder(new Color(218, 226, 236), 1, true));
            btn.setPreferredSize(new Dimension(0, 30));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            String komut = komutOlustur(isim);
            btn.setActionCommand(komut);
            btn.addActionListener(this::butonTiklandi);

            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setRolloverEnabled(false);
            btn.setToolTipText(null);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(240, 245, 255));
                    btn.setBorder(new LineBorder(ACIK_MAVI, 1, true));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(new LineBorder(new Color(218, 226, 236), 1, true));
                }
            });

            butonIzgara.add(btn);
        }

        anaIslemlerPanel.add(butonIzgara, BorderLayout.CENTER);
        return anaIslemlerPanel;
    }

    private String komutOlustur(String isim) {
        if (isim.contains("Sil")) return "Sil";
        if (isim.contains("Güncelle")) return "Güncelle";
        if (isim.contains("Geri Al")) return "Geri Al";
        if (isim.contains("Detay")) return "Detay Göster";
        if (isim.contains("Favorilere Ekle")) return "Favorilere Ekle";
        if (isim.contains("Favoriden Kaldır")) return "Favoriden Kaldır";
        if (isim.contains("Ziyaret Talebi") || isim.contains("Onaylanan Ziyaret")) return "Ziyaret Talebi";
        if (isim.contains("Talebi İşle")) return "Talebi İşle";
        if (isim.contains("Doğrusal Arama")) return "Doğrusal Arama";
        if (isim.contains("İkili Arama")) return "İkili Arama";
        if (isim.contains("Fiyata Göre")) return "Fiyata Göre Sırala";
        if (isim.contains("m²")) return "m² Göre Sırala";
        if (isim.contains("Oda Sayısına")) return "Oda Sayısına Göre";
        if (isim.contains("BST")) return "BST Sıralı Göster";
        if (isim.contains("İstatistik")) return "İstatistik Raporu";
        return isim;
    }

    private void yanMenuTiklandi(String menuMetni) {
        try {
            if (menuMetni.contains("Emlaklar")) {
                tabloyuGuncelle();
            } else if (menuMetni.contains("Favoriler")) {
                favorileriAnaTablodaGoster();
            } else if (menuMetni.contains("Filtrele")) {
                filtrele();
            } else if (menuMetni.contains("En Çok")) {
                enCokGoruntulenenleriGoster();
            } else if (menuMetni.contains("Konuma")) {
                konumaGoreGrupla();
            } else if (menuMetni.contains("Çıkış")) {
                cikisYap();
            } else if (menuMetni.contains("Ayarlar")) {
                ayarlarPenceresiGoster();
            }
            istatistikleriGuncelle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void butonTiklandi(ActionEvent e) {
        String komut = e.getActionCommand().trim();

        try {
            if (komut.contains("Favorilere Ekle")) {
                favorilereEkle();
            } else if (komut.contains("Favoriden Kaldır")) {
                favoridenKaldir();
            } else if (komut.contains("Favorileri Göster")) {
                favorileriGoster();
            } else if (komut.contains("Resim Seç")) {
                resimSec();
            } else if (komut.contains("Ekle")) {
                emlakEkle();
            } else if (komut.contains("Sil")) {
                emlakSil();
            } else if (komut.contains("Güncelle")) {
                emlakGuncelle();
            } else if (komut.contains("Geri Al")) {
                geriAl();
            } else if (komut.contains("Detay")) {
                detayGoster();
            } else if (komut.contains("Filtrele")) {
                filtrele();
            } else if (komut.contains("En Çok Görüntülenen")) {
                enCokGoruntulenenleriGoster();
            } else if (komut.contains("Konuma Göre Grupla")) {
                konumaGoreGrupla();
            } else if (komut.contains("İstatistik Raporu")) {
                istatistikRaporuGoster();
            } else if (komut.contains("Ziyaret Talebi")) {
                talebeEkle();
            } else if (komut.contains("Talebi İşle")) {
                talebiIsle();
            } else if (komut.contains("Doğrusal Arama")) {
                dogrusalArama();
            } else if (komut.contains("İkili Arama")) {
                ikiliArama();
            } else if (komut.contains("Fiyata Göre Sırala")) {
                sirala("fiyat");
            } else if (komut.contains("m²")) {
                sirala("metrekare");
            } else if (komut.contains("Oda Sayısına Göre")) {
                sirala("oda");
            } else if (komut.contains("BST")) {
                bstSiraliGoster();
            } else if (komut.contains("Listeyi Yenile")) {
                tabloyuGuncelle();
            } else if (komut.contains("Temizle")) {
                temizleForm();
            }

            istatistikleriGuncelle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void yetkiUyarisi() {
        JOptionPane.showMessageDialog(this, "Bu işlem sadece yönetici panelinde yapılabilir.", "Yetki Uyarısı", JOptionPane.WARNING_MESSAGE);
    }



    private void ayarlarPenceresiGoster() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JButton btnTema = new JButton("1) Tema Değiştir (Açık / Koyu)");
        JButton btnHesap = new JButton("3) Hesap Bilgileri / Şifre Değiştir");
        JButton btnFavoriTemizle = new JButton("4) Favorileri Temizle");
        JButton btnVeriSifirla = new JButton("5) Verileri Sıfırla (Yönetici)");
        JButton btnDil = new JButton("6) Dil Seçimi");

        JButton[] ayarButonlari = {btnTema, btnHesap, btnFavoriTemizle, btnVeriSifirla, btnDil};
        for (JButton b : ayarButonlari) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(b);
        }

        JDialog dialog = new JDialog(this, "Ayarlar", true);
        dialog.setContentPane(panel);
        dialog.setSize(430, 330);
        dialog.setLocationRelativeTo(this);

        btnTema.addActionListener(e -> {
            temaDegistir();
            dialog.dispose();
        });
        btnHesap.addActionListener(e -> hesapBilgileriGoster());
        btnFavoriTemizle.addActionListener(e -> favorileriTemizle());
        btnVeriSifirla.addActionListener(e -> verileriSifirla());
        btnDil.addActionListener(e -> dilSecimiGoster());

        dialog.setVisible(true);
    }

    private void temaDegistir() {
        Object[] secenekler = {"Varsayılan Tema","Açık Tema", "Koyu Tema"};
        int secim = JOptionPane.showOptionDialog(this,
                "Hangi temayı kullanmak istiyorsunuz?",
                "Tema Değiştir",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                secenekler,
                secenekler[0]);

        if (secim == JOptionPane.CLOSED_OPTION) return;

        if (secim == 0) {
            favorileriKaydet();
            verileriKaydet();
            SwingUtilities.invokeLater(() -> {
                dispose();
                new EmlakUygulamasi(yoneticiMi, aktifKullanici);
            });
            return;
        }
        boolean koyuTema = secim == 2;
        Color arkaPlan = koyuTema ? new Color(30, 41, 59) : new Color(240, 244, 248);
        Color panelRenk = koyuTema ? new Color(15, 23, 42) : Color.WHITE;
        Color yaziRenk = koyuTema ? Color.WHITE : new Color(45, 55, 72);

        temaUygula(getContentPane(), arkaPlan, panelRenk, yaziRenk);
        tablo.setBackground(koyuTema ? new Color(17, 24, 39) : Color.WHITE);
        tablo.setForeground(yaziRenk);
        tablo.setGridColor(koyuTema ? new Color(71, 85, 105) : new Color(226, 232, 240));
        tablo.getTableHeader().setBackground(koyuTema ? new Color(51, 65, 85) : new Color(226, 232, 240));
        tablo.getTableHeader().setForeground(yaziRenk);

        JOptionPane.showMessageDialog(this,
                koyuTema ? "Koyu tema uygulandı." : "Açık tema uygulandı.",
                "Tema", JOptionPane.INFORMATION_MESSAGE);
    }

    private void temaUygula(Component component, Color arkaPlan, Color panelRenk, Color yaziRenk) {
        if (component instanceof JPanel) component.setBackground(arkaPlan);
        if (component instanceof JLabel) component.setForeground(yaziRenk);
        if (component instanceof JTextField) {
            component.setBackground(panelRenk);
            component.setForeground(yaziRenk);
        }
        if (component instanceof JComboBox) {
            component.setBackground(panelRenk);
            component.setForeground(yaziRenk);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                temaUygula(child, arkaPlan, panelRenk, yaziRenk);
            }
        }
    }

    private void hesapBilgileriGoster() {
        if (yoneticiMi) {
            JOptionPane.showMessageDialog(this,
                    "Aktif hesap: Yönetici\nYönetici şifresi sabit olarak admin123 şeklindedir.",
                    "Hesap Bilgileri", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPasswordField eskiSifre = new JPasswordField();
        JPasswordField yeniSifre = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Kullanıcı adı:"));
        panel.add(new JLabel(aktifKullanici));
        panel.add(new JLabel("Eski şifre:"));
        panel.add(eskiSifre);
        panel.add(new JLabel("Yeni şifre:"));
        panel.add(yeniSifre);

        int cevap = JOptionPane.showConfirmDialog(this, panel, "Şifre Değiştir", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (cevap != JOptionPane.OK_OPTION) return;

        String eski = new String(eskiSifre.getPassword()).trim();
        String yeni = new String(yeniSifre.getPassword()).trim();
        if (eski.isEmpty() || yeni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Şifre alanları boş bırakılamaz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!kullaniciDogrula(aktifKullanici, eski)) {
            JOptionPane.showMessageDialog(this, "Eski şifre hatalı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (kullaniciSifresiDegistir(aktifKullanici, yeni)) {
            JOptionPane.showMessageDialog(this, "Şifre güncellendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void favorileriTemizle() {
        if (favoriListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Favori listesi zaten boş.");
            return;
        }
        int cevap = JOptionPane.showConfirmDialog(this,
                "Tüm favoriler silinsin mi?",
                "Favorileri Temizle",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (cevap == JOptionPane.YES_OPTION) {
            favoriListesi.temizle();
            favorileriKaydet();
            istatistikleriGuncelle();
            tabloyuGuncelle();
            JOptionPane.showMessageDialog(this, "Favoriler temizlendi.");
        }
    }

    private void verileriSifirla() {
        if (!yoneticiMi) {
            JOptionPane.showMessageDialog(this, "Bu işlem sadece yönetici panelinde yapılabilir.", "Yetki Uyarısı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cevap = JOptionPane.showConfirmDialog(this,
                "Tüm emlak verileri, ziyaret talepleri ve onaylanan ziyaretler silinsin mi?",
                "Verileri Sıfırla",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (cevap != JOptionPane.YES_OPTION) return;

        emlakListesi.temizle();
        favoriListesi.temizle();
        fiyatAgaci.temizle();
        sonId = 0;
        verileriKaydet();
        new File(TALEP_DOSYASI).delete();
        new File(ONAYLI_ZIYARET_DOSYASI).delete();
        tabloyuGuncelle();
        istatistikleriGuncelle();
        temizleForm();
        JOptionPane.showMessageDialog(this, "Veriler sıfırlandı.");
    }

    private void dilSecimiGoster() {
        String[] diller = {"Türkçe", "English", "العربية"};
        String secim = (String) JOptionPane.showInputDialog(this,
                "Dil seçiniz:",
                "Dil Seçimi",
                JOptionPane.PLAIN_MESSAGE,
                null,
                diller,
                diller[0]);
        if (secim == null) return;
        JOptionPane.showMessageDialog(this,
                "Seçilen dil: " + secim + "\nNot: Bu ayar proje içinde demo amaçlıdır.",
                "Dil Seçimi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void seciliEmlagiFormaYukle() {
        if (!yoneticiMi || txtBaslik == null) return;
        Emlak secili = seciliEmlakGetir();
        if (secili == null) return;
        txtBaslik.setText(secili.baslik);
        txtKonum.setText(secili.konum);
        cmbTur.setSelectedItem(secili.tur);
        cmbIlanTipi.setSelectedItem(secili.ilanTipi);
        txtFiyat.setText(String.valueOf((long) secili.fiyat));
        txtMetrekare.setText(String.valueOf(secili.metrekare));
        txtOda.setText(String.valueOf(secili.odaSayisi));
        txtArama.setText(String.valueOf(secili.id));
        txtResimYolu.setText(secili.resimYolu == null ? "" : secili.resimYolu);
    }

    private void cikisYap() {
        favorileriKaydet();
        verileriKaydet();
        dispose();
        girisEkraniGoster();
    }

    private void verileriYukle() {
        File dosya = new File(VERI_DOSYASI);
        if (!dosya.exists()) {
            ornekVerileriYukle();
            verileriKaydet();
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.trim().isEmpty()) continue;
                String[] p = satir.split("\\|", -1);
                if (p.length < 10) continue;
                int id = Integer.parseInt(p[0]);
                String baslik = p[1];
                String konum = p[2];
                String tur = p[3];
                String ilanTipi = p[4];
                double fiyat = Double.parseDouble(p[5]);
                int metrekare = Integer.parseInt(p[6]);
                int oda = Integer.parseInt(p[7]);
                String resimYolu = p[8];
                int goruntulenme = Integer.parseInt(p[9]);
                Emlak e = new Emlak(id, baslik, konum, tur, ilanTipi, fiyat, metrekare, oda, resimYolu);
                e.goruntulenmeSayisi = goruntulenme;
                emlakListesi.sonaEkle(e);
                fiyatAgaci.ekle(e);
                if (id > sonId) sonId = id;
            }
            if (emlakListesi.boyut() == 0) ornekVerileriYukle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Kayıt dosyası okunamadı, örnek veriler yüklendi.\n" + ex.getMessage(), "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            emlakListesi.temizle();
            fiyatAgaci.temizle();
            sonId = 0;
            ornekVerileriYukle();
        }
    }

    private void verileriKaydet() {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(VERI_DOSYASI), StandardCharsets.UTF_8))) {
            Dugum<Emlak> gec = emlakListesi.basDugum();
            while (gec != null) {
                Emlak e = gec.veri;
                pw.println(e.id + "|" + temizKayit(e.baslik) + "|" + temizKayit(e.konum) + "|" + temizKayit(e.tur) + "|" +
                        temizKayit(e.ilanTipi) + "|" + e.fiyat + "|" + e.metrekare + "|" + e.odaSayisi + "|" +
                        temizKayit(e.resimYolu) + "|" + e.goruntulenmeSayisi);
                gec = gec.sonraki;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Veriler kaydedilemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String temizKayit(String deger) {
        if (deger == null) return "";
        return deger.replace("|", " ").replace("\n", " ").replace("\r", " ");
    }

    private void ornekVerileriYukle() {
        Emlak e1 = new Emlak(++sonId, "Merkezde 2+1 Daire", "İstanbul", "Daire", "Kiralık", 28000, 95, 2, "");
        Emlak e2 = new Emlak(++sonId, "Bahçeli Villa", "Bursa", "Villa", "Satılık", 8500000, 240, 5, "");
        Emlak e3 = new Emlak(++sonId, "Ofis Katı", "Ankara", "Ofis", "Kiralık", 45000, 160, 4, "");
        Emlak e4 = new Emlak(++sonId, "Denize Yakın Arsa", "İzmir", "Arsa", "Satılık", 3200000, 500, 0, "");

        emlakListesi.sonaEkle(e1); fiyatAgaci.ekle(e1);
        emlakListesi.sonaEkle(e2); fiyatAgaci.ekle(e2);
        emlakListesi.sonaEkle(e3); fiyatAgaci.ekle(e3);
        emlakListesi.sonaEkle(e4); fiyatAgaci.ekle(e4);
    }

    private void istatistikleriGuncelle() {
        lblToplamEmlak.setText(String.valueOf(emlakListesi.boyut()));
        lblFavoriler.setText(String.valueOf(favoriListesi.boyut()));

        int toplamIzlenme = 0;
        double toplamFiyat = 0;
        double enPahali = 0;
        double enUcuz = 0;
        int sayi = 0;

        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            Emlak e = gec.veri;
            toplamIzlenme += e.goruntulenmeSayisi;
            toplamFiyat += e.fiyat;
            if (sayi == 0 || e.fiyat > enPahali) enPahali = e.fiyat;
            if (sayi == 0 || e.fiyat < enUcuz) enUcuz = e.fiyat;
            sayi++;
            gec = gec.sonraki;
        }

        lblToplamGoruntulenme.setText(String.valueOf(toplamIzlenme));
        lblOrtalamaFiyat.setText(sayi == 0 ? "0" : String.format("%.0f", toplamFiyat / sayi));
        lblEnPahali.setText(sayi == 0 ? "0" : String.format("%.0f", enPahali));
        lblEnUcuz.setText(sayi == 0 ? "0" : String.format("%.0f", enUcuz));
    }

    private void emlakEkle() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }
        if (txtBaslik.getText().trim().isEmpty() || txtKonum.getText().trim().isEmpty() || txtFiyat.getText().trim().isEmpty()
                || txtMetrekare.getText().trim().isEmpty() || txtOda.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurun!");
            return;
        }

        try {
            String baslik = txtBaslik.getText().trim();
            String konum = txtKonum.getText().trim();
            String tur = cmbTur.getSelectedItem().toString();
            String ilanTipi = cmbIlanTipi.getSelectedItem().toString();
            String resimYolu = txtResimYolu.getText().trim();
            double fiyat = Double.parseDouble(txtFiyat.getText().trim());
            int m2 = Integer.parseInt(txtMetrekare.getText().trim());
            int oda = Integer.parseInt(txtOda.getText().trim());

            String idMetni = txtArama.getText().trim();
            if (idMetni.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Yeni emlak eklemek için Arama (ID) alanına sıradaki ID'yi girin.\nSıradaki ID: " + (sonId + 1));
                return;
            }

            int yeniId = Integer.parseInt(idMetni);
            if (idIleEmlakBul(yeniId) != null) {
                JOptionPane.showMessageDialog(this, "Bu ID zaten var! Aynı ID ile yeni emlak eklenemez.");
                return;
            }
            if (yeniId != sonId + 1) {
                JOptionPane.showMessageDialog(this, "ID sıralı olmalıdır. Son ID: " + sonId + "\nGirilmesi gereken ID: " + (sonId + 1));
                return;
            }

            Emlak yeni = new Emlak(yeniId, baslik, konum, tur, ilanTipi, fiyat, m2, oda, resimYolu);
            sonId = yeniId;
            emlakListesi.sonaEkle(yeni);
            fiyatAgaci.ekle(yeni);
            tabloyuGuncelle();
            temizleForm();
            verileriKaydet();
            JOptionPane.showMessageDialog(this, "Emlak başarıyla eklendi: " + baslik);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fiyat, m² ve Oda sayısı sayısal değer olmalıdır!");
        }
    }

    private void emlakSil() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }
        Emlak silinecek = seciliEmlakGetir();
        if (silinecek == null) {
            JOptionPane.showMessageDialog(this, "Silmek için bir emlak seçin.");
            return;
        }

        if (emlakListesi.veriSil(silinecek)) {
            favoriListesi.veriSil(silinecek);
            silinenlerYigini.ekle(silinecek);
            rebuildBST();
            tabloyuGuncelle();
            verileriKaydet();
            JOptionPane.showMessageDialog(this, "Emlak silindi ve yığına eklendi.");
        }
    }

    private void geriAl() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }
        if (silinenlerYigini.bosMu()) {
            JOptionPane.showMessageDialog(this, "Geri alınacak emlak yok.");
            return;
        }
        Emlak geri = silinenlerYigini.cikar();
        emlakListesi.sonaEkle(geri);
        fiyatAgaci.ekle(geri);
        tabloyuGuncelle();
        verileriKaydet();
        JOptionPane.showMessageDialog(this, "Emlak geri alındı: " + geri.baslik);
    }

    private void emlakGuncelle() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }
        Emlak guncellenecek = seciliEmlakGetir();
        if (guncellenecek == null) {
            JOptionPane.showMessageDialog(this, "Güncellemek için bir emlak seçin.");
            return;
        }

        if (txtBaslik.getText().trim().isEmpty() || txtKonum.getText().trim().isEmpty() || txtFiyat.getText().trim().isEmpty()
                || txtMetrekare.getText().trim().isEmpty() || txtOda.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Güncellemek için önce tablodan emlak seçin ve form alanlarını doldurun.");
            return;
        }

        try {
            guncellenecek.baslik = txtBaslik.getText().trim();
            guncellenecek.konum = txtKonum.getText().trim();
            guncellenecek.tur = cmbTur.getSelectedItem().toString();
            guncellenecek.ilanTipi = cmbIlanTipi.getSelectedItem().toString();
            guncellenecek.resimYolu = txtResimYolu.getText().trim();
            guncellenecek.fiyat = Double.parseDouble(txtFiyat.getText().trim());
            guncellenecek.metrekare = Integer.parseInt(txtMetrekare.getText().trim());
            guncellenecek.odaSayisi = Integer.parseInt(txtOda.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fiyat, m² ve Oda sayısı sayısal değer olmalıdır!");
            return;
        }

        rebuildBST();
        tabloyuGuncelle();
        verileriKaydet();
        JOptionPane.showMessageDialog(this, "Emlak güncellendi.");
    }

    private void detayGoster() {
        Emlak secili = seciliEmlakGetir();
        if (secili == null) {
            JOptionPane.showMessageDialog(this, "Detay görmek için bir emlak seçin.");
            return;
        }
        secili.goruntulenmeSayisi++;
        tabloyuGuncelle();
        verileriKaydet();

        JPanel detayPanel = new JPanel(new BorderLayout(10, 10));
        JTextArea bilgi = new JTextArea(secili.toString());
        bilgi.setEditable(false);
        bilgi.setLineWrap(true);
        bilgi.setWrapStyleWord(true);
        detayPanel.add(bilgi, BorderLayout.CENTER);

        if (secili.resimYolu != null && !secili.resimYolu.isEmpty()) {
            ImageIcon icon = new ImageIcon(secili.resimYolu);
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(260, 180, Image.SCALE_SMOOTH);
                detayPanel.add(new JLabel(new ImageIcon(img)), BorderLayout.NORTH);
            }
        }
        JOptionPane.showMessageDialog(this, detayPanel, "Emlak Detayı", JOptionPane.INFORMATION_MESSAGE);
    }

    
    private void favorileriKaydet() {
        try {
            // Başka kullanıcıların favorilerini silmemek için önce eski dosyayı okuyoruz.
            List<String> satirlar = new ArrayList<>();
            File f = new File(FAVORI_DOSYASI);
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = line.split(";", 2);
                        if (p.length == 2 && !p[0].equals(aktifKullanici)) {
                            satirlar.add(line);
                        }
                    }
                }
            }

            Dugum<Emlak> d = favoriListesi.basDugum();
            while (d != null) {
                satirlar.add(aktifKullanici + ";" + d.veri.id);
                d = d.sonraki;
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(FAVORI_DOSYASI, false))) {
                for (String satir : satirlar) {
                    pw.println(satir);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Favoriler kaydedilemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void favorileriYukle() {
        try {
            File f = new File(FAVORI_DOSYASI);
            if (!f.exists()) return;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while((line=br.readLine())!=null){
                String[] p=line.split(";");
                if(p.length==2 && p[0].equals(aktifKullanici)){
                    int id=Integer.parseInt(p[1]);
                    Emlak e=emlakBulById(id);
                    if(e!=null && !favorideVarMi(e)) favoriListesi.sonaEkle(e);
                }
            }
        } catch (Exception ignored) {}
    }

    private Emlak emlakBulById(int id){
        Dugum<Emlak> d = emlakListesi.basDugum();
        while(d!=null){ if(d.veri.id==id) return d.veri; d=d.sonraki; }
        return null;
    }

    private void favorilereEkle() {
        Emlak secili = seciliEmlakGetir();
        if (secili == null) {
            JOptionPane.showMessageDialog(this, "Favorilere eklemek için bir emlak seçin.");
            return;
        }
        if (favorideVarMi(secili)) {
            JOptionPane.showMessageDialog(this, "Bu emlak zaten favorilerde.");
            return;
        }
        favoriListesi.sonaEkle(secili);
        favorileriKaydet();
        JOptionPane.showMessageDialog(this, "Favorilere eklendi: " + secili.baslik);
    }

    private void favoridenKaldir() {
        Emlak secili = seciliEmlakGetir();
        if (secili == null) {
            JOptionPane.showMessageDialog(this, "Favoriden kaldırmak için bir emlak seçin.");
            return;
        }
        if (favoriListesi.veriSil(secili)) {
            favorileriKaydet();
            JOptionPane.showMessageDialog(this, "Favorilerden kaldırıldı: " + secili.baslik);
        } else {
            JOptionPane.showMessageDialog(this, "Bu emlak favorilerde değil.");
        }
    }

    private void favorileriGoster() {
        favorileriAnaTablodaGoster();
    }

    private void favorileriAnaTablodaGoster() {
        if (favoriListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Favori emlak yok.");
            tabloModeli.setRowCount(0);
            return;
        }
        tabloyuListeyleGuncelle(favoriListesi);
    }

    private void filtrele() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField minFiyat = new JTextField();
        JTextField maxFiyat = new JTextField();
        JTextField minOda = new JTextField();
        JTextField konum = new JTextField();
        JComboBox<String> tur = new JComboBox<>(new String[]{"Hepsi", "Daire", "Villa", "Ofis", "Arsa", "Dükkan"});
        JComboBox<String> ilanTipi = new JComboBox<>(new String[]{"Hepsi", "Satılık", "Kiralık"});

        panel.add(new JLabel("Minimum fiyat:")); panel.add(minFiyat);
        panel.add(new JLabel("Maksimum fiyat:")); panel.add(maxFiyat);
        panel.add(new JLabel("Minimum oda sayısı:")); panel.add(minOda);
        panel.add(new JLabel("Konum içerir:")); panel.add(konum);
        panel.add(new JLabel("Tür:")); panel.add(tur);
        panel.add(new JLabel("İlan Tipi:")); panel.add(ilanTipi);

        int cevap = JOptionPane.showConfirmDialog(this, panel, "Filtreleme", JOptionPane.OK_CANCEL_OPTION);
        if (cevap != JOptionPane.OK_OPTION) return;

        double min = minFiyat.getText().trim().isEmpty() ? 0 : Double.parseDouble(minFiyat.getText().trim());
        double max = maxFiyat.getText().trim().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxFiyat.getText().trim());
        int oda = minOda.getText().trim().isEmpty() ? 0 : Integer.parseInt(minOda.getText().trim());
        String konumAranan = konum.getText().trim().toLowerCase();
        String turSecimi = tur.getSelectedItem().toString();
        String ilanSecimi = ilanTipi.getSelectedItem().toString();

        DefaultTableModel sonuc = yeniModel();
        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            Emlak e = gec.veri;
            boolean uygun = e.fiyat >= min && e.fiyat <= max
                    && e.odaSayisi >= oda
                    && e.konum.toLowerCase().contains(konumAranan)
                    && (turSecimi.equals("Hepsi") || e.tur.equals(turSecimi))
                    && (ilanSecimi.equals("Hepsi") || e.ilanTipi.equals(ilanSecimi));
            if (uygun) satirEkle(sonuc, e);
            gec = gec.sonraki;
        }

        if (sonuc.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Filtreye uygun emlak bulunamadı.");
            tabloModeli.setRowCount(0);
        } else {
            tabloModeli.setRowCount(0);
            for (int i = 0; i < sonuc.getRowCount(); i++) {
                Object[] satir = new Object[sonuc.getColumnCount()];
                for (int j = 0; j < sonuc.getColumnCount(); j++) satir[j] = sonuc.getValueAt(i, j);
                tabloModeli.addRow(satir);
            }
        }
    }

    private void enCokGoruntulenenleriGoster() {
        if (emlakListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Listede emlak yok.");
            return;
        }
        Emlak[] dizi = listeyiDiziyeCevir();
        hizliSiralama(dizi, 0, dizi.length - 1, Comparator.comparingInt((Emlak e) -> e.goruntulenmeSayisi).reversed());

        tabloModeli.setRowCount(0);
        for (int i = 0; i < dizi.length && i < 3; i++) satirEkle(tabloModeli, dizi[i]);
    }

    private void konumaGoreGrupla() {
        if (emlakListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Listede emlak yok.");
            return;
        }

        Map<String, Integer> gruplar = new LinkedHashMap<>();
        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            String konumVal = gec.veri.konum.trim();
            gruplar.put(konumVal, gruplar.getOrDefault(konumVal, 0) + 1);
            gec = gec.sonraki;
        }

        StringBuilder sb = new StringBuilder("Konuma göre emlak sayıları:\n\n");
        for (Map.Entry<String, Integer> entry : gruplar.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" emlak\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Konuma Göre Gruplama", JOptionPane.INFORMATION_MESSAGE);
    }


    private void istatistikRaporuGoster() {
        if (emlakListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Listede emlak yok.");
            return;
        }

        int sayi = 0;
        double toplamFiyat = 0;
        int toplamOda = 0;
        Emlak enPahali = null;
        Emlak enUcuz = null;
        Map<String, Integer> tipler = new LinkedHashMap<>();
        Map<String, Integer> ilanlar = new LinkedHashMap<>();

        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            Emlak e = gec.veri;
            sayi++;
            toplamFiyat += e.fiyat;
            toplamOda += e.odaSayisi;
            if (enPahali == null || e.fiyat > enPahali.fiyat) enPahali = e;
            if (enUcuz == null || e.fiyat < enUcuz.fiyat) enUcuz = e;
            tipler.put(e.tur, tipler.getOrDefault(e.tur, 0) + 1);
            ilanlar.put(e.ilanTipi, ilanlar.getOrDefault(e.ilanTipi, 0) + 1);
            gec = gec.sonraki;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("İSTATİSTİK RAPORU\n\n");
        sb.append("Toplam emlak: ").append(sayi).append("\n");
        sb.append("Ortalama fiyat: ").append(String.format("%.2f", toplamFiyat / sayi)).append(" TL\n");
        sb.append("Ortalama oda sayısı: ").append(String.format("%.2f", (double) toplamOda / sayi)).append("\n");
        sb.append("En pahalı: ").append(enPahali.baslik).append(" - ").append(enPahali.fiyat).append(" TL\n");
        sb.append("En ucuz: ").append(enUcuz.baslik).append(" - ").append(enUcuz.fiyat).append(" TL\n\n");
        sb.append("Tür dağılımı:\n");
        for (Map.Entry<String, Integer> entry : tipler.entrySet()) sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        sb.append("\nİlan tipi dağılımı:\n");
        for (Map.Entry<String, Integer> entry : ilanlar.entrySet()) sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "İstatistik Raporu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resimSec() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Emlak resmi seç");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int sonuc = chooser.showOpenDialog(this);
        if (sonuc == JFileChooser.APPROVE_OPTION) {
            txtResimYolu.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void rebuildBST() {
        fiyatAgaci.temizle();
        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            fiyatAgaci.ekle(gec.veri);
            gec = gec.sonraki;
        }
    }

    private void talebeEkle() {
        // Kullanıcı paneli: seçilen emlak için ziyaret talebi gönderir.
        // Yönetici paneli: sadece onaylanan ziyaretleri gösterir.
        if (yoneticiMi) {
            onaylananZiyaretleriGoster();
            return;
        }

        Emlak secili = seciliEmlakGetir();

        // Kullanıcı satır seçmediyse ID sorulur. Böylece buton her zaman kullanılabilir.
        if (secili == null) {
            String idMetni = JOptionPane.showInputDialog(this,
                    "Ziyaret talebi göndermek istediğiniz emlak ID'sini girin:",
                    "Ziyaret Talebi", JOptionPane.QUESTION_MESSAGE);
            if (idMetni == null) return;
            idMetni = idMetni.trim();
            if (idMetni.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen emlak ID girin.");
                return;
            }
            try {
                secili = idIleEmlakBul(Integer.parseInt(idMetni));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Geçerli bir ID girin.");
                return;
            }
        }

        if (secili == null) {
            JOptionPane.showMessageDialog(this, "Bu ID'ye ait emlak bulunamadı.");
            return;
        }

        List<ZiyaretTalebi> talepler = talepleriDosyadanOku();
        for (ZiyaretTalebi t : talepler) {
            if (t.kullaniciAdi.equalsIgnoreCase(aktifKullanici) && t.emlakId == secili.id) {
                JOptionPane.showMessageDialog(this,
                        "Bu emlak için daha önce ziyaret talebi gönderdiniz.\n" +
                        "Kullanıcı: " + aktifKullanici + "\n" +
                        "Emlak: " + secili.baslik);
                return;
            }
        }

        ZiyaretTalebi yeniTalep = new ZiyaretTalebi(aktifKullanici, secili.id, secili.baslik, secili.konum);
        talepler.add(yeniTalep);              // Queue sonuna ekleme mantığı
        talepKuyrugu.ekle(yeniTalep);
        talepleriDosyayaYaz(talepler);

        JOptionPane.showMessageDialog(this,
                "Ziyaret talebi gönderildi.\n\n" +
                "Kullanıcı: " + aktifKullanici + "\n" +
                "Emlak ID: " + secili.id + "\n" +
                "Emlak: " + secili.baslik + "\n" +
                "Konum: " + secili.konum,
                "Ziyaret Talebi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void talebiIsle() {
        if (!yoneticiMi) { yetkiUyarisi(); return; }

        List<ZiyaretTalebi> talepler = talepleriDosyadanOku();
        if (talepler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "İşlenecek ziyaret talebi yok.");
            return;
        }

        // FIFO: ilk gelen talep listenin/kuyruğun başındadır.
        ZiyaretTalebi ilkTalep = talepler.get(0);
        Object[] secenekler = {"Onayla", "Reddet", "Vazgeç"};
        int cevap = JOptionPane.showOptionDialog(this,
                "Sıradaki ziyaret talebi (FIFO):\n\n" +
                        "Kullanıcı: " + ilkTalep.kullaniciAdi + "\n" +
                        "Emlak ID: " + ilkTalep.emlakId + "\n" +
                        "Emlak: " + ilkTalep.emlakBaslik + "\n" +
                        "Konum: " + ilkTalep.konum + "\n\n" +
                        "Bu talebi onaylamak mı reddetmek mi istiyorsunuz?",
                "Talebi İşle",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                secenekler,
                secenekler[0]);

        if (cevap == 0 || cevap == 1) {
            talepler.remove(0); // Queue başından çıkarma mantığı
            talepleriDosyayaYaz(talepler);

            // Sadece onaylanan talepler ziyaret listesine eklenir.
            // Reddedilen talepler listede görünmez.
            if (cevap == 0) {
                List<ZiyaretTalebi> onaylananlar = onaylananZiyaretleriDosyadanOku();
                onaylananlar.add(ilkTalep);
                onaylananZiyaretleriDosyayaYaz(onaylananlar);
            }

            JOptionPane.showMessageDialog(this,
                    (cevap == 0 ? "Talep onaylandı.\nOnaylanan ziyaret listesine eklendi." : "Talep reddedildi.\nZiyaret listesine eklenmedi.") +
                            "\n\nKullanıcı: " + ilkTalep.kullaniciAdi +
                            "\nEmlak: " + ilkTalep.emlakBaslik +
                            "\nKalan bekleyen talep sayısı: " + talepler.size(),
                    "Talep İşlendi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onaylananZiyaretleriGoster() {
        if (!yoneticiMi) {
            JOptionPane.showMessageDialog(this, "Onaylanan ziyaret listesi sadece yönetici panelinde görüntülenir.");
            return;
        }

        List<ZiyaretTalebi> onaylananlar = onaylananZiyaretleriDosyadanOku();
        if (onaylananlar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Onaylanan ziyaret listesi boş.\nReddedilen talepler bu listeye eklenmez.");
            return;
        }

        String[] kolonlar = {"Sıra", "Kullanıcı", "Emlak ID", "Emlak", "Konum"};
        DefaultTableModel model = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (int i = 0; i < onaylananlar.size(); i++) {
            ZiyaretTalebi t = onaylananlar.get(i);
            model.addRow(new Object[]{i + 1, t.kullaniciAdi, t.emlakId, t.emlakBaslik, t.konum});
        }

        JTable tablo = new JTable(model);
        tablo.setRowHeight(28);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel bilgi = new JLabel("Onaylanan Ziyaret Sayısı: " + onaylananlar.size() + "  |  Reddedilen talepler burada gösterilmez.");
        bilgi.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(bilgi, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablo), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(650, 260));

        JOptionPane.showMessageDialog(this, panel, "Onaylanan Ziyaret Listesi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ziyaretTalepleriniGoster() {
        if (!yoneticiMi) {
            JOptionPane.showMessageDialog(this, "Ziyaret talebi listesi sadece yönetici panelinde görüntülenir.");
            return;
        }

        List<ZiyaretTalebi> talepler = talepleriDosyadanOku();
        if (talepler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ziyaret talebi listesi boş.");
            return;
        }

        String[] talepKolonlari = {"Sıra", "Kullanıcı", "Emlak ID", "Emlak", "Konum"};
        DefaultTableModel model = new DefaultTableModel(talepKolonlari, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (int i = 0; i < talepler.size(); i++) {
            ZiyaretTalebi t = talepler.get(i);
            model.addRow(new Object[]{i + 1, t.kullaniciAdi, t.emlakId, t.emlakBaslik, t.konum});
        }

        JTable talepTablosu = new JTable(model);
        talepTablosu.setRowHeight(28);
        talepTablosu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        talepTablosu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel bilgi = new JLabel("Bekleyen Talep Sayısı: " + talepler.size() + "  |  Queue/FIFO: İlk gelen talep önce işlenir.");
        bilgi.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(bilgi, BorderLayout.NORTH);
        panel.add(new JScrollPane(talepTablosu), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(650, 260));

        JOptionPane.showMessageDialog(this, panel, "Ziyaret Talepleri", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<ZiyaretTalebi> talepleriDosyadanOku() {
        List<ZiyaretTalebi> talepler = new ArrayList<>();
        File dosya = new File(TALEP_DOSYASI);
        if (!dosya.exists()) return talepler;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.trim().isEmpty()) continue;
                String[] p = satir.split("\\|", -1);
                if (p.length < 3) continue;
                String konum = p.length >= 4 ? p[3] : "";
                talepler.add(new ZiyaretTalebi(p[0], Integer.parseInt(p[1]), p[2], konum));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ziyaret talepleri okunamadı: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
        return talepler;
    }

    private void talepleriDosyayaYaz(List<ZiyaretTalebi> talepler) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(TALEP_DOSYASI), StandardCharsets.UTF_8))) {
            for (ZiyaretTalebi t : talepler) {
                pw.println(temizKayit(t.kullaniciAdi) + "|" + t.emlakId + "|" + temizKayit(t.emlakBaslik) + "|" + temizKayit(t.konum));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ziyaret talepleri kaydedilemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ZiyaretTalebi> onaylananZiyaretleriDosyadanOku() {
        List<ZiyaretTalebi> onaylananlar = new ArrayList<>();
        File dosya = new File(ONAYLI_ZIYARET_DOSYASI);
        if (!dosya.exists()) return onaylananlar;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                if (satir.trim().isEmpty()) continue;
                String[] p = satir.split("\\|", -1);
                if (p.length < 3) continue;
                String konum = p.length >= 4 ? p[3] : "";
                onaylananlar.add(new ZiyaretTalebi(p[0], Integer.parseInt(p[1]), p[2], konum));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Onaylanan ziyaretler okunamadı: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
        return onaylananlar;
    }

    private void onaylananZiyaretleriDosyayaYaz(List<ZiyaretTalebi> onaylananlar) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ONAYLI_ZIYARET_DOSYASI), StandardCharsets.UTF_8))) {
            for (ZiyaretTalebi t : onaylananlar) {
                pw.println(temizKayit(t.kullaniciAdi) + "|" + t.emlakId + "|" + temizKayit(t.emlakBaslik) + "|" + temizKayit(t.konum));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Onaylanan ziyaretler kaydedilemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dogrusalArama() {
        String idStr;
        if (txtArama == null) {
            idStr = JOptionPane.showInputDialog(this, "Aranacak emlak ID'sini girin:", "Doğrusal Arama", JOptionPane.QUESTION_MESSAGE);
            if (idStr == null) return;
            idStr = idStr.trim();
        } else {
            idStr = txtArama.getText().trim();
        }
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aranacak ID'yi girin.");
            return;
        }

        try {
            int arananId = Integer.parseInt(idStr);
            Emlak bulunan = null;
            Dugum<Emlak> gec = emlakListesi.basDugum();
            while (gec != null) {
                if (gec.veri.id == arananId) {
                    bulunan = gec.veri;
                    break;
                }
                gec = gec.sonraki;
            }

            if (bulunan == null) {
                JOptionPane.showMessageDialog(this, "ID bulunamadı.");
                return;
            }

            tabloModeli.setRowCount(0);
            satirEkle(tabloModeli, bulunan);
            JOptionPane.showMessageDialog(this, "Doğrusal arama ile bulundu:\n" + bulunan);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçerli bir ID girin.");
        }
    }

    private void ikiliArama() {
        String idStr;
        if (txtArama == null) {
            idStr = JOptionPane.showInputDialog(this, "Aranacak emlak ID'sini girin:", "İkili Arama", JOptionPane.QUESTION_MESSAGE);
            if (idStr == null) return;
            idStr = idStr.trim();
        } else {
            idStr = txtArama.getText().trim();
        }
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aranacak ID'yi girin.");
            return;
        }

        try {
            int arananId = Integer.parseInt(idStr);
            Emlak[] dizi = listeyiDiziyeCevir();
            if (dizi.length == 0) {
                JOptionPane.showMessageDialog(this, "Listede emlak yok.");
                return;
            }

            hizliSiralama(dizi, 0, dizi.length - 1, Comparator.comparingInt(e -> e.id));
            int sonuc = binarySearch(dizi, arananId);

            if (sonuc == -1) {
                JOptionPane.showMessageDialog(this, "ID bulunamadı.");
            } else {
                tabloModeli.setRowCount(0);
                satirEkle(tabloModeli, dizi[sonuc]);
                JOptionPane.showMessageDialog(this, "İkili arama ile bulundu:\n" + dizi[sonuc].toString());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçerli bir ID girin.");
        }
    }

    private void hizliSiralama(Emlak[] dizi, int sol, int sag, Comparator<Emlak> karsilastirici) {
        if (sol < sag) {
            int pivotIdx = parcala(dizi, sol, sag, karsilastirici);
            hizliSiralama(dizi, sol, pivotIdx - 1, karsilastirici);
            hizliSiralama(dizi, pivotIdx + 1, sag, karsilastirici);
        }
    }

    private int parcala(Emlak[] dizi, int sol, int sag, Comparator<Emlak> karsilastirici) {
        Emlak pivot = dizi[sag];
        int i = sol - 1;
        for (int j = sol; j < sag; j++) {
            if (karsilastirici.compare(dizi[j], pivot) <= 0) {
                i++;
                Emlak gecici = dizi[i];
                dizi[i] = dizi[j];
                dizi[j] = gecici;
            }
        }
        Emlak gecici = dizi[i + 1];
        dizi[i + 1] = dizi[sag];
        dizi[sag] = gecici;
        return i + 1;
    }

    private int binarySearch(Emlak[] dizi, int arananId) {
        int sol = 0, sag = dizi.length - 1;
        while (sol <= sag) {
            int orta = sol + (sag - sol) / 2;
            if (dizi[orta].id == arananId) return orta;
            else if (dizi[orta].id < arananId) sol = orta + 1;
            else sag = orta - 1;
        }
        return -1;
    }

    private void sirala(String kriter) {
        if (emlakListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Sıralanacak emlak yok!");
            return;
        }

        Emlak[] dizi = listeyiDiziyeCevir();
        Comparator<Emlak> karsilastirici = switch (kriter) {
            case "fiyat" -> Comparator.comparingDouble(e -> e.fiyat);
            case "metrekare" -> Comparator.comparingInt(e -> e.metrekare);
            case "oda" -> Comparator.comparingInt(e -> e.odaSayisi);
            default -> Comparator.comparingInt(e -> e.id);
        };

        hizliSiralama(dizi, 0, dizi.length - 1, karsilastirici);
        emlakListesi.dizidenYukle(dizi);
        rebuildBST();
        tabloyuGuncelle();
        verileriKaydet();
    }

    private void bstSiraliGoster() {
        if (emlakListesi.boyut() == 0) {
            JOptionPane.showMessageDialog(this, "Listede emlak yok.");
            return;
        }

        rebuildBST();
        tabloModeli.setRowCount(0);
        fiyatAgaci.siraliGezinti(emlak -> satirEkle(tabloModeli, emlak));
        JOptionPane.showMessageDialog(this, "BST sıralı gösterildi. Sıralama ID'ye göre yapılır.");
    }

    private Emlak seciliEmlakGetir() {
        int seciliSatir = tablo.getSelectedRow();
        if (seciliSatir == -1) return null;
        int id = (int) tabloModeli.getValueAt(seciliSatir, 0);
        return idIleEmlakBul(id);
    }

    private Emlak idIleEmlakBul(int id) {
        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            if (gec.veri.id == id) return gec.veri;
            gec = gec.sonraki;
        }
        return null;
    }

    private boolean favorideVarMi(Emlak emlak) {
        Dugum<Emlak> gec = favoriListesi.basDugum();
        while (gec != null) {
            if (gec.veri.equals(emlak)) return true;
            gec = gec.sonraki;
        }
        return false;
    }

    private Emlak[] listeyiDiziyeCevir() {
        Emlak[] dizi = new Emlak[emlakListesi.boyut()];
        Dugum<Emlak> gec = emlakListesi.basDugum();
        int i = 0;
        while (gec != null) {
            dizi[i++] = gec.veri;
            gec = gec.sonraki;
        }
        return dizi;
    }

    private DefaultTableModel yeniModel() {
        return new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void satirEkle(DefaultTableModel model, Emlak e) {
        model.addRow(new Object[]{e.id, e.baslik, e.tur, e.ilanTipi, e.fiyat, e.metrekare, e.odaSayisi, e.konum, e.goruntulenmeSayisi, (e.resimYolu == null || e.resimYolu.isEmpty()) ? "Yok" : "Var"});
    }

    private void tabloyuListeyleGuncelle(BagliListe<Emlak> liste) {
        tabloModeli.setRowCount(0);
        Dugum<Emlak> gec = liste.basDugum();
        while (gec != null) {
            satirEkle(tabloModeli, gec.veri);
            gec = gec.sonraki;
        }
    }

    private void tabloGoster(BagliListe<Emlak> liste, String baslik) {
        DefaultTableModel model = yeniModel();
        Dugum<Emlak> gec = liste.basDugum();
        while (gec != null) {
            satirEkle(model, gec.veri);
            gec = gec.sonraki;
        }
        JTable sonucTablosu = new JTable(model);
        sonucTablosu.setRowHeight(28);
        JOptionPane.showMessageDialog(this, new JScrollPane(sonucTablosu), baslik, JOptionPane.PLAIN_MESSAGE);
    }

    private void tabloyuGuncelle() {
        tabloModeli.setRowCount(0);
        Dugum<Emlak> gec = emlakListesi.basDugum();
        while (gec != null) {
            satirEkle(tabloModeli, gec.veri);
            gec = gec.sonraki;
        }
    }

    private void temizleForm() {
        if (txtBaslik == null) return;
        txtBaslik.setText("");
        txtKonum.setText("");
        txtFiyat.setText("");
        txtMetrekare.setText("");
        txtOda.setText("");
        txtArama.setText("");
        txtResimYolu.setText("");
        cmbTur.setSelectedIndex(0);
        cmbIlanTipi.setSelectedIndex(0);
    }

    private static void girisEkraniGoster() {
        JFrame giris = new JFrame("Emlak Uygulaması - Giriş");
        giris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        giris.setSize(430, 300);
        giris.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel baslik = new JLabel("Emlak Yönetim Sistemi", SwingConstants.CENTER);
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 24));
        baslik.setForeground(new Color(21, 39, 70));
        panel.add(baslik, BorderLayout.NORTH);

        JPanel kart = new JPanel(new GridLayout(3, 1, 10, 10));
        kart.setBackground(Color.WHITE);
        kart.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 226, 236), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JButton btnYonetici = new JButton("Yönetici Paneli");
        JButton btnKullanici = new JButton("Kullanıcı Paneli");
        JLabel aciklama = new JLabel("Yönetici düzenler, kullanıcı sadece görüntüler.", SwingConstants.CENTER);

        btnYonetici.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnKullanici.setFont(new Font("Segoe UI", Font.BOLD, 15));
        aciklama.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnYonetici.addActionListener(e -> {
            String sifre = JOptionPane.showInputDialog(giris, "Yönetici şifresi:", "Admin Girişi", JOptionPane.PLAIN_MESSAGE);
            if ("admin123".equals(sifre)) {
                giris.dispose();
                new EmlakUygulamasi(true);
            } else if (sifre != null) {
                JOptionPane.showMessageDialog(giris, "Şifre hatalı! Varsayılan şifre: admin123", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnKullanici.addActionListener(e -> kullaniciGirisPenceresi(giris));

        kart.add(btnYonetici);
        kart.add(btnKullanici);
        kart.add(aciklama);
        panel.add(kart, BorderLayout.CENTER);

        giris.setContentPane(panel);
        giris.setVisible(true);
    }

    private static void kullaniciGirisPenceresi(JFrame anaGiris) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField kullaniciAdi = new JTextField();
        JPasswordField sifre = new JPasswordField();
        panel.add(new JLabel("Kullanıcı adı:"));
        panel.add(kullaniciAdi);
        panel.add(new JLabel("Şifre:"));
        panel.add(sifre);

        Object[] secenekler = {"Giriş Yap", "Kayıt Ol", "Vazgeç"};
        int cevap = JOptionPane.showOptionDialog(anaGiris, panel, "Kullanıcı Girişi",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, secenekler, secenekler[0]);

        if (cevap == 2 || cevap == JOptionPane.CLOSED_OPTION) return;

        String ad = kullaniciAdi.getText().trim();
        String pass = new String(sifre.getPassword()).trim();
        if (ad.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(anaGiris, "Kullanıcı adı ve şifre boş bırakılamaz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cevap == 1) {
            if (kullaniciVarMi(ad)) {
                JOptionPane.showMessageDialog(anaGiris, "Bu kullanıcı adı zaten kayıtlı.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            kullaniciKaydet(ad, pass);
            JOptionPane.showMessageDialog(anaGiris, "Kayıt başarılı. Kullanıcı paneli açılıyor.");
            anaGiris.dispose();
            new EmlakUygulamasi(false, ad);
        } else if (cevap == 0) {
            if (kullaniciDogrula(ad, pass)) {
                anaGiris.dispose();
                new EmlakUygulamasi(false, ad);
            } else {
                JOptionPane.showMessageDialog(anaGiris, "Kullanıcı adı veya şifre hatalı.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static boolean kullaniciVarMi(String ad) {
        File dosya = new File(KULLANICI_DOSYASI);
        if (!dosya.exists()) return false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                String[] p = satir.split("\\|", -1);
                if (p.length >= 1 && p[0].equalsIgnoreCase(ad)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean kullaniciDogrula(String ad, String sifre) {
        File dosya = new File(KULLANICI_DOSYASI);
        if (!dosya.exists()) return false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                String[] p = satir.split("\\|", -1);
                if (p.length >= 2 && p[0].equalsIgnoreCase(ad) && p[1].equals(sifre)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static void kullaniciKaydet(String ad, String sifre) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(KULLANICI_DOSYASI, true), StandardCharsets.UTF_8))) {
            pw.println(ad.replace("|", " ") + "|" + sifre.replace("|", " "));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Kullanıcı kaydedilemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static boolean kullaniciSifresiDegistir(String ad, String yeniSifre) {
        File dosya = new File(KULLANICI_DOSYASI);
        if (!dosya.exists()) return false;
        List<String> satirlar = new ArrayList<>();
        boolean bulundu = false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                String[] p = satir.split("\\|", -1);
                if (p.length >= 2 && p[0].equalsIgnoreCase(ad)) {
                    satirlar.add(p[0].replace("|", " ") + "|" + yeniSifre.replace("|", " "));
                    bulundu = true;
                } else {
                    satirlar.add(satir);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Şifre okunamadı: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!bulundu) return false;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dosya), StandardCharsets.UTF_8))) {
            for (String satir : satirlar) pw.println(satir);
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Şifre güncellenemedi: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static class ZiyaretTalebi {
        String kullaniciAdi;
        int emlakId;
        String emlakBaslik;
        String konum;

        ZiyaretTalebi(String kullaniciAdi, int emlakId, String emlakBaslik, String konum) {
            this.kullaniciAdi = kullaniciAdi == null ? "Kullanıcı" : kullaniciAdi;
            this.emlakId = emlakId;
            this.emlakBaslik = emlakBaslik == null ? "" : emlakBaslik;
            this.konum = konum == null ? "" : konum;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmlakUygulamasi::girisEkraniGoster);
    }
}
