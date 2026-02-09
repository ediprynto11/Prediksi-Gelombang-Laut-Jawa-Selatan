# 🌊 Prediksi Gelombang Laut Jawa Selatan

Aplikasi **prediksi kondisi gelombang laut Jawa Selatan** berbasis **Machine Learning (LSTM)** yang terintegrasi antara **Backend Python (Flask API)** dan **Aplikasi Android (Kotlin)**.

Proyek ini dibuat untuk memberikan **perkiraan 72 jam ke depan** berupa **tinggi gelombang**, **kecepatan angin**, serta **klasifikasi tingkat keamanan pelayaran**.

---

## 🚀 Fitur Utama

### 🔹 Backend (Python + Flask)

* Prediksi cuaca laut **72 jam ke depan**
* Model **LSTM Seq2Seq**
* Input data historis **48 jam**
* Output prediksi:

  * 🌊 Tinggi gelombang (meter)
  * 💨 Kecepatan angin (m/s)
  * 🌧 Curah hujan (mm)
* Klasifikasi tingkat keamanan laut:

  * ✅ Aman
  * ⚠️ Waspada
  * ❌ Berbahaya
* REST API menggunakan **Flask**

### 🔹 Frontend (Android – Kotlin)

* Aplikasi Android native
* Konsumsi REST API Flask
* Menampilkan:

  * Grafik tinggi gelombang realtime
  * Prediksi cuaca laut per jam
  * Status keamanan pelayaran
* Arsitektur sederhana (**Repository Pattern**)

---

## 🗂 Struktur Repository

```text
Prediksi-Gelombang-Laut-Jawa-Selatan/
│
├── backend-python/
│   ├── app_predict.py          # Flask API (Inference)
│   ├── prepare_and_label.py    # Preprocessing & labeling data
│   ├── train_models.py         # Training LSTM & classifier
│   ├── requirements.txt
│   └── README.md               # (opsional)
│
├── android-app/
│   ├── app/
│   ├── gradle/
│   ├── build.gradle
│   └── settings.gradle
│
├── .gitignore
└── README.md
```

---

## 🧠 Machine Learning Model

### 🔹 Model Utama

* **LSTM Seq2Seq**
* Input: **48 jam data historis**
* Output: **72 jam prediksi (hourly)**

### 🔹 Fitur Input

* `wave_height_m`
* `wave_period_s`
* `wind_speed_mps`
* `wind_dir_sin`
* `wind_dir_cos`
* `temp_c`
* `pressure_hpa`
* `precip_mm`
* `wave_energy`
* `wind_power`

### ⚠️ Klasifikasi Keamanan Laut

* Model: **Random Forest**
* Kategori:

  * Aman
  * Waspada
  * Berbahaya

---

## 🔌 API Endpoint

### `POST /predict`

Digunakan oleh aplikasi Android untuk mengambil prediksi cuaca laut.

### 📥 Request Body

```json
{
  "data": [
    {
      "timestamp": "2025-11-14T00:00:00",
      "wave_height_m": 1.2,
      "wave_period_s": 6,
      "wind_speed_mps": 5,
      "wind_dir_sin": 0.5,
      "wind_dir_cos": 0.8,
      "temp_c": 28,
      "pressure_hpa": 1012,
      "precip_mm": 0,
      "wave_energy": 7200,
      "wind_power": 125
    }
  ]
}
```

⚠️ **Minimal data yang dikirim: 48 baris (48 jam)**

### 📤 Response

```json
{
  "status": "success",
  "safety_today": "AMAN",
  "safety_72h": "WASPADA",
  "predictions_hourly_72h": [
    {
      "timestamp": "2025-11-14T01:00:00",
      "wave_height_m": 1.3,
      "wind_speed_mps": 5.4,
      "precip_mm": 0.1
    }
  ]
}
```

---

## 🛠️ Menjalankan Backend (Python)

```bash
cd backend-python
pip install -r requirements.txt
python app_predict.py
```

Server akan berjalan di:

```
http://localhost:5051
```

---

## 📱 Menjalankan Aplikasi Android

1. Buka folder `android-app` di **Android Studio**
2. Ubah `baseUrl` di kode Kotlin menjadi:

   ```
   http://IP_SERVER:5051
   ```
3. Jalankan aplikasi di emulator atau device fisik

---

## 🔐 Keamanan Repository

File berikut **tidak di-upload ke GitHub**:

* Model Machine Learning (`*.pkl`, `*.keras`, `*.h5`)
* Dataset & hasil training
* File build Android (`build/`, `.gradle/`, `*.apk`)

Semua sudah diatur melalui file **`.gitignore`**.

---

## 📌 Catatan Pengembangan

Proyek ini dikembangkan untuk **pembelajaran dan riset**, dan masih dapat dikembangkan lebih lanjut, seperti:

* Integrasi data **BMKG**
* Deployment ke cloud (**Docker / VPS**)

---

## 👨‍💻 Author

**Edi Priyanto**
Mahasiswa | IoT & AI Enthusiast
📌 Fokus: Android, IoT, Machine Learning

🔗 GitHub: [https://github.com/ediprynto11](https://github.com/ediprynto11)
🔗 LinkedIn: [https://www.linkedin.com/in/edi-priyanto-b94b02317](https://www.linkedin.com/in/edi-priyanto-b94b02317)
