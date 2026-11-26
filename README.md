# Tashbich v1.2
Tasbih digital Android native Java.

- Tiga mode: bebas, otomatis, dan dzikir manual.
- Visual untaian manik melengkung yang bergeser tiap hitungan; manik terhitung berwarna (ungu → pink → emas tiap 33), sisanya putih mutiara.
- Mode manual menambah hitungan saat tombol/manik ditekan.
- Mode otomatis berjalan pada interval 0,5 atau 1 detik.
- Pilihan dzikir dengan teks Arab.
- Suara: "ting" manik tiap hitungan, lonceng tiap 33, lonceng panjang tiap 99 (sintesis asli lewat `tools/gen_sounds.py`, terinspirasi karakter suara video referensi; bukan rekaman aslinya). Tanpa TTS.
- Target opsional dan penyimpanan hitungan lokal SQLite.

## Build
Push seluruh isi folder project ke repository GitHub. Workflow ada di `.github/workflows/build.yml`. Jalankan tab Actions, lalu unduh artifact `Tashbich-debug`.
