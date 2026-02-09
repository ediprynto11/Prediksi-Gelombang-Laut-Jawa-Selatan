#!/usr/bin/env python3
# app_predict.py
from flask import Flask, request, jsonify
import requests
import pandas as pd
import numpy as np
import joblib
from tensorflow.keras.models import load_model
from datetime import datetime, timedelta
import pytz
import traceback

app = Flask(__name__)

# -------------------------
# Config / artifacts
# -------------------------
LSTM_MODEL_PATH = "lstm_seq2seq.keras"
SAFETY_TODAY_PATH = "safety_clf_today.pkl"
SAFETY_72_PATH = "safety_clf_72.pkl"
SCALER_PATH = "scaler.pkl"

TIMESTEPS = 48
OUT_STEPS = 72
OUT_FEATURES = 3  # wave_height_m, wind_speed_mps, precip_mm

# Feature order must match training:
FEATURES = [
    'wave_height_m', 'wave_period_s', 'wind_speed_mps',
    'wind_dir_sin', 'wind_dir_cos', 'temp_c', 'pressure_hpa', 'precip_mm'
]

# Simple region -> lat/lon table (extend as needed)
REGION_COORDS = {
    "laut jawa": (-6.0, 110.5),
    "laut selatan jawa": (-9.0, 110.0),
    "laut natuna": (4.0, 108.0),
    "laut flores": (-8.0, 122.0),
    "samudera hindia": (-10.0, 105.0)
}

# -------------------------
# Load models
# -------------------------
print("🔹 Loading model artifacts...")
lstm_model = load_model(LSTM_MODEL_PATH, compile=False)
clf_today = joblib.load(SAFETY_TODAY_PATH)
clf_72 = joblib.load(SAFETY_72_PATH)
scaler = joblib.load(SCALER_PATH)
print("✅ Models & scaler loaded")

# -------------------------
# Helpers
# -------------------------
def get_coords_from_region(region_name):
    if not region_name:
        return None
    key = region_name.strip().lower()
    return REGION_COORDS.get(key)

def fetch_data(lat, lon, hours_back=48):
    """
    Fetch marine + weather hourly data for the given range.
    We'll request slightly more than 24h (e.g., 48h) to be safe then take the most recent 24.
    Returns merged DataFrame indexed by timestamp with required columns.
    """

    jakarta = pytz.timezone("Asia/Jakarta")
    today_jkt = datetime.now(jakarta).date()
    start = today_jkt - timedelta(days=3)
    end = today_jkt  # ambil sampai hari ini
    print(f"📅 Fetching data start={start} end={end} (Jakarta)")

    marine_url = (
        f"https://marine-api.open-meteo.com/v1/marine?"
        f"latitude={lat}&longitude={lon}"
        f"&start_date={start}&end_date={end}"
        f"&hourly=wave_height,wave_direction,wave_period"
        f"&timezone=Asia%2FJakarta"
    )
    # Weather endpoint (wind, temp, pressure, precip, humidity)
    weather_url = (
        f"https://api.open-meteo.com/v1/forecast?"
        f"latitude={lat}&longitude={lon}"
        f"&start_date={start}&end_date={end}"
        f"&hourly=temperature_2m,pressure_msl,relative_humidity_2m,"
        f"wind_speed_10m,wind_direction_10m,precipitation"
        f"&timezone=Asia%2FJakarta"
    )

    r1 = requests.get(marine_url, timeout=20)
    r2 = requests.get(weather_url, timeout=20)
    r1.raise_for_status(); r2.raise_for_status()
    jm = r1.json()
    jw = r2.json()

    mdf = pd.DataFrame({
        'timestamp': jm['hourly']['time'],
        'wave_height_m': jm['hourly'].get('wave_height', []),
        'wave_dir_deg': jm['hourly'].get('wave_direction', []),
        'wave_period_s': jm['hourly'].get('wave_period', [])
    })
    wdf = pd.DataFrame({
        'timestamp': jw['hourly']['time'],
        'temp_c': jw['hourly'].get('temperature_2m', []),
        'pressure_hpa': jw['hourly'].get('pressure_msl', []),
        'humidity_pct': jw['hourly'].get('relative_humidity_2m', []),
        'wind_speed_mps': jw['hourly'].get('wind_speed_10m', []),
        'wind_dir_deg_weather': jw['hourly'].get('wind_direction_10m', []),
        'precip_mm': jw['hourly'].get('precipitation', [])
    })

    mdf['timestamp'] = pd.to_datetime(mdf['timestamp'])
    wdf['timestamp'] = pd.to_datetime(wdf['timestamp'])

    # Merge nearest timestamps
    df = pd.merge_asof(
        mdf.sort_values('timestamp'),
        wdf.sort_values('timestamp'),
        on='timestamp',
        direction='nearest',
        tolerance=pd.Timedelta('30min')
    )

    # prefer wind_dir from weather if present
    if 'wind_dir_deg_weather' in df.columns:
         df['wind_dir_deg'] = df['wind_dir_deg_weather'].fillna(0.0)
    else:
        df['wind_dir_deg'] = 0.0

    df = df[['timestamp','wave_height_m','wave_period_s','wave_dir_deg',
             'wind_speed_mps','temp_c','pressure_hpa','humidity_pct','precip_mm']]

    # resample hourly and interpolate
    df = df.set_index('timestamp').resample('1H').mean()
    df = df.interpolate(limit_direction='both')

    return df

def wind_dir_to_sincos(df, col='wind_dir_deg'):
    vals = df[col].fillna(0.0).values
    df['wind_dir_sin'] = np.sin(np.deg2rad(vals))
    df['wind_dir_cos'] = np.cos(np.deg2rad(vals))
    return df

def make_sequence_for_model(df):
    """
    df: DataFrame indexed hourly, must contain columns used in FEATURES.
    Return scaled numpy array shape (1, TIMESTEPS, n_features)
    """
    # ensure features exist
    for f in ['wave_height_m','wave_period_s','wind_speed_mps','wind_dir_deg','temp_c','pressure_hpa','precip_mm']:
        if f not in df.columns:
            df[f] = 0.0

    df = wind_dir_to_sincos(df, 'wind_dir_deg')

    # keep only FEATURES, take last TIMESTEPS rows
    seq_df = df[FEATURES].copy().tail(TIMESTEPS)
    if len(seq_df) < TIMESTEPS:
        raise ValueError(f"Not enough hourly data: need {TIMESTEPS}, got {len(seq_df)}")

    arr = seq_df.values  # (TIMESTEPS, n_features)
    scaled = scaler.transform(arr)  # scaler expects 2D (n_samples, n_features)
    return scaled.reshape(1, TIMESTEPS, scaled.shape[1])

def make_feat_for_clf(scaled_seq):
    # scaled_seq shape: (1, TIMESTEPS, n_features)
    last = scaled_seq[:, -1, :]               # (1, n_features)
    mean = scaled_seq.mean(axis=1)            # (1, n_features)
    std = scaled_seq.std(axis=1)              # (1, n_features)
    feat = np.concatenate([last, mean, std], axis=1)  # (1, n_features*3)
    return feat

def rule_label_text(code):
    # training used labels: 0=Aman,1=Waspada,2=Bahaya (but earlier code might differ)
    # We'll map robustly:
    mapping = {1: "Aman", 2: "Waspada", 3: "Berbahaya"}
    return mapping.get(int(code), "Unknown")

@app.route("/predict", methods=["POST"])
def predict_region():
    try:
        payload = request.get_json(force=True)
        if not payload:
            return jsonify({"error": "Empty request"}), 400

        # Resolve coordinates
        if "region" in payload:
            coords = get_coords_from_region(payload.get("region"))
            if coords is None:
                return jsonify({"error": "Unknown region"}), 400
            lat, lon = coords
            region_name = payload.get("region")
        elif "lat" in payload and "lon" in payload:
            lat = float(payload.get("lat"))
            lon = float(payload.get("lon"))
            region_name = payload.get("name", f"{lat},{lon}")
        else:
            return jsonify({"error": "Provide 'region' or ('lat' and 'lon')"}), 400

        # Fetch data (history)
        df = fetch_data(lat, lon)

        # ---------------------
        # Pastikan kolom wind_dir_deg selalu ada
        # ---------------------
        if 'wind_dir_deg' not in df.columns:
            df['wind_dir_deg'] = 0.0

        df = wind_dir_to_sincos(df)  # buat wind_dir_sin & wind_dir_cos

        jakarta = pytz.timezone("Asia/Jakarta")
        today_jkt = datetime.now(jakarta).date()

        # ---------------------
        # Real-time data hari ini per 3 jam
        # ---------------------
        df_today = df[df.index.date == today_jkt]
        df_today_3h = df_today.resample('3H').mean().interpolate(limit_direction='both')
        today_3h = []
        for ts, row in df_today_3h.iterrows():
            today_3h.append({
                "timestamp": ts.isoformat(),
                "wave_height_m": float(row['wave_height_m']),
                "wind_speed_mps": float(row['wind_speed_mps']),
                "precip_mm": float(row['precip_mm'])
            })

        # ---------------------
        # LSTM prediction 72h
        # ---------------------
        seq = make_sequence_for_model(df)
        decoder_input = np.zeros((1, OUT_STEPS, OUT_FEATURES))
        preds = lstm_model.predict([seq, decoder_input])[0]

        # Buat timestamps prediksi mulai BESOK 00:00 WIB
        next_midnight = (datetime.now(jakarta) + timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
        pred_times = [(next_midnight + timedelta(hours=i)).astimezone(jakarta) for i in range(OUT_STEPS)]

        # ✅ Cetak di sini, setelah pred_times sudah ada
        print(f"len(preds)={len(preds)}, len(pred_times)={len(pred_times)}")
        print(pred_times[0], "→", pred_times[-1])

        # Prediksi per 3 jam (72h → 24 entry)
        predictions_3h_3days = []
        for i in range(0, len(preds), 3):
            ts = pred_times[i]
            row = preds[i]
            predictions_3h_3days.append({
                "timestamp": ts.isoformat(),
                "wave_height_m": float(row[0]),
                "wind_speed_mps": float(row[1]),
                "precip_mm": float(row[2])
            })

            print(f"predictions_3h_3days count = {len(predictions_3h_3days)}")
            print("Range waktu:", predictions_3h_3days[0]["timestamp"], "→", predictions_3h_3days[-1]["timestamp"])

        # ---------------------
        # Safety classifier
        # ---------------------
        feat_clf = make_feat_for_clf(seq)
        safe_today_code = clf_today.predict(feat_clf)[0]
        safe_72_code = clf_72.predict(feat_clf)[0]

        # ---------------------
        # Daily worst per rule
        # ---------------------
        pred_df = pd.DataFrame(preds, columns=['wave_height_m','wind_speed_mps','precip_mm'])
        pred_df['timestamp'] = pred_times
        pred_df['date'] = [ts.date() for ts in pred_times]

        def rule_label_row(wave, wind):
            if wave >= 2.5 or wind >= 12.0: return 2
            if wave >= 1.0 or wind >= 7.0: return 1
            return 0

        pred_df['label'] = pred_df.apply(lambda r: rule_label_row(r['wave_height_m'], r['wind_speed_mps']), axis=1)
        daily = pred_df.groupby('date')['label'].max().reset_index()
        daily_list = [{"date": str(r['date']), "worst_label": int(r['label']), "text": rule_label_text(int(r['label']))} for _, r in daily.iterrows()]

        # ---------------------
        # Response JSON
        # ---------------------
        resp = {
            "region": region_name,
            "coords": {"lat": lat, "lon": lon},
            "today_3h": today_3h,
            "predictions_3h_3days": predictions_3h_3days,
            "daily_worst_3days": daily_list,
            "safety": {
                "today_model": rule_label_text(safe_today_code),
                "72h_model": rule_label_text(safe_72_code)
            }
        }
        return jsonify(resp)

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "API Cuaca Laut Jawa Selatan aktif ✅"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5050)



